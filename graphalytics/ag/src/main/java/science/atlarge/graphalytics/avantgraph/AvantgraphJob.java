package science.atlarge.graphalytics.avantgraph;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.execution.BenchmarkRunSetup;
import science.atlarge.graphalytics.execution.RunSpecification;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Base class for all jobs in the platform driver. Configures and executes a platform job using the parameters
 * and executable specified by the subclass for a specific algorithm.
 */
public abstract class AvantgraphJob {

	private static final Logger LOG = LogManager.getLogger();

	protected CommandLine commandLine;
    private final String jobId;
	private final String logPath;
	private final String inputDir;
	private final String outputFile;

	protected final RunSpecification runSpecification;
	protected final Graph benchmarkGraph;

	protected final AvantgraphConfiguration platformConfig;

	/**
     * Initializes the platform job with its parameters.
	 * @param runSpecification the benchmark run specification.
	 * @param platformConfig the platform configuration.
	 * @param inputDir the file path of the input graph dataset.
	 * @param outputFile the file path of the output graph dataset.
	 */
	public AvantgraphJob(RunSpecification runSpecification, AvantgraphConfiguration platformConfig,
						 String inputDir, String outputFile, Graph benchmarkGraph) {
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();

		this.jobId = benchmarkRun.getId();
		this.logPath = benchmarkRunSetup.getLogDir().resolve("platform").toString();

		this.inputDir = inputDir;
		this.outputFile = outputFile;

		this.platformConfig = platformConfig;
		this.runSpecification = runSpecification;
		this.benchmarkGraph = benchmarkGraph;
	}

	public abstract Path getQueryPath(RunSpecification runSpecification) throws IOException;
	public void postProcess(String outputFile) throws IOException {}

	/**
	 * Executes the platform job with the pre-defined parameters.
	 *
	 * @return the exit code
	 * @throws IOException if the platform failed to run
	 */
	public int execute() throws Exception {
		String executableDir = platformConfig.getExecutablePath();
		commandLine = new CommandLine(Paths.get(executableDir).toFile());

		// List of benchmark parameters.
		String jobId = getJobId();
		String logDir = getLogPath();

		// List of dataset parameters.
		String inputDir = getInputDir();
		String outputFile = getOutputFile();

		commandLine.addArgument("--output=id,val");
		commandLine.addArgument("--output-format=csv");
		commandLine.addArgument("--output-path=" + outputFile);
		// 16 threads
		commandLine.addArgument("--cpu-preferences-worker=0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
		commandLine.addArgument("--print-execution-time");

		commandLine.addArgument(inputDir);
		commandLine.addArgument(getQueryPath(runSpecification).toString());

		String commandString = StringUtils.toString(commandLine.toStrings(), " ");
		LOG.info(String.format("Execute benchmark job with command-line: [%s]", commandString));

		Executor executor = new DefaultExecutor();
		executor.setStreamHandler(new PumpStreamHandler(System.out, System.err));
		executor.setExitValue(0);
		int code = executor.execute(commandLine);

		if (code == 0) {
			postProcess(outputFile);
		}

		return code;
	}

	private String getJobId() {
		return jobId;
	}

	public String getLogPath() {
		return logPath;
	}

	protected String getInputDir() {
		return inputDir;
	}

	protected String getOutputFile() {
		return outputFile;
	}

}
