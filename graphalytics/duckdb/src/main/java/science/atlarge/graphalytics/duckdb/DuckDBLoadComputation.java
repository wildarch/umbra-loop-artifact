package science.atlarge.graphalytics.duckdb;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.Statement;

public class DuckDBLoadComputation {
    private static final Path TEMP_DATABASE_PATH = Paths.get("/tmp/graphalytics.duckdb");

    protected final Logger LOG = LogManager.getLogger();

    protected Statement statement;
    protected String verticesFilePath;
    protected String edgesFilePath;
    protected boolean directed;
    protected boolean weighted;

    public DuckDBLoadComputation(Statement statement, String verticesFilePath, String edgesFilePath, boolean directed, boolean weighted) {
        this.statement = statement;
        this.verticesFilePath = verticesFilePath;
        this.edgesFilePath = edgesFilePath;
        this.directed = directed;
        this.weighted = weighted;
    }

    public void load() throws SQLException {
        CommandLine commandLine = new CommandLine("bin/py/load.py");
        commandLine.addArgument(TEMP_DATABASE_PATH.toString());
        commandLine.addArgument("--vertices");
        commandLine.addArgument(verticesFilePath);
        commandLine.addArgument("--edges");
        commandLine.addArgument(edgesFilePath);
        if (weighted) {
            commandLine.addArgument("--weighted");
        }
        if (!directed) {
            commandLine.addArgument("--undirected");
        }

        String commandString = StringUtils.toString(commandLine.toStrings(), " ");
        LOG.info(String.format("Execute loader with command-line: [%s]", commandString));

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

    public void unload() throws IOException {
        Files.deleteIfExists(TEMP_DATABASE_PATH);
    }

}
