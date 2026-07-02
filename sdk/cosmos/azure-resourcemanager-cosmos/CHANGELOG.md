# Release History

## 2.55.0 (2026-07-01)

### Breaking Changes

#### `models.MongoDBCollectionListResult` was removed

#### `models.RestorableDatabaseAccountsListResult` was removed

#### `models.RestorableSqlResourcesListResult` was removed

#### `models.RestorableSqlContainersListResult` was removed

#### `models.SqlUserDefinedFunctionListResult` was removed

#### `models.TableListResult` was removed

#### `models.FleetListResult` was removed

#### `models.RestorableSqlDatabasesListResult` was removed

#### `models.MongoDBDatabaseListResult` was removed

#### `models.PartitionUsagesResult` was removed

#### `models.ListDataCenters` was removed

#### `models.DatabaseAccountsListResult` was removed

#### `models.RestorableGremlinGraphsListResult` was removed

#### `models.SqlTriggerListResult` was removed

#### `models.ManagedCassandraArmResourceProperties` was removed

#### `models.SqlContainerListResult` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.ClientEncryptionKeysListResult` was removed

#### `models.UsagesResult` was removed

#### `models.FleetspaceAccountListResult` was removed

#### `models.PartitionMetricListResult` was removed

#### `models.GremlinDatabaseListResult` was removed

#### `models.SqlRoleDefinitionListResult` was removed

#### `models.ServiceResourceListResult` was removed

#### `models.FleetspaceListResult` was removed

#### `models.RestorableMongodbDatabasesListResult` was removed

#### `models.RestorableMongodbCollectionsListResult` was removed

#### `models.GremlinGraphListResult` was removed

#### `models.LocationListResult` was removed

#### `models.MongoRoleDefinitionListResult` was removed

#### `models.PercentileMetricListResult` was removed

#### `models.SqlRoleAssignmentListResult` was removed

#### `models.RestorableGremlinResourcesListResult` was removed

#### `models.MetricListResult` was removed

#### `models.SqlStoredProcedureListResult` was removed

#### `models.MongoIndexKeys` was removed

#### `models.ListClusters` was removed

#### `models.RestorableTablesListResult` was removed

#### `models.RestorableTableResourcesListResult` was removed

#### `models.ExtendedResourceProperties` was removed

#### `models.RestorableGremlinDatabasesListResult` was removed

#### `models.CassandraTableListResult` was removed

#### `models.OperationListResult` was removed

#### `models.MetricDefinitionsListResult` was removed

#### `models.NotebookWorkspaceListResult` was removed

#### `models.RestorableMongodbResourcesListResult` was removed

#### `models.MongoUserDefinitionListResult` was removed

#### `models.SqlDatabaseListResult` was removed

#### `models.CassandraKeyspaceListResult` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.MongoDBCollectionGetPropertiesOptions` was modified

* `MongoDBCollectionGetPropertiesOptions()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.PercentileMetricValue` was modified

* `PercentileMetricValue()` was changed to private access

#### `models.SqlDedicatedGatewayRegionalServiceResource` was modified

* `SqlDedicatedGatewayRegionalServiceResource()` was changed to private access

#### `models.TableCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.GremlinDatabaseGetPropertiesResource` was modified

* `GremlinDatabaseGetPropertiesResource()` was changed to private access
* `withCreateMode(models.CreateMode)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed
* `withId(java.lang.String)` was removed

#### `models.SqlStoredProcedureCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.MongoIndex` was modified

* `key()` was removed
* `withKey(models.MongoIndexKeys)` was removed

#### `models.DatabaseAccountConnectionString` was modified

* `DatabaseAccountConnectionString()` was changed to private access

#### `models.MongoDBCollectionGetPropertiesResource` was modified

* `MongoDBCollectionGetPropertiesResource()` was changed to private access
* `withId(java.lang.String)` was removed
* `withShardKey(java.util.Map)` was removed
* `withIndexes(java.util.List)` was removed
* `withCreateMode(models.CreateMode)` was removed
* `withAnalyticalStorageTtl(java.lang.Integer)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed

#### `models.RestorableSqlDatabasePropertiesResourceDatabase` was modified

* `RestorableSqlDatabasePropertiesResourceDatabase()` was changed to private access
* `withId(java.lang.String)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed
* `withCreateMode(models.CreateMode)` was removed

#### `models.RestorableTablePropertiesResource` was modified

* `RestorableTablePropertiesResource()` was changed to private access

#### `models.TableGetPropertiesResource` was modified

* `TableGetPropertiesResource()` was changed to private access
* `withCreateMode(models.CreateMode)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed
* `withId(java.lang.String)` was removed

#### `models.GremlinDatabaseCreateUpdateParameters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.MaterializedViewsBuilderServiceResourceProperties` was modified

* `MaterializedViewsBuilderServiceResourceProperties()` was changed to private access
* `withInstanceSize(models.ServiceSize)` was removed
* `withInstanceCount(java.lang.Integer)` was removed

#### `models.CassandraClusterPublicStatusDataCentersItem` was modified

* `CassandraClusterPublicStatusDataCentersItem()` was changed to private access
* `withNodes(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withSeedNodes(java.util.List)` was removed

#### `models.MongoDBDatabaseGetPropertiesOptions` was modified

* `MongoDBDatabaseGetPropertiesOptions()` was changed to private access
* `withThroughput(java.lang.Integer)` was removed
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed

#### `models.LocationProperties` was modified

* `LocationProperties()` was changed to private access

#### `models.RestorableLocationResource` was modified

* `RestorableLocationResource()` was changed to private access

#### `models.MetricName` was modified

* `MetricName()` was changed to private access

#### `models.SqlTriggerGetPropertiesResource` was modified

* `SqlTriggerGetPropertiesResource()` was changed to private access
* `withBody(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withTriggerOperation(models.TriggerOperation)` was removed
* `withTriggerType(models.TriggerType)` was removed

#### `models.MongoDBDatabaseGetPropertiesResource` was modified

* `MongoDBDatabaseGetPropertiesResource()` was changed to private access
* `withCreateMode(models.CreateMode)` was removed
* `withId(java.lang.String)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed

#### `models.ThroughputSettingsUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.MongoDBDatabaseCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.DataTransferServiceResourceProperties` was modified

* `DataTransferServiceResourceProperties()` was changed to private access
* `withInstanceSize(models.ServiceSize)` was removed
* `withInstanceCount(java.lang.Integer)` was removed

#### `models.AccountKeyMetadata` was modified

* `AccountKeyMetadata()` was changed to private access

#### `models.SqlTriggerCreateUpdateParameters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.RestorableMongodbDatabasePropertiesResource` was modified

* `RestorableMongodbDatabasePropertiesResource()` was changed to private access

#### `models.ClientEncryptionKeyGetPropertiesResource` was modified

* `ClientEncryptionKeyGetPropertiesResource()` was changed to private access
* `withKeyWrapMetadata(models.KeyWrapMetadata)` was removed
* `withId(java.lang.String)` was removed
* `withWrappedDataEncryptionKey(byte[])` was removed
* `withEncryptionAlgorithm(java.lang.String)` was removed

#### `models.ConnectionError` was modified

* `ConnectionError()` was changed to private access
* `withConnectionState(models.ConnectionState)` was removed
* `withPort(java.lang.Integer)` was removed
* `withIPTo(java.lang.String)` was removed
* `withException(java.lang.String)` was removed
* `withIPFrom(java.lang.String)` was removed

#### `models.DatabaseAccountCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.TableGetPropertiesOptions` was modified

* `TableGetPropertiesOptions()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.SqlDatabaseCreateUpdateParameters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.ServiceResourceProperties` was modified

* `models.ServiceResourceProperties withInstanceCount(java.lang.Integer)` -> `models.ServiceResourceProperties withInstanceCount(java.lang.Integer)`
* `models.ServiceResourceProperties withInstanceSize(models.ServiceSize)` -> `models.ServiceResourceProperties withInstanceSize(models.ServiceSize)`
* `models.ServiceResourceProperties withAdditionalProperties(java.util.Map)` -> `models.ServiceResourceProperties withAdditionalProperties(java.util.Map)`

#### `models.FleetResourceUpdate` was modified

* `withProvisioningState(models.Status)` was removed

#### `models.SqlDedicatedGatewayServiceResourceProperties` was modified

* `SqlDedicatedGatewayServiceResourceProperties()` was changed to private access
* `withInstanceCount(java.lang.Integer)` was removed
* `withInstanceSize(models.ServiceSize)` was removed
* `withDedicatedGatewayType(models.DedicatedGatewayType)` was removed
* `withSqlDedicatedGatewayEndpoint(java.lang.String)` was removed

#### `models.RestorableGremlinDatabasePropertiesResource` was modified

* `RestorableGremlinDatabasePropertiesResource()` was changed to private access

#### `models.GraphApiComputeRegionalServiceResource` was modified

* `GraphApiComputeRegionalServiceResource()` was changed to private access

#### `models.SqlUserDefinedFunctionGetPropertiesResource` was modified

* `SqlUserDefinedFunctionGetPropertiesResource()` was changed to private access
* `withBody(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.ThroughputSettingsGetPropertiesResource` was modified

* `ThroughputSettingsGetPropertiesResource()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettingsResource)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.MongoDBCollectionCreateUpdateParameters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.GremlinGraphGetPropertiesOptions` was modified

* `GremlinGraphGetPropertiesOptions()` was changed to private access
* `withThroughput(java.lang.Integer)` was removed
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed

#### `models.SqlContainerCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.CassandraKeyspaceCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.CassandraClusterPublicStatusDataCentersItemNode` was modified

* `CassandraClusterPublicStatusDataCentersItemNode()` was changed to private access
* `withDiskFreeKB(java.lang.Long)` was removed
* `withRack(java.lang.String)` was removed
* `withLoad(java.lang.String)` was removed
* `withMemoryUsedKB(java.lang.Long)` was removed
* `withTokens(java.util.List)` was removed
* `withCassandraProcessStatus(java.lang.String)` was removed
* `withMemoryBuffersAndCachedKB(java.lang.Long)` was removed
* `withTimestamp(java.lang.String)` was removed
* `withDiskUsedKB(java.lang.Long)` was removed
* `withMemoryFreeKB(java.lang.Long)` was removed
* `withAddress(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withState(models.NodeState)` was removed
* `withMemoryTotalKB(java.lang.Long)` was removed
* `withHostId(java.lang.String)` was removed
* `withSize(java.lang.Integer)` was removed
* `withCpuUsage(java.lang.Double)` was removed

#### `models.RestorableSqlContainerPropertiesResource` was modified

* `RestorableSqlContainerPropertiesResource()` was changed to private access
* `withContainer(models.RestorableSqlContainerPropertiesResourceContainer)` was removed

#### `models.OptionsResource` was modified

* `models.OptionsResource withThroughput(java.lang.Integer)` -> `models.OptionsResource withThroughput(java.lang.Integer)`
* `models.OptionsResource withAutoscaleSettings(models.AutoscaleSettings)` -> `models.OptionsResource withAutoscaleSettings(models.AutoscaleSettings)`

#### `models.FleetspaceUpdate` was modified

* `withProvisioningState(models.Status)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed

#### `models.ArmResourceProperties` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.SqlStoredProcedureGetPropertiesResource` was modified

* `SqlStoredProcedureGetPropertiesResource()` was changed to private access
* `withBody(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.MaterializedViewsBuilderRegionalServiceResource` was modified

* `MaterializedViewsBuilderRegionalServiceResource()` was changed to private access

#### `models.GraphApiComputeServiceResourceProperties` was modified

* `GraphApiComputeServiceResourceProperties()` was changed to private access
* `withInstanceCount(java.lang.Integer)` was removed
* `withInstanceSize(models.ServiceSize)` was removed
* `withGraphApiComputeEndpoint(java.lang.String)` was removed

#### `models.MetricAvailability` was modified

* `MetricAvailability()` was changed to private access

#### `models.CassandraTableGetPropertiesResource` was modified

* `CassandraTableGetPropertiesResource()` was changed to private access
* `withId(java.lang.String)` was removed
* `withAnalyticalStorageTtl(java.lang.Integer)` was removed
* `withSchema(models.CassandraSchema)` was removed
* `withDefaultTtl(java.lang.Integer)` was removed

#### `models.SqlDatabaseGetPropertiesResource` was modified

* `SqlDatabaseGetPropertiesResource()` was changed to private access
* `withCreateMode(models.CreateMode)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed
* `withUsers(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withColls(java.lang.String)` was removed

#### `models.DataTransferRegionalServiceResource` was modified

* `DataTransferRegionalServiceResource()` was changed to private access

#### `models.SqlContainerGetPropertiesOptions` was modified

* `SqlContainerGetPropertiesOptions()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.SqlContainerGetPropertiesResource` was modified

* `SqlContainerGetPropertiesResource()` was changed to private access
* `withPartitionKey(models.ContainerPartitionKey)` was removed
* `withAnalyticalStorageTtl(java.lang.Long)` was removed
* `withDefaultTtl(java.lang.Integer)` was removed
* `withFullTextPolicy(models.FullTextPolicy)` was removed
* `withIndexingPolicy(models.IndexingPolicy)` was removed
* `withConflictResolutionPolicy(models.ConflictResolutionPolicy)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed
* `withClientEncryptionPolicy(models.ClientEncryptionPolicy)` was removed
* `withCreateMode(models.CreateMode)` was removed
* `withComputedProperties(java.util.List)` was removed
* `withUniqueKeyPolicy(models.UniqueKeyPolicy)` was removed
* `withVectorEmbeddingPolicy(models.VectorEmbeddingPolicy)` was removed
* `withId(java.lang.String)` was removed

#### `models.RestorableSqlContainerPropertiesResourceContainer` was modified

* `RestorableSqlContainerPropertiesResourceContainer()` was changed to private access
* `withVectorEmbeddingPolicy(models.VectorEmbeddingPolicy)` was removed
* `withClientEncryptionPolicy(models.ClientEncryptionPolicy)` was removed
* `withComputedProperties(java.util.List)` was removed
* `withCreateMode(models.CreateMode)` was removed
* `withPartitionKey(models.ContainerPartitionKey)` was removed
* `withConflictResolutionPolicy(models.ConflictResolutionPolicy)` was removed
* `withUniqueKeyPolicy(models.UniqueKeyPolicy)` was removed
* `withDefaultTtl(java.lang.Integer)` was removed
* `withFullTextPolicy(models.FullTextPolicy)` was removed
* `withIndexingPolicy(models.IndexingPolicy)` was removed
* `withId(java.lang.String)` was removed
* `withAnalyticalStorageTtl(java.lang.Long)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed

#### `models.CassandraKeyspaceGetPropertiesOptions` was modified

* `CassandraKeyspaceGetPropertiesOptions()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.CassandraTableGetPropertiesOptions` was modified

* `CassandraTableGetPropertiesOptions()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.RestorableSqlDatabasePropertiesResource` was modified

* `RestorableSqlDatabasePropertiesResource()` was changed to private access
* `withDatabase(models.RestorableSqlDatabasePropertiesResourceDatabase)` was removed

#### `models.SqlDatabaseGetPropertiesOptions` was modified

* `SqlDatabaseGetPropertiesOptions()` was changed to private access
* `withThroughput(java.lang.Integer)` was removed
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed

#### `models.GremlinGraphGetPropertiesResource` was modified

* `GremlinGraphGetPropertiesResource()` was changed to private access
* `withCreateMode(models.CreateMode)` was removed
* `withAnalyticalStorageTtl(java.lang.Long)` was removed
* `withId(java.lang.String)` was removed
* `withUniqueKeyPolicy(models.UniqueKeyPolicy)` was removed
* `withConflictResolutionPolicy(models.ConflictResolutionPolicy)` was removed
* `withIndexingPolicy(models.IndexingPolicy)` was removed
* `withDefaultTtl(java.lang.Integer)` was removed
* `withPartitionKey(models.ContainerPartitionKey)` was removed
* `withRestoreParameters(models.ResourceRestoreParameters)` was removed

#### `models.DatabaseAccountKeysMetadata` was modified

* `DatabaseAccountKeysMetadata()` was changed to private access

#### `models.ManagedCassandraReaperStatus` was modified

* `ManagedCassandraReaperStatus()` was changed to private access
* `withRepairRunIds(java.util.Map)` was removed
* `withRepairSchedules(java.util.Map)` was removed
* `withHealthy(java.lang.Boolean)` was removed

#### `models.SqlUserDefinedFunctionCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

#### `models.ContinuousBackupInformation` was modified

* `ContinuousBackupInformation()` was changed to private access
* `withLatestRestorableTimestamp(java.lang.String)` was removed

#### `models.CassandraKeyspaceGetPropertiesResource` was modified

* `CassandraKeyspaceGetPropertiesResource()` was changed to private access
* `withId(java.lang.String)` was removed

#### `models.RestorableMongodbCollectionPropertiesResource` was modified

* `RestorableMongodbCollectionPropertiesResource()` was changed to private access

#### `models.GremlinDatabaseGetPropertiesOptions` was modified

* `GremlinDatabaseGetPropertiesOptions()` was changed to private access
* `withAutoscaleSettings(models.AutoscaleSettings)` was removed
* `withThroughput(java.lang.Integer)` was removed

#### `models.CassandraTableCreateUpdateParameters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed

#### `models.RestorableGremlinGraphPropertiesResource` was modified

* `RestorableGremlinGraphPropertiesResource()` was changed to private access

#### `models.GremlinGraphCreateUpdateParameters` was modified

* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed

### Features Added

* `models.AutoReplicate` was added

* `models.ScheduledEventStrategy` was added

* `models.BackupSchedule` was added

#### `models.MongoDBCollectionGetPropertiesOptions` was modified

* `throughput()` was added
* `autoscaleSettings()` was added

#### `models.DatabaseAccountUpdateParameters` was modified

* `enforceHierarchicalPartitionKeyIdLastLevel()` was added
* `withEnforceHierarchicalPartitionKeyIdLastLevel(java.lang.Boolean)` was added

#### `models.TableCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.SqlStoredProcedureCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MongoIndex` was modified

* `withKeys(java.util.List)` was added
* `keys()` was added

#### `models.GremlinDatabaseCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MongoDBDatabaseGetPropertiesOptions` was modified

* `autoscaleSettings()` was added
* `throughput()` was added

#### `models.Permission` was modified

* `withId(java.lang.String)` was added
* `id()` was added

#### `models.ThroughputSettingsUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.MongoDBDatabaseCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.SqlTriggerCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.DatabaseAccountCreateUpdateParameters` was modified

* `withEnforceHierarchicalPartitionKeyIdLastLevel(java.lang.Boolean)` was added
* `enforceHierarchicalPartitionKeyIdLastLevel()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.TableGetPropertiesOptions` was modified

* `throughput()` was added
* `autoscaleSettings()` was added

#### `models.SqlDatabaseCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.FleetResourceUpdate` was modified

* `tags()` was added
* `withTags(java.util.Map)` was added

#### `models.MongoDBCollectionCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.FleetspacePropertiesThroughputPoolConfiguration` was modified

* `withMaxConsumableRUs(java.lang.Long)` was added
* `withDedicatedRUs(java.lang.Long)` was added
* `dedicatedRUs()` was added
* `maxConsumableRUs()` was added

#### `models.GremlinGraphGetPropertiesOptions` was modified

* `autoscaleSettings()` was added
* `throughput()` was added

#### `models.SqlContainerCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CassandraKeyspaceCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CassandraClusterPublicStatusDataCentersItemNode` was modified

* `isLatestModel()` was added

#### `models.ClusterResourceProperties` was modified

* `withExternalDataCenters(java.util.List)` was added
* `autoReplicate()` was added
* `backupSchedules()` was added
* `withBackupSchedules(java.util.List)` was added
* `externalDataCenters()` was added
* `withScheduledEventStrategy(models.ScheduledEventStrategy)` was added
* `scheduledEventStrategy()` was added
* `withAutoReplicate(models.AutoReplicate)` was added
* `withExtensions(java.util.List)` was added
* `extensions()` was added

#### `models.ArmResourceProperties` was modified

* `location()` was added
* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `tags()` was added

#### `models.RestoreParameters` was modified

* `restoreTimestampInUtc()` was added
* `restoreSource()` was added

#### `models.SqlContainerGetPropertiesOptions` was modified

* `throughput()` was added
* `autoscaleSettings()` was added

#### `models.CassandraKeyspaceGetPropertiesOptions` was modified

* `throughput()` was added
* `autoscaleSettings()` was added

#### `models.CassandraTableGetPropertiesOptions` was modified

* `throughput()` was added
* `autoscaleSettings()` was added

#### `models.SqlDatabaseGetPropertiesOptions` was modified

* `autoscaleSettings()` was added
* `throughput()` was added

#### `models.SqlUserDefinedFunctionCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.GremlinDatabaseGetPropertiesOptions` was modified

* `autoscaleSettings()` was added
* `throughput()` was added

#### `models.CassandraTableCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.GremlinGraphCreateUpdateParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

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
