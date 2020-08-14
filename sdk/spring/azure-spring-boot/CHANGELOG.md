# Release History

## 2.4.0-beta.1 (Unreleased)


## 2.3.3 (2020-08-07)
_New Features_
- Support connection to multiple Key Vault from a single application configuration file 
- Support case sensitive keys in Key Vault 
- Key Vault Spring Boot Actuator 

_Improved_ 
- Revamp KeyVault refreshing logic to avoid unnecessary updates. 
- Update the underpinning JMS library for Service Bus to JMS 2.0 to support seamlessly lift and shift their Spring workloads to Azure and automatic creation of resources.
 
_Bug Fixes_ 
- Address CVEs and cleaned up all warnings at build time. 

_Deprecated_
- azure-servicebus-spring-boot-starter 
- azure-mediaservices-spring-boot-starter 
- azure-storage-spring-boot-starter  
