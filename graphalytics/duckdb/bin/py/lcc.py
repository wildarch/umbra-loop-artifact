#!/usr/bin/env python3
import duckdb
import argparse
import time

def now_millis():
    return time.time_ns() // 1_000_000

def run(graph, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    print(f"Processing starts at: {now_millis()}")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    conn.execute("""
        CREATE VIEW neighbours AS (
            SELECT e.source AS vertex, e.target as neighbour
            FROM g.e

            UNION

            SELECT e.target AS vertex, e.source as neighbour
            FROM g.e
        )
    """)

    conn.execute("""
        CREATE TABLE LCC AS
        SELECT
            id,
            CASE
                WHEN tri = 0 THEN 0.0
                ELSE (CAST(tri AS double) / (deg*(deg-1)))
            END AS value
            FROM (
                SELECT
                    v.id AS id,
                    (SELECT count(*) FROM neighbours WHERE neighbours.vertex = v.id) AS deg,
                    (
                        SELECT count(*)
                        FROM neighbours n1
                        JOIN neighbours n2
                          ON n1.vertex = n2.vertex
                        JOIN g.e e3
                          ON e3.source = n1.neighbour
                         AND e3.target = n2.neighbour
                        WHERE n1.vertex = v.id
                    ) AS tri
                FROM g.v
                ORDER BY v.id ASC
            )
    """)

    print(f"Processing ends at: {now_millis()}")

    if out:
        conn.execute(f"COPY (SELECT * FROM lcc ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('graph')
    parser.add_argument('--out')
    parser.add_argument('--profile', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    run(args.graph, args.out, args.profile)
