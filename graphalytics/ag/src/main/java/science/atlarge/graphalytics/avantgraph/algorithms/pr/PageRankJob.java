package science.atlarge.graphalytics.avantgraph.algorithms.pr;

import science.atlarge.graphalytics.avantgraph.AvantgraphConfiguration;
import science.atlarge.graphalytics.domain.algorithms.CommunityDetectionLPParameters;
import science.atlarge.graphalytics.domain.algorithms.PageRankParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.avantgraph.AvantgraphJob;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * PageRank job implementation for AvantGraph. This class is responsible for formatting PR-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 *
 * @author Anonymous authors
 */
public final class PageRankJob extends AvantgraphJob {

    /**
     * Creates a new PageRankJob object with all mandatory parameters specified.
     *
     * @param platformConfig the platform configuration.
     * @param inputDir      the path to the input graph.
     */
    public PageRankJob(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
                       String inputDir, String outputPath, Graph benchmarkGraph) {
        super(runSpecification, platformConfig, inputDir, outputPath, benchmarkGraph);
    }

    @Override
    public Path getQueryPath(RunSpecification runSpecification) throws IOException {
        PageRankParameters params =
                (PageRankParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();
        var inputFile = platformConfig.getAlgorithmsDirectory().resolve("pr.ipr");
        var inputContents = Files.readString(inputFile);
        inputContents = inputContents
                .replace("damping = real(0.85)", String.format("damping = real(%f)", params.getDampingFactor()))
                .replace("iterations = int(10)", String.format("iterations = int(%d)", params.getNumberOfIterations()));
        var tmpFile = Path.of("/tmp/pr.ipr");
        Files.writeString(tmpFile, inputContents);
        return tmpFile;
    }
}
