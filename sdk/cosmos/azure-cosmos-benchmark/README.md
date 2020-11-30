# Azure CosmosDB Client Library for Java

# Benchmarking Tool

## Build the benchmarking tool

```bash
git clone https://github.com/Azure/azure-sdk-for-java.git
cd azure-sdk-for-java
cd sdk/cosmos/
mvn clean package -f pom.xml -DskipTests -Dgpg.skip -Ppackage-assembly
```

and then the package will be generated.

## Run the WriteLatency workload

```bash
java -jar azure-cosmos-benchmark/target/azure-cosmos-benchmark-4.0.1-beta.1-jar-with-dependencies.jar \
 -serviceEndpoint $endpoint -masterKey $masterkey \
 -databaseId $dbname -collectionId $colname \
 -consistencyLevel Eventual -concurrency 10 -numberOfOperations 1000000 \
 -operation WriteLatency -connectionMode Direct
```

## Sample Report:

```
2/13/19 9:32:39 PM =============================================================

-- Meters ----------------------------------------------------------------------
#Successful Operations
             count = 89934
         mean rate = 1798.56 events/second
     1-minute rate = 1718.45 events/second
     5-minute rate = 1630.17 events/second
    15-minute rate = 1610.01 events/second
#Unsuccessful Operations
             count = 0
         mean rate = 0.00 events/second
     1-minute rate = 0.00 events/second
     5-minute rate = 0.00 events/second
    15-minute rate = 0.00 events/second

-- Timers ----------------------------------------------------------------------
Latency
             count = 89938
         mean rate = 1798.64 calls/second
     1-minute rate = 1718.65 calls/second
     5-minute rate = 1630.37 calls/second
    15-minute rate = 1610.21 calls/second
               min = 3.97 milliseconds
               max = 22.81 milliseconds
              mean = 5.37 milliseconds
            stddev = 0.96 milliseconds
            median = 5.26 milliseconds
              75% <= 5.70 milliseconds
              95% <= 6.40 milliseconds
              98% <= 6.93 milliseconds
              99% <= 7.51 milliseconds
            99.9% <= 17.37 milliseconds
```

## Other Currently Supported Workloads

* ReadLatency,
* WriteLatency,
* ReadThroughput,
* WriteThroughput,
* QueryInClauseParallel
* ReadMyWrites


You can provide ``--help`` to the tool to see the list of other work loads (read, etc) and other options.



![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcosmos%2Fazure-cosmos-benchmark%2FREADME.png)
