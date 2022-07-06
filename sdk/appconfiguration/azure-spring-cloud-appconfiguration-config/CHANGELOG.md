# Release History

## 2.9.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.8.0 (2022-06-29)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.9, 2.7.0-2.7.1. (Note: 2.5.x (x>14), 2.6.y (y>9) and 2.7.z (z>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.3. (Note: 2020.0.x (x>5) and 2021.0.y (y>3) should be supported, but they aren't tested with this release.)

### Dependency Upgrades
- Upgrade azure-sdk's version to latest released version.

## 2.7.0 (2022-05-24)
- This release is compatible with Spring Boot 2.5.0-2.5.13, 2.6.0-2.6.7. (Note: 2.5.x (x>13) and 2.6.y (y>7) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.2. (Note: 2020.0.x (x>5) and 2021.0.y (y>2) should be supported, but they aren't tested with this release.)

### Dependency Upgrades
 - Upgrade azure-sdk's version to latest released version.

## 2.6.0 (2022-04-29)
- This release is compatible with Spring Boot 2.5.0-2.5.13, 2.6.0-2.6.7. (Note: 2.5.x (x>13) and 2.6.y (y>7) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.2. (Note: 2020.0.x (x>5) and 2021.0.y (y>2) should be supported, but they aren't tested with this release.)

### Dependency Upgrades
- Regular updates for Azure SDK dependency versions.
- Upgrade external dependencies' version according to [spring-boot-dependencies:2.6.6](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.6/spring-boot-dependencies-2.6.6.pom) to address [CVE-2022-22965](https://github.com/advisories/GHSA-36p3-wjmg-h94x) [#28280](https://github.com/Azure/azure-sdk-for-java/pull/28280).
- Upgrade external dependencies' version according to [spring-cloud-dependencies:2021.0.2](https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2021.0.2/spring-cloud-dependencies-2021.0.2.pom) to address [CVE-2022-22963](https://github.com/advisories/GHSA-6v73-fgf6-w5j7) [#28179](https://github.com/Azure/azure-sdk-for-java/issues/28179).

## 2.5.0 (2022-03-28)
This release is compatible with Spring Boot 2.5.0-2.5.11, 2.6.0-2.6.5.

### Features Added
* Added refresh interval parameter to `spring.cloud.azure.appconfiguraiton` to force refreshes on a given interval. Can be used to make sure secrets are kept up to date.
* Added BackoffTimeCalculator, which sets the next refresh period to sooner if a refresh fails.

### Dependency Upgrades
- Regular updates for Azure SDK dependency versions.
- Upgrade external dependencies' version according to [spring-boot-dependencies:2.6.3](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.3/spring-boot-dependencies-2.6.3.pom) and [spring-cloud-dependencies:2021.0.1](https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2021.0.1/).


## 2.4.0 (2022-03-01)
This release is compatible with Spring Boot 2.5.5-2.5.8, 2.6.0-2.6.2.

### Dependency Upgrades
 - Regular updates for Azure SDK dependency versions.
 - Upgrade external dependencies' version according to [spring-boot-dependencies:2.6.2](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.2/spring-boot-dependencies-2.6.2.pom).

## 2.3.0 (2022-01-06)
This release is compatible with Spring Boot 2.5.5-2.5.8, 2.6.0-2.6.1.

### Bugs Fixed

* Fixed a bug where `spring.cloud.application` was still used in some locations. This caused a refresh bug where a `null` value was used on refresh. `spring.cloud.application` is replaced by `key-filter`.

## 2.2.0 (2021-11-25)

* Fixed a bug where JsonNode type was passed to Spring instead of a String, when the JsonNode was a number Spring had issues resolving the value.

## 2.1.1 (2021-09-28)

### Bugs Fixed

* Fixed usage of `null` for watch keys. Updates returned `null` labels automatically to `\0`.
* Reworked Feature Flag watching to make sure all changes are detected. Such as new or deleted feature flags.

### Other Changes

* Updated Tracing to check for Key Vault and Dev usage.

## 2.1.0 (2021-09-05)

* Add Health Indicator with the property `management.endpoint.health.azure-app-configuration.enabled` to enable the endpoint.
* Added Secret Resolver with use of `KeyVaultSecretProvider` which enables overriding connecting to Key Vault with client provided values.
* Update to JUnit 5 from JUnit 4

## 2.0.0 (2021-07-20)

* GA of 2.0.0 version, no changes from 2.0.0-beta.2 version.

## 2.0.0-beta.2 (2021-06-21)
### Breaking Changes
- Changed package path to `com.azure.spring.cloud.config`
- Renamed cache-expiration to refresh-interval
- Moved and renamed feature-flag cache-expiration to `spring.cloud.azure.appconfiguration.stores[0].monitoring.feature-flag-refresh-interval`
- Removed use of revisions endpoint

## 2.0.0-beta.1 (2021-05-04)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-appconfiguration-config` to `azure-spring-cloud-appconfiguration-config`.
- Format and options of library configuration has completely changed. See Readme in Starter
- Use of a Watch Key is now required see `spring.cloud.azure.appconfiguration.stores[0].monitoring.triggers`
- Added support for JSON content type
- Feature Management config loading is no longer on by default.
- Users can now select multiple groups of keys from one store see `spring.cloud.azure.appconfiguration.stores[0].selects`. Same default select happens as before.
- By default, `spring.profiles.active` is used as the label of all filters. This can be overridden using selects. If no profile is set `\0` is used i.e. `(No Label)
