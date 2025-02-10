# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2024-08-22)

- Azure Resource Manager HDInsightContainers client library for Java. This package contains Microsoft Azure SDK for HDInsightContainers Management SDK. HDInsight Containers Management Client. Package tag package-preview-2024-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ClusterHotfixUpgradeProperties` was modified

* `targetBuildNumber()` was removed
* `targetClusterVersion()` was removed
* `componentName()` was removed
* `targetOssVersion()` was removed

#### `models.ClusterAvailableUpgradeHotfixUpgradeProperties` was modified

* `componentName()` was removed
* `description()` was removed
* `sourceOssVersion()` was removed
* `extendedProperties()` was removed
* `targetOssVersion()` was removed
* `sourceClusterVersion()` was removed
* `targetBuildNumber()` was removed
* `createdTime()` was removed
* `sourceBuildNumber()` was removed
* `severity()` was removed
* `targetClusterVersion()` was removed

#### `models.KafkaProfile` was modified

* `clusterIdentity()` was removed

### Features Added

* `models.ClusterPatchVersionUpgradeRollbackHistoryProperties` was added

* `models.PyPiLibraryProperties` was added

* `models.ClusterPoolUpgradeHistoryListResult` was added

* `models.ClusterAvailableUpgradePatchVersionUpgradeProperties` was added

* `models.ClusterLibraryManagementOperation` was added

* `models.ClusterPoolUpgradeHistoryType` was added

* `models.ClusterAvailableInPlaceUpgradeProperties` was added

* `models.ManagedIdentityType` was added

* `models.ClusterPoolUpgradeHistories` was added

* `models.ClusterPoolNodeOsUpgradeHistoryProperties` was added

* `models.ClusterUpgradeHistoryType` was added

* `models.ClusterUpgradeHistory` was added

* `models.Category` was added

* `models.Status` was added

* `models.ClusterLibraries` was added

* `models.ClusterUpgradeHistorySeverityType` was added

* `models.ClusterAksPatchUpgradeHistoryProperties` was added

* `models.ClusterLibraryManagementOperationProperties` was added

* `models.MavenLibraryProperties` was added

* `models.ClusterUpgradeHistoryListResult` was added

* `models.ClusterUpgradeRollback` was added

* `models.ClusterHotfixUpgradeHistoryProperties` was added

* `models.ClusterInPlaceUpgradeHistoryProperties` was added

* `models.ClusterLibraryList` was added

* `models.ClusterPatchVersionUpgradeProperties` was added

* `models.Type` was added

* `models.ClusterPoolUpgradeHistoryUpgradeResultType` was added

* `models.ClusterUpgradeHistories` was added

* `models.ClusterUpgradeRollbackProperties` was added

* `models.ClusterPoolUpgradeHistory` was added

* `models.ManagedIdentityProfile` was added

* `models.ClusterInPlaceUpgradeProperties` was added

* `models.IpTag` was added

* `models.ClusterLibrary` was added

* `models.ClusterPatchVersionUpgradeHistoryProperties` was added

* `models.ClusterPoolUpgradeHistoryProperties` was added

* `models.ClusterLibraryProperties` was added

* `models.ClusterPoolAksPatchUpgradeHistoryProperties` was added

* `models.LibraryManagementAction` was added

* `models.ClusterUpgradeHistoryUpgradeResultType` was added

* `models.ClusterUpgradeHistoryProperties` was added

* `models.ManagedIdentitySpec` was added

* `models.ClusterHotfixUpgradeRollbackHistoryProperties` was added

#### `models.ClusterServiceConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterUpgradeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `upgradeType()` was added

#### `models.RangerAdminSpecDatabase` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterInstanceViewStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScriptActionProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceConfigResultProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RangerUsersyncSpec` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPatchProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolResourcePropertiesAksClusterProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `aksVersion()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolNodeOsImageUpdateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `upgradeType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrinoCoordinator` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolResourcePropertiesComputeProfile` was modified

* `count()` was added
* `withAvailabilityZones(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterAccessProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecretReference` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster` was modified

* `upgradeManualRollback(models.ClusterUpgradeRollback)` was added
* `upgradeManualRollback(models.ClusterUpgradeRollback,com.azure.core.util.Context)` was added

#### `models.ServiceConfigListResultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FlinkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrinoUserPlugin` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FlinkCatalogOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterProfile` was modified

* `managedIdentityProfile()` was added
* `withManagedIdentityProfile(models.ManagedIdentityProfile)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterAvailableUpgradeAksPatchUpgradeProperties` was modified

* `upgradeType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SparkUserPlugins` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CatalogOptions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SparkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterResizeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterRangerPluginProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterInstanceViewsResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceConfigListResultValueEntity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterHotfixUpgradeProperties` was modified

* `withTargetOssVersion(java.lang.String)` was added
* `upgradeType()` was added
* `withTargetBuildNumber(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withComponentName(java.lang.String)` was added
* `withTargetClusterVersion(java.lang.String)` was added

#### `models.ComputeProfile` was modified

* `availabilityZones()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withAvailabilityZones(java.util.List)` was added

#### `models.ClusterLogAnalyticsApplicationLogs` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectivityProfileWeb` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterAvailableUpgradeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `upgradeType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolVersionsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrinoUserPlugins` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `HDInsightContainersManager` was modified

* `clusterLibraries()` was added
* `clusterPoolUpgradeHistories()` was added
* `clusterUpgradeHistories()` was added

#### `models.ClusterPoolResourcePropertiesNetworkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LoadBasedConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterAvailableUpgradeHotfixUpgradeProperties` was modified

* `withTargetBuildNumber(java.lang.String)` was added
* `withSourceClusterVersion(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withCreatedTime(java.time.OffsetDateTime)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withComponentName(java.lang.String)` was added
* `upgradeType()` was added
* `withExtendedProperties(java.lang.String)` was added
* `withSourceOssVersion(java.lang.String)` was added
* `withTargetClusterVersion(java.lang.String)` was added
* `withDescription(java.lang.String)` was added
* `withSourceBuildNumber(java.lang.String)` was added
* `withSeverity(models.Severity)` was added
* `withTargetOssVersion(java.lang.String)` was added

#### `models.ClusterServiceConfigsProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecretsProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SparkUserPlugin` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebConnectivityEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrinoDebugConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterVersionsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterAksPatchVersionUpgradeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `upgradeType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleBasedConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrinoWorker` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolAvailableUpgradeList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolVersionProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshConnectivityEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TagsObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TrinoTelemetryConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KafkaProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolComputeProfile` was modified

* `withAvailabilityZones(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `availabilityZones()` was added

#### `models.ClusterResizeData` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.DiskStorageProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AksClusterProfileAksClusterAgentPoolIdentityProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutoscaleProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolResourcePropertiesLogAnalyticsProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KafkaConnectivityEndpoints` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RangerAuditSpec` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshProfile` was modified

* `withVmSize(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `vmSize()` was added

#### `models.ClusterVersionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AksClusterProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NameAvailabilityParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrinoUserTelemetry` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolResourcePropertiesClusterPoolProfile` was modified

* `withPublicIpTag(models.IpTag)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolUpgrade` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterJobList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolAvailableUpgradeNodeOsUpgradeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `upgradeType()` was added

#### `models.ClusterPoolAksPatchVersionUpgradeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `upgradeType()` was added

#### `models.ClusterComponentsItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FlinkJobProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectivityProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withPublicIpTag(models.IpTag)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `publicIpTag()` was added

#### `models.FlinkJobProperties` was modified

* `jobType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComparisonRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TrinoProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RangerProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterAvailableUpgradeList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScalingRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RangerAdminSpec` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceConfigListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterLogAnalyticsProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPoolNetworkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterInstanceViewResultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterUpgrade` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthorizationProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FlinkStorageProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Schedule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NodeProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterInstanceViewPropertiesStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SparkMetastoreSpec` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterInstanceViewProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterJobProperties` was modified

* `jobType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolLogAnalyticsProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Clusters` was modified

* `upgradeManualRollback(java.lang.String,java.lang.String,java.lang.String,models.ClusterUpgradeRollback,com.azure.core.util.Context)` was added
* `upgradeManualRollback(java.lang.String,java.lang.String,java.lang.String,models.ClusterUpgradeRollback)` was added

#### `models.UpdatableClusterProfile` was modified

* `secretsProfile()` was added
* `withTrinoProfile(models.TrinoProfile)` was added
* `withSecretsProfile(models.SecretsProfile)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `trinoProfile()` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolAvailableUpgradeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `upgradeType()` was added

#### `models.ClusterPrometheusProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FlinkHiveCatalogOption` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HiveCatalogOption` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterConfigFile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ComputeResourceDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPoolAvailableUpgradeAksPatchUpgradeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `upgradeType()` was added

#### `models.ClusterPoolUpgradeProperties` was modified

* `upgradeType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2024-04-03)

- Azure Resource Manager HDInsightContainers client library for Java. This package contains Microsoft Azure SDK for HDInsightContainers Management SDK. HDInsight Containers Management Client. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.TrinoWorker` was modified

* `withEnable(java.lang.Boolean)` was removed
* `withPort(java.lang.Integer)` was removed
* `port()` was removed
* `suspend()` was removed
* `withSuspend(java.lang.Boolean)` was removed
* `enable()` was removed

#### `models.ClusterResizeData` was modified

* `withTargetWorkerNodeCount(java.lang.Integer)` was removed
* `targetWorkerNodeCount()` was removed

#### `models.ClusterPool` was modified

* `logAnalyticsProfile()` was removed
* `computeProfile()` was removed
* `aksManagedResourceGroupName()` was removed
* `deploymentId()` was removed
* `networkProfile()` was removed
* `aksClusterProfile()` was removed
* `managedResourceGroupName()` was removed
* `status()` was removed
* `provisioningState()` was removed
* `clusterPoolProfile()` was removed

#### `models.TrinoCoordinator` was modified

* `suspend()` was removed
* `withPort(java.lang.Integer)` was removed
* `port()` was removed
* `enable()` was removed
* `withEnable(java.lang.Boolean)` was removed
* `withSuspend(java.lang.Boolean)` was removed

#### `models.ClusterPool$Definition` was modified

* `withComputeProfile(models.ClusterPoolResourcePropertiesComputeProfile)` was removed
* `withLogAnalyticsProfile(models.ClusterPoolResourcePropertiesLogAnalyticsProfile)` was removed
* `withClusterPoolProfile(models.ClusterPoolResourcePropertiesClusterPoolProfile)` was removed
* `withManagedResourceGroupName(java.lang.String)` was removed
* `withNetworkProfile(models.ClusterPoolResourcePropertiesNetworkProfile)` was removed

#### `models.ServiceConfigResult` was modified

* `path()` was removed
* `componentName()` was removed
* `serviceName()` was removed
* `type()` was removed
* `content()` was removed
* `customKeys()` was removed
* `defaultKeys()` was removed
* `fileName()` was removed

#### `models.Cluster` was modified

* `clusterProfile()` was removed
* `deploymentId()` was removed
* `status()` was removed
* `clusterType()` was removed
* `provisioningState()` was removed
* `computeProfile()` was removed

#### `models.ClusterJobs` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ClusterProfile` was modified

* `withKafkaProfile(java.util.Map)` was removed
* `java.util.Map kafkaProfile()` -> `models.KafkaProfile kafkaProfile()`

#### `models.ClusterInstanceViewResult` was modified

* `status()` was removed
* `serviceStatuses()` was removed

#### `models.ClusterPatch` was modified

* `clusterProfile()` was removed
* `withTags(java.util.Map)` was removed
* `withLocation(java.lang.String)` was removed
* `withClusterProfile(models.UpdatableClusterProfile)` was removed
* `systemData()` was removed

#### `models.Cluster$Definition` was modified

* `withClusterType(java.lang.String)` was removed
* `withComputeProfile(models.ComputeProfile)` was removed
* `withClusterProfile(models.ClusterProfile)` was removed

#### `models.ClusterPoolVersion` was modified

* `aksVersion()` was removed
* `isPreview()` was removed
* `clusterPoolVersion()` was removed

#### `models.ClusterVersion` was modified

* `components()` was removed
* `clusterVersion()` was removed
* `clusterType()` was removed
* `isPreview()` was removed
* `ossVersion()` was removed
* `clusterPoolVersion()` was removed

#### `models.Cluster$Update` was modified

* `withClusterProfile(models.UpdatableClusterProfile)` was removed

### Features Added

* `models.ClusterPoolAvailableUpgrades` was added

* `models.ClusterAvailableUpgradeType` was added

* `models.ClusterAvailableUpgrade` was added

* `models.ClusterPoolAvailableUpgradeList` was added

* `models.ClusterUpgradeProperties` was added

* `models.RangerAdminSpecDatabase` was added

* `models.ClusterAvailableUpgrades` was added

* `models.DataDiskType` was added

* `models.DbConnectionAuthenticationMode` was added

* `models.ClusterPoolVersionProperties` was added

* `models.ClusterPoolUpgradeType` was added

* `models.KafkaProfile` was added

* `models.ServiceConfigResultProperties` was added

* `models.ClusterUpgradeType` was added

* `models.DiskStorageProfile` was added

* `models.MetastoreDbConnectionAuthenticationMode` was added

* `models.RangerUsersyncSpec` was added

* `models.KafkaConnectivityEndpoints` was added

* `models.ClusterPatchProperties` was added

* `models.RangerAuditSpec` was added

* `models.ClusterPoolNodeOsImageUpdateProperties` was added

* `models.ClusterVersionProperties` was added

* `models.CurrentClusterAksVersionStatus` was added

* `models.ClusterAccessProfile` was added

* `models.ClusterPoolUpgrade` was added

* `models.ClusterPoolAvailableUpgradeNodeOsUpgradeProperties` was added

* `models.ClusterPoolAvailableUpgradeType` was added

* `models.ClusterPoolAksPatchVersionUpgradeProperties` was added

* `models.DeploymentMode` was added

* `models.FlinkJobProfile` was added

* `models.ClusterAvailableUpgradeAksPatchUpgradeProperties` was added

* `models.CurrentClusterPoolAksVersionStatus` was added

* `models.RangerProfile` was added

* `models.ClusterAvailableUpgradeList` was added

* `models.RangerAdminSpec` was added

* `models.ClusterInstanceViewResultProperties` was added

* `models.ClusterUpgrade` was added

* `models.ClusterResizeProperties` was added

* `models.ClusterRangerPluginProfile` was added

* `models.RangerUsersyncMode` was added

* `models.OutboundType` was added

* `models.ClusterHotfixUpgradeProperties` was added

* `models.ClusterPoolAvailableUpgrade` was added

* `models.ClusterResourceProperties` was added

* `models.ClusterAvailableUpgradeProperties` was added

* `models.UpgradeMode` was added

* `models.Severity` was added

* `models.ClusterPoolAvailableUpgradeProperties` was added

* `models.ClusterPoolResourceProperties` was added

* `models.ClusterAvailableUpgradeHotfixUpgradeProperties` was added

* `models.TrinoDebugConfig` was added

* `models.ClusterPoolAvailableUpgradeAksPatchUpgradeProperties` was added

* `models.ClusterPoolUpgradeProperties` was added

* `models.ClusterAksPatchVersionUpgradeProperties` was added

#### `models.TrinoWorker` was modified

* `withDebug(models.TrinoDebugConfig)` was added
* `debug()` was added

#### `models.SshConnectivityEndpoint` was modified

* `withPrivateSshEndpoint(java.lang.String)` was added
* `privateSshEndpoint()` was added

#### `models.ClusterResizeData` was modified

* `withProperties(models.ClusterResizeProperties)` was added
* `properties()` was added

#### `models.ClusterPools` was modified

* `upgrade(java.lang.String,java.lang.String,models.ClusterPoolUpgrade)` was added
* `upgrade(java.lang.String,java.lang.String,models.ClusterPoolUpgrade,com.azure.core.util.Context)` was added

#### `models.ClusterPool` was modified

* `upgrade(models.ClusterPoolUpgrade)` was added
* `upgrade(models.ClusterPoolUpgrade,com.azure.core.util.Context)` was added
* `properties()` was added

#### `models.TrinoCoordinator` was modified

* `debug()` was added
* `withDebug(models.TrinoDebugConfig)` was added

#### `models.ClusterPool$Definition` was modified

* `withProperties(models.ClusterPoolResourceProperties)` was added

#### `models.ServiceConfigResult` was modified

* `properties()` was added

#### `models.Cluster` was modified

* `upgrade(models.ClusterUpgrade,com.azure.core.util.Context)` was added
* `properties()` was added
* `upgrade(models.ClusterUpgrade)` was added

#### `models.FlinkProfile` was modified

* `deploymentMode()` was added
* `withDeploymentMode(models.DeploymentMode)` was added
* `withJobSpec(models.FlinkJobProfile)` was added
* `jobSpec()` was added

#### `models.ClusterJobs` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.FlinkJobProperties` was modified

* `runId()` was added
* `withRunId(java.lang.String)` was added

#### `models.ClusterProfile` was modified

* `withClusterAccessProfile(models.ClusterAccessProfile)` was added
* `withRangerPluginProfile(models.ClusterRangerPluginProfile)` was added
* `withRangerProfile(models.RangerProfile)` was added
* `withKafkaProfile(models.KafkaProfile)` was added
* `rangerProfile()` was added
* `clusterAccessProfile()` was added
* `rangerPluginProfile()` was added

#### `models.ClusterInstanceViewResult` was modified

* `properties()` was added

#### `models.ClusterPoolNetworkProfile` was modified

* `apiServerAuthorizedIpRanges()` was added
* `enablePrivateApiServer()` was added
* `withApiServerAuthorizedIpRanges(java.util.List)` was added
* `withEnablePrivateApiServer(java.lang.Boolean)` was added
* `withOutboundType(models.OutboundType)` was added
* `outboundType()` was added

#### `models.ClusterPatch` was modified

* `withProperties(models.ClusterPatchProperties)` was added
* `properties()` was added
* `tags()` was added

#### `models.ConnectivityProfileWeb` was modified

* `withPrivateFqdn(java.lang.String)` was added

#### `models.SparkMetastoreSpec` was modified

* `withDbConnectionAuthenticationMode(models.DbConnectionAuthenticationMode)` was added
* `dbConnectionAuthenticationMode()` was added

#### `models.Clusters` was modified

* `upgrade(java.lang.String,java.lang.String,java.lang.String,models.ClusterUpgrade,com.azure.core.util.Context)` was added
* `upgrade(java.lang.String,java.lang.String,java.lang.String,models.ClusterUpgrade)` was added

#### `models.UpdatableClusterProfile` was modified

* `withRangerProfile(models.RangerProfile)` was added
* `rangerPluginProfile()` was added
* `withRangerPluginProfile(models.ClusterRangerPluginProfile)` was added
* `rangerProfile()` was added

#### `models.Cluster$Definition` was modified

* `withProperties(models.ClusterResourceProperties)` was added

#### `models.ClusterPoolVersion` was modified

* `properties()` was added
* `systemData()` was added

#### `HDInsightContainersManager` was modified

* `clusterAvailableUpgrades()` was added
* `clusterPoolAvailableUpgrades()` was added

#### `models.ClusterPoolResourcePropertiesNetworkProfile` was modified

* `withOutboundType(models.OutboundType)` was added
* `withApiServerAuthorizedIpRanges(java.util.List)` was added
* `withEnablePrivateApiServer(java.lang.Boolean)` was added

#### `models.FlinkHiveCatalogOption` was modified

* `withMetastoreDbConnectionAuthenticationMode(models.MetastoreDbConnectionAuthenticationMode)` was added
* `metastoreDbConnectionAuthenticationMode()` was added

#### `models.HiveCatalogOption` was modified

* `metastoreDbConnectionAuthenticationMode()` was added
* `withMetastoreDbConnectionAuthenticationMode(models.MetastoreDbConnectionAuthenticationMode)` was added

#### `models.ClusterVersion` was modified

* `systemData()` was added
* `properties()` was added

#### `models.WebConnectivityEndpoint` was modified

* `privateFqdn()` was added
* `withPrivateFqdn(java.lang.String)` was added

#### `models.Cluster$Update` was modified

* `withProperties(models.ClusterPatchProperties)` was added

## 1.0.0-beta.1 (2023-08-24)

- Azure Resource Manager HDInsightContainers client library for Java. This package contains Microsoft Azure SDK for HDInsightContainers Management SDK. HDInsight Containers Management Client. Package tag package-2023-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
