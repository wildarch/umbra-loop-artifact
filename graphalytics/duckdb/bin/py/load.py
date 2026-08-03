#!/usr/bin/env python3
import duckdb
import argparse

def run(db_path, vertices_path, edges_path, weighted=False, undirected=False):
    conn = duckdb.connect(db_path)

    conn.execute("DROP TABLE IF EXISTS v")
    conn.execute("DROP TABLE IF EXISTS e")
    conn.execute("DROP TABLE IF EXISTS u")

    # Create vertices table
    conn.execute("CREATE TABLE v(id INT64)")
    conn.execute(f"COPY v (id) FROM '{vertices_path}'")


    # Create edges table
    loader_conf = "(DELIMITER ' ', FORMAT csv)"
    if weighted:
        conn.execute("CREATE TABLE e(source INT64, target INT64, weight DOUBLE)")
        conn.execute(f"COPY e (source, target, weight) FROM '{edges_path}' {loader_conf}")
        if undirected:
            # For undirected graphs only one way is stored.
            # Also copy the reverse direction.
            conn.execute(f"COPY e (target, source, weight) FROM '{edges_path}' {loader_conf}")
    else:
        conn.execute("CREATE TABLE e(source INT64, target INT64)")
        conn.execute(f"COPY e (source, target) FROM '{edges_path}' {loader_conf}")
        if undirected:
            # For undirected graphs only one way is stored.
            # Also copy the reverse direction.
            conn.execute(f"COPY e (target, source) FROM '{edges_path}' {loader_conf}")

    # Create undirected table
    conn.execute("CREATE TABLE u(source INT64, target INT64)")
    if undirected:
        # Just a copy of e
        conn.execute("INSERT INTO u SELECT source, target FROM e")
    else:
        # Also needs a reverse copy
        conn.execute("INSERT INTO u SELECT source, target FROM e")
        conn.execute("INSERT INTO u SELECT target, source FROM e")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('dbfile')
    parser.add_argument('--vertices', required=True)
    parser.add_argument('--edges', required=True)
    parser.add_argument('--weighted', action=argparse.BooleanOptionalAction)
    parser.add_argument('--undirected', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    run(args.dbfile, args.vertices, args.edges, args.weighted, args.undirected)
