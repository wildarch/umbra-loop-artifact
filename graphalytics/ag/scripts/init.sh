#!/bin/bash

set -eo pipefail

GRAPHS_DIR=${1:-~/graphs}
AVANTGRAPH_DIR=${2:-~/workspace/avantgraph}

rootdir="$( cd "$( dirname "${BASH_SOURCE[0]:-${(%):-%x}}" )" >/dev/null 2>&1 && pwd )/.."
cd ${rootdir}
. scripts/project-vars.sh

ALGORITHMS_DIR=${3:-${rootdir}/algorithms}

rm -rf ${PROJECT}
mvn package -Dmaven.buildNumber.doCheck=false
tar xf ${PROJECT}-bin.tar.gz
cd ${PROJECT}

cp -r config-template config
# set directories
sed -i.bkp "s|^graphs.root-directory =$|graphs.root-directory = ${GRAPHS_DIR}|g" config/benchmark.properties
sed -i.bkp "s|^graphs.validation-directory =$|graphs.validation-directory = ${GRAPHS_DIR}|g" config/benchmark.properties

# set avantgraph directory
sed -i.bkp "s|^platform.avantgraph.source-directory =$|platform.avantgraph.source-directory = ${AVANTGRAPH_DIR}|g" config/platform.properties
sed -i.bkp "s|^platform.avantgraph.algorithms-directory =$|platform.avantgraph.algorithms-directory = ${ALGORITHMS_DIR}|g" config/platform.properties
