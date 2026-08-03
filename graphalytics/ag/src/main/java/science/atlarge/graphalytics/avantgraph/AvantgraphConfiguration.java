package science.atlarge.graphalytics.avantgraph;

import org.apache.commons.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.configuration.ConfigurationUtil;
import science.atlarge.graphalytics.configuration.GraphalyticsExecutionException;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Collection of configurable platform options.
 */
public final class AvantgraphConfiguration {

	protected static final Logger LOG = LogManager.getLogger();

	private static final String BENCHMARK_PROPERTIES_FILE = "benchmark.properties";
	private static final String AVANTGRAPH_SOURCE_DIRECTORY_KEY = "platform.avantgraph.source-directory";
	private static final String ALGORITHMS_DIRECTORY_KEY = "platform.avantgraph.algorithms-directory";

	private String loaderPath;
	private String unloaderPath;
	private String executablePath;
	private String terminatorPath;
	private Path sourceDirectory;
	private Path algorithmsDirectory;

	/**
	 * Creates a new AvantGraphConfiguration object to capture all platform parameters that are not specific to any
	 * algorithm.
	 */
	public AvantgraphConfiguration(){
	}

	public String getLoaderPath() {
		return "/usr/bin/ag-load-graph";
	}

	public Path getSchemaPath() {
		return Path.of("/usr/bin/ag-schema");
	}

	public Path getCSRPath() {
		return Path.of("/usr/bin/ag-csr");
	}

	public void setLoaderPath(String loaderPath) {
		this.loaderPath = loaderPath;
	}

	public String getUnloaderPath() {
		return unloaderPath;
	}

	public void setUnloaderPath(String unloaderPath) {
		this.unloaderPath = unloaderPath;
	}

	/**
	 * @param executablePath the directory containing executables
	 */
	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	/**
	 * @return the directory containing executables
	 */
	public String getExecutablePath() {
		return "/usr/bin/avantgraph";
	}

	public String getTerminatorPath() {
		return terminatorPath;
	}

	public void setTerminatorPath(String terminatorPath) {
		this.terminatorPath = terminatorPath;
	}

	public void setSourceDirectory(Path sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public void setAlgorithmsDirectory(Path algorithmsDirectory) {
		this.algorithmsDirectory = algorithmsDirectory;
	}

	public Path getAlgorithmsDirectory() {
		return this.algorithmsDirectory;
	}

	public static AvantgraphConfiguration parsePropertiesFile() {

		AvantgraphConfiguration platformConfig = new AvantgraphConfiguration();

		Configuration configuration = null;
		try {
			configuration = ConfigurationUtil.loadConfiguration(BENCHMARK_PROPERTIES_FILE);
		} catch (Exception e) {
			LOG.warn(String.format("Failed to load configuration from %s", BENCHMARK_PROPERTIES_FILE));
			throw new GraphalyticsExecutionException("Failed to load configuration. Benchmark run aborted.", e);
		}

		String loaderPath = Paths.get("./bin/sh/load-graph.sh").toString();
		platformConfig.setLoaderPath(loaderPath);

		String unloaderPath = Paths.get("./bin/sh/unload-graph.sh").toString();
		platformConfig.setUnloaderPath(unloaderPath);

		String executablePath = Paths.get("./bin/sh/execute-job.sh").toString();
		platformConfig.setExecutablePath(executablePath);

		String terminatorPath = Paths.get("./bin/sh/terminate-job.sh").toString();
		platformConfig.setTerminatorPath(terminatorPath);

		String avantgraphSourceDirectory = configuration.getString(AVANTGRAPH_SOURCE_DIRECTORY_KEY);
		platformConfig.setSourceDirectory(Paths.get(avantgraphSourceDirectory));

		Path algorithmsDirectory = Paths.get("./bin/algorithms");
		platformConfig.setAlgorithmsDirectory(algorithmsDirectory);

		return platformConfig;
	}

}
