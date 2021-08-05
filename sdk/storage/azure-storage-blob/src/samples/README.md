---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-storage
  - azure-blob-storage
urlFragment: storage-blob-samples
---

# Azure Storage Blob Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure Storage Blob APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## Examples
   Following section document various examples.

1. [Basic Examples][samples_basic]: Create storage, container and blob clients. Upload, download and list blobs.
2. [File Transfer Examples][samples_file_transfer]: Upload and download a large file through blobs.
3. [Storage Error Examples][samples_storage_error]: Handle the exceptions thrown from the Storage Blob service side.
4. [List Container Examples][samples_list_containers]: Create, list and delete containers.
5. [Set Metadata and HTTPHeaders Examples][samples_metadata]: Set metadata for containers and blobs, and set HTTPHeaders for blobs.
6. [Azure Identity Examples][samples_identity]: Use `DefaultAzureCredential` to do the authentication.

## Troubleshooting
When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`

## Next steps
Start using Storage blob Java SDK in your solutions. Our SDK details could be found at [SDK README][BLOB_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Storage blob, see the [API reference documentation][storageblob_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[BLOB_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md
[SDK_README_CONTRIBUTING]:https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md#key-concepts
[samples_basic]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/BasicExample.java
[samples_file_transfer]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/FileTransferExample.java
[samples_storage_error]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/StorageErrorHandlingExample.java
[samples_list_containers]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/ListContainersExample.java
[samples_metadata]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/SetMetadataAndHTTPHeadersExample.java
[samples_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/AzureIdentityExample.java
[storageblob_rest]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob%2Fsrc%2Fsamples%2FREADME.png)
