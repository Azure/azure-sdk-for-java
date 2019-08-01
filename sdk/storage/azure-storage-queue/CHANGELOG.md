# Release History

## 12.0.0-preview.2 (2019-08-08)
Version 12.0.0-preview.2 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).
TODO: Will update the release accoucement link if any. It is placeholder for now.
For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-queue/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue)
demonstrate the new API.

### Major changes from `azure-storage-queue`
- Packages scoped by functionality
    - `azure-storage-queue` contains a `QueueServiceClient`, `QueueServiceAsyncClient`, `QueueClient` and `QueueAsyncClient` for storage queue operations.
- Client instances are scoped to storage queue service.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
  
