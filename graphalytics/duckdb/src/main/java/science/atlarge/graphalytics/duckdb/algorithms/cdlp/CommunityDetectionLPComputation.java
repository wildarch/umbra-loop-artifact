package science.atlarge.graphalytics.duckdb.algorithms.cdlp;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import science.atlarge.graphalytics.duckdb.DuckDBComputation;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class CommunityDetectionLPComputation extends DuckDBComputation {

    protected int numIterations;

    public CommunityDetectionLPComputation(Statement statement, String outputPath, int numIterations) {
        super(statement, outputPath);
        this.numIterations = numIterations;
    }

    @Override
    public void cleanup() throws SQLException {
        for (int i = 0; i <= numIterations; i++) {
            statement.executeUpdate(String.format("DROP TABLE IF EXISTS cdlp%d", i));
        }
    }

    @Override
    public void compute() throws SQLException {
        CommandLine commandLine = new CommandLine("bin/py/cdlp.py");
        commandLine.addArgument("/tmp/graphalytics.duckdb");
        commandLine.addArgument("--iterations");
        commandLine.addArgument(String.valueOf(numIterations));
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
