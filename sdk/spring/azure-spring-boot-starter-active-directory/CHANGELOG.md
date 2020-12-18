# Release History

## 3.0.0-beta.2 (Unreleased)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-active-directory-spring-boot-starter` to `azure-spring-boot-starter-active-directory`.
- Deprecate `AADAppRoleStatelessAuthenticationFilter` and `AADAuthenticationFilter`.

### New Features
- Support consent multiple client-registration when login.
- Support on-demand client-registration.
- Support the use of `@RegisteredOAuth2AuthorizedClient` to get `OAuth2AuthorizedClient`.
- Support to obtain the claim in access token, such as `scp`, `roles` etc, to carry out permission control.
- Support on-behalf-of flow when the `azure-spring-boot-starter-active-directory` used in resource-server.
- Provide some AAD specific token validation, such as audience validation, issuer validation.
- Expose a flag in the `AzureOAuth2AuthenticatedPrincipal` to tell which account type is being used, work account or personal account.

## 2.3.5 (2020-09-14)
### Key Bug Fixes
- Get full list of groups the user belongs to from Graph API

## 2.3.3 (2020-08-13)
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
