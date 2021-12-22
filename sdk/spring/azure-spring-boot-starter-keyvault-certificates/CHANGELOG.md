# Release History

## 3.12.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 3.11.0 (2021-11-24)

This release is compatible with Spring Boot 2.5.0 - 2.5.4.
### Dependency Upgrades
Regular updates for Azure SDK dependency versions.

## 3.10.0 (2021-11-02)
### Breaking Changes
Rename `azure.keyvault.jca.certificates-refresh-interval` to `azure.keyvault.jca.certificates-refresh-interval-in-ms`.
### Other Changes
- Skip minor versions to make version align to other azure-spring-boot-starters

## 3.2.0 (2021-09-27)
This release is compatible with Spring Boot 2.5.0 - 2.5.4.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).


## 3.1.0 (2021-08-25)
This release is compatible with Spring Boot 2.5.0 - 2.5.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).
### New Features
- Support key less certificate. ([#22105](https://github.com/Azure/azure-sdk-for-java/issues/22105))

## 3.0.1 (2021-07-01)
### Bug Fixes
- Fixed bug: Not get certificates from Key Vault when `azure.keyvault.jca.certificates-refresh-interval` is not set. [#22666](https://github.com/Azure/azure-sdk-for-java/pull/22666)


## 3.0.0 (2021-06-23)
### New Features
- Load JRE key store certificates to AzureKeyVault key store. ([#21845](https://github.com/Azure/azure-sdk-for-java/pull/21845))


## 3.0.0-beta.7 (2021-05-24)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).


## 3.0.0-beta.6 (2021-04-19)
- Remove configurable property of azure.keyvault.aad-authentication-url which is configured according to azure.keyvault.uri automatically [#20530](https://github.com/Azure/azure-sdk-for-java/pull/20530)


## 3.0.0-beta.5 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).


## 3.0.0-beta.4 (2021-03-03)


## 3.0.0-beta.3 (2021-01-20)


## 3.0.0-beta.2 (2020-11-18)
- Add support for user-assigned managed identity.


## 3.0.0-beta.1 (2020-10-21)
 - First release.
