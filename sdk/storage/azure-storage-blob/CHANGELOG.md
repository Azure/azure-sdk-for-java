# Change Log azure-storage-blob

## Version XX.X.X (XXXX-XX-XX)
- Added SAS generation methods on clients to improve discoverability and convenience of sas. Deprecated setContainerName, setBlobName, setSnapshotId, generateSasQueryParameters methods on BlobServiceSasSignatureValues to direct users to using the methods added on clients.
- Fixed a bug where Account SAS would not work when set on clients.

## Version 12.1.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.1.0/sdk/storage/azure-storage-blob/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.1.0/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob)

- Optimized downloadToFile to avoid an unnecessary getProperties call and to lock on an etag once the operation has started.
- Fixed a race condition that would sometimes result in a RuntimeException with a message related to unexpected header value of client-request-id.
- Fixed a bug in the RetryPolicy that would apply the delay of a fixed retry policy to the first try.
- Fixed a bug that could cause the overwrite flag to not be honored in cases where data was uploaded by another source after a parallel operation has already started.
- Added overloads to accept an overwrite flag to commitBlockList and getBlobOutputStream. Note that this changes the default behavior of the min overload and these methods will now fail if they are attempting to overwrite data.
- Added a check in ClientBuilders to enforce HTTPS for bearer token authentication.
- Upgraded to version 1.1.0 of Azure Core.

## Version 12.0.0 (2019-10-31)

- Removed BaseBlobClientBuilder
- Removed BlobClientBuilder, BlobContainerClientBuilder, BlobServiceClientBuilder, and SpecializedBlobClientBuilder inheritance of BaseBlobClientBuilder
- Renamed ListBlobContainerOptions getMaxResults and setMaxResults to getMaxResultsPerPage and setMaxResultsPerPage
- Renamed ListBlobsOptions getMaxResults and setMaxResults to getMaxResultsPerPage and setMaxResultsPerPage
- Renamed BlobProperties to BlobItemProperties and BlobContainerProperties to BlobContainerItemProperties
- Removes StorageError and StorageErrorException from public API
- Renamed StorageErrorCode to BlobErrorCode, SignedIdentifier to BlobSignedIdentifier, StorageServiceProperties to BlobServiceProperties, StorageServiceStats to BlobServiceStatistics, CorRules to BlobCorRules, AccessPolicy to BlobAccessPolicy, Logging to BlobAnalyticsLogging, Metrics to BlobMetrics, and RetentionPolicy to BlobRetentionPolicy
- Renamed BlobHTTPHeaders to BlobHttpHeaders and removed Blob from getter names
- Renamed StorageException to BlobStorageException
- Added BlobServiceVersion and the ability to set it on client builders
- Replaced URL parameters with String on appendBlockFromUrl, beginCopy, copyFromUrl, stageBlockFromUrl, uploadPagesFromUrl, and copyIncremental
- Added support for emulator endpoints
- Added support for additional connection string configurations and support for use development connection
- Changed constructors for AppendBlobItem, BlockBlobItem, PageBlobItem, 
- Renamed listBlobsFlat to listBlobs and listBlobHierarchy to listBlobsByHierarchy
- Replaced startCopyFromUrl with beginCopy and return poller
- Renamed BlobContainerSasPermission and BlobSasPermission getters to use has prefix
- Replaced BlobAccessConditions, AppendBlobAccessConditions, and PageBlobAccessConditions with BlobRequestConditions, AppendBlobRequestConditions, and PageBlobRequestConditions. 
- Removed ModifiedAccessConditions and SourceModifiedAccessConditions in favor of RequestConditions, removed BlobContainerAccessConditions in favor of BlobRequestConditions.
- Removed AppendPositionAccessConditions, LeaseAccessConditions, and SequenceNumberAccessConditions
- Renamed LeaseClient, LeaseAsyncClient, and LeaseClientBuilder to BlobLeaseClient, BlobLeaseAsyncClient, and BlobLeaseClientBuilder
- Added upload overloads which allow passing a flag to indicate if an existing blob should be overwritten
- Added support for blob names with special characters
- Changed return type for BlobClient.downloadWithProperties from Response<Void> to BlobDownloadResponse and BlobAsyncClient.downloadWithProperties from Mono<Response<Flux<ByteBuffer>>> to Mono<BlobDownloadAsyncResponse>

## Version 12.0.0-preview.4 (2019-10-8)
For details on the Azure SDK for Java (October 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.0.0-preview.4/sdk/storage/azure-storage-blob/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.0.0-preview.4/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob)

- Moved the specialized `BlobClient`, `AppendBlobClient`, `BlockBlobClient`, and `PageBlobClient`, into the `specialized` package within Azure Storage Blobs. Additionally, moved any model classes that are tied to a specific specialized client.
- Added a `BlobClientBase` which is now the super class for `BlobClient`, `AppendBlobClient`, `BlockBlobClient`, and `PageBlobClient`.
- Getters and setters were updated to use Java Bean notation.
- Added `getBlobContainerName` on `BlobContainerClient` and `BlobContainerAsyncClient` and `getContainerName`, `getBlobName` on `BlobClientBase` and `BlobAsyncClientBase` for fetching the resource names.
- Updated to be fully compliant with the Java 9 Platform Module System.
- Changed `VoidResponse` to `Response<Void>` on sync API, and `Mono<VoidResponse>` to `Mono<Response<Void>>` on async API.
- Fixed metadata does not allow capital letter issue. [`Bug 5295`](https://github.com/Azure/azure-sdk-for-java/issues/5295)
- Updated the return type of `downloadToFile` API to `BlobProperties` on sync API and `Mono<BlobProperties>` on async API.
- `getAccountUrl`, `getBlobContainerUrl`, `getBlobUrl` API now returns URL with scheme, host, resource name and snapshot if any.
- Added `LeaseClient` and `LeaseAsyncClient` to the specialized package and removed the leasing methods from `BlobClient`, `BlobAsyncClient`, `ContainerClient`, and `ContainerAsyncClient`.
- Added `blocksize` parameter to sync `blockBlobClient`.
- Use Primitives for `exist` API return type.
- Removed a `create` and `appendBlockFromUrl` overload API in `AppendBlob`. 
- Fixed `create` method name in PageBlob.
- Renamed `setTier` to `setAccessTier` from `BlobAsyncClientBase` and `BlobClientBase` classes.
- Added `ParallelTransferOptions` to buffered upload, upload from file and download to file methods.
- Removed `Metadata` class and uses Map<String, String> for `matadata` field of `BlobProperties` and `ContainerProperties`.
- Removed SAS token generation APIs from clients, use BlobServiceSasSignatureValues to generate SAS tokens. 
- Removed `SASTokenCredential`, `SASTokenCredentialPolicy` and the corresponding `credential(SASTokenCredential)` method in client builder, and added sasToken(String) instead.

## Version 12.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (September 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

- Added tracing telemetry on maximum overload API.
- Throw `UnexpectedLengthException` when the upload body doesn't match the expected input length.
- Added validation policy to check the equality of request client ID between request and response.
- Updated to use service version 2019-02-02.
- Added dependency to azure-storage-common.
- Replaced `ByteBuf` with `ByteBuffer` and removed dependency on `Netty`.
- Added convenience upload method to `BlockBlobClient` and `BlockBlobAsyncClient`.
- Added rehydrate priority support.
- Added capability to set tier on additional APIs.
- Added customer provided key support.

**Breaking changes: New API design**
- Changed list responses to `PagedFlux` on async APIs and `PagedIterable` on sync APIs.
- Simplified API to return model types directly on non-maximal overloads. Maximal overloads return `Response<T>` and suffixed with WithResponse.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/085c8570b411defff26860ef56ea189af07d3d6a/sdk/storage/azure-storage-blob/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/085c8570b411defff26860ef56ea189af07d3d6a/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob)
demonstrate the new API.

## Version 12.0.0-preview.2 (2019-08-08)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

- Renamed `StorageClient`, `StorageAsyncClient`, and `StorageClientBuilder` to `BlobServiceClient`, `BlobServiceAsyncClient`, and `BlobServiceClientBuilder`.
- Combined `AppendBlobClientBuilder`, `BlockBlobClientBuilder`, and `PageBlobClientBuilder` into `BlobClientBuilder`. Methods to create each client type were added.
- Removed static builder method from clients. Builders are now instantiable.
- Changed return type of `createSnapshot` in `BlobClient` to return a client associated to the blob snapshot instead of the snapshot ID. Use `getSnapshotId` to get snapshot ID and `isSnapshot` to indicate if the client is associated to a blob snapshot.
- Added `getSnapshotClient` to clients that returns a new client associated to the snapshot.
- Added SAS token generation to clients.
- Added `deleteContainer` to `BlobServiceClient` and `BlobServiceAsyncClient`.
- Added `getAppendBlobClient` with snapshot overload to `ContainerClient`.
- Removed `AnonymousClientCredential` class.
- Changed parameter ordering of `BlobClient` and `BlobAsyncClient` `download` and `downloadToFile`.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.0.0-preview.2/sdk/storage/azure-storage-blob/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.0.0-preview.2/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob)
demonstrate the new API.

## Version 12.0.0-preview.1 (2019-06-28)
Version 12.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

**Breaking changes: New API design**
- Operations are now scoped to a particular client:
    - `BlobServiceClient`: StorageURL's functionality was migrated to BlobServiceClient. This client handles account-level operations. This includes managing service properties and listing the containers within an account.
    - `ContainerClient`: ContainerURL's functionality was migrated to ContainerClient. The client handles operations for a particular container. This includes creating or deleting that container, as well as listing the blobs within that container.
    - `BlobClient`: BlobURL's functionality was migrated to BlobClient, TransferManager download functionality was migrated to BlobClient and TransferManager upload functionality was migrated to BlockBlobClient. The client handles most operations, excluding upload, for an individual blob, including downloading data and working with blob properties.
    There are subclients (BlockBlobClient, PageBlobClient, AppendBlobClient) available for their respective blob types on the service.

    These clients can be accessed by navigating down the client hierarchy, or instantiated directly using builder to the resource (account, container or blob).
- New module level operations for simple upload and download using a block or page blob client.
- Download operations can download data in multiple ways:
    - `download_to_stream`: Download the entire content to an open stream handle (e.g. an open file). Supports multi-threaded download.
- New underlying REST pipeline implementation, based on the new `azure-core` library.
- Client and pipeline configuration is now available via keyword arguments at both the client level.
- Authentication using `azure-identity` credentials.

## 2019.04.30 Version 11.1.1
- Upgraded to version 2.1.1 of the autorest-clientime which upgrades to a more secure version of jackson and fixes a NPE on unkown host errors.

## 2019.03.22 Version 11.0.0
- Upgraded to version 2.1.0 of the autorest-clientruntime which includes several important fixes to mitigate a commonly-seen "Connection reset by peer" error and other similar bugs.
- Support for 2018-11-09 REST version. Please see our REST API documentation and blogs for information about the related added features.
- Added appendBlockFromURL method. A block may be created with another blob as its source.
- Added uploadPagesFromURL method. Pages may be written to with another blob as their source.
- Fixed a bug that would set an invalid range header when downloading an empty blob.
- Modified the LoggingFactory to redact SAS signatures on outgoing requests.
- HTTPGetterInfo was made an internal type as it is an internal implementation detail.
- Removed DEFAULT and NONE static variables. Empty constructors should be used instead. DEFAULT static values were error prone and unsafe to use because although the field was final, the objects were mutable, so it was possible the value could be changed accidentally and alter the behavior of the program.
- Optimized the TransferManager download to file method to skip the initial HEAD request.
- Added an option to configure that maximum size data that will be uploaded in a single shot via the TransferManager.
- Added request Http Method, URL, and headers to logging messages.
- Changed *ListingDetails to *ListDetails. These name changes are to mitigate conflicts with v8, allowing for side-by-side loading of different versions, which may help with upgrading.
- Removed the extra quotes around etags in some responses so they are consistently now consistently formatted.
- Moved the Generated*** types into the blob package to avoid conflicts with generated types from other services (i.e. queues and files)
- Changed the logger name to be the name of class that uses it, which is a more conventional practice
- Support added for SAS tokens to scope to blob snapshot.
- Added getUserDelegationKey to ServiceURL, the result of which can be used to generate a user-delegation SAS.
- Made the max results field on listing responses Integer instead of int as it is an optional field only returned when specified in the request.

## 2019.02.15 Version 10.5.0
- Added uploadFromNonReplayableFlowable to support uploading arbitrary data sources (like network streams) to a block blob.

## 2019.01.11 Version 10.4.0
- Fixed a bug that caused errors when java.io.tempdir has no trailing separator.
- Upgrade autorest-clientruntime dependency to include some bug fixes.

## 2018.11.19 Version 10.3.0
- Added support for SLF4J.
- Included default logging to log warnings and errors to the temp directory by default.
- Fixed a bug in hierarchical listings that would sometimes return incomplete results.
- Included the whole HTTP Request in log statements (except for sensitive authorization information, which is redacted).
- Fixed a bug that made the request property on the response object always null.

## 2018.10.29 Version 10.2.0
- Added overloads which only accept the required parameters.
- Added CopyFromURL, which will do a synchronous server-side copy, meaning the service will not return an HTTP response until it has completed the copy.
- Added support for IProgressReceiver in TransferManager operations. This parameter was previously ignored but is now supported.
- Removed internal dependency on javafx to be compatible with openjdk.
- Fixed a bug that would cause downloading large files with the TransferManager to fail.
- Fixed a bug in BlobURL.download() logic for setting up reliable download. This had the potential to download the wrong range when a download stream was retried.

## 2018.09.11 Version 10.1.0
- Interfaces for helper types updated to be more consistent throughout the library. All types, with the exception of the options for pipeline factories, use a fluent pattern.
- Removed RetryReader type as it's functionality was moved to be built into the DownloadResponse. RetryReaderOptions are now named DownloadRetryOptions.
- Restructured the access conditions to be more logically adhere to their respective functions.
- Added support for context parameter on each api to allow communication with the pipeline from the application level

## 2018.08.22 Version 10.0.4-rc
- Support for the 2017-11-09 REST version. Please see our REST api documentation and blogs for information about the related added features.
- Support for 2018-03-28 REST version. Please see our REST api documentation and blogs for information about the related added features.
- Support for the getAccountInfo api on ServiceURL, ContainerURL, and BlobURL.
- Added support for setting service properties related to static websites.
- Changed BlobURL.startCopy sourceAccessConditions parameter to be HTTPAccessConditions as lease is not actually supported.
- Added methods to TransferManager for conveniently downloading a blob to a file.
- UploadFromFile now takes an AsynchronousFileChannel.
- UploadByteBuffersToBlockBlob, UploadByteBufferToBlockBlob, and DownloadToBuffer have been removed.
- IPRange fields are now strings.
- Fixed retry policy.
- Fixed logging policy.

## 2018.08.08 Version 10.0.3-Preview
- Resolved dependency issues

## 2018.08.07 Version 10.0.2-Preview
- Support for 2017-07-29 REST version. Please see our REST api documentation and blogs for information about the related added features.
- Support for setting a block blob's tier.
- Added support for soft delete feature. If a delete retention policy is enabled through the set service properties API, then blobs or snapshots can be deleted softly and retained for a specified number of days, before being permanently removed by garbage collection.
- Changed BlobListingDetails constructor to take a flag to include deleted blobs.
- Restructured the blob and container listing responses.
- BlockBlobURL.MAX_PUT_BLOCK_BYTES renamed to BlockBlobURL.MAX_STAGE_BLOCK_BYTES.
- Changed the accessConditions parameter to be HTTPAccessConditions instead of BlobAccessConditions, since only http access conditions are supported.

## 2018.07.03 Version 10.0.1-Preview
- Added the RetryReader class to allow for more reliable streaming on large downloads. This is now the return type of blobURL.download
- Fixed a bug that caused generation of signatures to fail at high levels of parallelism.
- Created the StorageException type to give easy access to the ErrorCode, StatusCode, and Message as available for unsuccessful responses.
- Added the StorageErrorCode type for checking against error codes returned by the service.
- Changed the AppendBlobAccessConditions field types to be Long instead of Int.
- Upgraded Netty dependency to allow uploading memory mapped files with https.
- Upgraded the autorest runtime dependency to fix a dependency bug in their package.
- Changed RequestRetryOptions maxTries and tryTimeout fields to be Integer instead of int. 0 is no longer allowed.
- Changed CommonRestResponse.lastModifiedTime to be lastModified.
- Added statusCode property to CommonRestResponse.
- Change dateProperty to be date on all generated types.
- Fixed a bug that prevented proper reset of body stream upon retry.
- Updated the defaults for RequestRetryOptions.

## 2018.04.27 Version 10.0.0-preview
- Initial Release. Please see the README and wiki for information on the new design.
