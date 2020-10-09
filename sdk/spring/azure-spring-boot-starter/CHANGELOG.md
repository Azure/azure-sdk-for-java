# Release History

## 3.0.0-beta.1 (Unreleased)


## 2.3.5 (2020-09-14)
### Breaking Changes
- Unify spring-boot-starter version

## 2.3.4 (2020-08-20)
### Key Bug Fixes
- Replace underpinning JMS library for Service Bus of Service Bus JMS Starter to Apache Qpid to support all tiers of Service Bus.

## 2.3.3 (2020-08-13)
### New Features
- Support connection to multiple Key Vault from a single application configuration file 
- Support case sensitive keys in Key Vault 
- Key Vault Spring Boot Actuator 

### Breaking Changes
- Revamp KeyVault refreshing logic to avoid unnecessary updates. 
- Update the underpinning JMS library for Service Bus to JMS 2.0 to support seamlessly lift and shift their Spring workloads to Azure and automatic creation of resources.
- Deprecated azure-servicebus-spring-boot-starter 
- Deprecated azure-mediaservices-spring-boot-starter 
- Deprecated azure-storage-spring-boot-starter  

### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 

