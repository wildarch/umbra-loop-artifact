#!/usr/bin/env python3
from pathlib import Path
import argparse
import subprocess
import sys

repo_root = Path(__file__).resolve().parent.parent.parent

def main():
    parser = argparse.ArgumentParser(description="Compile FlowLog algorithms")
    parser.add_argument("--algorithms", required=True,
                        help="Comma-separated list of algorithm names, e.g. andersen,dyck")
    parser.add_argument("--algorithms-dir",
                        default=repo_root / "datalog" / "perf",
                        type=Path,
                        help="Directory containing .dl algorithm files")
    parser.add_argument("--compiler",
                        default=repo_root / "datalog" / "flowlog" / "flowlog-compiler",
                        type=Path,
                        help="Path to the flowlog-compiler binary")
    parser.add_argument("--output-dir",
                        default=repo_root / "datalog" / "perf" / "compiled",
                        type=Path,
                        help="Directory to store compiled binaries")
    args = parser.parse_args()

    algo_names = [name.strip() for name in args.algorithms.split(",")]

    if not args.algorithms_dir.is_dir():
        print(f"Error: algorithms directory not found: {args.algorithms_dir}", file=sys.stderr)
        sys.exit(1)

    if not args.compiler.is_file():
        print(f"Error: flowlog compiler not found: {args.compiler}", file=sys.stderr)
        sys.exit(1)

    args.output_dir.mkdir(parents=True, exist_ok=True)

    for name in algo_names:
        dl_file = args.algorithms_dir / f"{name}.dl"
        if not dl_file.is_file():
            print(f"Warning: {dl_file} not found, skipping {name}", file=sys.stderr)
            continue

        output_binary = args.output_dir / f"{name}-flowlog"
        cmd = [
            str(args.compiler),
            "-D", ".",
            "-F", ".",
            "-o", str(output_binary),
            str(dl_file),
        ]
        print(f"Compiling {name}...")
        result = subprocess.run(cmd, capture_output=True, text=True)
        if result.returncode != 0:
            print(f"Error compiling {name}:", file=sys.stderr)
            print(result.stderr, file=sys.stderr)
            sys.exit(1)
        if result.stdout:
            print(result.stdout)
        print(f"  -> {output_binary}")

    print("Done.")


if __name__ == "__main__":
    main()