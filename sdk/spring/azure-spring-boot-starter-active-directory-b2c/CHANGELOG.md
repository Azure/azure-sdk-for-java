# Release History

## 3.2.0-beta.1 (Unreleased)
### Breaking Changes
- Remove `azure.activedirectory.b2c.oidc-enabled` configuration.
- Change user flow configuration, below is the new structure:
    ```yaml
    azure:
      activedirectory:
        b2c:
          sign-up-or-sign-in: ${your-sign-up-or-in-user-flow-key}
          user-flows:
            password-reset: ${your-profile-edit-user-flow}      # optional
            profile-edit: ${your-password-reset-user-flow}      # optional
            sign-in: ${your-sign-in-user-flow}                  # optional
            sign-up: ${your-sign-up-user-flow}                  # optional
            sign-up-or-sign-in: ${your-sign-up-or-in-user-flow} # optional
    ```

## 3.1.0 (2021-01-20)
### Breaking Changes
- Exposed `userNameAttributeName` to configure the user's name.

## 3.0.0 (2020-12-30)


## 3.0.0-beta.1 (2020-11-18)
### Breaking Changes
- Change group id from `com.microsoft.azure` to `com.azure.spring`.
- Change artifact id from `azure-active-directory-b2c-spring-boot-starter` to `azure-spring-boot-starter-active-directory-b2c`.

## 2.3.5 (2020-09-14)
### Breaking Changes
- Unify spring-boot-starter version

## 2.3.3 (2020-08-13)
### Key Bug Fixes 
- Address CVEs and cleaned up all warnings at build time. 
