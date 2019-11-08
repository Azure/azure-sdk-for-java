
## Azure Azure Storage Queue Samples client library for Java
This document explains samples and how to use them.

## Key concepts
More detail is defined at [queue key concept][queue_key_concept].

# Samples Azure Storage Blob Batch APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## How to run
These sample can be run in your IDE with default JDK.

## Examples

Get started with our samples:
- [QueueServiceSample][samples_queue_service]: Create, list and delete queues
- [MessageSample][samples_message]: Enqueue, peek dequeue, update, clear and delete messages. Get properties of the queue.
- [QueueExceptionSample][samples_queue_exception]: Handle the exceptions from storage queue service side.
- [AsyncSample][samples_async]: Create queue and enqueue message using async queue client call.

## Troubleshooting
### General
When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`

## Next steps
Start using Storage Queue Java SDK in your solutions. Our SDK details could be found at [SDK README][SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Storage blob, see the [API reference documentation][storage_queue_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[SDK_README]: ../../README.md
[SDK_README_GETTING_STARTDED]: ../../README.md#getting-started
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[samples_queue_service]: java/com/azure/storage/queue/QueueServiceSamples.java
[samples_message]: java/com/azure/storage/queue/MessageSamples.java
[samples_queue_exception]: java/com/azure/storage/queue/QueueExceptionSamples.java
[samples_async]: java/com/azure/storage/queue/AsyncSamples.java
[storage_queue_rest]: https://docs.microsoft.com/en-us/rest/api/storageservices/queue-service-rest-api
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
[queue_key_concept]: ../../README.md#key-concepts
[samples]: java/samples/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-queue%2Fsrc%2Fsamples%2FREADME.png)
