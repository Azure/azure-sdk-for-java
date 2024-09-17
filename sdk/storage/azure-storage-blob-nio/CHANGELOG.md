# Release History

## 12.0.0-beta.26 (2024-09-17)

### Features Added
- Added support for service version 2024-11-04.

## 12.0.0-beta.25 (2024-08-06)

### Features Added
- Added support for service version 2024-11-04.

## 12.0.0-beta.24 (2024-07-18)

### Features Added
- Added support for service version 2024-08-04.

## 12.0.0-beta.23 (2024-06-11)

### Features Added
- Added support for service version 2024-08-04.

## 12.0.0-beta.22 (2024-05-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

## 12.0.0-beta.21 (2024-04-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

## 12.0.0-beta.20 (2023-11-08)

### Features Added
- Added support for 2021-12-02 service version.

### Other Changes
- Migrate test recordings to assets repo.

## 12.0.0-beta.19 (2022-05-06)

### Features Added
- Added support for 2021-06-08 service version.

## 12.0.0-beta.18 (2022-04-07)

### Other Changes
#### Dependency Updates
- Updated blob dependency to 12.16.0

## 12.0.0-beta.17 (2022-03-09)

### Features Added
- Enabled support for Files.exists()
- Enabled support for Files.walkFileTree()

### Breaking Changes
- `AzureFileSystemProvider.readAttributes()` no longer throws an IOException for virtual directories and instead returns a set of attributes that are all empty except for an `isVirtual` property set to true.

### Other Changes
- Enabling support for Files.exists() to support virtual directories required supporting virtual directories in reading file attributes. This required introducing a perf hit in the way of an extra getProps request

#### Dependency Updates

- Updated blob dependency to 12.15.0

## 12.0.0-beta.16 (2022-02-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.15.0-beta.3` to version `12.14.4`.

## 12.0.0-beta.15 (2022-02-09)

### Features Added
- Added support for 2021-04-10 service version.
- Added `AzurePath.fromBlobUrl` to help convert from a blob url to an AzurePath
- Added a configuration option `AZURE_STORAGE_SKIP_INITIAL_CONTAINER_CHECK` to skip the initial container check in cases where the authentication method used will not have necessary permissions.

### Bugs Fixed
- Fixed a bug that would prevent deleting an empty directory in the case where one directory name was a prefix of the other.


## 12.0.0-beta.14 (2022-01-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.23.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.3` to version `1.11.6`.
- Upgraded `azure-storage-blob` from `12.15.0-beta.2` to version `12.14.3`.

## 12.0.0-beta.13 (2021-12-07)

### Features Added
- Added support for 2021-02-12 service version.

## 12.0.0-beta.12 (2021-11-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to version `1.22.0`.
- Upgraded `azure-core-http-netty` from `1.11.1` to version `1.11.2`.
- Upgraded `azure-storage-blob` from `12.15.0-beta.1` to version `12.14.2.

## 12.0.0-beta.11 (2021-11-05)

### Features Added
- Added support for the 2020-12-06 service version.

### Bugs Fixed
- Fixes an off-by-one error in read() returns 0 bytes read instead of -1 (EOF) when reading at channel position == size.
- Fixes a bug where read() (and write()) do not respect initial position (and limit) of provided ByteBuffer when backed by an array

## 12.0.0-beta.10 (2021-10-12)

### Other Changes
#### Dependency Updates
- Updated `azure-storage-blob` to version `12.14.1`

## 12.0.0-beta.9 (2021-09-15)
### Other changes
- Updated `azure-storage-blob` to version `12.14.0`

## 12.0.0-beta.8 (2021-07-28)
- Added support for the 2020-10-02 service version.

## 12.0.0-beta.7 (2021-06-09)
### Dependency Updates
- Updated `azure-storage-blob` to version `12.12.0`

## 12.0.0-beta.6 (2021-04-29)
- Update `azure-storage-blob` to version `12.11.0`

## 12.0.0-beta.5 (2021-04-16)
- Fixed a bug where a file would be determined to be a directory if another file with the same prefix exists

## 12.0.0-beta.4 (2021-03-29)
- Made AzurePath.toBlobClient public
- Added support for Azurite
- Change FileSystem configuration to accept an endpoint and credential types instead of a string for the account name, key, and token

## 12.0.0-beta.3 (2021-02-10)
- Added support for FileSystemProvider.checkAccess method
- Added support for file key on AzureBasicFileAttributes and AzureBlobFileAttributes
- Added support for SeekableByteChannel
- When an operation is performed on a closed FileSystem, a ClosedFileSystemException is thrown instead of an IOException
- Adjusted the required flags for opening an outputstream

## 12.0.0-beta.2 (2020-08-13)
- Added checks to ensure file system has not been closed before operating on data

## 12.0.0-beta.1 (2020-07-17)
- Initial Release. Please see the README for more information.
