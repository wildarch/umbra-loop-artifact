package science.atlarge.graphalytics.avantgraph.algorithms.wcc;

import science.atlarge.graphalytics.avantgraph.AvantgraphConfiguration;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.avantgraph.AvantgraphJob;

import java.nio.file.Path;

/**
 * Weakly Connected Components job implementation for AvantGraph. This class is responsible for formatting WCC-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 *
 * @author Anonymous authors
 */
public final class WeaklyConnectedComponents extends AvantgraphJob {

    /**
     * Creates a new BreadthFirstSearchJob object with all mandatory parameters specified.
     * @param platformConfig the platform configuration.
     * @param inputDir       the path to the input graph.
     */
    public WeaklyConnectedComponents(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
                                     String inputDir, String outputPath, Graph benchmarkGraph) {
        super(runSpecification, platformConfig, inputDir, outputPath, benchmarkGraph);
    }

    @Override
    public Path getQueryPath(RunSpecification runSpecification) {
        boolean directed = runSpecification.getBenchmarkRun().getGraph().isDirected();
        return platformConfig.getAlgorithmsDirectory().resolve(
                directed ? "wcc.ipr" : "wcc_undirected.ipr");
    }
}
