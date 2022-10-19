# Azure Communication Common Performance test client library for Java

Represents Performance tests for Azure Communication Common for Java.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Adding the package to your product

### Adding the package to your product
## Key concepts

## Examples
### Executing the performance test
1. Compile the performance project into a standalone jar using the command from the root of the perf project folder
```
mvn clean package -f sdk/communication/azure-communication-common-perf/pom.xml
```

2. Execute the corresponding perf test in the project using the command.
```
java -jar <path-to-packaged-jar-with-dependencies-from-step-1> <options-for-the-test>
java -jar sdk/communication/azure-communication-common-perf/target/azure-communication-common-perf-1.0.0-beta.1-jar-with-dependencies.jar hmacauthenticationpolicy --warmup 1 --iterations 1 --parallel 50 --duration 15
```

### Common perf test command line options for Text Analytics
- `--duration` - Number of seconds to run the main test for. Default is 10.
- `--iterations` - Number of iterations of main test loop.
- `--parallel` - Number of operations to execute in parallel,
- `--warmup` - Duration of test warmup time in seconds before the test attributes are calculated.

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/master/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcommunication%2Fazure-communication-common-perf%2FREADME.png)
