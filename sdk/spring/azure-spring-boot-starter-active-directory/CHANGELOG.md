# Release History

## 3.5.0-beta.1 (Unreleased)


## 3.4.0 (2021-04-19)
### Key Bug Fixes
- Fix bug that user-name-attribute cannot be configured. ([#20209](https://github.com/Azure/azure-sdk-for-java/issues/20209))


## 3.3.0 (2021-03-22)
### New Features
- Upgrade to `Spring Boot` [2.4.3](https://github.com/spring-projects/spring-boot/releases/tag/v2.4.3).
- Upgrade to `Spring Security` [5.4.5](https://github.com/spring-projects/spring-security/releases/tag/5.4.5).
- Support creating `GrantedAuthority` by "roles" claim from id-token for web application.

## 3.2.0 (2021-03-03)


## 3.1.0 (2021-01-20)


## 3.0.0 (2020-12-30)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-active-directory-spring-boot-starter` to `azure-spring-boot-starter-active-directory`.
- Deprecate `AADAppRoleStatelessAuthenticationFilter` and `AADAuthenticationFilter`.
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
- Provide AAD specific token validation of audience validation and issuer validation.
- Expose a flag `isPersonalAccount` in `AADOAuth2AuthenticatedPrincipal` to specify the account type in use: work account or personal account.
- Enable loading transitive membership information from Microsoft Graph API.
- Enable following `azure-spring-boot-starter-active-directory` configuration properties:
    ```properties
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
## 2.3.5 (2020-09-14)
### Key Bug Fixes
- Get full list of groups the user belongs to from Graph API

## 2.3.3 (2020-08-13)
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
