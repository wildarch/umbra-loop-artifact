package science.atlarge.graphalytics.duckdb.algorithms.wcc;

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
 * Breadth First Search job implementation for DuckDB. This class is responsible for formatting BFS-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class WeaklyConnectedComponentsJob extends DuckDBJob {

	/**
	 * Creates a new BreadthFirstSearchJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the path to the input graph.
	 */
	public WeaklyConnectedComponentsJob(RunSpecification runSpecification, DuckDBConfiguration platformConfig,
										String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}


	@Override
	public void execute() throws SQLException, ClassNotFoundException, IOException {
		Connection conn = DuckDBUtil.getConnection();
		Statement statement = conn.createStatement();

		WeaklyConnectedComponentsComputation weaklyConnectedComponentsComputation = new WeaklyConnectedComponentsComputation(
				statement,
				getOutputPath(),
				platformConfig.getUsingKey());
		weaklyConnectedComponentsComputation.execute();
	}

}
