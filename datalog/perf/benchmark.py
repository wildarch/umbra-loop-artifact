#!/usr/bin/env python3
from pathlib import Path
import argparse
import csv
import re
import shutil
import subprocess
import sys
import tarfile
import tempfile
import threading
import time
import psutil

repo_root = Path(__file__).resolve().parent.parent.parent


def run_souffle(args, tmp, algorithm):
    """Run Souffle benchmark and return list of (exec_time, verify) tuples."""
    binary = args.compiled_dir / f"{algorithm}-souffle"
    if not binary.is_file():
        print(f"Error: compiled binary not found: {binary}", file=sys.stderr)
        return [("ERR", "")] * args.reps

    print(f"Copying {binary.name}...")
    shutil.copy(binary, tmp / binary.name)

    relation_pattern = re.compile(r"^(\S+)\s+(\d+)$", re.MULTILINE)
    results = []
    for i in range(args.reps):
        print(f"  Run {i + 1}/{args.reps}...")

        start = time.time()
        proc = subprocess.run(
            [str(tmp / binary.name), "-j", str(args.threads)],
            capture_output=True, text=True, cwd=tmp,
        )
        elapsed = time.time() - start

        if proc.returncode != 0:
            print(f"Error: algorithm failed on run {i + 1}", file=sys.stderr)
            print(proc.stderr, file=sys.stderr)
            remaining = args.reps - len(results)
            results.extend([("ERR", "")] * remaining)
            break

        exec_time = elapsed

        # Parse relation counts from stdout
        counts = []
        for match in relation_pattern.finditer(proc.stdout):
            counts.append(f"{match.group(1)}:{match.group(2)}")
        counts.sort()
        verify = ":".join(counts) if counts else ""

        results.append((exec_time, verify))
        print(f"    {exec_time:.2f}s  verify={verify}")
    return results

def run_flowlog(args, tmp, algorithm):
    """Run FlowLog benchmark and return list of (exec_time, verify) tuples."""
    binary = args.compiled_dir / f"{algorithm}-flowlog"
    if not binary.is_file():
        print(f"Error: compiled binary not found: {binary}", file=sys.stderr)
        return [("ERR", "")] * args.reps

    # Snapshot existing CSV files before the run so we know what are the output CSVs
    before_csvs = set(tmp.glob("*.csv"))

    print(f"Copying {binary.name}...")
    shutil.copy(binary, tmp / binary.name)

    time_pattern = re.compile(r"([\d.]+)s:\s+Dataflow executed")
    results = []
    for i in range(args.reps):
        print(f"  Run {i + 1}/{args.reps}...")

        MEM_LIMIT = 80 * 1024 * 1024 * 1024  # 80 GB in bytes
        oom = threading.Event()

        proc = subprocess.Popen(
            [str(tmp / binary.name), "-w", str(args.threads)],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, cwd=tmp,
        )

        def monitor(proc, oom):
            p = psutil.Process(proc.pid)
            while proc.poll() is None:
                try:
                    rss = p.memory_info().rss
                    if rss > MEM_LIMIT:
                        oom.set()
                        proc.kill()
                except (psutil.NoSuchProcess, psutil.AccessDenied):
                    pass
                time.sleep(1)

        monitor_thread = threading.Thread(target=monitor, args=(proc, oom), daemon=True)
        monitor_thread.start()

        stdout, stderr = proc.communicate()
        monitor_thread.join(timeout=5)

        if oom.is_set():
            print(f"    OOM (RSS exceeded 80G)")
            results.append(("OOM", ""))
            continue

        if proc.returncode != 0:
            print(f"Error: algorithm failed on run {i + 1}", file=sys.stderr)
            print(stderr.decode(), file=sys.stderr)
            remaining = args.reps - len(results)
            results.extend([("ERR", "")] * remaining)
            break

        match = time_pattern.search(stdout.decode())
        if not match:
            print(f"Error: could not find timing line in output", file=sys.stderr)
            print(stdout.decode(), file=sys.stderr)
            remaining = args.reps - len(results)
            results.extend([("ERR", "")] * remaining)
            break

        exec_time = float(match.group(1))

        # Find new CSV files (output relations) and read them
        after_csvs = set(tmp.glob("*.csv"))
        new_csvs = after_csvs - before_csvs
        counts = []
        for csv_file in sorted(new_csvs, key=lambda p: p.stem):
            count = csv_file.read_text().strip()
            counts.append(f"{csv_file.stem}:{count}")
            csv_file.unlink()  # delete so next run starts clean
        verify = ":".join(counts) if counts else ""

        results.append((exec_time, verify))
        print(f"    {exec_time}s  verify={verify}")
    return results


def run_umbra(args, tmp, algorithm):
    """Run Umbra benchmark and return list of (exec_time, verify) tuples."""
    umbra_sql = args.umbra_sql
    if not umbra_sql.is_file():
        print(f"Error: Umbra SQL binary not found: {umbra_sql}", file=sys.stderr)
        return [("ERR", "")] * args.reps

    sql_file = args.perf_dir / f"{algorithm}.sql"
    if not sql_file.is_file():
        print(f"Error: SQL file not found: {sql_file}", file=sys.stderr)
        return [("ERR", "")] * args.reps

    time_pattern = re.compile(r"exec:\s+([\d.]+)\s+s")
    relation_header = re.compile(r"^relation tupcount$", re.MULTILINE)
    results = []
    for i in range(args.reps):
        print(f"  Run {i + 1}/{args.reps}...")
        result = subprocess.run(
            [str(umbra_sql), "", str(sql_file)],
            capture_output=True, text=True, cwd=tmp,
            env = {'PARALLEL': str(args.threads)}
        )
        if result.returncode != 0:
            print(f"Error: Umbra failed on run {i + 1}", file=sys.stderr)
            print(result.stderr, file=sys.stderr)
            remaining = args.reps - len(results)
            results.extend([("ERR", "")] * remaining)
            break

        matches = time_pattern.findall(result.stderr)
        if not matches:
            print(f"Error: could not find timing line in output", file=sys.stderr)
            print(result.stdout, file=sys.stderr)
            remaining = args.reps - len(results)
            results.extend([("ERR", "")] * remaining)
            break

        exec_time = float(matches[-1])

        # Parse relation counts from stdout
        verify = ""
        rel_match = relation_header.search(result.stdout)
        if rel_match:
            lines = result.stdout[rel_match.end():].strip().splitlines()
            rel_counts = []
            for line in lines:
                parts = line.strip().split()
                if len(parts) == 2:
                    rel_counts.append((parts[0], parts[1]))
            rel_counts.sort(key=lambda x: x[0])
            verify = ":".join(f"{r}:{c}" for r, c in rel_counts)

        results.append((exec_time, verify))
        print(f"    {exec_time}s  verify={verify}")
    return results


def main():
    parser = argparse.ArgumentParser(description="Benchmark FlowLog/Umbra/Souffle algorithms")
    parser.add_argument("--graph", required=True,
                        help="Graph dataset name, e.g. medium-andersen")
    parser.add_argument("--algorithms", required=True,
                        help="Comma-separated list of algorithm names, e.g. andersen,sssp")
    parser.add_argument("--reps", type=int, default=3,
                        help="Number of repetitions (default: 3)")
    parser.add_argument("--systems", default="flowlog",
                        help="Comma-separated list of systems to benchmark (flowlog,umbra,souffle)")
    parser.add_argument("--compiled-dir",
                        default=repo_root / "datalog" / "perf" / "compiled",
                        type=Path,
                        help="Directory containing compiled algorithm binaries")
    parser.add_argument("--csv-dir",
                        default=repo_root / "datalog" / "csv",
                        type=Path,
                        help="Directory containing compressed graph archives")
    parser.add_argument("--output",
                        default=repo_root / "datalog" / "flowlog" / "results.csv",
                        type=Path,
                        help="Output CSV file path")
    parser.add_argument("--umbra-sql",
                        default=repo_root.parent / "umbra" / "build-release" / "sql",
                        type=Path,
                        help="Path to the Umbra SQL binary")
    parser.add_argument("--perf-dir",
                        default=repo_root / "datalog" / "perf",
                        type=Path,
                        help="Directory containing Umbra SQL benchmark files")
    parser.add_argument("--threads",
                        default=16,
                        type=int,
                        help="Number of threads to use")
    args = parser.parse_args()

    algorithms = [a.strip() for a in args.algorithms.split(",")]

    systems = [s.strip() for s in args.systems.split(",")]
    for s in systems:
        if s not in ("flowlog", "umbra", "souffle"):
            print(f"Error: unknown system '{s}'. Choose from: flowlog, umbra, souffle", file=sys.stderr)
            sys.exit(1)

    graph_archive = args.csv_dir / f"{args.graph}.tar.gz"
    if not graph_archive.is_file():
        print(f"Error: graph archive not found: {graph_archive}", file=sys.stderr)
        sys.exit(1)

    with tempfile.TemporaryDirectory() as tmpdir:
        tmp = Path(tmpdir)

        print(f"Extracting {graph_archive.name}...")
        with tarfile.open(graph_archive, "r:gz") as tar:
            tar.extractall(path=tmp)

        csv_files = list(tmp.glob("*.csv"))
        print(f"  {len(csv_files)} CSV files in working directory")

        args.output.parent.mkdir(parents=True, exist_ok=True)
        write_header = not args.output.is_file()
        if write_header:
            with open(args.output, "w", newline="") as f:
                writer = csv.writer(f)
                writer.writerow(["system", "algorithm", "dataset", "exec_time", "verify"])
            print(f"Created {args.output} with header")

        for algorithm in algorithms:
            print(f"\n=== Algorithm: {algorithm} ===")
            algorithm_results = []
            for system in systems:
                print(f"\nBenchmarking system: {system}")
                if system == "flowlog":
                    results = run_flowlog(args, tmp, algorithm)
                if system == "souffle":
                    results = run_souffle(args, tmp, algorithm)
                elif system == "umbra":
                    results = run_umbra(args, tmp, algorithm)
                for t, v in results:
                    algorithm_results.append((system, algorithm, t, v))

            # Flush results for this algorithm immediately
            with open(args.output, "a", newline="") as f:
                writer = csv.writer(f)
                for system, algorithm_name, t, v in algorithm_results:
                    writer.writerow([system, algorithm_name, args.graph, t, v])
            print(f"Results for {algorithm} flushed to {args.output}")

    # Re-read and print summary
    if args.output.is_file():
        with open(args.output, newline="") as f:
            reader = list(csv.DictReader(f))
        for system in systems:
            sys_results = [r for r in reader if r["system"] == system]
            times = []
            for r in sys_results:
                try:
                    times.append(float(r["exec_time"]))
                except ValueError:
                    pass  # skip ERR/OOM entries
            if times:
                avg_time = sum(times) / len(times)
                print(f"\nAverage time ({system}): {avg_time}s")


if __name__ == "__main__":
    main()
