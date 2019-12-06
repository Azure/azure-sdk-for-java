# Release History

## Version XX.X.X (XXXX-XX-XX)
- Added generateSas methods on service clients to improve discoverability and convenience of sas. Deprecated setters of required parameters, generateSasQueryParameters methods on AccountSasSignatureValues to direct users to using the methods added on clients.

## Version 12.1.0 (2019-12-04)
- Upgraded to version 1.1.0 of Azure Core.

## Version 12.0.0 (2019-10-31)
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
  
