# Release History

## 4.0.0-beta.4 (Unreleased)
- Support Spring Boot version: 2.5.5+ and 2.6.0 - 2.6.3.
- Support Spring Cloud version: 2020.0.3+ and 2021.0.0.

### Dependency Updates
Upgrade dependency according to spring-boot-dependencies:2.6.3 and spring-cloud-dependencies:2021.0.0.

### Spring Cloud Azure Resource Manager
This section includes changes in the `spring-cloud-azure-resourcemanager` module.

#### Bugs Fixed
- Fix the bug that the auto-created consumer group takes the name of Event Hub [#26622](https://github.com/Azure/azure-sdk-for-java/pull/26622).

### Spring Cloud Stream Service Bus Binder
This section includes changes in `spring-cloud-azure-stream-binder-servicebus` module.

#### Breaking Changes
- Change the type of the binding producer property of `send-timeout` from `long` to `Duration` [#26625](https://github.com/Azure/azure-sdk-for-java/pull/26625).
- Change property from `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.session-aware` to `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.session-enabled` [#27331](https://github.com/Azure/azure-sdk-for-java/pull/27331).
- Unify the root package name of Spring libraries. [#27420](https://github.com/Azure/azure-sdk-for-java/pull/27420).
- Remove message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Change the property of `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.checkpoint-mode` to `spring.cloud.stream.servicebus.bindings.<binding-name>.consumer.auto-complete`. 
To disable the auto-complete mode is equivalent to `MANUAL` checkpoint mode and to enable it will trigger the `RECORD` mode [#27646](https://github.com/Azure/azure-sdk-for-java/pull/27646).


#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).

### Spring Cloud Azure Starter Integration Service Bus
This section includes changes in the `spring-cloud-azure-starter-integration-servicebus` module.

#### Breaking Changes
- Remove message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Drop class `CheckpointConfig`. To set the checkpoint configuration for `ServiceBusInboundChannelAdapter`, 
users can call the method `ServiceBusContainerProperties#setAutoComplete` instead. To disable the auto-complete mode is 
equivalent to `MANUAL` checkpoint mode and to enable it will trigger the `RECORD` mode [#27646](https://github.com/Azure/azure-sdk-for-java/pull/27646).

#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).

### Spring Integration Azure Service Bus
This section includes changes in the `spring-integration-azure-servicebus` module.

#### Breaking Changes
- Remove message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).
- Drop class `CheckpointConfig`. To set the checkpoint configuration for `ServiceBusInboundChannelAdapter`, 
users can call the method `ServiceBusContainerProperties#setAutoComplete` instead. To disable the auto-complete mode is 
equivalent to `MANUAL` checkpoint mode and to enable it will trigger the `RECORD` mode [#27646](https://github.com/Azure/azure-sdk-for-java/pull/27646).

#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- Change the type of the binding producer property of `send-timeout` from `long` to `Duration` [#26625](https://github.com/Azure/azure-sdk-for-java/pull/26625).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_`.

### Spring Cloud Azure Starter Integration Event Hubs
This section includes changes in the `spring-cloud-azure-starter-integration-eventhubs` module.

#### Breaking Changes
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_`.

### Spring Integration Azure Event Hubs
This section includes changes in the `spring-integration-azure-eventhubs` module.

#### Breaking Changes
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_`.

### Spring Cloud Azure Event Hubs Starter
This section includes changes in `spring-cloud-azure-starter-eventhubs` module.

#### Breaking Changes
- Remove property of `spring.cloud.azure.eventhubs.processor.partition-ownership-expiration-interval` which can be replaced by
`spring.cloud.azure.eventhubs.processor.load-balancing.partition-ownership-expiration-interval` [#27331](https://github.com/Azure/azure-sdk-for-java/pull/27331).

### Spring Messaging Azure

#### Breaking Changes
- Move class `com.azure.spring.messaging.PartitionSupplier` to library com.azure.spring:spring-messaging-azure-eventhubs, which is `com.azure.spring.messaging.eventhubs.core.PartitionSupplier` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).

### Spring Messaging Azure Event Hubs

#### Breaking Changes
- Change class from `com.azure.spring.messaging.PartitionSupplier` to `com.azure.spring.messaging.eventhubs.core.PartitionSupplier` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Remove parameter of `PartitionSupplier` from the sending API for a single message in `EventHubsTemplate`. 
Please use message headers of `com.azure.spring.messaging.AzureHeaders.PARTITION_ID` and `com.azure.spring.messaging.AzureHeaders.PARTITION_KEY` instead [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Change the message header prefix from `azure_eventhub` to `azure_eventhubs_`.

### Spring Messaging Azure Service Bus

#### Breaking Changes
- Remove parameter of `PartitionSupplier` from the sending API for a single message in `ServiceBusTemplate`.
Please use message header of `com.azure.spring.messaging.AzureHeaders.PARTITION_KEY` instead [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).
- Remove message header of `AzureHeaders.RAW_ID`. Please use `ServiceBusMessageHeaders.MESSAGE_ID` instead [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).

#### Features Added
- Support converting all headers and properties exposed directly by `ServiceBusReceivedMessage` when receiving messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675), newly supported headers and properties can be get according to the keys of:
  * ServiceBusMessageHeaders.DEAD_LETTER_ERROR_DESCRIPTION
  * ServiceBusMessageHeaders.DEAD_LETTER_REASON
  * ServiceBusMessageHeaders.DEAD_LETTER_SOURCE
  * ServiceBusMessageHeaders.DELIVERY_COUNT
  * ServiceBusMessageHeaders.ENQUEUED_SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.ENQUEUED_TIME
  * ServiceBusMessageHeaders.EXPIRES_AT
  * ServiceBusMessageHeaders.LOCK_TOKEN
  * ServiceBusMessageHeaders.LOCKED_UNTIL
  * ServiceBusMessageHeaders.SEQUENCE_NUMBER
  * ServiceBusMessageHeaders.STATE
  * ServiceBusMessageHeaders.SUBJECT
- Support the message header of `ServiceBusMessageHeaders.SUBJECT` to specify the AMQP property of `subject` when sending messages [#27675](https://github.com/Azure/azure-sdk-for-java/pull/27675).

### Spring Messaging Azure Storage Queue

#### Breaking Changes
- Remove parameter of `PartitionSupplier` from the sending API for a single message in `StorageQueueTemplate` [#27422](https://github.com/Azure/azure-sdk-for-java/issues/27422).

## 4.0.0-beta.3 (2022-01-18)
Please refer to [Spring Cloud Azure Migration Guide for 4.0](https://microsoft.github.io/spring-cloud-azure/4.0.0-beta.3/4.0.0-beta.3/reference/html/appendix.html#migration-guide-for-4-0) to learn how to migrate to version 4.0.

- Support Spring Boot version: 2.5.5+ and 2.6.0 - 2.6.1.
- Support Spring Cloud version: 2020.0.3+ and 2021.0.0.

### Dependency Updates
Upgrade dependency according to spring-boot-dependencies:2.6.1 and spring-cloud-dependencies:2021.0.0.

### Spring Cloud Azure AutoConfigure
This section includes changes in the `spring-cloud-azure-autoconfigure` module.

#### Features Added
- Enable `Caching` and `Pooling` ConnectionFactory autoconfiguration for Service Bus JMS [#26072](https://github.com/Azure/azure-sdk-for-java/issues/26072).
- Support all configuration options of the initial position when processing an event hub [#26434](https://github.com/Azure/azure-sdk-for-java/issues/26434).
- Support autoconfiguration of `QueueClient` by adding configuration property `spring.cloud.azure.storage.queue.queue-name` [#26382](https://github.com/Azure/azure-sdk-for-java/pull/26382).
- Improve the `spring-configuration-metadata.json` [#26292](https://github.com/Azure/azure-sdk-for-java/issues/26292), [#26274](https://github.com/Azure/azure-sdk-for-java/pull/26274).
- Support `StorageQueueMessageConverter` as a bean to support customize ObjectMapper [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).
- Support `EventHubsMessageConverter` as a bean to support customize ObjectMapper [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).

#### Breaking Changes
- Change AAD configuration properties to use the namespace for credential and environment properties [#25646](https://github.com/Azure/azure-sdk-for-java/issues/25646).
  * Property name "spring.cloud.azure.active-directory.tenant-id" changed to "spring.cloud.azure.active-directory.profile.tenant-id".
  * Property name "spring.cloud.azure.active-directory.client-id" changed to "spring.cloud.azure.active-directory.credential.client-id".
  * Property name "spring.cloud.azure.active-directory.client-secret" changed to "spring.cloud.azure.active-directory.credential.client-secret".
  * Property name "spring.cloud.azure.active-directory.base-uri" changed to "spring.cloud.azure.active-directory.profile.environment.active-directory-endpoint".
  * Property name "spring.cloud.azure.active-directory.graph-base-uri" changed to "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint".
  * Property name "spring.cloud.azure.active-directory.graph-membership-uri" changed to "spring.cloud.azure.active-directory.profile.environment.microsoft-graph-endpoint" and "spring.cloud.azure.active-directory.user-group.use-transitive-members".
- Change AAD B2C configuration properties to use the namespace for credential and environment properties [#25799](https://github.com/Azure/azure-sdk-for-java/pull/25799).
- Change Event Hubs processor configuration properties `spring.cloud.azure.eventhbs.processor.partition-ownership-expiration-interval` to `spring.cloud.azure.eventhbs.processor.load-balancing.partition-ownership-expiration-interval` [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
- Change Event Hubs configuration properties `spring.cloud.azure.eventhubs.fqdn` to `spring.cloud.azure.eventhubs.fully-qualified-namespace` [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
- Rename all `*CP` classes to `*ConfigurationProperties` [#26209](https://github.com/Azure/azure-sdk-for-java/pull/26209).

#### Bugs Fixed
- Fix global HTTP client properties `spring.cloud.azure.client.http.*` not set correctly [#26190](https://github.com/Azure/azure-sdk-for-java/issues/26190).
- Fix Cosmos properties `spring.cloud.azure.cosmos.proxy.*` not set correctly [#25690](https://github.com/Azure/azure-sdk-for-java/issues/25690).
- Fix `PoolAcquirePendingLimitException` when using `EventProcessorClient` or `spring-cloud-azure-stream-binder-eventhubs` [#26027](https://github.com/Azure/azure-sdk-for-java/issues/26027).

### Spring Cloud Stream Event Hubs Binder
This section includes changes in `spring-cloud-azure-stream-binder-eventhubs` module.

#### Breaking Changes
- For the batch consuming mode, change the message header names converted from batched messages [#26291](https://github.com/Azure/azure-sdk-for-java/pull/26291).
  * Change message header from `azure_eventhub_enqueued_time` to `azure_eventhub_batch_converted_enqueued_time`.
  * Change message header from `azure_eventhub_offset` to `azure_eventhub_batch_converted_offset`.
  * Change message header from `azure_eventhub_sequence_number` to `azure_eventhub_batch_converted_sequence_number`.
  * Change message header from `azure_partition_key` to `azure_batch_converted_partition_key`.
- When publish messages to Event Hubs, ignore all message headers converted from batched messages [#26213](https://github.com/Azure/azure-sdk-for-java/issues/26213).

  Headers include:
  * `azure_eventhub_batch_converted_enqueued_time`
  * `azure_eventhub_batch_converted_offset`
  * `azure_eventhub_batch_converted_sequence_number`
  * `azure_batch_converted_partition_key`
  * `azure_eventhub_batch_converted_system_properties`
  * `azure_eventhub_batch_converted_application_properties`
- Expose message header `azure_eventhub_last_enqueued_event_properties` [#25960](https://github.com/Azure/azure-sdk-for-java/issues/25960).
- Improve logging information when converting between `com.azure.messaging.eventhubs.EventData` and `org.springframework.messaging.Message` [#26291](https://github.com/Azure/azure-sdk-for-java/pull/26291).
- Fix `PoolAcquirePendingLimitException` when using `EventHubsProcessorContainer` [#26027](https://github.com/Azure/azure-sdk-for-java/issues/26027).
### Spring Integration Event Hubs

This section includes changes in `spring-messaging-azure-eventhubs` and `spring-integration-azure-eventhubs` modules.

#### Features Added
- Expose message header `azure_eventhub_last_enqueued_event_properties` in `spring-integration-azure-eventhubs` module [#25960](https://github.com/Azure/azure-sdk-for-java/issues/25960).

#### Breaking Changes
- Refactor `spring-messaging-azure-eventhubs` message converter [#26291](https://github.com/Azure/azure-sdk-for-java/pull/26291).
  * Rename `EventHubBatchMessageConverter` to `EventHubsBatchMessageConverter`.
  * Refactor `EventHubsBatchMessageConverter` by disabling the ability of converting Spring Message to EventData.
  * Improve logging information when converting between `com.azure.messaging.eventhubs.EventData` and `org.springframework.messaging.Message`.
- Change `DefaultEventHubsNamespaceProcessorFactory` constructor parameter type from `PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>` to `PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>` [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).

#### Bugs Fixed
- Fix `PoolAcquirePendingLimitException` when using `EventHubsProcessorContainer` [#26027](https://github.com/Azure/azure-sdk-for-java/issues/26027).
- Fix `EventHubsTemplate` not sending all messages in a collection [#24445](https://github.com/Azure/azure-sdk-for-java/issues/24445).


### Spring Integration Service Bus

This section includes changes in `spring-messaging-azure-servicebus` and `spring-integration-azure-servicebus` modules.

#### Breaking Changes
- Change `DefaultServiceBusNamespaceProcessorFactory` constructor parameter type from `PropertiesSupplier<Tuple2<String, String>, ProcessorProperties>` to `PropertiesSupplier<ConsumerIdentifier, ProcessorProperties>` [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).
- Refactor `ServiceBusInboundChannelAdapter` constructors [#26200](https://github.com/Azure/azure-sdk-for-java/pull/26200).

### Spring Integration Storage Queue
This section includes changes in `spring-messaging-azure-storage-queue` and `spring-integration-azure-storage-queue` modules.

#### Breaking Changes

- Refactor `spring-messaging-azure-storage-queue` module [#26273](https://github.com/Azure/azure-sdk-for-java/issues/26273).
  * Remove `StorageQueueOperation`.
  * Remove configuration of checkpoint mode for `StorageQueueTemplate`, and support only MANUAL mode.
  * Remove auto creating Storage Queue when send/receive messages via `StorageQueueTemplate`.

### Spring Cloud Azure Core

This section includes changes in `spring-cloud-azure-core`, `spring-cloud-azure-service`, and `spring-cloud-azure-resourcemanager` modules.

#### Breaking Changes
- Refactor `spring-cloud-azure-core` module [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
  * Support JPMS.
- Refactor `spring-cloud-azure-service` module [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
  * Move all `*BuilderFactory` to `*.implementation.*` packages [#26404](https://github.com/Azure/azure-sdk-for-java/issues/26404).
  * Support JPMS [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
- Refactor `spring-cloud-azure-resourcemanager` module [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).
  * Rename `com.azure.spring.resourcemanager.provisioner` to `com.azure.spring.resourcemanager.provisioning` [#26472](https://github.com/Azure/azure-sdk-for-java/pull/26472).
  * Support JPMS [#25851](https://github.com/Azure/azure-sdk-for-java/pull/25851).

#### Bugs Fixed
- Fix exception thrown by `AzureStorageFileProtocolResolver` and `AzureStorageBlobProtocolResolver` when target file, directory or container does not exist [#25916](https://github.com/Azure/azure-sdk-for-java/issues/25916).

### Other Changes
- Refactor the test structure and increase the test coverage [#23773](https://github.com/Azure/azure-sdk-for-java/issues/23773), [#26175](https://github.com/Azure/azure-sdk-for-java/issues/26175).
- Improve Java code documentation [#24332](https://github.com/Azure/azure-sdk-for-java/issues/24332).
- Improve User-Agent headers for Spring Cloud Azure libraries [#25892](https://github.com/Azure/azure-sdk-for-java/issues/25892), [#26122](https://github.com/Azure/azure-sdk-for-java/pull/26122).
- Improve [Sample Projects](https://github.com/Azure-Samples/azure-spring-boot-samples/tree/spring-cloud-azure_4.0.0-beta.3).
  * Switch to `DefaultAzureCredential` [#25652](https://github.com/Azure/azure-sdk-for-java/issues/25652).
  * Provision resources using **Terraform** [#25652](https://github.com/Azure/azure-sdk-for-java/issues/25652).
  * Unify names of Sample Projects [#26308](https://github.com/Azure/azure-sdk-for-java/issues/26308).
- Improve [Spring Cloud Azure Reference Doc](https://microsoft.github.io/spring-cloud-azure/4.0.0-beta.3/4.0.0-beta.3/reference/html/index.html).
  * Make sure all contents were in original README files are included [#25921](https://github.com/Azure/azure-sdk-for-java/issues/25921).
  * Add RBAC description [#25973](https://github.com/Azure/azure-sdk-for-java/issues/25973).

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
- Drop `RxJava` support of `EventHubRxOperation` and `EventHubRxTemplate` and support `Reactor` only.
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
- Drop `RxJava` support of `EventHubRxOperation` and `EventHubRxTemplate` and support `Reactor` only.
- Drop `EventHubOperation`, and move its `subscribe` API to class of `EventHubsProcessorContainer`.
- Rename `EventHubsInboundChannelAdapter` as `EventHubsInboundChannelAdapter` to keep consistent with the service of
  Azure
  Event Hubs, and change constructor signature as well.

* Change `CheckpointConfig` instantiation style to simple constructor instead of build style.

### spring-cloud-azure-starter-integration-servicebus

#### Breaking Changes

- Change artifact id from `azure-spring-cloud-starter-servicebus` to `spring-cloud-azure-starter-integration-servicebus`.
- Combine the original `ServiceBusQueueTemplate#sendAsync` and `ServiceBusTopicTemplate#sendAsync` as `ServiceBusTemplate#sendAsync` and drop classes of `ServiceBusQueueTemplate` and `ServiceBusTopicTemplate`.
- Drop `CompletableFuture` support of ServiceBusTemplate and support `Reactor` instead.
- Drop interface of `ServiceBusQueueOperation` and `ServiceBusTopicOperation`.
- Drop API of `ServiceBusQueueOperation#abandon` and `ServiceBusQueueOperation#deadletter`.
- Combine the original `ServiceBusQueueTemplate#subscribe` and `ServiceBusTopicTemplate#subscribe` as `ServiceBusProcessorClient#subscribe`.
- Deprecate the interface of `SubscribeOperation`.
- Add new API of `setDefaultEntityType` for ServiceBusTemplate, the default entity type of `ServiceBusTemplate` is required when no bean of `PropertiesSupplier<String, ProducerProperties>` is provided for the `ProducerProperties#entityType`.
- Drop class of `ServiceBusQueueInboundChannelAdapter` and `ServiceBusTopicInboundChannelAdapter` and combine them as `ServiceBusInboundChannelAdapter`.
- Class of `DefaultMessageHandler` is moved from `com.azure.spring.integration.core` to package `com.azure.spring.integration.handler`

#### Features Added

- Provide the ability to connect to multiple Azure Service Bus entities in different namespaces.

### spring-integration-azure-servicebus

#### Breaking Changes

- Change artifact id from `azure-spring-integration-servicebus` to `spring-integration-azure-servicebus`.
- Combine the original `ServiceBusQueueTemplate#sendAsync` and `ServiceBusTopicTemplate#sendAsync` as `ServiceBusTemplate#sendAsync` and drop classes of `ServiceBusQueueTemplate` and `ServiceBusTopicTemplate`.
- Drop and `CompletableFuture` support of ServiceBusTemplate and support `Reactor` instead.
- Drop interface of `ServiceBusQueueOperation` and `ServiceBusTopicOperation`.
- Drop API of `ServiceBusQueueOperation#abandon` and `ServiceBusQueueOperation#deadletter`.
- Combine the original `ServiceBusQueueTemplate#subscribe` and `ServiceBusTopicTemplate#subscribe` as `ServiceBusProcessorClient#subscribe`.
- Deprecate the interface of `SubscribeOperation`.
- Add new API of `setDefaultEntityType` for ServiceBusTemplate, the default entity type of `ServiceBusTemplate` is required when no bean of `PropertiesSupplier<String, ProducerProperties>` is provided for the `ProducerProperties#entityType`.
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
