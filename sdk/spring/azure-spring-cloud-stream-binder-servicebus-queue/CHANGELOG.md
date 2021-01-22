# Release History

## 2.1.0 (2021-01-20)


## 2.0.0 (2020-12-30)
### Breaking Changes
- Deprecated the `spring.cloud.azure.managed-identity.client-id` property,
  use `spring.cloud.azure.client-id` to set the managed identity id when using Managed Identity.

## 2.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-servicebus-queue-stream-binder` to `azure-spring-cloud-stream-binder-servicebus-queue`.

## 1.2.8 (2020-09-14)
### New Features
 - Enable scheduled enqueue message in Service Bus binders
