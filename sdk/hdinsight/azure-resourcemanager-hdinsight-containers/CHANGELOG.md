# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
