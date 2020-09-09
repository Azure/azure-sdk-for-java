# Release History

## 12.7.0-beta.1 (Unreleased)
- Fixed a bug that would cause message text to be erased when only updating the visibility timeout
- Fixed a bug that would cause auth failures when building a client by passing an endpoint which had a sas token with protocol set to https,http

## 12.6.0 (2020-08-13)
- GA release for 2019-12-12 service version.

## 12.6.0-beta.1 (2019-07-07)
- Added support for the 2019-12-12 service version.

## 12.5.2 (2020-06-12)
- Updated azure-storage-common and azure-core dependencies.

## 12.5.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.5.0 (2020-04-06)
- Fixed a bug that would prevent client initialization against Azurite in some containerized environments.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.

## 12.4.0 (2020-03-11)
- Update `azure-storage-common` to version 12.5.0

## 12.3.0 (2020-02-12)
- Added support for the 2019-07-07 service version.

## 12.2.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-common` to version 12.3.1

## 12.2.0 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.2.0/sdk/storage/azure-storage-queue/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.2.0/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue)

## 12.2.0-beta.1 (2019-12-18)
- Added SAS generation methods on clients to improve discoverability and convenience of sas. Deprecated setQueueName, generateSasQueryParameters methods on QueueServiceSasSignatureValues to direct users to using the methods added on clients.

## 12.1.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.0.0/sdk/storage/azure-storage-queue/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.0.0/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue)

- Added a check in ClientBuilders to enforce HTTPS for bearer token authentication.
- Upgraded to version 1.1.0 of Azure Core.

## 12.0.0 (2019-10-31)
- Removed QueueMessage from public API
- Removed BaseQueueClientBuilder
- Removed QueueClientBuilder and QueueServiceClientBuilder inheritance of BaseQueueClientBuilder
- Renamed QueueSegmentOptions getMaxResults and setMaxResults to getMaxResultsPerPage and setMaxResultsPerPage
- Removes StorageError and StorageErrorException from public API
- Renamed StorageErrorCode to QueueErrorCode, SignedIdentifier to QueueSignedIdentifier, StorageServiceProperties to QueueServiceProperties, StorageServiceStats to QueueServiceStatistics, CorRules to QueueCorRules, AccessPolicy to QueueAccessPolicy, Logging to QueueAnalyticsLogging, Metrics to QueueMetrics, and RetentionPolicy to QueueRetentionPolicy
- Renamed StorageException to QueueStorageException
- Added QueueServiceVersion and the ability to set it on client builders
- Renamed enqueueMessage to sendMessage and changed the response type from EnqueueMessage to SendMessageResult
- Renamed dequeueMessages to receiveMessages and changed the response type from DequeuedMessage to QueueMessageItem
- Renamed PeekedMessage to PeekedMessageItem and UpdatedMessage to UpdatedMessageResult
- Added support for emulator endpoints
- Renamed QueueSasPermission getters to use has prefix

## 12.0.0-preview.4 (2019-10-8)
For details on the Azure SDK for Java (October 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.0.0-preview.4/sdk/storage/azure-storage-queue/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.0.0-preview.4/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue)

- Getters and setters were updated to use Java Bean notation.
- Added `getQueueName` for fetching the resource names.
- Updated to be fully compliant with the Java 9 Platform Module System.
- Changed `VoidResponse` to `Response<Void>` on sync API, and `Mono<VoidResponse>` to `Mono<Response<Void>>` on async API.
- Fixed metadata does not allow capital letter issue. [`Bug 5295`](https://github.com/Azure/azure-sdk-for-java/issues/5295)
- `getQueueServiceUrl`, `getQueueUrl` API now returns URL with scheme, host, resource name and snapshot if any.
- Removed SAS token generation APIs from clients, use QueueServiceSasSignatureValues to generate SAS tokens.
- Removed `SASTokenCredential`, `SASTokenCredentialPolicy` and the corresponding `credential(SASTokenCredential)` method in client builder, and added sasToken(String) instead.

## 12.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (September 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/085c8570b411defff26860ef56ea189af07d3d6a/sdk/storage/azure-storage-queue/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/085c8570b411defff26860ef56ea189af07d3d6a/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue)

- Added tracing telemetry on maximum overload API.
- Added generate SAS token APIs.
- Throw `StorageException` with error code when get error response from service.
- Renamed `getHandles` to `listHandles`.
- Added `clearRange` API and removed the parameter of `FileRangeWriteType` from `upload` API.
- Moved `ReactorNettyClient` into a separate module as default plugin. Customer can configure a custom http client through builder.
- Throw `UnexpectedLengthException` when the upload body length does not match the input length. [GitHub #4193](https://github.com/Azure/azure-sdk-for-java/issues/4193)
- Added validation policy to check the equality of request client id between request and response.
- Upgraded to use service version 2019-02-02 from 2018-11-09.
- Replaced `ByteBuf` with `ByteBuffer` and removed dependency on `Netty`.
- Added `azure-storage-common` as a dependency.

**Breaking changes: New API design**
- Changed list responses to `PagedFlux` on async APIs and `PagedIterable` on sync APIs.

## 12.0.0-preview.2 (2019-08-08)
Version 12.0.0-preview.2 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (August 2019 Preview) release, you can refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-08-06/java.html).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-queue_12.0.0-preview.2/sdk/storage/azure-storage-queue/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-queue_12.0.0-preview.2/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue)
demonstrate the new API.

### Features included in `azure-storage-queue`
- This is initial SDK release for storage queue service.
- Packages scoped by functionality
    - `azure-storage-queue` contains a `QueueServiceClient`, `QueueServiceAsyncClient`, `QueueClient` and `QueueAsyncClient` for storage queue operations.
- Client instances are scoped to storage queue service.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
