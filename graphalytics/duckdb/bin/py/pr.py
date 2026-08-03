#!/usr/bin/env python3
import duckdb
import argparse
import time

def now_millis():
    return time.time_ns() // 1_000_000

def run(graph, iterations, damping, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    print(f"Processing starts at: {now_millis()}")

    (prN,) = conn.sql("SELECT COUNT(*) FROM g.v").fetchone()
    teleport = (1 - damping) / prN
    redistFactor = damping / prN

    conn.execute("""
        CREATE TABLE dangling AS
        SELECT id
        FROM g.v
        ANTI JOIN g.e ON (source = id)
    """)

    conn.execute("""
        CREATE TABLE out_degree AS
        SELECT source AS  id, COUNT(*) AS degree
        FROM g.e
        GROUP BY source
    """)

    conn.execute(f"""
        CREATE TABLE PR0 AS
        SELECT id, CAST(1.0/{prN} AS DOUBLE) AS value
        FROM g.v
    """)

    for i in range(1, iterations+1):
        (redist,) = conn.sql(f"""
            SELECT coalesce($redistFactor * SUM(pr{i-1}.value), 0)
            FROM dangling
            JOIN PR{i-1} USING (id)
        """, params={
            "redistFactor": redistFactor,
        }).fetchone()

        conn.execute(f"""
            CREATE TABLE PR{i} AS
            SELECT
                g.v.id AS id,
                  ($teleport + ($damping * SUM(coalesce(pr{i-1}.value / out_degree.degree, 0))) + $redist) AS value
            FROM g.v
            LEFT JOIN g.e ON (g.e.target = g.v.id)
            LEFT JOIN pr{i-1} ON (pr{i-1}.id = g.e.source)
            LEFT JOIN out_degree ON (pr{i-1}.id = out_degree.id)
            GROUP BY g.v.id
        """, {
            "teleport": teleport,
            "damping": damping,
            "redist": redist,
        })

        conn.execute(f"DROP TABLE pr{i-1}")

    print(f"Processing ends at: {now_millis()}")

    if out:
        conn.execute(f"COPY (SELECT * FROM pr{iterations} ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('graph')
    parser.add_argument('--iterations', type=int, required=True)
    parser.add_argument('--damping', type=float, required=True)
    parser.add_argument('--out')
    parser.add_argument('--profile', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    run(args.graph, args.iterations, args.damping, args.out, args.profile)
