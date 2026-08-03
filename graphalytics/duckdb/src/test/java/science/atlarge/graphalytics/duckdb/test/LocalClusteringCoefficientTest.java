package science.atlarge.graphalytics.duckdb.test;

import org.junit.Test;
import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;
import science.atlarge.graphalytics.duckdb.algorithms.lcc.LocalClusteringCoefficientComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalClusteringCoefficientTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement, "/tmp/output.csv");
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement, "/tmp/output.csv");
        c.execute();
    }

    @Test
    public void testDirectedLccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-lcc-directed.v", "example-data-sets/graphs/test-lcc-directed.e", true, false);
        duckDBLoadComputation.load();
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement, "/tmp/output.csv");
        c.execute();
    }

    @Test
    public void testUndirectedLccTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-lcc-undirected.v", "example-data-sets/graphs/test-lcc-undirected.e", true, false);
        duckDBLoadComputation.load();
        LocalClusteringCoefficientComputation c = new LocalClusteringCoefficientComputation(statement, "/tmp/output.csv");
        c.execute();
    }

}
