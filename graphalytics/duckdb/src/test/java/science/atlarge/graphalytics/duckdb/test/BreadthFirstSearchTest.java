package science.atlarge.graphalytics.duckdb.test;

import org.junit.Test;
import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;
import science.atlarge.graphalytics.duckdb.algorithms.bfs.BreadthFirstSearchComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BreadthFirstSearchTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        BreadthFirstSearchComputation breadthFirstSearchComputation = new BreadthFirstSearchComputation(statement, "/tmp/output.csv", 2);
        breadthFirstSearchComputation.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        BreadthFirstSearchComputation breadthFirstSearchComputation = new BreadthFirstSearchComputation(statement, "/tmp/output.csv", 1);
        breadthFirstSearchComputation.execute();
    }

    @Test
    public void testDirectedBfsTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-bfs-directed.v", "example-data-sets/graphs/test-bfs-directed.e", true, false);
        duckDBLoadComputation.load();
        BreadthFirstSearchComputation c = new BreadthFirstSearchComputation(statement, "/tmp/output.csv", 1);
        c.execute();
    }

    @Test
    public void testUndirectedBfsTestGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-bfs-undirected.v", "example-data-sets/graphs/test-bfs-undirected.e", true, false);
        duckDBLoadComputation.load();
        BreadthFirstSearchComputation c = new BreadthFirstSearchComputation(statement, "/tmp/output.csv", 1);
        c.execute();
    }
}
