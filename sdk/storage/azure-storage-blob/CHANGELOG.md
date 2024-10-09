# Release History

## 12.29.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 12.28.1 (2024-10-08)

### Bugs Fixed
- Fixed a bug where downloadToFile and openInputStream was throwing an InvalidRange exception if the target file size was a multiple of the
  authenticated region length.

### Other Changes

#### Dependency Updates
- Upgraded `azure-storage-common` from `12.27.0` to version `12.27.1`.

## 12.28.0 (2024-09-17)

### Features Added
- Added ability to retrieve SAS string to sign for debugging purposes.
- Added support for service version 2024-11-04.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.50.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.4`.
- Upgraded `azure-storage-common` from `12.26.0` to version `12.27.0`.
- Upgraded `azure-storage-internal-avro` from `12.12.0` to version `12.13.0`.

## 12.28.0-beta.1 (2024-08-06)

### Features Added
- Added ability to retrieve SAS string to sign for debugging purposes.
- Added support for service version 2024-11-04.

## 12.27.0 (2024-07-18)

### Features Added
- Added support for getting account info on blob container clients and the blob base client.
- Added support for bearer token challenges.
- Added support for service version 2024-08-04.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.
- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-storage-common` from `12.25.1` to version `12.26.0`.
- Upgraded `azure-storage-internal-avro` from `12.11.1` to version `12.12.0`.

## 12.27.0-beta.1 (2024-06-11)

### Features Added
- Added support for getting account info on blob container clients and the blob base client.
- Added support for bearer token challenges.
- Added support for service version 2024-08-04.

## 12.26.1 (2024-06-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-storage-common` from `12.25.0` to version `12.25.1`.
- Upgraded `azure-storage-internal-avro` from `12.11.0` to version `12.11.1`.

## 12.26.0 (2024-05-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.
- Upgraded `azure-storage-common` from `12.25.0-beta.2` to version `12.25.0`.
- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-storage-internal-avro` from `12.11.0-beta.2` to version `12.11.0`.


## 12.25.4 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.
- Upgraded `azure-storage-common` from `12.24.3` to version `12.25.0-beta.2`.
- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-storage-internal-avro` from `12.10.3` to version `12.11.0-beta.2`.


## 12.26.0-beta.1 (2024-04-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

### Breaking Changes
- When creating a `BlobClient` via `BlobContainerClient.getBlobClient(String blobName)` or 
`BlobServiceClient.getBlobClient(String blobName)`, the blob name will be stored exactly as passed in and will not be 
URL-encoded. For example, if blob name is "test%25test" and is created by calling 
`BlobContainerClient.getBlobClient("test%25test")` or `BlobClient.getBlobName("test%25test")`, 
`BlobClient.getBlobName()` will return "test%25test" and the blob's url will result in 
“https://account.blob.core.windows.net/container/test%25%25test”.

## 12.25.3 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.
- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-storage-common` from `12.24.2` to version `12.24.3`.
- Upgraded `azure-storage-internal-avro` from `12.10.2` to version `12.10.3`.


## 12.25.2 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-storage-common` from `12.24.1` to version `12.24.2`.
- Upgraded `azure-storage-internal-avro` from `12.10.1` to version `12.10.2`.


## 12.25.1 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.
- Upgraded `azure-storage-internal-avro` from `12.10.0` to version `12.10.1`.
- Upgraded `azure-storage-common` from `12.24.0` to version `12.24.1`.
- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.

## 12.25.0 (2023-11-08)

### Features Added
- Added BlobProperties.getRequestId() to access the x-ms-request-id header property.
- Added support for service versions 2023-11-03.
- Added support for BlobAudience.

## 12.24.1 (2023-10-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-storage-internal-avro` from `12.9.0` to version `12.9.1`.
- Upgraded `azure-storage-common` from `12.23.0` to version `12.23.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.

## 12.25.0-beta.1 (2023-10-19)

### Features Added
- Added support for service versions 2023-11-03.
- Added support for BlobAudience.

## 12.24.0 (2023-09-12)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.
- Added RehydratePendingToCold value to ArchiveStatus enum.

## 12.23.1 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-common` from `12.22.0` to version `12.22.1`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-storage-internal-avro` from `12.8.0` to version `12.8.1`.

## 12.24.0-beta.1 (2023-08-08)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.
- Added RehydratePendingToCold value to ArchiveStatus enum.

## 12.23.0 (2023-07-11)

### Features Added
- Added support for the `2023-01-03` service version.
- Content length limit for `AppendBlobClient.appendBlock()` raised from 4 MiB to 100 MiB.

## 12.22.3 (2023-06-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-storage-common` from `12.21.1` to version `12.21.2`.
- Upgraded `azure-storage-internal-avro` from `12.7.1` to version `12.8.0-beta.2`.

## 12.23.0-beta.1 (2023-05-30)

### Features Added
- Added support for 2023-01-03 service version.
- Content length limit for `AppendBlobClient.appendBlock()` raised from 4 MiB to 100 MiB.

## 12.22.2 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-common` from `12.21.0` to version `12.21.1`.
- Upgraded `azure-storage-internal-avro` from `12.7.0` to version `12.7.1`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.
- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.

## 12.22.1 (2023-05-15)

### Bugs Fixed
Fixed bug in `BlobBaseClient.downloadToFile()` and `BlobBaseClient.downloadToFileWithResponse()` where incorrect data could be written to the file in some circumstances if the SDK experienced a network error when using `azure-core-http-netty`'s `HttpClient` implementation.

## 12.22.0 (2023-04-13)

### Features Added
- Added new method `StorageAccountInfo.isHierarchicalNamespaceEnabled()` to determine whether storage account has hierarchical namespace enabled.  
- Added support for 2022-11-02 service version.
- Added support for reading and writing a block blob via SeekableByteChannel.

### Bugs Fixed
- Fixed bug for when `FindBlobsOptions.setMaxResultsPerPage(Integer)` was set and `.byPage()` was called on `BlobServiceAsyncClient.findBlobsByTags(FindBlobsOptions)`, number of results being returned was greater than the specified max results.

## 12.22.0-beta.1 (2023-03-28)

### Features Added
- Added support for 2022-11-02 service version.
- Added support for reading and writing a block blob via SeekableByteChannel.

### Bugs Fixed
- Fixed bug for when `FindBlobsOptions.setMaxResultsPerPage(Integer)` was set and `.byPage()` was called on `BlobServiceAsyncClient.findBlobsByTags(FindBlobsOptions)`, number of results being returned was greater than the specified max results.

## 12.21.1 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-common` from `12.20.0` to version `12.20.1`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-storage-internal-avro` from `12.6.0` to version `12.6.1`.

## 12.21.0 (2023-02-21)

### Features Added
- Added `BlobDownloadHeaders.getCreationTime()` and `BlobDownloadHeaders.setCreationTime(OffsetDateTime)` to access the x-ms-creation-time property.
- Added support for 2021-12-02 service version.
- Added support for Blob Cold Tier `AccessTier.COLD`.
- Added new overload `AppendBlobClient.getBlobOutputStream(boolean)` that takes in a boolean for overwrite and appends to existing data if overwrite is specified `false`, or deletes and recreates a blob if overwrite is specified `true`.

### Bugs Fixed
- Fixed bug where `BlobErrorCode.IncrementalCopyOfEarlierVersionSnapshotNotAllowed` was spelled incorrectly.

## 12.21.0-beta.1 (2023-02-07)

### Features Added
- Added support for 2021-12-02 service version.
- Added support for Blob Cold Tier `AccessTier.COLD`.
- Fixed bug where `BlobErrorCode.IncrementalCopyOfEarlierVersionSnapshotNotAllowed` was spelled incorrectly.
- Added new overload `AppendBlobClient.getBlobOutputStream(boolean)` that takes in a boolean for overwrite and appends to existing data if overwrite is specified `false`, or deletes and recreates a blob if overwrite is specified `true`.

## 12.20.2 (2023-01-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.
- Upgraded `azure-storage-common` from `12.19.1` to version `12.19.2`.
- Upgraded `azure-storage-internal-avro` from `12.5.1` to version `12.5.2`.

## 12.20.1 (2022-11-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.
- Upgraded `azure-storage-common` from `12.19.0` to version `12.19.1`.
- Upgraded `azure-storage-internal-avro` from `12.5.0` to version `12.5.1`.

## 12.20.0 (2022-10-11)

### Features Added
- Added support for 2021-10-04 service version.
- Added ability to rename existing containers with `BlobContainerClient.rename()`.
- Added new overload for `BlobClient.upload()` that takes in `InputStream` without specifying length.

### Breaking Changes
- `BlobItem.isPrefix()` now returns false if it is not a virtual directory instead of null.

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.
- Upgraded `azure-storage-common` from `12.18.1` to version `12.19.0`.
- Upgraded `azure-storage-internal-avro` from `12.4.2` to version `12.5.0`.

## 12.19.1 (2022-09-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.
- Upgraded `azure-storage-common` from `12.18.0` to version `12.18.1`.
- Upgraded `azure-storage-internal-avro` from `12.4.1` to version `12.4.2`.

## 12.20.0-beta.1 (2022-09-06)

### Features Added
- Added support for 2021-10-04 service version.
- Added new overload for `BlobClient.upload()` that takes in `InputStream` without specifying length.

### Breaking Changes
- `BlobItem.isPrefix()` now returns false if it is not a virtual directory instead of null.

## 12.19.0 (2022-08-12)

### Features Added
- Added `ParallelTransferOptions.getProgressListener` and `ParallelTransferOptions.setProgressListener`
  that replaces deprecated `ParallelTransferOptions.getProgressReceiver` and `ParallelTransferOptions.setProgressReceiver`
- `com.azure.storage.blob.ProgressReceiver` extends `com.azure.core.util.ProgressListener` for backwards compatibility.
- Added `BlobDownloadAsyncResponse.writeValueToAsync` and `BlobDownloadAsyncResponse.close`.
- Added `BlockBlobSimpleUploadOptions(BinaryData)` constructor, `BlockBlobStageBlockOptions`,
  `BlockBlobAsyncClient.stageBlock(String, BinaryData)`, `BlockBlobAsyncClient.stageBlockWithResponse(BlockBlobStageBlockOptions)`,
  `BlockBlobAsyncClient.upload(BinaryData)`, `BlockBlobAsyncClient.upload(BinaryData, boolean)`,
  `BlockBlobClient.stageBlock(String, BinaryData)`, `BlockBlobClient.stageBlockWithResponse(BlockBlobStageBlockOptions, Duration, Context)`,
  `BlockBlobClient.upload(BinaryData)`, `BlockBlobClient.upload(BinaryData, boolean)`

### Other Changes
- `com.azure.storage.blob.ProgressReceiver` and `com.azure.storage.blob.ProgressReporter` are deprecated
  and replaced by `com.azure.core.util.ProgressListener` and `com.azure.core.util.ProgressReporter`.

## 12.18.0 (2022-07-07)

### Features Added
- BlobOutputStream.close() will now attempt to close the stream at first attempt. Subsequent calls to close have no effect.
- GA release for 2021-08-06 service version.

## 12.18.0-beta.1 (2022-06-15)

### Features Added
- Added support for 2021-08-06 service version.

## 12.17.1 (2022-06-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.
- Upgraded `azure-storage-common` from `12.16.0` to version `12.16.1`.
- Upgraded `azure-storage-internal-avro` from `12.3.0` to version `12.4.0-beta.1`.

## 12.17.0 (2022-05-25)

### Other Changes
- GA release for STG 82

### Other Changes
- Deprecated BlobClientBase.download and BlobClientBase.downloadWithResponse that downloads entire blob into an output
  stream. Use BlobClientBase.downloadStream and BlobClientBase.downloadStreamWithResponse instead.

## 12.16.1 (2022-05-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.
- Upgraded `azure-storage-common` from `12.15.1` to version `12.15.2`.
- Upgraded `azure-storage-internal-avro` from `12.2.1` to version `12.2.2`.

## 12.17.0-beta.1 (2022-05-06)

### Features Added
- Added support for 2021-06-08 service version.
- Added the ability to list PageRanges and PageRangesDiff by page.

## 12.16.0 (2022-04-07)

### Bugs Fixed
- Fixed a bug where `uploadFromFile(String, boolean)` used a different size to determine whether blob existence should
  be checked before using chunked upload than the size used to triggered chunked upload.
### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.
- Upgraded `azure-storage-common` from `12.15.0` to version `12.15.1`.
- Upgraded `azure-storage-internal-avro` from `12.2.0` to version `12.2.1`.

## 12.15.0 (2022-03-09)

### Bugs Fixed
- Fixed a bug where a timeout parameter was being ignored on an uploadFromFileWithResponse. 

### Other Changes
- GA release for STG 79, 80, 81

## 12.14.4 (2022-02-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.
- Upgraded `azure-storage-common` from `12.14.2` to version `12.14.3`.
- Upgraded `azure-storage-internal-avro` from `12.1.3` to version `12.1.4`.

## 12.15.0-beta.3 (2022-02-09)

### Features Added
- Added support for 2021-04-10 service version.
- Added support for filterBlobs api on container clients.

### Bugs Fixed
- Fixed a bug in builders that would cause container or blobName to be erased if specified before the connection string.

## 12.14.3 (2022-01-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.2` to version `1.11.6`.
- Upgraded `azure-storage-common` from `12.14.1` to version `12.14.2`.
- Upgraded `azure-storage-internal-avro` from `12.1.2` to version `12.1.3`.

## 12.15.0-beta.2 (2021-12-07)

### Features Added
- Added support for 2021-02-12 service version.
- Added support for listing system containers.
- Added support for listing blobs which contain invalid xml characters.

- When opening a BlobInputStream, removed the initial getProperties call in favor of a download for better performance.

### Bugs Fixed
- Fixed a bug that would cause authenticating with a sas token to fail if the timestamps in the token were formatted differently.

### Other Changes
- Deprecated BlobClient.uploadWithResponse that does not return a response.

## 12.14.2 (2021-11-10)

### Other Changes
#### Dependency Updates
- Updated to version `1.22.0` of `azure-core`
- Updated to version `12.14.1` of `azure-storage-common`

## 12.15.0-beta.1 (2021-11-05)

### Features Added
- Added support for permanent delete permissions in blob and account level SAS.

- Added support for the 2020-12-06 service version.
- Added support for setting an encryption scope on a BlobServiceSas and an AccountSas.
- Added support for setting encryption scopes on the destination of a sync copy.

## 12.14.1 (2021-10-12)

### Bugs Fixed

- Fixed a bug when Blob Endpoint is provided as an IP address (e.g., `https://x.x.x.x:10000`) with 
  empty component path, the parsing fails with StringIndexOutOfBoundsException

### Other Changes
#### Dependency Updates
- Updated to version `1.21.0` of `azure-core`
- Updated to version `12.14.0` of `azure-storage-common`

## 12.14.0 (2021-09-15)
- GA release

## 12.14.0-beta.1 (2021-07-28)
- Fixed a bug where BlobClient.exists would not function correctly on blobs encrypted with CPK.
- Added support for the 2020-10-02 service version.
- Added support to list blobs deleted with versioning enabled.
- Added support to specify Parquet Input Serialization when querying a blob.
- Updated DownloadRetryOptions.maxRetryRequests to default downloads to retry 5 times.

## 12.13.0 (2021-07-22)
- Added support to get a blob client that uses an encryption scope and customer provided key.  

## 12.12.0 (2021-06-09)
- GA release

## 12.12.0-beta.1 (2021-05-13)
- Added support for the 2020-08-04 service version.
- Deprecated support to undelete a blob container to a new name. 

## 12.11.1 (2021-05-13)
### Dependency Updates
- Updated `azure-core` to version `1.16.0`

## 12.11.0 (2021-04-29)
- Fixed a bug where large files would not respond when the upload method was called. 

## 12.11.0-beta.3 (2021-04-16)
- Fixed a bug where BlobOutputStream would lock up if the inner uploadWithResponse call is cancelled for any reason.
- Fixed a bug where BlobOutputStream could not respond when writing in a tight loop because the inner FluxSink would buffer in an unbounded manner. This would cause memory issues especially if the heap size was set to less than the size of the data being uploaded.
- Fixed a bug where a null check was placed on the wrong parameter of the InputStream constructor for BlobParallelUploadOptions

## 12.11.0-beta.2 (2021-03-29)
- Fixed a bug where downloading would throw a NPE on large downloads due to a lack of eTag.
- Fixed a bug where more data would be buffered in buffered upload than expected due to Reactor's concatMap operator.
- Added upload and download methods on BlobClient and BlobAsyncClient that work with BinaryData.
- Fixed a bug that ignored the page size when calling PagedIterable.byPage(pageSize)

## 12.10.2 (2021-03-26)
- Fixed a bug where BlobInputStream would not use request conditions when doing the initial getProperties call in openInputStream.

## 12.10.1 (2021-03-19)
- Removed a deep copy in the general upload path to reduce memory consumption and increase perf
- Added a deep copy immediately after calling BlobOutputStream.write to prevent overwriting data in the case of reusing a single buffer to write to an output stream

## 12.11.0-beta.1 (2021-02-10)
- Added support for the 2020-06-12 service version. 
- Added support to lock on version id by specifying a consistent read control when opening a BlobInputStream.
- Removed a deep copy in the general upload path to reduce memory consumption and increase perf
- Added a deep copy immediately after calling BlobOutputStream.write to prevent overwriting data in the case of reusing a single buffer to write to an output stream

## 12.10.0 (2021-01-14)
- GA release

## 12.10.0-beta.1 (2020-12-07)
- Exposed ClientOptions on all client builders, allowing users to set a custom application id and custom headers.
- Added ability to get container client from blob clients and service client from container clients
- Added a MetadataValidationPolicy to check for leading and trailing whitespace in metadata that would cause Auth failures.
- Fixed a bug where the error message would not be displayed the exception message of a HEAD request.
- Added support for the 2020-04-08 service version. 
- Added support to upload block blob from URL.
- Added lease ID parameter to Get and Set Blob Tags.
- Added blob tags to BlobServiceClient.findBlobsByTags() result.

## 12.9.0 (2020-11-11)
- Fixed a bug where interspersed element types returned by page listing would deserialize incorrectly.
- Fixed a bug where BlobInputStream would not eTag lock on the blob, resulting in undesirable behavior if the blob was modified in the middle of reading. 
- Renamed BlobDownloadToFileOptions.rangeGetContentMd5 to BlobDownloadToFileOptions.retrieveContentRangeMd5.
- Added support for move and execute permissions on blob SAS and container SAS, and list permissions on blob SAS.
- Added support to specify a preauthorized user id and correlation id for user delegation SAS.

## 12.9.0-beta.2 (2020-10-08)
- Added support to specify whether or not a pipeline policy should be added per call or per retry.

## 12.9.0-beta.1 (2020-10-01)
- Added support for the 2020-02-10 service version.
- Added support to specify Arrow Output Serialization when querying a blob. 
- Added support to undelete a container. 
- Added support to set BlobParallelUploadOptions.computeMd5 so the service can perform an md5 verification.
- Added support to specify block size when using BlobInputStream.
- Fixed a bug where users could not download more than 5000MB of data in one shot in the downloadToFile API.
- Fixed a bug where the TokenCredential scope would be incorrect for custom URLs.
- Fixed a bug where Default Azure Credential would not work with Azurite.
- Fixed a bug where a custom application id in HttpLogOptions would not be added to the User Agent String.
- Fixed a bug where BlockBlobOutputStream would not handle certain errors.
- Added BlobImmutableDueToPolicy to the BlobErrorCode enum.

## 12.8.0 (2020-08-13)
- Fixed a bug that, when the data length parameter did not match the actual length of the data in BlobClient.upload, caused a zero length blob to be uploaded rather than throwing an exception.
- Fixed a bug that ignored the customer's specified block size when determining buffer sizes in BlobClient.upload
- Added support for Object Replication Service on listBlobs and getProperties.
- Added support for blob tags. Added tagsConditions to BlobRequestConditions that allow a user to specify a SQL statement for the blob's tags to satisfy.
- Added support for setting tags and filterTags operations on SAS by adding to AccountSASPermissions, BlobSASPermissions, and BlobContainerSASPermissions.
- Added support for setting and getting the StaticWebsite.DefaultIndexDocumentPath property on the service client.
- Added RehydratePriority to BlobProperties and BlobItemProperties.
- Fixed bug where Query Input Stream would throw when a ByteBuffer of length 0 was encountered.
- Added support to seal an append blob. Added AppendBlob.seal. Added ability to specify destinationSealed on BlobClient.beginCopy. isSealed property returned on getProperties/getBlob/listBlob. 
- Added support to set tier on a snapshot or version.
- Fixed a bug that would cause buffered upload to always put an empty blob before uploading actual data.

## 12.8.0-beta.1 (2020-07-07)
- Added support for the 2019-12-12 service version.
- Added support for blob tags. Added get/setTags method to Blob(Async)ClientBase. Added filterTags api to BlobServiceClient. Added ability to specify tags on all methods that create a blob. Tag count returned on getProperties/getBlob. Option to include returning tags on listing. 
- Added support to query a blob. Added query and openQueryInputStream methods to Blob(Async)ClientBase.
- Added support to version a blob. Added `getVersionClient` to clients that returns a new client associated to the version. 
- Added support to increase the maximum size of data that can be sent via a stage block. 

## 12.7.0 (2020-06-12)
- Moved BlobParallelUploadOptions into options package.
- Added data source and data length to BlobParallelUploadOptions and removed them from the relevant method parameter lists

## 12.7.0-beta.1 (2020-06-08)
- Fixed a bug that would cause empty data to be sent if a call to stage block, block blob upload, append block, or upload pages was automatically retried by the SDK.
- Added a maxConcurrency option on ParallelTransferOptions that allows the customer to limit how many concurrent network requests will be outstanding per api request at once. 
- Added an overload to BlobClient.upload which returns a BlockBlobItem containing the properties returned by the service upon blob creation.
- Fixed a bug that caused auth failures when constructing a client to a secondary endpoint using token auth.
- Modified client constructors to throw on invalid urls early to prevent SAS tokens from being logged in Exceptions.

## 12.6.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.6.0 (2020-04-06)
- Fixed a bug that would prevent client initialization against Azurite in some containerized environments.
- Fixed a bug that would prevent progress from being reported when uploading small files.
- Modified BlobOutputStream to wait on a condition variable until transfer is complete instead of polling.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.
- Fixed a bug that ignored the timeout and context parameters on BlobClient.uploadWithResponse.
- Added an overload to BlobOutputStream which accepts a context.

## 12.5.0 (2020-03-11)
- Fixed a bug that was adding an invalid 'include' query-parameter for list blob item requests if no dataset-include options were specified.
- Fixed a bug in ReliableDownload that would cause multiple subscriber errors.
- Added logic to ReliableDownload to retry on TimeoutException
- Added default timeout to download stream to timeout if a certain amount of time passes without seeing any data.
- Fixed a bug that would cause IOExceptions to be swallowed in BlobClient.upload(InputStream, long)

## 12.4.0 (2020-02-12)
- Added ability to access BlobProperties from BlobInputStream.
- Modified downloadToFile to populate BlobProperties.blobSize to be the actual blob size instead of the content length of the first range.
- Added upload methods on BlobClient to upload from an InputStream.

- Added support for the 2019-07-07 service version.
- Added support for encryption scopes service, container and blob builders now accept an encryption scope parameter and service and container builders accept a BlobContainerEncryptionScope parameter.
- Added support for managed disk page range diff for managed disk accounts.

## 12.3.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-common` to version 12.3.1

## 12.3.0 (2020-01-16)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.3.0/sdk/storage/azure-storage-blob/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.3.0/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob)

- Added ability to create service clients anonymously and should only be used to create anonymous container and blob clients. Anonymous service clients will throw on attempting to create network requests.
- Added an overload to listBlobs to include a continuation token.
- Added a check in BlobServiceClient.setAccountProperties to block invalid requests.
- Fixed a bug that could result in data corruption on download when using the downloadToFile method.

## 12.2.0 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.2.0/sdk/storage/azure-storage-blob/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob_12.2.0/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob)

- Added a field to ParallelTransferOptions that allows customers to configure the maximum size to upload in a single PUT. Data sizes larger than this value will be chunked and parallelized.
- Added overloads to downloadToFile to add the option to overwrite existing files. Default behavior is to not overwrite.
- Improved performance of BlockBlobOutputStream.
- Added overloads to BlockBlobClient.getBlobOutputStream to allow users to provide parallel transfer options, http headers, metadata, access tier, and request conditions.

## 11.1.1 (2019.04.30)
- Upgraded to version 2.1.1 of the autorest-clientime which upgrades to a more secure version of jackson and fixes a NPE on unknown host errors.

## 11.0.0 (2019.03.22)
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

## 10.5.0 (2019.02.15)
- Added uploadFromNonReplayableFlowable to support uploading arbitrary data sources (like network streams) to a block blob.

## 10.4.0 (2019.01.11)
- Fixed a bug that caused errors when java.io.tempdir has no trailing separator.
- Upgrade autorest-clientruntime dependency to include some bug fixes.

## 12.2.0-beta.1 (2019-12-17)
- Added SAS generation methods on clients to improve discoverability and convenience of sas. Deprecated setContainerName, setBlobName, setSnapshotId, generateSasQueryParameters methods on BlobServiceSasSignatureValues to direct users to using the methods added on clients.
- Fixed a bug where Account SAS would not work when set on clients.

## 12.1.0 (2019-12-04)
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

## 12.0.0 (2019-10-31)

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

## 12.0.0-preview.4 (2019-10-08)
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

## 12.0.0-preview.3 (2019-09-10)
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

## 12.0.0-preview.2 (2019-08-08)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-08-06/java.html).

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

## 12.0.0-preview.1 (2019-06-28)
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

## 10.3.0 (2018.11.19)
- Added support for SLF4J.
- Included default logging to log warnings and errors to the temp directory by default.
- Fixed a bug in hierarchical listings that would sometimes return incomplete results.
- Included the whole HTTP Request in log statements (except for sensitive authorization information, which is redacted).
- Fixed a bug that made the request property on the response object always null.

## 10.2.0 (2018.10.29)
- Added overloads which only accept the required parameters.
- Added CopyFromURL, which will do a synchronous server-side copy, meaning the service will not return an HTTP response until it has completed the copy.
- Added support for IProgressReceiver in TransferManager operations. This parameter was previously ignored but is now supported.
- Removed internal dependency on javafx to be compatible with openjdk.
- Fixed a bug that would cause downloading large files with the TransferManager to fail.
- Fixed a bug in BlobURL.download() logic for setting up reliable download. This had the potential to download the wrong range when a download stream was retried.

## 10.1.0 (2018.09.11)
- Interfaces for helper types updated to be more consistent throughout the library. All types, with the exception of the options for pipeline factories, use a fluent pattern.
- Removed RetryReader type as it's functionality was moved to be built into the DownloadResponse. RetryReaderOptions are now named DownloadRetryOptions.
- Restructured the access conditions to be more logically adhere to their respective functions.
- Added support for context parameter on each api to allow communication with the pipeline from the application level

## 10.0.4-rc (2018.08.22)
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

## 10.0.3-Preview (2018.08.08)
- Resolved dependency issues

## 10.0.2-Preview (2018.08.07)
- Support for 2017-07-29 REST version. Please see our REST api documentation and blogs for information about the related added features.
- Support for setting a block blob's tier.
- Added support for soft delete feature. If a delete retention policy is enabled through the set service properties API, then blobs or snapshots can be deleted softly and retained for a specified number of days, before being permanently removed by garbage collection.
- Changed BlobListingDetails constructor to take a flag to include deleted blobs.
- Restructured the blob and container listing responses.
- BlockBlobURL.MAX_PUT_BLOCK_BYTES renamed to BlockBlobURL.MAX_STAGE_BLOCK_BYTES.
- Changed the accessConditions parameter to be HTTPAccessConditions instead of BlobAccessConditions, since only http access conditions are supported.

## 10.0.1-Preview (2018.07.03)
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

## 10.0.0-preview (2018.04.27)
- Initial Release. Please see the README and wiki for information on the new design.
