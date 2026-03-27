# Release History

## 2.55.0-beta.1 (2026-03-27)

### Breaking Changes

#### `models.SearchServiceListResult` was removed

#### `models.PrivateLinkResourcesResult` was removed

#### `models.SharedPrivateLinkResourceListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.QuotaUsagesListResult` was removed

#### `models.NetworkSecurityPerimeterConfigurationListResult` was removed

#### `models.OperationListResult` was removed

#### `models.ListQueryKeysResult` was removed

#### `models.NetworkSecurityPerimeterConfigurationProperties` was modified

* `NetworkSecurityPerimeterConfigurationProperties()` was changed to private access
* `withProfile(models.NetworkSecurityProfile)` was removed
* `withNetworkSecurityPerimeter(models.NetworkSecurityPerimeter)` was removed
* `withResourceAssociation(models.ResourceAssociation)` was removed

#### `models.AccessRuleProperties` was modified

* `AccessRuleProperties()` was changed to private access
* `withPhoneNumbers(java.util.List)` was removed
* `withDirection(models.AccessRuleDirection)` was removed
* `withAddressPrefixes(java.util.List)` was removed
* `withEmailAddresses(java.util.List)` was removed
* `withNetworkSecurityPerimeters(java.util.List)` was removed
* `withSubscriptions(java.util.List)` was removed
* `withFullyQualifiedDomainNames(java.util.List)` was removed

#### `models.ProvisioningIssue` was modified

* `ProvisioningIssue()` was changed to private access

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access

#### `models.ProvisioningIssueProperties` was modified

* `ProvisioningIssueProperties()` was changed to private access

#### `models.QuotaUsageResultName` was modified

* `QuotaUsageResultName()` was changed to private access
* `withLocalizedValue(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed

#### `models.ShareablePrivateLinkResourceProperties` was modified

* `ShareablePrivateLinkResourceProperties()` was changed to private access

#### `models.AccessRulePropertiesSubscriptionsItem` was modified

* `AccessRulePropertiesSubscriptionsItem()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.NetworkSecurityProfile` was modified

* `NetworkSecurityProfile()` was changed to private access
* `withAccessRules(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withEnabledLogCategories(java.util.List)` was removed
* `withAccessRulesVersion(java.lang.Integer)` was removed
* `withDiagnosticSettingsVersion(java.lang.Integer)` was removed

#### `models.AccessRule` was modified

* `AccessRule()` was changed to private access
* `withProperties(models.AccessRuleProperties)` was removed
* `withName(java.lang.String)` was removed

#### `models.NetworkSecurityPerimeter` was modified

* `NetworkSecurityPerimeter()` was changed to private access
* `withId(java.lang.String)` was removed
* `withPerimeterGuid(java.util.UUID)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.ShareablePrivateLinkResourceType` was modified

* `ShareablePrivateLinkResourceType()` was changed to private access

#### `models.CheckNameAvailabilityOutput` was modified

* `CheckNameAvailabilityOutput()` was changed to private access

#### `models.PrivateLinkResourceProperties` was modified

* `PrivateLinkResourceProperties()` was changed to private access

#### `models.PublicNetworkAccess` was modified

* `toString()` was removed
* `models.PublicNetworkAccess[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.ResourceAssociation` was modified

* `ResourceAssociation()` was changed to private access
* `withName(java.lang.String)` was removed
* `withAccessMode(models.ResourceAssociationAccessMode)` was removed

#### `models.SharedPrivateLinkResourceProvisioningState` was modified

* `toString()` was removed
* `models.SharedPrivateLinkResourceProvisioningState[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.SkuName` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.SkuName[] values()` -> `java.util.Collection values()`

#### `models.SharedPrivateLinkResourceStatus` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.SharedPrivateLinkResourceStatus[] values()` -> `java.util.Collection values()`

#### `models.IdentityType` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.IdentityType[] values()` -> `java.util.Collection values()`

### Features Added

* `models.AzureActiveDirectoryApplicationCredentials` was added

* `models.SkuOffering` was added

* `models.SkuLimits` was added

* `models.OfferingsByRegion` was added

* `models.DataIdentity` was added

* `models.DataNoneIdentity` was added

* `models.KnowledgeRetrieval` was added

* `models.SearchResourceEncryptionKey` was added

* `models.DataUserAssignedIdentity` was added

* `models.FeatureOffering` was added

#### `models.EncryptionWithCmk` was modified

* `withServiceLevelEncryptionKey(models.SearchResourceEncryptionKey)` was added
* `serviceLevelEncryptionKey()` was added

#### `models.SearchBypass` was modified

* `AZURE_PORTAL` was added

#### `models.PublicNetworkAccess` was modified

* `PublicNetworkAccess()` was added

#### `models.SearchServiceUpdate` was modified

* `knowledgeRetrieval()` was added
* `withKnowledgeRetrieval(models.KnowledgeRetrieval)` was added

#### `models.SharedPrivateLinkResourceProvisioningState` was modified

* `SharedPrivateLinkResourceProvisioningState()` was added

#### `models.SkuName` was modified

* `SkuName()` was added
* `SERVERLESS` was added

#### `models.SharedPrivateLinkResourceStatus` was modified

* `SharedPrivateLinkResourceStatus()` was added

#### `models.IdentityType` was modified

* `IdentityType()` was added

## 2.54.5 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.54.4 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.3 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.2 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.1 (2025-08-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0 (2025-08-12)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-05-01`.

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
