#!/usr/bin/env python3
import duckdb
import argparse
import time

def now_millis():
    return time.time_ns() // 1_000_000

def run_using_key(graph, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    start_ms = now_millis()
    print(f"Processing starts at: {start_ms}")

    conn.execute(f"""
        CREATE TABLE wcc AS
        WITH RECURSIVE wcc(id, comp) USING KEY (id) AS (
            SELECT init.id, init.id AS comp FROM g.v AS init
            UNION ALL
            SELECT
                neighbour.id,
                MIN(self.comp) AS comp
            FROM wcc self -- note: only the nodes that got an update in the previous iteration
            JOIN g.u ON (self.id = g.u.source)
            JOIN recurring.wcc neighbour ON (g.u.target = neighbour.id
                                                AND self.comp < neighbour.comp)
            GROUP BY neighbour.id
        )
        TABLE wcc
    """)

    end_ms = now_millis()
    print(f"Processing ends at: {end_ms}")
    print(f"Processing time: {end_ms - start_ms}")

    if out:
        conn.execute(f"COPY (SELECT * FROM wcc ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)")

def run(graph, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    start_ms = now_millis()
    print(f"Processing starts at: {start_ms}")

    conn.execute("""
        CREATE TABLE wcc AS
        SELECT id, id as prev, id as curr
        FROM g.v
    """)

    while True:
        conn.execute("""
                CREATE TABLE wcc_next AS
                SELECT
                    self.id,
                    any_value(self.curr) AS prev,
                    coalesce(MIN(neighbour.curr), any_value(self.curr)) AS curr
                FROM wcc self
                LEFT JOIN g.u ON (self.id = g.u.source)
                LEFT JOIN wcc neighbour ON (g.u.target = neighbour.id
                                            AND neighbour.curr < self.curr)
                GROUP BY self.id
        """)
        conn.execute("DROP TABLE wcc")
        conn.execute("ALTER TABLE wcc_next RENAME TO wcc")

        (diff,) = conn.sql("""
            SELECT COUNT(*) FROM wcc WHERE curr != prev
        """).fetchone()
        if diff == 0:
            break

    end_ms = now_millis()
    print(f"Processing ends at: {end_ms}")
    print(f"Processing time: {end_ms - start_ms}")

    if out:
        conn.execute(f"COPY (SELECT id, curr FROM wcc ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)")

def run_cte(graph, out=None, profile=False):
    conn = duckdb.connect()

    conn.execute(f"ATTACH '{graph}' AS g (READ_ONLY)")
    conn.execute("SET THREADS=16")

    if profile:
        conn.execute("PRAGMA enable_profiling='json'")

    print(f"Processing starts at: {now_millis()}")

    # NOTE: Taken from the PostgreSQL reference impl.
    conn.execute("""
            CREATE TABLE wcc AS
            WITH RECURSIVE paths(startVertex, endVertex, path) AS (
                SELECT -- define the path as the first e of the traversal
                    id AS startVertex,
                    id AS endVertex,
                    array[id, id] AS path
                    FROM g.v
                UNION ALL
                SELECT -- concatenate new u to the path
                    paths.startVertex AS startVertex,
                    g.u.target AS endVertex,
                    array_append(paths.path, g.u.target) AS path
                    FROM paths
                    JOIN g.u ON paths.endVertex = g.u.source
                -- Prevent adding a repeated v to the path.
                -- This ensures that no cycles occur.
                WHERE target != ALL(paths.path)
            )
            SELECT startVertex AS id, min(p) AS component
            FROM (
                SELECT startVertex, endVertex, unnest(path) AS p
                FROM paths
            ) sub
            GROUP BY startVertex
            ORDER BY startVertex
    """)

    print(f"Processing ends at: {now_millis()}")

    if out:
        conn.execute(f"COPY (SELECT id, component FROM wcc ORDER BY id ASC) TO '{out}' (DELIMITER ' ', HEADER false)")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('graph')
    parser.add_argument('--out')
    parser.add_argument('--profile', action=argparse.BooleanOptionalAction)
    parser.add_argument('--using-key', action=argparse.BooleanOptionalAction)

    args = parser.parse_args()
    if args.using_key:
        run_using_key(args.graph, args.out, args.profile)
    else:
        run(args.graph, args.out, args.profile)
