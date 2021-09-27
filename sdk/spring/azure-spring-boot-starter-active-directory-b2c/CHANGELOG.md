# Release History

## 3.9.0 (2021-09-27)
This release is compatible with Spring Boot 2.5.0 - 2.5.4.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.4](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.4/spring-boot-dependencies-2.5.4.pom).


## 3.8.0 (2021-08-25)
This release is compatible with Spring Boot 2.5.0 - 2.5.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).
### Deprecations
- Deprecate `AADB2CJwtBearerTokenAuthenticationConverter`, use `AADJwtBearerTokenAuthenticationConverter` instead.


## 3.7.0 (2021-07-20)
No changes from 3.6.1.

## 3.6.1 (2021-07-02)
### New Features
- Upgrade to [spring-boot-dependencies:2.5.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.2/spring-boot-dependencies-2.5.2.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.3](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.3/spring-cloud-dependencies-2020.0.3.pom).
### Key Bug Fixes
- Fix [cve-2021-22119](https://tanzu.vmware.com/security/cve-2021-22119).

## 3.6.0 (2021-06-23)
### Breaking Changes
- Remove class `AADB2COAuth2AuthenticatedPrincipal`, use class `AADOAuth2AuthenticatedPrincipal` instead.

### Deprecations
- Deprecate `allowTelemetry` configuration item.

## 3.5.0 (2021-05-24)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).
- Support OAuth 2.0 Client Credentials Flow.

### Key Bug Fixes
- Fix the issue [#21036](https://github.com/Azure/azure-sdk-for-java/issues/21036) where the AAD B2C starter cannot fetch the OpenID Connect metadata document via issuer.


## 3.4.0 (2021-04-19)


## 3.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).
- Upgrade to `Spring Security` [5.4.5](https://github.com/spring-projects/spring-security/releases/tag/5.4.5).

## 3.2.0 (2021-03-03)

## 3.1.0 (2021-01-20)
### Breaking Changes
- Exposed `userNameAttributeName` to configure the user's name.

## 3.0.0 (2020-12-30)


## 3.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-active-directory-b2c-spring-boot-starter` to `azure-spring-boot-starter-active-directory-b2c`.

## 2.3.5 (2020-09-14)
### Breaking Changes
- Unify spring-boot-starter version

## 2.3.3 (2020-08-13)
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
