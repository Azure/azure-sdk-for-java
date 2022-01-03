# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2021-08-31)

- Azure Resource Manager ServiceFabric client library for Java. This package contains Microsoft Azure SDK for ServiceFabric Management SDK. Service Fabric Management Client. Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ClusterVersionDetails` was removed

* `models.ApplicationResourceProperties` was removed

* `models.ServiceResourceUpdateProperties` was removed

* `models.ApplicationResourceUpdateProperties` was removed

* `models.ServiceResourceProperties` was removed

#### `models.ServiceResourceUpdate` was modified

* `serviceLoadMetrics()` was removed
* `withDefaultMoveCost(models.MoveCost)` was removed
* `withPlacementConstraints(java.lang.String)` was removed
* `correlationScheme()` was removed
* `placementConstraints()` was removed
* `servicePlacementPolicies()` was removed
* `withServiceLoadMetrics(java.util.List)` was removed
* `withCorrelationScheme(java.util.List)` was removed
* `defaultMoveCost()` was removed
* `withServicePlacementPolicies(java.util.List)` was removed

#### `models.ApplicationTypeResource$Update` was modified

* `withTags(java.util.Map)` was removed

#### `models.ApplicationResource$Update` was modified

* `withTags(java.util.Map)` was removed

#### `models.ServiceResource$Update` was modified

* `withDefaultMoveCost(models.MoveCost)` was removed
* `withCorrelationScheme(java.util.List)` was removed
* `withPlacementConstraints(java.lang.String)` was removed
* `withServicePlacementPolicies(java.util.List)` was removed
* `withServiceLoadMetrics(java.util.List)` was removed
* `withTags(java.util.Map)` was removed

#### `models.ApplicationTypeVersionResource$Update` was modified

* `withTags(java.util.Map)` was removed

### Features Added

* `models.ManagedIdentity` was added

* `models.UpgradableVersionsDescription` was added

* `models.VmssZonalUpgradeMode` was added

* `models.UpgradableVersionPathResult` was added

* `models.NotificationChannel` was added

* `models.NotificationTarget` was added

* `models.ManagedIdentityType` was added

* `models.NotificationCategory` was added

* `models.UserAssignedIdentity` was added

* `models.SfZonalUpgradeMode` was added

* `models.ApplicationUserAssignedIdentity` was added

* `models.Notification` was added

* `models.ApplicationTypeVersionsCleanupPolicy` was added

* `models.ClusterUpgradeCadence` was added

* `models.NotificationLevel` was added

* `models.RollingUpgradeMode` was added

#### `models.Cluster$Definition` was modified

* `withUpgradePauseStartTimestampUtc(java.time.OffsetDateTime)` was added
* `withUpgradePauseEndTimestampUtc(java.time.OffsetDateTime)` was added
* `withNotifications(java.util.List)` was added
* `withApplicationTypeVersionsCleanupPolicy(models.ApplicationTypeVersionsCleanupPolicy)` was added
* `withUpgradeWave(models.ClusterUpgradeCadence)` was added
* `withVmssZonalUpgradeMode(models.VmssZonalUpgradeMode)` was added
* `withWaveUpgradePaused(java.lang.Boolean)` was added
* `withInfrastructureServiceManager(java.lang.Boolean)` was added
* `withSfZonalUpgradeMode(models.SfZonalUpgradeMode)` was added

#### `models.StatefulServiceProperties` was modified

* `withServiceDnsName(java.lang.String)` was added
* `withServiceDnsName(java.lang.String)` was added

#### `models.StatelessServiceProperties` was modified

* `instanceCloseDelayDuration()` was added
* `withInstanceCloseDelayDuration(java.lang.String)` was added
* `withServiceDnsName(java.lang.String)` was added
* `withServiceDnsName(java.lang.String)` was added

#### `models.ApplicationResourceUpdate` was modified

* `managedIdentities()` was added
* `withManagedIdentities(java.util.List)` was added
* `systemData()` was added

#### `models.ApplicationResource$Definition` was modified

* `withManagedIdentities(java.util.List)` was added
* `withIdentity(models.ManagedIdentity)` was added

#### `models.ApplicationTypeVersionResource` was modified

* `systemData()` was added

#### `models.Cluster$Update` was modified

* `withSfZonalUpgradeMode(models.SfZonalUpgradeMode)` was added
* `withApplicationTypeVersionsCleanupPolicy(models.ApplicationTypeVersionsCleanupPolicy)` was added
* `withVmssZonalUpgradeMode(models.VmssZonalUpgradeMode)` was added
* `withWaveUpgradePaused(java.lang.Boolean)` was added
* `withInfrastructureServiceManager(java.lang.Boolean)` was added
* `withUpgradeWave(models.ClusterUpgradeCadence)` was added
* `withUpgradePauseStartTimestampUtc(java.time.OffsetDateTime)` was added
* `withUpgradePauseEndTimestampUtc(java.time.OffsetDateTime)` was added
* `withNotifications(java.util.List)` was added

#### `models.ServiceResource$Definition` was modified

* `withServiceDnsName(java.lang.String)` was added

#### `models.ClusterUpdateParameters` was modified

* `withUpgradePauseStartTimestampUtc(java.time.OffsetDateTime)` was added
* `upgradeWave()` was added
* `withVmssZonalUpgradeMode(models.VmssZonalUpgradeMode)` was added
* `withWaveUpgradePaused(java.lang.Boolean)` was added
* `upgradePauseEndTimestampUtc()` was added
* `withNotifications(java.util.List)` was added
* `withUpgradeWave(models.ClusterUpgradeCadence)` was added
* `upgradePauseStartTimestampUtc()` was added
* `notifications()` was added
* `vmssZonalUpgradeMode()` was added
* `applicationTypeVersionsCleanupPolicy()` was added
* `sfZonalUpgradeMode()` was added
* `withInfrastructureServiceManager(java.lang.Boolean)` was added
* `withSfZonalUpgradeMode(models.SfZonalUpgradeMode)` was added
* `infrastructureServiceManager()` was added
* `waveUpgradePaused()` was added
* `withApplicationTypeVersionsCleanupPolicy(models.ApplicationTypeVersionsCleanupPolicy)` was added
* `withUpgradePauseEndTimestampUtc(java.time.OffsetDateTime)` was added

#### `models.ServiceResourceUpdate` was modified

* `systemData()` was added

#### `models.NodeTypeDescription` was modified

* `multipleAvailabilityZones()` was added
* `isStateless()` was added
* `withMultipleAvailabilityZones(java.lang.Boolean)` was added
* `withIsStateless(java.lang.Boolean)` was added

#### `models.ApplicationUpgradePolicy` was modified

* `recreateApplication()` was added
* `upgradeMode()` was added
* `withRecreateApplication(java.lang.Boolean)` was added
* `withUpgradeMode(models.RollingUpgradeMode)` was added

#### `models.ServiceResource` was modified

* `systemData()` was added
* `serviceDnsName()` was added

#### `models.DiagnosticsStorageAccountConfig` was modified

* `withProtectedAccountKeyName2(java.lang.String)` was added
* `protectedAccountKeyName2()` was added

#### `models.Cluster` was modified

* `vmssZonalUpgradeMode()` was added
* `waveUpgradePaused()` was added
* `notifications()` was added
* `sfZonalUpgradeMode()` was added
* `upgradeWave()` was added
* `upgradePauseEndTimestampUtc()` was added
* `listUpgradableVersions()` was added
* `systemData()` was added
* `applicationTypeVersionsCleanupPolicy()` was added
* `upgradePauseStartTimestampUtc()` was added
* `listUpgradableVersionsWithResponse(models.UpgradableVersionsDescription,com.azure.core.util.Context)` was added
* `infrastructureServiceManager()` was added

#### `models.ApplicationResource$Update` was modified

* `withManagedIdentities(java.util.List)` was added

#### `models.ApplicationTypeResource` was modified

* `systemData()` was added

#### `models.OperationResult` was modified

* `isDataAction()` was added

#### `models.Clusters` was modified

* `listUpgradableVersionsWithResponse(java.lang.String,java.lang.String,models.UpgradableVersionsDescription,com.azure.core.util.Context)` was added
* `listUpgradableVersions(java.lang.String,java.lang.String)` was added

#### `models.ApplicationResource` was modified

* `systemData()` was added
* `managedIdentities()` was added
* `identity()` was added

#### `models.StatelessServiceUpdateProperties` was modified

* `withInstanceCloseDelayDuration(java.lang.String)` was added
* `instanceCloseDelayDuration()` was added

#### `ServiceFabricManager$Configurable` was modified

* `withScope(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-16)

- Azure Resource Manager ServiceFabric client library for Java. This package contains Microsoft Azure SDK for ServiceFabric Management SDK. Service Fabric Management Client. Package tag package-2019-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
