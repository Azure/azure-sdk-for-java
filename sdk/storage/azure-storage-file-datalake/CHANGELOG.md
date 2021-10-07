# Release History

## 12.8.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed
- Fixed bug where composite BlobServiceException wasn't translated into DataLakeServiceException.

### Other Changes

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

