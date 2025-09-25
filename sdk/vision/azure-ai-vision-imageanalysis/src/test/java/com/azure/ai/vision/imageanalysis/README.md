# Azure Image Analysis client library tests for Java

## Running tests locally, on a Windows PC, against the live service

### Prerequisites

See [Prerequisites](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/README.md#prerequisites). Create an Azure resource in one of the GPU-supported regions, otherwise some of the tests will fail.

### Setup

1. Clone or download the azure-sdk-for-java repository.
1. Open a command prompt window in the root folder of your clone, and run:
   ```bash
   mvn install -f eng\code-quality-reports\pom.xml
   ```

### Set environment variables

See [Set environment variables](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/vision/azure-ai-vision-imageanalysis/README.md#set-environment-variables).

### Configure test proxy

Configure the test proxy to run live service tests without recordings:
```
set AZURE_TEST_MODE=LIVE
```
Other supported values for this enviroment variable are `RECORD` and `PLAYBACK`.

### Run tests

To run all tests, change directory to the folder `sdk\vision\azure-ai-vision-imageanalysis` and run:
```
mvn clean test
```

### Additional information

See [test documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-test/README.md) for additional information, including how to set proxy recordings and run tests using recordings.