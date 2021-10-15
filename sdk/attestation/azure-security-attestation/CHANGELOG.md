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
 * Renamed `AttestOpenEnclaveRequest` to `AttestOpenEnclaveOptions` and `AttestSgxEnclaveRequest` to `AttestSgxEnclaveOptions`.
 * Instead of being directly instantiated, `AttestOpenEnclaveOptions` and `AttestSgxEnclaveOptions` are instantiated via a
factory method:
```java
AttestSgxEnclaveOptions options = AttestSgxEnclaveOptions
    .fromQuote(decodedOpenEnclaveReport)
    .setRunTimeData(new byte[] { 1, 2, 3, 4, 5});
```
or
```java
AttestOpenEnclaveOptions options = AttestOpenEnclaveOptions
    .fromQuote(decodedOpenEnclaveReport)
    .setRunTimeJson("{ \"xxx\": 123 }".getBytes(StandardCharsets.UTF_8))
```

 * `attestSgxEnclave` and `attestOpenEnclave` return an `AttestationResponse` type instead of
a `Response` type to get access to the `AttestationToken` returned from the attestation service.
 * Converted the `AttestationToken` and `AttestationSigner` types to interfaces since there are no scenarios where customers
will instantiate them directly (`AttestationToken` will be instantiated via the `AttestationPolicyToken` class which will 
be introduced later.)
 * Renamed `buildAttestationClient` to `buildClient` and `buildAsyncAttestationClient` to `buildAsyncClient` to match API
design guidelines.

### Bugs Fixed
* Attestation tests now all pass when run in Live mode.

### Other Changes

## 1.0.0-beta.1 (2021-01-28)

- Initial release. Please see the README and wiki for information on the new design.
