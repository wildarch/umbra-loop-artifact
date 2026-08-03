package science.atlarge.graphalytics.duckdb.algorithms.pr;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import science.atlarge.graphalytics.duckdb.DuckDBComputation;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class PageRankComputation extends DuckDBComputation {

    protected int maxIterations;
    protected double dampingFactor;

    public PageRankComputation(Statement statement, String outputPath, int maxIterations, double dampingFactor) {
        super(statement, outputPath);
        this.maxIterations = maxIterations;
        this.dampingFactor = dampingFactor;
    }

    @Override
    public void cleanup() throws SQLException {
        statement.executeUpdate("DROP TABLE IF EXISTS dangling");
        statement.executeUpdate("DROP TABLE IF EXISTS e_with_source_outdegrees");
    }

    @Override
    public void compute() throws SQLException {
        CommandLine commandLine = new CommandLine("bin/py/pr.py");
        commandLine.addArgument("/tmp/graphalytics.duckdb");
        commandLine.addArgument("--iterations");
        commandLine.addArgument(String.valueOf(maxIterations));
        commandLine.addArgument("--damping");
        commandLine.addArgument(String.valueOf(dampingFactor));
        commandLine.addArgument("--out");
        commandLine.addArgument(outputPath);

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
