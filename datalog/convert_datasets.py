#!/usr/bin/env python3
"""
Convert all .ddin datasets (stored as .zip files) into compressed tar.gz archives.

For each .zip file in the data directory:
  1. Parse the .ddin inside the zip and write CSV files
  2. Pack the CSV files into a .tar.gz archive
  3. Delete the plain CSV files

The script is restart-safe: if a .tar.gz already exists for a dataset,
it is skipped. Interrupt with Ctrl-C at any time; re-running will pick
up where you left off.

Usage:
    python convert_datasets.py                          # uses ./data/
    python convert_datasets.py --datadir /path/to/data  # custom data directory
"""

import argparse
import csv
import io
import os
import re
import sys
import tarfile
import tempfile
import zipfile
from pathlib import Path


# ---------------------------------------------------------------------------
#  Streaming DDIN parser — writes CSV on the fly
# ---------------------------------------------------------------------------

def parse_ddin_stream(file, outdir: str) -> dict[str, int]:
    """
    Parse a .ddin file (opened file-like object) and write rows directly
    to CSV files in *outdir* as they are encountered.

    Returns a mapping from relation name to row count.
    """
    os.makedirs(outdir, exist_ok=True)
    files: dict[str, io.TextIOWrapper] = {}
    writers: dict[str, csv.writer] = {}
    counts: dict[str, int] = {}
    pattern = re.compile(r"insert\s+(\w+)\s*\(([^)]*)\)\s*[,;]?")

    for line in file:
        line = line.strip()
        if not line:
            continue
        if line in ("start;", "commit;") or line.startswith("dump"):
            continue
        for match in pattern.finditer(line):
            relname = match.group(1)
            values_str = match.group(2)
            values = [v.strip() for v in values_str.split(",")]

            # Lazily create CSV writer for each relation
            if relname not in writers:
                f = open(os.path.join(outdir, f"{relname}.csv"), "w", newline="")
                files[relname] = f
                writers[relname] = csv.writer(f)
                counts[relname] = 0
            writers[relname].writerow(values)
            counts[relname] += 1

    # Close all open CSV files
    for f in files.values():
        f.close()

    return counts


def parse_ddin_zip_stream(zip_path: str, outdir: str) -> dict[str, int]:
    """Open a .zip archive containing a .ddin file and stream-parse it."""
    with zipfile.ZipFile(zip_path, "r") as zf:
        ddin_members = [n for n in zf.namelist() if n.endswith(".ddin")]
        if not ddin_members:
            print(f"  Warning: no .ddin file found, skipping", file=sys.stderr)
            return {}
        ddin_name = ddin_members[0]
        with zf.open(ddin_name, "r") as raw:
            text = io.TextIOWrapper(raw, encoding="utf-8")
            return parse_ddin_stream(text, outdir)


# ---------------------------------------------------------------------------
#  tar.gz creation
# ---------------------------------------------------------------------------

def pack_csvs(csv_dir: str, archive_path: str) -> None:
    """Pack all CSV files in *csv_dir* into a .tar.gz archive."""
    with tarfile.open(archive_path, "w:gz") as tar:
        for name in sorted(os.listdir(csv_dir)):
            if name.endswith(".csv"):
                path = os.path.join(csv_dir, name)
                tar.add(path, arcname=name)


# ---------------------------------------------------------------------------
#  Main conversion logic
# ---------------------------------------------------------------------------

def convert_zip(zip_path: str, work_dir: str) -> bool:
    """
    Convert a single .zip dataset.

    Steps:
      1. Stream-parse the .ddin inside the zip, writing CSV files directly
      2. Pack CSVs into <dataset-name>.tar.gz

    Returns True on success, False on failure.
    """
    stem = Path(zip_path).stem  # e.g. "livejournal-reach" from "livejournal-reach.zip"
    archive_name = f"{stem}.tar.gz"
    archive_path = os.path.join(work_dir, archive_name)

    # --- restart check ---
    if os.path.isfile(archive_path):
        print(f"  [{archive_name}] already exists, skipping")
        return True

    print(f"  Parsing {os.path.basename(zip_path)} ...", end=" ", flush=True)

    with tempfile.TemporaryDirectory(prefix=stem, dir=work_dir) as tmp_dir:
        counts = parse_ddin_zip_stream(zip_path, tmp_dir)
        if not counts:
            print("no data found, skipping")
            return False

        print(f"packing {archive_name} ...", end=" ", flush=True)
        pack_csvs(tmp_dir, archive_path)

    total_rows = sum(counts.values())
    print(f"done ({len(counts)} relation(s), {total_rows} rows)")
    return True


# ---------------------------------------------------------------------------
#  Entry point
# ---------------------------------------------------------------------------

def main() -> None:
    parser = argparse.ArgumentParser(
        description="Convert .ddin dataset archives (.zip) into .tar.gz archives."
    )
    parser.add_argument(
        "--datadir",
        default=None,
        help="Directory containing the .zip files (default: <scriptdir>/data)",
    )
    parser.add_argument(
        "--outdir",
        default=None,
        help="Output directory for .tar.gz files (default: same as datadir)",
    )
    args = parser.parse_args()

    # Resolve data directory
    if args.datadir is not None:
        data_dir = args.datadir
    else:
        data_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "data")

    if not os.path.isdir(data_dir):
        print(f"Error: data directory not found: {data_dir}", file=sys.stderr)
        sys.exit(1)

    # Resolve output directory (defaults to data directory)
    out_dir = args.outdir if args.outdir is not None else data_dir
    os.makedirs(out_dir, exist_ok=True)

    # Collect zip files
    zip_files = sorted(
        f for f in os.listdir(data_dir)
        if f.endswith(".zip") and not f.endswith(".zip.1")
    )

    if not zip_files:
        print(f"No .zip files found in {data_dir}", file=sys.stderr)
        sys.exit(0)

    print(f"Found {len(zip_files)} dataset(s) in {data_dir}")
    print(f"Output directory: {out_dir}")
    print()

    processed = 0
    skipped = 0
    failed = 0

    for zip_name in zip_files:
        zip_path = os.path.join(data_dir, zip_name)
        print(f"[{processed + 1}/{len(zip_files)}] {zip_name}")
        try:
            ok = convert_zip(zip_path, out_dir)
            if ok:
                processed += 1
            else:
                skipped += 1
        except KeyboardInterrupt:
            print("\nInterrupted by user. Exiting cleanly.")
            print(f"Processed: {processed}, skipped: {skipped}, failed: {failed}")
            sys.exit(130)
        except Exception as e:
            print(f"FAILED: {e}", file=sys.stderr)
            failed += 1

    print()
    print(f"All done. Processed: {processed}, skipped: {skipped}, failed: {failed}")


if __name__ == "__main__":
    main()