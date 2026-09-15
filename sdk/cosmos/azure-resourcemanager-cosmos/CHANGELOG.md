# Release History

## 2.56.0-beta.1 (2026-08-20)

- Package api-version 2026-04-01-preview.

### Features Added

* `models.ThroughputPoolUpdate` was added

* `models.CassandraRUToCassandraRUCopyJobTask` was added

* `models.SoftDeletedSqlContainerProperties` was added

* `models.GarnetCacheProvisioningState` was added

* `models.SoftDeletedDatabaseAccountResource` was added

* `models.CosmosDBMongoCollection` was added

* `models.CassandraViewCreateUpdateParameters` was added

* `models.ResourceAssociation` was added

* `models.GraphResourceGetPropertiesOptions` was added

* `models.CosmosMongoDataTransferDataSourceSink` was added

* `models.DataTransferDataSourceSink` was added

* `models.NoSqlRUToNoSqlRUCopyJobTask` was added

* `models.CosmosCassandraDataTransferDataSourceSink` was added

* `models.CosmosSqlDataTransferDataSourceSink` was added

* `models.CapacityModeTransitionStatus` was added

* `models.SoftDeletedDatabaseAccountProperties` was added

* `models.GarnetClusterResourcePatch` was added

* `models.PhysicalPartitionStorageInfo` was added

* `models.GarnetClusterResourcePatchProperties` was added

* `models.BlobToCassandraRUCopyJobTask` was added

* `models.CosmosDBMongoVCoreCollection` was added

* `models.CapacityMode` was added

* `models.CopyJobStatus` was added

* `models.MergeParameters` was added

* `models.MongoRUToMongoRUCopyJobTask` was added

* `models.SoftDeleteConfiguration` was added

* `models.CassandraViewResource` was added

* `models.GarnetAuthenticationType` was added

* `models.GraphResource` was added

* `models.SoftDeletionMetadata` was added

* `models.CassandraRUToBlobCopyJobTask` was added

* `models.AccessRulePropertiesSubscription` was added

* `models.IssueType` was added

* `models.ResourceAssociationAccessMode` was added

* `models.EnableFullTextQuery` was added

* `models.DataMaskingPolicyIncludedPathsItem` was added

* `models.CassandraRUToBlobCopyJobProperties` was added

* `models.DiagnosticLogSettings` was added

* `models.NetworkSecurityPerimeterConfigurationProperties` was added

* `models.CosmosDBCassandraTable` was added

* `models.CommandAsyncPostBody` was added

* `models.SoftDeletedSqlDatabaseResource` was added

* `models.BaseCopyJobTask` was added

* `models.BackupState` was added

* `models.BlobToCassandraRUCopyJobProperties` was added

* `models.RetrieveThroughputPropertiesResource` was added

* `models.MongoRUToMongoRUCopyJobProperties` was added

* `models.RetrieveThroughputParameters` was added

* `models.AzureBlobDataTransferDataSourceSink` was added

* `models.CosmosDBSourceSinkDetails` was added

* `models.DataMaskingPolicy` was added

* `models.PhysicalPartitionThroughputInfoResultPropertiesResource` was added

* `models.MongoRUToMongoVCoreCopyJobTask` was added

* `models.AzureBlobContainer` was added

* `models.DataMaskingPolicyExcludedPathsItem` was added

* `models.SupportedActions` was added

* `models.CommandStatus` was added

* `models.DataTransferJobMode` was added

* `models.PhysicalPartitionThroughputInfoResource` was added

* `models.MaterializedViewDetails` was added

* `models.NetworkSecurityProfile` was added

* `models.CosmosMongoVCoreDataTransferDataSourceSink` was added

* `models.Severity` was added

* `models.PhysicalPartitionId` was added

* `models.BaseCosmosDataTransferDataSourceSink` was added

* `models.AzureBlobSourceSinkDetails` was added

* `models.NoSqlRUToNoSqlRUCopyJobProperties` was added

* `models.MaterializedViewDefinition` was added

* `models.MongoRUToMongoVCoreCopyJobProperties` was added

* `models.DataTransferComponent` was added

* `models.PhysicalPartitionThroughputInfoProperties` was added

* `models.RedistributeThroughputPropertiesResource` was added

* `models.ThroughputBucketResource` was added

* `models.RedistributeThroughputParameters` was added

* `models.BaseCopyJobProperties` was added

* `models.CosmosDBNoSqlContainer` was added

* `models.AccessRuleDirection` was added

* `models.CapacityModeChangeTransitionState` was added

* `models.GraphResourceGetPropertiesResource` was added

* `models.NetworkSecurityPerimeter` was added

* `models.CopyJobMode` was added

* `models.NetworkSecurityPerimeterConfigurationProvisioningState` was added

* `models.GarnetClusterResourceProperties` was added

* `models.SoftDeletedSqlDatabaseProperties` was added

* `models.CassandraViewGetPropertiesResource` was added

* `models.CassandraRUToCassandraRUCopyJobProperties` was added

* `models.MaterializedViewsProperties` was added

* `models.AccessRule` was added

* `models.ProvisioningIssueProperties` was added

* `models.CreateJobRequest` was added

* `models.ClusterType` was added

* `models.MongoVCoreSourceSinkDetails` was added

* `models.CopyJobProperties` was added

* `models.GarnetClusterResourcePropertiesEndPointsItem` was added

* `models.CassandraViewGetPropertiesOptions` was added

* `models.AccessRuleProperties` was added

* `models.CopyJobType` was added

* `models.SoftDeletedSqlContainerResource` was added

* `models.ThroughputPolicyType` was added

* `models.AllocationState` was added

* `models.SoftDeleteActionKind` was added

* `models.GraphResourceCreateUpdateParameters` was added

* `models.FleetAnalyticsPropertiesStorageLocationType` was added

* `models.ProvisioningIssue` was added

#### `models.DatabaseAccountUpdateParameters` was modified

* `enableAllVersionsAndDeletesChangeFeed()` was added
* `capacityMode()` was added
* `withCapacityMode(models.CapacityMode)` was added
* `enableMaterializedViews()` was added
* `softDeleteConfiguration()` was added
* `withEnableMaterializedViews(java.lang.Boolean)` was added
* `withSoftDeleteConfiguration(models.SoftDeleteConfiguration)` was added
* `withDiagnosticLogSettings(models.DiagnosticLogSettings)` was added
* `withEnableAllVersionsAndDeletesChangeFeed(java.lang.Boolean)` was added
* `diagnosticLogSettings()` was added

#### `models.AccountKeyMetadata` was modified

* `approximateLastUsageTime()` was added

#### `models.DatabaseAccountCreateUpdateParameters` was modified

* `withSoftDeleteConfiguration(models.SoftDeleteConfiguration)` was added
* `withCapacityMode(models.CapacityMode)` was added
* `enableAllVersionsAndDeletesChangeFeed()` was added
* `withDiagnosticLogSettings(models.DiagnosticLogSettings)` was added
* `softDeleteConfiguration()` was added
* `diagnosticLogSettings()` was added
* `withEnableAllVersionsAndDeletesChangeFeed(java.lang.Boolean)` was added
* `withEnableMaterializedViews(java.lang.Boolean)` was added
* `enableMaterializedViews()` was added
* `capacityMode()` was added

#### `models.ClusterResourceProperties` was modified

* `withClusterType(models.ClusterType)` was added
* `clusterType()` was added

#### `models.DatabaseAccountRegenerateKeyParameters` was modified

* `withSkipAccountKeysLastUsageCheck(java.lang.Boolean)` was added
* `skipAccountKeysLastUsageCheck()` was added

#### `models.SqlContainerResource` was modified

* `materializedViewsProperties()` was added
* `materializedViews()` was added
* `withMaterializedViewDefinition(models.MaterializedViewDefinition)` was added
* `withDataMaskingPolicy(models.DataMaskingPolicy)` was added
* `withMaterializedViews(java.util.List)` was added
* `withMaterializedViewsProperties(models.MaterializedViewsProperties)` was added
* `dataMaskingPolicy()` was added
* `materializedViewDefinition()` was added

#### `models.ThroughputSettingsResource` was modified

* `withThroughputBuckets(java.util.List)` was added
* `throughputBuckets()` was added

#### `models.ContinuousTier` was modified

* `CONTINUOUS35DAYS` was added

## 2.55.1 (2026-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.2` to version `2.54.3`.

## 2.55.0 (2026-07-01)

### Other Changes

- Updated `api-version` to `2026-03-15`.

## 2.54.4 (2026-07-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.1` to version `2.54.2`.


## 2.54.3 (2026-05-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.54.0` to version `2.54.1`.

## 2.54.2 (2026-03-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-resources` from `2.53.5` to version `2.54.0`.


## 2.54.1 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.54.0 (2025-11-18)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-10-15`.

## 2.54.0-beta.1 (2025-11-13)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-11-01-preview`.

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

### Features Added

- Supported `automaticFailoverEnabled()`, `enableAutomaticFailover` and `disableAutomaticFailover` in `CosmosDBAccount`.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-04-15`.

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

- Updated `api-version` to `2024-11-15`.

## 2.46.0-beta.1 (2024-12-09)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-12-01-preview`.

## 2.45.0 (2024-11-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.44.1 (2024-11-07)

### Bugs Fixed

- Removed wrong validation on non-required `location` property for some classes.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0-beta.1 (2024-09-13)

### Other Changes

### Dependency Updates

- Updated `api-version` to `2024-09-01-preview`.

## 2.42.0 (2024-08-23)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Breaking Changes

- Properties under `ServiceResourceCreateUpdateParameters` class is moved to `ServiceResourceCreateUpdateProperties` class of its "properties" property.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-05-15`.

## 2.40.0-beta.1 (2024-06-04)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-05-15-preview`.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.38.0 (2024-04-16)

### Features Added

- Supported disabling public network access in `CosmosDBAccount` via `disablePublicNetworkAccess()`, for private link feature.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0-beta.1 (2024-03-07)

### Other Changes

#### Dependency Updates

- Preview release for `api-version` `2024-02-15-preview`.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0-beta.1 (2024-01-24)

- Preview release for `api-version` `2023-11-15-preview`.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-11-15`.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0-beta.1 (2023-10-16)

- Preview release for `api-version` `2023-09-15-preview`.

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

## 2.28.0-beta.1 (2023-05-31)

- Preview release for `api-version` `2023-03-15-preview`.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-04-15`.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-03-15`.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-11-15`.

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

- Updated `api-version` to `2022-08-15`.

## 2.18.0 (2022-08-26)

### Breaking Changes

- Fixed incorrect class name of `CassandraClusterPublicStatusDataCentersItemNode`.

## 2.17.0 (2022-07-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-05-15`.

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

- Updated `api-version` to `2021-10-15`.

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

- Updated `api-version` to `2021-05-15`

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated `api-version` to `2021-03-15`
- Removed `PrivateEndpointConnectionAutoGenerated` and `PrivateLinkServiceConnectionStatePropertyAutoGenerated`, they are duplicate class of `PrivateEndpointConnection` and `PrivateLinkServiceConnectionStateProperty`, respectively.

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2020-09-01`
- Deprecated `ipRangeFilter` and `withIpRangeFilter`, replaced by `ipRules` and `withIpRules`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
