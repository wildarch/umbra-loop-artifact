package science.atlarge.graphalytics.umbra.algorithms.cdlp;

import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import science.atlarge.graphalytics.umbra.UmbraComputation;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;

import java.sql.SQLException;
import java.sql.Statement;

public class CommunityDetectionLPComputation extends UmbraComputation {

    protected int numIterations;
    protected UmbraConfiguration.IterationStrategy iterationStrategy;
    protected String outputPath;

    public CommunityDetectionLPComputation(Statement statement, int numIterations, UmbraConfiguration.IterationStrategy iterationStrategy, String outputPath) {
        super(statement);
        this.numIterations = numIterations;
        this.iterationStrategy = iterationStrategy;
        this.outputPath = outputPath;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS cdlp");
    }

    private void computeExternalDriver() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        statement.executeUpdate(
                "CREATE TABLE cdlp AS\n" +
                        "SELECT id AS id, id AS label\n" +
                        "FROM v");

        // We select the minimum mode value (the smallest one from the most frequent labels).
        // We use the cdlp table to compute cdlp_new, then throw away the cdlp table.
        for (int i = 0; i < numIterations; i++) {
            statement.executeUpdate(
                    "CREATE TABLE cdlp_new AS\n" +
                    "    SELECT source AS id, arg_max(label, freq * (SELECT MAX(id) + 1 FROM v) - label) AS label\n" +
                    "    FROM (\n" +
                    "        SELECT u.source, cdlp.label, COUNT(*) AS freq\n" +
                    "        FROM u\n" +
                    "        JOIN cdlp ON (cdlp.id = u.target)\n" +
                    "        GROUP BY 1, 2\n" +
                    "    )\n" +
                    "    GROUP BY 1\n");
            statement.executeUpdate("DROP TABLE cdlp");
            statement.executeUpdate("ALTER TABLE cdlp_new RENAME TO cdlp");
        }

        LOG.info("Processing ends at: {}", System.currentTimeMillis());

        // export results
        statement.executeUpdate(String.format("COPY cdlp TO '%s' (DELIMITER ' ')", outputPath));
    }

    private void computeLoop() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        statement.executeUpdate(String.format(
                "CREATE TABLE cdlp AS\n" +
                "SELECT * FROM umbra.iterate(\n" +
                "    cdlp_init => TABLE(SELECT id, id as label FROM v),\n" +
                "    cdlp_next => TABLE(\n" +
                "        SELECT source AS id, arg_max(label, freq * (SELECT MAX(id) + 1 FROM v) - label) AS label\n" +
                "        FROM (\n" +
                "            SELECT u.source, cdlp.label, COUNT(*) AS freq\n" +
                "            FROM u\n" +
                "            JOIN cdlp ON (cdlp.id = u.target)\n" +
                "            GROUP BY 1, 2\n" +
                "        )\n" +
                "        GROUP BY 1\n" +
                "    ),\n" +
                "    count_init => TABLE(SELECT 0 AS cnt),\n" +
                "    count_next => TABLE(SELECT cnt + 1 AS cnt FROM count),\n" +
                "    until => TABLE(SELECT cnt = %d FROM count)\n" +
                ")\n",
                numIterations));

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate(String.format("COPY cdlp TO '%s' (DELIMITER ' ')", outputPath));
    }

    @Override
    public void compute() throws SQLException {
        switch (iterationStrategy) {
            case EXTERNAL_DRIVER:
                computeExternalDriver();
                break;
            case USING_KEY:
                throw new GraphalyticsExecutionException("USING KEY for cdlp is not supported");
            case LOOP:
                computeLoop();
                break;
        }
    }

}
