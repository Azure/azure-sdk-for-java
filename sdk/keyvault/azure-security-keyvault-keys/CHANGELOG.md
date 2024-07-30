# Release History

## 4.9.0-beta.1 (2024-07-29)

### Features Added
- Added a new configuration flag to cryptography clients to defer all cryptographic operations to the Key Vault service. ([#40384](https://github.com/Azure/azure-sdk-for-java/pull/40384))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.
- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.

## 4.8.6 (2024-07-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-json` from `1.1.0` to version `1.2.0`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.

## 4.8.5 (2024-06-27)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.15.0` to version `1.15.1`.

## 4.8.4 (2024-05-13)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.0`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.0`.

## 4.8.3 (2024-04-30)

### Other Changes

- No changes but only upgrade version to fix Microsoft Doc.

## 4.8.2 (2024-04-09)

### Bugs Fixed
- Fixed issue where `hsmPlatform` was not being set in `KeyProperties`. ([#39537](https://github.com/Azure/azure-sdk-for-java/pull/39537))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to version `1.48.0`.
- Upgraded `azure-core-http-netty` from `1.14.1` to version `1.14.2`.

## 4.8.1 (2024-03-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to version `1.47.0`.
- Upgraded `azure-core-http-netty` from `1.14.0` to version `1.14.1`.


## 4.8.0 (2024-02-22)
Changes when compared to the last stable release (`4.7.3`) include:

### Features Added
- Added support for service version `7.5`.
- Added `KeyProperties.getHsmPlatform()` to get the underlying HSM platform that a key was generated with.

- Added fallback logic to use service-side cryptography if a key cannot be retrieved for local operations. ([#38334](https://github.com/Azure/azure-sdk-for-java/pull/38334))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.
- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.

## 4.7.3 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.

## 4.7.2 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.

## 4.8.0-beta.1 (2023-11-09)

### Features Added
- Added support for service version `7.5-preview.1`.
- Added `KeyProperties.getHsmPlatform()` to get the underlying HSM platform that a key was generated with.

#### Dependency Updates
- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.

## 4.7.1 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.

## 4.7.0 (2023-09-25)

### Bugs fixed
- Added a fallback mechanism to use service-side cryptography if not possible to perform operations locally. ([#36657](https://github.com/Azure/azure-sdk-for-java/pull/36657))

### Other Changes
- Due to internal client changes, made `KeyEncryptionKeyClient` extend `CryptographyClient`, mirroring their async counterparts. Functionality remains intact.
- Migrate test recordings to assets repo.

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 4.6.5 (2023-08-21)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 4.6.4 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.


## 4.6.3 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 4.6.2 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 4.6.1 (2023-04-20)

### Other Changes

- Test proxy server migration.
- Made all logger instances static.

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.
- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.

## 4.6.0 (2023-03-18)

### Features Added
- Added support for service version `7.4`.

### Breaking Changes
> These changes do not impact the API of stable versions such as `4.5.4`. Only code written against a beta version such as `4.6.0-beta.1` may be affected.
- Removed support for Octet Key Pair (OKP) operations.

### Other Changes
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.
- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.

## 4.5.4 (2023-02-16)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.12.8` to version `1.13.0`.
- Upgraded `azure-core` from `1.35.0` to version `1.36.0`.

## 4.5.3 (2023-01-09)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.34.0` to version `1.35.0`.
- Upgraded `azure-core-http-netty` from `1.12.7` to version `1.12.8`.

## 4.6.0-beta.1 (2022-11-11)

### Features Added
- Added `CreateOkpKeyOptions` to pass key options when creating an Octet Key Pair (OKP) on Managed HSM.
- Added `createOkpKey()` and `createOkpKeyWithResponse()` to `KeyClient` and `KeyAsyncClient` to create an Octet Key Pair (OKP) on Managed HSM.
- Added `OKP` and `OKP_HSM` to `KeyType`.
- Added `Ed25519` to `KeyCurveName` to create an Octet Key Pair (OKP) using the Ed25519 curve.
- Added `EDDSA` to `SignatureAlgorithm` to support signing and verifying using an Edwards Curve Digital Signature Algorithm (EdDSA) on Managed HSM.
- Added support for service version `7.4-preview.1`.

## 4.5.2 (2022-11-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.33.0` to version `1.34.0`.
- Upgraded `azure-core-http-netty` from `1.12.6` to version `1.12.7`.

## 4.5.1 (2022-10-17)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` from `1.32.0` to version `1.33.0`.
- Upgraded `azure-core-http-netty` from `1.12.5` to version `1.12.6`.

## 4.5.0 (2022-09-20)

### Breaking Changes
- Made it so that we verify that the challenge resource matches the vault domain by default. This should affect few customers who can use the `disableChallengeResourceVerification()` method in client builders to disable this functionality. See https://aka.ms/azsdk/blog/vault-uri for more information.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to version `1.32.0`.
- Upgraded `azure-core-http-netty` from `1.12.4` to version `1.12.5`.

## 4.4.6 (2022-08-17)

### Bugs Fixed
- Fixed an issue where requests sent by sync clients that should include a body could have an empty body instead. ([#30512](https://github.com/Azure/azure-sdk-for-java/pull/30512))

## 4.4.5 (2022-08-15)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.31.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.4`.

## 4.4.4 (2022-07-06)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.30.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.3`.

## 4.4.3 (2022-06-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.29.1`.
- Upgraded `azure-core-http-netty` dependency to `1.12.2`.

## 4.4.2 (2022-05-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.28.0`.
- Upgraded `azure-core-http-netty` dependency to `1.12.0`.

## 4.4.1 (2022-04-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.27.0`.
- Upgraded `azure-core-http-netty` dependency to `1.11.9`.

## 4.4.0 (2022-03-31)

### Features Added
- Added support for service version `7.3`.
- Added the following APIs to `KeyClient` and `KeyAsyncClient`:
  - `getRandomBytes` and `getRandomBytesWithResponse` which, when connected to a managed HSM, can be used to generate a byte array of a given length with random values.
  - `releaseKey` and `releaseKeyWithResponse` which support securely releasing a key from a Managed HSM.
  - `rotateKey` and `rotateKeyWithResponse` which allow to rotate a key on-demand in Azure Key Vault and Managed HSM.
  - `getKeyRotationPolicy` and `getKeyRotationPolicyWithResponse` which allow to retrieve a key's automated rotation policy.
  - `updateKeyRotationPolicy` and `updateKeyRotationPolicyWithResponse` which allow to update a key's automated rotation policy.
  - `getCryptographyClient` and `getCryptographyAsyncClient` which provide a simple way to create a `CryptographyClient` and `CryptographyAsyncClient` respectively for a key given its name and optionally a version.
- Additionally added the following classes to support the aforementioned APIs:
  - `KeyRotationPolicy` which represents a key's automated rotation policy.
      - `KeyRotationLifetimeAction` which represents an action that will be performed by Key Vault over the lifetime of a key.
        - `KeyRotationPolicyAction`, an enum for the types of key rotation policy actions that can be executed relative to a key.
  - `KeyReleasePolicy` which represents the policy rules under which the key can be exported.
  - `ReleaseKeyOptions` which represents the configurable options to release a key.
    - `KeyExportEncryptionAlgorithm`, an enum for specifying an encryption algorithm to be used during key release.
  - `ReleaseKeyResult` which contains the value of a released key.
- `exportable` and `releasePolicy` were added to the following classes as well:
    - `KeyProperties`
    - `CreateKeyOptions`
    - `CreateEcKeyOptions`
    - `CreateOctKeyOptions`
    - `CreateRsaKeyOptions`

  in order to specify whether the key is exportable and to associate a release policy to a given key
- `CryptographyClientBuilder` does not require `keyIdentifier` to a include a key version. If no version is provided, cryptographic operations will be made using the latest version of the key.
- Implemented new traits (micro-interfaces) in `KeyClientBuilder`, `CryptographyClientBuilder` and `KeyEncryptionKeyClientBuilder`. This makes the experience of using client builders more consistent across libraries in the Azure SDK for Java.

### Breaking Changes
> These changes do not impact the API of stable versions such as `4.3.0`.
> Only code written against beta version `4.4.0-beta.7` may be affected.
- Changed `getRandomBytes` operations in `KeyClient` and `KeyAsyncClient` to return `byte[]` instead of `RandomBytes`.
- Removed the `RandomBytes` class.

## 4.3.8 (2022-03-17)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.26.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.8`

## 4.4.0-beta.7 (2022-02-11)

### Features added
- Implemented new traits (micro-interfaces) in `KeyClientBuilder`, `CryptographyClientBuilder` and `KeyEncryptionKeyClientBuilder`. This makes the experience of using client builders more consistent across libraries in the Azure SDK for Java.
- Added the `immutable` property to `KeyReleasePolicy`.

### Breaking Changes
- Removed the `exports` statement for `com.azure.security.keyvault.keys.implementation` in `module-info.java`.
- `KeyReleasePolicy`
    - Renamed `data` to `encodedPolicy` and changed its type from `byte[]` to `BinaryData`.
    - Flattened `KeyRotationPolicyProperties` into `KeyRotationPolicy`.
    - Renamed `expiryTime` to `expiresIn`.
- Renamed `target` to `targetAttestationToken` in `releaseKey` APIs.
- Removed `KeyExportRequestParameters` as the `export` operations will be pushed to a future release.
- Renamed `KeyRotationLifetimeAction`'s `type` to `action`, to align with existing similar APIs in Key Vault Certificates.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.25.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.7`

## 4.3.7 (2022-02-11)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.25.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.7`

## 4.4.0-beta.6 (2022-01-13)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.24.1`
- Upgraded `azure-core-http-netty` dependency to `1.11.6`

## 4.3.6 (2022-01-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.24.1`
- Upgraded `azure-core-http-netty` dependency to `1.11.6`

## 4.4.0-beta.5 (2021-11-12)

### Features Added

- Added support for multi-tenant authentication in clients.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.22.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.2`

## 4.3.5 (2021-11-12)

### Features Added

- Added support for multi-tenant authentication in clients.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.22.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.2`

## 4.4.0-beta.4 (2021-10-07)

### Features Added
- Added new functions to key clients to enable key rotation:
  - `KeyClient`
    - `rotateKey(String name)`
    - `rotateKeyWithResponse(String name, Context context)`
    - `getKeyRotationPolicy(String name)`
    - `getKeyRotationPolicyWithResponse(String name, Context context)`
    - `updateKeyRotationPolicy(String name, KeyRotationPolicyProperties keyRotationPolicyProperties)`
    - `updateKeyRotationPolicyWithResponse(String name, KeyRotationPolicyProperties keyRotationPolicyProperties, Context context)`
  - `KeyAsyncClient`
    - `rotateKey(String name)`
    - `rotateKeyWithResponse(String name)`
    - `getKeyRotationPolicy(String name)`
    - `getKeyRotationPolicyWithResponse(String name)`
    - `updateKeyRotationPolicy(String name, KeyRotationPolicyProperties keyRotationPolicyProperties)`
    - `updateKeyRotationPolicyWithResponse(String name, KeyRotationPolicyProperties keyRotationPolicyProperties)`
- Added convenience methods to create cryptography clients using key clients:
  - `KeyClient.getCryptographyClient(String keyName)`
  - `KeyClient.getCryptographyClient(String keyName, String keyVersion)`
  - `KeyAsyncClient.getCryptographyAsyncClient(String keyName)`
  - `KeyAsyncClient.getCryptographyAsyncClient(String keyName, String keyVersion)`
- `CryptographyClientBuilder` does not require `keyIdentifier` to a include a key version. If no version is provided, cryptographic operations will be made using the latest version of the key.

### Bugs Fixed
- Fixed an issue that made clients send unnecessary unauthorized requests to obtain a bearer challenge from the service even when already possessing a valid bearer token.
- Fixed issue that prevented creating a `CryptographyClient` or `CryptographyAsyncClient` with a key identifier that does not contain a key version.
- Fixed issue that made `createOctKey()` operations ignore a `keySize` set in `CreateOctKeyOptions`, making said keys be created with the default service key size instead.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.21.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.1`

## 4.3.4 (2021-10-06)

### Bugs Fixed
- Fixed an issue that made clients send unnecessary unauthorized requests to obtain a bearer challenge from the service even when already possessing a valid bearer token.
- Fixed issue that prevented creating a `CryptographyClient` or `CryptographyAsyncClient` with a key identifier that does not contain a key version.
- Fixed issue that made `createOctKey()` operations ignore a `keySize` set in `CreateOctKeyOptions`, making said keys be created with the default service key size instead.

## 4.4.0-beta.3 (2021-09-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 4.3.3 (2021-09-10)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.20.0`
- Upgraded `azure-core-http-netty` dependency to `1.11.0`

## 4.4.0-beta.2 (2021-08-13)

### Features Added
- To support Secure Key Release for Key Vault and Managed HSM, added `Exportable` and `ReleasePolicy` to the following classes:
  - `CreateKeyOptions` and its children classes: `CreateEcKeyOptions`, `CreateOctKeyOptions` and `CreateRsaKeyOptions`.
  - `ImportKeyOptions`
  - `KeyProperties`
- Added `releaseKey()` and `releaseKeyWithResponse()` operations to `KeyClient` and `KeyAsyncClient` to securely release a key for Key Vault and Managed HSM.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.19.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.2`

## 4.3.2 (2021-08-12)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.19.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.2`

## 4.4.0-beta.1 (2021-07-09)

### Features Added
- Added support for service version `7.3-preview`.
- Added support for requesting a desired amount of randomly generated bytes from a Managed HSM.

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 4.3.1 (2021-07-08)

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` dependency to `1.18.0`
- Upgraded `azure-core-http-netty` dependency to `1.10.1`

## 4.3.0 (2021-06-17)

### Features Added
- Changed default service version to `7.2`.
- Added `KeyVaultKeyIdentifier` to parse key URLs.
- Added local-only support for `CryptographyClient` and `CryptographyAsyncClient` by providing a `JsonWebKey` during client creation.
- Added `KeyType.OCT-HSM` to support oct-HSM key operations to support Managed HSM.
- Added the `CreateOctKeyOptions` class and associated `createOctKey()` methods.
- Added AES-GCM and AES-CBC support for encrypting and decrypting, including new `Encrypt` and `Decrypt` overloads.
- Added the ability to set a public exponent on RSA keys during creation.
- Made all getters for properties of a `JsonWebKey` public.

### Changes since 4.3.0-beta.8

#### Bug Fixes
- Ensured that `RetryPolicy` and `HttpLogOptions` use a default implementation when creating Key Vault clients if not set or set to `null`.

#### New Features
- Added `createOctKey()` and `createOctKeyWithResponse()` to `KeyClient` and `KeyAsyncClient`.
- Added factory methods for RSA algorithms in `DecryptParameters` and `EncryptParameters`:
    - `createRsa15Parameters()`
    - `createRsaOaepParameters()`
    - `createRsaOaep256Parameters()`

#### Breaking Changes
- Removed `EXPORT` from the `KeyOperation` enum.
- Re-ordered parameters in the `EncryptResult` constructor to show `authenticationTag` before `additionalAuthenticatedData` to align with classes like `DecryptParameters`.
- Removed service method overloads that take a `pollingInterval`, since `PollerFlux` and `SyncPoller` objects allow for setting this value directly on them.
- Moved `EncryptParameters` and `DecryptParameters` from the `cryptography` package to the `cryptography.models` package and made them both `final`.

#### Non-Breaking
- Renamed `keyId` to `sourceId` in `KeyVaultKeyIdentifier`.
- `KeyVaultKeyIdentifier` can now be used to parse any Key Vault identifier.
- Added the `@ServiceMethod` annotation to all public methods that call the Key Vault service in `KeyClient`, `KeyAsyncClient`, `CryptographyClient` and `CryptographyAsyncClient`.

## 4.3.0-beta.8 (2021-05-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.16.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.2`
- Upgraded `azure-identity` dependency to `1.3.0`

## 4.3.0-beta.7 (2021-04-29)

### Bug fixes
- Fixed issue that prevented setting tags on keys when creating or importing them.

### Breaking Changes
- Removed the `exportKey()` operation from `KeyAsyncClient` and `KeyClient`, as it is not yet supported in the current service version.

## 4.3.0-beta.6 (2021-04-09)

### Breaking Changes
- Renamed `EncryptOptions` to `EncryptParameters`.
- Renamed `DecryptOptions` to `DecryptParameters`.
- Changed `KeyVaultKeyIdentifier` so it is instantiated via its constructor as opposed to via a `parse()` factory method.
- Removed the following classes:
    - `LocalCryptographyAsyncClient`
    - `LocalCryptographyClient`
    - `LocalCryptographyClientBuilder`
    - `LocalKeyEncryptionKeyClient`
    - `LocalKeyEncryptionKeyAsyncClient`
    - `LocalKeyEncryptionKeyClientBuilder`

### New features
- Added support for service version `7.2`.
- Made all `JsonWebKey` properties settable.
- Added support to specify whether or not a pipeline policy should be added per call or per retry.
- Added convenience class `CreateOctKeyOptions`.
- Added support for building local-only cryptography clients by providing a `JsonWebKey` for local operations:
    - `CryptograhpyClientBuilder.jsonWebKey(JsonWebKey)`
- Added support for building local-only key encryption key clients by providing a `JsonWebKey` for local operations:
    - `KeyEncryptionKeyClientBuilder.buildKeyEncryptionKey(JsonWebKey)`
    - `KeyEncryptionKeyClientBuilder.buildAsyncKeyEncryptionKey(JsonWebKey)`
- `CryptograhpyClientBuilder.keyIdentifier(String)` now throws a `NullPointerException` if a `null` value is provided as an argument.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.15.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.1`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.1`
- Upgraded `azure-identity` dependency to `1.2.5`

## 4.3.0-beta.5 (2021-03-12)

### Breaking Changes
- Removed local support for encryption and decryption using AESGCM, as per guidance of Microsoft's cryptography board. Remote encryption and decryption using said algorithm is still supported.

### Changed
- Changed logging level in `onRequest` and `onSuccess` calls for service operations from `INFO` to `VERBOSE`.

### Bug fixes
- Fixed issue that caused a `NullPointerException` when attempting to use a `CryptographyClient` for symmetric key encryption operations after the first one.
- Fixed issue where `JsonWebKey` byte array contents would get serialized/deserialized using Base64 instead of URL-safe Base64.
- Fixed issue where properties of responses received when using a `CryptographyClient` for encryption/decryption were not populated on the `EncryptResult` and `DecryptResult` classes.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.14.0`
- Upgraded `azure-core-http-netty` dependency to `1.9.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.6.0`
- Upgraded `azure-identity` dependency to `1.2.4`

## 4.3.0-beta.4 (2021-02-11)

### Bug Fixes
- Fixed issue where cryptographic operations would be attempted locally for symmetric keys that were missing their key material ('k' component).

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.2.5 (2021-02-11)

### Bug Fixes
- Fixed issue where cryptographic operations would be attempted locally for symmetric keys that were missing their key material ('k' component).

### Dependency Updates
- Upgraded `azure-core` dependency to `1.13.0`
- Upgraded `azure-core-http-netty` dependency to `1.8.0`
- Upgraded `azure-core-http-okhttp` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.2.3`

## 4.2.4 (2021-01-15)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.12.0`
- Upgraded `azure-core-http-netty` dependency to `1.7.1`
- Upgraded `azure-core-http-okhttp` dependency to `1.4.1`
- Upgraded `azure-identity` dependency to `1.2.2`

## 4.3.0-beta.3 (2020-11-19)

### New Features
- Added support for encrypting and decrypting AES-GCM and AES-CBC keys.
- Added `KeyType.OCT_HSM` to support "oct-HSM" key operations.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.3`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.3`
- Upgraded `azure-core-test` dependency to `1.5.1`
- Upgraded `azure-identity` dependency to `1.2.0`

## 4.2.3 (2020-11-12)

### Dependency Updates
- Upgraded `azure-core` dependency to `1.10.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.3`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.3`
- Upgraded `azure-core-test` dependency to `1.5.1`
- Upgraded `azure-identity` dependency to `1.2.0`

## 4.3.0-beta.2 (2020-10-09)

### New Features
- Added `KeyVaultKeyIdentifier`. Use its [`parse`](https://github.com/Azure/azure-sdk-for-java/blob/ff52067a3772a430e5913b898f2806078aec8ef2/sdk/keyvault/azure-security-keyvault-keys/src/main/java/com/azure/security/keyvault/keys/models/KeyVaultKeyIdentifier.java#L78) method to parse the different elements of a given key identifier.
- Added API overloads that allow for passing specific polling intervals for long-running operations:
    - `KeyAsyncClient`
        - `beginDeleteKey(String, Duration)`
        - `beginRecoverDeletedKey(String, Duration)`
    - `KeyClient`
        - `beginDeleteKey(String, Duration)`
        - `beginRecoverDeletedKey(String, Duration)`
- Added support for `com.azure.core.util.ClientOptions` in client builders.

### Bug Fixes
- Fixed an issue that prevented the `tags` and `managed` members of `KeyProperties` from getting populated when retrieving a single key using `KeyClient`, `KeyAsyncClient`, `CryptographyClient` and `CryptographyAsyncClient`.

### Dependency updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.2.2 (2020-10-08)

### Bug Fixes
- Fixed an issue that prevented the `tags` and `managed` members of `KeyProperties` from getting populated when retrieving a single key using `KeyClient`, `KeyAsyncClient`, `CryptographyClient` and `CryptographyAsyncClient`.

### Dependency Updates
- Upgraded `azure-core` dependency to `1.9.0`
- Upgraded `azure-core-http-netty` dependency to `1.6.2`
- Upgraded `azure-core-http-okhttp` dependency to `1.3.2`
- Upgraded `azure-core-test` dependency to `1.5.0`
- Upgraded `azure-identity` dependency to `1.1.3`

## 4.3.0-beta.1 (2020-09-11)
- Updated versions for azure-core and azure-identity.

## 4.2.1 (2020-09-10)
- Updated versions for azure-core and azure-identity.

## 4.2.0 (2020-08-12)
- Added support for service version `7.1`.
- Added `retryPolicy` setter in `KeyClientBuilder`, `CryptographyClientBuilder` and `KeyEncryptionKeyClientBuilder`.
- Added `recoverableDays` property to `KeyProperties`.
- Added `Import` operation to `KeyOperation`.

## 4.2.0-beta.5 (2020-07-08)
- Updated versions for azure-core, azure-identity.

## 4.1.5 (2020-07-08)
- Updated versions for azure-core and azure-identity.

## 4.2.0-beta.4 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.
- `404` responses from `listPropertiesOfKeyVersions` in `KeyAsyncClient` and `KeyClient` now throw a `ResourceNotFoundException`.
- `buildAsyncKeyEncryptionKey` in `LocalKeyEncryptionKeyClientBuilder` now throws an exception when no ID is present in a given `JsonWebKey`.

## 4.1.4 (2020-06-10)
- Updated version for azure-core, azure-identity and external dependencies.
- `404` responses from `listPropertiesOfKeyVersions` in `KeyAsyncClient` and `KeyClient` now throw a `ResourceNotFoundException`.

## 4.1.3 (2020-05-06)
- Update azure-core dependency to version 1.5.0.

## 4.2.0-beta.3 (2020-04-09)
- Added `LocalCryptographyClient`, `LocalCryptographyAsyncClient`, `LocalKeyEncryptionKeyClient` and `LocalKeyEncryptionKeyAsyncClient` to perform cryptography operations locally.
- Added `retryPolicy` setter in `KeyClientBuilder`, `CryptographyClientBuilder` and `KeyEncryptionKeyClientBuilder`
- Update azure-core dependency to version 1.4.0.

## 4.1.2 (2020-04-07)
- Update azure-core dependency to version 1.4.0.

## 4.1.1 (2020-03-25)
- Update azure-core dependency to version 1.3.0.

## 4.2.0-beta.2 (2020-03-10)
### Added
- Added `recoverableDays` property to `KeyProperties`.
- Added `Import` operation to `KeyOperation`.
- Added support for `7.1-Preview` service version


## 4.2.0-beta.1
- `KeyVaultKey` model can be instantiated using `fromKeyId(String keyId, JsonWebKey jsonWebKey)` and `fromName(String name, JsonWebKey jsonWebKey)` methods on the `KeyVaultKey` model.
- Allows `KeyEncryptionKeyClientBuilder` to consume `KeyVaultKey` model and build `KeyEncryptionKey` and `AsyncKeyEncryptionKey` via`buildKeyEncryptionKey(KeyVaultKey key) ` and `buildAsyncKeyEncryptionKey(KeyVaultKey key)` methods respectively.

## 4.1.0 (2020-01-07)
- Fixes the logic of `getKeyId()` method in `KeyEncryptionKeyClient` and `KeyEncryptionKeyAsyncClient` to ensure key id is available in all scenarios.
- Update azure-core dependency to version 1.2.0.

## 4.0.1 (2019-12-06)

### Major changes
- `KeyEncryptionKeyClientBuilder.buildKeyEncryptionKey` and `KeyEncryptionKeyClientBuilder.buildAsyncKeyEncryptionKey`supports consumption of a secret id representing the symmetric key stored in the Key Vault as a secret.
- Dropped third party dependency on apache commons codec library.


### Breaking changes
- Key has been renamed to KeyVaultKey to avoid ambiguity with other libraries and to yield better search results.
- Key.keyMaterial has been renamed to KeyVaultKey.key.
- The setters of JsonWebKey properties have been removed.
- JsonWebKey methods fromRsa, fromEc and fromAes now take an optional collection of key operations.
- JsonWebKey.keyOps is now read-only. You must pass a list of key operations at construction time.
- endpoint method on KeyClientBuilder has been renamed to vaultUrl.
- hsm properties and parameters have been renamed to hardwareProtected.
- On KeyProperties, expires, created, and updated have been renamed to expiresOn, createdOn, and updatedOn respectively.
- On DeletedKey, deletedDate has been renamed to DeletedOn.
- listKeys and listKeyVersions methods have been renamed to listPropertiesOfKeys and listPropertiesOfKeyVersions respectively in `KeyClient` and `KeyAsyncClient`.
- restoreKey method has been renamed to restoreKeyBackup in `KeyClient` and `KeyAsyncClient` to better associate it with KeyClient.backupKey.
- deleteKey method has been renamed to beginDeleteKey and now returns a SyncPoller in `KeyClient` and PollerFlux in `KeyAsyncClient` to track this long-running operation.
- recoverDeletedKey method has been renamed to beginRecoverDeletedKey and now returns a SyncPoller in `KeyClient` and PollerFlux in `KeyAsyncClient` to track this long-running operation.
- KeyCreateOptions has been renamed to CreateKeyOptions.
- EcCreateKeyOptions has been renamed to CreateEcKeyOptions.
- CreateEcKeyOptions.curve has been renamed to curveName to be consistent.
- RsaKeyCreateOptions has been renamed to CreateRsaKeyOptions.
- KeyImportOptions has been renamed to ImportKeyOptions.

### Major changes
- JsonWebKey.keyType and JsonWebKey.keyOps have been exposed as KeyVaultKey.keyType and KeyVaultKey.keyOperations respectively.
- KeyClient.vaultUrl has been added with the original value pass to KeyClient.

## 4.0.0-preview.4 (2019-10-08)
For details on the Azure SDK for Java (September 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

- Updated to be fully compliant with the Java 9 Platform Module System.

### Breaking changes
- `KeyBase` has been renamed to `KeyProperties`.
- `Key` and `DeletedKey` no longer extend `KeyProperties`, but instead contain a `KeyProperties` property named `Properties`.
- `updateKey` method has been renamed to `updateKeyProperties` in `KeyClient` and `KeyAsyncClient`.
- Getters and setters were updated to use Java Bean notation.
- Changed VoidResponse to Response<Void> on sync API, and Mono<VoidResponse> to Mono<Response<Void>> on async API.
- Enumerations including `KeyCurveName`, `KeyOperation`, and `KeyType` are now structures that define well-known, supported static fields.

## 4.0.0-preview.3 (2019-09-10)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

## 4.0.0-preview.2 (2019-08-06)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://azure.github.io/azure-sdk/releases/2019-08-06/index.html).

- Added service side Cryptography Operations support for asymmetric keys (sign, un/wrap, verify, encrypt and decrypt)
- Added client side Cryptography Operations support both asymmetric and symmetric keys.
- Added Cryptography clients to `azure-keyvault-keys` package.
    - `azure-keyvault-keys` contains a `CryptographyClient` and `CryptographyAsyncClient` for cryptography operations and  `KeyClient` and `KeyAsyncClient` for key operations.
    - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-keys_4.0.0-preview.2/sdk/keyvault/README.md) and
  [samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-keyvault-keys_4.0.0-preview.2/sdk/keyvault/azure-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys) for more information.
- Added support for HTTP challenge based authentication, allowing clients to interact with vaults in sovereign clouds.
- Combined KeyClientBuilder, KeyAsyncClientBuilder into KeyClientBuilder. Methods to create both sync and async clients type were added.
- Removed static builder method from clients. Builders are now instantiable.

## 4.0.0-preview.1 (2019-06-28)
Version 4.0.0-preview.1 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://aka.ms/azsdk/guide/java).

For details on the Azure SDK for Java (July 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview1-java).

This library is not a direct replacement for keys management operations from microsoft-azure-keyvault. Applications using that library would require code changes to use `azure-keyvault-keys`.
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-keyvault-keys_4.0.0-preview.1/keyvault/client/keys/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/azure-keyvault-keys_4.0.0-preview.1/keyvault/client/keys/src/samples/java)
demonstrate the new API.


### Major changes from `azure-keyvault`
- Packages scoped by functionality
    - `azure-keyvault-keys` contains a `KeyClient` and `KeyAsyncClient` for key operations, 
    `azure-keyvault-secrets` contains a `SecretClient` and `SecretAsyncClient` for secret operations
- Client instances are scoped to vaults (an instance interacts with one vault
only)
- Reactive streams support using [Project Reactor](https://projectreactor.io/).
- Authentication using `azure-identity` credentials
  - see this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-keys/README.md)
  , and the
  [Azure Identity documentation](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity/README.md)
  for more information
  
### `azure-keyvault` features not implemented in this library
- Certificate management APIs
- Cryptographic operations, e.g. sign, un/wrap, verify, encrypt and decrypt
- National cloud support. This release supports public global cloud vaults,
    e.g. https://{vault-name}.vault.azure.net
