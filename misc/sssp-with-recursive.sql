WITH RECURSIVE sssp AS (
    -- Base Case: Start at the source node (e.g., node 1) with distance 0
    SELECT 
        239044 AS id,
        0::DOUBLE PRECISION AS dist

    UNION

    -- Recursive Step: Traverse to connected target nodes
    SELECT 
        e.target AS id,
        s.dist + e.weight AS dist
    FROM sssp s
    JOIN e ON s.id = e.source
    WHERE (s.dist + e.weight) < (SELECT SUM(weight) FROM e)
)
SELECT 
    id,
    MIN(dist) AS dist
FROM sssp
GROUP BY id;