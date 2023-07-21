# Release History

## 2.29.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.29.0-beta.1 (2023-07-19)

- Preview release for `api-version` `2022-05-01-preview`.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Bugs Fixed

- Fixed a bug that `Permission.dataActions()` and `Permission.notDataActions()` return wrong results.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Breaking Changes

- `filter` parameters in list API from `RoleAssignmentsClient` is required to be encoded by user.

### Other Changes

#### Dependency Updates

- Updated `api-version` of authorization to `2022-04-01`.

## 2.20.0 (2022-10-26)

### Features Added

- Supported description in role assignment.

## 2.19.0 (2022-09-23)

### Bugs Fixed

- Supported delayed retry on 404 for eventual consistency, after creating AAD service principal.
- Improved the delayed retry on 400 for service principal, when creating role assignment. Now the retry will continue for only about a minute.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Bugs Fixed

- Supported delayed retry on 404 for eventual consistency, after creating AAD application.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Breaking Changes

- Removed `DenyAssignmentsClient` as it is preview feature.

### Other Changes

#### Dependency Updates

- Updated `api-version` of authentication to `2020-10-01`.

## 2.14.0 (2022-04-11)

### Features Added

- Supported Azure Kubernetes Service related roles to `BuiltInRole`.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated `api-version` of authentication to `2020-08-01-preview`

## 2.10.0 (2021-11-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Supported key vault data related roles to `BuiltInRole`, for RBAC authorization of data access to data in `Vault`

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Supported `listByServicePrincipal` in `RoleAssignments`
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
