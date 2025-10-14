# Release History

## 1.4.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.3.0 (2025-04-23)

- Azure Resource Manager SiteRecovery client library for Java. This package contains Microsoft Azure SDK for SiteRecovery Management SDK.  Package tag package-2025-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ClusterRecoveryPointCollection` was added

* `models.DiskState` was added

* `models.A2ASharedDiskReplicationDetails` was added

* `models.ApplyClusterRecoveryPointProviderSpecificInput` was added

* `models.SharedDiskReplicationProviderSpecificSettings` was added

* `models.InMageRcmAddDisksInput` was added

* `models.FailoverDirection` was added

* `models.RegisteredClusterNodes` was added

* `models.ClusterSwitchProtectionJobDetails` was added

* `models.SwitchClusterProtectionProviderSpecificInput` was added

* `models.A2AClusterRecoveryPointDetails` was added

* `models.ClusterRecoveryPoints` was added

* `models.ClusterTestFailoverProviderSpecificInput` was added

* `models.LinuxLicenseType` was added

* `models.ManagedRunCommandScriptInput` was added

* `models.ClusterProviderSpecificRecoveryPointDetails` was added

* `models.ReplicationProtectionCluster$Definition` was added

* `models.SecurityProfileProperties` was added

* `models.ClusterUnplannedFailoverInputProperties` was added

* `models.ClusterTestFailoverInputProperties` was added

* `models.SwitchClusterProtectionInputProperties` was added

* `models.ReplicationProtectionCluster$DefinitionStages` was added

* `models.A2AApplyClusterRecoveryPointInput` was added

* `models.ClusterRecoveryPointOperations` was added

* `models.ApplyClusterRecoveryPointInput` was added

* `models.ClusterRecoveryPointProperties` was added

* `models.A2AClusterUnplannedFailoverInput` was added

* `models.SwitchClusterProtectionInput` was added

* `models.ClusterTestFailoverCleanupInput` was added

* `models.A2AClusterTestFailoverInput` was added

* `models.ClusterRecoveryPointType` was added

* `models.SecurityConfiguration` was added

* `models.A2ASwitchClusterProtectionInput` was added

* `models.ClusterUnplannedFailoverProviderSpecificInput` was added

* `models.ReplicationClusterProviderSpecificSettings` was added

* `models.ReplicationProtectionCluster` was added

* `models.ClusterTestFailoverCleanupInputProperties` was added

* `models.A2ASharedDiskIRErrorDetails` was added

* `models.SharedDiskReplicationItemProperties` was added

* `models.ApplyClusterRecoveryPointInputProperties` was added

* `models.ClusterRecoveryPoint` was added

* `models.ReplicationProtectionClusterProperties` was added

* `models.ReplicationProtectionClusters` was added

* `models.A2AProtectedItemDetail` was added

* `models.UserCreatedResourceTag` was added

* `models.InMageRcmUnProtectedDiskDetails` was added

* `models.ReplicationProtectionClusterCollection` was added

* `models.ClusterTestFailoverInput` was added

* `models.ClusterUnplannedFailoverInput` was added

* `models.ClusterTestFailoverJobDetails` was added

* `models.A2AReplicationProtectionClusterDetails` was added

* `models.ClusterFailoverJobDetails` was added

#### `models.OSDetails` was modified

* `userSelectedOSName()` was added
* `withUserSelectedOSName(java.lang.String)` was added

#### `models.InMageRcmDiskInput` was modified

* `withSectorSizeInBytes(java.lang.Integer)` was added
* `sectorSizeInBytes()` was added

#### `SiteRecoveryManager` was modified

* `replicationProtectionClusters()` was added
* `clusterRecoveryPointOperations()` was added
* `clusterRecoveryPoints()` was added

#### `models.VMwareCbtUpdateMigrationItemInput` was modified

* `userSelectedOSName()` was added
* `linuxLicenseType()` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `withUserSelectedOSName(java.lang.String)` was added

#### `models.HyperVReplicaAzureReplicationDetails` was modified

* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `linuxLicenseType()` was added
* `withTargetVmSecurityProfile(models.SecurityProfileProperties)` was added
* `targetVmSecurityProfile()` was added

#### `models.InMageRcmProtectedDiskDetails` was modified

* `customTargetDiskName()` was added
* `withSectorSizeInBytes(java.lang.Integer)` was added
* `diskState()` was added
* `withCustomTargetDiskName(java.lang.String)` was added
* `sectorSizeInBytes()` was added

#### `models.InMageRcmNicDetails` was modified

* `targetNicName()` was added
* `withTargetNicName(java.lang.String)` was added

#### `models.InMageRcmUpdateReplicationProtectedItemInput` was modified

* `targetManagedDiskTags()` was added
* `withTargetManagedDiskTags(java.util.List)` was added
* `sqlServerLicenseType()` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `withSqlServerLicenseType(models.SqlServerLicenseType)` was added
* `targetVmTags()` was added
* `withTargetNicTags(java.util.List)` was added
* `linuxLicenseType()` was added
* `withTargetVmTags(java.util.List)` was added
* `targetNicTags()` was added
* `userSelectedOSName()` was added
* `withUserSelectedOSName(java.lang.String)` was added

#### `models.ProtectionContainer` was modified

* `switchClusterProtection(models.SwitchClusterProtectionInput)` was added
* `switchClusterProtection(models.SwitchClusterProtectionInput,com.azure.core.util.Context)` was added

#### `models.A2AReplicationDetails` was modified

* `withProtectionClusterId(java.lang.String)` was added
* `protectionClusterId()` was added
* `isClusterInfraReady()` was added
* `withIsClusterInfraReady(java.lang.Boolean)` was added

#### `models.VMwareCbtDiskInput` was modified

* `sectorSizeInBytes()` was added
* `withSectorSizeInBytes(java.lang.Integer)` was added

#### `models.VMwareCbtProtectedDiskDetails` was modified

* `sectorSizeInBytes()` was added
* `withSectorSizeInBytes(java.lang.Integer)` was added

#### `models.VMwareCbtTestMigrateInput` was modified

* `withPostMigrationSteps(java.util.List)` was added
* `postMigrationSteps()` was added

#### `models.VMwareCbtEnableMigrationInput` was modified

* `withUserSelectedOSName(java.lang.String)` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `userSelectedOSName()` was added
* `linuxLicenseType()` was added

#### `models.InMageRcmTestFailoverInput` was modified

* `withOsUpgradeVersion(java.lang.String)` was added
* `osUpgradeVersion()` was added

#### `models.InMageRcmReplicationDetails` was modified

* `withTargetVmTags(java.util.List)` was added
* `seedManagedDiskTags()` was added
* `targetVmSecurityProfile()` was added
* `withOsName(java.lang.String)` was added
* `withSupportedOSVersions(java.util.List)` was added
* `osName()` was added
* `targetNicTags()` was added
* `withSeedManagedDiskTags(java.util.List)` was added
* `withTargetVmSecurityProfile(models.SecurityProfileProperties)` was added
* `sqlServerLicenseType()` was added
* `withTargetManagedDiskTags(java.util.List)` was added
* `unprotectedDisks()` was added
* `linuxLicenseType()` was added
* `supportedOSVersions()` was added
* `withUnprotectedDisks(java.util.List)` was added
* `withSqlServerLicenseType(java.lang.String)` was added
* `withTargetNicTags(java.util.List)` was added
* `targetVmTags()` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `targetManagedDiskTags()` was added

#### `models.InMageRcmDisksDefaultInput` was modified

* `sectorSizeInBytes()` was added
* `withSectorSizeInBytes(java.lang.Integer)` was added

#### `models.VMwareCbtMigrateInput` was modified

* `withPostMigrationSteps(java.util.List)` was added
* `postMigrationSteps()` was added

#### `models.HyperVReplicaAzureEnableProtectionInput` was modified

* `targetVmSecurityProfile()` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `userSelectedOSName()` was added
* `withUserSelectedOSName(java.lang.String)` was added
* `linuxLicenseType()` was added
* `withTargetVmSecurityProfile(models.SecurityProfileProperties)` was added

#### `models.ReplicationProtectionContainers` was modified

* `switchClusterProtection(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SwitchClusterProtectionInput,com.azure.core.util.Context)` was added
* `switchClusterProtection(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.SwitchClusterProtectionInput)` was added

#### `models.A2AEnableProtectionInput` was modified

* `protectionClusterId()` was added
* `withProtectionClusterId(java.lang.String)` was added

#### `models.HyperVReplicaAzureUpdateReplicationProtectedItemInput` was modified

* `withUserSelectedOSName(java.lang.String)` was added
* `linuxLicenseType()` was added
* `userSelectedOSName()` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added

#### `models.HyperVReplicaAzureDiskInputDetails` was modified

* `withSectorSizeInBytes(java.lang.Integer)` was added
* `sectorSizeInBytes()` was added

#### `models.HyperVReplicaAzureManagedDiskDetails` was modified

* `withTargetDiskAccountType(models.DiskAccountType)` was added
* `withSectorSizeInBytes(java.lang.Integer)` was added
* `targetDiskAccountType()` was added
* `sectorSizeInBytes()` was added

#### `models.InMageRcmEnableProtectionInput` was modified

* `withTargetManagedDiskTags(java.util.List)` was added
* `withUserSelectedOSName(java.lang.String)` was added
* `withTargetVmTags(java.util.List)` was added
* `seedManagedDiskTags()` was added
* `userSelectedOSName()` was added
* `targetManagedDiskTags()` was added
* `linuxLicenseType()` was added
* `sqlServerLicenseType()` was added
* `withSeedManagedDiskTags(java.util.List)` was added
* `withTargetVmSecurityProfile(models.SecurityProfileProperties)` was added
* `targetNicTags()` was added
* `targetVmTags()` was added
* `targetVmSecurityProfile()` was added
* `withTargetNicTags(java.util.List)` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added
* `withSqlServerLicenseType(models.SqlServerLicenseType)` was added

#### `models.InMageRcmNicInput` was modified

* `withTargetNicName(java.lang.String)` was added
* `targetNicName()` was added

#### `models.InMageRcmUnplannedFailoverInput` was modified

* `withOsUpgradeVersion(java.lang.String)` was added
* `osUpgradeVersion()` was added

#### `models.VMwareCbtMigrationDetails` was modified

* `linuxLicenseType()` was added
* `withLinuxLicenseType(models.LinuxLicenseType)` was added

## 1.2.0 (2024-12-23)

- Azure Resource Manager SiteRecovery client library for Java. This package contains Microsoft Azure SDK for SiteRecovery Management SDK.  Package tag package-2023-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.ProtectionProfileCustomDetails` was modified

* `resourceType()` was added

#### `models.TestFailoverJobDetails` was modified

* `instanceType()` was added

#### `models.InMageAzureV2ReplicationDetails` was modified

* `instanceType()` was added

#### `models.A2ARecoveryPointDetails` was modified

* `instanceType()` was added

#### `models.RecoveryProximityPlacementGroupCustomDetails` was modified

* `resourceType()` was added

#### `models.InMageRcmEventDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanInMageFailoverInput` was modified

* `instanceType()` was added

#### `models.FabricReplicationGroupTaskDetails` was modified

* `instanceType()` was added

#### `models.AzureFabricCreationInput` was modified

* `instanceType()` was added

#### `models.AzureToAzureNetworkMappingSettings` was modified

* `instanceType()` was added

#### `models.VmmToAzureCreateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.VmmDetails` was modified

* `instanceType()` was added

#### `models.EventProviderSpecificDetails` was modified

* `instanceType()` was added

#### `models.VMwareCbtUpdateMigrationItemInput` was modified

* `instanceType()` was added

#### `models.A2AProtectionContainerMappingDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanA2AFailoverInput` was modified

* `instanceType()` was added

#### `models.VMwareVirtualMachineDetails` was modified

* `instanceType()` was added

#### `models.RecoveryResourceGroupCustomDetails` was modified

* `resourceType()` was added

#### `models.InMageAzureV2ReprotectInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaBluePolicyInput` was modified

* `instanceType()` was added

#### `models.SwitchProtectionProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaPolicyDetails` was modified

* `instanceType()` was added

#### `models.SwitchProtectionJobDetails` was modified

* `instanceType()` was added

#### `models.InMageAzureV2EventDetails` was modified

* `instanceType()` was added

#### `models.UpdateMigrationItemProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzurePolicyInput` was modified

* `instanceType()` was added

#### `models.RecoveryAvailabilitySetCustomDetails` was modified

* `resourceType()` was added

#### `models.RecoveryPlanInMageRcmFailbackFailoverInput` was modified

* `instanceType()` was added

#### `models.InMageRcmPolicyCreationInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureReplicationDetails` was modified

* `instanceType()` was added

#### `models.HyperVVirtualMachineDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmRecoveryPointDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmFailbackPolicyCreationInput` was modified

* `instanceType()` was added

#### `models.VmwareCbtPolicyDetails` was modified

* `instanceType()` was added

#### `models.JobDetails` was modified

* `instanceType()` was added

#### `models.InMageAzureV2PolicyDetails` was modified

* `instanceType()` was added

#### `models.ReverseReplicationProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.InMageAzureV2SwitchProviderInput` was modified

* `instanceType()` was added

#### `models.PolicyProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.A2AReprotectInput` was modified

* `instanceType()` was added

#### `models.A2AContainerCreationInput` was modified

* `instanceType()` was added

#### `models.InMageRcmUpdateApplianceForReplicationProtectedItemInput` was modified

* `instanceType()` was added

#### `models.ConsistencyCheckTaskDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmUpdateReplicationProtectedItemInput` was modified

* `instanceType()` was added

#### `models.PolicyProviderSpecificDetails` was modified

* `instanceType()` was added

#### `models.VMwareCbtResyncInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanHyperVReplicaAzureFailbackInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaBluePolicyDetails` was modified

* `instanceType()` was added

#### `models.DisableProtectionProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.A2AUnplannedFailoverInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaBasePolicyDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaBaseReplicationDetails` was modified

* `instanceType()` was added

#### `models.AzureFabricSpecificDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmFailbackPlannedFailoverProviderInput` was modified

* `instanceType()` was added

#### `models.A2ACrossClusterMigrationContainerCreationInput` was modified

* `instanceType()` was added

#### `models.A2AReplicationIntentDetails` was modified

* `instanceType()` was added

#### `models.ExportJobDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureFailbackProviderInput` was modified

* `instanceType()` was added

#### `models.A2AUpdateContainerMappingInput` was modified

* `instanceType()` was added

#### `models.UpdateReplicationProtectedItemProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.AzureToAzureUpdateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaBlueReplicationDetails` was modified

* `instanceType()` was added

#### `models.A2AReplicationDetails` was modified

* `instanceType()` was added

#### `models.JobTaskDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmFabricCreationInput` was modified

* `instanceType()` was added

#### `models.FabricSpecificCreationInput` was modified

* `instanceType()` was added

#### `models.VmmToVmmUpdateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.A2APolicyCreationInput` was modified

* `instanceType()` was added

#### `models.ManualActionTaskDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmReprotectInput` was modified

* `instanceType()` was added

#### `models.ReplicationProviderSpecificContainerMappingInput` was modified

* `instanceType()` was added

#### `models.InMageAzureV2PolicyInput` was modified

* `instanceType()` was added

#### `models.InMageReplicationDetails` was modified

* `instanceType()` was added

#### `models.MigrationProviderSpecificSettings` was modified

* `instanceType()` was added

#### `models.AzureToAzureCreateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanGroupTaskDetails` was modified

* `instanceType()` was added

#### `models.AsrJobDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanAutomationRunbookActionDetails` was modified

* `instanceType()` was added

#### `models.VmmToVmmCreateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.VMwareCbtTestMigrateInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaPolicyInput` was modified

* `instanceType()` was added

#### `models.InMageAzureV2TestFailoverInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanInMageAzureV2FailoverInput` was modified

* `instanceType()` was added

#### `models.VMwareCbtEnableMigrationInput` was modified

* `instanceType()` was added

#### `models.ApplyRecoveryPointProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.UnplannedFailoverProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.A2ARemoveDisksInput` was modified

* `instanceType()` was added

#### `models.ExistingRecoveryVirtualNetwork` was modified

* `resourceType()` was added

#### `models.ExistingRecoveryAvailabilitySet` was modified

* `resourceType()` was added

#### `models.InMageRcmFailbackPolicyDetails` was modified

* `instanceType()` was added

#### `models.ReplicationProtectionIntentProviderSpecificSettings` was modified

* `instanceType()` was added

#### `models.InMageUnplannedFailoverInput` was modified

* `instanceType()` was added

#### `models.SwitchProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.VMwareDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmTestFailoverInput` was modified

* `instanceType()` was added

#### `models.NetworkMappingFabricSpecificSettings` was modified

* `instanceType()` was added

#### `models.ProtectionContainerMappingProviderSpecificDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzurePolicyDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanProviderSpecificDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmApplianceSpecificDetails` was modified

* `instanceType()` was added

#### `models.ReplicationProviderSpecificSettings` was modified

* `instanceType()` was added

#### `models.CreateProtectionIntentProviderSpecificDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmFailbackEventDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmFailbackReprotectInput` was modified

* `instanceType()` was added

#### `models.InMageRcmPolicyDetails` was modified

* `instanceType()` was added

#### `models.VmmToAzureUpdateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.ExistingStorageAccount` was modified

* `resourceType()` was added

#### `models.VirtualMachineTaskDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmFailbackReplicationDetails` was modified

* `instanceType()` was added

#### `models.NewProtectionProfile` was modified

* `resourceType()` was added

#### `models.A2AAddDisksInput` was modified

* `instanceType()` was added

#### `models.InMageRcmReplicationDetails` was modified

* `instanceType()` was added

#### `models.VMwareCbtEventDetails` was modified

* `instanceType()` was added

#### `models.ExistingRecoveryResourceGroup` was modified

* `resourceType()` was added

#### `models.AutomationRunbookTaskDetails` was modified

* `instanceType()` was added

#### `models.VMwareCbtContainerMappingInput` was modified

* `instanceType()` was added

#### `models.VMwareCbtPolicyCreationInput` was modified

* `instanceType()` was added

#### `models.VMwareCbtMigrateInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureTestFailoverInput` was modified

* `instanceType()` was added

#### `models.InMageTestFailoverInput` was modified

* `instanceType()` was added

#### `models.StorageAccountCustomDetails` was modified

* `resourceType()` was added

#### `models.FailoverJobDetails` was modified

* `instanceType()` was added

#### `models.A2ASwitchProtectionInput` was modified

* `instanceType()` was added

#### `models.InMageAzureV2RecoveryPointDetails` was modified

* `instanceType()` was added

#### `models.VMwareV2FabricCreationInput` was modified

* `instanceType()` was added

#### `models.TaskTypeDetails` was modified

* `instanceType()` was added

#### `models.ScriptActionTaskDetails` was modified

* `instanceType()` was added

#### `models.ConfigurationSettings` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureEventDetails` was modified

* `instanceType()` was added

#### `models.A2ACrossClusterMigrationEnableProtectionInput` was modified

* `instanceType()` was added

#### `models.InMageDisableProtectionProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.EnableMigrationProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.A2AApplyRecoveryPointInput` was modified

* `instanceType()` was added

#### `models.ReplicationProviderSpecificUpdateContainerMappingInput` was modified

* `instanceType()` was added

#### `models.A2ATestFailoverInput` was modified

* `instanceType()` was added

#### `models.FabricSpecificDetails` was modified

* `instanceType()` was added

#### `models.VMwareCbtResumeReplicationInput` was modified

* `instanceType()` was added

#### `models.A2ACrossClusterMigrationApplyRecoveryPointInput` was modified

* `instanceType()` was added

#### `models.ResumeReplicationProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.InMageBasePolicyDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureEnableProtectionInput` was modified

* `instanceType()` was added

#### `models.A2APolicyDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanActionDetails` was modified

* `instanceType()` was added

#### `models.NewRecoveryVirtualNetwork` was modified

* `resourceType()` was added

#### `models.ApplianceSpecificDetails` was modified

* `instanceType()` was added

#### `models.InMageAzureV2UnplannedFailoverInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanA2ADetails` was modified

* `instanceType()` was added

#### `models.RemoveDisksProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanScriptActionDetails` was modified

* `instanceType()` was added

#### `models.VmmToAzureNetworkMappingSettings` was modified

* `instanceType()` was added

#### `models.HyperVReplicaBaseEventDetails` was modified

* `instanceType()` was added

#### `models.ExistingProtectionProfile` was modified

* `resourceType()` was added

#### `models.EventSpecificDetails` was modified

* `instanceType()` was added

#### `models.GroupTaskDetails` was modified

* `instanceType()` was added

#### `models.InMageAzureV2EnableProtectionInput` was modified

* `instanceType()` was added

#### `models.HyperVReplica2012R2EventDetails` was modified

* `instanceType()` was added

#### `models.InMageRcmProtectionContainerMappingDetails` was modified

* `instanceType()` was added

#### `models.InMageReprotectInput` was modified

* `instanceType()` was added

#### `models.A2AContainerMappingInput` was modified

* `instanceType()` was added

#### `models.VMwareV2FabricSpecificDetails` was modified

* `instanceType()` was added

#### `models.InMagePolicyInput` was modified

* `instanceType()` was added

#### `models.InMageEnableProtectionInput` was modified

* `instanceType()` was added

#### `models.InMageRcmUpdateContainerMappingInput` was modified

* `instanceType()` was added

#### `models.InMagePolicyDetails` was modified

* `instanceType()` was added

#### `models.A2AEnableProtectionInput` was modified

* `instanceType()` was added

#### `models.UpdateReplicationProtectedItemProviderInput` was modified

* `instanceType()` was added

#### `models.ReplicationGroupDetails` was modified

* `instanceType()` was added

#### `models.MigrateProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.A2AUpdateReplicationProtectedItemInput` was modified

* `instanceType()` was added

#### `models.PlannedFailoverProviderSpecificFailoverInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureApplyRecoveryPointInput` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureUpdateReplicationProtectedItemInput` was modified

* `instanceType()` was added

#### `models.InMageRcmApplyRecoveryPointInput` was modified

* `instanceType()` was added

#### `models.ResyncProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.TestFailoverProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.FabricSpecificUpdateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.TestMigrateProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.VMwareCbtProtectionContainerMappingDetails` was modified

* `instanceType()` was added

#### `models.ProviderSpecificRecoveryPointDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanA2AInput` was modified

* `instanceType()` was added

#### `models.VmmVirtualMachineDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureReprotectInput` was modified

* `instanceType()` was added

#### `models.ReplicationProviderSpecificContainerCreationInput` was modified

* `instanceType()` was added

#### `models.RecoveryVirtualNetworkCustomDetails` was modified

* `resourceType()` was added

#### `models.HyperVReplicaReplicationDetails` was modified

* `instanceType()` was added

#### `models.JobStatusEventDetails` was modified

* `instanceType()` was added

#### `models.InMageAzureV2UpdateReplicationProtectedItemInput` was modified

* `instanceType()` was added

#### `models.FabricSpecificCreateNetworkMappingInput` was modified

* `instanceType()` was added

#### `models.AddDisksProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.HyperVSiteDetails` was modified

* `instanceType()` was added

#### `models.A2ACrossClusterMigrationPolicyCreationInput` was modified

* `instanceType()` was added

#### `models.A2ACreateProtectionIntentInput` was modified

* `instanceType()` was added

#### `models.InlineWorkflowTaskDetails` was modified

* `instanceType()` was added

#### `models.VmmToVmmNetworkMappingSettings` was modified

* `instanceType()` was added

#### `models.HyperVReplica2012EventDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanShutdownGroupTaskDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanProviderSpecificFailoverInput` was modified

* `instanceType()` was added

#### `models.A2AEventDetails` was modified

* `instanceType()` was added

#### `models.RecoveryPlanHyperVReplicaAzureFailoverInput` was modified

* `instanceType()` was added

#### `models.InMageRcmEnableProtectionInput` was modified

* `instanceType()` was added

#### `models.InMageAzureV2ApplyRecoveryPointInput` was modified

* `instanceType()` was added

#### `models.InMageRcmFabricSpecificDetails` was modified

* `instanceType()` was added

#### `models.ExistingRecoveryProximityPlacementGroup` was modified

* `resourceType()` was added

#### `models.RecoveryPlanInMageRcmFailoverInput` was modified

* `instanceType()` was added

#### `models.RecoveryPlanManualActionDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzureUnplannedFailoverInput` was modified

* `instanceType()` was added

#### `models.InMageRcmUnplannedFailoverInput` was modified

* `instanceType()` was added

#### `models.VMwareCbtContainerCreationInput` was modified

* `instanceType()` was added

#### `models.VmNicUpdatesTaskDetails` was modified

* `instanceType()` was added

#### `models.HyperVReplicaAzurePlannedFailoverProviderInput` was modified

* `instanceType()` was added

#### `models.EnableProtectionProviderSpecificInput` was modified

* `instanceType()` was added

#### `models.A2ACrossClusterMigrationReplicationDetails` was modified

* `instanceType()` was added

#### `models.VMwareCbtMigrationDetails` was modified

* `instanceType()` was added

## 1.1.0 (2024-02-22)

- Azure Resource Manager SiteRecovery client library for Java. This package contains Microsoft Azure SDK for SiteRecovery Management SDK.  Package tag package-2023-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ReplicationFabrics` was modified

* `removeInfra(java.lang.String,java.lang.String,java.lang.String)` was added
* `removeInfra(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.A2AEnableProtectionInput` was modified

* `autoProtectionOfDataDisk()` was added
* `withAutoProtectionOfDataDisk(models.AutoProtectionOfDataDisk)` was added

#### `models.Fabric` was modified

* `removeInfra()` was added
* `removeInfra(com.azure.core.util.Context)` was added

## 1.0.0 (2023-09-22)

- Azure Resource Manager SiteRecovery client library for Java. This package contains Microsoft Azure SDK for SiteRecovery Management SDK.  Package tag package-2023-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OSUpgradeSupportedVersions` was added

* `models.ChurnOptionSelected` was added

* `models.GatewayOperationDetails` was added

* `models.VMwareCbtSecurityProfileProperties` was added

* `models.ApplianceMonitoringDetails` was added

* `models.A2AFabricSpecificLocationDetails` was added

* `models.SecurityType` was added

* `models.DataStoreUtilizationDetails` was added

* `models.ApplianceResourceDetails` was added

#### `models.InMageAzureV2ReplicationDetails` was modified

* `osName()` was added
* `allAvailableOSUpgradeConfigurations()` was added
* `withSupportedOSVersions(java.util.List)` was added
* `supportedOSVersions()` was added
* `withAllAvailableOSUpgradeConfigurations(java.util.List)` was added

#### `models.HyperVReplicaAzureReplicationDetails` was modified

* `withAllAvailableOSUpgradeConfigurations(java.util.List)` was added
* `allAvailableOSUpgradeConfigurations()` was added

#### `models.AzureFabricSpecificDetails` was modified

* `withLocationDetails(java.util.List)` was added
* `locationDetails()` was added

#### `models.A2AReplicationDetails` was modified

* `churnOptionSelected()` was added

#### `models.VMwareCbtProtectedDiskDetails` was modified

* `gatewayOperationDetails()` was added

#### `models.VMwareCbtTestMigrateInput` was modified

* `osUpgradeVersion()` was added
* `withOsUpgradeVersion(java.lang.String)` was added

#### `models.InMageAzureV2TestFailoverInput` was modified

* `osUpgradeVersion()` was added
* `withOsUpgradeVersion(java.lang.String)` was added

#### `models.VMwareCbtEnableMigrationInput` was modified

* `withTargetVmSecurityProfile(models.VMwareCbtSecurityProfileProperties)` was added
* `withConfidentialVmKeyVaultId(java.lang.String)` was added
* `targetVmSecurityProfile()` was added
* `confidentialVmKeyVaultId()` was added

#### `models.VMwareCbtMigrateInput` was modified

* `withOsUpgradeVersion(java.lang.String)` was added
* `osUpgradeVersion()` was added

#### `models.HyperVReplicaAzureTestFailoverInput` was modified

* `osUpgradeVersion()` was added
* `withOsUpgradeVersion(java.lang.String)` was added

#### `models.InMageAzureV2UnplannedFailoverInput` was modified

* `osUpgradeVersion()` was added
* `withOsUpgradeVersion(java.lang.String)` was added

#### `models.VMwareCbtProtectionContainerMappingDetails` was modified

* `withExcludedSkus(java.util.List)` was added
* `excludedSkus()` was added

#### `models.HyperVReplicaAzurePlannedFailoverProviderInput` was modified

* `withOsUpgradeVersion(java.lang.String)` was added
* `osUpgradeVersion()` was added

#### `models.VMwareCbtMigrationDetails` was modified

* `gatewayOperationDetails()` was added
* `isCheckSumResyncCycle()` was added
* `targetVmSecurityProfile()` was added
* `withTargetVmSecurityProfile(models.VMwareCbtSecurityProfileProperties)` was added
* `osName()` was added
* `operationName()` was added
* `applianceMonitoringDetails()` was added
* `confidentialVmKeyVaultId()` was added
* `withConfidentialVmKeyVaultId(java.lang.String)` was added
* `supportedOSVersions()` was added
* `deltaSyncProgressPercentage()` was added
* `withSupportedOSVersions(java.util.List)` was added
* `deltaSyncRetryCount()` was added

## 1.0.0-beta.1 (2023-01-11)

- Azure Resource Manager SiteRecovery client library for Java. This package contains Microsoft Azure SDK for SiteRecovery Management SDK.  Package tag package-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

