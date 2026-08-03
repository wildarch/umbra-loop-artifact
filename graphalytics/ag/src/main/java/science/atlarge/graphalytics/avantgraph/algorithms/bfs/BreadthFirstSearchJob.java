package science.atlarge.graphalytics.avantgraph.algorithms.bfs;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.avantgraph.AvantgraphConfiguration;
import science.atlarge.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.avantgraph.AvantgraphJob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Breadth First Search job implementation for AvantGraph. This class is responsible for formatting BFS-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class BreadthFirstSearchJob extends AvantgraphJob {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Creates a new BreadthFirstSearchJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputDir the path to the input graph.
	 */
	public BreadthFirstSearchJob(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
                                 String inputDir, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputDir, outputPath, benchmarkGraph);
	}

	@Override
	public Path getQueryPath(RunSpecification runSpecification) throws IOException {
		var params = (BreadthFirstSearchParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
		var inputFile = platformConfig.getAlgorithmsDirectory().resolve("bfs.ipr");
		var inputContents = Files.readString(inputFile);
		inputContents = inputContents.replace("%v.orig_id = \"1\"", "%v.orig_id = \"" + params.getSourceVertex() + "\"");
		var tmpFile = Path.of("/tmp/bfs.ipr");
		Files.writeString(tmpFile, inputContents);
		return tmpFile;
    }

	@Override
	public void postProcess(String outputFile) throws IOException {
		var relabelScript = platformConfig.getAlgorithmsDirectory().resolve("bfs_relabel.py");
		var commandLine = new CommandLine(relabelScript.toFile());
		commandLine.addArgument(runSpecification.getBenchmarkRun().getFormattedGraph().getVertexFilePath());
		commandLine.addArgument(outputFile);
		commandLine.addArgument(outputFile);

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Apply post-processing: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);

		if (executor.execute(commandLine) != 0) {
			throw new IllegalStateException("postprocessing failed");
		}
    }
}
