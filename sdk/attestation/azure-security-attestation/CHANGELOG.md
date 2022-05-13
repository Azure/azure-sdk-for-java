# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.2 (2022-05-11)

#### Dependency Updates
- Updated `azure-core` dependency to `1.28.0`.

## 1.1.1 (2022-04-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.26.0` to version `1.27.0`.

## 1.1.0 (2022-03-11)

### Features Added
- Added interfaces from `com.azure.core.client.traits` to `AttestationClientBuilder` and `AttestationAdministrationClientBuilder`.
- Added `retryOptions()` to `AttestationClientBuilder` and `AttestationAdministrationClientBuilder`.

### Other Changes

#### Dependency Updates
- Updated `azure-core` to `1.26.0`.

## 1.0.0 (2022-02-08)
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
    .fromQuote(decodedSgxEnclaveReport)
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
will instantiate them directly.
 * Renamed `buildAttestationClient` to `buildClient` and `buildAsyncAttestationClient` to `buildAsyncClient` to match API
design guidelines.
 * Removed `buildPolicyClient`, `buildPolicyAsyncClient`, `buildPolicyCertificatesClient` and `buildPolicyCertificatesAsyncClient` methods 
on the `AttestationClientBuilder` class and implemented a new `AttestationAdministrationClient` class which contains the administrative APIs.
 * Removed `buildPolicyCertificatesClient` and `buildPolicyCertificatesAsyncClient`, and `PolicyCertificatesClient` and `PolicyCertificatesAsyncClient` replacing the functionality 
with the  `listPolicyManagementCertificates`, `addPolicyManagementCertificate` and `removePolicyManagementCertificate` APIs on the `AttestationAdministrationClient` object.
 * Removed `JsonWebKey`, `JsonWebKeySet`, `PolicyCertificatesModificationResult`, `PolicyCertificatesModifyResponse`, and `CertificatesResponse` objects 
because they are no longer a part of the public API surface.
 * Refactored `AttestationSigningKey` class to require certificate and signing key parameters in constructor.
 * listAttestationSigners now returns an `AttestationSignersCollection` object instead of a raw `List<AttestationSigner>`

### Bugs Fixed
* Attestation tests now all pass when run in Live mode.

## 1.0.0-beta.1 (2021-01-28)

- Initial release. Please see the README and wiki for information on the new design.
