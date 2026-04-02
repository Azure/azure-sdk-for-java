# Release History

## 2.49.0-beta.1 (2026-04-02)

### Breaking Changes

#### `models.ApplicationGroupListResult` was removed

#### `models.AuthorizationRuleListResult` was removed

#### `models.ArmDisasterRecoveryListResult` was removed

#### `models.NetworkSecurityPerimeterConfiguration` was removed

#### `models.EHNamespaceListResult` was removed

#### `models.SchemaGroupListResult` was removed

#### `models.OperationListResult` was removed

#### `models.EventHubListResult` was removed

#### `models.ClusterListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.ConsumerGroupListResult` was removed

#### `models.NspAccessRuleProperties` was modified

* `NspAccessRuleProperties()` was changed to private access
* `withDirection(models.NspAccessRuleDirection)` was removed
* `withSubscriptions(java.util.List)` was removed
* `withAddressPrefixes(java.util.List)` was removed

#### `models.ProvisioningIssueProperties` was modified

* `ProvisioningIssueProperties()` was changed to private access
* `withIssueType(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.NetworkSecurityPerimeter` was modified

* `NetworkSecurityPerimeter()` was changed to private access
* `withId(java.lang.String)` was removed
* `withPerimeterGuid(java.lang.String)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.PrivateLinkResource` was modified

* `PrivateLinkResource()` was changed to private access
* `withGroupId(java.lang.String)` was removed
* `withRequiredZoneNames(java.util.List)` was removed
* `withRequiredMembers(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withType(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.NspAccessRule` was modified

* `NspAccessRule()` was changed to private access
* `withName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.NetworkSecurityPerimeterConfigurationPropertiesResourceAssociation` was modified

* `NetworkSecurityPerimeterConfigurationPropertiesResourceAssociation()` was changed to private access
* `withAccessMode(models.ResourceAssociationAccessMode)` was removed
* `withName(java.lang.String)` was removed

#### `models.EHNamespaceIdContainer` was modified

* `EHNamespaceIdContainer()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.NetworkSecurityPerimeterConfigurationPropertiesProfile` was modified

* `NetworkSecurityPerimeterConfigurationPropertiesProfile()` was changed to private access
* `withAccessRulesVersion(java.lang.String)` was removed
* `withAccessRules(java.util.List)` was removed
* `withName(java.lang.String)` was removed

#### `models.NspAccessRulePropertiesSubscriptionsItem` was modified

* `NspAccessRulePropertiesSubscriptionsItem()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access

#### `models.AvailableCluster` was modified

* `AvailableCluster()` was changed to private access
* `withLocation(java.lang.String)` was removed

#### `models.ProvisioningIssue` was modified

* `ProvisioningIssue()` was changed to private access
* `withName(java.lang.String)` was removed

### Features Added

* `models.PlatformCapabilities` was added

* `models.NamespaceReplicaLocation` was added

* `models.TimestampType` was added

* `models.MessageTimestampDescription` was added

* `models.GeoDRRoleType` was added

* `models.GeoDataReplicationProperties` was added

* `models.FailOver` was added

* `models.ConfidentialCompute` was added

* `models.Mode` was added

#### `models.RetentionDescription` was modified

* `minCompactionLagTimeInMinutes()` was added
* `withMinCompactionLagTimeInMinutes(java.lang.Long)` was added

#### `models.CleanupPolicyRetentionDescription` was modified

* `DELETE_OR_COMPACT` was added

#### `models.TlsVersion` was modified

* `ONE_THREE` was added

#### `models.SchemaType` was modified

* `PROTO_BUF` was added
* `JSON` was added

## 2.53.6 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-storage` from `2.55.1` to version `2.55.2`.

## 2.53.5 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.4 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

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

## 2.48.0-beta.1 (2025-02-14)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-05-01-preview`.

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

### Features Added

- Supported enabling zone redundant in `EventHubNamespace` via `enableZoneRedundant()`.
- Supported setting minimum Tls version in `EventHubNamespace` via `withMinimumTlsVersion(TlsVersion minimumTlsVersion)`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-01-01`.

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

## 2.33.0-beta.1 (2023-11-20)

Preview release for `api-version` `2023-01-01-preview`.

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

## 2.27.0-beta.1 (2023-04-25)

Preview release for `api-version` `2022-10-01-preview`.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

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

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.13.0 (2022-03-11)

### Features Added

- Added `PREMIUM` in class `EventHubNamespaceSkuType`.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.10.0 (2021-11-22)

### Breaking Changes

- Enum `KeyType` changed to subclass of `ExpandableStringEnum`.
- `RegionsClient` removed.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-11-01`.

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

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0-beta.5 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Migrated from previous sdk
