package science.atlarge.graphalytics.avantgraph;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;


/**
 * Base class for graph loading in the platform driver.
 *
 * @author Bálint Hegyi
 */
public class AvantgraphLoader {

	private static final Logger LOG = LogManager.getLogger();

	protected CommandLine commandLine;
	protected FormattedGraph formattedGraph;
	protected AvantgraphConfiguration platformConfig;


	/**
	 *	Graph loader for AvantGraph.
	 * @param formattedGraph
	 * @param platformConfig
	 */
	public AvantgraphLoader(FormattedGraph formattedGraph, AvantgraphConfiguration platformConfig) {
		this.formattedGraph = formattedGraph;
		this.platformConfig = platformConfig;
	}

	public int load(String loadedInputPath) throws Exception {
		var inputPath = Path.of(loadedInputPath);
		if (Files.exists(inputPath)) {
			unload(loadedInputPath);
		}
		Files.createDirectories(inputPath);

		var loadScript = Paths.get("./bin/py/load.py");
		commandLine = new CommandLine(loadScript.toFile());
		commandLine.addArgument(loadedInputPath);
		commandLine.addArgument("--vertices=" + formattedGraph.getVertexFilePath());
		commandLine.addArgument("--edges=" + formattedGraph.getEdgeFilePath());
		commandLine.addArgument("--dbfile=" + loadedInputPath + "/graph.duckdb");
		if (formattedGraph.hasEdgeProperties()) {
			commandLine.addArgument("--weighted");
		}

		if (!formattedGraph.isDirected()) {
			commandLine.addArgument("--undirected");
		}

		commandLine.addArgument("--ag-schema=" + platformConfig.getSchemaPath());
		commandLine.addArgument("--ag-load-graph=" + platformConfig.getLoaderPath());
		commandLine.addArgument("--ag-csr=" + platformConfig.getCSRPath());

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute graph loader with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		return executor.execute(commandLine);
	}

	public int unload(String loadedInputPath) throws Exception {
		commandLine = new CommandLine("rm");
		commandLine.addArgument("-r");
		commandLine.addArgument(loadedInputPath);

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Delete input graph with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		return executor.execute(commandLine);
	}

}
