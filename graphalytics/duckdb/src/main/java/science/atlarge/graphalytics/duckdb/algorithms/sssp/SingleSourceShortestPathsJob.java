package science.atlarge.graphalytics.duckdb.algorithms.sssp;

import science.atlarge.graphalytics.domain.algorithms.SingleSourceShortestPathsParameters;
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
 * Single Source Shortest Paths job implementation for DuckDB. This class is responsible for formatting SSSP-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class SingleSourceShortestPathsJob extends DuckDBJob {

	/**
	 * Creates a new SingleSourceShortestPathsJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public SingleSourceShortestPathsJob(RunSpecification runSpecification, DuckDBConfiguration platformConfig,
										String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}

	@Override
	public void execute() throws SQLException, IOException, ClassNotFoundException {
		SingleSourceShortestPathsParameters params = (SingleSourceShortestPathsParameters) runSpecification.getBenchmarkRun().getAlgorithmParameters();

		Connection conn = DuckDBUtil.getConnection();
		Statement statement = conn.createStatement();

		SingleSourceShortestPathsComputation singleSourceShortestPathComputation = new SingleSourceShortestPathsComputation(
				statement,
				getOutputPath(),
				params.getSourceVertex(),
				platformConfig.getUsingKey());
		singleSourceShortestPathComputation.execute();
	}
}
