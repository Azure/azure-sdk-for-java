# Release History

## 3.5.0-beta.1 (Unreleased)


## 3.4.0 (2021-04-19)
### Key Bug Fixes
- Fix bug of Keyvault refresh Timer task blocking application termination. ([#20014](https://github.com/Azure/azure-sdk-for-java/pull/20014))
- Fix bug that user-name-attribute cannot be configured. ([#20209](https://github.com/Azure/azure-sdk-for-java/issues/20209))

## 3.3.0 (2021-03-22)
### New Features
Updated to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).

### Key Bug Fixes
- Fix bug of using closed `MessageProducer` and `MessageConsumer` when a link is force detached in azure-spring-boot-starter-servicebus-jms.

### New Features
- Support creating `GrantedAuthority` by "roles" claim of id-token for web application in azure-spring-boot-starter-active-directory. 

## 3.2.0 (2021-03-03)
### Breaking Changes
- Remove `azure.activedirectory.b2c.oidc-enabled` property.
- Add `azure.activedirectory.b2c.login-flow` property. 
- Change the type of `azure.activedirectory.b2c.user-flows` to map and below is the new structure:
    ```yaml
    azure:
      activedirectory:
        b2c:
          login-flow: ${your-login-user-flow-key}               # default to sign-up-or-sign-in, will look up the user-flows map with provided key.
          user-flows:
            ${your-user-flow-key}: ${your-user-flow-name-defined-on-azure-portal}
    ```
- Require new property of `spring.jms.servicebus.pricing-tier` to set pricing tier of Azure Service Bus. Supported values are `premium`, `standard` and `basic`.
### New Features
- Enable MessageConverter bean customization.
- Update the underpinning JMS library for the Premium pricing tier of Service Bus to JMS 2.0.

## 3.1.0 (2021-01-20)
### Breaking Changes
- Exposed `userNameAttributeName` to configure the user's name.
- Deprecated `tenant` configuration item, add `base-uri` item to support multi-cloud environments.

## 3.0.0 (2020-12-30)
### Breaking Changes
- Deprecate `AADAppRoleStatelessAuthenticationFilter` and `AADAuthenticationFilter`.
- Change artifact id from `azure-active-directory-spring-boot-starter` to `azure-spring-boot-starter-active-directory`.
- Deprecate following `azure-spring-boot-starter-active-directory` configuration properties:
    ```
    spring.security.oauth2.client.provider.azure.*
    spring.security.oauth2.client.registration.azure.*
    azure.activedirectory.environment
    azure.activedirectory.user-group.key
    azure.activedirectory.user-group.value
    azure.activedirectory.user-group.object-id-key
    ```
- Stop support of Azure Active Directory Endpoints.

### New Features
- Support consent of multiple client registrations during user login.
- Support on-demand client registrations.
- Support the use of `@RegisteredOAuth2AuthorizedClient` annotation to get `OAuth2AuthorizedClient`.
- Support access control through users' membership information.
- Support on-behalf-of flow in the resource server.
- Provide AAD specific token validation methods of audience validation and issuer validation.
- Expose a flag `isPersonalAccount` in `AADOAuth2AuthenticatedPrincipal` to specify the account type in use: work account or personal account.
- Enable loading transitive membership information from Microsoft Graph API.
- Enable following `azure-spring-boot-starter-active-directory` configuration properties:
    ```yaml
    # Redirect URI of authorization server
    azure.activedirectory.redirect-uri-template
    # Refresh time of the cached JWK set before it expires, default value is 5 minutes.
    azure.activedirectory.jwk-set-cache-refresh-time
    # Logout redirect URI
    azure.activedirectory.post-logout-redirect-uri
    # base URI for authorization server, default value is "https://login.microsoftonline.com/"
    azure.activedirectory.base-uri
    # Membership URI of Microsoft Graph API to get users' group information, default value is "https://graph.microsoft.com/v1.0/me/memberOf"
    azure.activedirectory.graph-membership-uri
    ```
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

