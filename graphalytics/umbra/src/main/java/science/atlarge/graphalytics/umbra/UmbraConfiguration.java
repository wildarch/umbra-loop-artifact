package science.atlarge.graphalytics.umbra;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;

/**
 * Collection of configurable platform options.
 */
public final class UmbraConfiguration {
	protected static final Logger LOG = LogManager.getLogger();

	public enum IterationStrategy {
		EXTERNAL_DRIVER,
		USING_KEY,
		LOOP,
	}

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String SERVER_PORT_KEY = "platform.umbra.server-port";
	private static final String ITERATION_STRATEGY_KEY = "platform.umbra.iteration-strategy";

	private long serverPort = 5432;
	private IterationStrategy iterationStrategy = IterationStrategy.EXTERNAL_DRIVER;

	/**
	 * Creates a new UmbraConfiguration object to capture all platform parameters that are not specific to any algorithm.
	 */
	public UmbraConfiguration(){
	}

	public void setServerPort(long p) { serverPort = p; }
	public long getServerPort() { return serverPort; }

	public void setIterationStrategy(IterationStrategy strategy) {
        this.iterationStrategy = strategy;
    }
	public IterationStrategy getIterationStrategy() { return iterationStrategy; }

	public static UmbraConfiguration parsePropertiesFile() {

		UmbraConfiguration platformConfig = new UmbraConfiguration();

		Configuration configuration = null;
		try {
			configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
		} catch (Exception e) {
			LOG.warn("Failed to load configuration from {}", BENCHMARK_PROPERTIES_FILE);
			throw new GraphalyticsExecutionException("Failed to load configuration. Benchmark run aborted.", e);
		}

		long serverPort = configuration.getLong(SERVER_PORT_KEY, 5432);
		platformConfig.setServerPort(serverPort);

        String iterationStrategy = configuration.getString(ITERATION_STRATEGY_KEY, "external-driver");
        if (iterationStrategy.equals("external-driver")) {
            platformConfig.setIterationStrategy(IterationStrategy.EXTERNAL_DRIVER);
        } else if (iterationStrategy.equals("using-key")) {
            platformConfig.setIterationStrategy(IterationStrategy.USING_KEY);
        } else if (iterationStrategy.equals("loop")) {
            platformConfig.setIterationStrategy(IterationStrategy.LOOP);
        } else {
            LOG.error("Invalid iteration strategy '{}'", iterationStrategy);
            throw new GraphalyticsExecutionException("Invalid iteration strategy setting");
        }

		return platformConfig;
	}

}
