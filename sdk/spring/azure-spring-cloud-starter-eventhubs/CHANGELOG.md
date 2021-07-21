# Release History

## 2.7.0-beta.1 (Unreleased)
### Key Bug Fixes
- Fixed `EventHubMessageConverter` to load all system properties of `EventData` and put in the header of org.springframework.messaging.Message.([#22683](https://github.com/Azure/azure-sdk-for-java/pull/22683/))

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
- Upgrade to `azure-messaging-eventhubs` [5.6.0](https://github.com/azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs/CHANGELOG.md#560-2021-03-10).

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
- Change artifact id from `spring-cloud-starter-azure-eventhubs` to `azure-spring-cloud-starter-eventhubs`.

## 1.2.8 (2020-09-14)
### Key Bug Fixes
 - Fixed the repeated consumption of Event Hubs messages when the checkpoint mode is BATCH
