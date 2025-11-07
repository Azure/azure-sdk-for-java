# Release History

## 2.54.0 (2025-10-31)

### Breaking Changes

#### `models.ManagedHsmKeyListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.KeyListResult` was removed

#### `models.SecretListResult` was removed

#### `models.VaultListResult` was removed

#### `models.ResourceListResult` was removed

#### `models.MhsmPrivateEndpointConnectionsListResult` was removed

#### `models.ManagedHsmListResult` was removed

#### `models.OperationListResult` was removed

#### `models.MhsmRegionsListResult` was removed

#### `models.ProxyResourceWithoutSystemData` was removed

#### `models.DeletedVaultListResult` was removed

#### `models.DeletedManagedHsmListResult` was removed

#### `models.DeletedVaultProperties` was modified

* `DeletedVaultProperties()` was changed to private access

#### `models.PrivateEndpointConnectionsPutHeaders` was modified

* `withRetryAfter(java.lang.Integer)` was removed
* `withAzureAsyncOperation(java.lang.String)` was removed

#### `models.MhsmPrivateEndpointConnectionItem` was modified

* `MhsmPrivateEndpointConnectionItem()` was changed to private access
* `withId(java.lang.String)` was removed
* `withPrivateLinkServiceConnectionState(models.MhsmPrivateLinkServiceConnectionState)` was removed
* `withProvisioningState(models.PrivateEndpointConnectionProvisioningState)` was removed
* `withEtag(java.lang.String)` was removed
* `withPrivateEndpoint(models.MhsmPrivateEndpoint)` was removed

#### `models.PrivateEndpointConnectionItem` was modified

* `PrivateEndpointConnectionItem()` was changed to private access
* `withProvisioningState(models.PrivateEndpointConnectionProvisioningState)` was removed
* `withPrivateEndpoint(models.PrivateEndpoint)` was removed
* `withPrivateLinkServiceConnectionState(models.PrivateLinkServiceConnectionState)` was removed
* `withId(java.lang.String)` was removed
* `withEtag(java.lang.String)` was removed

#### `models.ManagedHsmSecurityDomainProperties` was modified

* `ManagedHsmSecurityDomainProperties()` was changed to private access

#### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withSupportedTimeGrainTypes(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withDimensions(java.util.List)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withLockAggregationType(java.lang.String)` was removed
* `withUnit(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withSupportedAggregationTypes(java.util.List)` was removed
* `withInternalMetricName(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed

#### `models.Error` was modified

* `Error()` was changed to private access

#### `models.MhsmPrivateLinkResource` was modified

* `MhsmPrivateLinkResource()` was changed to private access
* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withIdentity(models.ManagedServiceIdentity)` was removed
* `withSku(models.ManagedHsmSku)` was removed
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.DeletedManagedHsmProperties` was modified

* `DeletedManagedHsmProperties()` was changed to private access

#### `models.MhsmPrivateEndpointConnectionsPutHeaders` was modified

* `withAzureAsyncOperation(java.lang.String)` was removed
* `withRetryAfter(java.lang.Integer)` was removed

#### `models.ManagedHsmResource` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed
* `models.ManagedHsmResource withIdentity(models.ManagedServiceIdentity)` -> `models.ManagedHsmResource withIdentity(models.ManagedServiceIdentity)`
* `models.ManagedHsmResource withLocation(java.lang.String)` -> `models.ManagedHsmResource withLocation(java.lang.String)`
* `models.ManagedHsmResource withSku(models.ManagedHsmSku)` -> `models.ManagedHsmResource withSku(models.ManagedHsmSku)`
* `models.ManagedHsmResource withTags(java.util.Map)` -> `models.ManagedHsmResource withTags(java.util.Map)`

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `models.LogSpecification` was modified

* `LogSpecification()` was changed to private access
* `withBlobDuration(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.DimensionProperties` was modified

* `DimensionProperties()` was changed to private access
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withToBeExportedForShoebox(java.lang.Boolean)` was removed

#### `models.PrivateLinkResource` was modified

* `PrivateLinkResource()` was changed to private access
* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withLogSpecifications(java.util.List)` was removed
* `withMetricSpecifications(java.util.List)` was removed

### Features Added

* `models.MhsmServiceTagRule` was added

#### `models.MhsmPrivateLinkResource` was modified

* `tags()` was added
* `identity()` was added
* `location()` was added
* `sku()` was added

#### `models.MhsmNetworkRuleSet` was modified

* `serviceTags()` was added
* `withServiceTags(java.util.List)` was added

#### `models.ManagedHsmResource` was modified

* `location()` was added
* `tags()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added
* `tags()` was added
* `location()` was added

## 2.53.4 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.3 (2025-09-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.53.1` to version `2.53.3`.
- Upgraded `azure-security-keyvault-keys` from `4.10.2` to version `4.10.3`.
- Upgraded `azure-security-keyvault-secrets` from `4.10.2` to version `4.10.3`.
- Upgraded `azure-resourcemanager-authorization` from `2.53.1` to version `2.53.3`.

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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

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

### Features Added

- Supported disabling public network access in `Vault` via `disablePublicNetworkAccess()`, for private link feature.

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

- Updated `api-version` to `2023-07-01`.
- 
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

### Features Added

- Supported `ManagedHsm` for Azure Key Vault Managed HSM.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-02-01`.

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

### Breaking Changes

- Removed class `PrivateEndpointConnectionsDeleteHeaders`.
- Removed class `PrivateEndpointConnectionsDeleteResponse`.
- Added parameter `HttpHeaders` to the constructor of class `PrivateEndpointConnectionsPutHeaders`.
- Changed return type of method `actionsRequired` in `PrivateLinkServiceConnectionState` from `String` to `ActionsRequired`.
- Changed parameter type of method `withActionsRequired` in `PrivateLinkServiceConnectionState` from `String` to `ActionsRequired`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-10-01`.

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

- Updated core dependency from resources.

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
- Updated `api-version` to `2019-09-01`
- Soft-delete protection in `Vault` is enabled by default. A soft-deleted `Vault` can be purged via `Vaults.purgeDeleted`.
- Supported `withRoleBasedAccessControl` for `Vault`

## 2.4.0 (2021-04-28)

- Supported Private Link in `Vault`

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Supported `enableByNameAndVersion` and `disableByNameAndVersion` method in `Secrets`.
- Added `enabled` method in `Secret`.
- Renamed `getAttributes`, `getTags`, `isManaged` method to `attributes`, `tags`, `managed` in `Key`.
- Updated `list` method in `Keys` and `Secrets`. It will no longer retrieve key and secret value. Key can be retrieved via `getJsonWebKey`. Secret value can be retrieved via `getValue`.

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
