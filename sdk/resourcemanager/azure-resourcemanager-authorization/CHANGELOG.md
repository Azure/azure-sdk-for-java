# Release History

## 2.2.0-beta.1 (Unreleased)

- Updated API from `AAD Graph` to `Microsoft Graph`. New permission needs to be granted before calling the API, [Reference](https://docs.microsoft.com/graph/permissions-reference)
- Removed `applicationPermissions` in `ActiveDirectoryApplication`
- Removed `signInName` in `ActiveDirectoryUser`
- Removed `withPasswordValue` in `PasswordCredential.Definition`
- Supported `withPasswordConsumer` in `PasswordCredential.Definition` to consume the password value.

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Supported `listByFilter` in `ActiveDirectoryApplications`, `ActiveDirectoryGroups`, `ActiveDirectoryUsers`, `ServicePrincipals`

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
