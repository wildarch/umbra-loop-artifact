package science.atlarge.graphalytics.duckdb.test;

import org.junit.Test;
import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;
import science.atlarge.graphalytics.duckdb.algorithms.pr.PageRankComputation;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PageRankTest {

    @Test
    public void testUndirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadUndirected(statement);
        PageRankComputation c = new PageRankComputation(statement, "/tmp/output.csv", 2, 0.85);
        c.execute();
    }

    @Test
    public void testDirected() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        ExampleGraphLoader.loadDirected(statement);
        PageRankComputation c = new PageRankComputation(statement, "/tmp/output.csv", 2, 0.85);
        c.execute();
    }

    @Test
    public void testPageRankDirectedGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-pr-directed.v", "example-data-sets/graphs/test-pr-directed.e", true, false);
        duckDBLoadComputation.load();
        PageRankComputation c = new PageRankComputation(statement, "/tmp/output.csv", 14, 0.85);
        c.execute();
    }

    @Test
    public void testPageRankUndirectedGraph() throws SQLException, ClassNotFoundException {
        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(
                statement, "example-data-sets/graphs/test-pr-undirected.v", "example-data-sets/graphs/test-pr-undirected.e", true, false);
        duckDBLoadComputation.load();
        PageRankComputation c = new PageRankComputation(statement, "/tmp/output.csv", 26, 0.85);
        c.execute();
    }

}
