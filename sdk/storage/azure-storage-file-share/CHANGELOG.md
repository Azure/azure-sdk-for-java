# Release History

## 12.11.0-beta.1 (2021-07-28)
- Added support to reliably download a file. 
- Added support for the 2020-10-02 service version.
- Fixed a bug that was cause an Exception when downloading a zero length file.

## 12.10.0 (2021-06-09)
- GA release

## 12.10.0-beta.1 (2021-05-13)
- Added support for the 2020-08-04 service version.
- Added back ability to create a ShareLeaseClient for a Share or Share Snapshot.
- Added upload() overloads to ShareFileClient supporting large ranges and parallel upload.
- Deprecated old upload() overloads on ShareFileClient that only supported single Put Range operations, replacing them
with uploadRange() methods.

## 12.9.1 (2021-05-13)
### Dependency Updates
- Updated `azure-core` to version `1.16.0`
- Updated `azure-storage-common` to version `12.11.1`

## 12.9.0 (2021-04-29)
- ShareLeaseClient now updates it's leaseID through a lease change.
- Fixed a bug where working with a root directory client could improperly form requests and subdirectory clients

## 12.9.0-beta.3 (2021-04-16)
- Updated azure-storage-common dependencies.

## 12.9.0-beta.2 (2021-03-29)
- Updated azure-storage-common and azure-core dependencies.

## 12.9.0-beta.1 (2021-02-10)
- Added support for the 2020-06-12 service version. 

## 12.8.0 (2021-01-14)
- GA release

## 12.8.0-beta.1 (2020-12-07)
- Exposed ClientOptions on all client builders, allowing users to set a custom application id and custom headers.
- Fixed a bug where snapshot would be appended to a share snapshot instead of sharesnapshot.
- Fixed a bug where the sharesnapshot query parameter would be ignored in share and share file client builders.
- Fixed a bug where the error message would not be displayed the exception message of a HEAD request.
- Added a MetadataValidationPolicy to check for leading and trailing whitespace in metadata that would cause Auth failures.
- Added support for the 2020-04-08 service version. 
- Added support for specifying enabled protocols on share creation
- Added support for setting root squash on share creation and through set properties.

## 12.7.0 (2020-11-11)
- Added support to specify whether or not a pipeline policy should be added per call or per retry.
- Added support for setting access tier on a share through ShareClient.create, ShareClient.setAccessTier.
- Added support for getting access tier on a share through ShareClient.getProperties, ShareServiceClient.listShares
- Fixed a bug where interspersed element types returned by range diff listing would deserialize incorrectly.
- Renamed setAccessTier to setProperties and deprecated setQuotaInGb in favor of setProperties.
- Renamed DeleteSnapshotsOptionType to ShareSnapshotsDeleteOptionType in ShareClient.delete
- Removed ability to create a ShareLeaseClient for a Share or Share Snapshot. This feature has been rescheduled for future release.

## 12.7.0-beta.1 (2020-10-01)
- Added support for the 2020-02-10 service version. 
- Added support to getFileRanges on a previous snapshot by adding the getFileRangesDiff API. 
- Added support to set whether or not smb multichannel is enabled.
- Added support to lease shares and snapshot shares.
- Added support to specify a lease id for share operations.
- Fixed a bug where getProperties on a file client would throw a HttpResponseException instead of ShareStorageException.
- Fixed a bug where snapshot would be appended to a share snapshot instead of sharesnapshot.
- Fixed a bug that would cause auth failures when building a client by passing an endpoint which had a sas token with protocol set to https,http
- Fixed a bug where a custom application id in HttpLogOptions would not be added to the User Agent String.

## 12.6.0 (2020-08-13)
- GA release for 2019-12-12 service version

## 12.6.0-beta.1 (2020-07-07)
- Added support for the 2019-12-12 service version.
- Added support for restoring file share.

## 12.5.0 (2020-06-12)
- Fixed bug in ShareFileClient.uploadRangeFromUrl and ShareFileClient.beginCopy where sourceUrl was not getting encoded.
- Updated azure-storage-common and azure-core dependencies. 

## 12.4.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.4.0 (2020-04-06)
- Fixed an issue where whitespace would cause NtfsFileAttributes.toAttributes/fromAttributes to result in an error parsing the attributes.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.

## 12.3.0 (2020-03-11)
- Added support for exists methods on Share, ShareDirectory and ShareFile clients.

## 12.2.0 (2020-02-12)
- Fixed bug in ShareClient.getStatistics where shareUsageInGB was not properly converted. Added parameter to ShareStatistics to include a shareUsageInBytes parameter.
- Fixed bug where ShareDirectoryAsyncClient.getFileClient appended an extra / for files in the root directory.

- Added support for the 2019-07-07 service version.
- Added support for file leases. Includes adding the ShareLeaseClientBuilder, ShareLeaseClient, and ShareLeaseAsync client and overloads accepting leaseIds for operations that support leases.
- Added failedClosedHandles property to CloseHandlesInfo to allow users to access number of failed handles in forceCloseAllHandles and closeHandle.
- Added support for obtaining premium file properties in ShareServiceClient.listShares and ShareClient.getProperties.
- Added support for additional start copy parameters - FileSmbProperties, file permission, file permission copy mode, set archive and ignore read only.

## 12.1.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-common` to version 12.3.1

## 12.1.0 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-share_12.1.0/sdk/storage/azure-storage-file-share/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-share_12.1.0/sdk/storage/azure-storage-file-share/src/samples/java/com/azure/storage/file/share)

## 12.1.0-beta.1 (2019-12-18)
- Added SAS generation methods on clients to improve discoverability and convenience of sas. Deprecated setFilePath, setShareName generateSasQueryParameters methods on ShareServiceSasSignatureValues to direct users to using the methods added on clients.

## 12.0.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-share_12.0.0/sdk/storage/azure-storage-file-share/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-share_12.0.0/sdk/storage/azure-storage-file-share/src/samples/java/com/azure/storage/file/share)

- GA release.
- Changed return type for forceCloseHandle from void to CloseHandlesInfo.
- Changed return type for forceCloseAllHandles from int to CloseHandlesInfo.
- Upgraded to version 1.1.0 of Azure Core.

## 12.0.0-preview.5 (2019-10-31)
- Renamed FileReference to StorageFileItem
- Changed response of ShareClient.listFilesAndDirectories FileReference to StorageFileItem
- FileUploadRangeFromUrlInfo eTag() changed to getETag() and lastModified() changed to getLastModidified()
- Changed response of FileAsyncClient.download() from Mono<FileDownloadInfo> to Flux<ByteBuffer>
- Renamed FileAsyncClient.downloadWithPropertiesWithResponse to downloadLoadWithResponse
- Removed FileAsyncClient.uploadWithResponse(Flux<ByteBuffer>, long, long)
- Changed response of FileClient.download() from FileDownloadInfo to voif
- Renamed FileClient.downloadWithPropertiesWithResponse to downloadLoadWithResponse
- Changed FileClient upload methods to take InputStreams instead of ByteBuffers
- Removed FileClient.uploadWithResponse(ByteBuffer, long, long)
- Deleted FileDownloadInfo
- Removed FileProperty from public API
- Removed BaseFileClientBuilder
- Removed FileClientBuilder, ShareClientBuilder, and FileServiceClientBuilder inheritance of BaseFileClientBuilder
- Renamed ListShatesOptions getMaxResults and setMaxResults to getMaxResultsPerPage and setMaxResultsPerPage
- Removes StorageError and StorageErrorException from public API
- Renamed StorageErrorCode to FileErrorCode, SignedIdentifier to FileSignedIdentifier, StorageServiceProperties to FileServiceProperties, CorRules to FileCorRules, AccessPolicy to FileAccessPolicy, and Metrics to FileMetrics
- Renamed FileHTTPHeaders to FileHttpHeaders and removed File from getter names
- Replaced forceCloseHandles(String, boolean) with forceCloseHandle(String) and forceCloseHandles(boolean)
- Renamed StorageException to FileStorageException
- Added FileServiceVersion and the ability to set it on client builders
- Replaced URL parameters with String on uploadRangeFromUrl
- Replaced startCopy with beginCopy and return poller
- Renamed FileSasPermission getters to use has prefix
- Changed return type for FileClient.downloadWithProperties from Response<Void> to FileDownloadResponse and FileAsyncClient.downloadWithProperties from Mono<Response<Flux<ByteBuffer>>> to Mono<FileDownloadAsyncResponse>

## 12.0.0-preview.4 (2019-10-08)
For details on the Azure SDK for Java (October 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file_12.0.0-preview.4/sdk/storage/azure-storage-file/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file_12.0.0-preview.4/sdk/storage/azure-storage-file/src/samples/java/com/azure/storage/file)

- Getters and setters were updated to use Java Bean notation.
- Added `getShareName`, `getDirectoryPath` and `getFilePath` for fetching the resource names.
- Updated to be fully compliant with the Java 9 Platform Module System.
- Changed `VoidResponse` to `Response<Void>` on sync API, and `Mono<VoidResponse>` to `Mono<Response<Void>>` on async API.
- Fixed metadata does not allow capital letter issue. [`Bug 5295`](https://github.com/Azure/azure-sdk-for-java/issues/5295)
- Updated the return type of `downloadToFile` API to `FileProperties` on sync API and `Mono<FileProperties>` on async API.
- `getFileServiceUrl`, `getShareUrl`, `getDirectoryUrl`, `getFileUrl` API now returns URL with scheme, host, resource name and snapshot if any.
- Removed SAS token generation APIs from clients, use FileServiceSasSignatureValues to generate SAS tokens.
- Removed `SASTokenCredential`, `SASTokenCredentialPolicy` and the corresponding `credential(SASTokenCredential)` method in client builder, and added sasToken(String) instead.

## 12.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (September 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/085c8570b411defff26860ef56ea189af07d3d6a/sdk/storage/azure-storage-file/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/085c8570b411defff26860ef56ea189af07d3d6a/sdk/storage/azure-storage-file/src/samples/java/com/azure/storage/file)
demonstrate the new API.

- Added tracing telemetry on maximum overload API.
- Added generate SAS token APIs.
- Throw `StorageException` with error code when get error response from service.
- Moved `ReactorNettyClient` into a separate module as default plugin. Customer can configure a custom http client through builder.
- Throw `UnexpectedLengthException` when the upload body length does not match the input length. [GitHub #4193](https://github.com/Azure/azure-sdk-for-java/issues/4193)
- Added validation policy to check the equality of request client id between request and response.
- Added `PageFlux` on async APIs and `PageIterable` on sync APIs.
- Upgraded to use service version 2019-02-02 from 2018-11-09.
- Replaced `ByteBuf` with `ByteBuffer` and removed dependency on `Netty`.
- Added `uploadRangeFromUrl` APIs on sync and async File client.
- Added `timeout` parameter for sync APIs which allows requests throw exception if no response received within the time span.
- Added `azure-storage-common` as a dependency.
- Added the ability for the user to obtain file SMB properties and file permissions from getProperties APIs on File and Directory and download APIs on File.
- Added setProperties APIs on sync and async Directory client. Allows users to set file SMB properties and file permission.

**Breaking changes: New API design**
- Changed list responses to `PagedFlux` on async APIs and `PagedIterable` on sync APIs.
- Replaced setHttpHeaders with setProperties APIs on sync and async File client. Additionally Allows users to set file SMB properties and file permission.
- Added file smb properties and file permission parameters to create APIs on sync and async File and Directory clients.

## 12.0.0-preview.2 (2019-08-08)
Version 12.0.0-preview.2 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (August 2019 Preview) release, you can refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-08-06/java.html).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file_12.0.0-preview.2/sdk/storage/azure-storage-file/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-file_12.0.0-preview.2/sdk/storage/azure-storage-file/src/samples/java/com/azure/storage/file)
demonstrate the new API.

### Features included in `azure-storage-file`
- This is initial SDK release for storage file service.
- Packages scoped by functionality
    - `azure-storage-file` contains a `FileServiceClient`,  `FileServiceAsyncClient`, `ShareClient`, `ShareAsyncClient`, `DirectoryClient`, `DirectoryAsyncClient`, `FileClient` and `FileAsyncClient` for storage file operations.
- Client instances are scoped to storage file service.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
