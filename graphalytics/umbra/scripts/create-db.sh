#!/bin/bash
mkdir -p /tmp/db
../umbra/build-release/sql -createdb graphalytics-1.10.0-umbra-0.0.1-SNAPSHOT/scratch/umbra.db <<<"ALTER ROLE postgres WITH LOGIN SUPERUSER PASSWORD 'postgres';" || exit 1
# ../umbra/build-release/server graphalytics-1.10.0-umbra-0.0.1-SNAPSHOT/scratch/umbra.db --address=localhost