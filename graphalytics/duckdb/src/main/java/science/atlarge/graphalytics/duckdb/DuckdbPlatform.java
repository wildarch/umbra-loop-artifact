package science.atlarge.graphalytics.duckdb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import science.atlarge.graphalytics.domain.algorithms.Algorithm;
import science.atlarge.graphalytics.domain.benchmark.BenchmarkRun;
import science.atlarge.graphalytics.domain.graph.FormattedGraph;
import science.atlarge.graphalytics.domain.graph.Graph;
import science.atlarge.graphalytics.domain.graph.LoadedGraph;
import science.atlarge.graphalytics.execution.*;
import science.atlarge.graphalytics.duckdb.algorithms.bfs.BreadthFirstSearchJob;
import science.atlarge.graphalytics.duckdb.algorithms.cdlp.CommunityDetectionLPJob;
import science.atlarge.graphalytics.duckdb.algorithms.lcc.LocalClusteringCoefficientJob;
import science.atlarge.graphalytics.duckdb.algorithms.pr.PageRankJob;
import science.atlarge.graphalytics.duckdb.algorithms.sssp.SingleSourceShortestPathsJob;
import science.atlarge.graphalytics.duckdb.algorithms.wcc.WeaklyConnectedComponentsJob;
import science.atlarge.graphalytics.report.result.BenchmarkMetrics;

import java.nio.file.Path;


/**
 * Umbra platform driver for the Graphalytics benchmark.
 *
 * Note: Need this exact naming so the graphalytics benchmark suite can find it.
 */
public class DuckdbPlatform implements Platform {

	protected static final Logger LOG = LogManager.getLogger();

	public static final String PLATFORM_NAME = "duckdb";
	public DuckDBLoader loader;

	@Override
	public void verifySetup() throws Exception { }

	@Override
	public LoadedGraph loadGraph(FormattedGraph formattedGraph) throws Exception {
		DuckDBConfiguration platformConfig = DuckDBConfiguration.parsePropertiesFile();

		loader = new DuckDBLoader(formattedGraph, platformConfig);

		LOG.info("Loading graph " + formattedGraph.getName());
		try {
			loader.load();
		} catch (Exception e) {
			throw new PlatformExecutionException("Failed to load a DuckDB dataset.", e);
		}
		LOG.info("Loaded graph " + formattedGraph.getName());
		return new LoadedGraph(formattedGraph, "");
	}

	@Override
	public void deleteGraph(LoadedGraph loadedGraph) throws Exception {
		if (loadedGraph == null) {
			LOG.warn("Graph failed to load, skipping unload.");
			return;
		}

		LOG.info("Unloading graph " + loadedGraph.getFormattedGraph().getName());
		try {
			loader.unload();
		} catch (Exception e) {
			throw new PlatformExecutionException("Failed to unload a DuckDB dataset.", e);
		}
		LOG.info("Unloaded graph " +  loadedGraph.getFormattedGraph().getName());
	}

	@Override
	public void prepare(RunSpecification runSpecification) throws Exception {

	}

	@Override
	public void startup(RunSpecification runSpecification) throws Exception {
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		Path logDir = benchmarkRunSetup.getLogDir().resolve("platform").resolve("runner.logs");
		DuckDBCollector.startPlatformLogging(logDir);
	}

	@Override
	public void run(RunSpecification runSpecification) throws PlatformExecutionException {
		BenchmarkRun benchmarkRun = runSpecification.getBenchmarkRun();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		RuntimeSetup runtimeSetup = runSpecification.getRuntimeSetup();

		Algorithm algorithm = benchmarkRun.getAlgorithm();
		DuckDBConfiguration platformConfig = DuckDBConfiguration.parsePropertiesFile();
		String inputPath = runtimeSetup.getLoadedGraph().getLoadedPath();
		String outputPath = benchmarkRunSetup.getOutputDir().resolve(benchmarkRun.getName()).toAbsolutePath().toString();
		Graph benchmarkGraph = benchmarkRun.getGraph();

		DuckDBJob job;
		switch (algorithm) {
			case BFS:
				job = new BreadthFirstSearchJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case CDLP:
				job = new CommunityDetectionLPJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case LCC:
				job = new LocalClusteringCoefficientJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case PR:
				job = new PageRankJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case SSSP:
				job = new SingleSourceShortestPathsJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			case WCC:
				job = new WeaklyConnectedComponentsJob(runSpecification, platformConfig, inputPath, outputPath, benchmarkGraph);
				break;
			default:
				throw new PlatformExecutionException("Failed to load algorithm implementation.");
		}

		LOG.info("Executing benchmark with algorithm \"{}\" on graph \"{}\".",
				benchmarkRun.getAlgorithm().getName(),
				benchmarkRun.getFormattedGraph().getName());

		try {
			job.execute();
		} catch (Exception e) {
			throw new PlatformExecutionException("Failed to execute a DuckDB job.", e);
		}

		LOG.info("Executed benchmark with algorithm \"{}\" on graph \"{}\".",
				benchmarkRun.getAlgorithm().getName(),
				benchmarkRun.getFormattedGraph().getName());

	}

	@Override
	public BenchmarkMetrics finalize(RunSpecification runSpecification) throws Exception {
		DuckDBCollector.stopPlatformLogging();
		BenchmarkRunSetup benchmarkRunSetup = runSpecification.getBenchmarkRunSetup();
		Path logDir = benchmarkRunSetup.getLogDir().resolve("platform");

		BenchmarkMetrics metrics = new BenchmarkMetrics();
		metrics.setProcessingTime(DuckDBCollector.collectProcessingTime(logDir));
		return metrics;
	}

	@Override
	public void terminate(RunSpecification runSpecification) throws Exception {
		BenchmarkRunner.terminatePlatform(runSpecification);
	}

	@Override
	public String getPlatformName() {
		return PLATFORM_NAME;
	}
}
