# umbra-loop-artifact
Artifact for the paper 'Running Loops around Recursive SQL'.

Instructions below detail how to reproduce the experimental results from the paper.

> Umbra binaries for the specific version used in this paper are required to reproduce the results.
> Contact the authors for a copy, mentioning the version string `v0.2-2946-g99705df8d`.
> Instructions below assume you have Umbra binaries `sql` and `server` available under path `$UMBRA/`

## Retrieving Datasets
Run the relevant scripts:

```bash
datalog/download_datasets.sh
graphalytics/download_datasets.sh
```

Datalog datasets must be converted before they can be used:

```bash
datalog/convert_datasets.py --datadir datalog/data --outdir datalog/csv
```

## Datalog Experiments
Next to Umbra, Souffle and FlowLog need to be setup.

### Souffle
Build souffle from source:

```bash
git clone git@github.com:souffle-lang/souffle.git
cd souffle
git checkout a1303be3c0166400dee3d1f36f0d96abe03e6901
sudo apt install \
  bison \
  build-essential \
  clang \
  cmake \
  doxygen \
  flex \
  g++ \
  git \
  libffi-dev \
  libncurses5-dev \
  libsqlite3-dev \
  make \
  mcpp \
  python3 \
  sqlite3 \
  zlib1g-dev
cmake -S . -B build -DCMAKE_BUILD_TYPE=Release -DSOUFFLE_DOMAIN_64BIT=ON
cmake --build build -j8
```

Prepare the compiled binaries per algorithm:

```bash
datalog/perf/prepare_souffle.py --algorithms cspa,galen,cvc5,batik --compiler ~/workspace/souffle/build/src/souffle 
```

### FlowLog
Download the FlowLog compiler binaries:
```bash
wget https://github.com/flowlog-rs/flowlog/releases/download/flowlog-compiler-v0.4.4/flowlog-compiler-0.4.4-x86_64-unknown-linux-gnu.tar.gz
tar xvf flowlog-compiler-0.4.4-x86_64-unknown-linux-gnu.tar.gz
```

Also install the Rust compiler:

```bash
# Choose 'Proceed with standard installation'
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh

# To load into the current shell
source ~/.cargo/env
```

Prepare the compiled binaries per algorithm:

```bash
datalog/perf/prepare_flowlog.py --algorithms cspa,galen,cvc5,batik --compiler ~/Downloads/flowlog-compiler-0.4.4-x86_64-unknown-linux-gnu/flowlog-compiler 
```

### Running the Experiments
Run the benchmark script for the workloads included in the paper:

```bash
datalog/perf/benchmark.py --graph httpd-cspa --algorithms cspa --systems flowlog,souffle,umbra --output ./datalog/perf/results.csv --umbra-sql $UMBRA/sql
datalog/perf/benchmark.py --graph galen-galen --algorithms galen --systems flowlog,souffle,umbra --output ./datalog/perf/results.csv --umbra-sql $UMBRA/sql
datalog/perf/benchmark.py --graph cvc5-cvc5 --algorithms cvc5 --systems flowlog,souffle,umbra --output ./datalog/perf/results.csv --umbra-sql $UMBRA/sql
datalog/perf/benchmark.py --graph batik-batik --algorithms batik --systems flowlog,souffle,umbra --output ./datalog/perf/results.csv --umbra-sql $UMBRA/sql
```

Combined results can be found at `./datalog/perf/results.csv`.

## Graphalytics Experiments 
Install the following dependencies first, then execute the per-platform instructions below.

```bash
sudo apt install maven apptainer postgresql-client
```

### AvantGraph
```bash
snellius/build_avantgraph.sh

cd graphalytics/

./ag/scripts/init.sh $PWD/data
cp configs/ag.properties ag/graphalytics-1.10.0-avantgraph-0.1.0-SNAPSHOT/config/benchmarks/custom.properties
apptainer run ../snellius/avantgraph.sif
cd ag/graphalytics-1.10.0-avantgraph-0.1.0-SNAPSHOT/ 
./bin/sh/run-benchmark.sh
```

### DuckDB
```bash
cd graphalytics/
# Optional: do this in a virtual env to avoid cluttering your regular Python environment 
pip install duckdb==1.5.0
./duckdb/scripts/init.sh $PWD/data
cp configs/duckdb.properties duckdb/graphalytics-1.10.0-duckdb-0.0.1-SNAPSHOT/config/benchmarks/custom.properties
cd duckdb/graphalytics-1.10.0-duckdb-0.0.1-SNAPSHOT/
./bin/sh/run-benchmark.sh
```

### Umbra
```bash
cd graphalytics/
./umbra/scripts/init.sh $PWD/data

# In a separate terminal, start the umbra server and keep it running:
# Use the -createdb flag with a target file path if you need to control where exactly the database file is created.
$UMBRA/server ""

# Configure the postgres user to allow the benchmark suite to connect
psql -h /tmp -U postgres
postgres=# ALTER ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';

# Start with the external driver configuration
cp configs/umbra-ext.properties umbra/graphalytics-1.10.0-umbra-0.0.1-SNAPSHOT/config/benchmarks/custom.properties
cd umbra/graphalytics-1.10.0-umbra-0.0.1-SNAPSHOT/
./bin/sh/run-benchmark.sh

# WITH LOOP configuration
cp ../../configs/umbra-loop.properties config/benchmarks/custom.properties
./bin/sh/run-benchmark.sh

# USING KEY
cp ../../configs/umbra-uk.properties config/benchmarks/custom.properties
./bin/sh/run-benchmark.sh
```

### Plotting Results
The graphalytics benchmark platform will write reports that include a JSON output file to paths such as `graphalytics/ag/graphalytics-1.10.0-avantgraph-0.1.0-SNAPSHOT/report/260801-160511-AVANTGRAPH-report-CUSTOM/json/results.json`.
Copy the relevant JSON files from for each platform into the directory `graphalytics/reports/`, then run `./graphalytics/table.py` to produce a LaTeX table.

## Other Experiments
We include the code for two smaller experiments that back up two claims made in the paper.
Additional code for this is kept under `misc/`.

Install additional script dependencies first:

```bash
pip install jproperties psycopg[binary]
```

### WITH RECURSIVE Memory Consumption
Claim:
> running a WITH RECURSIVE implementation of Bellman–Ford on the smallest 
> graph from the LDBC Graphalytics benchmark [12] (kgs) requires more than 1TiB 
> of memory, while for WITH LOOP 1GiB is more than sufficient.

```bash
misc/load.py --umbra-sql $UMBRA/sql graphalytics/data/ kgs /tmp/kgs.umbra
$UMBRA/sql /tmp/kgs.umbra misc/sssp-with-recursive.sql
DATABASE_QUERYMEMORY=1G $UMBRA/sql /tmp/kgs.umbra misc/sssp-loop.sql
```

### Performance of GraphAlg Compiled to WITH LOOP
Claim:
> With the addition of WITH LOOP, we can represent GraphAlg programs entirely 
> in SQL within Umbra, resulting in up to 2x performance improvement compared 
> to using external drivers.

The SQL file `pr-graphalg.sql` was generated using the GraphAlg compiler.
To reproduce also this SQL file, go to `https://wildarch.dev/graphalg/playground/pr.html`, click the `Run` button, then under `Export (experimental)` click `Umbra`. 
Finally, modify the top-level WITH clause to use the `v` and `e` tables from the loaded graph.

```bash
misc/load.py --umbra-sql $UMBRA/sql graphalytics/data/ wiki-Talk /tmp/wiki-Talk.umbra
$UMBRA/sql /tmp/wiki-Talk.umbra misc/pr-graphalg.sql
```

Compare the `exec` time to the scores obtained from running the external driver implementation as part of the graphalytics benchmark (for the `wiki-Talk` dataset).