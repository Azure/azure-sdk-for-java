# Azure Container Registry Performance test client library for Java

Represents Performance tests for Azure Container Registry for Java.

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]

### Adding the package to your product

You will need the following environment variables for running the tests to access the live resources:

```
CONTAINERREGISTRY_ENDPOINT=<azure container registry service endpoint>
CONTAINERREGISTRY_REGISTRY_NAME=<azure container registry name.>
CONTAINERREGISTRY_SUBSCRIPTION_ID=<subscription id in which azure container registry is created.>

// You should also have the default credentials set up done.
AZURE_TENANT_ID=<azure tenant id.>
AZURE_CLIENT_ID=<azure client id.>
```

### Adding the package to your product
## Key concepts

## Examples
#### Executing the performance test
1. Compile the performance project into a standalone jar using the command from the root of the perf project folder
   ```
   mvn clean package -f sdk/containerregistry/azure-containers-containerregistry-perf/pom.xml

2. Execute the corresponding perf test in the project using the command.
   ```
   java -jar <path-to-packaged-jar-with-dependencies-from-step-1> <options-for-the-test>
   java -jar sdk/containerregistry/azure-containers-containerregistry-perf/target/azure-containers-containerregistry-perf-1.0.0-beta.1-jar-with-dependencies.jar getmanifestproperties --warmup 1 --iterations 1 --parallel 50 --duration 15 --count 1000
   java -jar sdk/containerregistry/azure-containers-containerregistry-perf/target/azure-containers-containerregistry-perf-1.0.0-beta.1-jar-with-dependencies.jar listrepositorytests --warmup 1 --iterations 1 --parallel 50 --duration 15 --count 1000

#### Common perf test command line options for Text Analytics
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%template%2Fperf-test-core%2FREADME.png)
