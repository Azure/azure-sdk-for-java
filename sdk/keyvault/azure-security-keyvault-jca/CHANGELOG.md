# Release History

## 2.9.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

 - Fixed bug about intermediate certificate not loaded. [#39715](https://github.com/Azure/azure-sdk-for-java/issues/39715).

### Other Changes

## 2.9.0-beta.2 (2024-07-09)

### Features Added
- Added the new system property `azure.keyvault.disable-challenge-resource-verification`, which can be set to `true` to disable challenge resource verification when authenticating against the Azure Key Vault service. For more information, please refer to [this link](https://devblogs.microsoft.com/azure-sdk/guidance-for-applications-using-the-key-vault-libraries/).

### Breaking Changes
- Removed support for providing a custom login URI to get access tokens from via the system property `azure.login.uri`.

### Bugs Fixed
- Fix bug: AccessTokenUtil does not urlencode its parameters when getting an access token. ([#40616](https://github.com/Azure/azure-sdk-for-java/issues/40616))
- Changed the authentication mechanism to allow for discovering the login URI for a given Azure Key Vault instance by requesting an authentication challenge from the service, as opposed to using a hard-coded list of URIs to choose from depending on a vault's URI. This should add support for customers using Azure Stack instances, for example.

## 2.9.0-beta.1 (2024-05-15)

### Features Added
- Added support for providing a custom login URI to get access tokens from via the system property `azure.login.uri`.

### Other Changes

#### Dependency Updates
- Upgraded `conscrypt-openjdk-uber` from `2.2.1` to version `2.5.2`.

## 2.8.1 (2023-12-04)

### Other Changes

#### Dependency Updates

Regular updates for dependency versions.

## 2.8.0 (2023-09-28)

### Features Added
- Support key type of `RSA-HSM` and `EC-HSM` in JCA [#36648](https://github.com/Azure/azure-sdk-for-java/pull/36648).

## 2.7.1 (2023-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `httpclient` from `4.5.13` to version `4.5.14`.
- Upgraded `jackson-databind` from `2.13.2.2` to version `2.13.5`.

## 2.7.0 (2022-05-24)

### Dependency Upgrades
Regular updates for dependency versions.

## 2.6.0 (2022-02-25)

### Dependency Upgrades
Regular updates for dependency versions.

## 2.5.0 (2022-01-25)

### Dependency Upgrades
Regular updates for dependency versions.

## 2.4.0 (2021-12-24)

### Dependency Upgrades
Regular updates for dependency versions.

## 2.3.0 (2021-11-25)
### Dependency Upgrades
Regular updates for dependency versions.

## 2.2.0 (2021-11-02)
### Features Added
- Support connect to multi keyvault for keyless. ([24718](https://github.com/Azure/azure-sdk-for-java/pull/24718))

## 2.1.0 (2021-09-26)

### Features Added
- Enable access token cache. ([23847](https://github.com/Azure/azure-sdk-for-java/pull/23847))


### Bugs Fixed
- Fix bug about dead loop. ([23923](https://github.com/Azure/azure-sdk-for-java/pull/23923))


## 2.0.0 (2021-08-25)
### New Features
- Support key less certificate. ([#22105](https://github.com/Azure/azure-sdk-for-java/issues/22105))

## 1.0.1 (2021-07-01)
### Bug Fixes
- Fixed bug: Not get certificates from Key Vault when `azure.keyvault.jca.certificates-refresh-interval` is not set. [#22666](https://github.com/Azure/azure-sdk-for-java/pull/22666)

## 1.0.0 (2021-06-23)
### New Features
- Load JRE key store certificates to AzureKeyVault key store. ([#21845](https://github.com/Azure/azure-sdk-for-java/pull/21845))
- Support properties of azure.cert-path.well-known and azure.cert-path.custom to support load cert from file system. ([#21947](https://github.com/Azure/azure-sdk-for-java/pull/21947))

## 1.0.0-beta.7 (2021-05-24)
### New Features
- Add "module-info.java".


## 1.0.0-beta.6 (2021-04-19)
### Breaking Changes
 - Remove configurable property of azure.keyvault.aad-authentication-url which is configured according to azure.keyvault.uri automatically [#20530](https://github.com/Azure/azure-sdk-for-java/pull/20530)

## 1.0.0-beta.5 (2021-03-22)


## 1.0.0-beta.4 (2021-03-03)


## 1.0.0-beta.3 (2021-01-20)

### Key Bug Fixes
 - Fix NullPointerException in KeyVaultKeyManagerFactory.

### New Features
 - Support properties named in hyphens style, like "azure.keyvault.tenant-id".


## 1.0.0-beta.2 (2020-11-17)

### New Features
- Add support for PEM based certificates.


## 1.0.0-beta.1 (2020-10-21)
 - First release.
