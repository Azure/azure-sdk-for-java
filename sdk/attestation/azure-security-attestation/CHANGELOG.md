# Release History

## 1.1.26 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.1.25 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.


## 1.1.24 (2024-05-28)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.


## 1.1.23 (2024-04-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.


## 1.1.22 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `nimbus-jose-jwt` from `9.31` to version `9.37.3`.


## 1.1.21 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.


## 1.1.20 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.1.19 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.


## 1.1.18 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.


## 1.1.17 (2023-09-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.

## 1.1.16 (2023-08-22)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.

## 1.1.15 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.

## 1.1.14 (2023-06-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.


## 1.1.13 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.


## 1.1.12 (2023-04-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `nimbus-jose-jwt` from `9.22` to version `9.31`.


## 1.1.11 (2023-03-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.


## 1.1.10 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.


## 1.1.9 (2023-01-09)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.


## 1.1.8 (2022-11-10)

### Other Changes

#### Dependency Updates
- Updated `azure-core` dependency to `1.34.0`.

## 1.1.7 (2022-10-17)

### Other Changes

#### Dependency Updates
- Updated `azure-core` dependency to `1.33.0`.

## 1.1.6 (2022-09-08)

### Other Changes

#### Dependency Updates
- Updated `azure-core` dependency to `1.32.0`.

## 1.1.5 (2022-08-16)

### Other Changes

#### Dependency Updates
- Updated `azure-core` dependency to `1.31.0`.

## 1.1.4 (2022-07-07)

### Other Changes

#### Dependency Updates
- Updated `azure-core` dependency to `1.30.0`.

## 1.1.3 (2022-06-10)

### Other Changes

#### Dependency Updates
- Updated `azure-core` dependency to `1.29.1`.

## 1.1.2 (2022-05-11)

### Other Changes

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
