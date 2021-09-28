# Release History

## 2.1.1 (2021-09-28)

### Bugs Fixed

* Fixed usage of `null` for watch keys. Updates returned `null` labels automatically to `\0`.
* Reworked Feature Flag watching to make sure all changes are detected. Such as new or deleted feature flags.

### Other Changes

* Updated Tracing to check for Key Vault and Dev usage.

## 2.1.0 (2021-09-05)

* Update to JUnit 5 from JUnit 4

## 2.0.0 (2021-07-20)

* GA of 2.0.0 version, no changes from 2.0.0-beta.2 version.

## 2.0.0-beta.2 (2021-06-21)
- Changed package path to `com.azure.spring.cloud.config`

## 2.0.0-beta.1 (2021-05-04)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-appconfiguration-config-web` to `azure-spring-cloud-appconfiguration-config-web`.
- Added a new Push based Refresh method. Two Spring Actuator endpoints have been added `appconfiguration-refresh` and `appconfiguration-refresh-bus`. The first triggers the cache to reset on configurations on an application. The second triggers a refresh on all instances subscribed to the same Service Bus.
