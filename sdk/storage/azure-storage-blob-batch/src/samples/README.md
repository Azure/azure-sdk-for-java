---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-blob-storage
  - azure-storage
urlFragment: storage-blob-samples
---

# Azure Storage Blob Batch Samples client library for Java
This document explains samples and how to use them.

## Key concepts

Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
   Following section document various examples.

1. [Crete Blob Client Example][samples_basic]
2. [Bulk Deleting Blobs Example][samples_basic]
3. [Bulk Setting Access Tier Example][samples_basic]
4. [Advanced Batching Delete Example][samples_basic]
5. [Advanced Batching Setting Tier Examples][samples_basic]

## Troubleshooting
When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`

## Next steps
Start using Storage blob batch Java SDK in your solutions. Our SDK details could be found at [SDK README][BATCH_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Storage blob, see the [API reference documentation][storageblob_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-batch/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-batch/README.md#key-concepts
[BATCH_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-batch/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-batch/README.md#contributing
[samples_basic]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch/ReadmeSamples.java
[storageblob_rest]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob-batch%2Fsrc%2Fsamples%2FREADME.png)
