# Release History

## 4.0.0-beta.3 (Unreleased)
- Supported spring-boot version: 2.6.0 - 2.6.1
- Supported spring-cloud version: 2021.0.0

### Features Added
- Support StorageQueueMessageConverter as a bean to support customize ObjectMapper.
- Support EventHubsMessageConverter as a bean to support customize ObjectMapper.

### Dependency Updates
Upgrade dependency according to spring-boot-dependencies:2.6.1 and spring-cloud-dependencies:2021.0.0

### Breaking Changes
1. Property name "spring.cloud.azure.active-directory.tenant-id" changed to "spring.cloud.azure.active-directory.profile.tenant-id".
2. Property name "spring.cloud.azure.active-directory.client-id" changed to "spring.cloud.azure.active-directory.credential.client-id".
3. Property name "spring.cloud.azure.active-directory.client-secret" changed to "spring.cloud.azure.active-directory.credential.client-secret".
4. Property name "spring.cloud.azure.active-directory.base-uri" changed to "spring.cloud.azure.active-directory.profile.environment.active-directory-endpoint".
5. Property name "spring.cloud.azure.active-directory.graph-base-uri" changed to "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint".
6. Property name "spring.cloud.azure.active-directory.graph-membership-uri" changed to "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint" and "spring.cloud.azure.active-directory.user-group.use-transitive-members".
7. Remove artifact id `spring-cloud-azure-stream-binder-test`.
8. Remove `StorageQueueOperation`.
9. Remove configuration of checkpoint mode for StorageQueueTemplate, and support only MANUAL mode.
10. Remove auto creating Storage Queue when send/receive messages via `StorageQueueTemplate`.
### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 4.0.0-beta.2 (2021-11-22)


Please refer to [Spring Cloud Azure Migration Guide for 4.0][Spring-Cloud-Azure-Migration-Guide-for-4.0] to learn how to migrate to version 4.0.

### spring-cloud-azure-stream-binder-eventhubs

#### Features Added

- Support batch consumers.

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-stream-binder-eventhubs` to `spring-cloud-azure-stream-binder-eventhubs`.
- Change the binder type from `eventhub` to `eventhubs`.
- Change the Spring Cloud Stream Binding extended properties prefix from `spring.cloud.stream.eventhub` to `spring.cloud.stream.eventhubs`.
- BATCH checkpoint-mode only works in batch-consuming mode.

### spring-cloud-azure-stream-binder-servicebus

#### Breaking Changes

- Combine libraries of `azure-spring-cloud-stream-binder-servicebus-queue` and `azure-spring-cloud-stream-binder-servicebus-topic` to `spring-cloud-azure-stream-binder-servicebus` with new binder type as `servicebus`.
- When using the binder to send messages, one of the following two attributes must be provided:
  - spring.cloud.stream.servicebus.bindings.{channel-name}.producer.entity-type
  - spring.cloud.azure.servicebus.producer.entity-type

#### Features Added

- Provide the ability of interacting with both queue and topic.

### spring-cloud-azure-starter-integration-eventhubs

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-eventhubs` to
  `spring-cloud-azure-starter-integration-eventhubs`.
- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Drop `EventHubOperation`, and move its `subscribe` API to class of `EventHubsProcessorContainer`.
- Rename `EventHubsInboundChannelAdapter` as `EventHubsInboundChannelAdapter` to keep consistent with the service of
  Azure
  Event Hubs, and change constructor signature as well.

* Change `CheckpointConfig` instantiation style to simple constructor instead of build style.

### spring-integration-azure-eventhubs

#### Breaking Changes

- Change artifact id from `azure-spring-integration-eventhubs` to
  `spring-integration-azure-eventhubs`.
- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Drop `EventHubOperation`, and move its `subscribe` API to class of `EventHubsProcessorContainer`.
- Rename `EventHubsInboundChannelAdapter` as `EventHubsInboundChannelAdapter` to keep consistent with the service of
  Azure
  Event Hubs, and change constructor signature as well.

* Change `CheckpointConfig` instantiation style to simple constructor instead of build style.

### spring-cloud-azure-starter-integration-servicebus

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-servicebus` to `spring-cloud-azure-starter-integration-servicebus`.
- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Combine the original `ServiceBusQueueTemplate#sendAsync` and `ServiceBusTopicTemplate#sendAsync` as `ServiceBusTemplate#sendAsync` and drop classes of `ServiceBusQueueTemplate` and `ServiceBusTopicTemplate`.
- Drop `RxJava` and `CompletableFuture` support of ServiceBusTemplate and support `Reactor` instead.
- Drop interface of `ServiceBusQueueOperation` and `ServiceBusTopicOperation`.
- Drop API of `ServiceBusQueueOperation#abandon` and `ServiceBusQueueOperation#deadletter`.
- Combine the original `ServiceBusQueueTemplate#subscribe` and `ServiceBusTopicTemplate#subscribe` as `ServiceBusProcessorClient#subscribe`.
- Deprecate the interface of `SubscribeOperation`.
- Add new API of `setDefaultEntityType` for ServiceBusTemplate, the default entity type of a ServiceBusTemplate is required when no bean of `PropertiesSupplier<String, ProducerProperties>` is provided for the `ProducerProperties#entityType`.
- Drop class of `ServiceBusQueueInboundChannelAdapter` and `ServiceBusTopicInboundChannelAdapter` and combine them as `ServiceBusInboundChannelAdapter`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`

#### Features Added

- Provide the ability to connect to multiple Azure Service Bus entities in different namespaces.

### spring-integration-azure-servicebus

#### Breaking Changes

- Change artifact id from `azure-spring-integration-servicebus` to `spring-integration-azure-servicebus`.
- Combine the original `ServiceBusQueueTemplate#sendAsync` and `ServiceBusTopicTemplate#sendAsync` as `ServiceBusTemplate#sendAsync` and drop classes of `ServiceBusQueueTemplate` and `ServiceBusTopicTemplate`.
- Drop `RxJava` and `CompletableFuture` support of ServiceBusTemplate and support `Reactor` instead.
- Drop interface of `ServiceBusQueueOperation` and `ServiceBusTopicOperation`.
- Drop API of `ServiceBusQueueOperation#abandon` and `ServiceBusQueueOperation#deadletter`.
- Combine the original `ServiceBusQueueTemplate#subscribe` and `ServiceBusTopicTemplate#subscribe` as `ServiceBusProcessorClient#subscribe`.
- Deprecate the interface of `SubscribeOperation`.
- Add new API of `setDefaultEntityType` for ServiceBusTemplate, the default entity type of a ServiceBusTemplate is required when no bean of `PropertiesSupplier<String, ProducerProperties>` is provided for the `ProducerProperties#entityType`.
- Drop class of `ServiceBusQueueInboundChannelAdapter` and `ServiceBusTopicInboundChannelAdapter` and combine them as `ServiceBusInboundChannelAdapter`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`

#### Features Added

- Provide the ability to connect to multiple Azure Service Bus entities in different namespaces.

### spring-messaging-azure

#### Breaking Changes

- Annotation of `@AzureMessageListeners`, `@AzureMessageListener` and `@EnableAzureMessaging` are dropped.
- Change artifact id from `azure-spring-cloud-messaging` to `spring-messaging-azure`.

### spring-cloud-azure-starter-servicebus-jms

#### Breaking Changes

- Change artifact id from `azure-spring-boot-starter-servicebus-jms` to `spring-cloud-azure-starter-servicebus-jms`.

### spring-integration-azure-storage-queue

#### Breaking Changes

- Change artifact id from `azure-spring-integration-storage-queue` to `spring-integration-azure-storage-queue`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`.
- Class of `StorageQueueMessageSource` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource`.
- Class of `StorageQueueOperation` is moved from `com.azure.spring.integration.storage.queue.StorageQueueOperation` to package `com.azure.spring.storage.queue.core.StorageQueueOperation`.
- Class of `StorageQueueTemplate` is moved from `com.azure.spring.integration.storage.queue.StorageQueueTemplate` to package `com.azure.spring.storage.queue.core.StorageQueueTemplate`.

### spring-cloud-azure-starter-integration-storage-queue

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-storage-queue` to `spring-cloud-azure-starter-integration-storage-queue`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`.
- Class of `StorageQueueMessageSource` is moved from `com.azure.spring.integration.storage.queue.inbound` to package `com.azure.spring.integration.storage.queue.inbound.StorageQueueMessageSource`.
- Class of `StorageQueueOperation` is moved from `com.azure.spring.integration.storage.queue.StorageQueueOperation` to package `com.azure.spring.storage.queue.core.StorageQueueOperation`.
- Class of `StorageQueueTemplate` is moved from `com.azure.spring.integration.storage.queue.StorageQueueTemplate` to package `com.azure.spring.storage.queue.core.StorageQueueTemplate`.

### spring-cloud-azure-trace-sleuth

#### Features Added

- Support http pipeline policy implemented by Spring Cloud Sleuth API. ([#24192])

[Spring-Cloud-Azure-Migration-Guide-for-4.0]: https://microsoft.github.io/spring-cloud-azure/docs/4.0.0-beta.2/reference/html/appendix.html#migration-guide-for-4-0
[#24192]: https://github.com/Azure/azure-sdk-for-java/pull/24192
