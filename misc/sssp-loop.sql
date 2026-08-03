EXPLAIN ANALYZE
SELECT * FROM umbra.iterate(
    dist_init => TABLE (SELECT 239044::BIGINT AS id, 0.0::DOUBLE PRECISION AS dist),
    dist_next => TABLE (
        SELECT target AS id, dist + weight AS dist
        FROM dist
        JOIN e ON (dist.id = e.source)
    ),
    dist_opts => '{
        "keys": [0], 
        "aggregations": [
            { "func": "min", "output": 1, "input": 1}
        ],
        "converge": true
    }'
)