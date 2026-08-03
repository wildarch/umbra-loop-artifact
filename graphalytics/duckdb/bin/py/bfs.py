#!/usr/bin/env python3
import duckdb
import argparse
import time

def now_millis():
    return time.time_ns() // 1_000_000

def run(graph, start, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    print(f"Processing starts at: {now_millis()}")

    conn.execute("CREATE TABLE frontier(id INT64)")
    conn.execute("CREATE TABLE next(id INT64)")
    conn.execute("CREATE TABLE seen(id INT64, level INT64)")

    level = 0
    conn.execute(f"INSERT INTO next VALUES ({start})")
    conn.execute(f"INSERT INTO seen (SELECT id, {level} FROM next)")
    conn.execute("DELETE FROM frontier")
    conn.execute("INSERT INTO frontier (SELECT * FROM next)")
    conn.execute("DELETE FROM next")

    while True:
            level += 1
            conn.execute("""
                INSERT INTO next
                SELECT DISTINCT g.e.target
                FROM frontier
                JOIN g.e ON (g.e.source = frontier.id)
                WHERE NOT EXISTS (SELECT 1 FROM seen WHERE id = g.e.target)
            """)

            (count,) = conn.sql("SELECT COUNT(id) AS count FROM next").fetchone()
            if count == 0:
                break

            conn.execute(f"INSERT INTO seen (SELECT id, {level} FROM next)")
            conn.execute("DELETE FROM frontier")
            conn.execute("INSERT INTO frontier (SELECT * FROM next)")
            conn.execute("DELETE FROM next")

    print(f"Processing ends at: {now_millis()}")

    if out:
        conn.execute("""
            CREATE TABLE bfs AS
            SELECT v.id, coalesce(seen.level, 9223372036854775807) AS level
            FROM g.v
            LEFT JOIN seen ON (seen.id = v.id)
        """)

        conn.execute(f"""
            COPY (SELECT * FROM bfs ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)
        """)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('graph')
    parser.add_argument('start', type=int)
    parser.add_argument('--out')
    parser.add_argument('--profile', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    run(args.graph, args.start, args.out, args.profile)
