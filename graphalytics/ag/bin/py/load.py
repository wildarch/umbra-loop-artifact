#!/usr/bin/env python3
from pathlib import Path
import argparse
import subprocess

def init_schema(output_dir, args):
    print("Initialize schema")
    subprocess.run([
        args.ag_schema,
        "create-graph",
        output_dir,
    ])
    subprocess.run([
        args.ag_schema,
        "create-vertex-table",
        output_dir,
        "--vertex-label=evlp_vertex",
        "--vertex-property=orig_id=U64",
    ])
    subprocess.run([
        args.ag_schema,
        "create-edge-table",
        output_dir,
        "--edge-label=evlp_edge",
        "--src-labels=evlp_vertex",
        "--trg-labels=evlp_vertex",
    ] + (["--edge-property=weight=F64"] if args.weighted else []))

def load_graph(vertices_file, edges_file, output_dir, args):
    init_schema(output_dir, args)

    print("Load vertices")
    subprocess.run([
        args.ag_load_graph,
        "--graph-format=evlp_vertex",
        vertices_file,
        output_dir,
    ])

    print("Load edges...")
    subprocess.run([
        args.ag_csr,
        "--edge-label=evlp_edge",
        edges_file,
        output_dir,
    ]
        + (["--weighted"] if args.weighted else [])
        + (["--undirected"] if args.undirected else []))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('output_dir')
    parser.add_argument('--vertices', required=True)
    parser.add_argument('--edges', required=True)
    parser.add_argument('--cache')
    parser.add_argument('--dbfile')
    parser.add_argument('--weighted', action=argparse.BooleanOptionalAction)
    parser.add_argument('--undirected', action=argparse.BooleanOptionalAction)
    parser.add_argument('--ag-schema', default="ag-schema")
    parser.add_argument('--ag-load-graph', default="ag-load-graph")
    parser.add_argument('--ag-csr', default="ag-csr")

    args = parser.parse_args()

    output_dir = Path(args.output_dir)
    vertices_file = Path(args.vertices)
    edges_file = Path(args.edges)

    load_graph(vertices_file, edges_file, output_dir, args)
