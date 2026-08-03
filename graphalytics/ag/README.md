# AvantGraph driver for LDBC Graphalytics

Platform driver for the [LDBC Graphalytics benchmark](https://graphalytics.org) using [AvantGraph](https://avantgraph.io).

To execute the Graphalytics benchmark on AvantGraph, follow the steps in the Graphalytics tutorial on [Running Benchmark](https://github.com/ldbc/ldbc_graphalytics/wiki/Manual%3A-Running-Benchmark) with the AvantGraph-specific instructions listed below.

### Dependencies

On Debian/Fedora-based Linux distributions, you may install the prerequisite packages and dependencies listed below using a singe command:

```
scripts/install-dependencies.sh
```

#### Prerequisite packages

Make sure you have the following software packages installed:

* Apache Maven 3+
* Python 3.8+
* DuckDB Python package (`duckdb`)

On Linux, you may use the following script to install these dependencies:

```bash
scripts/install-prerequisites.sh
```

1. To initialize the benchmark package, run:

    ```bash
    scripts/init.sh ${GRAPHS_DIR} ${AVANTGRAPH_DIR} ${ALGORITHMS_DIR}
    ```

    where

    * `GRAPHS_DIR` is the directory of the graphs and the validation data. The argument is optional and its default value is `~/graphs`.
    * `AVANTGRAPH_DIR` is the AvantGraph source directory. 
      It should contain a `build-release` directory with AvantGraph binaries.
    * `ALGORITHMS_DIR` is the directory containing algorithms.

    This script creates a Maven package (`graphalytics-${GRAPHALYTICS_VERSION}-avantgraph-${PROJECT_VERSION}.tar.gz`). Then, it decompresses the package, initializes a configuration directory `config` (based on the content of the `config-template` directory) and sets default values of the directories (see above).

2. Navigate to the directory created by the `init.sh` script:

    ```bash
    cd graphalytics-*-avantgraph-*/
    ```

3. Edit the configuration files (e.g. graphs to be included in the benchmark) in the `config` directory.

    * To conduct benchmark runs, edit the `config/benchmark.properties` file and replace the `include = benchmarks/custom.properties` to select the dataset size you wish to use, e.g. `include = benchmarks/xl.properties`

4. Run the benchmark with the following command:

    ```bash
    bin/sh/run-benchmark.sh
    ```