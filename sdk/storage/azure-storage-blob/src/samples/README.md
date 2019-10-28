
## Azure Azure Storage Blob Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Blob Storage is designed for:

- Serving images or documents directly to a browser
- Storing files for distributed access
- Streaming video and audio
- Writing to log files
- Storing data for backup and restore, disaster recovery, and archiving
- Storing data for analysis by an on-premises or Azure-hosted service
# Samples Azure Storage Blob APIs
This document describes how to use samples and what is done in each sample.

## Getting started

### Adding the package to your project

Maven dependency for Azure Storage blob Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-storage-blob;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.0.0-preview.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## How to run
These sample can be run in your IDE with default JDK.

## Examples
   Following section document various examples.

1. [Basic Examples][samples_basic]: Create storage, container and blob clients. Upload, download and list blobs.
2. [File Transfer Examples][samples_file_transfer]: Upload and download a large file through blobs.
3. [Storage Error Examples][samples_storage_error]: Handle the exceptions thrown from the Storage Blob service side.
4. [List Container Examples][samples_list_containers]: Create, list and delete containers.
5. [Set Metadata and HTTPHeaders Examples][samples_metadata]: Set metadata for containers and blobs, and set HTTPHeaders for blobs.
6. [Azure Identity Examples][samples_identity]: Use `DefaultAzureCredential` to do the authentication.

## Troubleshooting
### General
When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`

## Next steps
Start using Storage blob Java SDK in your solutions. Our SDK details could be found at [SDK README] [BLOB_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Storage blob, see the [API reference documentation][storageblob_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more conributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[source_code]:  src
[BLOB_SDK_README]: ../../README.md
[SDK_README_CONTRIBUTING]:../../README.md#contributing
[samples_basic]: java/com/azure/storage/blob/BasicExample.java
[samples_file_transfer]: java/com/azure/storage/blob/FileTransferExample.java
[samples_storage_error]: java/com/azure/storage/blob/StorageErrorHandlingExample.java
[samples_list_containers]: java/com/azure/storage/blob/ListContainersExample.java
[samples_metadata]: java/com/azure/storage/blob/SetMetadataAndHTTPHeadersExample.java
[samples_identity]: java/com/azure/storage/blob/AzureIdentityExample.java
[storageblob_rest]: https://docs.microsoft.com/en-us/rest/api/storageservices/blob-service-rest-api
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/storage/azure-storage-blob/README.png)
