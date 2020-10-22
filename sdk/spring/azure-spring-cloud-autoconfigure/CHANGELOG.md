# Release History

## 2.0.0-beta.1 (Unreleased)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-autoconfigure` to `azure-spring-cloud-autoconfigure`.

## 1.2.8 (2020-09-14)
### New Features
 - Enable Storage starter to support overwriting blob data
 - Enable Actuator for storage blob
 - Enable scheduled enqueue message in Service Bus binders

### Key Bug Fixes
 - Fixed the repeated consumption of Event Hubs messages when the checkpoint mode is BATCH
