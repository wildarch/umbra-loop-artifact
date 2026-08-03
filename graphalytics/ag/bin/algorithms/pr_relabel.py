#!/usr/bin/env python3
import sys
import duckdb

input = sys.argv[1]
output = sys.argv[2]

input = duckdb.sql(f"""
    SELECT CAST(id AS INT64), pr
    FROM read_csv('{input}', columns = {{'pr': 'double', 'id': 'string'}})
""")

duckdb.execute(f"""
    COPY input TO '{output}' WITH (HEADER false, DELIMITER ' ', use_tmp_file true)
""")