#!/usr/bin/env python3
import argparse
import tempfile
from jproperties import Properties
from pathlib import Path
import psycopg
import subprocess

def load_graph(umbra_sql, dbfile, vertices_path, edges_path, directed, weighted):
    script = f"""
DROP TABLE IF EXISTS v;
DROP TABLE IF EXISTS e;
DROP TABLE IF EXISTS u;

CREATE TABLE v(id BIGINT NOT NULL PRIMARY KEY);
COPY v (id) FROM '{vertices_path}' (DELIMITER ' ', FORMAT text);
    """

    # Create edges table
    loader_conf = "(DELIMITER ' ', FORMAT csv)"
    if weighted:
        script += "CREATE TABLE e(source BIGINT NOT NULL, target BIGINT NOT NULL, weight DOUBLE PRECISION);\n"
        script += f"COPY e (source, target, weight) FROM '{edges_path}' {loader_conf};\n"
        if not directed:
            # For undirected graphs only one way is stored.
            # Also copy the reverse direction.
            script += f"COPY e (target, source, weight) FROM '{edges_path}' {loader_conf};\n"
    else:
        script += "CREATE TABLE e(source BIGINT NOT NULL, target BIGINT NOT NULL);\n"
        script += f"COPY e (source, target) FROM '{edges_path}' {loader_conf};\n"
        if not directed:
            script += f"COPY e (target, source) FROM '{edges_path}' {loader_conf};\n"

    # Write script to a NamedTempFile
    with tempfile.NamedTemporaryFile('w', suffix='.sql', delete=False) as f:
        f.write(script)
        f.flush()
        script_path = f.name

        # Check that the subprocess succeeded
        subprocess.run([
            umbra_sql,
            '-createdb',
            dbfile,
            script_path,
        ], check=True)

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('graphs_dir', type=Path)
    parser.add_argument('graph_name')
    parser.add_argument('dbfile', type=Path)
    parser.add_argument('--umbra-sql', type=Path, default='umbra-sql')

    args = parser.parse_args()

    # Parse properties file to extract configuration
    configs = Properties()
    with open(args.graphs_dir / (args.graph_name + '.properties'), 'rb') as config_file:
        configs.load(config_file)
    directed, meta = configs[f"graph.{args.graph_name}.directed"]
    directed = directed == 'true'
    weighted = f'graph.{args.graph_name}.edge-properties.names' in configs
    vertex_file = args.graphs_dir / f'{args.graph_name}.v'
    edge_file = args.graphs_dir / f'{args.graph_name}.e'

    load_graph(args.umbra_sql, args.dbfile, vertex_file, edge_file, directed, weighted)

if __name__ == "__main__":
    main()
