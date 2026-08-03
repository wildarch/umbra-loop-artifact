package science.atlarge.graphalytics.avantgraph.algorithms.lcc;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.avantgraph.AvantgraphConfiguration;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.avantgraph.AvantgraphJob;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Local Clustering Coefficient job implementation for AvantGraph. This class is responsible for formatting LCC-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 *
 * @author Anonymous authors
 */
public final class LocalClusteringCoefficientJob extends AvantgraphJob {
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * Creates a new LocalClusteringCoefficientJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputDir the path to the input graph.
	 */
	public LocalClusteringCoefficientJob(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
										 String inputDir, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputDir, outputPath, benchmarkGraph);
	}

	@Override
	public Path getQueryPath(RunSpecification runSpecification) {
		if (runSpecification.getBenchmarkRun().getFormattedGraph().isDirected()) {
			return platformConfig.getAlgorithmsDirectory().resolve("lcc.ipr");
		} else {
			return platformConfig.getAlgorithmsDirectory().resolve("lcc_undirected.ipr");
		}
	}

	@Override
	public void postProcess(String outputFile) throws IOException {
		var relabelScript = platformConfig.getAlgorithmsDirectory().resolve("lcc_relabel.py");
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
