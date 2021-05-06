# Release History

## 3.5.0-beta.1 (Unreleased)
### New Features
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).



## 3.4.0 (2021-04-19)
### Key Bug Fixes
- Fix bug of Keyvault refresh Timer task blocking application termination. ([#20014](https://github.com/Azure/azure-sdk-for-java/pull/20014))

## 3.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).

## 3.2.0 (2021-03-03)


## 3.1.0 (2021-01-20)


## 3.0.0 (2020-12-30)


## 3.0.0-beta.1 (2020-11-18)
### Breaking Changes 
 - Change configure item from `azure.keyvault.secret.keys` to `azure.keyvault.secret-keys`
 - Change configure item from `azure.keyvault.allow.telemetry` to `azure.keyvault.allow-telemetry`
 - Change group id from `com.microsoft.azure` to `com.azure.spring`.
 - Change artifact id from `azure-keyvault-secrets-spring-boot-starter` to `azure-spring-boot-starter-keyvault-secrets`.

## 2.3.5 (2020-09-14)
### Key Bug Fixes 
- Exclude disabled secrets when getting Key Vault secrets

## 2.3.3 (2020-08-13)
### New Features
- Support connection to multiple Key Vault from a single application configuration file 
- Support case sensitive keys in Key Vault 
- Key Vault Spring Boot Actuator 

### Breaking Changes 
- Revamp KeyVault refreshing logic to avoid unnecessary updates. 
 
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
