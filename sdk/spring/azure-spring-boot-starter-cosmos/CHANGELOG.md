# Release History

## 3.8.0-beta.1 (Unreleased)
This release is compatible with Spring Boot 2.5.0 - 2.5.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).


## 3.7.0 (2021-07-20)
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.2/spring-boot-dependencies-2.5.2.pom).
- Upgrade to [Azure Spring Data Cosmos 3.9.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos/CHANGELOG.md#390-2021-07-08).

## 3.6.0 (2021-06-23)
### Breaking Changes

### Deprecations
- Deprecate `allowTelemetry` configuration item.

## 3.5.0 (2021-05-24)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).



## 3.4.0 (2021-04-19)


## 3.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).
- Upgrade to `azure-spring-data-cosmos` [3.5.0](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/cosmos/azure-spring-data-cosmos/CHANGELOG.md#350-2021-03-11)

## 3.2.0 (2021-03-03)


## 3.1.0 (2021-01-20)


## 3.0.0 (2020-12-30)


## 3.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Updated properties prefix from `azure.cosmosdb.xxx` to `azure.cosmos.xxx`.
- Added new property items `azure.cosmos.connection-mode`.
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-cosmosdb-spring-boot-starter` to `azure-spring-boot-starter-cosmos`.

## 2.3.5 (2020-09-14)
### Breaking Changes
- Unify spring-boot-starter version

## 2.3.3 (2020-08-13)
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
