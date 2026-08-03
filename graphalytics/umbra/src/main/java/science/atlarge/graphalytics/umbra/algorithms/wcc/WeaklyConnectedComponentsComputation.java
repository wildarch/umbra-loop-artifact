package science.atlarge.graphalytics.umbra.algorithms.wcc;

import org.apache.commons.lang.NotImplementedException;
import science.atlarge.graphalytics.umbra.UmbraComputation;
import science.atlarge.graphalytics.umbra.UmbraConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WeaklyConnectedComponentsComputation extends UmbraComputation {
    private final UmbraConfiguration.IterationStrategy iterationStrategy;
    protected String outputPath;

    public WeaklyConnectedComponentsComputation(Statement statement, UmbraConfiguration.IterationStrategy iterationStrategy, String outputPath) {
        super(statement);
        this.iterationStrategy = iterationStrategy;
        this.outputPath = outputPath;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS wcc");
        statement.executeUpdate("DROP TABLE IF EXISTS wcc_next");
    }

    public void computeExternalDriver() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        statement.executeUpdate("CREATE TABLE wcc(id BIGINT, prev BIGINT, curr BIGINT)");
        statement.executeUpdate("INSERT INTO wcc SELECT id AS id, id AS prev, id AS curr FROM v");
        while (true) {
            statement.executeUpdate("CREATE TABLE wcc_next(id BIGINT, prev BIGINT, curr BIGINT)");
            statement.executeUpdate("INSERT INTO wcc_next " +
                    "SELECT" +
                    "    self.id,\n" +
                    "    any_value(self.curr) AS prev,\n" +
                    "    COALESCE(MIN(neighbour.curr), any_value(self.curr)) AS curr\n" +
                    "FROM wcc self\n" +
                    "LEFT JOIN u ON (self.id = u.source)\n" +
                    "LEFT JOIN wcc neighbour ON (u.target = neighbour.id AND neighbour.curr < self.curr)\n" +
                    "GROUP BY self.id");
            statement.executeUpdate("DROP TABLE wcc");
            statement.executeUpdate("ALTER TABLE wcc_next RENAME TO wcc");
            ResultSet res = statement.executeQuery("SELECT COUNT(*) FROM wcc WHERE curr != prev");
            res.next();
            long count = res.getLong(1);
            res.close();
            if (count == 0) {
                break;
            }
        }

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate(String.format("COPY (SELECT id, curr FROM wcc) TO '%s' (DELIMITER ' ')", outputPath));

    }

    public void computeUsingKey() throws SQLException {
        LOG.info(String.format("Processing starts at: %d", System.currentTimeMillis()));

        String query =
            "WITH RECURSIVE wcc(id, comp) USING KEY (id) AS (\n" +
            "    SELECT init.id, init.id AS comp FROM v AS init\n" +
            "    UNION ALL RECURRING\n" +
            "    SELECT\n" +
            "        neighbour.id,\n" +
            "        MIN(self.comp) AS comp\n" +
            "    FROM wcc self -- note: only the nodes that got an update in the previous iteration\n" +
            "    JOIN u ON (self.id = u.source)\n" +
            "    JOIN recurring neighbour ON (u.target = neighbour.id\n" +
            "                                        AND self.comp < neighbour.comp)\n" +
            "    GROUP BY neighbour.id\n" +
            ")\n" +
            "TABLE wcc";
        LOG.info("Query: {}", query);

        statement.executeUpdate(
            "CREATE TABLE wcc AS " + query);

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate(String.format("COPY wcc TO '%s' (DELIMITER ' ')", outputPath));
    }

    public void computeLoop() throws SQLException {
        LOG.info("Processing starts at: {}", System.currentTimeMillis());

        String query =
            "SELECT * FROM umbra.iterate(\n" +
            "comp_init => TABLE (SELECT id, id AS val FROM v),\n" +
            "comp_next => TABLE (\n" +
            "SELECT u.target AS id, comp.val\n" +
            "FROM u, comp\n" +
            "WHERE u.source = comp.id\n" +
            "),\n" +
            "comp_opts => '{\n" +
            "\"keys\": [0], \n" +
            "\"aggregations\": [\n" +
            "{ \"func\": \"min\", \"output\": 1, \"input\": 1}\n" +
            "],\n" +
            "\"converge\": true\n" +
            "}'\n" +
            ")\n";
        LOG.info("Query: {}", query);

        statement.executeUpdate("CREATE TABLE wcc AS " + query);

        LOG.info(String.format("Processing ends at: %d", System.currentTimeMillis()));

        // export results
        statement.executeUpdate(String.format("COPY wcc TO '%s' (DELIMITER ' ')", outputPath));
    }

    @Override
    public void compute() throws SQLException {
        switch (iterationStrategy) {
            case EXTERNAL_DRIVER:
                computeExternalDriver();
                break;
            case USING_KEY:
                computeUsingKey();
                break;
            case LOOP:
                computeLoop();
                break;
        }
    }

}
