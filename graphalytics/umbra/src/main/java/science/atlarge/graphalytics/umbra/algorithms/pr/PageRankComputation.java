package science.atlarge.graphalytics.umbra.algorithms.pr;

import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;
import science.atlarge.graphalytics.umbra.UmbraComputation;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class PageRankComputation extends UmbraComputation {

    protected int maxIterations;
    protected double dampingFactor;
    protected UmbraConfiguration.IterationStrategy iterationStrategy;
    protected String outputPath;

    public PageRankComputation(Statement statement, int maxIterations, double dampingFactor, UmbraConfiguration.IterationStrategy iterationStrategy, String outputPath) {
        super(statement);
        this.maxIterations = maxIterations;
        this.dampingFactor = dampingFactor;
        this.iterationStrategy = iterationStrategy;
        this.outputPath = outputPath;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS dangling");
        statement.executeUpdate("DROP TABLE IF EXISTS out_degree");
        statement.executeUpdate("DROP TABLE IF EXISTS pr");
        statement.executeUpdate("DROP TABLE IF EXISTS pr_next");
    }

    private void computeLoop() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        PreparedStatement pr = statement.getConnection().prepareStatement(
            "CREATE TABLE pr AS\n" +
            "WITH \n" +
            "    d AS (\n" +
            "        SELECT source AS id, CAST(COUNT(*) AS DOUBLE PRECISION) / ? AS degree\n" +
            "        FROM e\n" +
            "        GROUP BY 1\n" +
            "    ),\n" +
            "    sinks AS (\n" +
            "        SELECT id FROM v\n" +
            "        EXCEPT\n" +
            "        SELECT source\n" +
            "        FROM e\n" +
            "    ),\n" +
            "    n AS (SELECT COUNT(*) AS n FROM v),\n" +
            "    teleport AS (SELECT (1.0::DOUBLE PRECISION - ?) / n AS teleport FROM n)\n" +
            "SELECT * FROM umbra.iterate(\n" +
            "    pr_init => TABLE(\n" +
            "        SELECT \n" +
            "            id AS id, \n" +
            "            1.0::DOUBLE PRECISION / (SELECT n FROM n) AS val\n" +
            "        FROM v),\n" +
            "    pr_next => TABLE(\n" +
            "        WITH\n" +
            "            sink_pr AS (\n" +
            "                SELECT COALESCE(SUM(val), 0) AS sink_pr\n" +
            "                FROM sinks\n" +
            "                JOIN pr USING (id)\n" +
            "            ),\n" +
            "            redist AS (\n" +
            "                SELECT ? / n * sink_pr AS redist\n" +
            "                FROM n, sink_pr\n" +
            "            ),\n" +
            "            w AS (\n" +
            "                SELECT id, val / degree AS w\n" +
            "                FROM pr JOIN d USING (id)\n" +
            "            )\n" +
            "        SELECT \n" +
            "            v.id AS id,\n" +
            "            (SELECT teleport FROM teleport) + (SELECT redist FROM redist) + COALESCE(SUM(w), 0) AS val\n" +
            "        FROM v\n" +
            "        LEFT OUTER JOIN e ON (v.id = e.target)\n" +
            "        LEFT OUTER JOIN w ON (e.source = w.id)\n" +
            "        GROUP BY v.id\n" +
            "    ),\n" +
            "    count_init => TABLE(SELECT 0 AS cnt),\n" +
            "    count_next => TABLE(SELECT cnt + 1 AS cnt FROM count),\n" +
            "    until => TABLE(SELECT cnt = ? FROM count)\n" +
            ")\n");
        pr.setDouble(1, dampingFactor);
        pr.setDouble(2, dampingFactor);
        pr.setDouble(3, dampingFactor);
        pr.setLong(4, maxIterations);
        pr.executeUpdate();

        LOG.info("Processing ends at: {}", System.currentTimeMillis());

        // export results
        statement.executeUpdate(String.format("COPY pr TO '%s' (DELIMITER ' ')", outputPath));
    }

    private void computeExternalDriver() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM v");
        resultSet.next();
        long prN = resultSet.getLong(1);
        double prTeleport = (1 - dampingFactor)/ prN;
        double redistributionFactor = dampingFactor / prN;

        statement.executeUpdate(
                "CREATE TABLE dangling AS\n" +
                        "SELECT id\n" +
                        "FROM v\n" +
                        "WHERE NOT EXISTS (SELECT 1 FROM e WHERE source = id)");

        statement.executeUpdate(
                "CREATE TABLE out_degree AS\n" +
                        "SELECT source AS  id, COUNT(*) AS degree\n" +
                        "FROM e\n" +
                        "GROUP BY source");

        PreparedStatement prInit = statement.getConnection().prepareStatement(
                "CREATE TABLE pr AS\n" +
                        "SELECT id, CAST(1.0/? AS DOUBLE PRECISION) AS value\n" +
                        "FROM v\n");
        prInit.setLong(1, prN);
        prInit.executeUpdate();

        for (int i = 0; i < maxIterations; i++) {
            PreparedStatement redistStmt = statement.getConnection().prepareStatement(
                    "SELECT coalesce(? * SUM(pr.value), 0)\n" +
                            "FROM dangling\n" +
                            "JOIN pr USING (id)\n");
            redistStmt.setDouble(1, redistributionFactor);
            ResultSet res = redistStmt.executeQuery();
            res.next();
            double redist = res.getDouble(1);

            PreparedStatement nextStmt = statement.getConnection().prepareStatement(
                    "CREATE TABLE pr_next AS\n" +
                            "SELECT\n" +
                            "    v.id AS id,\n" +
                            "      (? + (? * SUM(coalesce(pr.value / out_degree.degree, 0))) + ?) AS value\n" +
                            "FROM v\n" +
                            "LEFT OUTER JOIN e ON (e.target = v.id)\n" +
                            "LEFT OUTER JOIN pr ON (pr.id = e.source)\n" +
                            "LEFT OUTER JOIN out_degree ON (pr.id = out_degree.id)\n" +
                            "GROUP BY v.id\n");
            nextStmt.setDouble(1, prTeleport);
            nextStmt.setDouble(2, dampingFactor);
            nextStmt.setDouble(3, redist);
            nextStmt.executeUpdate();

            statement.executeUpdate("DROP TABLE pr");
            statement.executeUpdate("ALTER TABLE pr_next RENAME TO pr");
        }

        LOG.info("Processing ends at: {}", System.currentTimeMillis());

        // export results
        statement.executeUpdate(String.format("COPY pr TO '%s' (DELIMITER ' ')", outputPath));
    }

    @Override
    public void compute() throws SQLException {
        switch (iterationStrategy) {
            case EXTERNAL_DRIVER:
                computeExternalDriver();
                break;
            case USING_KEY:
                throw new GraphalyticsExecutionException("USING KEY for pr is not supported");
            case LOOP:
                computeLoop();
                break;
        }
    }
}
