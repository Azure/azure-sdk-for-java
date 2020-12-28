# Release History

## 3.0.0-beta.2 (Unreleased)
### Breaking Changes
- Deprecate `AADAppRoleStatelessAuthenticationFilter` and `AADAuthenticationFilter`.
- Support the setting of post logout redirect uri 

### New Features
- Support consent multiple client-registration when login.
- Support on-demand client-registration.
- Support the use of `@RegisteredOAuth2AuthorizedClient` to get `OAuth2AuthorizedClient`.
- Support to obtain the claim in access token, such as `scp`, `roles` etc, to carry out permission control.
- Support on-behalf-of flow when the `azure-spring-boot-starter-active-directory` used in resource-server.
- Provide some AAD specific token validation, such as audience validation, issuer validation.
- Expose a flag in the `AzureOAuth2AuthenticatedPrincipal` to tell which account type is being used, work account or personal account.

## 3.0.0-beta.1 (2020-11-18)
### Breaking Changes
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

