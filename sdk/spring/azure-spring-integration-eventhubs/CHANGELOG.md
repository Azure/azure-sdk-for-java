# Release History

## 2.9.0 (2021-09-27)
This release is compatible with Spring Boot 2.5.0 - 2.5.4 and Spring Cloud 2020.0.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).


## 2.8.0 (2021-08-25)
This release is compatible with Spring Boot 2.5.0 - 2.5.3 and Spring Cloud 2020.0.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).


## 2.7.0 (2021-07-20)
### Key Bug Fixes
- Fixed `EventHubMessageConverter` to load all system properties of `EventData` and put in the header of org.springframework.messaging.Message.([#22683](https://github.com/Azure/azure-sdk-for-java/pull/22683/))
### Dependency Upgrades
- Upgrade to [Spring Integration 5.5.1](https://mvnrepository.com/artifact/org.springframework.integration/spring-integration-core/5.5.1).
- Upgrade to [Azure Event Hubs 5.9.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/CHANGELOG.md#590-2021-07-09).

### Breaking Changes
- Encode message payload with UTF-8 charset instead of default charset of JVM when the payload is String. ([#23056](https://github.com/Azure/azure-sdk-for-java/pull/23056))

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


## 2.1.0 (2021-01-20)


## 2.0.0 (2020-12-30)
### Breaking Changes
- Remove the `spring.cloud.azure.credential-file-path` property.
- Deprecated the `spring.cloud.azure.managed-identity.client-id` property,
  use `spring.cloud.azure.client-id` to set the managed identity id when using Managed Identity.
  
## 2.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-integration-eventhubs` to `azure-spring-integration-eventhubs`.

## 1.2.8 (2020-09-14)
### Breaking Changes
- Unify Spring Cloud for Azure packages version
