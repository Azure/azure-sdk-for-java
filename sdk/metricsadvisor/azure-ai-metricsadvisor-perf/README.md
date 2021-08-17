# Azure MetricAdvisor Performance test client library for Java

Represents Performance tests for Azure MetricAdvisor SDK for Java.

## Getting started

### Prerequisites

- Java Development Kit [JDK][jdk_link] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Metrics Advisor account][metrics_advisor_account] to use this package.

#### Setup for test resources

You will need the following environment variables for running the tests to access the live resources:

```
METRICS_ADVISOR_ENDPOINT=<metric advisor service endpoint>
METRICS_ADVISOR_SUBSCRIPTION_KEY=<metric advisor subscription Key>
METRICS_ADVISOR_API_KEY=<metric advisor api Key>
METRICS_ADVISOR_ALERT_CONFIG_ID=<id of a metric advisor alert configuration>
METRICS_ADVISOR_ALERT_ID=<id of a metric advisor alert>
METRICS_ADVISOR_DETECTION_CONFIG_ID=<id of a metric advisor detection configurtion>
METRICS_ADVISOR_INCIDENT_ID=<id of a metric advisor incident>
```

The following environment variable limits the number of items in list APIs; the default value is 100.

```
METRICS_ADVISOR_MAX_LIST_ELEMENTS=<max-list-elements>
```

### Adding the package to your product
[//]: # ({x-version-update-start;com.azure:azure-ai-metricsadvisor;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-metricsadvisor</artifactId>
    <version>1.0.0-beta.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

## Examples
#### Executing the performance test
1. Compile the performance project into a standalone jar using the command from the root of the perf project folder
   ```java
   mvn clean package

2. Execute the corresponding perf test in the project using the command.
   ```java
   java -jar <path-to-packaged-jar-with-dependencies-from-step-1> <options-for-the-test>

#### Common perf test command line options for Metrics Advisor
- `--duration` - Number of seconds to run the main test for. Default is 10.
- `--iterations` - Number of iterations of main test loop.
- `--parallel` - Number of operations to execute in parallel,
- `--warmup` - Duration of test warmup time in seconds before the test attributes are calculated.

#### Example

The tests can be executed as below

```java

java -jar target/azure-ai-metricsadvisor-perf-1.0.0-beta.1-jar-with-dependencies.jar anomalieslist --warmup 1 --iterations 1 --parallel 5 --duration 30

java -jar target/azure-ai-metricsadvisor-perf-1.0.0-beta.1-jar-with-dependencies.jar incidentslist --warmup 1 --iterations 1 --parallel 5 --duration 30

java -jar target/azure-ai-metricsadvisor-perf-1.0.0-beta.1-jar-with-dependencies.jar rootcauselist --warmup 1 --iterations 1 --parallel 5 --duration 30

```

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
[metrics_advisor_account]: https://ms.portal.azure.com/#create/Microsoft.CognitiveServicesMetricsAdvisor
