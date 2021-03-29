# Azure Text Analytics Performance test client library for Java

Represents Performance tests for Azure Text Analytics SDK for Java.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- [Cognitive Services or Text Analytics account][text_analytics_account] to use this package.

#### Setup for test resources

You will need the following environment variables for running the tests to access the live resources:

```
AZURE_TEXTANALYTICS_ENDPOINT=<text analytics service endpoint>
AZURE_TEXTANALYTICS_API_KEY=<text analytics API Key>
```

### Adding the package to your product
[//]: # ({x-version-update-start;com.azure:azure-ai-textanalytics;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-ai-textanalytics</artifactId>
    <version>5.1.0-beta.6</version>
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

#### Common perf test command line options for Text Analytics
- `--duration` - Number of seconds to run the main test for. Default is 10.
- `--iterations` - Number of iterations of main test loop.
- `--parallel` - Number of operations to execute in parallel,
- `--warmup` - Duration of test warmup time in seconds before the test attributes are calculated.

Use [PerfStressOptions](https://github.com/Azure/azure-sdk-for-java/blob/master/common/perf-test-core/src/main/java/com/azure/perf/test/core/PerfStressOptions.java)
for the other command line options that could be used.

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[azure_subscription]: https://azure.microsoft.com/free
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[text_analytics_account]: https://docs.microsoft.com/en-us/azure/cognitive-services/cognitive-services-apis-create-account?tabs=singleservice%2Cwindows
