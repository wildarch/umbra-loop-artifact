package science.atlarge.graphalytics.avantgraph.algorithms.sssp;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import science.atlarge.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.avantgraph.AvantgraphConfiguration;
import science.atlarge.graphalytics.avantgraph.AvantgraphJob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Single Source Shortest Paths job implementation for AvantGraph. This class is responsible for formatting
 * SSSP-specific arguments to be passed to the platform executable, and does not include the implementation of the
 * algorithm.
 *
 * @author Anonymous authors
 */
public final class SingleSourceShortestPathsJob extends AvantgraphJob {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Creates a new SingleSourceShortestPathsJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputDir the path to the input graph.
	 */
	public SingleSourceShortestPathsJob(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
										String inputDir, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputDir, outputPath, benchmarkGraph);
	}

	@Override
	public Path getQueryPath(RunSpecification runSpecification) throws IOException {
		var params = (SingleSourceShortestPathsParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
		var inputFile = platformConfig.getAlgorithmsDirectory().resolve("sssp.ipr");
		var inputContents = Files.readString(inputFile);
		inputContents = inputContents.replace("%v.orig_id = \"1\"", "%v.orig_id = \"" + params.getSourceVertex() + "\"");
		var tmpFile = Path.of("/tmp/sssp.ipr");
		Files.writeString(tmpFile, inputContents);
		return tmpFile;
	}

	@Override
	public void postProcess(String outputFile) throws IOException {
		var relabelScript = platformConfig.getAlgorithmsDirectory().resolve("sssp_relabel.py");
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
