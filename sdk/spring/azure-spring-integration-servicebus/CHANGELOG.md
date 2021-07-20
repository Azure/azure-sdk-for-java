# Release History

## 2.7.0-beta.1 (Unreleased)
### Key Bug Fixes
- Fix bug of setting message headers repeatedly with different value types. ([#22939](https://github.com/Azure/azure-sdk-for-java/pull/22939))
### New Features
- Support configuration of `AmqpTransportType` for ServiceBusClientBuilder with property of `spring.cloud.azure.servicebus.transportType`, supported values are `AMQP` and `AMQP_WEB_SOCKETS`.


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
