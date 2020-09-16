# Release History

## 12.9.0-beta.1 (Unreleased)


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
- Renamed RequestRetryOptions maxTries, tryTimeout, secondaryHost, retryDelayInMs, maxRetryDelayInMs to getMaxTries, getTryTimeout, getSecondaryHosy, getRetryDelayInMs, getMaxRetryDelayInMs
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
