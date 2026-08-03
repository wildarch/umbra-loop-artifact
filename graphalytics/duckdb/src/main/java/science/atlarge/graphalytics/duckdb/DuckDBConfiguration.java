package science.atlarge.graphalytics.duckdb;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;

/**
 * Collection of configurable platform options.
 */
public final class DuckDBConfiguration {

	protected static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String USING_KEY_KEY = "platform.duckdb.using-key";

	private boolean usingKey = false;

	public void setUsingKey(boolean usingKey) {
		this.usingKey = usingKey;
	}

	public boolean getUsingKey() {
		return usingKey;
	}

	/**
	 * Creates a new UmbraConfiguration object to capture all platform parameters that are not specific to any algorithm.
	 */
	public DuckDBConfiguration(){
	}

	public static DuckDBConfiguration parsePropertiesFile() {

		DuckDBConfiguration platformConfig = new DuckDBConfiguration();

		Configuration configuration = null;
		try {
			configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
		} catch (Exception e) {
			LOG.warn(String.format("Failed to load configuration from %s", BENCHMARK_PROPERTIES_FILE));
			throw new GraphalyticsExecutionException("Failed to load configuration. Benchmark run aborted.", e);
		}

		Boolean usingKey = configuration.getBoolean(USING_KEY_KEY, null);
		if (usingKey != null) {
			platformConfig.setUsingKey(usingKey);
		}

		return platformConfig;
	}

}
