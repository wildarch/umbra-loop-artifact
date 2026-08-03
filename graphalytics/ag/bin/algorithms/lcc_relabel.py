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
    SELECT v AS id, val
    FROM read_csv('{input}', delim=' ', columns = {{'v': 'int64', 'val': 'double'}})
""")

with_zeros = duckdb.sql("""
    SELECT id, coalesce(val, 0) AS val
    FROM vertices
    LEFT JOIN input USING (id)
""")

duckdb.execute(f"""
    COPY with_zeros TO '{output}' WITH (HEADER false, DELIMITER ' ', use_tmp_file true)
""")
