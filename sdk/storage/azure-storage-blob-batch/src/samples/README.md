
## Azure Azure Storage Blob Batch Samples client library for Java
This document explains samples and how to use them.

## Key concepts

Blob storage is designed for:

- Serving images or documents directly to a browser.
- Storing files for distributed access.
- Streaming video and audio.
- Writing to log files.
- Storing data for backup and restore, disaster recovery, and archiving.
- Storing data for analysis by an on-premises or Azure-hosted service.

# Samples Azure Storage Blob Batch APIs
This document describes how to use samples and what is done in each sample.

## Getting started

### Adding the package to your project

Maven dependency for Azure Storage blob Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-batch;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob-batch</artifactId>
  <version>12.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## How to run
These sample can be run in your IDE with default JDK.

## Examples
   Following section document various examples.

1. [Crete Blob Client Example][samples_basic]
2. [Bulk Deleting Blobs Example][samples_basic]
3. [Bulk Setting Access Tier Example][samples_basic]
4. [Advanced Batching Delete Example][samples_basic]
5. [Advanced Batching Setting Tier Examples][samples_basic]

## Troubleshooting
### General
When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`

## Next steps
Start using Storage blob batch Java SDK in your solutions. Our SDK details could be found at [SDK README] [BATCH_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Storage blob, see the [API reference documentation][storageblob_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[BATCH_SDK_README]: ../../README.md
[samples_basic]: java/com/azure/storage/blob/batch/ReadmeCodeSamples.java
[storageblob_rest]: https://docs.microsoft.com/en-us/rest/api/storageservices/blob-service-rest-api
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/storage/azure-storage-blob-batch/README.png)
