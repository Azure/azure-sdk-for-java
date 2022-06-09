# Azure Core Performance test client library for Java

Represents Performance tests for Azure Core SDK for Java.

## Getting started

### Prerequisites

- Java Development Kit (JDK) with version 8 or above

### Adding the package to your product


## Key concepts


## Examples

### Running with in memory http client

Just run it.

```shell
mvn install
java -jar .\target\azure-core-perf-1.0.0-beta.1-jar-with-dependencies.jar bytebufferreceive
```

### Running with Wiremock

Just run it with extra parameters.

```shell
mvn install
java -jar .\target\azure-core-perf-1.0.0-beta.1-jar-with-dependencies.jar binarydatareceive --http-client netty --backend-type wiremock
```

### Running with Blobs

1. Create Storage account. Premium Blobs are recommended.
2. Create container.
3. Generate SAS URI for the container with read and write access. It should look like `https://accountname.blob.core.windows.net/containerName?sp=racwdl&st=2022-03-16T21:50:46Z&se=2022-04-01T05:50:46Z&spr=https&sv=2020-08-04&sr=c&sig=REDACTED`
4. Run the command
   ```shell
   java -jar .\target\azure-core-perf-1.0.0-beta.1-jar-with-dependencies.jar bytebuffersend  --http-client netty --endpoint "https://accountname.blob.core.windows.net/containerName?sp=racwdl&st=2022-03-16T21:50:46Z&se=2022-04-01T05:50:46Z&spr=https&sv=2020-08-04&sr=c&sig=REDACTED" --backend-type blobs
   ```

## Troubleshooting

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fperf-test-core%2FREADME.png)
