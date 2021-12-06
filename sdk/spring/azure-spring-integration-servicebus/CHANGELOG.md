# Release History

## 2.12.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.11.0 (2021-11-24)

This release is compatible with Spring Boot 2.5.0 - 2.5.4 and Spring Cloud 2020.0.3.
### Dependency Upgrades
Regular updates for Azure SDK dependency versions.

## 2.10.0 (2021-11-02)
### Other Changes
- Deprecate CompletableFuture support APIs, which will be replaced by Reactor Core from version 4.0.0.

## 2.9.0 (2021-09-27)
This release is compatible with Spring Boot 2.5.0 - 2.5.4 and Spring Cloud 2020.0.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).
### Breaking Changes
- Change the value type of ServiceBusMessageHeaders.SCHEDULED_ENQUEUE_TIME from Instant to OffsetDateTime.
### Bugs Fixed
- Fix the bug of ClassCastException when forward Service Bus messages with the header of schedule enqueued time.


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
- Upgrade to [Spring Integration 5.5.1](https://mvnrepository.com/artifact/org.springframework.integration/spring-integration-core/5.5.1).
- Upgrade to [Azure Service Bus 7.3.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/CHANGELOG.md#730-2021-07-08).

## 2.6.0 (2021-06-23)
### New Features
- Upgrade to [spring-boot-dependencies:2.5.0](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.0/spring-boot-dependencies-2.5.0.pom).

## 2.5.0 (2021-05-24)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).



## 2.4.0 (2021-04-19)


## 2.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).
- Upgrade to `Spring Integration` [5.4.4](https://github.com/spring-projects/spring-integration/releases/tag/v5.4.4).

## 2.2.0 (2021-03-03)
### New Features
 - Support `ServiceBusMessageConverter` as a bean to support customize `ObjectMapper`.

## 2.1.0 (2021-01-20)


## 2.0.0 (2020-12-30)
### Breaking Changes
- Remove the `spring.cloud.azure.credential-file-path` property.
- Deprecated the `spring.cloud.azure.managed-identity.client-id` property,
  use `spring.cloud.azure.client-id` to set the managed identity id when using Managed Identity.

## 2.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-integration-servicebus` to `azure-spring-integration-servicebus`.

## 1.2.8 (2020-09-14)
### New Features
 - Enable scheduled enqueue message in Service Bus binders
