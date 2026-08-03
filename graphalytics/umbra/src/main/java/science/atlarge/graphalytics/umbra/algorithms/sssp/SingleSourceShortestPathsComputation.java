package science.atlarge.graphalytics.umbra.algorithms.sssp;

import science.atlarge.graphalytics.umbra.UmbraComputation;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SingleSourceShortestPathsComputation extends UmbraComputation {

    protected long sourceVertex;
    private final UmbraConfiguration.IterationStrategy iterationStrategy;
    protected String outputPath;

    public SingleSourceShortestPathsComputation(Statement statement, long sourceVertex, UmbraConfiguration.IterationStrategy iterationStrategy, String outputPath) {
        super(statement);
        this.sourceVertex = sourceVertex;
        this.iterationStrategy = iterationStrategy;
        this.outputPath = outputPath;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS d");
        statement.executeUpdate("DROP TABLE IF EXISTS d2");
        statement.executeUpdate("DROP TABLE IF EXISTS sssp");
    }

    public void computeExternalDriver() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate(String.format(
                "CREATE TABLE d AS\n" +
                        "    SELECT %d AS id, CAST(0 AS DOUBLE PRECISION) AS dist",
                sourceVertex
        ));

        statement.executeUpdate("INSERT INTO e SELECT id, id, 0.0 FROM v");

        while (true) {
            statement.executeUpdate(
                    "CREATE TABLE d2 AS\n" +
                            "    SELECT e.target AS id, min(d.dist + e.weight) AS dist\n" +
                            "    FROM d\n" +
                            "    JOIN e\n" +
                            "      ON d.id = e.source\n" +
                            "    GROUP BY e.target"
            );
            ResultSet resultSet = statement.executeQuery(
                    "SELECT count(id) AS numChanged FROM (\n" +
                            "    (\n" +
                            "        SELECT id, dist FROM d\n" +
                            "        EXCEPT\n" +
                            "        SELECT id, dist FROM d2\n" +
                            "    )\n" +
                            "    UNION ALL\n" +
                            "    (\n" +
                            "        SELECT id, dist FROM d2\n" +
                            "        EXCEPT\n" +
                            "        SELECT id, dist FROM d\n" +
                            "    )\n" +
                            ") sub"
            );
            resultSet.next();
            long numChanged = resultSet.getLong(1);

            statement.executeUpdate("DROP TABLE d");
            statement.executeUpdate("ALTER TABLE d2 RENAME TO d");

            if (numChanged == 0) {
                break;
            }
        }

        // cleanup loop edges
        statement.executeUpdate("DELETE FROM e WHERE source = target AND weight = 0.0");

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        statement.executeUpdate(
                "CREATE TABLE sssp AS " +
                        "  SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist " +
                        "  FROM v " +
                        "  LEFT JOIN d ON d.id = v.id"
        );

        // export results
        statement.executeUpdate(String.format("COPY sssp TO '%s' (DELIMITER ' ')", outputPath));
    }

    public void computeLoop() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate(String.format(
            "CREATE TABLE d AS\n" +
            "SELECT * FROM umbra.iterate(\n" +
            "    dist_init => TABLE (SELECT %d::BIGINT AS id, 0.0::DOUBLE PRECISION AS dist),\n" +
            "    dist_next => TABLE (\n" +
            "        SELECT t AS id, dist + v AS dist\n" +
            "        FROM dist\n" +
            "        JOIN e ON (dist.id = e.s)\n" +
            "    ),\n" +
            "    dist_opts => '{\n" +
            "        \"keys\": [0], \n" +
            "        \"aggregations\": [\n" +
            "            { \"func\": \"min\", \"output\": 1, \"input\": 1}\n" +
            "        ],\n" +
            "        \"converge\": true\n" +
            "    }'\n" +
            ")\n", sourceVertex));

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        statement.executeUpdate(
                "CREATE TABLE sssp AS " +
                        "  SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist " +
                        "  FROM v " +
                        "  LEFT JOIN d ON d.id = v.id"
        );

        // export results
        statement.executeUpdate(String.format("COPY sssp TO '%s' (DELIMITER ' ')", outputPath));
    }

    public void computeUsingKey() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate(String.format(
            "CREATE TABLE d AS\n" +
            "WITH RECURSIVE d(id, dist) USING KEY (id) AS (\n" +
            "    VALUES (CAST(%d AS BIGINT), CAST(0 AS DOUBLE PRECISION))\n" +
            "    UNION ALL RECURRING (\n" +
            "        SELECT \n" +
            "            edge.target AS id, \n" +
            "            MIN(source.dist + edge.weight) AS dist\n" +
            "        FROM d source -- note: only vertices updated in last iter\n" +
            "        JOIN e edge ON (source.id = edge.source)\n" +
            "        LEFT OUTER JOIN recurring target ON (target.id = edge.target)\n" +
            "        WHERE target.id IS NULL \n" +
            "        OR source.dist + edge.weight < target.dist\n" +
            "        GROUP BY edge.target\n" +
            "    )\n" +
            ")\n" +
            "TABLE d", sourceVertex));

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        statement.executeUpdate(
                "CREATE TABLE sssp AS " +
                        "  SELECT v.id, coalesce(cast(d.dist AS text), 'infinity') AS dist " +
                        "  FROM v " +
                        "  LEFT JOIN d ON d.id = v.id"
        );

        // export results
        statement.executeUpdate(String.format("COPY sssp TO '%s' (DELIMITER ' ')", outputPath));
    }

    @Override
    public void compute() throws SQLException {
        switch (iterationStrategy) {
            case EXTERNAL_DRIVER:
                computeExternalDriver();
                break;
            case USING_KEY:
            case LOOP:
                computeUsingKey();
                break;
        }
    }

}
