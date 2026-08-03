package science.atlarge.graphalytics.duckdb.algorithms.sssp;

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

public class SingleSourceShortestPathsComputation extends DuckDBComputation {

    protected long sourceVertex;
    private final boolean usingKey;

    public SingleSourceShortestPathsComputation(
            Statement statement,
            String outputPath,
            long sourceVertex,
            boolean usingKey) {
        super(statement, outputPath);
        this.sourceVertex = sourceVertex;
        this.usingKey = usingKey;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS d");
        statement.executeUpdate("DROP TABLE IF EXISTS d2");
        statement.executeUpdate("DROP TABLE IF EXISTS sssp");
    }

    @Override
    public void compute() throws SQLException {
        CommandLine commandLine = new CommandLine("bin/py/sssp.py");
        commandLine.addArgument("/tmp/graphalytics.duckdb");
        commandLine.addArgument(String.valueOf(sourceVertex));
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
