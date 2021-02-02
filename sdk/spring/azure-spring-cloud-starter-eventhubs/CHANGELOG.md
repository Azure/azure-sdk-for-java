# Release History

## 2.2.0-beta.1 (Unreleased)


## 2.1.0 (2021-01-20)


## 2.0.0 (2020-12-30)
### Breaking Changes
- Deprecated the `spring.cloud.azure.managed-identity.client-id` property,
  use `spring.cloud.azure.client-id` to set the managed identity id when using Managed Identity.

## 2.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-starter-azure-eventhubs` to `azure-spring-cloud-starter-eventhubs`.

## 1.2.8 (2020-09-14)
### Key Bug Fixes
 - Fixed the repeated consumption of Event Hubs messages when the checkpoint mode is BATCH
