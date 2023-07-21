# Release History

## 2.29.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

- Fixed `SpringApp` creation failure for some pricing tiers with default settings.
- Fixed a bug that calling `withActivation` multiple times during `SpringAppDeployment` creation would result in
  multiple REST API calls.

### Other Changes

## 2.29.0-beta.1 (2023-07-21)

- Use preview `api-version` `2023-03-01-preview`.

## 2.28.0 (2023-06-25)

### Breaking Changes

- Removed `withFqdn` from `AppResourceProperties` as it's never functional in the backend.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-12-01`.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0-beta.1 (2023-04-17)

Preview release for api-version `2023-03-01-preview`.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.20.0 (2022-10-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.19.0 (2022-09-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Features Added

- Supported Enterprise Tier `Configuration Service`.
- Supported Enterprise Tier `Build Service`.
- Supported Enterprise Tier binding `Spring App` to `Configuration Service`.
- Supported Enterprise Tier `Spring App Deployment` with `Jar` and `Maven Source Code`.
- Supported `Java_17` runtime for all tiers.
- Supported `jvmOptions()` in `SpringAppDeployment` for all tiers.
- Supported Enterprise Tier `runtimeVersion()` in `SpringAppDeployment`.
- Supported Enterprise Tier binding `Spring App` to `Service Registry`.

### Breaking Changes

- Removed `createdTime` from `AppResourceProperties` and `DeploymentResourceProperties`.
- Removed `activeDeploymentName` from `AppResourceProperties`.
- Removed `appName` from `DeploymentResourceProperties`.
- Removed `type` from `UserSourceInfo`.
- Moved `relativePath` from `UserSourceInfo` to `UploadedUserSourceInfo`.
- Moved `runtimeVersion` from `DeploymentSettings` to `JarUploadedUserSourceInfo`, `SourceUploadedUserSourceInfo` and `NetCoreZipUploadedUserSourceInfo`.
- Moved `artifactSelector` from `UserSourceInfo` to `SourceUploadedUserSourceInfo`.
- Moved `jvmOptions` from `DeploymentSettings` to `JarUploadedUserSourceInfo`.
- Moved `cpu` from `DeploymentSettings` to `ResourceRequests` and changed type from `Integer` to `String`.
- Moved `memoryInGB` from `DeploymentSettings` to `ResourceRequests`, renamed to `memory` and changed type from `Integer` to `String`.
- Added a new parameter for PATCH in `SpringService` update.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-04-01`.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

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

- Updated core dependency from resources

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
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2020-11-01-preview`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0-beta.5 (2020-10-19)

- Added `withSku(SkuName)` in SpringService

## 2.0.0-beta.4 (2020-09-02)

- Updated `api-version` to `2020-07-01`
- Added `defineActiveDeployment` and `getActiveDeployment` in SpringApp
- Removed `withoutTemporaryDisk` and `withoutPersistentDisk` in SpringAppDeployment
- Removed `withSettingsFromDeployment` in SpringAppDeployment
- Removed `deployJar` and `deploySource` in SpringApp
- Changed `serverProperties` to `getServerProperties` and `traceProperties` to `getMonitoringSetting` in SpringService
