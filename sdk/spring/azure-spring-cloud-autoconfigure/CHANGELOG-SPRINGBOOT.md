# Release History

## 3.8.0-beta.1 (Unreleased)
This release is compatible with Spring Boot 2.5.0 - 2.5.3.
### Dependency Upgrades
- Upgrade to [spring-boot-dependencies:2.5.3](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.3/spring-boot-dependencies-2.5.3.pom).
### New Features
- Add property `azure.activedirectory.resource-server.principal-claim-name` to configure principal claim name.
- Add property `azure.activedirectory.resource-server.claim-to-authority-prefix-map` to configure claim to authority prefix map.
### Deprecations
 - Deprecate `AADB2CJwtBearerTokenAuthenticationConverter`, use `AADJwtBearerTokenAuthenticationConverter` instead.


## 3.7.0 (2021-07-20)
### New Features
- Add property `azure.activedirectory.application-type` to configure the application type.

    Here are the 4 valid values:
    - *web_application*: Web Application.
    - *resource_server*: Resource Server.
    - *resource_server_with_obo*: Resource Server with authorization grant type `on_behalf_of`.
    - *web_application_and_resource_server*: Web Application and Resource Server in one application, it also supports `on_behalf_of`.

    This property is optional, its value can be inferred by dependencies, only `web_application_and_resource_server` must be configured manually: `azure.activedirectory.application-type=web_application_and_resource_server`.
    
    Here is the table about how the AAD starter infers application type by dependencies:
    
    | Has dependency: spring-security-oauth2-client | Has dependency: spring-security-oauth2-resource-server |                  Valid values of application type                 | Default value               |
    |-----------------------------------------------|--------------------------------------------------------|-------------------------------------------------------------------|-----------------------------|
    |                      Yes                      |                          No                            |                       `web_application`                           |       `web_application`     |
    |                      No                       |                          Yes                           |                       `resource_server`                           |       `resource_server`     |
    |                      Yes                      |                          Yes                           | `resource_server_with_obo`, `web_application_and_resource_server` | `resource_server_with_obo`  |

- Support new value `on_behalf_of` for OBO authorization grant type, originally we only support `on-behalf-of`.



## 3.6.1 (2021-07-02)
### New Features
- Upgrade to [spring-boot-dependencies:2.5.2](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.2/spring-boot-dependencies-2.5.2.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.3](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.3/spring-cloud-dependencies-2020.0.3.pom).
### Key Bug Fixes
- Fix [cve-2021-22119](https://tanzu.vmware.com/security/cve-2021-22119).


## 3.6.0 (2021-06-23)
### Breaking Changes
- Deprecate aad.group.enable-full-list, use aad.group.allowed-group-ids=all instead.
- Deprecated `allowTelemetry` configuration item in each module property class.

### New Features
- Upgrade to [spring-boot-dependencies:2.5.0](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.5.0/spring-boot-dependencies-2.5.0.pom).
- Support domain_hint in aad-starter.([#21517](https://github.com/Azure/azure-sdk-for-java/issues/21517))
- "aad.group.allowed-group-ids=all" can be used to map all group ids to GrantedAuthority (ROLE_group_id). 
  And it can work together with group names configured by "aad.group.allowed-group-names" (ROLE_group_name).[#21983](https://github.com/Azure/azure-sdk-for-java/issues/21983)


## 3.5.0 (2021-05-24)
### New Features
- Add `AADB2CTrustedIssuerRepository` to manage the trusted issuer in AAD B2C.
- Upgrade to [spring-boot-dependencies:2.4.5](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/2.4.5/spring-boot-dependencies-2.4.5.pom).
- Upgrade to [spring-cloud-dependencies:2020.0.2](https://repo.maven.apache.org/maven2/org/springframework/cloud/spring-cloud-dependencies/2020.0.2/spring-cloud-dependencies-2020.0.2.pom).
- Enable property azure.activedirectory.redirect-uri-template.([#21116](https://github.com/Azure/azure-sdk-for-java/issues/21116))
- Support creating `GrantedAuthority` by groupId and groupName for web application.([#20218](https://github.com/Azure/azure-sdk-for-java/issues/20218))
  ```yaml
  user-group:
      allowed-group-names: group1,group2
      allowed-group-ids: <group1-id>,<group2-id>
      enable-full-list: false  
  ```
  | Parameter           | Description                                             |
  | ------------------- | ------------------------------------------------------------ |
  | allowed-group-names | if `enable-full-list` is `false`, create `GrantedAuthority` with groupNames which belong to user and `allowed-group-names` |
  | allowed-group-ids   | if `enable-full-list` is `false`, create `GrantedAuthority` with groupIds which belong to user and `allowed-group-ids` |
  | enable-full-list    | default is `false`.<br> if the value is `true`, create `GrantedAuthority` only with user's  all groupIds, ignore group names|

### Key Bug Fixes
- Fix issue that where the AAD B2C starter cannot fetch the OpenID Connect metadata document via issuer. [#21036](https://github.com/Azure/azure-sdk-for-java/issues/21036)
- Deprecate *addB2CIssuer*, *addB2CUserFlowIssuers*, *createB2CUserFlowIssuer* methods in `AADTrustedIssuerRepository`.


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
- For the required scopes in AAD auth code flow, use `Directory.Read.All` instead of `Directory.AccessAsUser.All`. ([#18901](https://github.com/Azure/azure-sdk-for-java/pull/18901))
  Now The requested scopes are: `openid`, `profile`, `offline_access`, `User.Read`, `Directory.Read.All`.
  You can Refer to [AADWebAppConfiguration.java](https://github.com/Azure/azure-sdk-for-java/blob/azure-spring-boot_3.2.0/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADWebAppConfiguration.java#L119) for detailed information.
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
- Removed support for older `AAD v1` style endpoints.
    - Support for `AAD v1`, also named `Azure Active Directory`, endpoints in the form https://login.microsoftonline.com/common/oauth2/authorize has been removed.
    - `AAD v2`, also named `Microsoft Identity Platform`, endpoints in the form https://login.microsoftonline.com/common/oauth2/v2.0/authorize continue to be supported.
    - Please see [this documentation](https://github.com/MicrosoftDocs/azure-docs/blob/master/articles/active-directory/azuread-dev/azure-ad-endpoint-comparison.md) for more information.
- The required scopes in AAD auth code flow are: `openid`, `profile`, `offline_access`, `User.Read`, `Directory.AccessAsUser.All`.
  You can Refer to [AADWebAppConfiguration.java](https://github.com/Azure/azure-sdk-for-java/blob/azure-spring-boot_3.0.0/sdk/spring/azure-spring-boot/src/main/java/com/azure/spring/aad/webapp/AADWebAppConfiguration.java#L117) for detailed information.

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
