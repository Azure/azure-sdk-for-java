# Release History

## 12.18.1 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.25.0` to version `12.26.0-beta.1`.
- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 12.18.0 (2023-11-08)

### Features Added
- Added support for service versions 2023-11-03.
- Added support for DataLakeAudience.

## 12.17.1 (2023-10-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-storage-blob` from `12.24.0` to version `12.24.1`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.

## 12.18.0-beta.1 (2023-10-19)

### Features Added
- Added support for service versions 2023-11-03.
- Added support for DataLakeAudience.

## 12.17.0 (2023-09-12)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.
- Added `DataLakeDirectoryClient.deleteRecursively()` which allows users to delete a directory and all of its contents recursively.
- Added support for paginated directory delete when using AAD authentication. Note that this feature only applies to HNS storage accounts and when using token authentication.

## 12.16.1 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.23.0` to version `12.23.1`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 12.17.0-beta.1 (2023-08-08)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.
- Added `DataLakeDirectoryClient.deleteRecursively()` which allows users to delete a directory and all of its contents recursively. 
- Added support for paginated directory delete when using AAD authentication. Note that this feature only applies to HNS storage accounts and when using token authentication.

## 12.16.0 (2023-07-11)

### Features Added
- Added `DataLakeFileClient.getOutputStream()` which allows users to open an `OutputStream` on the file and write data
  to it using `ParallelTransferOptions`.
- Added support for the `2023-01-03` service version.
- Added owner, group, and permissions properties to `PathProperties`.
- Added encryptionContext property to `FileParallelUploadOptions`.

### Bugs Fixed
- Fixed a bug that would cause authentication failures when trying to rename a path object while authenticating with a sas token that contained a leading question mark.

## 12.15.3 (2023-06-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-storage-blob` from `12.22.1` to version `12.22.2`.

## 12.16.0-beta.1 (2023-05-30)

### Features Added
- Added `DataLakeFileClient.getOutputStream()` which allows users to open an `OutputStream` on the file and write data 
to it using `ParallelTransferOptions`.
- Added support for 2023-01-03 service version.
- Added owner, group, and permissions properties to `PathProperties`.
- Added encryptionContext property to `FileParallelUploadOptions`.

## 12.15.2 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 12.15.1 (2023-05-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.22.0` to version `12.22.1`.

## 12.15.0 (2023-04-13)

### Features Added
- Added support for 2022-11-02 service version.
- Adding support for Encryption Context on DataLake files, a string value that can be passed to allow users to generate
  file-level encryption keys. This value is returned on `DataLakeFileClient.getProperties()`, `DataLakeFileClient.readWithResponse()`,
  and `DataLakeFileClient.listPaths()`.

## 12.15.0-beta.1 (2023-03-28)

### Features Added
- Added support for 2022-11-02 service version.
- Adding support for Encryption Context on DataLake files, a string value that can be passed to allow users to generate 
file-level encryption keys. This value is returned on `DataLakeFileClient.getProperties()`, `DataLakeFileClient.readWithResponse()`,
and `DataLakeFileClient.listPaths()`.

## 12.14.1 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.21.0` to version `12.21.1`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 12.14.0 (2023-02-21)

### Features Added
- Added `FileReadHeaders.getCreationTime()` and `FileReadHeaders.setCreationTime(OffsetDateTime)` to access the x-ms-creation-time property.
- Added support for 2021-12-02 service version.
- Added support for leasing operations on `DataLakeFileClient.appendWithResponse()` and `DataLakeFileClient.flushWithResponse()`.

### Other Changes
- Deprecated the always-failing API `DataLakeFileClient.flush(long)` to avoid confusion regarding overwrite semantics in DataLake.

## 12.13.3 (2023-02-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-storage-blob` from `12.20.2` to version `12.20.3`.

## 12.14.0-beta.1 (2023-02-07)

### Features Added
- Added support for 2021-12-02 service version.
- Added support for leasing operations on `DataLakeFileClient.appendWithResponse()` and `DataLakeFileClient.flushWithResponse()`.

### Other Changes
- Deprecated the always-failing API `DataLakeFileClient.flush(long)` to avoid confusion regarding overwrite semantics in DataLake.

## 12.13.2 (2023-01-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.
- Upgraded `azure-storage-blob` from `12.20.1` to version `12.20.2`.

## 12.13.1 (2022-11-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.
- Upgraded `azure-storage-blob` from `12.20.0` to version `12.20.1`.

## 12.13.0 (2022-10-11)

### Features Added
- Added support for 2021-10-04 service version.
- Added support for flush parameter to `DataLakeFileClient.appendWithResponse()`
- Added support for encryption scopes.
- Added support for encryption scope SAS.
- Added new method `DataLakeFileSystemClient.exists()` to determine whether file system exists.
- Added `DataLakeServiceClientBuilder.connectionString()` to allow connection string to be set on the client builder.
- Added `DataLakeFileClient.uploadFromFileWithResponse()` which returns a response of `PathItem` upon completion.

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.
- Upgraded `azure-storage-blob` from `12.19.1` to version `12.20.0`.

## 12.12.1 (2022-09-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.
- Upgraded `azure-storage-blob` from `12.19.0` to version `12.19.1`.

## 12.13.0-beta.1 (2022-09-06)

### Features Added
- Added support for 2021-10-04 service version.
- Added support for flush parameter to `DataLakeFileClient.appendWithResponse()`
- Added support for encryption scopes.
- Added support for encryption scope SAS.
- Added new method `DataLakeFileSystemClient.exists()` to determine whether file system exists.
- Added `DataLakeServiceClientBuilder.connectionString()` to allow connection string to be set on the client builder.
- Added `DataLakeFileClient.uploadFromFileWithResponse()` which returns a response of `PathItem` upon completion.

## 12.12.0 (2022-08-12)

### Features Added
- Added upload and append methods on DataLakeFileClient and DataLakeFileAsyncClient that support BinaryData.

## 12.11.0 (2022-07-07)

### Features Added
- GA release for 2021-08-06 service version.

## 12.11.0-beta.1 (2022-06-15)

### Features Added
- Added support for 2021-08-06 service version.
- Added ability to set permission, umask, owner, group, ACL, lease, and expiry time to the following:
  DataLakePathClient.createWithResponse, DataLakeDirectoryClient.createFileWithResponse,
  DataLakeDirectoryClient.createSubdirectoryWithResponse DataLakeFile.createWithResponse,
  DataLakeFileSystemClient.createFileWithResponse, and DataLakeFileSystemClient.createDirectoryWithResponse.

## 12.10.1 (2022-06-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.
- Upgraded `azure-storage-blob` from `12.17.0` to version `12.17.1`.

## 12.10.0 (2022-05-25)

### Other Changes
- GA release for STG 82

## 12.9.1 (2022-05-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.
- Upgraded `azure-storage-blob` from `12.16.0` to version `12.16.1`.

## 12.10.0-beta.1 (2022-05-06)

### Features Added
- Added support for 2021-06-08 service version.
- Added support for Customer Provided Keys.
- Added creationTime and expiryTime fields to `PathItem`.

## 12.9.0 (2022-04-07)

### Bugs Fixed
- Fixed a bug that would cause auth failures when trying to rename a path object while authenticating with a sas.
- Fixed a bug where `uploadFromFile(String, boolean)` used a different size to determine whether file existence should
  be checked before using chunked upload than the size used to triggered chunked upload.
### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.
- Upgraded `azure-storage-blob` from `12.15.0` to version `12.16.0`.

## 12.8.0 (2022-03-09)

### Other Changes
- GA release for STG 79, 80, 81

## 12.7.4 (2022-02-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.
- Upgraded `azure-storage-blob` from `12.14.3` to version `12.14.4`.

## 12.8.0-beta.3 (2022-02-09)

### Features Added
- Added support for 2021-04-10 service version.

### Bugs Fixed
- Fixed a bug in builders that would cause fileSystem or path name to be erased if specified before the connection string.

## 12.7.3 (2022-01-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.2` to version `1.11.6`.
- Upgraded `azure-storage-blob` from `12.14.2` to version `12.14.3`.

## 12.8.0-beta.2 (2021-12-07)

### Features Added
- Added support for 2021-02-12 service version.
- Added support for listing system containers.

### Bugs Fixed
- Fixed a bug in DataLakeFileClient.flush() in which the logic of the overwrite parameter was reversed.
- Fixed a bug that would cause authenticating with a sas token to fail if the timestamps in the token were formatted differently.

## 12.7.2 (2021-11-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to version `1.22.0`.
- Upgraded `azure-core-http-netty` from `1.11.1` to version `1.11.2`.
- Upgraded `azure-storage-blob` from `12.14.1` to version `12.14.2`.

## 12.8.0-beta.1 (2021-11-05)

### Features Added
- Added support for the 2020-12-06 service version.

## 12.7.1 (2021-10-12)

### Bugs Fixed
- Fixed bug where composite BlobServiceException wasn't translated into DataLakeServiceException.

### Other Changes
#### Dependency Updates
- Updated to version `12.14.1` of `azure-storage-blob`
- Updated to version `1.21.0` of `azure-core`

## 12.7.0 (2021-09-15)
- GA release

## 12.7.0-beta.1 (2021-07-28)
- Added support for openInputStream to sync data lake file clients
- Added support for the 2020-10-02 service version.
- Added support to specify Parquet Input Serialization when querying a file.
- Updated DownloadRetryOptions.maxRetryRequests to default downloads to retry 5 times.

## 12.6.0 (2021-06-09)
- GA release

## 12.6.0-beta.1 (2021-05-13)
- Added support for the 2020-08-04 service version.
- Added support to undelete a file or directory
- Added support to list deletedPaths
- Added support to get/set service properties
- Deprecated support to undelete a file system to a new name. 

## 12.5.1 (2021-05-13)
### Dependency Updates
- Updated `azure-storage-blob` to version `12.11.1`
- Updated `azure-core` to version `1.16.0`

## 12.5.0 (2021-04-29)
- Fixed a bug where large files would not respond when the upload method was called. 
- DataLakeLeaseClient now remembers the Lease ID after a lease change.

## 12.5.0-beta.3 (2021-04-16)
- Updated `azure-storage-blob` version to `12.11.0-beta.3` to pickup fixes for blob output stream.

## 12.5.0-beta.2 (2021-03-29)
- Fixed a bug where files/directories in root directories could not be renamed.
- Fixed a bug where more data would be buffered in buffered upload than expected due to Reactor's concatMap operator.

## 12.4.1 (2021-03-19)
- Updated `azure-storage-blob` version to `12.10.1` to pickup fixes for blob output stream.

## 12.5.0-beta.1 (2021-02-10)
- Added support for the 2020-06-12 service version. 
- Added support to undelete a file system. 

## 12.4.0 (2021-01-14)
- GA release
- Fixed bug where getFileClient and getSubDirectoryClient on DirectoryClient would throw IllegalArgumentException if either resource had special characters.

## 12.4.0-beta.1 (2020-12-07)
- Added support to list paths on a directory.
- Exposed ClientOptions on all client builders, allowing users to set a custom application id and custom headers.
- Fixed a bug where the error message would not be displayed the exception message of a HEAD request.
- Added a MetadataValidationPolicy to check for leading and trailing whitespace in metadata that would cause Auth failures.

## 12.3.0 (2020-11-11)
- Added support to specify whether or not a pipeline policy should be added per call or per retry.
- Modified DataLakeAclChangeFailedException to extend AzureException
- Fixed a bug where the endpoint would be improperly converted if the account name contained the word dfs.

## 12.3.0-beta.1 (2020-10-01)
- Added support for the 2020-02-10 service version.
- Added support for setting, modifying, and removing ACLs recursively.
- Added support to schedule file expiration. 
- Added support to specify Arrow Output Serialization when querying a file. 
- Added support to generate directory SAS and added support to specify additional user ids and correlation ids for user delegation SAS.
- Fixed a bug where users could not download more than 5000MB of data in one shot in the readToFile API.
- Fixed a bug where the TokenCredential scope would be incorrect for custom URLs.
- Added support to upload data to a file from an InputStream.
- Added support to specify permissions and umask when uploading a file. 
- Fixed a bug where an empty string would be sent with the x-ms-properties header when metadata was null or empty.
- Fixed a bug where a custom application id in HttpLogOptions would not be added to the User Agent String.

## 12.2.0 (2020-08-13)
- Fixed bug where Query Input Stream would throw when a ByteBuffer of length 0 was encountered.

## 12.2.0-beta.1 (2020-07-07)
- Added support for the 2019-12-12 service version.
- Added support to query a file. 
- Added support to increase the maximum size of data that can be sent via an append.
- Fixed a bug that would cause buffered upload to always put an empty file before uploading actual data.

## 12.1.2 (2020-06-12)
- Updated azure-storage-common and azure-core dependencies.

## 12.1.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.1.0 (2020-04-06)
- Fixed a NPE caused due to deserializing a non existent lastModifiedTime.
- Added an isDirectory property to PathProperties.
- Fixed DataLakeFileSystemClient.createFile/createDirectory, DataLakeDirectoryClient.createFile/createSubdirectory to not overwrite by default
- Added overloads to DataLakeFileSystemClient.createFile/createDirectory, DataLakeDirectoryClient.createFile/createSubdirectory to allow overwrite behavior.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.
- Fixed a bug where rename would not work with Url encoded destinations.

## 12.0.1 (2020-03-11)
- GA release.
- Fixed bug that caused rename to fail on paths that are url encoded.
- Mapped StorageErrorException and BlobStorageException to DataLakeStorageException on DataLakeServiceClient.listFileSystems
- Removed DataLakeFileSystem.getRootDirectory methods to get the root directory in a file system.

## 12.0.0-beta.12 (2020-02-12)
- Added ability to rename files and directories across file systems.
- Added DataLakeFileSystem.getRootDirectory methods to get the root directory in a file system.
- Fixed bug which caused NullPointerException when creating a PathItem.

## 12.0.0-beta.11 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-blob` to version 12.3.1

## 12.0.0-beta.10 (2020-01-15)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-datalake_12.0.0-beta.10/sdk/storage/azure-storage-file-datalake/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-datalake_12.0.0-beta.10/sdk/storage/azure-storage-file-datalake/src/samples/java/com/azure/storage/file/datalake)

- Upgraded to version 12.3.0 of Azure Storage Blob.

## 12.0.0-beta.9 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-datalake_12.0.0-beta.9/sdk/storage/azure-storage-file-datalake/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-file-datalake_12.0.0-beta.9/sdk/storage/azure-storage-file-datalake/src/samples/java/com/azure/storage/file/datalake)

## 12.0.0-beta.8 (2019-12-18)
- Added SAS generation methods on clients to improve discoverability and convenience of sas.
- Mapped StorageErrorException and BlobStorageException to DataLakeStorageException.
- Added support for exists method on FileClients and DirectoryClients
- Added support for no overwrite by default on min create method on FileClients and DirectoryClients and flush method on FileClients

## 12.0.0-beta.7 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-datalake/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-file-datalake/src/samples/java/com/azure/storage/file/datalake)

- Fixed bug in ClientBuilders that prevented OAuth from functioning.
- Added a check in ClientBuilders to enforce HTTPS for bearer token authentication.
- Added support for URl encoding.
- Moved LeaseClients to the specialized package.
- Split setAccessControl API into setPermissions and setAccessControlList.
- Renamed setters and getters in PathPermissions and RolePermissions to be more detailed.
- Fixed camel-casing of the word SubDirectory.
- Upgraded to version 1.1.0 of Azure Core.
- Upgraded to version 12.1.0 of Azure Storage Blob.

## 12.0.0-preview.5
- Initial Release. Please see the README and wiki for information on the new design.
- Support for Azure Data Lake Storage REST APIs.
- Support for DataLakeServiceClient: create file system, delete file system, get file systems, and get user delegation key
- Support for DataLakeLeaseClient: acquire, renew, release, change, and break lease
- Support for DataLakeFileSystemClient: create, delete, get properties, set metadata, get paths, create directory, delete directory, create file, delete file
- Support for DataLakeDirectoryClient: create, delete, rename, get properties, get access control, set metadata, set properties, set access control, create file, delete file, create sub-directory, delete sub-directory
- Support for DataLakeFileClient: create, delete, rename, get properties, get access control, set metadata, set properties, set access control, append, flush, read

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-datalake/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-file-datalake/src/samples/java/com/azure/storage/file/datalake)

