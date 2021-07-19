# Release History

## 1.0.0-beta.2 (Unreleased)
### Features Added

### Breaking Changes
 * Removed `buildMetadataConfigurationClient` and `buildMetadataConfigurationAsyncClient` API
   * `get` API becomes `getMetadataConfiguration` on `AttestationClient`
   * `getWithResponse` becomes `getMetadataConfigurationWithResponse`
   * `getAsync` becomes `getMetadataConfiguration` on `AttestationAsyncClient`.
   * `getWithResponseAsync` becomes `getMetadataConfigurationWithResponse` on `AttestationAsyncClient`.  
 * Split `AttestationClientBuilder` into sync and async builders:
   * `AttestationClientBuilder` and `AttestationAsyncClientBuilder`. 
 * Moved `buildAttestationAsyncClient` and `buildSigningCertificatesAsyncClient`
 methods to `AttestationAsyncClientBuilder`
   

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2021-01-28)

- Initial release. Please see the README and wiki for information on the new design.
