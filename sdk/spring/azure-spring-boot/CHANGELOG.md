# Release History

## 3.0.0-beta.1 (Unreleased)
### Breaking Changes
- Configuration items like `spring.security.oauth2.client.xxx` is not supported anymore. Please use the following configuration items instead:
    ```
    azure.activedirectory.tenant-id=xxxxxx-your-tenant-id-xxxxxx
    azure.activedirectory.client-id=xxxxxx-your-client-id-xxxxxx
    azure.activedirectory.client-secret=xxxxxx-your-client-secret-xxxxxx
    azure.activedirectory.user-group.allowed-groups=group1, group2
    azure.activedirectory.scope = your-customized-scope1, your-customized-scope2
    azure.activedirectory.redirect-uri-template=xxxxxx-your-redirect-uri-xxxxxx
    ```
- Check scope parameter for AAD authorization requests before configuration. Necessary permissions would be automatically added if needed.
- Update `com.azure` group id to `com.azure.spring`.
- Deprecated azure-spring-boot-metrics-starter.
- Change group id from `com.microsoft.azure` to `com.azure.spring`.

## 2.3.5 (2020-09-14)
### Key Bug Fixes
- Get full list of groups the user belongs to from Graph API
- Exclude disabled secrets when getting Key Vault secrets

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

