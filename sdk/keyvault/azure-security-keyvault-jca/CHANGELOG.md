# Release History

## 1.1.0-beta.1 (Unreleased)


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
