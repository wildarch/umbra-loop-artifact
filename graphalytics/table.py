#!/usr/bin/env python3
"""
Generate a LaTeX table comparing LDBC Graphalytics processing times across different platforms
"""
import json
from pathlib import Path
import statistics
import sys

REPORT_DIR = Path(__file__).parent

ALGORITHMS = ['BFS', 'CDLP', 'PR', 'SSSP', 'WCC']

# Ordered list of datasets for the table columns
DATASETS = [
    'wiki-Talk',
    'cit-Patents',
    'kgs',
    'dota-league',
    'graph500-22',
    #'datagen-7_5-fb',
    #'datagen-7_6-fb',
    #'datagen-7_7-zf',
    'datagen-7_8-zf',
    'datagen-7_9-fb',
]

PLATFORM_STRATEGIES = [
    ('Umbra', 'external-driver'),
    ('Umbra', 'using-key'),
    ('Umbra', 'loop'),
    ('DuckDB', 'unknown'),
    ('AvantGraph', 'unknown'),
]

STRATEGY_NAME = {
    'external-driver': 'ext',
    'using-key': '\\texttt{KEY}',
    'loop': '\\texttt{LOOP}',
}

USING_KEY_ALGOS = ['SSSP', 'WCC']

UNWEIGHTED_DATASETS = [
    'wiki-Talk',
    'cit-Patents',
    'graph500-22',
]

def heatmap_cell(pct):
    """Return LaTeX cell with background colour for a speedup ratio.

    Green when loop is faster (ratio > 1), red when loop is slower (ratio < 1).
    Full intensity at 1.8x speedup or slowdown.
    """
    if pct >= 1:
        diff = pct - 1
        strength = min(diff / 0.8, 1.0)
        intensity = int(strength * 70)
        return f"\\cellcolor{{green!{intensity}!white}}{pct:.1f}x"
    else:
        diff = (1 / pct) - 1
        strength = min(diff / 0.8, 1.0)
        intensity = int(strength * 70)
        return f"\\cellcolor{{red!{intensity}!white}}{pct:.1f}x"


def parse_json(filepath):
    """Extract (platform, strategy, algorithm, dataset, processing_time) tuples and attempted combos."""
    with open(filepath, 'r') as f:
        data = json.load(f)

    platform = data.get('system', {}).get('platform', {}).get('name', 'unknown')
    configs = data['benchmark']['configurations']
    strategy = configs.get('platform.umbra.iteration-strategy', 'unknown')

    jobs = data['result']['jobs']
    runs = data['result']['runs']

    records = []      # (platform, strategy, algorithm, dataset, processing_time) — successful only
    attempted = set() # (platform, strategy, algorithm, dataset) — all jobs regardless of success

    for job in jobs.values():
        algorithm = job['algorithm']
        dataset = job['dataset']
        attempted.add((platform, strategy, algorithm, dataset))
        for run_id in job['runs']:
            run = runs[run_id]
            if run['success'] == 'true':
                proc_time = float(run['processing_time'])
                records.append((platform, strategy, algorithm, dataset, proc_time))

    return records, attempted

def compute_medians(records):
    """Compute median processing time per (platform, strategy, algorithm, dataset)."""
    buckets = {}
    for platform, strategy, algorithm, dataset, proc_time in records:
        key = (platform, strategy, algorithm, dataset)
        buckets.setdefault(key, []).append(proc_time)

    medians = {}
    for key, times in buckets.items():
        medians[key] = statistics.median(times)

    return medians

def render_cell(medians, alg, platform, strategy, dataset):
    ptime = medians.get((platform, strategy, alg, dataset))
    print(f'{ptime:.1f}', end='')

def render_cell_heat(medians, alg, platform, strategy, dataset):
    samples = []
    for p, s in PLATFORM_STRATEGIES:
        ptime = medians.get((p, s, alg, dataset))
        if ptime:
            samples.append(ptime)
    min_ptime = min(samples)
    max_ptime = max(samples)
    med_ptime = statistics.median(samples)

    my_ptime = medians.get((platform, strategy, alg, dataset))
    if not my_ptime or not med_ptime:
        print(f'MIS', end='')
    elif my_ptime <= med_ptime:
        # Faster than median
        strength = 1 - (my_ptime - min_ptime) / (med_ptime - min_ptime)
        intensity = int(strength * 70)
        print(f'\\cellcolor{{green!{intensity}!white}}{my_ptime:.1f}', end='')
    else:
        # Slower than median
        strength = 1 - (max_ptime - my_ptime) / (max_ptime - med_ptime)
        intensity = int(strength * 70)
        print(f'\\cellcolor{{red!{intensity}!white}}{my_ptime:.1f}', end='')

def render_line(medians, alg, nrows, platform, strategy):
    if strategy == 'unknown':
        print(f"& {platform}", end='')
    elif platform == 'Umbra' and strategy == 'loop':
        print(f"& \\textbf{{{platform} (\\texttt{{LOOP}})}}", end='')
    else:
        print(f"& {platform} ({STRATEGY_NAME[strategy]})", end='')

    for dataset in DATASETS:
        print(" & ", end='')
        if dataset in UNWEIGHTED_DATASETS and alg == 'SSSP':
            # SSSP needs edge weights, does not run on this dataset
            if (platform, strategy) == PLATFORM_STRATEGIES[0]:
                # First row for this dataset, mark as N/A for all strategies
                print("\\multirow{", nrows, "}{*}{\\textit{N/A}}", end='')
        else:
            render_cell_heat(medians, alg, platform, strategy, dataset)
    
    print(' \\\\')

def render_table(medians):
    col_spec = '|l|l|' + 'c|' * len(DATASETS)
    print(f"\\begin{{tabular}}{{{col_spec}}}")
    print("\\hline")

    # Header
    print("    \\rotatebox{90}{Algorithm} & System", end='')
    for ds in DATASETS:
        ds = ds.replace('_', '\\_')
        print(" & \\rotatebox{90}{", end='')
        print(ds, end='')
        print("\\ }", end='')
    print(" \\\\")
    print("\\hline")

    for alg in ALGORITHMS:
        no_using_key = alg not in USING_KEY_ALGOS

        nrows = len(PLATFORM_STRATEGIES)
        if no_using_key:
            nrows -= 1

        print("\\multirow{", nrows, "}{*}{ \\rotatebox{90}{", alg, "} }", end='')
        for platform, strategy in PLATFORM_STRATEGIES:
            if strategy == 'using-key' and no_using_key: 
                continue
            render_line(medians, alg, nrows, platform, strategy)
        print("    \\hline")

    print("\\end{tabular}")

def main():
    # Parse JSON files
    all_records = []
    all_attempted = set()  # (platform, strategy, algorithm, dataset) combos that exist in jobs
    for json_path in REPORT_DIR.glob('graphalytics/results/*.json'):
        #print(f"found JSON: {json_path}", file=sys.stderr)
        records, attempted = parse_json(json_path)
        all_records.extend(records)
        all_attempted.update(attempted)

    if not all_records:
        print("Error: No records found in report JSON files.", file=sys.stderr)
        sys.exit(1)

    medians = compute_medians(all_records)

    render_table(medians)

if __name__ == '__main__':
    main()
