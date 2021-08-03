# Release History

## 1.0.0-beta.2 (Unreleased)
### Features Added

### Breaking Changes
 * Removed `buildSigningCertificatesClient` and `buildSigningCertificatesAsyncClient` replaced
   with `getAttestationSigners` and `getAttestationSignersWithResponse` on `AttestationClient` 
   and `AttestationAsyncClient`. 
 * Removed `buildMetadataConfigurationClient` and `buildMetadataConfigurationAsyncClient` API
   * `get` API becomes `getMetadataConfiguration` on `AttestationClient`
   * `getWithResponse` becomes `getMetadataConfigurationWithResponse`
   * `getAsync` becomes `getMetadataConfiguration` on `AttestationAsyncClient`.
   * `getWithResponseAsync` becomes `getMetadataConfigurationWithResponse` on `AttestationAsyncClient`.
 * Removed `InitTimeData`, `RunTimeData`, and `DataType` types
   * All functionality incorporated into `AttestOpenEnclaveRequest` and `AttestSgxEnclaveRequest`
 * Changed function signature for `AttestOpenEnclaveRequest` and `AttestSgxEnclaveRequest`.
   * Changed `setInitTimeData` to accept a `byte[]` instead of a `InitTimeData`. 
   `setInitTimeData` sets a binary `InitTime` data value.
   * Added `setInitTimeJson` which takes a `byte[]` and sets the
    `InitTime` data as JSON.
   * Similarly, `setRunTimeData` was changed to accept a `byte[]`.
   * And `setRunTimeJson` was added to set the `RunTimeData` as JSON.
 * `attestSgxEnclave` and `attestOpenEnclave` return an `AttestationResponse` type instead of
a `Response` type to get access to the `AttestationToken` returned from the attestation service.
 

### Bugs Fixed
* Attestation tests now all pass when run in Live mode.

### Other Changes

## 1.0.0-beta.1 (2021-01-28)

- Initial release. Please see the README and wiki for information on the new design.
