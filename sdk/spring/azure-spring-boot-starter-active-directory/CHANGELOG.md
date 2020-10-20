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
    ```
- Check scope parameter for AAD authorization requests before configuration. Necessary permissions would be automatically added if needed.
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-active-directory-spring-boot-starter` to `azure-spring-boot-starter-active-directory`.

## 2.3.5 (2020-09-14)
### Key Bug Fixes
- Get full list of groups the user belongs to from Graph API

## 2.3.3 (2020-08-13)
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
