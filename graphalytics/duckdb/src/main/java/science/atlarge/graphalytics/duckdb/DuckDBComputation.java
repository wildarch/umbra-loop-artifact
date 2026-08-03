package science.atlarge.graphalytics.duckdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.sql.Statement;

public abstract class DuckDBComputation {

    protected final Statement statement;
    protected final String outputPath;
    protected final Logger LOG = LogManager.getLogger();


    public DuckDBComputation(Statement statement, String outputPath) {
        this.statement = statement;
        this.outputPath = outputPath;
    }

    public abstract void cleanup() throws SQLException;

    public abstract void compute() throws SQLException;

    public void execute() throws SQLException {
        cleanup();
        compute();
        cleanup();
    }

}
