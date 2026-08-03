package science.atlarge.graphalytics.duckdb.test;

import science.atlarge.graphalytics.duckdb.DuckDBLoadComputation;

import java.sql.SQLException;
import java.sql.Statement;

public class ExampleGraphLoader {

    public static void loadDirected(Statement statement) throws SQLException {
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(statement, "example-data-sets/graphs/example-directed.v", "example-data-sets/graphs/example-directed.e", true, true);
        duckDBLoadComputation.load();
    }

    public static void loadUndirected(Statement statement) throws SQLException {
        DuckDBLoadComputation duckDBLoadComputation = new DuckDBLoadComputation(statement, "example-data-sets/graphs/example-undirected.v", "example-data-sets/graphs/example-undirected.e", true, true);
        duckDBLoadComputation.load();
    }

}
