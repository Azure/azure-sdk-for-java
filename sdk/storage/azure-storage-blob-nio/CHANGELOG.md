# Release History

## 12.0.0-beta.13 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
