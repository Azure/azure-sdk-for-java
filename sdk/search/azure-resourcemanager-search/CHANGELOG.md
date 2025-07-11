# Release History

## 2.53.0-beta.1 (Unreleased)

### Breaking Changes

#### `SearchServiceManager` was removed

#### `models.SearchService$Definition` was removed

#### `models.SearchService$Update` was removed

#### `models.SearchService` was removed

#### `models.SearchService$UpdateStages` was removed

#### `models.SearchServices` was removed

#### `models.SearchService$DefinitionStages` was removed

#### `models.QueryKey` was removed

#### `SearchServiceManager$Configurable` was removed

#### `models.AdminKeys` was removed

#### `models.PublicNetworkAccess` was modified

* `toString()` was removed
* `models.PublicNetworkAccess[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.CheckNameAvailabilityInput` was modified

* `withType(java.lang.String)` was removed

#### `models.SharedPrivateLinkResourceProvisioningState` was modified

* `models.SharedPrivateLinkResourceProvisioningState[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed
* `toString()` was removed

#### `models.SkuName` was modified

* `models.SkuName[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

#### `models.SharedPrivateLinkResourceStatus` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.SharedPrivateLinkResourceStatus[] values()` -> `java.util.Collection values()`

#### `models.IdentityType` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.IdentityType[] values()` -> `java.util.Collection values()`

### Features Added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.AccessRuleProperties` was added

* `models.ProvisioningIssue` was added

* `models.ProvisioningIssueProperties` was added

* `models.ActionType` was added

* `models.AccessRulePropertiesSubscriptionsItem` was added

* `models.NetworkSecurityProfile` was added

* `models.AccessRule` was added

* `models.SearchBypass` was added

* `models.NetworkSecurityPerimeter` was added

* `models.Origin` was added

* `models.ResourceAssociationAccessMode` was added

* `models.NetworkSecurityPerimeterConfigurationProvisioningState` was added

* `models.NetworkSecurityPerimeterConfigurationListResult` was added

* `models.UpgradeAvailable` was added

* `models.AccessRuleDirection` was added

* `models.ResourceAssociation` was added

* `models.UserAssignedIdentity` was added

* `models.SearchDataExfiltrationProtection` was added

* `models.IssueType` was added

* `models.ComputeType` was added

* `models.Severity` was added

#### `models.NetworkRuleSet` was modified

* `bypass()` was added
* `withBypass(models.SearchBypass)` was added

#### `models.SearchServiceUpdate` was modified

* `withUpgradeAvailable(models.UpgradeAvailable)` was added
* `dataExfiltrationProtections()` was added
* `withEndpoint(java.lang.String)` was added
* `endpoint()` was added
* `withDataExfiltrationProtections(java.util.List)` was added
* `computeType()` was added
* `withComputeType(models.ComputeType)` was added
* `systemData()` was added
* `serviceUpgradedAt()` was added
* `upgradeAvailable()` was added
* `etag()` was added

#### `models.Identity` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

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

## 2.50.0-beta.1 (2025-04-07)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-02-01-preview`.

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

## 2.40.0-beta.1 (2024-06-05)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-06-01-preview`

## 2.39.0 (2024-05-24)

### Features Added

- Supported disabling public network access in `SearchService` via `disablePublicNetworkAccess()`, for private link feature.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0-beta.1 (2024-03-07)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-03-01-preview`

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

- Updated `api-version` to `2023-11-01`.

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

### Breaking Changes

- Removed unused classes.

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
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Migrated from previous sdk and updated `api-version` to `2020-08-01`.
