package science.atlarge.graphalytics.duckdb.test;

import org.junit.Test;
import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;
import science.atlarge.graphalytics.duckdb.algorithms.sssp.SingleSourceShortestPathsComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SingleSourceShortestPathsTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(
                statement,
                "/tmp/output.csv",
                2,
                false);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(
                statement,
                "/tmp/output.csv",
                1,
                false);
        c.execute();
    }

    @Test
    public void testDirectedSsspTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-sssp-directed.v", "example-data-sets/graphs/test-sssp-directed.e", true, true);
        duckDBLoadComputation.load();
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(
                statement,
                "/tmp/output.csv",
                1,
                false);
        c.execute();
    }

    @Test
    public void testUndirectedSsspTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-sssp-undirected.v", "example-data-sets/graphs/test-sssp-undirected.e", true, true);
        duckDBLoadComputation.load();
        SingleSourceShortestPathsComputation c = new SingleSourceShortestPathsComputation(
                statement,
                "/tmp/output.csv",
                1,
                false);
        c.execute();
    }

}
