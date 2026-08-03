#!/usr/bin/env python3
import duckdb
import argparse
import time

def now_millis():
    return time.time_ns() // 1_000_000

def run(graph, iterations, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    print(f"Processing starts at: {now_millis()}")

    conn.execute("CREATE TABLE cdlp AS SELECT id, id AS label FROM g.v")

    for i in range(iterations):
        conn.execute("""
            CREATE TABLE cdlp_new AS
            SELECT id, label
            FROM (
                SELECT
                    u.source AS id,
                    cdlp.label AS label,
                    ROW_NUMBER() OVER (PARTITION BY u.source ORDER BY count(*) DESC, cdlp.label ASC) AS seqnum
                FROM g.u
                LEFT JOIN cdlp ON (cdlp.id = u.target)
                GROUP BY u.source, cdlp.label
            )
            WHERE seqnum = 1
            ORDER BY id
        """)
        conn.execute("DROP TABLE cdlp")
        conn.execute("ALTER TABLE cdlp_new RENAME TO cdlp")

    print(f"Processing ends at: {now_millis()}")

    if out:
        conn.execute(f"COPY (SELECT * FROM cdlp ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('graph')
    parser.add_argument('--iterations', type=int, required=True)
    parser.add_argument('--out')
    parser.add_argument('--profile', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    run(args.graph, args.iterations, args.out, args.profile)
