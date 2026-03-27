# Release History

## 2.56.0 (2026-03-27)

### Breaking Changes

#### `models.ListQueueResource` was removed

#### `models.StorageTaskAssignmentsList` was removed

#### `models.StorageTaskReportSummary` was removed

#### `models.StorageSkuListResult` was removed

#### `models.FileShareItems` was removed

#### `models.OperationListResult` was removed

#### `models.DeletedAccountListResult` was removed

#### `models.NetworkSecurityPerimeterConfigurationList` was removed

#### `models.EncryptionScopeListResult` was removed

#### `models.ListBlobInventoryPolicy` was removed

#### `models.LocalUsers` was removed

#### `models.ListContainerItems` was removed

#### `models.BlobServiceItems` was removed

#### `models.ListTableResource` was removed

#### `models.FileServiceUsages` was removed

#### `models.ObjectReplicationPolicies` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.StorageAccountListResult` was removed

#### `models.UsageListResult` was removed

#### `models.LegalHoldProperties` was modified

* `LegalHoldProperties()` was changed to private access
* `withProtectedAppendWritesHistory(models.ProtectedAppendWritesHistory)` was removed
* `withTags(java.util.List)` was removed

#### `models.FileSharesLeaseHeaders` was modified

* `withEtag(java.lang.String)` was removed

#### `models.BurstingConstants` was modified

* `BurstingConstants()` was changed to private access

#### `models.Restriction` was modified

* `Restriction()` was changed to private access
* `withReasonCode(models.ReasonCode)` was removed

#### `models.NetworkSecurityPerimeterConfigurationPropertiesProfile` was modified

* `NetworkSecurityPerimeterConfigurationPropertiesProfile()` was changed to private access
* `withName(java.lang.String)` was removed
* `withAccessRules(java.util.List)` was removed
* `withAccessRulesVersion(java.lang.Float)` was removed
* `withEnabledLogCategories(java.util.List)` was removed
* `withDiagnosticSettingsVersion(java.lang.Float)` was removed

#### `models.FileServiceUsageProperties` was modified

* `FileServiceUsageProperties()` was changed to private access

#### `models.BlobContainersGetImmutabilityPolicyHeaders` was modified

* `withEtag(java.lang.String)` was removed

#### `models.Endpoints` was modified

* `Endpoints()` was changed to private access
* `withInternetEndpoints(models.StorageAccountInternetEndpoints)` was removed
* `withIpv6Endpoints(models.StorageAccountIpv6Endpoints)` was removed
* `withMicrosoftEndpoints(models.StorageAccountMicrosoftEndpoints)` was removed

#### `models.NetworkSecurityPerimeterConfigurationPropertiesResourceAssociation` was modified

* `NetworkSecurityPerimeterConfigurationPropertiesResourceAssociation()` was changed to private access
* `withName(java.lang.String)` was removed
* `withAccessMode(models.ResourceAssociationAccessMode)` was removed

#### `models.ImmutabilityPolicyProperties` was modified

* `ImmutabilityPolicyProperties()` was changed to private access
* `withAllowProtectedAppendWritesAll(java.lang.Boolean)` was removed
* `withAllowProtectedAppendWrites(java.lang.Boolean)` was removed
* `withImmutabilityPeriodSinceCreationInDays(java.lang.Integer)` was removed

#### `models.AccountUsageElements` was modified

* `AccountUsageElements()` was changed to private access

#### `models.KeyCreationTime` was modified

* `KeyCreationTime()` was changed to private access
* `withKey1(java.time.OffsetDateTime)` was removed
* `withKey2(java.time.OffsetDateTime)` was removed

#### `models.StorageAccountIpv6Endpoints` was modified

* `StorageAccountIpv6Endpoints()` was changed to private access
* `withMicrosoftEndpoints(models.StorageAccountMicrosoftEndpoints)` was removed
* `withInternetEndpoints(models.StorageAccountInternetEndpoints)` was removed

#### `models.StorageAccountMicrosoftEndpoints` was modified

* `StorageAccountMicrosoftEndpoints()` was changed to private access

#### `models.BlobContainersExtendImmutabilityPolicyHeaders` was modified

* `withEtag(java.lang.String)` was removed

#### `models.SkuInformationLocationInfoItem` was modified

* `SkuInformationLocationInfoItem()` was changed to private access

#### `models.BlobContainersLockImmutabilityPolicyHeaders` was modified

* `withEtag(java.lang.String)` was removed

#### `models.NetworkSecurityPerimeter` was modified

* `NetworkSecurityPerimeter()` was changed to private access
* `withPerimeterGuid(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.GeoReplicationStats` was modified

* `GeoReplicationStats()` was changed to private access

#### `models.BlobContainersDeleteImmutabilityPolicyHeaders` was modified

* `withEtag(java.lang.String)` was removed

#### `models.FileShareRecommendations` was modified

* `FileShareRecommendations()` was changed to private access

#### `models.StorageAccountInternetEndpoints` was modified

* `StorageAccountInternetEndpoints()` was changed to private access

#### `models.UsageName` was modified

* `UsageName()` was changed to private access

#### `models.AccountLimits` was modified

* `AccountLimits()` was changed to private access

#### `models.MetricSpecification` was modified

* `MetricSpecification()` was changed to private access
* `withDisplayName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withResourceIdDimensionNameOverride(java.lang.String)` was removed
* `withDisplayDescription(java.lang.String)` was removed
* `withFillGapWithZero(java.lang.Boolean)` was removed
* `withAggregationType(java.lang.String)` was removed
* `withDimensions(java.util.List)` was removed
* `withUnit(java.lang.String)` was removed
* `withCategory(java.lang.String)` was removed

#### `models.NspAccessRule` was modified

* `NspAccessRule()` was changed to private access
* `withName(java.lang.String)` was removed

#### `models.ProtectedAppendWritesHistory` was modified

* `ProtectedAppendWritesHistory()` was changed to private access
* `withAllowProtectedAppendWritesAll(java.lang.Boolean)` was removed

#### `models.AccountUsage` was modified

* `AccountUsage()` was changed to private access

#### `models.NspAccessRuleProperties` was modified

* `NspAccessRuleProperties()` was changed to private access
* `withDirection(models.NspAccessRuleDirection)` was removed
* `withSubscriptions(java.util.List)` was removed
* `withAddressPrefixes(java.util.List)` was removed

#### `models.FileShareLimits` was modified

* `FileShareLimits()` was changed to private access

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withProvider(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.NspAccessRulePropertiesSubscriptionsItem` was modified

* `NspAccessRulePropertiesSubscriptionsItem()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.TagProperty` was modified

* `TagProperty()` was changed to private access

#### `models.ProvisioningIssue` was modified

* `ProvisioningIssue()` was changed to private access
* `withName(java.lang.String)` was removed

#### `models.StorageAccountKey` was modified

* `StorageAccountKey()` was changed to private access

#### `models.ServiceSpecification` was modified

* `ServiceSpecification()` was changed to private access
* `withMetricSpecifications(java.util.List)` was removed

#### `models.SkuCapability` was modified

* `SkuCapability()` was changed to private access

#### `models.PrivateLinkResource` was modified

* `PrivateLinkResource()` was changed to private access
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.Dimension` was modified

* `Dimension()` was changed to private access
* `withName(java.lang.String)` was removed
* `withDisplayName(java.lang.String)` was removed

#### `models.BlobContainersCreateOrUpdateImmutabilityPolicyHeaders` was modified

* `withEtag(java.lang.String)` was removed

#### `models.UpdateHistoryProperty` was modified

* `UpdateHistoryProperty()` was changed to private access
* `withAllowProtectedAppendWrites(java.lang.Boolean)` was removed
* `withAllowProtectedAppendWritesAll(java.lang.Boolean)` was removed

#### `models.ProvisioningIssueProperties` was modified

* `ProvisioningIssueProperties()` was changed to private access
* `withIssueType(models.IssueType)` was removed
* `withDescription(java.lang.String)` was removed
* `withSeverity(models.Severity)` was removed

### Features Added

* `models.DataShareSource` was added

* `models.StorageConnectorSourceType` was added

* `models.StorageConnectorAuthProperties` was added

* `models.StorageConnectorState` was added

* `models.StorageConnectorProperties` was added

* `models.StorageConnectorAuthType` was added

* `models.StorageConnectorSource` was added

* `models.StorageAccountSharedKeyAccessProperties` was added

* `models.StorageDataShareProperties` was added

* `models.StorageDataShareAccessPolicyPermission` was added

* `models.StorageConnectorConnection` was added

* `models.StorageDataShareAccessPolicy` was added

* `models.ObjectReplicationPolicyPropertiesTagsReplication` was added

* `models.StorageConnectorConnectionType` was added

* `models.StorageConnectorDataSourceType` was added

* `models.StorageDataShareAsset` was added

* `models.TestExistingConnectionRequest` was added

* `models.NativeDataSharingProvisioningState` was added

* `models.ManagedIdentityAuthProperties` was added

* `models.StaticWebsite` was added

* `models.StorageDataCollaborationPolicyProperties` was added

* `models.ServiceSharedKeyAccessProperties` was added

* `models.DataShareConnection` was added

#### `models.AccessTier` was modified

* `SMART` was added

#### `models.TriggerType` was modified

* `MOCK_RUN` was added

#### `models.StorageAccountUpdateParameters` was modified

* `withDataCollaborationPolicyProperties(models.StorageDataCollaborationPolicyProperties)` was added
* `allowSharedKeyAccessForServices()` was added
* `dataCollaborationPolicyProperties()` was added
* `withAllowSharedKeyAccessForServices(models.StorageAccountSharedKeyAccessProperties)` was added

#### `models.AllowedCopyScope` was modified

* `ALL` was added

#### `models.AzureEntityResource` was modified

* `systemData()` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.StorageAccountCreateParameters` was modified

* `allowSharedKeyAccessForServices()` was added
* `withAllowSharedKeyAccessForServices(models.StorageAccountSharedKeyAccessProperties)` was added
* `withDataCollaborationPolicyProperties(models.StorageDataCollaborationPolicyProperties)` was added
* `dataCollaborationPolicyProperties()` was added

## 2.55.3 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-authorization` from `2.53.5` to version `2.53.6`.
- Upgraded `azure-resourcemanager-msi` from `2.53.4` to version `2.53.5`.

## 2.55.2 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.55.1 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.55.0 (2025-10-22)

### Other Changes

- Updated `api-version` to `2025-06-01`.

## 2.54.1 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0 (2025-09-23)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-01-01`.

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

- Updated code from `api-version` `2024-01-01`.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Breaking Changes

- Added `Accepted` to `ProvisioningState` enum.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-01-01`.

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

### Breaking Changes

- Behavior changed on `StorageAccount`, as default, disallows cross-tenant replication. Supported `allowCrossTenantReplication` in `Create` mode.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-05-01`.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.38.0 (2024-04-16)

### Features Added

- Supported disabling public network access in `StorageAccount` via `disablePublicNetworkAccess()`, for private link feature.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Features Added

- Supported encryption with Customer-managed keys using user-assigned service identity.
- Supported `withExistingUserAssignedManagedServiceIdentity(String)` for `StorageAccount`. 

## 2.35.0 (2024-01-26)

### Features Added

- Supported user-assigned service identity for `StorageAccount` in create and update. 

### Other Changes

- Added dependency of `azure-resourcemanager-msi` and `azure-resourcemanager-authorization`.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Features Added

- Supported `allowCrossTenantReplication` and `defaultToOAuthAuthentication` for `StorageAccount` in create and update.

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

- Updated `api-version` to `2023-01-01`.

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

### Features Added

- Supported enabling last access time tracking policy for `BlobServiceProperties`.

### Bugs Fixed

- Fixed a bug that `StorageAccount.infrastructureEncryptionEnabled()` always returns `false`.
- Fixed a bug that `PolicyRule`s returned by `ManagementPolicy::rules()` don't support all base blob actions.

## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-09-01`.

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

- Updated `api-version` to `2022-05-01`.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.16.0 (2022-06-24)

### Features Added

- Supported toggling blob versioning in `BlobServiceProperties`.
- Supported `containerDeleteRetentionPolicy` in `BlobServiceProperties`.

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-09-01`.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Breaking Changes

- Remove field `STORAGE_FILE_DATA_SMB_SHARE_OWNER` from class `DefaultSharePermission`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-08-01`.

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

- Updated `api-version` to `2021-04-01`.

### Bugs Fixed

- Fixed bug on ETag for `ImmutabilityPolicy`.

### Breaking Changes

- Removed class `GetShareExpand`, `ListSharesExpand`, `PutSharesExpand`. Parameter is now comma-separated strings.
- Moved `destination` field from `BlobInventoryPolicySchema` class to its `rules` (`BlobInventoryPolicyRule` class).

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Supported enabling infrastructure encryption for `StorageAccount`.
- Supported enabling customer-managed key for Tables and Queues in `StorageAccount`.

## 2.4.0 (2021-04-28)

- Supported Private Link in `StorageAccount`

## 2.3.0 (2021-03-30)

- Updated `api-version` to `2021-02-01`
- Storage account default to Transport Layer Security (TLS) 1.2 for HTTPS

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2021-01-01`
- Return type of `Identity.type()` changed from `String` to `IdentityType`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
