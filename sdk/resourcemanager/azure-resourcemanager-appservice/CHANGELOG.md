# Release History

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.52.0 (2025-06-27)

### Breaking Changes

- Removed unused classes from ContainerApp.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-11-01`.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Features Added

- Supported Tomcat 11.0 and Java 11, 17, 21 in `RuntimeStack` for `WebApp`.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Features Added

- Supported Java SE 21 in `RuntimeStack` for `WebApp`.
- Supported Tomcat 10.1, JBoss EAP 8 and Java 21 in `RuntimeStack` for `WebApp`.
- Supported .Net 6, 8, 9 in `RuntimeStack` for `WebApp`.
- Supported PHP 8.2, 8.3 in `RuntimeStack` for `WebApp`.
- Supported Python 3.9, 3.10, 3.11, 3.12 in `RuntimeStack` for `WebApp`.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.45.0 (2024-11-28)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-04-01`.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Features Added

- Added `deploy` and `pushDeploy` API to `FunctionApp` class for deploying zip file to FunctionApp of Flex Consumption plan.

## 2.42.0 (2024-08-23)

### Breaking Changes

- Type changed from `Float` to `Integer`, on `maximumInstanceCount` and `instanceMemoryMB` property in `FunctionsScaleAndConcurrency` class. 
- Type changed from `Float` to `Integer`, on `instanceCount` property in `FunctionsAlwaysReadyConfig` class.
- Type changed from `Float` to `Integer`, on `perInstanceConcurrency` property in `FunctionsScaleAndConcurrencyTriggersHttp` class.

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-12-01`.

## 2.39.0 (2024-05-24)

### Features Added

- Supported disabling public network access in `FunctionApp` via `disablePublicNetworkAccess()`, for private link feature.
- Supported disabling public network access in `DeploymentSlot` via `disablePublicNetworkAccess()`, for private link feature.
- Supported disabling public network access in `FunctionDeploymentSlot` via `disablePublicNetworkAccess()`, for private link feature.
- Added extra retry for Function App on ACA.

## 2.38.0 (2024-04-16)

### Features Added

- Supported disabling public network access in `WebApp` via `disablePublicNetworkAccess()`, for private link feature.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.1 (2024-03-04)

### Bugs Fixed

- Fixed NullPointerException when updating Function App in Azure Container Apps.
- Fixed a bug that `withPrivateRegistryImage` doesn't work as expected for Function App in Azure Container Apps.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.2 (2024-02-05)

### Bugs Fixed

- Fixed a bug that Function App in Azure Container Apps environment doesn't support creating from private container registry image.

## 2.35.1 (2024-01-31)

### Bugs Fixed

- Mitigated backend error when creating Function App in Azure Container Apps environment.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0 (2023-12-22)

### Features Added

- Supported Function App in Azure Container Apps environment.
  - Supported `withManagedEnvironmentId` for `FunctionApp`.
  - Supported `withMaxReplica` and `withMinReplica` for `FunctionApp`.

## 2.33.0 (2023-11-24)

### Bugs Fixed

- Deprecated `RuntimeStack.TOMCAT_10_0_JRE11`. Please use `RuntimeStack.TOMCAT_10_0_JAVA11`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-01-01`.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.31.0 (2023-09-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.30.0 (2023-08-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0 (2023-07-28)

### Features Added

- Supported `withContainerSize` for `FunctionApp`.

### Bugs Fixed

- Updated SKU that automatically set Function App "Always On".
Function App on `FREE`, `SHARED`, `DYNAMIC` (consumption plan), `ELASTIC_PREMIUM` (premium plan), `ELASTIC_ISOLATED` App Service has "Always On" turned off.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.1 (2023-04-25)

### Breaking Changes

- Changed to use AAD Auth for Kudu deployment.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Bugs Fixed

- Fixed potential `NullPointerException`, when query tag on `WebApp` and `FunctionApp`. 

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-09-01`.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Other Changes

- Added "WEBSITE_CONTENTAZUREFILECONNECTIONSTRING" and "WEBSITE_CONTENTSHARE" app settings to FunctionApp of Linux Consumption plan and Premium plan.

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

### Features Added

- Supported Java SE 17 in `FunctionRuntimeStack` for `FunctionApp`.

## 2.17.0 (2022-07-25)

### Features Added

- Supported `getDeploymentStatus` in `SupportsOneDeploy`.

### Breaking Changes

- Merged multiple classes `AppServiceCertificateOrderPatchResourcePropertiesAppServiceCertificateNotRenewableReasonsItem`,
  `AppServiceCertificateOrderPropertiesAppServiceCertificateNotRenewableReasonsItem`, 
  `DomainPatchResourcePropertiesDomainNotRenewableReasonsItem` and `DomainPropertiesDomainNotRenewableReasonsItem` 
  into one class `ResourceNotRenewableReason`.
- `AppServiceEnvironmentPatchResource` was removed.
- `ValidateRequest` was removed.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-03-01`.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Features Added

- Supported `checkNameAvailability` method for `WebApps`.

### Breaking Changes

- Behavior changed on `WebApps.list()` and `WebApps.listByResourceGroup()` method, that results include sites with `kind` be empty or `linux`.

## 2.14.0 (2022-04-11)

###  Bugs Fixed

- Fixed a bug that `WebAppBase.getPublishingProfile()` failed to extract FTP profile, when web app is FTPS-only.
- Supported Java SE 17 in `RuntimeStack` for `WebApp`.

## 2.13.0 (2022-03-11)

### Features Added

- Supported Tomcat 10 and Java 8, 11, 17 in `RuntimeStack` for `WebApp`.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Features Added

- Supported option for tracking deployment status via `pushDeploy` in `WebApp` and `DeploymentSlot`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-03-01`.

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

- Updated `api-version` to `2021-02-01`.

### Features Added

- Supported `NODEJS_14_LTS` and `PHP_7_4` in `RuntimeStack`.

### Breaking Changes

- Renamed `ManagedServiceIdentityUserAssignedIdentities` class to `UserAssignedIdentity`.

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Supported the configuration of network access for `WebApp`, `FunctionApp`.

## 2.4.0 (2021-04-28)

- Updated `api-version` to `2020-12-01`
- Enum `IpFilterTag` changed to subclass of `ExpandableStringEnum`
- Major changes to `AppServiceEnvironment`
- Supported Private Link in `WebApp` and `FunctionApp`

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Supported OneDeploy feature

## 2.0.0 (2020-10-19)

- Supported the configuration of container image for Windows web app.
- Supported the configuration of container image for deployment slot in update stage.
- Changed return type of `list` and `listByResourceGroup` in `WebApps`, `FunctionApps`, `DeploymentSlots`, `FunctionDeploymentSlots`.
- Added site properties for `WebApp`, `FunctionApp`, `DeploymentSlot`, `FunctionDeploymentSlot`.

## 2.0.0-beta.4 (2020-09-02)

- Fixed function app slot
