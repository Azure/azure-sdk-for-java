# Release History

## 2.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
