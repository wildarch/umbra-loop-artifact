package science.atlarge.graphalytics.duckdb.algorithms.lcc;

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
 * Local Clustering Coefficient job implementatione for DuckDB. This class is responsible for formatting LCC-specific
 * arguments to be passed to the platform executable, and does not include the implementation of the algorithm.
 */
public final class LocalClusteringCoefficientJob extends DuckDBJob {

	/**
	 * Creates a new LocalClusteringCoefficientJob object with all mandatory parameters specified.
	 * @param platformConfig the platform configuration.
	 * @param inputPath th path to the input graph.
	 */
	public LocalClusteringCoefficientJob(RunSpecification runSpecification, DuckDBConfiguration platformConfig,
										 String inputPath, String outputPath, Graph benchmarkGraph) {
		super(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
	}

	@Override
	public void execute() throws SQLException, ClassNotFoundException, IOException {
		Connection conn = DuckDBUtil.getConnection();
		Statement statement = conn.createStatement();

		LocalClusteringCoefficientComputation localClusteringCoefficientComputation = new LocalClusteringCoefficientComputation(statement, getOutputPath());
		localClusteringCoefficientComputation.execute();
	}

}
