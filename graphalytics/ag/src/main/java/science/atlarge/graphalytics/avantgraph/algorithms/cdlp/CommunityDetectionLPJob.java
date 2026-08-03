package science.atlarge.graphalytics.avantgraph.algorithms.cdlp;

import science.atlarge.graphalytics.avantgraph.AvantgraphConfiguration;
import science.atlarge.graphalytics.avantgraph.AvantgraphJob;
import science.atlarge.graphalytics.domain.algorithms.BreadthFirstSearchParameters;
import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Community Detection by job implementation for AvantGraph. This class is responsible for formatting CDLP-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 *
 * @author Anonymous authors
 */
public final class CommunityDetectionLPJob extends AvantgraphJob {

	/**
	 * Creates a new LocalClusteringCoefficientJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputDir the path to the input graph.
	 */
	public CommunityDetectionLPJob(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
                                   String inputDir, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputDir, outputPath, benchmarkGraph);
	}

	@Override
	public Path getQueryPath(RunSpecification runSpecification) throws IOException {
		var params = (CommunityDetectionLPParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
        boolean directed = runSpecification.getBenchmarkRun().getGraph().isDirected();
        var inputFile = platformConfig.getAlgorithmsDirectory().resolve(
                directed ? "cdlp.ipr" : "cdlp_undirected.ipr");
		var inputContents = Files.readString(inputFile);
		inputContents = inputContents.replace(
				"iterations = int(5)",
				String.format("iterations = int(%d)", params.getMaxIterations()));
		var tmpFile = Path.of("/tmp/cdlp.ipr");
		Files.writeString(tmpFile, inputContents);
		return tmpFile;
	}
}
