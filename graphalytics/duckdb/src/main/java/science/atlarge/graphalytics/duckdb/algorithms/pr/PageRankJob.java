package science.atlarge.graphalytics.duckdb.algorithms.pr;

import science.atlarge.graphalytics.domain.algorithms.PageRankParameters;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.RunSpecification;
import science.atlarge.graphalytics.duckdb.DuckDBConfiguration;
import science.atlarge.graphalytics.duckdb.DuckDBJob;
import science.atlarge.graphalytics.duckdb.DuckDBUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * PageRank job implementation for DuckDB. This class is responsible for formatting PR-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class PageRankJob extends DuckDBJob {

    /**
     * Creates a new PageRankJob object with all mandatory parameters specified.
     *
     * @param platformConfig the platform configuration.
     * @param inputPath      the path to the input graph.
     */
    public PageRankJob(RunSpecification runSpecification, DuckDBConfiguration platformConfig,
                       String inputPath, String outputPath, Graph benchmarkGraph) {
        super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
    }

    @Override
    public void execute() throws SQLException, IOException, ClassNotFoundException {
        PageRankParameters params = (PageRankParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();

        Connection conn = DuckDBUtil.getConnection();
        Statement statement = conn.createStatement();

        PageRankComputation pageRankComputation = new PageRankComputation(statement, getOutputPath(), params.getNumberOfIterations(), params.getDampingFactor());
        pageRankComputation.execute();
    }

}
