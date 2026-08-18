# Azure Cosmos DB Client Library for Java

# Benchmarking Tool

## Build the benchmarking tool

```bash
git clone https://github.com/Azure/azure-sdk-for-java.git
cd azure-sdk-for-java
cd sdk/cosmos/
mvn clean package -f pom.xml -DskipTests -Dgpg.skip -Ppackage-assembly
```

and then the package will be generated.

## Run a workload

```bash
java -jar azure-cosmos-benchmark/target/azure-cosmos-benchmark-4.0.1-beta.1-jar-with-dependencies.jar \
 -workloadConfig azure-cosmos-benchmark/workload-config-sample.json
```

All benchmark settings are supplied through the JSON workload configuration. See
`workload-config-sample.json` for the complete structure.

## Container fault injection

Standard asynchronous workloads can configure one server-error fault rule through either
`orchestrator.tenantDefaults` or an individual tenant. A tenant-level value overrides the default.

```json
"faultInjection": {
    "serverErrorType": "SERVICE_UNAVAILABLE",
    "region": "West US",
    "startDelay": "PT30S",
    "duration": "PT30S",
    "injectionRate": 1.0
}
```

The rule targets the tenant's entire workload container and has no feed-range or operation filter.
When `region` is present, only requests targeting that region are eligible; omit it to allow the rule
in any region. `startDelay` and `duration` are ISO-8601 durations measured from rule arming
immediately before workload dispatch. `injectionRate` must be greater than zero and at most one; it
is the percentage of eligible fault-rule evaluations, not a guarantee that the same percentage of
top-level benchmark operations will fail after SDK retries.

Fault injection is not supported for synchronous, encryption, or CTL workloads. Configurations that
combine those workload families with `faultInjection` fail during configuration validation.

To capture PPCB failback progress in Cosmos DB, configure a dedicated metrics destination such as
`gatewayv2-scale-test` database `benchmarkdb` and container `cosmosbenchmarkmetricsfeed`. The metrics
container must already exist with partition key `/partition_key`; do not reuse a container with a
different partition-key path. Supply credentials at runtime; do not commit account keys. The reporter preserves zero values only for
`cosmos.client.ppcb.failback.pendingRecoveryCount`, allowing a positive value followed by zero to
show completed failback.

Keep the workload running beyond `startDelay + duration` long enough for a PPCB recovery scan and at
least one additional metrics reporting interval. Otherwise the run can end before the recovery zero
is emitted.

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

* ReadThroughput,
* WriteThroughput,
* QueryInClauseParallel
* ReadMyWrites


You can provide ``--help`` to the tool to see the list of other work loads (read, etc) and other options.




