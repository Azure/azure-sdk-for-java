# Azure Monitor Query Performance test client library for Java

Represents Performance tests for Azure Monitor Query  SDK for Java.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]

#### Setup for test resources

You will need the following environment variables for running the tests to access the live resources:

```
AZURE_TENANT_ID=<Tenant ID of Azure subscription>
AZURE_CLIENT_ID=<Client ID of Azure AAD application>
AZURE_CLIENT_SECRET=<Client secret of Azure AAD application>
AZURE_MONITOR_LOGS_WORKSPACE_ID=<Workspace ID of Log Analytics workspace>
AZURE_MONITOR_METRICS_RESOURCE_ID=<Resource ID of an Azure Resource>
```

## Key concepts

## Examples
#### Executing the performance test
1. Compile the performance project into a standalone jar using the command from the root of the perf project folder
```
   mvn clean package -f sdk/monitor/azure-monitor-query-perf/pom.xml
```

2. Execute the corresponding perf test in the project using the command.
```
   java -jar <path-to-packaged-jar-with-dependencies-from-step-1> <options-for-the-test>
   java -jar sdk/monitor/azure-monitor-query-perf/target/azure-monitor-query-perf-1.0.0-beta.1-jar-with-dependencies.jar logsquery --warmup 1 --iterations 1 --parallel 50 --duration 15 --count 1000
   java -jar sdk/monitor/azure-monitor-query-perf/target/azure-monitor-query-perf-1.0.0-beta.1-jar-with-dependencies.jar logsbatchquery --warmup 1 --iterations 1 --parallel 50 --duration 15 --count 1000
   java -jar sdk/monitor/azure-monitor-query-perf/target/azure-monitor-query-perf-1.0.0-beta.1-jar-with-dependencies.jar logsqueryasmodel --warmup 1 --iterations 1 --parallel 50 --duration 15 --count 1000
   java -jar sdk/monitor/azure-monitor-query-perf/target/azure-monitor-query-perf-1.0.0-beta.1-jar-with-dependencies.jar metricsquery --warmup 1 --iterations 1 --parallel 50 --duration 15 --count 1000
```   

#### Common perf test command line options for Text Analytics
- `--duration` - Number of seconds to run the main test for. Default is 10.
- `--iterations` - Number of iterations of main test loop.
- `--parallel` - Number of operations to execute in parallel,
- `--warmup` - Duration of test warmup time in seconds before the test attributes are calculated.

Use [PerfStressOptions](https://github.com/Azure/azure-sdk-for-java/blob/main/common/perf-test-core/src/main/java/com/azure/perf/test/core/PerfStressOptions.java)
for the other command line options that could be used.

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
