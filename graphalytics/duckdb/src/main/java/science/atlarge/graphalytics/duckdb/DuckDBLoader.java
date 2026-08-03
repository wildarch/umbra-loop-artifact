package science.atlarge.graphalytics.duckdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Base class for graph loading in the platform driver.
 */
public class DuckDBLoader {

	private static final Logger LOG = LogManager.getLogger();

	protected FormattedGraph formattedGraph;
	protected DuckDBConfiguration platformConfig;
	protected DuckDBLoadComputation duckDBLoadComputation;

	/**
	 * Graph loader for DuckDB.
	 * @param formattedGraph
	 * @param platformConfig
	 */
	public DuckDBLoader(FormattedGraph formattedGraph, DuckDBConfiguration platformConfig) throws SQLException, ClassNotFoundException {
		this.formattedGraph = formattedGraph;
		this.platformConfig = platformConfig;
		Connection conn = DuckDBUtil.getConnection();
		Statement statement = conn.createStatement();

		duckDBLoadComputation = new DuckDBLoadComputation(statement,
				formattedGraph.getVertexFilePath(),
				formattedGraph.getEdgeFilePath(),
				formattedGraph.isDirected(),
				formattedGraph.hasEdgeProperties()
		);
	}

	public void load() throws Exception {
		duckDBLoadComputation.load();
	}

	public void unload() throws Exception {
		duckDBLoadComputation.unload();
	}

}
