package science.atlarge.graphalytics.duckdb.test;

import org.junit.Test;
import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;
import science.atlarge.graphalytics.duckdb.algorithms.cdlp.CommunityDetectionLPComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CommunityDetectionLPTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, "/tmp/output.csv", 2);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, "/tmp/output.csv", 2);
        c.execute();
    }

    @Test
    public void testDirectedCdlpTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-cdlp-directed.v", "example-data-sets/graphs/test-cdlp-directed.e", true, false);
        duckDBLoadComputation.load();
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, "/tmp/output.csv", 5);
        c.execute();
    }

    @Test
    public void testUndirectedCdlpTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-cdlp-undirected.v", "example-data-sets/graphs/test-cdlp-undirected.e", true, false);
        duckDBLoadComputation.load();
        CommunityDetectionLPComputation c = new CommunityDetectionLPComputation(statement, "/tmp/output.csv", 5);
        c.execute();
    }

}
