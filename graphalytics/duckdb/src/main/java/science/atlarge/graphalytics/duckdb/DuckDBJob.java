package science.atlarge.graphalytics.duckdb;

import org.apache.commons.exec.CommandLine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.BenchmarkRunSetup;
import science.atlarge.graphalytics.execution.RunSpecification;

import java.io.IOException;


/**
 * Base class for all jobs in the platform driver. Configures and executes a platform job using the parameters
 * and executable specified by the subclass for a specific algorithm.
 */
public abstract class DuckDBJob {

	protected static final Logger LOG = LogManager.getLogger();

	protected CommandLine commandLine;
    private final String jobId;
	private final String logPath;
	private final String inputPath;
	private final String outputPath;

	protected final RunSpecification runSpecification;
	protected final Graph benchmarkGraph;

	protected final DuckDBConfiguration platformConfig;

	/**
     * Initializes the platform job with its parameters.
	 * @param runSpecification the benchmark run specification.
	 * @param platformConfig the platform configuration.
	 * @param inputPath the file path of the input graph dataset.
	 * @param outputPath the file path of the output graph dataset.
	 */
	public DuckDBJob(RunSpecification runSpecification, DuckDBConfiguration platformConfig,
					 String inputPath, String outputPath, Graph benchmarkGraph) {
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();

		this.jobId = benchmarkRun.getId();
		this.logPath = benchmarkRunSetup.getLogDir().resolve("platform").toString();

		this.inputPath = inputPath;
		this.outputPath = outputPath;

		this.platformConfig = platformConfig;
		this.runSpecification = runSpecification;
		this.benchmarkGraph = benchmarkGraph;
	}

	/**
	 * Executes the platform job with the pre-defined parameters.
	 *
	 * @return the exit code
	 * @throws IOException if the platform failed to run
	 */
	public abstract void execute() throws Exception;

	protected String getJobId() {
		return jobId;
	}

	public String getLogPath() {
		return logPath;
	}

	protected String getInputPath() {
		return inputPath;
	}

	protected String getOutputPath() {
		return outputPath;
	}

}
