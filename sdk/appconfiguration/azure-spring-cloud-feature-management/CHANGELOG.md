# Release History

## 2.8.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.7.0 (2022-06-29)
- This release is compatible with Spring Boot 2.5.0-2.5.14, 2.6.0-2.6.9, 2.7.0-2.7.1. (Note: 2.5.x (x>14), 2.6.y (y>9) and 2.7.z (z>1) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.3. (Note: 2020.0.x (x>5) and 2021.0.y (y>3) should be supported, but they aren't tested with this release.)

### Dependency Upgrades
- Upgrade azure-sdk's version to latest released version.

## 2.6.0 (2022-05-24)
- This release is compatible with Spring Boot 2.5.0-2.5.13, 2.6.0-2.6.7. (Note: 2.5.x (x>13) and 2.6.y (y>7) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.2. (Note: 2020.0.x (x>5) and 2021.0.y (y>2) should be supported, but they aren't tested with this release.)

### Dependency Upgrades
- Upgrade azure-sdk's version to latest released version.

## 2.5.0 (2022-04-29)
- This release is compatible with Spring Boot 2.5.0-2.5.13, 2.6.0-2.6.7. (Note: 2.5.x (x>13) and 2.6.y (y>7) should be supported, but they aren't tested with this release.)
- This release is compatible with Spring Cloud 2020.0.3-2020.0.5, 2021.0.0-2021.0.2. (Note: 2020.0.x (x>5) and 2021.0.y (y>2) should be supported, but they aren't tested with this release.)

### Dependency Upgrades
- Regular updates for Azure SDK dependency versions.
- Upgrade external dependencies' version according to [spring-boot-dependencies:2.6.6](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.6/spring-boot-dependencies-2.6.6.pom) to address [CVE-2022-22965](https://github.com/advisories/GHSA-36p3-wjmg-h94x) [#28280](https://github.com/Azure/azure-sdk-for-java/pull/28280).
- Upgrade external dependencies' version according to [spring-cloud-dependencies:2021.0.2](https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2021.0.2/spring-cloud-dependencies-2021.0.2.pom) to address [CVE-2022-22963](https://github.com/advisories/GHSA-6v73-fgf6-w5j7) [#28179](https://github.com/Azure/azure-sdk-for-java/issues/28179).

## 2.4.0 (2022-03-28)
This release is compatible with Spring Boot 2.5.0-2.5.11, 2.6.0-2.6.5.

### Features Added
- Updated PercentageFilter to support Strings and Doubles.

### Dependency Upgrades
- Regular updates for Azure SDK dependency versions.
- Upgrade external dependencies' version according to [spring-boot-dependencies:2.6.3](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.3/spring-boot-dependencies-2.6.3.pom) and [spring-cloud-dependencies:2021.0.1](https://repo1.maven.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2021.0.1/).

## 2.3.0 (2022-03-01)
This release is compatible with Spring Boot 2.5.5-2.5.8, 2.6.0-2.6.2.

### Dependency Upgrades
- Regular updates for Azure SDK dependency versions.
- Upgrade external dependencies' version according to [spring-boot-dependencies:2.6.2](https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/2.6.2/spring-boot-dependencies-2.6.2.pom).

## 2.2.0 (2022-01-06)
This release is compatible with Spring Boot 2.5.5-2.5.8, 2.6.0-2.6.1.

## 2.1.0 (2021-11-25)

- Regular updates for dependency versions.

## 2.0.1 (2021-09-28)

- Updated to JUnit 5

## 2.0.0 (2021-06-21)

- Updated TimeWindowFilter to support ISO-8601

### Breaking Change

- Changed package path to `com.azure.spring.cloud.feature.manager`

## 2.0.0-beta.1 (2021-05-04)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `spring-cloud-azure-feature-management` to `azure-spring-cloud-feature-management`.
- New Targeting Feature filter has been added.
