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

## 12.27.0 (2024-09-17)

### Features Added
- Added ability to retrieve SAS string to sign for debugging purposes.
- Added support for service version 2024-11-04.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.50.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.4`.

## 12.27.0-beta.1 (2024-08-06)

### Features Added
- Added ability to retrieve SAS string to sign for debugging purposes.
- Added support for service version 2024-11-04.

## 12.26.0 (2024-07-18)

### Features Added
- Added support for service version 2024-08-04.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.
- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.

## 12.26.0-beta.1 (2024-06-11)

### Features Added
- Added support for service version 2024-08-04.

## 12.25.1 (2024-06-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.

## 12.25.0 (2024-05-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

### Bugs Fixed
- Fixed bug where RequestRetryOptions.tryTimeout adds delay to the client request in the synchronous http client flow.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.


## 12.24.4 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.


## 12.25.0-beta.1 (2024-04-15)

### Features Added
- Added support for service versions 2024-02-04 and 2024-05-04.

### Bugs Fixed
- Fixed bug where RequestRetryOptions.tryTimeout adds delay to the client request in the synchronous http client flow.

## 12.24.3 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 12.24.2 (2024-02-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.


## 12.24.1 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.

## 12.24.0 (2023-11-08)

### Features Added
- Added support for service versions 2023-11-03.

## 12.23.1 (2023-10-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.
- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.

## 12.24.0-beta.1 (2023-10-19)

### Features Added
- Added support for service versions 2023-11-03.

## 12.23.0 (2023-09-12)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.

## 12.22.1 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.
- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 12.23.0-beta.1 (2023-08-08)

### Features Added
- Added support for service versions 2023-05-03 and 2023-08-03.

## 12.22.0 (2023-07-11)

### Features Added
- Added `ServiceTimeoutPolicy` which allows for service level timeouts to be set on client builders through `HttpPipelinePolicy`. If the
  server timeout interval elapses before the service has finished processing the request, the service returns an error.
- Added support for the `2023-01-03` service version.

### Bugs Fixed
- Adding support for sas tokens with start and end times that only contain a date, no timestamp, ex: st=2021-06-21&se=2021-06-22.

## 12.21.2 (2023-06-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 12.22.0-beta.1 (2023-05-30)

### Features Added
- Added `ServiceTimeoutPolicy` which allows for service level timeouts to be set on client builders through `HttpPipelinePolicy`. If the
server timeout interval elapses before the service has finished processing the request, the service returns an error.
- Added support for 2023-01-03 service version.

## 12.21.1 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 12.21.0 (2023-04-13)

### Features Added
- Added support for 2022-11-02 service version.

## 12.21.0-beta.1 (2023-03-28)

### Features Added
- Added support for 2022-11-02 service version.

## 12.20.1 (2023-03-16)

### Other Changes

#### Dependency Updates

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

## 12.20.0-beta.1 (2023-02-07)

### Features Added
- Added support for 2021-12-02 service version.

## 12.19.2 (2023-01-10)

### Features Added
- Updated RequestRetryPolicy to inspect causal exceptions when determining if an error should be retried.
- Updated exception message when generating SharedKey authorization to indicate if the account or SAS key isn't properly base64 encoded.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 12.19.1 (2022-11-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 12.19.0 (2022-10-11)

### Features Added
- Added support for 2021-10-04 service version.

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 12.18.1 (2022-09-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.

## 12.19.0-beta.1 (2022-09-06)

### Features Added
- Added support for 2021-10-04 service version.

## 12.18.0 (2022-08-12)

### Features Added
- Added `ParallelTransferOptions.getProgressListener` and `ParallelTransferOptions.setProgressListener`
  that replaces deprecated `ParallelTransferOptions.getProgressReceiver` and `ParallelTransferOptions.setProgressReceiver`
- `com.azure.storage.common.ProgressReceiver` extends `com.azure.core.util.ProgressListener` for backwards compatibility.

### Bugs Fixed
- Fixed bug where connection string with SAS token would not work if the token contains leading `?`.

### Other Changes
- `com.azure.storage.common.ProgressReceiver` and `com.azure.storage.common.ProgressReporter` are deprecated
  and replaced by `com.azure.core.util.ProgressListener` and `com.azure.core.util.ProgressReporter`.

## 12.17.0 (2022-07-07)

### Features Added
- GA release for 2021-08-06 service version.

## 12.17.0-beta.1 (2022-06-15)
### Features Added
- Added support for 2021-08-06 service version.

## 12.16.1 (2022-06-08)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to version `1.29.1`.
- Upgraded `azure-core-http-netty` from `1.12.0` to version `1.12.2`.

## 12.16.0 (2022-05-25)

### Other Changes
- GA release for STG 82

## 12.15.2 (2022-05-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to version `1.28.0`.
- Upgraded `azure-core-http-netty` from `1.11.9` to version `1.12.0`.

## 12.16.0-beta.1 (2022-05-06)

### Features Added
- Added support for 2021-06-08 service version.

## 12.15.1 (2022-04-07)

### Other Changes
#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.
- Upgraded `azure-core-http-netty` from `1.11.8` to version `1.11.9`.

## 12.15.0 (2022-03-09)

### Other Changes
- GA release for STG 79, 80, 81

## 12.14.3 (2022-02-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to version `1.25.0`.
- Upgraded `azure-core-http-netty` from `1.11.6` to version `1.11.7`.

## 12.15.0-beta.3 (2022-02-09)

### Features Added
- Added support for 2021-04-10 service version.

## 12.14.2 (2022-01-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to version `1.24.1`.
- Upgraded `azure-core-http-netty` from `1.11.2` to version `1.11.6`.

## 12.15.0-beta.2 (2021-12-07)

### Features Added
- Added support for 2021-02-12 service version.

### Bugs Fixed
- Fixed a bug that would cause authenticating with a sas token to fail if the timestamps in the token were formatted differently.

## 12.14.1 (2021-11-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to version `1.22.0`.
- Upgraded `azure-core-http-netty` from `1.11.1` to version `1.11.2`.

## 12.15.0-beta.1 (2021-11-05)

### Features Added
- Added support for the 2020-12-06 service version.

## 12.14.0 (2021-10-12)

### Other Changes
#### Dependency Updates
- Updated to version `1.21.0` of `azure-core`

## 12.13.0 (2021-09-15)
- GA release

## 12.13.0-beta.1 (2021-07-28)
- Added support for the 2020-10-02 service version.
- Added support for the set immutability policy permission for Account SAS.
- Fixed bug where InputStream to Flux converter could append extra bytes at the end of payload.

## 12.12.0 (2021-06-09)
- GA release

## 12.12.0-beta.1 (2021-05-13)
- Fixed bug in Utility.convertStreamToByteBuffer where variable updates would happen incorrectly if we hit the end of stream.

## 12.11.1 (2021-05-13)
### Dependency Updates
- Updated `azure-core` to version `1.16.0`

## 12.11.0 (2021-04-29)
- Fixed concurrency issue in UploadBufferPool that caused large files to not respond.

## 12.11.0-beta.3 (2021-04-16)
- Fixed a bug where connection strings with unencoded SAS's would result in URL exceptions. 

## 12.11.0-beta.2 (2021-03-29)
- Update `azure-core` to version `1.14.1`

## 12.10.1 (2021-03-19)
- Removed a deep copy in PayloadSizeGate

## 12.11.0-beta.1 (2021-02-10)
- Added support to log retries 
- Removed a deep copy in PayloadSizeGate
- Fixed a bug that would throw if uploading using a stream that returned a number > 0 from available() after the stream had ended

## 12.10.0 (2021-01-14)
- GA release

## 12.10.0-beta.1 (2020-12-07)
- Added ability to specify timeout units in RequestRetryOptions.
- Fixed bug where query params were being parsed incorrectly if an encoded comma was the query value.
- Added a MetadataValidationPolicy to check for leading and trailing whitespace in metadata that would cause Auth failures.

## 12.9.0 (2020-11-11)
- GA release

## 12.9.0-beta.2 (2020-10-08)
- Updated `azure-core` version to `1.9.0` to pick up fixes related to listBlobs.

## 12.9.0-beta.1 (2020-10-01)
- Added a Constant that represented the default storage scope for TokenCredentials.
- Added UploadUtils.computeMd5 that computes the md5 of a flux and wraps it with the data.

## 12.8.0 (2020-08-13)
- Added support for setting tags and filterTags operations on SAS by adding to AccountSASPermissions.
- Fixed bug where FluxInputStream would throw when a ByteBuffer of length 0 was encountered.
- Added appendQueryParameter method to support adding version and snapshot support to BlobBatch setTier.
- Fixed a bug in StorageInputStream.read(byte[]) that would incorrectly return -1 if the byte array was larger than both the requested data and the chunk size

## 12.8.0-beta.1 (2020-07-07)
- Added support for the 2019-12-12 service version.
- Buffered UploadUtils now supports uploading data of long sized length.

## 12.7.0 (2020-06-12)
- Updated azure-core dependency.

## 12.7.0-beta.1 (2020-06-08)


## 12.6.1 (2020-05-06)
- Updated `azure-core` version to `1.5.0` to pickup fixes for percent encoding `UTF-8` and invalid leading bytes in a body string.

## 12.6.0 (2020-04-06)
- Added a constant for the directory metadata marker.
- Fixed bug where x-ms headers were not being word-sorted.

## 12.5.0 (2020-03-11)
- Added types that enabled buffered upload in datalake.

## 12.4.0 (2020-02-12)

## 12.3.1 (2020-02-10)
- Updated `azure-core-http-netty` to version 1.3.0

## 12.3.0 (2020-01-15)

## 12.2.0 (2020-01-08)

## 12.2.0-beta.1 (2019-12-18)
- Added generateSas methods on service clients to improve discoverability and convenience of sas. Deprecated setters of required parameters, generateSasQueryParameters methods on AccountSasSignatureValues to direct users to using the methods added on clients.

## 12.1.0 (2019-12-04)
- Upgraded to version 1.1.0 of Azure Core.

## 12.0.0 (2019-10-31)
- Removed BaseClientBuilder
- Renamed RequestRetryOptions maxTries, tryTimeout, secondaryHost, retryDelayInMs, maxRetryDelayInMs to getMaxTries, getTryTimeout, getSecondaryHost, getRetryDelayInMs, getMaxRetryDelayInMs
- Renamed IpRange to SasIpRange
- Moved AccountSasQueryParameters, AccountSasSignatureValues, BaseSasQueryParameters, SasIpRange into Sas package
- Removed SR class from public API
- Renamed SharedKeyCredential and SharedKeyCredentialPolicy to StorageSharedKeyCredential and StorageSharedKeyCredentialPolicy
- Removes many methods in Utility from public API
- Renamed AccountSasPermission getters to use has prefix
- Changed naming pattern of AccountSasService getters to use hasXAccess instead of isX (ex. hasFileAccess)

### Features included in `azure-storage-blob-cryptography`
- This package supports common functionality needed for blob storage.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
