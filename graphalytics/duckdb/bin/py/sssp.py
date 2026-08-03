#!/usr/bin/env python3
import duckdb
import argparse
import time

def now_millis():
    return time.time_ns() // 1_000_000

def run_using_key(graph, start, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    start_ms = now_millis()
    print(f"Processing starts at: {start_ms}")

    conn.execute(f"""
        CREATE TABLE d AS
        WITH RECURSIVE d(id, dist) USING KEY (id) AS (
            VALUES (CAST({start} AS INT64), CAST(0 AS DOUBLE PRECISION))
            UNION ALL (
                SELECT
                    edge.target AS id,
                    MIN(source.dist + edge.weight) AS dist
                FROM d source -- note: only vertices updated in last iter
                JOIN g.e edge ON (source.id = edge.source)
                LEFT JOIN recurring.d target ON (target.id = edge.target)
                WHERE target.id IS NULL
                   OR source.dist + edge.weight < target.dist
                GROUP BY edge.target
            )
        )
        TABLE d
    """)

    end_ms = now_millis()
    print(f"Processing ends at: {end_ms}")
    print(f"Processing time: {end_ms - start_ms}")

    if out:
        conn.execute("""
            CREATE TABLE sssp AS
            SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist
            FROM g.v
            LEFT JOIN d ON d.id = v.id
        """)

        conn.execute(f"""
            COPY (
                SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist
                FROM g.v
                LEFT JOIN d ON d.id = v.id
                ORDER BY id ASC
            ) TO '{out}' (DELIMITER ' ', HEADER false)
        """)

def run(graph, start, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    start_ms = now_millis()
    print(f"Processing starts at: {start_ms}")

    conn.execute(f"""
        CREATE TABLE d AS SELECT {start} as id, CAST(0 AS DOUBLE) AS dist
    """)

    while True:
        conn.execute("""
            CREATE TABLE d2 AS
            SELECT id, min(dist) AS dist
            FROM (
                SELECT g.e.target AS id, d.dist + e.weight AS dist
                FROM d
                JOIN g.e ON d.id = e.source

                UNION ALL

                SELECT id, dist
                FROM d
            )
            GROUP BY id
        """)

        (diff,) = conn.sql("""
            SELECT count(id) AS numChanged FROM (
                (
                    SELECT id, dist FROM d
                    EXCEPT
                    SELECT id, dist FROM d2
                )
                UNION ALL
                (
                    SELECT id, dist FROM d2
                    EXCEPT
                    SELECT id, dist FROM d
                )
            )
        """).fetchone()

        conn.execute("DROP TABLE d")
        conn.execute("ALTER TABLE d2 RENAME TO d")

        if diff == 0:
            break

    end_ms = now_millis()
    print(f"Processing ends at: {end_ms}")
    print(f"Processing time: {end_ms - start_ms}")

    if out:
        conn.execute("""
            CREATE TABLE sssp AS
            SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist
            FROM g.v
            LEFT JOIN d ON d.id = v.id
        """)

        conn.execute(f"""
            COPY (SELECT * FROM sssp ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)
        """)

def run_df(graph, start, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    start_ms = now_millis()
    print(f"Processing starts at: {start_ms}")

    d = conn.sql(f"SELECT {start} AS id, CAST(0 AS DOUBLE) AS dist").df()

    while True:
        d2 = conn.sql("""
            SELECT id, min(dist) AS dist
            FROM (
                SELECT g.e.target AS id, d.dist + e.weight AS dist
                FROM d
                JOIN g.e ON d.id = e.source

                UNION ALL

                SELECT id, dist
                FROM d
            )
            GROUP BY id
        """).df()

        (diff,) = conn.sql("""
            SELECT count(id) AS numChanged FROM (
                (
                    SELECT id, dist FROM d
                    EXCEPT
                    SELECT id, dist FROM d2
                )
                UNION ALL
                (
                    SELECT id, dist FROM d2
                    EXCEPT
                    SELECT id, dist FROM d
                )
            )
        """).fetchone()

        d = d2

        if diff == 0:
            break


    end_ms = now_millis()
    print(f"Processing ends at: {end_ms}")
    print(f"Processing time: {end_ms - start_ms}")

    if out:
        conn.execute("""
            CREATE TABLE sssp AS
            SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist
            FROM g.v
            LEFT JOIN d ON d.id = v.id
        """)

        conn.execute(f"""
            COPY (SELECT * FROM sssp ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)
        """)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('graph')
    parser.add_argument('start', type=int)
    parser.add_argument('--out')
    parser.add_argument('--profile', action=argparse.BooleanOptionalAction)
    parser.add_argument('--using-key', action=argparse.BooleanOptionalAction)
    parser.add_argument('--using-df', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    if args.using_key:
        run_using_key(args.graph, args.start, args.out, args.profile)
    elif args.using_df:
        run_df(args.graph, args.start, args.out, args.profile)
    else:
        run(args.graph, args.start, args.out, args.profile)
