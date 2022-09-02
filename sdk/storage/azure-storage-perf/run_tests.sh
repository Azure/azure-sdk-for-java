#!/bin/bash

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar downloadblob --size 10240 --parallel 64 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        downloadblob --size 10240 --parallel 64 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar downloadblob --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        downloadblob --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar downloadblob --size 1073741824 --parallel 1 --warmup 60 --duration 60 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        downloadblob --size 1073741824 --parallel 1 --warmup 60 --duration 60 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar downloadblob --size 1073741824 --parallel 8 --warmup 60 --duration 60 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        downloadblob --size 1073741824 --parallel 8 --warmup 60 --duration 60 -i 10


java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar downloadblobtofile --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        downloadblobtofile --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar uploadblob --size 10240 --parallel 64 --warmup 15 --duration 30  -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        uploadblob --size 10240 --parallel 64 --warmup 15 --duration 30  -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar uploadblob --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        uploadblob --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar uploadblob --size 1073741824 --parallel 1 --warmup 60 --duration 60 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        uploadblob --size 1073741824 --parallel 1 --warmup 60 --duration 60 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar uploadblob --size 1073741824 --parallel 8 --warmup 60 --duration 60 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        uploadblob --size 1073741824 --parallel 8 --warmup 60 --duration 60 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar uploadfromfile --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        uploadfromfile --size 10485760 --parallel 32 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar listblobs --count 5 --parallel 64 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        listblobs --count 5 --parallel 64 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar listblobs --count 500 --parallel 32 --warmup 15 --duration 30 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        listblobs --count 500 --parallel 32 --warmup 15 --duration 30 -i 10

java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies_nootel.jar listblobs --count 50000 --parallel 32 --warmup 60 --duration 60 -i 10
java -jar ./target/azure-storage-perf-1.0.0-beta.1-jar-with-dependencies.jar        listblobs --count 50000 --parallel 32 --warmup 60 --duration 60 -i 10













