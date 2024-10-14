# Release History

## 12.28.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 12.27.1 (2024-10-08)

### Bugs Fixed
- Fixed a bug where downloadToFile and openInputStream was throwing an InvalidRange exception if the target file size was a multiple of the
  authenticated region length.

#### Dependency Updates
- Upgraded `azure-storage-blob` from `12.28.0` to version `12.28.1`.

## 12.27.0 (2024-09-17)

### Features Added
- Added a new `EncryptionVersion.V2_1` that allows encrypted blobs to be uploaded using a configurable authenticated region length.
- Added configuration to allow encrypted blobs to be uploaded using a configurable authenticated region length via 
  `BlobClientSideEncryptionOptions`. The region length can be configured to range between 16 bytes to 1GB. The region 
  length can be set via `BlobClientSideEncryptionOptions.setAuthenticatedRegionDataLengthInBytes(long authenticatedRegionDataLength)`. 
  Note: This change only applies to `EncryptionVersion.V2_1`. Also, only applies to upload operations, this does not directly 
  change the authenticated region length used to download and decrypt blobs.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.50.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.4`.
- Upgraded `azure-storage-blob` from `12.27.0` to version `12.28.0`.

## 12.27.0-beta.1 (2024-08-06)

### Features Added
- Added support for service version 2024-11-04.

## 12.26.0 (2024-07-18)

### Features Added
- Added support for service version 2024-08-04.

### Breaking Changes
- When creating a `EncryptedBlobClient` via EncryptedBlobClientBuilder, the blob name will be stored exactly as passed 
  in and will not be URL-encoded. For example, if blob name is "test%25test" and is created by calling
  `EncryptedBlobClientBuilder.blobName("test%25test")` along with other required parameters, 
  `EncryptedBlobClient.getBlobName()` will return "test%25test" and the blob's url will result in 
  “https://account.blob.core.windows.net/container/test%25%25test”.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.
- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-storage-blob` from `12.26.1` to version `12.27.0`.

## 12.26.0-beta.1 (2024-06-11)

### Features Added
- Added support for service version 2024-08-04.

## 12.25.1 (2024-06-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.
- Upgraded `azure-storage-blob` from `12.26.0` to version `12.26.1`.

## 12.25.0 (2024-05-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.26.0-beta.2` to version `12.26.0`.
- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 12.24.4 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.25.3` to version `12.26.0-beta.2`.
- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 12.25.0-beta.1 (2024-04-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

## 12.24.3 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.
- Upgraded `azure-storage-blob` from `12.25.2` to version `12.25.3`.


## 12.24.2 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-storage-blob` from `12.25.1` to version `12.25.2`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.


## 12.24.1 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.25.0` to version `12.25.1`.
- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.

## 12.24.0 (2023-11-08)

### Features Added
- Added support for service versions 2023-11-03.

## 12.23.1 (2023-10-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-storage-blob` from `12.24.0` to version `12.24.1`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.

## 12.24.0-beta.1 (2023-10-19)

### Features Added
- Added support for service versions 2023-11-03.

## 12.23.0 (2023-09-12)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.

### Bugs Fixed
- Fixed bug where a blob's contents could not be decrypted if the metadata's key set on the blob had any uppercase letters. The metadata key is now able to handle any casing.

## 12.22.1 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.23.0` to version `12.23.1`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 12.23.0-beta.1 (2023-08-08)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.

## 12.22.0 (2023-07-11)

### Features Added
- Added support for the `2023-01-03` service version.

### Other Changes
- Migrate test recordings to assets repo.

## 12.21.3 (2023-06-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.
- Upgraded `azure-storage-blob` from `12.22.1` to version `12.22.2`.

## 12.22.0-beta.1 (2023-05-30)

### Features Added
- Added support for 2023-01-03 service version.

## 12.21.2 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 12.21.1 (2023-05-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.22.0` to version `12.22.1`.

## 12.21.0 (2023-04-13)

### Features Added
- Added support for 2022-11-02 service version.

## 12.21.0-beta.1 (2023-03-28)

### Features Added
- Added support for 2022-11-02 service version.

## 12.20.1 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-storage-blob` from `12.21.0` to version `12.21.1`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 12.20.0 (2023-02-21)

### Features Added
- Added support for 2021-12-02 service version.

## 12.19.3 (2023-02-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-storage-blob` from `12.20.2` to version `12.20.3`.

## 12.20.0-beta.1 (2023-02-07)

### Features Added
- Added support for 2021-12-02 service version.

## 12.19.2 (2023-01-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.
- Upgraded `azure-storage-blob` from `12.20.1` to version `12.20.2`.

## 12.19.1 (2022-11-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.
- Upgraded `azure-storage-blob` from `12.20.0` to version `12.20.1`.
- 
## 12.19.0 (2022-10-11)

### Features Added
- Added support for 2021-10-04 service version.

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.
- Upgraded `azure-storage-blob` from `12.19.1` to version `12.20.0`.

## 12.18.1 (2022-09-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.
- Upgraded `azure-storage-blob` from `12.19.0` to version `12.19.1`.

## 12.19.0-beta.1 (2022-09-06)

### Features Added
- Added support for 2021-10-04 service version.

## 12.18.0 (2022-08-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to version `1.31.0`.
- Upgraded `azure-core-http-netty` from `1.12.3` to version `1.12.4`.
- Upgraded `azure-storage-blob` from `12.18.0` to version `12.19.0`.

## 12.17.0 (2022-07-07)

### Features Added
- GA release for 2021-08-06 service version.
- GA release for encryption protocol version 2.

## 12.17.0-beta.1 (2022-06-15)

### Features Added
- Added support for encryption protocol version 2, using AES/GCM/NoPadding. 
- Added support for 2021-08-06 service version.

## 12.16.1 (2022-06-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.
- Upgraded `azure-storage-blob` from `12.17.0` to version `12.17.1`.

## 12.16.0 (2022-05-25)

### Other Changes
- GA release for STG 82

## 12.15.2 (2022-05-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.
- Upgraded `azure-storage-blob` from `12.16.0` to version `12.16.1`.

## 12.16.0-beta.1 (2022-05-06)

### Features Added
- Added support for 2021-06-08 service version.

## 12.15.1 (2022-04-07)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.
- Upgraded `azure-storage-blob` from `12.15.0` to version `12.16.0`.

## 12.15.0 (2022-03-09)

### Other Changes
- GA release for STG 79, 80, 81

## 12.14.4 (2022-02-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.
- Upgraded `azure-storage-blob` from `12.14.3` to version `12.14.4`.

## 12.15.0-beta.3 (2022-02-09)

### Features Added
- Added support for 2021-04-10 service version.

### Bugs Fixed
- Fixed a bug in builders that would cause container or blobName to be erased if specified before the connection string.

## 12.14.3 (2022-01-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.2` to version `1.11.6`.
- Upgraded `azure-storage-blob` from `12.14.2` to version `12.14.3`.

## 12.15.0-beta.2 (2021-12-07)

### Features Added
- Added support for 2021-02-12 service version.

## 12.14.2 (2021-11-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to version `1.22.0`.
- Upgraded `azure-core-http-netty` from `1.11.1` to version `1.11.2`.
- Upgraded `azure-storage-blob` from `12.14.1` to version `12.14.2`.

## 12.15.0-beta.1 (2021-11-05)

### Features Added
- Added support for the 2020-12-06 service version.

## 12.14.1 (2021-10-12)

### Other Changes
#### Dependency Updates
- Updated to version `12.14.1` of `azure-storage-blob`
- Updated to version `1.21.0` of `azure-core`

## 12.14.0 (2021-09-15)
- GA release

## 12.14.0-beta.1 (2021-07-28)
- Added support for the 2020-10-02 service version.

## 12.13.0 (2021-07-22)
- Added support to get a blob client that uses an encryption scope and customer provided key. 

## 12.12.0 (2021-06-09)
- GA release

## 12.12.0-beta.1 (2021-05-13)
- Added support for specifying an encryption scope.
- Added support for the 2020-08-04 service version.

## 12.11.1 (2021-05-13)
### Dependency Updates
- Updated `azure-storage-blob` to version `12.11.1`
- Updated `azure-core` to version `1.16.0`

## 12.11.0 (2021-04-29)
- GA release

## 12.11.0-beta.3 (2021-04-16)
- Update `azure-storage-blob` to version `12.11.0-beta.3`

## 12.11.0-beta.2 (2021-03-29)
- Update `azure-storage-blob` to version `12.11.0-beta.2`

## 12.11.0-beta.1 (2021-02-10)
- Updated azure-storage-common and azure-storage-blob dependencies to add support for the 2020-06-12 service version. 

## 12.10.0 (2021-01-14)
- GA release

## 12.10.0-beta.1 (2020-12-07)
- Exposed ClientOptions on all client builders, allowing users to set a custom application id and custom headers.
- Added a MetadataValidationPolicy to check for leading and trailing whitespace in metadata that would cause Auth failures.

## 12.9.0 (2020-11-11)
- Added support to specify whether or not a pipeline policy should be added per call or per retry.

## 12.9.0-beta.1 (2020-10-01)
- Added support to set BlobParallelUploadOptions.computeMd5 so the service can perform an md5 verification.
- Added support to specify 'requiresEncryption' on the EncryptedBlobClientBuilder to specify whether or not to enforce that the blob is encrypted on download.
- Fixed a bug where the TokenCredential scope would be incorrect for custom URLs.
- Fixed a bug where a custom application id in HttpLogOptions would not be added to the User Agent String.

## 12.8.0 (2020-08-13)
- GA release for 2019-12-12 service version

## 12.8.0-beta.1 (2020-07-07)
- Added support for setting blob tags on upload. 

## 12.7.0 (2020-06-12)
- Updated azure-storage-common and azure-core dependencies.

## 12.6.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.6.0 (2020-04-06)
- It is now possible to specify a key/keyResolver after they specify a pipeline/client on the builder.
- The builder will now throw if a pipeline/client was already configured for decryption as the encryption info may conflict.
- Fixed a bug where the Date header wouldn't be updated with a new value on request retry.

## 12.5.0 (2020-03-11)
- Fixed a bug where the EncryptedBlockClientBuilder.pipeline method would not allow the client to support decryption.
- Added support for specifying a customer provided key.
- Fixed a bug that would cause EncryptedBlobClient.upload(InputStream, long) to not encrypt the data.
- Changed getAppendBlobClient, getBlockBlobClient, and getPageBlobClient on EncryptedBlobClient to throw as working with such clients is not supported.

## 12.4.0 (2020-02-12)
- Added support for upload via OutputStream by adding EncryptedBlobClient.getOutputStream methods

## 12.3.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0
- Update `azure-storage-blob` to version 12.3.1

## 12.3.0 (2020-01-15)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.3.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-blob-cryptography_12.3.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/specialized/cryptography)

- Upgraded to version 12.3.0 of Azure Storage Blob.
- Added .blobClient(BlobClient) and .blobAsyncClient(BlobAsyncClient) methods on EncryptedBlobClientBuilder to create an EncryptedBlobClient from a BlobClient.

## 12.2.0 (2020-01-08)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.2.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-blob-cryptography_12.2.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/specialized/cryptography)

- Upgraded to version 12.2.0 of Azure Storage Blob.

## 12.1.0 (2019-12-04)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-storage-blob-cryptography_12.1.0/sdk/storage/azure-storage-blob-cryptography/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-storage-blob-cryptography_12.1.0/sdk/storage/azure-storage-blob-cryptography/src/samples/java/com/azure/storage/blob/specialized/cryptography)

- Upgraded to version 12.1.0 of Azure Storage Blob.
- Upgraded to version 1.1.0 of Azure Core.
- Added a check in EncryptedBlobClientBuilder to enforce HTTPS for bearer token authentication.

## 12.0.0 (2019-10-31)
- Removed EncryptedBlobClientBuilder inheritance of BaseBlobClientBuilder

### Features included in `azure-storage-blob-cryptography`
- This package supports client side encryption for blob storage.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

