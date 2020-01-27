# Release History

## 12.0.0-beta.11 (Unreleased)
- Added ability to rename files and directories across file systems.
- Added DataLakeFileSystem.getRootDirectory methods to get the root directory in a file system.

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
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-file-datalake/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-file-datalake/src/samples/java/com/azure/storage/file/datalake) 

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
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-file-datalake/README.md) 
and 
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-file-datalake/src/samples/java/com/azure/storage/file/datalake) 
