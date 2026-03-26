# Release History

## 2.54.0-beta.1 (2026-03-26)

### Breaking Changes

#### `models.CachedImagesListResult` was removed

#### `models.UsageListResult` was removed

#### `models.ContainerGroupListResult` was removed

#### `models.CapabilitiesListResult` was removed

#### `models.OperationListResult` was removed

#### `models.ContainerState` was modified

* `ContainerState()` was changed to private access

#### `models.ContainerPropertiesInstanceView` was modified

* `ContainerPropertiesInstanceView()` was changed to private access

#### `models.InitContainerPropertiesDefinitionInstanceView` was modified

* `InitContainerPropertiesDefinitionInstanceView()` was changed to private access

#### `models.Capabilities` was modified

* `Capabilities()` was changed to private access

#### `models.Event` was modified

* `Event()` was changed to private access

#### `models.CapabilitiesCapabilities` was modified

* `CapabilitiesCapabilities()` was changed to private access

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed

#### `models.ContainerGroupPropertiesInstanceView` was modified

* `ContainerGroupPropertiesInstanceView()` was changed to private access

#### `models.CachedImages` was modified

* `CachedImages()` was changed to private access
* `withImage(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed

#### `models.UsageName` was modified

* `UsageName()` was changed to private access

#### `models.Operation` was modified

* `Operation()` was changed to private access
* `withDisplay(models.OperationDisplay)` was removed
* `withProperties(java.lang.Object)` was removed
* `withName(java.lang.String)` was removed
* `withOrigin(models.ContainerInstanceOperationsOrigin)` was removed

### Features Added

* `models.NGroupPatch` was added

* `models.StandbyPoolProfileDefinition` was added

* `models.LoadBalancerBackendAddressPool` was added

* `models.CGProfilesUpdateHeaders` was added

* `models.AzureFileShareAccessTier` was added

* `models.ApplicationGateway` was added

* `models.IdentityAccessControl` was added

* `models.ElasticProfile` was added

* `models.ElasticProfileContainerGroupNamingPolicyGuidNamingPolicy` was added

* `models.LoadBalancer` was added

* `models.NGroupCGPropertyContainerProperties` was added

* `models.ElasticProfileContainerGroupNamingPolicy` was added

* `models.FileShareProperties` was added

* `models.CGProfilesUpdateResponse` was added

* `models.ContainerGroupProfilePatch` was added

* `models.UpdateProfile` was added

* `models.NGroupUpdateMode` was added

* `models.NGroupIdentity` was added

* `models.NGroupProvisioningState` was added

* `models.NGroupContainerGroupProperties` was added

* `models.UpdateProfileRollingUpdateProfile` was added

* `models.ApiEntityReference` was added

* `models.ContainerGroupProvisioningState` was added

* `models.NGroupCGPropertyContainer` was added

* `models.ContainerGroupProfileReferenceDefinition` was added

* `models.SecretReference` was added

* `models.StorageProfile` was added

* `models.NGroupCGPropertyVolume` was added

* `models.ApplicationGatewayBackendAddressPool` was added

* `models.CGProfilesCreateOrUpdateHeaders` was added

* `models.IdentityAccessLevel` was added

* `models.AzureFileShareAccessType` was added

* `models.NetworkProfile` was added

* `models.CGProfilesCreateOrUpdateResponse` was added

* `models.ResourcePatch` was added

* `models.IdentityAcls` was added

* `models.ConfigMap` was added

* `models.PlacementProfile` was added

* `models.FileShare` was added

* `models.ContainerGroupProfileStub` was added

#### `models.Container` was modified

* `configMap()` was added
* `withConfigMap(models.ConfigMap)` was added

#### `models.ImageRegistryCredential` was modified

* `passwordReference()` was added
* `withPasswordReference(java.lang.String)` was added

#### `models.ContainerGroupSku` was modified

* `NOT_SPECIFIED` was added

#### `models.Volume` was modified

* `secretReference()` was added
* `withSecretReference(java.util.Map)` was added

#### `models.EnvironmentVariable` was modified

* `withSecureValueReference(java.lang.String)` was added
* `secureValueReference()` was added

#### `models.AzureFileVolume` was modified

* `storageAccountKeyReference()` was added
* `withStorageAccountKeyReference(java.lang.String)` was added

## 2.53.8 (2026-02-26)

### Other Changes

#### Dependency Updates

- Updated `azure-resourcemanager-network` and `azure-storage-file-share` dependencies.

## 2.53.7 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-storage` from `2.55.1` to version `2.55.2`.
- Upgraded `azure-resourcemanager-msi` from `2.53.4` to version `2.53.5`.
- Upgraded `azure-storage-file-share` from `12.29.0` to version `12.29.1`.
- Upgraded `azure-resourcemanager-authorization` from `2.53.5` to version `2.53.6`.

## 2.53.6 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.5 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.4 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-msi` from `2.53.2` to version `2.53.3`.
- Upgraded `azure-resourcemanager-resources` from `2.53.2` to version `2.53.3`.
- Upgraded `azure-resourcemanager-network` from `2.53.3` to version `2.53.4`.
- Upgraded `azure-resourcemanager-storage` from `2.54.0` to version `2.54.1`.
- Upgraded `azure-resourcemanager-authorization` from `2.53.2` to version `2.53.3`.

## 2.53.3 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.2 (2025-08-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.52.0 (2025-06-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.51.0 (2025-05-26)

### Features Added

- Added `withPrivateImageRegistry` overload for managed identity in `ContainerGroup`.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.45.0 (2024-11-28)

### Features Added

- Supported `autoGeneratedDomainNameLabelScope` and `withAutoGeneratedDomainNameLabelScope` methods for `ContainerGroup`.

## 2.44.0 (2024-10-25)

### Features Added

- Supported `beginCreate` method for `ContainerGroup`.

## 2.44.0-beta.1 (2024-10-08)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-05-01-preview`.

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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Breaking Changes

- Renamed `autoGeneratedDomainNameLabelScope` property to `dnsNameLabelReusePolicy` in `IpAddress`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-05-01`.

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

### Features Added

- Supported configuring liveness probes and readiness probes for container instances.

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

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-10-01`.

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

### Breaking Changes

- `NetworkProfile` configuration in `ContainerGroup` is removed due to security concern from service. Please create `ContainerGroup` via `Subnet`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-09-01`.

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Supported attach for output stream for container instance in `ContainerGroup`.

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated `api-version` to `2021-03-01`

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0-beta.5 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
