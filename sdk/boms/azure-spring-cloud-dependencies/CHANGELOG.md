# Release History

## 2.8.0 (Unreleased)
This release is compatible with Spring Boot 2.5.0 - 2.5.4 and Spring Cloud 2020.0.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).

## 2.7.0 (2021-07-20)
This release is compatible with Spring Boot 2.5 and Spring Cloud 2020.0.3.
### Dependency Upgrades
- Upgrade to [spring-cloud-dependencies:2020.0.3](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.3/spring-cloud-dependencies-2020.0.3.pom).
- Upgrade to [Azure Core 1.18.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/CHANGELOG.md#1180-2021-07-01).
- Upgrade to [Azure Identity 1.3.3](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/CHANGELOG.md).
- Upgrade to [Azure Service Bus 7.3.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/servicebus/azure-messaging-servicebus/CHANGELOG.md#730-2021-07-08).
- Upgrade to [Azure Event Hubs 5.9.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/CHANGELOG.md#590-2021-07-09).
- Upgrade to [Azure Event Hubs Checkpoint Store Blob 1.8.1](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs-checkpointstore-blob/CHANGELOG.md#181-2021-07-09).  
- Upgrade to [Spring Cloud Azure App Configuration 2.0.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-spring-cloud-appconfiguration-config/CHANGELOG.md).
- Upgrade to [Spring Cloud Azure Feature Management 2.0.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-spring-cloud-feature-management/CHANGELOG.md#200-2021-06-21).
- Upgrade to [Spring Cloud Azure Feature Management Web 2.0.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/appconfiguration/azure-spring-cloud-feature-management-web/CHANGELOG.md#200-2021-06-21).

## 2.6.0 (2021-06-23)
This release is compatible with Spring Cloud 2020.0.2. 
### Breaking Changes
- Remove `azure-spring-cloud-telemetry` module dependency.

## 2.5.0 (2021-05-24)
This release is compatible with Spring Cloud 2020.0.2. 

## 2.4.0 (2021-04-23)
This release is compatible with Spring Cloud 2020.0.2. 

### Breaking Changes
- Update `com.azure` group id to `com.azure.spring`.
- Update `spring-cloud-azure-dependencies` artifact id to `azure-spring-cloud-dependencies`.
- Remove `spring-boot-dependencies` and `spring-cloud-dependencies` dependencies.

### Dependency Upgrades
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).
