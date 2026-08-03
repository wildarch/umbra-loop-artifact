package science.atlarge.graphalytics.duckdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DuckDBUtil {

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.duckdb.DuckDBDriver");
        Connection conn = DriverManager.getConnection("jdbc:duckdb:");
        return conn;
    }

}
