# Release History

## 2.0.0-beta.2 (Unreleased)


## 2.0.0-beta.1 (2021-05-04)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-appconfiguration-config-web` to `azure-spring-cloud-appconfiguration-config-web`.
- Added a new Push based Refresh method. Two Spring Actuator endpoints have been added `appconfiguration-refresh` and `appconfiguration-refresh-bus`. The first triggers the cache to reset on configurations on an application. The second triggers a refresh on all instaces subscribed to the same Service Bus.
