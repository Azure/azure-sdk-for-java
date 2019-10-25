# Release History
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
  
