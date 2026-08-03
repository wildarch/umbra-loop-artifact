#!/usr/bin/env python3
import sys
import duckdb

vertices = sys.argv[1]
input = sys.argv[2]
output = sys.argv[3]

vertices = duckdb.sql(f"""
    SELECT id
    FROM read_csv('{vertices}', columns = {{'id': 'int64'}})
""")

input = duckdb.sql(f"""
    SELECT id, depth
    FROM read_csv('{input}', delim=' ', columns = {{'id': 'int64', 'depth': 'int64'}})
""")

with_unreachable = duckdb.sql("""
    SELECT id, coalesce(depth-1, 9223372036854775807) AS depth
    FROM vertices
    LEFT JOIN input USING (id)
""")

duckdb.execute(f"""
    COPY with_unreachable TO '{output}' WITH (HEADER false, DELIMITER ' ', use_tmp_file true)
""")