package science.atlarge.graphalytics.umbra.algorithms.bfs;

import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.umbra.UmbraComputation;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class BreadthFirstSearchComputation extends UmbraComputation {

    protected String outputPath;
    protected UmbraConfiguration.IterationStrategy iterationStrategy;
    protected long sourceVertex;

    public BreadthFirstSearchComputation(Statement statement, UmbraConfiguration.IterationStrategy iterationStrategy, String outputPath, long sourceVertex) {
        super(statement);
        this.outputPath = outputPath;
        this.iterationStrategy = iterationStrategy;
        this.sourceVertex = sourceVertex;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS bfs");
        statement.executeUpdate("DROP TABLE IF EXISTS frontier");
        statement.executeUpdate("DROP TABLE IF EXISTS next");
        statement.executeUpdate("DROP TABLE IF EXISTS seen");
    }

    private void computeLoop() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        statement.executeUpdate(String.format(
                "CREATE TABLE bfs AS\n" +
                "SELECT * FROM umbra.iterate(\n" +
                "    -- begin initial state\n" +
                "    dist_init => TABLE (SELECT %d::BIGINT AS id, 0::BIGINT AS dist),\n" +
                "    frontier_init => TABLE (\n" +
                "        SELECT DISTINCT e.target AS id, 1::BIGINT AS dist\n" +
                "        FROM e\n" +
                "        WHERE e.source = %d\n" +
                "          AND e.target <> %d\n" +
                "    ),\n" +
                "    step_init => TABLE (SELECT 1 AS step),\n" +
                "    -- end initial state\n" +
                "    -- begin loop body \n" +
                "    dist_next => TABLE (\n" +
                "        SELECT id, dist\n" +
                "        FROM frontier\n" +
                "    ),\n" +
                "    dist_opts => '{\n" +
                "        \"keys\": [0],\n" +
                "        \"aggregations\": [\n" +
                "            { \"func\": \"min\", \"output\": 1, \"input\": 1 }\n" +
                "        ]\n" +
                "    }',\n" +
                "    frontier_next => TABLE (\n" +
                "        SELECT DISTINCT e.target AS id, step + 1 AS dist\n" +
                "        FROM frontier\n" +
                "        JOIN e ON (id = source)\n" +
                "        CROSS JOIN step\n" +
                "        WHERE NOT EXISTS (SELECT 1 FROM dist WHERE dist.id = target)\n" +
                "          AND NOT EXISTS (SELECT 1 FROM frontier WHERE frontier.id = target)\n" +
                "    ),\n" +
                "    step_next => TABLE (SELECT step + 1 AS step from step),\n" +
                "    until => TABLE (SELECT COUNT(*) = 0 FROM frontier)\n" +
                ")\n", sourceVertex, sourceVertex, sourceVertex));

        LOG.info("Processing ends at: {}", System.currentTimeMillis());

        // Mark everything else as unreachable
        statement.execute(
                "INSERT INTO bfs\n" +
                        "SELECT id, 9223372036854775807::BIGINT AS dist FROM v\n" +
                        "WHERE NOT EXISTS (SELECT bfs.id FROM bfs WHERE bfs.id = v.id)");

        // export results
        statement.executeUpdate(String.format("COPY bfs TO '%s' (DELIMITER ' ')", outputPath));
    }

    private void computeExternalDriver() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        statement.executeUpdate("CREATE TABLE frontier(id BIGINT)");
        statement.executeUpdate("CREATE TABLE next(id BIGINT)");
        statement.executeUpdate("CREATE TABLE seen(id BIGINT, level BIGINT)");

        int level = 0;
        statement.executeUpdate(String.format("INSERT INTO next VALUES (%d)", sourceVertex));
        statement.executeUpdate(String.format("INSERT INTO seen (SELECT id, %d FROM next)", level));
        statement.executeUpdate("DELETE FROM frontier");
        statement.executeUpdate("INSERT INTO frontier (SELECT * FROM next)");
        statement.executeUpdate("DELETE FROM next");

        while (true) {
            level++;
            statement.executeUpdate("INSERT INTO next " +
                    "SELECT DISTINCT e.target " +
                    "  FROM frontier JOIN e ON e.source = frontier.id " +
                    " WHERE NOT EXISTS (SELECT 1 FROM seen WHERE id = e.target)"
            );

            ResultSet resultSet = statement.executeQuery("SELECT count(id) AS count FROM next");
            resultSet.next();
            long count = resultSet.getLong(1);
            if (count == 0) {
                break;
            }

            statement.executeUpdate(String.format("INSERT INTO seen (SELECT id, %d FROM next)", level));
            statement.executeUpdate("DELETE FROM frontier");
            statement.executeUpdate("INSERT INTO frontier (SELECT * FROM next)");
            statement.executeUpdate("DELETE FROM next");
        }

        LOG.info("Processing ends at: {}", System.currentTimeMillis());

        statement.executeUpdate(
                "CREATE TABLE bfs AS " +
                        "  SELECT v.id, coalesce(seen.level, 9223372036854775807) AS level " +
                        "  FROM v " +
                        "  LEFT JOIN seen ON seen.id = v.id"
        );

        // export results
        statement.executeUpdate(String.format("COPY bfs TO '%s' (DELIMITER ' ')", outputPath));
    }

    @Override
    public void compute() throws SQLException {
        switch (iterationStrategy) {
            case EXTERNAL_DRIVER:
                computeExternalDriver();
                break;
            case USING_KEY:
                throw new GraphalyticsExecutionException("USING KEY for bfs is not supported");
            case LOOP:
                computeLoop();
                break;
        }
    }

}
