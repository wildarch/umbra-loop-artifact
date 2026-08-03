package science.atlarge.graphalytics.duckdb.algorithms.wcc;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import science.atlarge.graphalytics.duckdb.DuckDBComputation;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WeaklyConnectedComponentsComputation extends DuckDBComputation {

    private final boolean usingKey;

    public WeaklyConnectedComponentsComputation(Statement statement, String outputPath, boolean usingKey) {
        super(statement, outputPath);
        this.usingKey = usingKey;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS wcc");
    }

    @Override
    public void compute() throws SQLException {
        CommandLine commandLine = new CommandLine("bin/py/wcc.py");
        commandLine.addArgument("/tmp/graphalytics.duckdb");
        commandLine.addArgument("--out");
        commandLine.addArgument(outputPath);
        if (usingKey) {
            commandLine.addArgument("--using-key");
        }

        String commandString = StringUtils.toString(commandLine.toStrings(), " ");
        LOG.info(String.format("Execute benchmark job with command-line: [%s]", commandString));

        Executor executor = new DefaultExecutor();
        executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
        executor.setExitValue(0);
        int code = 0;
        try {
            code = executor.execute(commandLine);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (code != 0) {
            throw new RuntimeException("Failed to execute");
        }
    }

}
