# Release History

## 2.9.0-beta.1 (Unreleased)
This release is compatible with Spring Boot 2.5.0 - 2.5.4 and Spring Cloud 2020.0.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).

### Features Added

### Breaking Changes
- Change the value type of ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME from Instant to OffsetDateTime.

### Bugs Fixed
- Fix the bug of ClassCastException when forward Service Bus messages with the header of schedule enqueued time.

### Other Changes

## 2.8.0 (2021-08-25)
This release is compatible with Spring Boot 2.5.0 - 2.5.3 and Spring Cloud 2020.0.3.
### Key Bug Fixes
- Fixed service bus cleint factory destroyed, resource not released bug.([#23195](https://github.com/Azure/azure-sdk-for-java/pull/23195))
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).
### Breaking Changes
- Override paritionkey when session id is set. ([#23135](https://github.com/Azure/azure-sdk-for-java/pull/23135))
- Adjust the order of different partition key header. ([#23135](https://github.com/Azure/azure-sdk-for-java/pull/23135))
- Deprecate the `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer.concurrency` property,
  use `maxConcurrentSessions` and `maxConcurrentCalls` to set the properties.
### New Features
- Support configuration of `retryOptions` for ServiceBusClientBuilder with property of `spring.cloud.azure.servicebus.retry-options`, these parameters can be modified: `maxRetries`, `delay`, `maxDelay`, `tryTimeout`, `Mode`.
- Support configuration of `maxConcurrentCalls` for ServiceBusClientConfig with property of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer.maxConcurrentCalls`.
- Support configuration of `maxConcurrentSessions` for ServiceBusClientConfig with property of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer.maxConcurrentSessions`.
- Support configuration of `serviceBusReceiveMode` for ServiceBusClientConfig with property of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer.serviceBusReceiveMode`, supported values are `PEEK_LOCK` and `RECEIVE_AND_DELETE`.
- Support configuration of `enableAutoComplete` for ServiceBusClientConfig with property of `spring.cloud.stream.servicebus.queue.bindings.<channelName>.consumer.enableAutoComplete`.

## 2.7.0 (2021-07-20)
### Key Bug Fixes
- Fix bug of setting message headers repeatedly with different value types. ([#22939](https://github.com/Azure/azure-sdk-for-java/pull/22939))

### New Features
- Support configuration of `AmqpTransportType` for ServiceBusClientBuilder with property of `spring.cloud.azure.servicebus.transportType`, supported values are `AMQP` and `AMQP_WEB_SOCKETS`.

### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.2/spring-boot-dependencies-2.5.2.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.3](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.3/spring-cloud-dependencies-2020.0.3.pom).


## 2.6.0 (2021-06-23)
### Breaking Changes
- Removed `TelemetryCollector` usage.


## 2.5.0 (2021-05-24)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).



## 2.4.0 (2021-04-19)


## 2.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).
- Support setting service-bus message-id ([#20005](https://github.com/Azure/azure-sdk-for-java/issues/20005)).

## 2.2.0 (2021-03-03)


## 2.1.0 (2021-01-20)


## 2.0.0 (2020-12-30)
### Breaking Changes
- Remove the `spring.cloud.azure.credential-file-path` property.
- Deprecated the `spring.cloud.azure.managed-identity.client-id` property,
  use `spring.cloud.azure.client-id` to set the managed identity id when using Managed Identity.

## 2.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-servicebus-topic-stream-binder` to `azure-spring-cloud-stream-binder-servicebus-topic`.

## 1.2.8 (2020-09-14)
### New Features
 - Enable scheduled enqueue message in Service Bus binders
