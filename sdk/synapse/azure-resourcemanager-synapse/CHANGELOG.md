# Release History

## 1.0.0-beta.7 (2023-02-10)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-composite-v2. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentityDesiredState` was removed

* `models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentityActualState` was removed

#### `models.ServerBlobAuditingPolicy$Update` was modified

* `withStorageAccountSubscriptionId(java.util.UUID)` was removed
* `withAuditActionsAndGroups(java.util.List)` was removed
* `withRetentionDays(java.lang.Integer)` was removed

#### `models.ServerVulnerabilityAssessment$Definition` was modified

* `withRecurringScans(models.VulnerabilityAssessmentRecurringScansProperties)` was removed

#### `models.SqlPoolOperationResults` was modified

* `java.lang.Object getLocationHeaderResult(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `models.SqlPool getLocationHeaderResult(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `getLocationHeaderResultWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.SqlPoolSecurityAlertPolicy$Update` was modified

* `withEmailAddresses(java.util.List)` was removed
* `withDisabledAlerts(java.util.List)` was removed

#### `models.KustoPool$Update` was modified

* `withWorkspaceUid(java.lang.String)` was removed

#### `models.ServerVulnerabilityAssessment$Update` was modified

* `withRecurringScans(models.VulnerabilityAssessmentRecurringScansProperties)` was removed
* `withStorageContainerPath(java.lang.String)` was removed

#### `models.IpFirewallRules` was modified

* `java.lang.Object deleteById(java.lang.String)` -> `models.IpFirewallRuleInfo deleteById(java.lang.String)`
* `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.IpFirewallRuleInfo delete(java.lang.String,java.lang.String,java.lang.String)`
* `java.lang.Object deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.IpFirewallRuleInfo deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.IpFirewallRuleInfo delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.BigDataPools` was modified

* `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.BigDataPoolResourceInfo delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object deleteById(java.lang.String)` -> `models.BigDataPoolResourceInfo deleteById(java.lang.String)`
* `java.lang.Object deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.BigDataPoolResourceInfo deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.BigDataPoolResourceInfo delete(java.lang.String,java.lang.String,java.lang.String)`

#### `models.ExtendedServerBlobAuditingPolicy$Update` was modified

* `withRetentionDays(java.lang.Integer)` was removed
* `withStorageAccountSubscriptionId(java.util.UUID)` was removed

#### `models.ServerSecurityAlertPolicy$Update` was modified

* `withEmailAccountAdmins(java.lang.Boolean)` was removed
* `withStorageEndpoint(java.lang.String)` was removed
* `withEmailAddresses(java.util.List)` was removed
* `withDisabledAlerts(java.util.List)` was removed
* `withRetentionDays(java.lang.Integer)` was removed

#### `models.SqlPoolBlobAuditingPolicy$Update` was modified

* `withStorageAccountSubscriptionId(java.util.UUID)` was removed
* `withRetentionDays(java.lang.Integer)` was removed

#### `models.Workspaces` was modified

* `java.lang.Object deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.Workspace deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.Workspace delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.Workspace deleteByResourceGroup(java.lang.String,java.lang.String)`
* `java.lang.Object deleteById(java.lang.String)` -> `models.Workspace deleteById(java.lang.String)`

#### `models.BigDataPoolResourceInfo$Definition` was modified

* `withCacheSize(java.lang.Integer)` was removed

#### `models.Workspace` was modified

* `java.util.Map extraProperties()` -> `java.lang.Object extraProperties()`

#### `models.SqlPool` was modified

* `java.lang.Object pause()` -> `models.SqlPool pause()`
* `java.lang.Object resume()` -> `models.SqlPool resume()`
* `java.lang.Object pause(com.azure.core.util.Context)` -> `models.SqlPool pause(com.azure.core.util.Context)`
* `java.lang.Object resume(com.azure.core.util.Context)` -> `models.SqlPool resume(com.azure.core.util.Context)`

#### `models.Key$Update` was modified

* `withKeyVaultUrl(java.lang.String)` was removed

#### `models.SqlPool$Update` was modified

* `withRestorePointInTime(java.time.OffsetDateTime)` was removed
* `withCollation(java.lang.String)` was removed

#### `models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentity` was modified

* `models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentityActualState actualState()` -> `models.ActualState actualState()`
* `withDesiredState(models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentityDesiredState)` was removed
* `models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentityDesiredState desiredState()` -> `models.DesiredState desiredState()`

#### `models.ServerVulnerabilityAssessment` was modified

* `models.VulnerabilityAssessmentRecurringScansProperties recurringScans()` -> `models.VulnerabilityAssessmentRecurringScansPropertiesAutoGenerated recurringScans()`

#### `models.PrivateLinkResources` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String,java.lang.String)` was removed

#### `SynapseManager` was modified

* `privateLinkResources()` was removed

#### `models.SqlPools` was modified

* `java.lang.Object pause(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.SqlPool pause(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.SqlPool delete(java.lang.String,java.lang.String,java.lang.String)`
* `java.lang.Object pause(java.lang.String,java.lang.String,java.lang.String)` -> `models.SqlPool pause(java.lang.String,java.lang.String,java.lang.String)`
* `java.lang.Object resume(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.SqlPool resume(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object deleteById(java.lang.String)` -> `models.SqlPool deleteById(java.lang.String)`
* `java.lang.Object deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.SqlPool deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.SqlPool delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `java.lang.Object resume(java.lang.String,java.lang.String,java.lang.String)` -> `models.SqlPool resume(java.lang.String,java.lang.String,java.lang.String)`

#### `models.Workspace$Definition` was modified

* `withConnectivityEndpoints(java.util.Map)` was removed

### Features Added

* `models.VulnerabilityAssessmentRecurringScansPropertiesAutoGenerated` was added

* `models.WorkspaceStatus` was added

* `models.Gets` was added

* `models.IntegrationRuntimeStopOperationStatus` was added

* `models.IntegrationRuntimeOperationStatus` was added

* `models.KustoPoolPrivateLinkResources` was added

* `models.ActualState` was added

* `models.IntegrationRuntimeEnableinteractivequery` was added

* `models.KustoPoolPrivateLinkResourcesOperations` was added

* `models.PrivateLinkResourcesOperations` was added

* `models.DesiredState` was added

#### `models.ServerBlobAuditingPolicy` was modified

* `resourceGroupName()` was added

#### `models.AttachedDatabaseConfiguration` was modified

* `resourceGroupName()` was added

#### `models.ServerVulnerabilityAssessment$Definition` was modified

* `withRecurringScans(models.VulnerabilityAssessmentRecurringScansPropertiesAutoGenerated)` was added

#### `models.ExtendedSqlPoolBlobAuditingPolicy` was modified

* `resourceGroupName()` was added

#### `models.EncryptionProtector` was modified

* `resourceGroupName()` was added

#### `models.SqlPoolOperationResults` was modified

* `getLocationHeaderResult(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DataMaskingRule` was modified

* `resourceGroupName()` was added

#### `models.SelfHostedIntegrationRuntimeStatus` was modified

* `targetFramework()` was added
* `osType()` was added

#### `models.ServerVulnerabilityAssessment$Update` was modified

* `withRecurringScans(models.VulnerabilityAssessmentRecurringScansPropertiesAutoGenerated)` was added

#### `models.GeoBackupPolicy` was modified

* `resourceGroupName()` was added

#### `models.SqlPoolBlobAuditingPolicy` was modified

* `resourceGroupName()` was added

#### `models.IpFirewallRuleInfo` was modified

* `resourceGroupName()` was added

#### `models.TransparentDataEncryption` was modified

* `resourceGroupName()` was added

#### `models.WorkloadClassifier` was modified

* `resourceGroupName()` was added

#### `models.Key` was modified

* `resourceGroupName()` was added

#### `models.WorkloadGroup` was modified

* `resourceGroupName()` was added

#### `models.DatabasePrincipalAssignment` was modified

* `resourceGroupName()` was added
* `aadObjectId()` was added

#### `models.SqlPoolVulnerabilityAssessment` was modified

* `resourceGroupName()` was added

#### `models.BigDataPoolResourceInfo` was modified

* `resourceGroupName()` was added
* `isAutotuneEnabled()` was added

#### `models.BigDataPoolResourceInfo$Definition` was modified

* `withIsAutotuneEnabled(java.lang.Boolean)` was added

#### `models.Workspace` was modified

* `resourceGroupName()` was added

#### `models.SqlPool` was modified

* `resourceGroupName()` was added

#### `models.ExtendedServerBlobAuditingPolicy` was modified

* `resourceGroupName()` was added

#### `models.ManagedIdentitySqlControlSettingsModelPropertiesGrantSqlControlToManagedIdentity` was modified

* `withDesiredState(models.DesiredState)` was added

#### `models.ClusterPrincipalAssignment` was modified

* `resourceGroupName()` was added
* `aadObjectId()` was added

#### `models.PrivateLinkHub` was modified

* `resourceGroupName()` was added

#### `models.SparkConfigurationResource` was modified

* `configMergeRule()` was added

#### `models.ServerVulnerabilityAssessment` was modified

* `resourceGroupName()` was added

#### `models.ServerSecurityAlertPolicy` was modified

* `resourceGroupName()` was added

#### `models.PrivateLinkResources` was modified

* `value()` was added
* `validate()` was added
* `withValue(java.util.List)` was added

#### `SynapseManager` was modified

* `kustoPoolPrivateLinkResourcesOperations()` was added
* `gets()` was added
* `privateLinkResourcesOperations()` was added

#### `models.SqlPoolVulnerabilityAssessmentRuleBaseline` was modified

* `resourceGroupName()` was added

#### `models.IntegrationRuntimeResource` was modified

* `resourceGroupName()` was added

#### `models.SqlPoolSecurityAlertPolicy` was modified

* `resourceGroupName()` was added

#### `models.SensitivityLabel` was modified

* `resourceGroupName()` was added

#### `models.DataMaskingPolicy` was modified

* `resourceGroupName()` was added

#### `models.KustoPool` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.6 (2022-04-25)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-composite-v2. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.WorkspaceManagedSqlServerDedicatedSqlMinimalTlsSettings` was added

* `models.DedicatedSQLminimalTlsSettingsListResult` was added

* `models.DedicatedSqlMinimalTlsSettingsName` was added

* `models.DedicatedSQLminimalTlsSettings` was added

#### `SynapseManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `SynapseManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `workspaceManagedSqlServerDedicatedSqlMinimalTlsSettings()` was added

## 1.0.0-beta.5 (2022-01-24)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-composite-v2. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SqlPoolPatchInfo` was modified

* `withStatus(java.lang.String)` was removed
* `withCreationDate(java.time.OffsetDateTime)` was removed

#### `models.SqlPool$Definition` was modified

* `withStatus(java.lang.String)` was removed
* `withCreationDate(java.time.OffsetDateTime)` was removed

#### `models.BigDataPoolResourceInfo$Definition` was modified

* `withCreationDate(java.time.OffsetDateTime)` was removed

#### `models.SqlPool$Update` was modified

* `withStatus(java.lang.String)` was removed
* `withCreationDate(java.time.OffsetDateTime)` was removed
* `withSourceDatabaseDeletionDate(java.time.OffsetDateTime)` was removed

### Features Added

* `models.PrincipalsModificationKind` was added

* `models.ReadOnlyFollowingDatabase` was added

#### `models.ServerBlobAuditingPolicy$Update` was modified

* `withIsDevopsAuditEnabled(java.lang.Boolean)` was added

#### `models.ServerBlobAuditingPolicy` was modified

* `isDevopsAuditEnabled()` was added

#### `models.SelfHostedIntegrationRuntimeStatus` was modified

* `serviceRegion()` was added
* `withServiceRegion(java.lang.String)` was added
* `newerVersions()` was added
* `withNewerVersions(java.util.List)` was added

#### `models.ManagedIntegrationRuntime` was modified

* `withTypeManagedVirtualNetworkType(java.lang.String)` was added
* `typeManagedVirtualNetworkType()` was added
* `withId(java.lang.String)` was added
* `withReferenceName(java.lang.String)` was added
* `referenceName()` was added
* `id()` was added

#### `models.ExtendedServerBlobAuditingPolicy$Update` was modified

* `withIsDevopsAuditEnabled(java.lang.Boolean)` was added

#### `models.Workspace` was modified

* `trustedServiceBypassEnabled()` was added

#### `models.ExtendedServerBlobAuditingPolicy` was modified

* `isDevopsAuditEnabled()` was added

#### `models.ServerBlobAuditingPolicy$Definition` was modified

* `withIsDevopsAuditEnabled(java.lang.Boolean)` was added

#### `models.DynamicExecutorAllocation` was modified

* `withMaxExecutors(java.lang.Integer)` was added
* `maxExecutors()` was added
* `withMinExecutors(java.lang.Integer)` was added
* `minExecutors()` was added

#### `models.Workspace$Definition` was modified

* `withTrustedServiceBypassEnabled(java.lang.Boolean)` was added

#### `models.ExtendedServerBlobAuditingPolicy$Definition` was modified

* `withIsDevopsAuditEnabled(java.lang.Boolean)` was added

## 1.0.0-beta.4 (2021-11-10)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-composite-v2. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.KustoPoolsOperations` was removed

#### `models.SqlPoolPatchInfo` was modified

* `withCreateMode(java.lang.String)` was removed
* `java.lang.String createMode()` -> `models.CreateMode createMode()`

#### `models.SqlPool$Definition` was modified

* `withCreateMode(java.lang.String)` was removed

#### `models.SqlPool` was modified

* `java.lang.String createMode()` -> `models.CreateMode createMode()`

#### `models.SqlPool$Update` was modified

* `withCreateMode(java.lang.String)` was removed

#### `SynapseManager` was modified

* `kustoPoolsOperations()` was removed

### Features Added

* `models.CreateMode` was added

#### `models.SqlPoolPatchInfo` was modified

* `withCreateMode(models.CreateMode)` was added

#### `models.KustoPools` was modified

* `removeLanguageExtensions(java.lang.String,java.lang.String,java.lang.String,models.LanguageExtensionsList)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `detachFollowerDatabases(java.lang.String,java.lang.String,java.lang.String,fluent.models.FollowerDatabaseDefinitionInner)` was added
* `detachFollowerDatabases(java.lang.String,java.lang.String,java.lang.String,fluent.models.FollowerDatabaseDefinitionInner,com.azure.core.util.Context)` was added
* `listByWorkspaceWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String)` was added
* `listFollowerDatabases(java.lang.String,java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String)` was added
* `checkNameAvailability(java.lang.String,models.KustoPoolCheckNameRequest)` was added
* `listLanguageExtensions(java.lang.String,java.lang.String,java.lang.String)` was added
* `listSkusByResource(java.lang.String,java.lang.String,java.lang.String)` was added
* `addLanguageExtensions(java.lang.String,java.lang.String,java.lang.String,models.LanguageExtensionsList,com.azure.core.util.Context)` was added
* `checkNameAvailabilityWithResponse(java.lang.String,models.KustoPoolCheckNameRequest,com.azure.core.util.Context)` was added
* `listLanguageExtensions(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listFollowerDatabases(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByWorkspace(java.lang.String,java.lang.String)` was added
* `start(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `addLanguageExtensions(java.lang.String,java.lang.String,java.lang.String,models.LanguageExtensionsList)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `removeLanguageExtensions(java.lang.String,java.lang.String,java.lang.String,models.LanguageExtensionsList,com.azure.core.util.Context)` was added
* `stop(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listSkusByResource(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SqlPool$Definition` was modified

* `withCreateMode(models.CreateMode)` was added

#### `models.EventHubDataConnection` was modified

* `withManagedIdentityResourceId(java.lang.String)` was added
* `managedIdentityResourceId()` was added

#### `models.SqlPool$Update` was modified

* `withCreateMode(models.CreateMode)` was added

## 1.0.0-beta.3 (2021-10-09)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-composite-v2. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DefaultPrincipalsModificationKind` was added

* `models.DataConnectionCheckNameRequest` was added

* `models.AttachedDatabaseConfiguration` was added

* `models.KustoPoolDatabasePrincipalAssignments` was added

* `models.ClusterPrincipalAssignmentCheckNameRequest` was added

* `models.KustoPool$UpdateStages` was added

* `models.ResourceProvisioningState` was added

* `models.SkuName` was added

* `models.AzureCapacity` was added

* `models.KustoPool$DefinitionStages` was added

* `models.DatabasePrincipalAssignmentCheckNameRequest` was added

* `models.DataConnectionValidationResult` was added

* `models.KustoPool$Update` was added

* `models.AzureScaleType` was added

* `models.DatabaseStatistics` was added

* `models.EventHubDataFormat` was added

* `models.KustoPoolPrincipalAssignments` was added

* `models.KustoPoolsOperations` was added

* `models.DataConnection` was added

* `models.Compression` was added

* `models.AttachedDatabaseConfiguration$Update` was added

* `models.SkuDescription` was added

* `models.DatabasePrincipalAssignment$Update` was added

* `models.Database` was added

* `models.LanguageExtension` was added

* `models.ClusterPrincipalAssignment$Update` was added

* `models.PrincipalType` was added

* `models.Kind` was added

* `models.KustoPoolListResult` was added

* `models.DatabasePrincipalAssignment` was added

* `models.Operation` was added

* `models.SkuSize` was added

* `models.KustoPoolUpdate` was added

* `models.IotHubDataConnection` was added

* `models.KustoPools` was added

* `models.EventGridDataConnection` was added

* `models.AzureSku` was added

* `models.DatabasePrincipalAssignment$UpdateStages` was added

* `models.ClusterPrincipalAssignment$UpdateStages` was added

* `models.AttachedDatabaseConfigurationListResult` was added

* `models.OperationDisplay` was added

* `models.ClusterPrincipalAssignmentListResult` was added

* `models.DatabaseCheckNameRequest` was added

* `models.AttachedDatabaseConfiguration$DefinitionStages` was added

* `models.KustoPoolCheckNameRequest` was added

* `models.KustoPool$Definition` was added

* `models.LanguageExtensionsList` was added

* `models.DatabasePrincipalAssignment$DefinitionStages` was added

* `models.DataConnectionKind` was added

* `models.DatabasePrincipalAssignment$Definition` was added

* `models.ClusterPrincipalAssignment$DefinitionStages` was added

* `models.AttachedDatabaseConfiguration$UpdateStages` was added

* `models.FollowerDatabaseListResult` was added

* `models.EventHubDataConnection` was added

* `models.SkuLocationInfoItem` was added

* `models.DataConnectionValidationListResult` was added

* `models.OptimizedAutoscale` was added

* `models.KustoPoolChildResources` was added

* `models.FollowerDatabaseDefinition` was added

* `models.Reason` was added

* `models.ClusterPrincipalAssignment$Definition` was added

* `models.State` was added

* `models.DataConnectionListResult` was added

* `models.DataConnectionValidation` was added

* `models.CheckNameResult` was added

* `models.DatabasePrincipalAssignmentListResult` was added

* `models.KustoPoolDataConnections` was added

* `models.OperationListResult` was added

* `models.EventGridDataFormat` was added

* `models.IotHubDataFormat` was added

* `models.AttachedDatabaseConfiguration$Definition` was added

* `models.ClusterPrincipalAssignment` was added

* `models.KustoPoolAttachedDatabaseConfigurations` was added

* `models.SkuDescriptionList` was added

* `models.TableLevelSharingProperties` was added

* `models.LanguageExtensionName` was added

* `models.ClusterPrincipalRole` was added

* `models.BlobStorageEventType` was added

* `models.Type` was added

* `models.KustoPoolDatabases` was added

* `models.DatabaseListResult` was added

* `models.KustoOperations` was added

* `models.ReadWriteDatabase` was added

* `models.AzureResourceSku` was added

* `models.DatabasePrincipalRole` was added

* `models.KustoPool` was added

* `models.ListResourceSkusResult` was added

#### `SynapseManager` was modified

* `kustoPoolDatabases()` was added
* `kustoPoolAttachedDatabaseConfigurations()` was added
* `kustoPools()` was added
* `kustoPoolChildResources()` was added
* `kustoPoolDatabasePrincipalAssignments()` was added
* `kustoPoolDataConnections()` was added
* `kustoPoolsOperations()` was added
* `kustoPoolPrincipalAssignments()` was added
* `kustoOperations()` was added

## 1.0.0-beta.2 (2021-09-13)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-composite-v1. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.QueryExecutionType` was removed

* `models.LibraryInfo` was removed

* `models.QueryStatistic` was removed

* `models.QueryMetric` was removed

* `models.QueryInterval` was removed

* `models.QueryObservedMetricType` was removed

* `models.QueryAggregationFunction` was removed

* `models.IpFirewallRuleProperties` was removed

* `models.TopQueries` was removed

* `models.PrivateEndpointConnectionProperties` was removed

#### `models.PrivateEndpointConnectionForPrivateLinkHub` was modified

* `models.PrivateEndpointConnectionProperties properties()` -> `fluent.models.PrivateEndpointConnectionProperties properties()`

#### `models.WorkloadGroup` was modified

* `java.lang.Double minResourcePercentPerRequest()` -> `double minResourcePercentPerRequest()`
* `java.lang.Integer maxResourcePercent()` -> `int maxResourcePercent()`
* `java.lang.Integer minResourcePercent()` -> `int minResourcePercent()`

#### `models.WorkloadGroup$Definition` was modified

* `withMinResourcePercentPerRequest(java.lang.Double)` was removed
* `withMinResourcePercent(java.lang.Integer)` was removed
* `withMaxResourcePercent(java.lang.Integer)` was removed

#### `models.WorkloadGroup$Update` was modified

* `withMinResourcePercent(java.lang.Integer)` was removed
* `withMinResourcePercentPerRequest(java.lang.Double)` was removed
* `withMaxResourcePercent(java.lang.Integer)` was removed

#### `models.BigDataPoolResourceInfo` was modified

* `models.LibraryRequirements sparkConfigProperties()` -> `models.SparkConfigProperties sparkConfigProperties()`

#### `models.BigDataPoolResourceInfo$Definition` was modified

* `withSparkConfigProperties(models.LibraryRequirements)` was removed

#### `models.PrivateEndpointConnectionForPrivateLinkHubBasicAutoGenerated` was modified

* `models.PrivateEndpointConnectionProperties properties()` -> `fluent.models.PrivateEndpointConnectionProperties properties()`
* `withProperties(models.PrivateEndpointConnectionProperties)` was removed

### Features Added

* `models.UserAssignedManagedIdentity` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesCategoryEndpoint` was added

* `models.AzureADOnlyAuthenticationListResult` was added

* `models.AzureADOnlyAuthentications` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesEndpoint` was added

* `models.ConfigurationType` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesEndpointsResponse` was added

* `models.CspWorkspaceAdminProperties` was added

* `models.SensitivityLabelUpdateProperties` was added

* `models.StateValue` was added

* `models.SparkConfigProperties` was added

* `models.SparkConfigurations` was added

* `models.AzureADOnlyAuthenticationName` was added

* `models.SparkConfigurationsOperations` was added

* `models.AzureADOnlyAuthentication$DefinitionStages` was added

* `models.IntegrationRuntimeOutboundNetworkDependenciesEndpointDetails` was added

* `models.SparkConfigurationResource` was added

* `models.SparkConfigurationListResponse` was added

* `models.AzureADOnlyAuthentication$Definition` was added

* `models.AzureADOnlyAuthentication` was added

* `models.KekIdentityProperties` was added

#### `models.SqlPoolPatchInfo` was modified

* `withSourceDatabaseDeletionDate(java.time.OffsetDateTime)` was added
* `sourceDatabaseDeletionDate()` was added

#### `models.IntegrationRuntimeDataFlowProperties` was modified

* `cleanup()` was added
* `withCleanup(java.lang.Boolean)` was added

#### `SynapseManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.PrivateEndpointConnectionsPrivateLinkHubs` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.SqlPool$Definition` was modified

* `withSourceDatabaseDeletionDate(java.time.OffsetDateTime)` was added

#### `models.DataLakeStorageAccountDetails` was modified

* `withResourceId(java.lang.String)` was added
* `createManagedPrivateEndpoint()` was added
* `resourceId()` was added
* `withCreateManagedPrivateEndpoint(java.lang.Boolean)` was added

#### `models.WorkloadGroup$Definition` was modified

* `withMaxResourcePercent(int)` was added
* `withMinResourcePercentPerRequest(double)` was added
* `withMinResourcePercent(int)` was added

#### `models.WorkloadGroup$Update` was modified

* `withMinResourcePercentPerRequest(double)` was added
* `withMaxResourcePercent(int)` was added
* `withMinResourcePercent(int)` was added

#### `models.BigDataPoolResourceInfo$Definition` was modified

* `withSparkConfigProperties(models.SparkConfigProperties)` was added

#### `models.Workspace` was modified

* `settings()` was added
* `cspWorkspaceAdminProperties()` was added
* `azureADOnlyAuthentication()` was added

#### `models.SqlPool` was modified

* `sourceDatabaseDeletionDate()` was added

#### `models.ManagedIdentity` was modified

* `withUserAssignedIdentities(java.util.Map)` was added
* `userAssignedIdentities()` was added

#### `models.CustomerManagedKeyDetails` was modified

* `withKekIdentity(models.KekIdentityProperties)` was added
* `kekIdentity()` was added

#### `models.SqlPool$Update` was modified

* `withSourceDatabaseDeletionDate(java.time.OffsetDateTime)` was added

#### `models.PrivateEndpointConnectionForPrivateLinkHubBasicAutoGenerated` was modified

* `withProperties(fluent.models.PrivateEndpointConnectionProperties)` was added

#### `models.IntegrationRuntimes` was modified

* `listOutboundNetworkDependenciesEndpoints(java.lang.String,java.lang.String,java.lang.String)` was added
* `listOutboundNetworkDependenciesEndpointsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `SynapseManager` was modified

* `azureADOnlyAuthentications()` was added
* `sparkConfigurationsOperations()` was added
* `sparkConfigurations()` was added

#### `models.Workspace$Definition` was modified

* `withAzureADOnlyAuthentication(java.lang.Boolean)` was added
* `withCspWorkspaceAdminProperties(models.CspWorkspaceAdminProperties)` was added

#### `models.IntegrationRuntimeVNetProperties` was modified

* `withSubnetId(java.lang.String)` was added
* `subnetId()` was added

## 1.0.0-beta.1 (2021-04-07)

- Azure Resource Manager Synapse client library for Java. This package contains Microsoft Azure SDK for Synapse Management SDK. Azure Synapse Analytics Management Client. Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
