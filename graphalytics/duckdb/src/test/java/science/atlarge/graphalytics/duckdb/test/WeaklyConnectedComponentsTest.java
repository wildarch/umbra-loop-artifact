package science.atlarge.graphalytics.duckdb.test;

import org.junit.Test;
import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;
import science.atlarge.graphalytics.duckdb.algorithms.wcc.WeaklyConnectedComponentsComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class WeaklyConnectedComponentsTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement, "/tmp/output.csv", false);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement, "/tmp/output.csv", false);
        c.execute();
    }

    @Test
    public void testDirectedWccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-wcc-directed.v", "example-data-sets/graphs/test-wcc-directed.e", true, false);
        duckDBLoadComputation.load();
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement, "/tmp/output.csv", false);
        c.execute();
    }

    @Test
    public void testUndirectedWccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-wcc-undirected.v", "example-data-sets/graphs/test-wcc-undirected.e", true, false);
        duckDBLoadComputation.load();
        WeaklyConnectedComponentsComputation c = new WeaklyConnectedComponentsComputation(statement, "/tmp/output.csv", false);
        c.execute();
    }

}
