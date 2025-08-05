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

### Features Added

- Supported `beginDeleteByResourceGroup` and `beginDeleteById` in `Deployments`.
- Supported `beginDeleteByName` in `ResourceGroups`.

### Breaking Changes

- `DeploymentsClient` and `DeploymentOperationsClient` is now accessed via `ResourceManager::deploymentClient()`.

### Other Changes

- Updated `api-version` of resources to `2025-04-01`.
- `deploymentStackClient()` in `ResourceManager` class is now deprecated. The functionality is moved to "azure-resourcemanager-resources-deploymentstacks" package.

## 2.51.0 (2025-05-26)

### Other Changes

- Updated `api-version` of resources to `2025-03-01`.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Breaking Changes

- `managementGroupId` is moved to the 1st parameter in APIs in `PolicyDefinitionsClient`.

### Other Changes

- Updated `api-version` of policy to `2023-04-01`.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated `api-version` of resources to `2024-11-01`.

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

- Updated core dependency from resources.

## 2.44.0 (2024-10-25)

### Features Added

- Added data boundary client of api-version 2024-08-01.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0 (2024-08-23)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Features Added

- Supported Deployment Stack. See https://learn.microsoft.com/azure/azure-resource-manager/bicep/deployment-stacks

### Other Changes

#### Dependency Updates

- Updated `api-version` of resources to `2024-03-01`.

## 2.39.0 (2024-05-24)

### Features Added

- Supported `mode` and `metadata` in `PolicyDefinition` class.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Breaking Changes

- Removed unused classes `ActionType`, `Origin`.

## 2.31.0 (2023-09-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.30.0 (2023-08-25)

### Bugs Fixed

- Fixed a bug that `GenericResources.deleteById` might select the wrong default api-version for resources whose child 
  resource type collides with other resources.

## 2.30.0-beta.1 (2023-08-18)

### Features Added

- Supported Deployment Stack as preview. See https://learn.microsoft.com/azure/azure-resource-manager/bicep/deployment-stacks

## 2.29.0 (2023-07-28)

### Bugs Fixed

- Fixed a bug that methods in `GenericResources`, `PolicyAssignments` and `TagOperations` fails when get or delete a 
  resource, if the resource name contains space.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` of resources to `2022-09-01`.
- Updated `api-version` of subscriptions to `2022-12-01`.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Breaking Changes

- Updated class `Identity` to final class.

### Other Changes

#### Dependency Updates

- Updated `api-version` of policy (policyAssignments) to `2022-06-01`.

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

### Bugs Fixed

- Fixed bug that `ProviderRegistrationPolicy` does not work.

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

- Supported `ResourceManager.resourceChangeClient()`.

### Breaking Changes

- Removed unused classes `DeploymentsWhatIfAtManagementGroupScopeHeaders`, `DeploymentsWhatIfAtManagementGroupScopeResponse`, `DeploymentsWhatIfAtSubscriptionScopeHeaders`, `DeploymentsWhatIfAtSubscriptionScopeResponse`, `DeploymentsWhatIfAtTenantScopeHeaders`, `DeploymentsWhatIfAtTenantScopeResponse`, `DeploymentsWhatIfHeaders`, `DeploymentsWhatIfResponse`

## 2.14.0 (2022-04-11)

### Bugs Fixed

- Fixed a bug that `ResourceManager.pipeline()` be `null`.

### Other Changes

#### Dependency Updates

- Updated `api-version` of locks to `2017-04-01`.

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

### Features Added

- Added support for `validateMoveResources` in `GenericResources`.

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated `api-version` of policy to `2021-06-01`.

### Other Changes

- Updated to use `PATCH` HTTP method for the update flow of `GenericResource`. For tagging resource, it is advised to use `TagOperations` instead.

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated `api-version` of features to `2021-07-01`.

### Features Added

- Added overload of `create(Context)` for `Creatable`.
- Added overload of `apply(Context)` for `Appliable`.
- Added overload of `beginCreate(Context)` for the definition flow of `Deployment`.
- Added support for `resourceProviderName` and `featureName` in `Feature`.

## 2.6.0 (2021-06-18)

- Added support for Challenge Based Authentication in `AuthenticationPolicy`.
- Added support for `parameters` in `PolicyDefinition` and `PolicyAssignment`.

## 2.5.0 (2021-05-28)
- Updated `api-version` of resources to `2021-01-01`
- Updated `api-version` of subscriptions to `2021-01-01`
- Updated `api-version` of policy to `2020-09-01`

## 2.4.0 (2021-04-28)

- Added shared interfaces and classes for Private Link.
- Updated parameter of `SupportsBatchCreation.create` from `List<Creatable<ResourceT>>` to `List<? extends Creatable<ResourceT>>`

## 2.3.0 (2021-03-30)

- Added client-side validation for `getByResourceGroup`, `listByResourceGroup`, `deleteByResourceGroup` methods.
- Added method overload of `getById` and `deleteById` in `GenericResources` to take `apiVersion` parameters. It is always recommended for user to provide the `apiVersion` parameter for consistency across service versions.
- Supported `TagOperations`

## 2.2.0 (2021-02-24)

- Supported locks with API version `2016-09-01`. Added `ManagementLock` and related classes.

## 2.1.0 (2020-11-24)

- Changed `ProviderRegistrationPolicy` using `Providers` endpoint directly.
- Changed `Tenants::list` to return new interface `Tenant` instead of `TenantIdDescriptionInner`.

## 2.0.0 (2020-10-19)

- Removed `Region`, as it is replaced by `com.azure.core.management.Region`.
- Removed `PagedList`
- Added `AggregatedManagementException` exception class for aggregation of multiple `ManagementException`.
- Changed `Creatabele.createAsync()` return `Mono<ResourceT>` and `SupportsBatchCreation.createAsync()` return `Flux<ResourceT>`

## 2.0.0-beta.4 (2020-09-02)

- Updated `azure-core-management` dependency
- Supported `beginCreate` and `beginDelete` for `VirtualMachine`, `Disk`, `NetworkInterface`, `PublicIpAddress`.
- Removed `DateTimeDeserializer`, as it is in azure-core.
- Added `ReturnRequestIdHeaderPolicy`. It is added to pipeline by default.
