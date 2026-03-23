# Release History

## 1.4.0 (2026-03-23)

- Azure Resource Manager Site Recovery client library for Java. This package contains Microsoft Azure SDK for Site Recovery Management SDK. Open API for RecoveryServicesSiteRecovery. Package api-version 2025-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ClusterRecoveryPointCollection` was removed

#### `models.RecoveryServicesProviderCollection` was removed

#### `models.ProtectionContainerMappingCollection` was removed

#### `models.PolicyCollection` was removed

#### `models.ReplicationProtectedItemCollection` was removed

#### `models.OperationsDiscoveryCollection` was removed

#### `models.VaultSettingCollection` was removed

#### `models.RecoveryPlanCollection` was removed

#### `models.AlertCollection` was removed

#### `models.RecoveryPointCollection` was removed

#### `models.StorageClassificationMappingCollection` was removed

#### `models.ApplianceCollection` was removed

#### `models.MigrationRecoveryPointCollection` was removed

#### `models.StorageClassificationCollection` was removed

#### `models.ProtectionContainerCollection` was removed

#### `models.VCenterCollection` was removed

#### `models.NetworkCollection` was removed

#### `models.LogicalNetworkCollection` was removed

#### `models.EventCollection` was removed

#### `models.FabricCollection` was removed

#### `models.JobCollection` was removed

#### `models.NetworkMappingCollection` was removed

#### `models.ProtectableItemCollection` was removed

#### `models.ReplicationProtectionIntentCollection` was removed

#### `models.ReplicationProtectionClusterCollection` was removed

#### `models.TargetComputeSizeCollection` was removed

#### `models.MigrationItemCollection` was removed

#### `models.OSDetails` was modified

* `OSDetails()` was changed to private access
* `withOSMajorVersion(java.lang.String)` was removed
* `withOSMinorVersion(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withProductType(java.lang.String)` was removed
* `withOSVersion(java.lang.String)` was removed
* `withUserSelectedOSName(java.lang.String)` was removed
* `withOsEdition(java.lang.String)` was removed

#### `models.RetentionVolume` was modified

* `RetentionVolume()` was changed to private access
* `withFreeSpaceInBytes(java.lang.Long)` was removed
* `withVolumeName(java.lang.String)` was removed
* `withCapacityInBytes(java.lang.Long)` was removed
* `withThresholdPercentage(java.lang.Integer)` was removed

#### `models.TestFailoverJobDetails` was modified

* `TestFailoverJobDetails()` was changed to private access
* `withAffectedObjectDetails(java.util.Map)` was removed
* `withNetworkName(java.lang.String)` was removed
* `withComments(java.lang.String)` was removed
* `withNetworkFriendlyName(java.lang.String)` was removed
* `withNetworkType(java.lang.String)` was removed
* `withTestFailoverStatus(java.lang.String)` was removed
* `withProtectedItemDetails(java.util.List)` was removed

#### `models.InMageProtectedDiskDetails` was modified

* `InMageProtectedDiskDetails()` was changed to private access
* `withFileSystemCapacityInBytes(java.lang.Long)` was removed
* `withDiskId(java.lang.String)` was removed
* `withProgressHealth(java.lang.String)` was removed
* `withResyncProcessedBytes(java.lang.Long)` was removed
* `withRpoInSeconds(java.lang.Long)` was removed
* `withSourceDataInMB(java.lang.Double)` was removed
* `withProtectionStage(java.lang.String)` was removed
* `withResyncStartTime(java.time.OffsetDateTime)` was removed
* `withDiskResized(java.lang.String)` was removed
* `withDiskCapacityInBytes(java.lang.Long)` was removed
* `withHealthErrorCode(java.lang.String)` was removed
* `withResyncProgressPercentage(java.lang.Integer)` was removed
* `withDiskName(java.lang.String)` was removed
* `withProgressStatus(java.lang.String)` was removed
* `withResyncTotalTransferredBytes(java.lang.Long)` was removed
* `withResyncLast15MinutesTransferredBytes(java.lang.Long)` was removed
* `withTargetDataInMB(java.lang.Double)` was removed
* `withResyncRequired(java.lang.String)` was removed
* `withResyncLastDataTransferTimeUtc(java.time.OffsetDateTime)` was removed
* `withLastRpoCalculatedTime(java.time.OffsetDateTime)` was removed
* `withResyncDurationInSeconds(java.lang.Long)` was removed
* `withPsDataInMB(java.lang.Double)` was removed

#### `models.SupportedOSProperty` was modified

* `SupportedOSProperty()` was changed to private access
* `withSupportedOs(java.util.List)` was removed
* `withInstanceType(java.lang.String)` was removed

#### `models.InMageAzureV2ReplicationDetails` was modified

* `InMageAzureV2ReplicationDetails()` was changed to private access
* `withTotalProgressHealth(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withVhdName(java.lang.String)` was removed
* `withTargetVmTags(java.util.Map)` was removed
* `withTargetProximityPlacementGroupId(java.lang.String)` was removed
* `withAllAvailableOSUpgradeConfigurations(java.util.List)` was removed
* `withAgentVersion(java.lang.String)` was removed
* `withOsDiskId(java.lang.String)` was removed
* `withSwitchProviderDetails(models.InMageAzureV2SwitchProviderDetails)` was removed
* `withVCenterInfrastructureId(java.lang.String)` was removed
* `withTargetVmId(java.lang.String)` was removed
* `withSeedManagedDiskTags(java.util.Map)` was removed
* `withLicenseType(java.lang.String)` was removed
* `withVmId(java.lang.String)` was removed
* `withAzureVmGeneration(java.lang.String)` was removed
* `withUseManagedDisks(java.lang.String)` was removed
* `withAzureVMDiskDetails(java.util.List)` was removed
* `withProcessServerName(java.lang.String)` was removed
* `withIsAdditionalStatsAvailable(java.lang.Boolean)` was removed
* `withSwitchProviderBlockingErrorDetails(java.util.List)` was removed
* `withDiscoveryType(java.lang.String)` was removed
* `withResyncProgressPercentage(java.lang.Integer)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withSelectedRecoveryAzureNetworkId(java.lang.String)` was removed
* `withAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withDiskResized(java.lang.String)` was removed
* `withRecoveryAzureVMSize(java.lang.String)` was removed
* `withProtectedManagedDisks(java.util.List)` was removed
* `withVmNics(java.util.List)` was removed
* `withRecoveryAzureVMName(java.lang.String)` was removed
* `withRpoInSeconds(java.lang.Long)` was removed
* `withUncompressedDataRateInMB(java.lang.Double)` was removed
* `withTargetAvailabilityZone(java.lang.String)` was removed
* `withSupportedOSVersions(java.util.List)` was removed
* `withEnableRdpOnTargetOption(java.lang.String)` was removed
* `withReplicaId(java.lang.String)` was removed
* `withIsRebootAfterUpdateRequired(java.lang.String)` was removed
* `withSelectedTfoAzureNetworkId(java.lang.String)` was removed
* `withTargetNicTags(java.util.Map)` was removed
* `withMultiVmGroupId(java.lang.String)` was removed
* `withRecoveryAzureResourceGroupId(java.lang.String)` was removed
* `withSourceVmCpuCount(java.lang.Integer)` was removed
* `withInfrastructureVmId(java.lang.String)` was removed
* `withMultiVmSyncStatus(java.lang.String)` was removed
* `withLastUpdateReceivedTime(java.time.OffsetDateTime)` was removed
* `withOsVersion(java.lang.String)` was removed
* `withLastRpoCalculatedTime(java.time.OffsetDateTime)` was removed
* `withProcessServerId(java.lang.String)` was removed
* `withSourceVmRamSizeInMB(java.lang.Integer)` was removed
* `withTargetManagedDiskTags(java.util.Map)` was removed
* `withSelectedSourceNicId(java.lang.String)` was removed
* `withProtectedDisks(java.util.List)` was removed
* `withFirmwareType(java.lang.String)` was removed
* `withMultiVmGroupName(java.lang.String)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withMasterTargetId(java.lang.String)` was removed
* `withDatastores(java.util.List)` was removed
* `withProtectionStage(java.lang.String)` was removed
* `withRecoveryAvailabilitySetId(java.lang.String)` was removed
* `withVmProtectionState(java.lang.String)` was removed
* `withRecoveryAzureLogStorageAccountId(java.lang.String)` was removed
* `withRecoveryAzureStorageAccount(java.lang.String)` was removed
* `withIsAgentUpdateRequired(java.lang.String)` was removed
* `withCompressedDataRateInMB(java.lang.Double)` was removed
* `withValidationErrors(java.util.List)` was removed
* `withTotalDataTransferred(java.lang.Long)` was removed
* `withSqlServerLicenseType(java.lang.String)` was removed

#### `models.A2ARecoveryPointDetails` was modified

* `A2ARecoveryPointDetails()` was changed to private access
* `withDisks(java.util.List)` was removed
* `withRecoveryPointSyncType(models.RecoveryPointSyncType)` was removed

#### `models.ReplicationApplianceProperties` was modified

* `ReplicationApplianceProperties()` was changed to private access
* `withProviderSpecificDetails(models.ApplianceSpecificDetails)` was removed

#### `models.InMageRcmEventDetails` was modified

* `InMageRcmEventDetails()` was changed to private access

#### `models.IpConfigDetails` was modified

* `IpConfigDetails()` was changed to private access
* `withRecoverySubnetName(java.lang.String)` was removed
* `withTfoLBBackendAddressPoolIds(java.util.List)` was removed
* `withSubnetName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withIpAddressType(java.lang.String)` was removed
* `withRecoveryLBBackendAddressPoolIds(java.util.List)` was removed
* `withStaticIpAddress(java.lang.String)` was removed
* `withIsPrimary(java.lang.Boolean)` was removed
* `withTfoPublicIpAddressId(java.lang.String)` was removed
* `withTfoStaticIpAddress(java.lang.String)` was removed
* `withTfoSubnetName(java.lang.String)` was removed
* `withRecoveryStaticIpAddress(java.lang.String)` was removed
* `withRecoveryIpAddressType(java.lang.String)` was removed
* `withRecoveryPublicIpAddressId(java.lang.String)` was removed
* `withIsSeletedForFailover(java.lang.Boolean)` was removed

#### `models.FabricReplicationGroupTaskDetails` was modified

* `FabricReplicationGroupTaskDetails()` was changed to private access
* `withJobTask(models.JobEntity)` was removed
* `withSkippedReason(java.lang.String)` was removed
* `withSkippedReasonString(java.lang.String)` was removed

#### `models.InMageDiskDetails` was modified

* `InMageDiskDetails()` was changed to private access
* `withDiskSizeInMB(java.lang.String)` was removed
* `withDiskName(java.lang.String)` was removed
* `withDiskId(java.lang.String)` was removed
* `withDiskConfiguration(java.lang.String)` was removed
* `withDiskType(java.lang.String)` was removed
* `withVolumeList(java.util.List)` was removed

#### `models.AzureToAzureNetworkMappingSettings` was modified

* `AzureToAzureNetworkMappingSettings()` was changed to private access
* `withRecoveryFabricLocation(java.lang.String)` was removed
* `withPrimaryFabricLocation(java.lang.String)` was removed

#### `models.VmmDetails` was modified

* `VmmDetails()` was changed to private access

#### `models.NetworkProperties` was modified

* `NetworkProperties()` was changed to private access
* `withNetworkType(java.lang.String)` was removed
* `withFabricType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withSubnets(java.util.List)` was removed

#### `models.MarsAgentDetails` was modified

* `MarsAgentDetails()` was changed to private access

#### `models.A2AProtectionContainerMappingDetails` was modified

* `A2AProtectionContainerMappingDetails()` was changed to private access
* `withAutomationAccountArmId(java.lang.String)` was removed
* `withScheduleName(java.lang.String)` was removed
* `withAgentAutoUpdateStatus(models.AgentAutoUpdateStatus)` was removed
* `withJobScheduleName(java.lang.String)` was removed
* `withAutomationAccountAuthenticationType(models.AutomationAccountAuthenticationType)` was removed

#### `models.InMageRcmDiscoveredProtectedVmDetails` was modified

* `InMageRcmDiscoveredProtectedVmDetails()` was changed to private access

#### `models.VMwareVirtualMachineDetails` was modified

* `VMwareVirtualMachineDetails()` was changed to private access
* `withAgentVersion(java.lang.String)` was removed
* `withAgentInstalled(java.lang.String)` was removed
* `withPoweredOn(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withVCenterInfrastructureId(java.lang.String)` was removed
* `withAgentGeneratedId(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withDiscoveryType(java.lang.String)` was removed
* `withDiskDetails(java.util.List)` was removed
* `withValidationErrors(java.util.List)` was removed

#### `models.LogicalNetworkProperties` was modified

* `LogicalNetworkProperties()` was changed to private access
* `withLogicalNetworkDefinitionsStatus(java.lang.String)` was removed
* `withLogicalNetworkUsage(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withNetworkVirtualizationStatus(java.lang.String)` was removed

#### `models.AgentDetails` was modified

* `AgentDetails()` was changed to private access

#### `models.MobilityServiceUpdate` was modified

* `MobilityServiceUpdate()` was changed to private access
* `withOsType(java.lang.String)` was removed
* `withRebootStatus(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed

#### `models.HyperVReplicaPolicyDetails` was modified

* `HyperVReplicaPolicyDetails()` was changed to private access
* `withOfflineReplicationExportPath(java.lang.String)` was removed
* `withReplicaDeletionOption(java.lang.String)` was removed
* `withRecoveryPoints(java.lang.Integer)` was removed
* `withApplicationConsistentSnapshotFrequencyInHours(java.lang.Integer)` was removed
* `withInitialReplicationMethod(java.lang.String)` was removed
* `withReplicationPort(java.lang.Integer)` was removed
* `withAllowedAuthenticationType(java.lang.Integer)` was removed
* `withOfflineReplicationImportPath(java.lang.String)` was removed
* `withCompression(java.lang.String)` was removed
* `withOnlineReplicationStartTime(java.lang.String)` was removed

#### `models.SwitchProtectionJobDetails` was modified

* `SwitchProtectionJobDetails()` was changed to private access
* `withAffectedObjectDetails(java.util.Map)` was removed
* `withNewReplicationProtectedItemId(java.lang.String)` was removed

#### `models.InMageAzureV2EventDetails` was modified

* `InMageAzureV2EventDetails()` was changed to private access
* `withCorrectiveAction(java.lang.String)` was removed
* `withSummary(java.lang.String)` was removed
* `withComponent(java.lang.String)` was removed
* `withSiteName(java.lang.String)` was removed
* `withEventType(java.lang.String)` was removed
* `withCategory(java.lang.String)` was removed
* `withDetails(java.lang.String)` was removed

#### `models.ProtectionContainerProperties` was modified

* `ProtectionContainerProperties()` was changed to private access
* `withFabricFriendlyName(java.lang.String)` was removed
* `withFabricSpecificDetails(models.ProtectionContainerFabricSpecificDetails)` was removed
* `withProtectedItemCount(java.lang.Integer)` was removed
* `withPairingStatus(java.lang.String)` was removed
* `withFabricType(java.lang.String)` was removed
* `withRole(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.OSUpgradeSupportedVersions` was modified

* `OSUpgradeSupportedVersions()` was changed to private access

#### `models.AzureToAzureVmSyncedConfigDetails` was modified

* `AzureToAzureVmSyncedConfigDetails()` was changed to private access
* `withInputEndpoints(java.util.List)` was removed
* `withTags(java.util.Map)` was removed

#### `models.ClusterSwitchProtectionJobDetails` was modified

* `ClusterSwitchProtectionJobDetails()` was changed to private access
* `withNewReplicationProtectionClusterId(java.lang.String)` was removed
* `withAffectedObjectDetails(java.util.Map)` was removed

#### `models.HyperVReplicaAzureReplicationDetails` was modified

* `HyperVReplicaAzureReplicationDetails()` was changed to private access
* `withLastReplicatedTime(java.time.OffsetDateTime)` was removed
* `withSeedManagedDiskTags(java.util.Map)` was removed
* `withSelectedSourceNicId(java.lang.String)` was removed
* `withTargetAvailabilityZone(java.lang.String)` was removed
* `withUseManagedDisks(java.lang.String)` was removed
* `withRecoveryAzureStorageAccount(java.lang.String)` was removed
* `withTargetProximityPlacementGroupId(java.lang.String)` was removed
* `withRecoveryAzureVmName(java.lang.String)` was removed
* `withTargetManagedDiskTags(java.util.Map)` was removed
* `withLicenseType(java.lang.String)` was removed
* `withSourceVmRamSizeInMB(java.lang.Integer)` was removed
* `withRpoInSeconds(java.lang.Long)` was removed
* `withRecoveryAzureLogStorageAccountId(java.lang.String)` was removed
* `withOSDetails(models.OSDetails)` was removed
* `withInitialReplicationDetails(models.InitialReplicationDetails)` was removed
* `withAzureVmDiskDetails(java.util.List)` was removed
* `withRecoveryAvailabilitySetId(java.lang.String)` was removed
* `withRecoveryAzureVMSize(java.lang.String)` was removed
* `withVmId(java.lang.String)` was removed
* `withSqlServerLicenseType(java.lang.String)` was removed
* `withProtectedManagedDisks(java.util.List)` was removed
* `withEnableRdpOnTargetOption(java.lang.String)` was removed
* `withLinuxLicenseType(models.LinuxLicenseType)` was removed
* `withRecoveryAzureResourceGroupId(java.lang.String)` was removed
* `withLastRpoCalculatedTime(java.time.OffsetDateTime)` was removed
* `withTargetVmSecurityProfile(models.SecurityProfileProperties)` was removed
* `withEncryption(java.lang.String)` was removed
* `withAllAvailableOSUpgradeConfigurations(java.util.List)` was removed
* `withSelectedRecoveryAzureNetworkId(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed
* `withSourceVmCpuCount(java.lang.Integer)` was removed
* `withVmProtectionState(java.lang.String)` was removed
* `withTargetNicTags(java.util.Map)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withTargetVmTags(java.util.Map)` was removed

#### `models.HyperVVirtualMachineDetails` was modified

* `models.HyperVVirtualMachineDetails withSourceItemId(java.lang.String)` -> `models.HyperVVirtualMachineDetails withSourceItemId(java.lang.String)`
* `models.HyperVVirtualMachineDetails withHyperVHostId(java.lang.String)` -> `models.HyperVVirtualMachineDetails withHyperVHostId(java.lang.String)`
* `models.HyperVVirtualMachineDetails withDiskDetails(java.util.List)` -> `models.HyperVVirtualMachineDetails withDiskDetails(java.util.List)`
* `models.HyperVVirtualMachineDetails withHasPhysicalDisk(models.PresenceStatus)` -> `models.HyperVVirtualMachineDetails withHasPhysicalDisk(models.PresenceStatus)`
* `models.HyperVVirtualMachineDetails withOsDetails(models.OSDetails)` -> `models.HyperVVirtualMachineDetails withOsDetails(models.OSDetails)`
* `models.HyperVVirtualMachineDetails withHasFibreChannelAdapter(models.PresenceStatus)` -> `models.HyperVVirtualMachineDetails withHasFibreChannelAdapter(models.PresenceStatus)`
* `models.HyperVVirtualMachineDetails withHasSharedVhd(models.PresenceStatus)` -> `models.HyperVVirtualMachineDetails withHasSharedVhd(models.PresenceStatus)`
* `models.HyperVVirtualMachineDetails withGeneration(java.lang.String)` -> `models.HyperVVirtualMachineDetails withGeneration(java.lang.String)`

#### `models.ResourceHealthSummary` was modified

* `ResourceHealthSummary()` was changed to private access
* `withIssues(java.util.List)` was removed
* `withCategorizedResourceCounts(java.util.Map)` was removed
* `withResourceCount(java.lang.Integer)` was removed

#### `models.InMageRcmRecoveryPointDetails` was modified

* `InMageRcmRecoveryPointDetails()` was changed to private access

#### `models.VmwareCbtPolicyDetails` was modified

* `VmwareCbtPolicyDetails()` was changed to private access
* `withCrashConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withRecoveryPointHistoryInMinutes(java.lang.Integer)` was removed

#### `models.JobDetails` was modified

* `models.JobDetails withAffectedObjectDetails(java.util.Map)` -> `models.JobDetails withAffectedObjectDetails(java.util.Map)`

#### `models.CurrentJobDetails` was modified

* `CurrentJobDetails()` was changed to private access

#### `models.InMageAzureV2PolicyDetails` was modified

* `InMageAzureV2PolicyDetails()` was changed to private access
* `withMultiVmSyncStatus(java.lang.String)` was removed
* `withRecoveryPointHistory(java.lang.Integer)` was removed
* `withCrashConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withRecoveryPointThresholdInMinutes(java.lang.Integer)` was removed
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed

#### `models.A2AClusterRecoveryPointDetails` was modified

* `A2AClusterRecoveryPointDetails()` was changed to private access
* `withRecoveryPointSyncType(models.RecoveryPointSyncType)` was removed
* `withNodes(java.util.List)` was removed

#### `models.InMageRcmProtectedDiskDetails` was modified

* `InMageRcmProtectedDiskDetails()` was changed to private access
* `withCustomTargetDiskName(java.lang.String)` was removed
* `withIrDetails(models.InMageRcmSyncDetails)` was removed
* `withResyncDetails(models.InMageRcmSyncDetails)` was removed
* `withSectorSizeInBytes(java.lang.Integer)` was removed
* `withDiskType(models.DiskAccountType)` was removed

#### `models.ServiceError` was modified

* `ServiceError()` was changed to private access
* `withPossibleCauses(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `withActivityId(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed
* `withRecommendedAction(java.lang.String)` was removed

#### `models.GatewayOperationDetails` was modified

* `GatewayOperationDetails()` was changed to private access

#### `models.VCenterProperties` was modified

* `VCenterProperties()` was changed to private access
* `withRunAsAccountId(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withPort(java.lang.String)` was removed
* `withHealthErrors(java.util.List)` was removed
* `withInfrastructureId(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withInternalId(java.lang.String)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withProcessServerId(java.lang.String)` was removed
* `withDiscoveryStatus(java.lang.String)` was removed
* `withFabricArmResourceName(java.lang.String)` was removed

#### `models.DiskVolumeDetails` was modified

* `DiskVolumeDetails()` was changed to private access
* `withLabel(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.TargetComputeSizeProperties` was modified

* `TargetComputeSizeProperties()` was changed to private access
* `withHighIopsSupported(java.lang.String)` was removed
* `withMaxDataDiskCount(java.lang.Integer)` was removed
* `withErrors(java.util.List)` was removed
* `withHyperVGenerations(java.util.List)` was removed
* `withMemoryInGB(java.lang.Double)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withMaxNicsCount(java.lang.Integer)` was removed
* `withCpuCoresCount(java.lang.Integer)` was removed
* `withName(java.lang.String)` was removed

#### `models.ConsistencyCheckTaskDetails` was modified

* `ConsistencyCheckTaskDetails()` was changed to private access
* `withVmDetails(java.util.List)` was removed

#### `models.A2AExtendedLocationDetails` was modified

* `A2AExtendedLocationDetails()` was changed to private access
* `withPrimaryExtendedLocation(models.ExtendedLocation)` was removed
* `withRecoveryExtendedLocation(models.ExtendedLocation)` was removed

#### `models.InMageRcmNicDetails` was modified

* `InMageRcmNicDetails()` was changed to private access
* `withTestSubnetName(java.lang.String)` was removed
* `withTestIpAddress(java.lang.String)` was removed
* `withTargetIpAddressType(models.EthernetAddressType)` was removed
* `withTargetNicName(java.lang.String)` was removed
* `withTestIpAddressType(models.EthernetAddressType)` was removed
* `withIsPrimaryNic(java.lang.String)` was removed
* `withTargetSubnetName(java.lang.String)` was removed
* `withIsSelectedForFailover(java.lang.String)` was removed
* `withTargetIpAddress(java.lang.String)` was removed

#### `models.InMageAzureV2SwitchProviderBlockingErrorDetails` was modified

* `InMageAzureV2SwitchProviderBlockingErrorDetails()` was changed to private access

#### `models.InMageRcmMobilityAgentDetails` was modified

* `InMageRcmMobilityAgentDetails()` was changed to private access

#### `models.ReplicationProtectionIntentProperties` was modified

* `ReplicationProtectionIntentProperties()` was changed to private access
* `withProviderSpecificDetails(models.ReplicationProtectionIntentProviderSpecificSettings)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.AsrTask` was modified

* `AsrTask()` was changed to private access
* `withState(java.lang.String)` was removed
* `withAllowedActions(java.util.List)` was removed
* `withErrors(java.util.List)` was removed
* `withGroupTaskCustomDetails(models.GroupTaskDetails)` was removed
* `withCustomDetails(models.TaskTypeDetails)` was removed
* `withName(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withStateDescription(java.lang.String)` was removed
* `withTaskType(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withTaskId(java.lang.String)` was removed

#### `models.HyperVReplicaBluePolicyDetails` was modified

* `HyperVReplicaBluePolicyDetails()` was changed to private access
* `withReplicationFrequencyInSeconds(java.lang.Integer)` was removed
* `withCompression(java.lang.String)` was removed
* `withInitialReplicationMethod(java.lang.String)` was removed
* `withOnlineReplicationStartTime(java.lang.String)` was removed
* `withAllowedAuthenticationType(java.lang.Integer)` was removed
* `withApplicationConsistentSnapshotFrequencyInHours(java.lang.Integer)` was removed
* `withOfflineReplicationExportPath(java.lang.String)` was removed
* `withReplicaDeletionOption(java.lang.String)` was removed
* `withRecoveryPoints(java.lang.Integer)` was removed
* `withOfflineReplicationImportPath(java.lang.String)` was removed
* `withReplicationPort(java.lang.Integer)` was removed

#### `models.VersionDetails` was modified

* `VersionDetails()` was changed to private access
* `withVersion(java.lang.String)` was removed
* `withStatus(models.AgentVersionStatus)` was removed
* `withExpiryDate(java.time.OffsetDateTime)` was removed

#### `models.JobProperties` was modified

* `JobProperties()` was changed to private access
* `withErrors(java.util.List)` was removed
* `withTargetObjectId(java.lang.String)` was removed
* `withTasks(java.util.List)` was removed
* `withTargetObjectName(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withStateDescription(java.lang.String)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed
* `withAllowedActions(java.util.List)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withScenarioName(java.lang.String)` was removed
* `withState(java.lang.String)` was removed
* `withTargetInstanceType(java.lang.String)` was removed
* `withActivityId(java.lang.String)` was removed
* `withCustomDetails(models.JobDetails)` was removed

#### `models.HyperVReplicaBasePolicyDetails` was modified

* `HyperVReplicaBasePolicyDetails()` was changed to private access
* `withReplicationPort(java.lang.Integer)` was removed
* `withRecoveryPoints(java.lang.Integer)` was removed
* `withCompression(java.lang.String)` was removed
* `withAllowedAuthenticationType(java.lang.Integer)` was removed
* `withOfflineReplicationImportPath(java.lang.String)` was removed
* `withInitialReplicationMethod(java.lang.String)` was removed
* `withApplicationConsistentSnapshotFrequencyInHours(java.lang.Integer)` was removed
* `withOfflineReplicationExportPath(java.lang.String)` was removed
* `withReplicaDeletionOption(java.lang.String)` was removed
* `withOnlineReplicationStartTime(java.lang.String)` was removed

#### `models.HyperVReplicaBaseReplicationDetails` was modified

* `HyperVReplicaBaseReplicationDetails()` was changed to private access
* `withLastReplicatedTime(java.time.OffsetDateTime)` was removed
* `withVMDiskDetails(java.util.List)` was removed
* `withVmProtectionState(java.lang.String)` was removed
* `withInitialReplicationDetails(models.InitialReplicationDetails)` was removed
* `withVmId(java.lang.String)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed

#### `models.AzureFabricSpecificDetails` was modified

* `AzureFabricSpecificDetails()` was changed to private access
* `withContainerIds(java.util.List)` was removed
* `withLocation(java.lang.String)` was removed
* `withExtendedLocations(java.util.List)` was removed
* `withLocationDetails(java.util.List)` was removed
* `withZones(java.util.List)` was removed

#### `models.RcmProxyDetails` was modified

* `RcmProxyDetails()` was changed to private access

#### `models.ReplicationEligibilityResultsProperties` was modified

* `ReplicationEligibilityResultsProperties()` was changed to private access
* `withErrors(java.util.List)` was removed

#### `models.A2AReplicationIntentDetails` was modified

* `A2AReplicationIntentDetails()` was changed to private access
* `withAutomationAccountAuthenticationType(models.AutomationAccountAuthenticationType)` was removed
* `withRecoveryProximityPlacementGroup(models.RecoveryProximityPlacementGroupCustomDetails)` was removed
* `withAutoProtectionOfDataDisk(models.AutoProtectionOfDataDisk)` was removed
* `withDiskEncryptionInfo(models.DiskEncryptionInfo)` was removed
* `withMultiVmGroupId(java.lang.String)` was removed
* `withPrimaryLocation(java.lang.String)` was removed
* `withRecoveryBootDiagStorageAccount(models.StorageAccountCustomDetails)` was removed
* `withRecoveryResourceGroupId(java.lang.String)` was removed
* `withRecoveryLocation(java.lang.String)` was removed
* `withAutomationAccountArmId(java.lang.String)` was removed
* `withMultiVmGroupName(java.lang.String)` was removed
* `withRecoveryAvailabilitySet(models.RecoveryAvailabilitySetCustomDetails)` was removed
* `withFabricObjectId(java.lang.String)` was removed
* `withVmDisks(java.util.List)` was removed
* `withVmManagedDisks(java.util.List)` was removed
* `withPrimaryStagingStorageAccount(models.StorageAccountCustomDetails)` was removed
* `withRecoveryVirtualNetwork(models.RecoveryVirtualNetworkCustomDetails)` was removed
* `withRecoveryAvailabilityType(java.lang.String)` was removed
* `withRecoveryAvailabilityZone(java.lang.String)` was removed
* `withAgentAutoUpdateStatus(models.AgentAutoUpdateStatus)` was removed
* `withProtectionProfile(models.ProtectionProfileCustomDetails)` was removed
* `withRecoverySubscriptionId(java.lang.String)` was removed

#### `models.ExportJobDetails` was modified

* `ExportJobDetails()` was changed to private access
* `withBlobUri(java.lang.String)` was removed
* `withAffectedObjectDetails(java.util.Map)` was removed
* `withSasToken(java.lang.String)` was removed

#### `models.ReprotectAgentDetails` was modified

* `ReprotectAgentDetails()` was changed to private access

#### `models.InMageAzureV2ProtectedDiskDetails` was modified

* `InMageAzureV2ProtectedDiskDetails()` was changed to private access
* `withLastRpoCalculatedTime(java.time.OffsetDateTime)` was removed
* `withHealthErrorCode(java.lang.String)` was removed
* `withFileSystemCapacityInBytes(java.lang.Long)` was removed
* `withResyncStartTime(java.time.OffsetDateTime)` was removed
* `withSecondsToTakeSwitchProvider(java.lang.Long)` was removed
* `withProgressStatus(java.lang.String)` was removed
* `withDiskName(java.lang.String)` was removed
* `withProgressHealth(java.lang.String)` was removed
* `withResyncLast15MinutesTransferredBytes(java.lang.Long)` was removed
* `withResyncDurationInSeconds(java.lang.Long)` was removed
* `withResyncProcessedBytes(java.lang.Long)` was removed
* `withResyncTotalTransferredBytes(java.lang.Long)` was removed
* `withDiskCapacityInBytes(java.lang.Long)` was removed
* `withRpoInSeconds(java.lang.Long)` was removed
* `withTargetDataInMegaBytes(java.lang.Double)` was removed
* `withSourceDataInMegaBytes(java.lang.Double)` was removed
* `withResyncProgressPercentage(java.lang.Integer)` was removed
* `withDiskResized(java.lang.String)` was removed
* `withDiskId(java.lang.String)` was removed
* `withPsDataInMegaBytes(java.lang.Double)` was removed
* `withResyncLastDataTransferTimeUtc(java.time.OffsetDateTime)` was removed
* `withResyncRequired(java.lang.String)` was removed
* `withProtectionStage(java.lang.String)` was removed

#### `models.HyperVReplicaBlueReplicationDetails` was modified

* `HyperVReplicaBlueReplicationDetails()` was changed to private access
* `withVmProtectionState(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withInitialReplicationDetails(models.InitialReplicationDetails)` was removed
* `withLastReplicatedTime(java.time.OffsetDateTime)` was removed
* `withVMDiskDetails(java.util.List)` was removed
* `withVmId(java.lang.String)` was removed

#### `models.A2AReplicationDetails` was modified

* `A2AReplicationDetails()` was changed to private access
* `withRecoveryAzureResourceGroupId(java.lang.String)` was removed
* `withUnprotectedDisks(java.util.List)` was removed
* `withTestFailoverRecoveryFabricObjectId(java.lang.String)` was removed
* `withRecoveryAvailabilityZone(java.lang.String)` was removed
* `withPrimaryExtendedLocation(models.ExtendedLocation)` was removed
* `withSelectedRecoveryAzureNetworkId(java.lang.String)` was removed
* `withFabricObjectId(java.lang.String)` was removed
* `withIsReplicationAgentUpdateRequired(java.lang.Boolean)` was removed
* `withProtectedDisks(java.util.List)` was removed
* `withMonitoringJobType(java.lang.String)` was removed
* `withRecoveryFabricObjectId(java.lang.String)` was removed
* `withTfoAzureVMName(java.lang.String)` was removed
* `withRecoveryProximityPlacementGroupId(java.lang.String)` was removed
* `withInitialRecoveryExtendedLocation(models.ExtendedLocation)` was removed
* `withProtectionClusterId(java.lang.String)` was removed
* `withRecoveryAzureVMSize(java.lang.String)` was removed
* `withRpoInSeconds(java.lang.Long)` was removed
* `withRecoveryVirtualMachineScaleSetId(java.lang.String)` was removed
* `withInitialPrimaryExtendedLocation(models.ExtendedLocation)` was removed
* `withLastRpoCalculatedTime(java.time.OffsetDateTime)` was removed
* `withAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withIsReplicationAgentCertificateUpdateRequired(java.lang.Boolean)` was removed
* `withVmSyncedConfigDetails(models.AzureToAzureVmSyncedConfigDetails)` was removed
* `withAgentVersion(java.lang.String)` was removed
* `withSelectedTfoAzureNetworkId(java.lang.String)` was removed
* `withRecoveryBootDiagStorageAccountId(java.lang.String)` was removed
* `withProtectedManagedDisks(java.util.List)` was removed
* `withRecoveryCloudService(java.lang.String)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withMultiVmGroupName(java.lang.String)` was removed
* `withRecoveryCapacityReservationGroupId(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withMultiVmGroupCreateOption(models.MultiVmGroupCreateOption)` was removed
* `withIsClusterInfraReady(java.lang.Boolean)` was removed
* `withLifecycleId(java.lang.String)` was removed
* `withRecoveryFabricLocation(java.lang.String)` was removed
* `withRecoveryAzureVMName(java.lang.String)` was removed
* `withPrimaryAvailabilityZone(java.lang.String)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withPrimaryFabricLocation(java.lang.String)` was removed
* `withMultiVmGroupId(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed
* `withRecoveryExtendedLocation(models.ExtendedLocation)` was removed
* `withAutoProtectionOfDataDisk(models.AutoProtectionOfDataDisk)` was removed
* `withRecoveryAvailabilitySet(java.lang.String)` was removed
* `withVmProtectionState(java.lang.String)` was removed
* `withMonitoringPercentageCompletion(java.lang.Integer)` was removed
* `withManagementId(java.lang.String)` was removed

#### `models.InMageRcmFailbackSyncDetails` was modified

* `InMageRcmFailbackSyncDetails()` was changed to private access

#### `models.JobTaskDetails` was modified

* `models.JobTaskDetails withJobTask(models.JobEntity)` -> `models.JobTaskDetails withJobTask(models.JobEntity)`

#### `models.JobEntity` was modified

* `JobEntity()` was changed to private access
* `withJobScenarioName(java.lang.String)` was removed
* `withTargetInstanceType(java.lang.String)` was removed
* `withTargetObjectId(java.lang.String)` was removed
* `withTargetObjectName(java.lang.String)` was removed
* `withJobId(java.lang.String)` was removed
* `withJobFriendlyName(java.lang.String)` was removed

#### `models.ManualActionTaskDetails` was modified

* `ManualActionTaskDetails()` was changed to private access
* `withName(java.lang.String)` was removed
* `withInstructions(java.lang.String)` was removed
* `withObservation(java.lang.String)` was removed

#### `models.RunAsAccount` was modified

* `RunAsAccount()` was changed to private access
* `withAccountId(java.lang.String)` was removed
* `withAccountName(java.lang.String)` was removed

#### `models.InMageReplicationDetails` was modified

* `InMageReplicationDetails()` was changed to private access
* `withRpoInSeconds(java.lang.Long)` was removed
* `withDiscoveryType(java.lang.String)` was removed
* `withUncompressedDataRateInMB(java.lang.Double)` was removed
* `withMultiVmSyncStatus(java.lang.String)` was removed
* `withTotalDataTransferred(java.lang.Long)` was removed
* `withAzureStorageAccountId(java.lang.String)` was removed
* `withDiskResized(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed
* `withOsVersion(java.lang.String)` was removed
* `withSourceVmRamSizeInMB(java.lang.Integer)` was removed
* `withVCenterInfrastructureId(java.lang.String)` was removed
* `withOsDetails(models.OSDiskDetails)` was removed
* `withResyncDetails(models.InitialReplicationDetails)` was removed
* `withProcessServerId(java.lang.String)` was removed
* `withInfrastructureVmId(java.lang.String)` was removed
* `withReplicaId(java.lang.String)` was removed
* `withDatastores(java.util.List)` was removed
* `withCompressedDataRateInMB(java.lang.Double)` was removed
* `withProtectedDisks(java.util.List)` was removed
* `withRebootAfterUpdateStatus(java.lang.String)` was removed
* `withLastUpdateReceivedTime(java.time.OffsetDateTime)` was removed
* `withRetentionWindowEnd(java.time.OffsetDateTime)` was removed
* `withVmProtectionState(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withMultiVmGroupId(java.lang.String)` was removed
* `withProtectionStage(java.lang.String)` was removed
* `withConsistencyPoints(java.util.Map)` was removed
* `withMultiVmGroupName(java.lang.String)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withAgentDetails(models.InMageAgentDetails)` was removed
* `withIsAdditionalStatsAvailable(java.lang.Boolean)` was removed
* `withLastRpoCalculatedTime(java.time.OffsetDateTime)` was removed
* `withActiveSiteType(java.lang.String)` was removed
* `withMasterTargetId(java.lang.String)` was removed
* `withValidationErrors(java.util.List)` was removed
* `withTotalProgressHealth(java.lang.String)` was removed
* `withVmId(java.lang.String)` was removed
* `withSourceVmCpuCount(java.lang.Integer)` was removed
* `withRetentionWindowStart(java.time.OffsetDateTime)` was removed

#### `models.ProtectableItemProperties` was modified

* `ProtectableItemProperties()` was changed to private access
* `withReplicationProtectedItemId(java.lang.String)` was removed
* `withSupportedReplicationProviders(java.util.List)` was removed
* `withRecoveryServicesProviderId(java.lang.String)` was removed
* `withProtectionStatus(java.lang.String)` was removed
* `withProtectionReadinessErrors(java.util.List)` was removed
* `withCustomDetails(models.ConfigurationSettings)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.RecoveryPlanGroupTaskDetails` was modified

* `models.RecoveryPlanGroupTaskDetails withName(java.lang.String)` -> `models.RecoveryPlanGroupTaskDetails withName(java.lang.String)`
* `models.RecoveryPlanGroupTaskDetails withRpGroupType(java.lang.String)` -> `models.RecoveryPlanGroupTaskDetails withRpGroupType(java.lang.String)`
* `withChildTasks(java.util.List)` was removed
* `models.RecoveryPlanGroupTaskDetails withGroupId(java.lang.String)` -> `models.RecoveryPlanGroupTaskDetails withGroupId(java.lang.String)`

#### `models.VMwareCbtProtectedDiskDetails` was modified

* `VMwareCbtProtectedDiskDetails()` was changed to private access
* `withSectorSizeInBytes(java.lang.Integer)` was removed
* `withTargetDiskName(java.lang.String)` was removed
* `withDiskType(models.DiskAccountType)` was removed

#### `models.AsrJobDetails` was modified

* `AsrJobDetails()` was changed to private access
* `withAffectedObjectDetails(java.util.Map)` was removed

#### `models.StorageClassificationMappingProperties` was modified

* `StorageClassificationMappingProperties()` was changed to private access
* `withTargetStorageClassificationId(java.lang.String)` was removed

#### `models.ApplianceMonitoringDetails` was modified

* `ApplianceMonitoringDetails()` was changed to private access

#### `models.InMageFabricSwitchProviderBlockingErrorDetails` was modified

* `InMageFabricSwitchProviderBlockingErrorDetails()` was changed to private access

#### `models.Subnet` was modified

* `Subnet()` was changed to private access
* `withAddressList(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.NetworkMappingProperties` was modified

* `NetworkMappingProperties()` was changed to private access
* `withPrimaryNetworkId(java.lang.String)` was removed
* `withRecoveryNetworkFriendlyName(java.lang.String)` was removed
* `withFabricSpecificSettings(models.NetworkMappingFabricSpecificSettings)` was removed
* `withPrimaryNetworkFriendlyName(java.lang.String)` was removed
* `withRecoveryFabricArmId(java.lang.String)` was removed
* `withRecoveryNetworkId(java.lang.String)` was removed
* `withState(java.lang.String)` was removed
* `withRecoveryFabricFriendlyName(java.lang.String)` was removed
* `withPrimaryFabricFriendlyName(java.lang.String)` was removed

#### `models.OSDiskDetails` was modified

* `OSDiskDetails()` was changed to private access
* `withOsVhdId(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withVhdName(java.lang.String)` was removed

#### `models.FailoverReplicationProtectedItemDetails` was modified

* `FailoverReplicationProtectedItemDetails()` was changed to private access
* `withTestVmName(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withNetworkFriendlyName(java.lang.String)` was removed
* `withSubnet(java.lang.String)` was removed
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed
* `withTestVmFriendlyName(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withNetworkConnectionStatus(java.lang.String)` was removed
* `withRecoveryPointId(java.lang.String)` was removed

#### `models.DataStore` was modified

* `DataStore()` was changed to private access
* `withType(java.lang.String)` was removed
* `withSymbolicName(java.lang.String)` was removed
* `withCapacity(java.lang.String)` was removed
* `withFreeSpace(java.lang.String)` was removed
* `withUuid(java.lang.String)` was removed

#### `models.FabricProperties` was modified

* `FabricProperties()` was changed to private access
* `withBcdrState(java.lang.String)` was removed
* `withHealthErrorDetails(java.util.List)` was removed
* `withCustomDetails(models.FabricSpecificDetails)` was removed
* `withHealth(java.lang.String)` was removed
* `withRolloverEncryptionDetails(models.EncryptionDetails)` was removed
* `withInternalIdentifier(java.lang.String)` was removed
* `withEncryptionDetails(models.EncryptionDetails)` was removed
* `withFriendlyName(java.lang.String)` was removed

#### `models.InMageRcmFailbackPolicyDetails` was modified

* `InMageRcmFailbackPolicyDetails()` was changed to private access
* `withCrashConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed

#### `models.StorageClassificationProperties` was modified

* `StorageClassificationProperties()` was changed to private access
* `withFriendlyName(java.lang.String)` was removed

#### `models.VMwareDetails` was modified

* `VMwareDetails()` was changed to private access
* `withSslCertExpiryDate(java.time.OffsetDateTime)` was removed
* `withSpaceUsageStatus(java.lang.String)` was removed
* `withCsServiceStatus(java.lang.String)` was removed
* `withSystemLoad(java.lang.String)` was removed
* `withMemoryUsageStatus(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withWebLoad(java.lang.String)` was removed
* `withAgentVersion(java.lang.String)` was removed
* `withAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withCpuLoad(java.lang.String)` was removed
* `withRunAsAccounts(java.util.List)` was removed
* `withDatabaseServerLoad(java.lang.String)` was removed
* `withProtectedServers(java.lang.String)` was removed
* `withHostname(java.lang.String)` was removed
* `withDatabaseServerLoadStatus(java.lang.String)` was removed
* `withPsTemplateVersion(java.lang.String)` was removed
* `withAgentCount(java.lang.String)` was removed
* `withProcessServerCount(java.lang.String)` was removed
* `withMasterTargetServers(java.util.List)` was removed
* `withSslCertExpiryRemainingDays(java.lang.Integer)` was removed
* `withTotalSpaceInBytes(java.lang.Long)` was removed
* `withSystemLoadStatus(java.lang.String)` was removed
* `withAgentVersionDetails(models.VersionDetails)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withAvailableMemoryInBytes(java.lang.Long)` was removed
* `withSwitchProviderBlockingErrorDetails(java.util.List)` was removed
* `withTotalMemoryInBytes(java.lang.Long)` was removed
* `withProcessServers(java.util.List)` was removed
* `withCpuLoadStatus(java.lang.String)` was removed
* `withReplicationPairCount(java.lang.String)` was removed
* `withVersionStatus(java.lang.String)` was removed
* `withWebLoadStatus(java.lang.String)` was removed
* `withAvailableSpaceInBytes(java.lang.Long)` was removed

#### `models.ProviderError` was modified

* `ProviderError()` was changed to private access
* `withRecommendedAction(java.lang.String)` was removed
* `withErrorId(java.lang.String)` was removed
* `withPossibleCauses(java.lang.String)` was removed
* `withErrorMessage(java.lang.String)` was removed
* `withErrorCode(java.lang.Integer)` was removed

#### `models.HyperVReplicaAzurePolicyDetails` was modified

* `HyperVReplicaAzurePolicyDetails()` was changed to private access
* `withEncryption(java.lang.String)` was removed
* `withActiveStorageAccountId(java.lang.String)` was removed
* `withApplicationConsistentSnapshotFrequencyInHours(java.lang.Integer)` was removed
* `withReplicationInterval(java.lang.Integer)` was removed
* `withRecoveryPointHistoryDurationInHours(java.lang.Integer)` was removed
* `withOnlineReplicationStartTime(java.lang.String)` was removed

#### `models.RecoveryPlanProperties` was modified

* `RecoveryPlanProperties()` was changed to private access
* `withCurrentScenarioStatus(java.lang.String)` was removed
* `withGroups(java.util.List)` was removed
* `withLastPlannedFailoverTime(java.time.OffsetDateTime)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withProviderSpecificDetails(java.util.List)` was removed
* `withCurrentScenarioStatusDescription(java.lang.String)` was removed
* `withLastTestFailoverTime(java.time.OffsetDateTime)` was removed
* `withLastUnplannedFailoverTime(java.time.OffsetDateTime)` was removed
* `withRecoveryFabricId(java.lang.String)` was removed
* `withPrimaryFabricFriendlyName(java.lang.String)` was removed
* `withCurrentScenario(models.CurrentScenarioDetails)` was removed
* `withRecoveryFabricFriendlyName(java.lang.String)` was removed
* `withPrimaryFabricId(java.lang.String)` was removed
* `withAllowedOperations(java.util.List)` was removed
* `withFailoverDeploymentModel(java.lang.String)` was removed
* `withReplicationProviders(java.util.List)` was removed

#### `models.InMageRcmApplianceSpecificDetails` was modified

* `InMageRcmApplianceSpecificDetails()` was changed to private access

#### `models.ProtectionContainerFabricSpecificDetails` was modified

* `ProtectionContainerFabricSpecificDetails()` was changed to private access

#### `models.InMageRcmFailbackEventDetails` was modified

* `InMageRcmFailbackEventDetails()` was changed to private access

#### `models.ReplicationProtectedItemProperties` was modified

* `ReplicationProtectedItemProperties()` was changed to private access
* `withProtectableItemId(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withTestFailoverState(java.lang.String)` was removed
* `withLastSuccessfulTestFailoverTime(java.time.OffsetDateTime)` was removed
* `withFailoverHealth(java.lang.String)` was removed
* `withPrimaryFabricProvider(java.lang.String)` was removed
* `withProviderSpecificDetails(models.ReplicationProviderSpecificSettings)` was removed
* `withLastSuccessfulFailoverTime(java.time.OffsetDateTime)` was removed
* `withFailoverRecoveryPointId(java.lang.String)` was removed
* `withProtectionStateDescription(java.lang.String)` was removed
* `withHealthErrors(java.util.List)` was removed
* `withRecoveryProtectionContainerFriendlyName(java.lang.String)` was removed
* `withCurrentScenario(models.CurrentScenarioDetails)` was removed
* `withReplicationHealth(java.lang.String)` was removed
* `withSwitchProviderState(java.lang.String)` was removed
* `withTestFailoverStateDescription(java.lang.String)` was removed
* `withRecoveryContainerId(java.lang.String)` was removed
* `withPrimaryProtectionContainerFriendlyName(java.lang.String)` was removed
* `withProtectionState(java.lang.String)` was removed
* `withSwitchProviderStateDescription(java.lang.String)` was removed
* `withProtectedItemType(java.lang.String)` was removed
* `withActiveLocation(java.lang.String)` was removed
* `withRecoveryServicesProviderId(java.lang.String)` was removed
* `withRecoveryFabricFriendlyName(java.lang.String)` was removed
* `withPolicyFriendlyName(java.lang.String)` was removed
* `withPrimaryFabricFriendlyName(java.lang.String)` was removed
* `withEventCorrelationId(java.lang.String)` was removed
* `withRecoveryFabricId(java.lang.String)` was removed
* `withAllowedOperations(java.util.List)` was removed
* `withPolicyId(java.lang.String)` was removed

#### `models.HealthErrorSummary` was modified

* `HealthErrorSummary()` was changed to private access
* `withSeverity(models.Severity)` was removed
* `withAffectedResourceType(java.lang.String)` was removed
* `withAffectedResourceSubtype(java.lang.String)` was removed
* `withAffectedResourceCorrelationIds(java.util.List)` was removed
* `withCategory(models.HealthErrorCategory)` was removed
* `withSummaryCode(java.lang.String)` was removed
* `withSummaryMessage(java.lang.String)` was removed

#### `models.PolicyProperties` was modified

* `PolicyProperties()` was changed to private access
* `withFriendlyName(java.lang.String)` was removed
* `withProviderSpecificDetails(models.PolicyProviderSpecificDetails)` was removed

#### `models.InMageRcmPolicyDetails` was modified

* `InMageRcmPolicyDetails()` was changed to private access
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withRecoveryPointHistoryInMinutes(java.lang.Integer)` was removed
* `withCrashConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withEnableMultiVmSync(java.lang.String)` was removed

#### `models.VirtualMachineTaskDetails` was modified

* `VirtualMachineTaskDetails()` was changed to private access
* `withJobTask(models.JobEntity)` was removed
* `withSkippedReason(java.lang.String)` was removed
* `withSkippedReasonString(java.lang.String)` was removed

#### `models.InMageRcmFailbackReplicationDetails` was modified

* `InMageRcmFailbackReplicationDetails()` was changed to private access
* `withProtectedDisks(java.util.List)` was removed
* `withVmNics(java.util.List)` was removed
* `withMobilityAgentDetails(models.InMageRcmFailbackMobilityAgentDetails)` was removed
* `withDiscoveredVmDetails(models.InMageRcmFailbackDiscoveredProtectedVmDetails)` was removed

#### `models.InMageRcmReplicationDetails` was modified

* `InMageRcmReplicationDetails()` was changed to private access
* `withLicenseType(java.lang.String)` was removed
* `withTargetLocation(java.lang.String)` was removed
* `withDiscoveredVmDetails(models.InMageRcmDiscoveredProtectedVmDetails)` was removed
* `withUnprotectedDisks(java.util.List)` was removed
* `withTargetNicTags(java.util.List)` was removed
* `withTargetAvailabilityZone(java.lang.String)` was removed
* `withSupportedOSVersions(java.util.List)` was removed
* `withTargetVmSize(java.lang.String)` was removed
* `withProtectedDisks(java.util.List)` was removed
* `withVmNics(java.util.List)` was removed
* `withOsName(java.lang.String)` was removed
* `withSqlServerLicenseType(java.lang.String)` was removed
* `withTargetProximityPlacementGroupId(java.lang.String)` was removed
* `withMobilityAgentDetails(models.InMageRcmMobilityAgentDetails)` was removed
* `withTargetVmSecurityProfile(models.SecurityProfileProperties)` was removed
* `withLinuxLicenseType(models.LinuxLicenseType)` was removed
* `withTargetNetworkId(java.lang.String)` was removed
* `withTargetManagedDiskTags(java.util.List)` was removed
* `withTargetBootDiagnosticsStorageAccountId(java.lang.String)` was removed
* `withTestNetworkId(java.lang.String)` was removed
* `withLastAgentUpgradeErrorDetails(java.util.List)` was removed
* `withTargetVmName(java.lang.String)` was removed
* `withTargetVmTags(java.util.List)` was removed
* `withAgentUpgradeBlockingErrorDetails(java.util.List)` was removed
* `withTargetAvailabilitySetId(java.lang.String)` was removed
* `withSeedManagedDiskTags(java.util.List)` was removed
* `withTargetResourceGroupId(java.lang.String)` was removed

#### `models.InMageAzureV2ManagedDiskDetails` was modified

* `InMageAzureV2ManagedDiskDetails()` was changed to private access
* `withSeedManagedDiskId(java.lang.String)` was removed
* `withDiskId(java.lang.String)` was removed
* `withDiskEncryptionSetId(java.lang.String)` was removed
* `withReplicaDiskType(java.lang.String)` was removed
* `withTargetDiskName(java.lang.String)` was removed

#### `models.VMwareCbtEventDetails` was modified

* `VMwareCbtEventDetails()` was changed to private access

#### `models.ReplicationEligibilityResultsErrorInfo` was modified

* `ReplicationEligibilityResultsErrorInfo()` was changed to private access
* `withCode(java.lang.String)` was removed
* `withPossibleCauses(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed
* `withRecommendedAction(java.lang.String)` was removed

#### `models.AutomationRunbookTaskDetails` was modified

* `AutomationRunbookTaskDetails()` was changed to private access
* `withAccountName(java.lang.String)` was removed
* `withJobOutput(java.lang.String)` was removed
* `withSubscriptionId(java.lang.String)` was removed
* `withRunbookName(java.lang.String)` was removed
* `withIsPrimarySideScript(java.lang.Boolean)` was removed
* `withCloudServiceName(java.lang.String)` was removed
* `withJobId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withRunbookId(java.lang.String)` was removed

#### `models.InMageRcmFailbackMobilityAgentDetails` was modified

* `InMageRcmFailbackMobilityAgentDetails()` was changed to private access

#### `models.ClusterRecoveryPointProperties` was modified

* `ClusterRecoveryPointProperties()` was changed to private access
* `withRecoveryPointType(models.ClusterRecoveryPointType)` was removed
* `withProviderSpecificDetails(models.ClusterProviderSpecificRecoveryPointDetails)` was removed
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed

#### `models.OperationsDiscovery` was modified

* `java.lang.Object properties()` -> `com.azure.core.util.BinaryData properties()`

#### `models.FailoverJobDetails` was modified

* `FailoverJobDetails()` was changed to private access
* `withAffectedObjectDetails(java.util.Map)` was removed
* `withProtectedItemDetails(java.util.List)` was removed

#### `models.AzureVmDiskDetails` was modified

* `AzureVmDiskDetails()` was changed to private access
* `withDiskId(java.lang.String)` was removed
* `withTargetDiskName(java.lang.String)` was removed
* `withVhdName(java.lang.String)` was removed
* `withTargetDiskLocation(java.lang.String)` was removed
* `withDiskEncryptionSetId(java.lang.String)` was removed
* `withVhdType(java.lang.String)` was removed
* `withCustomTargetDiskName(java.lang.String)` was removed
* `withVhdId(java.lang.String)` was removed
* `withMaxSizeMB(java.lang.String)` was removed
* `withLunId(java.lang.String)` was removed

#### `models.InMageAzureV2RecoveryPointDetails` was modified

* `InMageAzureV2RecoveryPointDetails()` was changed to private access
* `withIsMultiVmSyncPoint(java.lang.String)` was removed

#### `models.AgentDiskDetails` was modified

* `AgentDiskDetails()` was changed to private access

#### `models.ScriptActionTaskDetails` was modified

* `ScriptActionTaskDetails()` was changed to private access
* `withName(java.lang.String)` was removed
* `withIsPrimarySideScript(java.lang.Boolean)` was removed
* `withOutput(java.lang.String)` was removed
* `withPath(java.lang.String)` was removed

#### `models.HyperVReplicaAzureEventDetails` was modified

* `HyperVReplicaAzureEventDetails()` was changed to private access
* `withFabricName(java.lang.String)` was removed
* `withContainerName(java.lang.String)` was removed
* `withRemoteContainerName(java.lang.String)` was removed

#### `models.ProcessServer` was modified

* `ProcessServer()` was changed to private access
* `withSystemLoad(java.lang.String)` was removed
* `withHealthErrors(java.util.List)` was removed
* `withSystemLoadStatus(java.lang.String)` was removed
* `withReplicationPairCount(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withVersionStatus(java.lang.String)` was removed
* `withAvailableSpaceInBytes(java.lang.Long)` was removed
* `withSpaceUsageStatus(java.lang.String)` was removed
* `withTotalSpaceInBytes(java.lang.Long)` was removed
* `withMemoryUsageStatus(java.lang.String)` was removed
* `withSslCertExpiryRemainingDays(java.lang.Integer)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withTotalMemoryInBytes(java.lang.Long)` was removed
* `withSslCertExpiryDate(java.time.OffsetDateTime)` was removed
* `withOsVersion(java.lang.String)` was removed
* `withCpuLoadStatus(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withAgentVersionDetails(models.VersionDetails)` was removed
* `withAvailableMemoryInBytes(java.lang.Long)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withCpuLoad(java.lang.String)` was removed
* `withMobilityServiceUpdates(java.util.List)` was removed
* `withAgentVersion(java.lang.String)` was removed
* `withAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withMachineCount(java.lang.String)` was removed
* `withPsServiceStatus(java.lang.String)` was removed
* `withHostId(java.lang.String)` was removed

#### `models.ProtectionContainerMappingProperties` was modified

* `ProtectionContainerMappingProperties()` was changed to private access
* `withSourceProtectionContainerFriendlyName(java.lang.String)` was removed
* `withTargetProtectionContainerFriendlyName(java.lang.String)` was removed
* `withPolicyFriendlyName(java.lang.String)` was removed
* `withSourceFabricFriendlyName(java.lang.String)` was removed
* `withPolicyId(java.lang.String)` was removed
* `withHealthErrorDetails(java.util.List)` was removed
* `withHealth(java.lang.String)` was removed
* `withProviderSpecificDetails(models.ProtectionContainerMappingProviderSpecificDetails)` was removed
* `withTargetProtectionContainerId(java.lang.String)` was removed
* `withState(java.lang.String)` was removed
* `withTargetFabricFriendlyName(java.lang.String)` was removed

#### `models.A2AFabricSpecificLocationDetails` was modified

* `A2AFabricSpecificLocationDetails()` was changed to private access
* `withInitialRecoveryFabricLocation(java.lang.String)` was removed
* `withInitialPrimaryFabricLocation(java.lang.String)` was removed
* `withPrimaryFabricLocation(java.lang.String)` was removed
* `withPrimaryExtendedLocation(models.ExtendedLocation)` was removed
* `withRecoveryZone(java.lang.String)` was removed
* `withInitialRecoveryZone(java.lang.String)` was removed
* `withInitialPrimaryExtendedLocation(models.ExtendedLocation)` was removed
* `withInitialRecoveryExtendedLocation(models.ExtendedLocation)` was removed
* `withRecoveryFabricLocation(java.lang.String)` was removed
* `withPrimaryZone(java.lang.String)` was removed
* `withRecoveryExtendedLocation(models.ExtendedLocation)` was removed
* `withInitialPrimaryZone(java.lang.String)` was removed

#### `models.DiskDetails` was modified

* `DiskDetails()` was changed to private access
* `withVhdName(java.lang.String)` was removed
* `withVhdId(java.lang.String)` was removed
* `withVhdType(java.lang.String)` was removed
* `withMaxSizeMB(java.lang.Long)` was removed

#### `models.InMageBasePolicyDetails` was modified

* `InMageBasePolicyDetails()` was changed to private access
* `withRecoveryPointHistory(java.lang.Integer)` was removed
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withRecoveryPointThresholdInMinutes(java.lang.Integer)` was removed
* `withMultiVmSyncStatus(java.lang.String)` was removed

#### `models.A2APolicyDetails` was modified

* `A2APolicyDetails()` was changed to private access
* `withRecoveryPointHistory(java.lang.Integer)` was removed
* `withMultiVmSyncStatus(java.lang.String)` was removed
* `withCrashConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withRecoveryPointThresholdInMinutes(java.lang.Integer)` was removed

#### `models.MigrationItemProperties` was modified

* `MigrationItemProperties()` was changed to private access
* `withProviderSpecificDetails(models.MigrationProviderSpecificSettings)` was removed

#### `models.RecoveryServicesProviderProperties` was modified

* `RecoveryServicesProviderProperties()` was changed to private access
* `withResourceAccessIdentityDetails(models.IdentityProviderDetails)` was removed
* `withDataPlaneAuthenticationIdentityDetails(models.IdentityProviderDetails)` was removed
* `withProviderVersionState(java.lang.String)` was removed
* `withAuthenticationIdentityDetails(models.IdentityProviderDetails)` was removed
* `withProviderVersionDetails(models.VersionDetails)` was removed
* `withDraIdentifier(java.lang.String)` was removed
* `withMachineName(java.lang.String)` was removed
* `withProtectedItemCount(java.lang.Integer)` was removed
* `withHealthErrorDetails(java.util.List)` was removed
* `withBiosId(java.lang.String)` was removed
* `withFabricFriendlyName(java.lang.String)` was removed
* `withAllowedScenarios(java.util.List)` was removed
* `withConnectionStatus(java.lang.String)` was removed
* `withProviderVersion(java.lang.String)` was removed
* `withFriendlyName(java.lang.String)` was removed
* `withMachineId(java.lang.String)` was removed
* `withServerVersion(java.lang.String)` was removed
* `withProviderVersionExpiryDate(java.time.OffsetDateTime)` was removed
* `withLastHeartBeat(java.time.OffsetDateTime)` was removed
* `withFabricType(java.lang.String)` was removed

#### `models.IdentityProviderDetails` was modified

* `IdentityProviderDetails()` was changed to private access
* `withApplicationId(java.lang.String)` was removed
* `withAudience(java.lang.String)` was removed
* `withObjectId(java.lang.String)` was removed
* `withTenantId(java.lang.String)` was removed
* `withAadAuthority(java.lang.String)` was removed

#### `models.RecoveryPlanA2ADetails` was modified

* `RecoveryPlanA2ADetails()` was changed to private access
* `withPrimaryZone(java.lang.String)` was removed
* `withRecoveryExtendedLocation(models.ExtendedLocation)` was removed
* `withPrimaryExtendedLocation(models.ExtendedLocation)` was removed
* `withRecoveryZone(java.lang.String)` was removed

#### `models.InMageRcmSyncDetails` was modified

* `InMageRcmSyncDetails()` was changed to private access

#### `models.DataStoreUtilizationDetails` was modified

* `DataStoreUtilizationDetails()` was changed to private access

#### `models.DraDetails` was modified

* `DraDetails()` was changed to private access

#### `models.VmmToAzureNetworkMappingSettings` was modified

* `VmmToAzureNetworkMappingSettings()` was changed to private access

#### `models.HyperVReplicaBaseEventDetails` was modified

* `HyperVReplicaBaseEventDetails()` was changed to private access
* `withRemoteFabricName(java.lang.String)` was removed
* `withRemoteContainerName(java.lang.String)` was removed
* `withContainerName(java.lang.String)` was removed
* `withFabricName(java.lang.String)` was removed

#### `models.GroupTaskDetails` was modified

* `models.GroupTaskDetails withChildTasks(java.util.List)` -> `models.GroupTaskDetails withChildTasks(java.util.List)`

#### `models.SupportedOSProperties` was modified

* `SupportedOSProperties()` was changed to private access
* `withSupportedOsList(java.util.List)` was removed

#### `models.HyperVReplica2012R2EventDetails` was modified

* `HyperVReplica2012R2EventDetails()` was changed to private access
* `withRemoteContainerName(java.lang.String)` was removed
* `withContainerName(java.lang.String)` was removed
* `withRemoteFabricName(java.lang.String)` was removed
* `withFabricName(java.lang.String)` was removed

#### `models.InMageRcmProtectionContainerMappingDetails` was modified

* `InMageRcmProtectionContainerMappingDetails()` was changed to private access

#### `models.ProcessServerDetails` was modified

* `ProcessServerDetails()` was changed to private access

#### `models.VMwareCbtNicDetails` was modified

* `VMwareCbtNicDetails()` was changed to private access
* `withTestIpAddressType(models.EthernetAddressType)` was removed
* `withTestIpAddress(java.lang.String)` was removed
* `withTestNetworkId(java.lang.String)` was removed
* `withTestSubnetName(java.lang.String)` was removed
* `withTargetSubnetName(java.lang.String)` was removed
* `withTargetIpAddressType(models.EthernetAddressType)` was removed
* `withTargetIpAddress(java.lang.String)` was removed
* `withIsPrimaryNic(java.lang.String)` was removed
* `withIsSelectedForMigration(java.lang.String)` was removed
* `withTargetNicName(java.lang.String)` was removed

#### `models.A2AZoneDetails` was modified

* `A2AZoneDetails()` was changed to private access
* `withSource(java.lang.String)` was removed
* `withTarget(java.lang.String)` was removed

#### `models.VaultHealthProperties` was modified

* `VaultHealthProperties()` was changed to private access
* `withFabricsHealth(models.ResourceHealthSummary)` was removed
* `withProtectedItemsHealth(models.ResourceHealthSummary)` was removed
* `withContainersHealth(models.ResourceHealthSummary)` was removed
* `withVaultErrors(java.util.List)` was removed

#### `models.VMwareV2FabricSpecificDetails` was modified

* `VMwareV2FabricSpecificDetails()` was changed to private access

#### `models.InconsistentVmDetails` was modified

* `InconsistentVmDetails()` was changed to private access
* `withErrorIds(java.util.List)` was removed
* `withDetails(java.util.List)` was removed
* `withVmName(java.lang.String)` was removed
* `withCloudName(java.lang.String)` was removed

#### `models.EncryptionDetails` was modified

* `EncryptionDetails()` was changed to private access
* `withKekCertExpiryDate(java.time.OffsetDateTime)` was removed
* `withKekState(java.lang.String)` was removed
* `withKekCertThumbprint(java.lang.String)` was removed

#### `models.InMagePolicyDetails` was modified

* `InMagePolicyDetails()` was changed to private access
* `withAppConsistentFrequencyInMinutes(java.lang.Integer)` was removed
* `withRecoveryPointThresholdInMinutes(java.lang.Integer)` was removed
* `withMultiVmSyncStatus(java.lang.String)` was removed
* `withRecoveryPointHistory(java.lang.Integer)` was removed

#### `models.ReplicationGroupDetails` was modified

* `ReplicationGroupDetails()` was changed to private access

#### `models.VMNicDetails` was modified

* `VMNicDetails()` was changed to private access
* `withReplicaNicId(java.lang.String)` was removed
* `withReuseExistingNic(java.lang.Boolean)` was removed
* `withSourceNicArmId(java.lang.String)` was removed
* `withTfoReuseExistingNic(java.lang.Boolean)` was removed
* `withTfoVMNetworkId(java.lang.String)` was removed
* `withTargetNicName(java.lang.String)` was removed
* `withVMNetworkName(java.lang.String)` was removed
* `withRecoveryVMNetworkId(java.lang.String)` was removed
* `withEnableAcceleratedNetworkingOnRecovery(java.lang.Boolean)` was removed
* `withEnableAcceleratedNetworkingOnTfo(java.lang.Boolean)` was removed
* `withRecoveryNicName(java.lang.String)` was removed
* `withRecoveryNetworkSecurityGroupId(java.lang.String)` was removed
* `withRecoveryNicResourceGroupName(java.lang.String)` was removed
* `withNicId(java.lang.String)` was removed
* `withIpConfigs(java.util.List)` was removed
* `withSelectionType(java.lang.String)` was removed
* `withTfoRecoveryNicResourceGroupName(java.lang.String)` was removed
* `withTfoNetworkSecurityGroupId(java.lang.String)` was removed
* `withTfoRecoveryNicName(java.lang.String)` was removed

#### `models.AlertProperties` was modified

* `AlertProperties()` was changed to private access
* `withCustomEmailAddresses(java.util.List)` was removed
* `withSendToOwners(java.lang.String)` was removed
* `withLocale(java.lang.String)` was removed

#### `models.ReplicationAgentDetails` was modified

* `ReplicationAgentDetails()` was changed to private access

#### `models.A2AProtectedDiskDetails` was modified

* `A2AProtectedDiskDetails()` was changed to private access
* `withRecoveryAzureStorageAccountId(java.lang.String)` was removed
* `withRecoveryDiskUri(java.lang.String)` was removed
* `withIsDiskEncrypted(java.lang.Boolean)` was removed
* `withDiskType(java.lang.String)` was removed
* `withFailoverDiskName(java.lang.String)` was removed
* `withMonitoringPercentageCompletion(java.lang.Integer)` was removed
* `withDiskName(java.lang.String)` was removed
* `withDiskCapacityInBytes(java.lang.Long)` was removed
* `withAllowedDiskLevelOperation(java.util.List)` was removed
* `withTfoDiskName(java.lang.String)` was removed
* `withSecretIdentifier(java.lang.String)` was removed
* `withIsDiskKeyEncrypted(java.lang.Boolean)` was removed
* `withKeyIdentifier(java.lang.String)` was removed
* `withPrimaryDiskAzureStorageAccountId(java.lang.String)` was removed
* `withDiskState(java.lang.String)` was removed
* `withDataPendingAtSourceAgentInMB(java.lang.Double)` was removed
* `withResyncRequired(java.lang.Boolean)` was removed
* `withDekKeyVaultArmId(java.lang.String)` was removed
* `withMonitoringJobType(java.lang.String)` was removed
* `withDiskUri(java.lang.String)` was removed
* `withKekKeyVaultArmId(java.lang.String)` was removed
* `withPrimaryStagingAzureStorageAccountId(java.lang.String)` was removed
* `withDataPendingInStagingStorageAccountInMB(java.lang.Double)` was removed

#### `models.SupportedOSDetails` was modified

* `SupportedOSDetails()` was changed to private access
* `withOsType(java.lang.String)` was removed
* `withOsName(java.lang.String)` was removed
* `withOsVersions(java.util.List)` was removed

#### `models.HyperVHostDetails` was modified

* `HyperVHostDetails()` was changed to private access

#### `models.InMageRcmFailbackProtectedDiskDetails` was modified

* `InMageRcmFailbackProtectedDiskDetails()` was changed to private access
* `withIrDetails(models.InMageRcmFailbackSyncDetails)` was removed
* `withResyncDetails(models.InMageRcmFailbackSyncDetails)` was removed

#### `models.VMwareCbtProtectionContainerMappingDetails` was modified

* `VMwareCbtProtectionContainerMappingDetails()` was changed to private access
* `withExcludedSkus(java.util.List)` was removed

#### `models.InMageAzureV2SwitchProviderDetails` was modified

* `InMageAzureV2SwitchProviderDetails()` was changed to private access

#### `models.MigrationRecoveryPointProperties` was modified

* `MigrationRecoveryPointProperties()` was changed to private access

#### `models.VmmVirtualMachineDetails` was modified

* `VmmVirtualMachineDetails()` was changed to private access
* `withDiskDetails(java.util.List)` was removed
* `withHasPhysicalDisk(models.PresenceStatus)` was removed
* `withSourceItemId(java.lang.String)` was removed
* `withHyperVHostId(java.lang.String)` was removed
* `withGeneration(java.lang.String)` was removed
* `withOsDetails(models.OSDetails)` was removed
* `withHasFibreChannelAdapter(models.PresenceStatus)` was removed
* `withHasSharedVhd(models.PresenceStatus)` was removed

#### `models.ComputeSizeErrorDetails` was modified

* `ComputeSizeErrorDetails()` was changed to private access
* `withSeverity(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.HyperVReplicaAzureManagedDiskDetails` was modified

* `HyperVReplicaAzureManagedDiskDetails()` was changed to private access
* `withDiskEncryptionSetId(java.lang.String)` was removed
* `withSectorSizeInBytes(java.lang.Integer)` was removed
* `withTargetDiskAccountType(models.DiskAccountType)` was removed
* `withSeedManagedDiskId(java.lang.String)` was removed
* `withDiskId(java.lang.String)` was removed
* `withReplicaDiskType(java.lang.String)` was removed

#### `models.InitialReplicationDetails` was modified

* `InitialReplicationDetails()` was changed to private access
* `withInitialReplicationProgressPercentage(java.lang.String)` was removed
* `withInitialReplicationType(java.lang.String)` was removed

#### `models.InputEndpoint` was modified

* `InputEndpoint()` was changed to private access
* `withEndpointName(java.lang.String)` was removed
* `withPrivatePort(java.lang.Integer)` was removed
* `withProtocol(java.lang.String)` was removed
* `withPublicPort(java.lang.Integer)` was removed

#### `models.JobErrorDetails` was modified

* `JobErrorDetails()` was changed to private access
* `withErrorLevel(java.lang.String)` was removed
* `withProviderErrorDetails(models.ProviderError)` was removed
* `withCreationTime(java.time.OffsetDateTime)` was removed
* `withTaskId(java.lang.String)` was removed
* `withServiceErrorDetails(models.ServiceError)` was removed

#### `models.CriticalJobHistoryDetails` was modified

* `CriticalJobHistoryDetails()` was changed to private access

#### `models.HyperVReplicaReplicationDetails` was modified

* `HyperVReplicaReplicationDetails()` was changed to private access
* `withVmId(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed
* `withInitialReplicationDetails(models.InitialReplicationDetails)` was removed
* `withVMDiskDetails(java.util.List)` was removed
* `withVmProtectionState(java.lang.String)` was removed
* `withLastReplicatedTime(java.time.OffsetDateTime)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed

#### `models.InMageRcmFailbackDiscoveredProtectedVmDetails` was modified

* `InMageRcmFailbackDiscoveredProtectedVmDetails()` was changed to private access

#### `models.InMageRcmUnProtectedDiskDetails` was modified

* `InMageRcmUnProtectedDiskDetails()` was changed to private access

#### `models.JobStatusEventDetails` was modified

* `JobStatusEventDetails()` was changed to private access
* `withJobStatus(java.lang.String)` was removed
* `withJobFriendlyName(java.lang.String)` was removed
* `withAffectedObjectType(java.lang.String)` was removed
* `withJobId(java.lang.String)` was removed

#### `models.RecoveryPointProperties` was modified

* `RecoveryPointProperties()` was changed to private access
* `withRecoveryPointTime(java.time.OffsetDateTime)` was removed
* `withProviderSpecificDetails(models.ProviderSpecificRecoveryPointDetails)` was removed
* `withRecoveryPointType(java.lang.String)` was removed

#### `models.Display` was modified

* `Display()` was changed to private access
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed

#### `models.HyperVSiteDetails` was modified

* `HyperVSiteDetails()` was changed to private access
* `withHyperVHosts(java.util.List)` was removed

#### `models.InMageRcmFabricSwitchProviderBlockingErrorDetails` was modified

* `InMageRcmFabricSwitchProviderBlockingErrorDetails()` was changed to private access

#### `models.PushInstallerDetails` was modified

* `PushInstallerDetails()` was changed to private access

#### `models.InlineWorkflowTaskDetails` was modified

* `InlineWorkflowTaskDetails()` was changed to private access
* `withChildTasks(java.util.List)` was removed
* `withWorkflowIds(java.util.List)` was removed

#### `models.VmmToVmmNetworkMappingSettings` was modified

* `VmmToVmmNetworkMappingSettings()` was changed to private access

#### `models.InMageRcmLastAgentUpgradeErrorDetails` was modified

* `InMageRcmLastAgentUpgradeErrorDetails()` was changed to private access

#### `models.HyperVReplica2012EventDetails` was modified

* `HyperVReplica2012EventDetails()` was changed to private access
* `withContainerName(java.lang.String)` was removed
* `withRemoteContainerName(java.lang.String)` was removed
* `withFabricName(java.lang.String)` was removed
* `withRemoteFabricName(java.lang.String)` was removed

#### `models.VaultSettingProperties` was modified

* `VaultSettingProperties()` was changed to private access
* `withMigrationSolutionId(java.lang.String)` was removed
* `withVmwareToAzureProviderType(java.lang.String)` was removed

#### `models.InMageRcmAgentUpgradeBlockingErrorDetails` was modified

* `InMageRcmAgentUpgradeBlockingErrorDetails()` was changed to private access

#### `models.RecoveryPlanShutdownGroupTaskDetails` was modified

* `RecoveryPlanShutdownGroupTaskDetails()` was changed to private access
* `withName(java.lang.String)` was removed
* `withChildTasks(java.util.List)` was removed
* `withGroupId(java.lang.String)` was removed
* `withRpGroupType(java.lang.String)` was removed

#### `models.ApplianceResourceDetails` was modified

* `ApplianceResourceDetails()` was changed to private access

#### `models.A2AEventDetails` was modified

* `A2AEventDetails()` was changed to private access
* `withRemoteFabricLocation(java.lang.String)` was removed
* `withRemoteFabricName(java.lang.String)` was removed
* `withFabricLocation(java.lang.String)` was removed
* `withFabricName(java.lang.String)` was removed
* `withProtectedItemName(java.lang.String)` was removed
* `withFabricObjectId(java.lang.String)` was removed

#### `models.ClusterTestFailoverJobDetails` was modified

* `ClusterTestFailoverJobDetails()` was changed to private access
* `withProtectedItemDetails(java.util.List)` was removed
* `withNetworkType(java.lang.String)` was removed
* `withAffectedObjectDetails(java.util.Map)` was removed
* `withNetworkName(java.lang.String)` was removed
* `withComments(java.lang.String)` was removed
* `withNetworkFriendlyName(java.lang.String)` was removed
* `withTestFailoverStatus(java.lang.String)` was removed

#### `models.InMageRcmFabricSpecificDetails` was modified

* `InMageRcmFabricSpecificDetails()` was changed to private access
* `withSourceAgentIdentityDetails(models.IdentityProviderDetails)` was removed

#### `models.MasterTargetServer` was modified

* `MasterTargetServer()` was changed to private access
* `withAgentVersion(java.lang.String)` was removed
* `withOsVersion(java.lang.String)` was removed
* `withMarsAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withDataStores(java.util.List)` was removed
* `withId(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed
* `withValidationErrors(java.util.List)` was removed
* `withVersionStatus(java.lang.String)` was removed
* `withRetentionVolumes(java.util.List)` was removed
* `withDiskCount(java.lang.Integer)` was removed
* `withAgentVersionDetails(models.VersionDetails)` was removed
* `withOsType(java.lang.String)` was removed
* `withLastHeartbeat(java.time.OffsetDateTime)` was removed
* `withName(java.lang.String)` was removed
* `withMarsAgentVersion(java.lang.String)` was removed
* `withHealthErrors(java.util.List)` was removed
* `withMarsAgentVersionDetails(models.VersionDetails)` was removed

#### `models.InMageRcmApplianceDetails` was modified

* `InMageRcmApplianceDetails()` was changed to private access

#### `models.InMageRcmFailbackNicDetails` was modified

* `InMageRcmFailbackNicDetails()` was changed to private access

#### `models.EventProperties` was modified

* `EventProperties()` was changed to private access
* `withTimeOfOccurrence(java.time.OffsetDateTime)` was removed
* `withEventCode(java.lang.String)` was removed
* `withEventSpecificDetails(models.EventSpecificDetails)` was removed
* `withFabricId(java.lang.String)` was removed
* `withProviderSpecificDetails(models.EventProviderSpecificDetails)` was removed
* `withEventType(java.lang.String)` was removed
* `withHealthErrors(java.util.List)` was removed
* `withDescription(java.lang.String)` was removed
* `withSeverity(java.lang.String)` was removed
* `withAffectedObjectCorrelationId(java.lang.String)` was removed
* `withAffectedObjectFriendlyName(java.lang.String)` was removed

#### `models.InMageAgentDetails` was modified

* `InMageAgentDetails()` was changed to private access
* `withPostUpdateRebootStatus(java.lang.String)` was removed
* `withAgentExpiryDate(java.time.OffsetDateTime)` was removed
* `withAgentUpdateStatus(java.lang.String)` was removed
* `withAgentVersion(java.lang.String)` was removed

#### `models.VmNicUpdatesTaskDetails` was modified

* `VmNicUpdatesTaskDetails()` was changed to private access
* `withVmId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withNicId(java.lang.String)` was removed

#### `models.ClusterFailoverJobDetails` was modified

* `ClusterFailoverJobDetails()` was changed to private access
* `withAffectedObjectDetails(java.util.Map)` was removed
* `withProtectedItemDetails(java.util.List)` was removed

#### `models.A2ACrossClusterMigrationReplicationDetails` was modified

* `A2ACrossClusterMigrationReplicationDetails()` was changed to private access
* `withFabricObjectId(java.lang.String)` was removed
* `withVmProtectionStateDescription(java.lang.String)` was removed
* `withLifecycleId(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withPrimaryFabricLocation(java.lang.String)` was removed
* `withVmProtectionState(java.lang.String)` was removed

#### `models.OSVersionWrapper` was modified

* `OSVersionWrapper()` was changed to private access
* `withVersion(java.lang.String)` was removed
* `withServicePack(java.lang.String)` was removed

#### `models.VMwareCbtMigrationDetails` was modified

* `VMwareCbtMigrationDetails()` was changed to private access
* `withLinuxLicenseType(models.LinuxLicenseType)` was removed
* `withTargetProximityPlacementGroupId(java.lang.String)` was removed
* `withTargetVmName(java.lang.String)` was removed
* `withConfidentialVmKeyVaultId(java.lang.String)` was removed
* `withTargetDiskTags(java.util.Map)` was removed
* `withSqlServerLicenseType(java.lang.String)` was removed
* `withTargetVmTags(java.util.Map)` was removed
* `withProtectedDisks(java.util.List)` was removed
* `withTargetBootDiagnosticsStorageAccountId(java.lang.String)` was removed
* `withSupportedOSVersions(java.util.List)` was removed
* `withTargetVmSecurityProfile(models.VMwareCbtSecurityProfileProperties)` was removed
* `withTargetAvailabilitySetId(java.lang.String)` was removed
* `withPerformAutoResync(java.lang.String)` was removed
* `withTargetAvailabilityZone(java.lang.String)` was removed
* `withTargetNetworkId(java.lang.String)` was removed
* `withTargetNicTags(java.util.Map)` was removed
* `withSeedDiskTags(java.util.Map)` was removed
* `withLicenseType(java.lang.String)` was removed
* `withTargetVmSize(java.lang.String)` was removed
* `withTargetResourceGroupId(java.lang.String)` was removed
* `withVmNics(java.util.List)` was removed
* `withTestNetworkId(java.lang.String)` was removed

### Features Added

* `models.AgentReinstallBlockedReason` was added

* `models.InMageRcmAgentReinstallBlockingErrorDetails` was added

* `models.ReinstallMobilityServiceRequest` was added

* `models.A2AAgentReinstallBlockingErrorDetails` was added

* `models.MobilityAgentReinstallType` was added

* `models.ReinstallMobilityServiceRequestProperties` was added

#### `models.RecoveryPoint` was modified

* `systemData()` was added

#### `models.InMageRcmDiskInput` was modified

* `throughputInMbps()` was added
* `diskSizeInGB()` was added
* `withThroughputInMbps(java.lang.Long)` was added
* `withIops(java.lang.Long)` was added
* `iops()` was added
* `withDiskSizeInGB(java.lang.Long)` was added

#### `models.MigrationRecoveryPoint` was modified

* `systemData()` was added

#### `models.VMwareCbtUpdateMigrationItemInput` was modified

* `targetCapacityReservationGroupId()` was added
* `withTargetCapacityReservationGroupId(java.lang.String)` was added

#### `models.NetworkMapping` was modified

* `systemData()` was added

#### `models.HyperVReplicaAzureReplicationDetails` was modified

* `targetCapacityReservationGroupId()` was added

#### `models.InMageRcmProtectedDiskDetails` was modified

* `iops()` was added
* `throughputInMbps()` was added
* `diskSizeInGB()` was added

#### `models.UpdateDiskInput` was modified

* `withDiskSizeInGB(java.lang.Long)` was added
* `iops()` was added
* `withIops(java.lang.Long)` was added
* `withThroughputInMbps(java.lang.Long)` was added
* `diskSizeInGB()` was added
* `throughputInMbps()` was added

#### `models.InMageRcmUpdateReplicationProtectedItemInput` was modified

* `withTargetCapacityReservationGroupId(java.lang.String)` was added
* `vmDisks()` was added
* `targetCapacityReservationGroupId()` was added
* `withVmDisks(java.util.List)` was added

#### `models.InMageRcmMobilityAgentDetails` was modified

* `isAgentUpgradeable()` was added
* `agentReinstallState()` was added
* `lastAgentReinstallType()` was added
* `distroName()` was added
* `reasonsBlockingReinstall()` was added
* `isAgentReinstallRequired()` was added
* `osFamilyName()` was added
* `agentReinstallJobId()` was added
* `agentReinstallAttemptToVersion()` was added
* `reasonsBlockingReinstallDetails()` was added
* `distroNameForWhichAgentIsInstalled()` was added
* `isLastReinstallSuccessful()` was added

#### `models.ProtectionContainer` was modified

* `systemData()` was added

#### `models.Alert` was modified

* `systemData()` was added

#### `models.ReplicationEligibilityResults` was modified

* `systemData()` was added

#### `models.A2AReplicationDetails` was modified

* `isAgentUpgradeInProgress()` was added
* `platformFaultDomain()` was added
* `autoAgentUpgradeRetryCount()` was added
* `distroName()` was added
* `distroNameForWhichAgentIsInstalled()` was added
* `isAgentReinstallRequired()` was added
* `reasonsBlockingReInstall()` was added
* `agentReinstallAttemptToVersion()` was added
* `isAgentUpgradeable()` was added
* `isAgentUpgradeRetryThresholdExhausted()` was added
* `reasonsBlockingReinstallDetails()` was added
* `osFamilyName()` was added

#### `models.RecoveryServicesProvider` was modified

* `systemData()` was added

#### `models.VMwareCbtDiskInput` was modified

* `withDiskSizeInGB(java.lang.Long)` was added
* `throughputInMbps()` was added
* `iops()` was added
* `diskSizeInGB()` was added
* `withIops(java.lang.Long)` was added
* `withThroughputInMbps(java.lang.Long)` was added

#### `models.VMwareCbtProtectedDiskDetails` was modified

* `iops()` was added
* `diskSizeInGB()` was added
* `throughputInMbps()` was added

#### `models.VMwareCbtEnableMigrationInput` was modified

* `withTargetCapacityReservationGroupId(java.lang.String)` was added
* `targetCapacityReservationGroupId()` was added

#### `models.VMwareCbtUpdateDiskInput` was modified

* `throughputInMbps()` was added
* `withThroughputInMbps(java.lang.Long)` was added
* `diskSizeInGB()` was added
* `iops()` was added
* `withDiskSizeInGB(java.lang.Long)` was added
* `withIops(java.lang.Long)` was added

#### `models.ProtectionContainerMapping` was modified

* `systemData()` was added

#### `models.VCenter` was modified

* `systemData()` was added

#### `models.Policy` was modified

* `systemData()` was added

#### `models.InMageRcmReplicationDetails` was modified

* `targetCapacityReservationGroupId()` was added

#### `models.InMageRcmDisksDefaultInput` was modified

* `diskSizeInGB()` was added
* `throughputInMbps()` was added
* `withIops(java.lang.Long)` was added
* `withDiskSizeInGB(java.lang.Long)` was added
* `withThroughputInMbps(java.lang.Long)` was added
* `iops()` was added

#### `models.VMwareCbtMigrateInput` was modified

* `targetCapacityReservationGroupId()` was added
* `withTargetCapacityReservationGroupId(java.lang.String)` was added

#### `models.A2ASwitchProtectionInput` was modified

* `platformFaultDomain()` was added
* `withPlatformFaultDomain(java.lang.Integer)` was added

#### `models.LogicalNetwork` was modified

* `systemData()` was added

#### `models.VaultSetting` was modified

* `systemData()` was added

#### `models.ReplicationProtectedItems` was modified

* `reinstallMobilityService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ReinstallMobilityServiceRequest,com.azure.core.util.Context)` was added
* `reinstallMobilityService(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ReinstallMobilityServiceRequest)` was added

#### `models.HyperVReplicaAzureEnableProtectionInput` was modified

* `targetCapacityReservationGroupId()` was added
* `withTargetCapacityReservationGroupId(java.lang.String)` was added

#### `models.SupportedOperatingSystems` was modified

* `systemData()` was added

#### `models.MigrationItem` was modified

* `systemData()` was added

#### `models.ReplicationProtectionCluster` was modified

* `systemData()` was added

#### `models.ProtectableItem` was modified

* `systemData()` was added

#### `models.Network` was modified

* `systemData()` was added

#### `models.A2AEnableProtectionInput` was modified

* `withPlatformFaultDomain(java.lang.Integer)` was added
* `platformFaultDomain()` was added

#### `models.A2AUpdateReplicationProtectedItemInput` was modified

* `withPlatformFaultDomain(java.lang.Integer)` was added
* `withRecoveryAvailabilityZone(java.lang.String)` was added
* `platformFaultDomain()` was added
* `recoveryAvailabilityZone()` was added

#### `models.HyperVReplicaAzureUpdateReplicationProtectedItemInput` was modified

* `withTargetCapacityReservationGroupId(java.lang.String)` was added
* `targetCapacityReservationGroupId()` was added

#### `models.HyperVReplicaAzureDiskInputDetails` was modified

* `throughputInMbps()` was added
* `withIops(java.lang.Long)` was added
* `withThroughputInMbps(java.lang.Long)` was added
* `iops()` was added
* `diskSizeInGB()` was added
* `withDiskSizeInGB(java.lang.Long)` was added

#### `models.Fabric` was modified

* `systemData()` was added

#### `models.HyperVReplicaAzureManagedDiskDetails` was modified

* `throughputInMbps()` was added
* `iops()` was added
* `diskSizeInGB()` was added

#### `models.Event` was modified

* `systemData()` was added

#### `models.AgentUpgradeBlockedReason` was modified

* `RE_INSTALL_REQUIRED` was added

#### `models.StorageClassification` was modified

* `systemData()` was added

#### `models.ReplicationProtectionIntent` was modified

* `systemData()` was added

#### `models.StorageClassificationMapping` was modified

* `systemData()` was added

#### `models.ReplicationProtectedItem` was modified

* `systemData()` was added
* `reinstallMobilityService(models.ReinstallMobilityServiceRequest,com.azure.core.util.Context)` was added
* `reinstallMobilityService(models.ReinstallMobilityServiceRequest)` was added

#### `models.InMageRcmEnableProtectionInput` was modified

* `withTargetCapacityReservationGroupId(java.lang.String)` was added
* `targetCapacityReservationGroupId()` was added

#### `models.InMageRcmUnplannedFailoverInput` was modified

* `withTargetCapacityReservationGroupId(java.lang.String)` was added
* `targetCapacityReservationGroupId()` was added

#### `models.VaultHealthDetails` was modified

* `systemData()` was added

#### `models.RecoveryPlan` was modified

* `systemData()` was added

#### `models.HyperVReplicaAzurePlannedFailoverProviderInput` was modified

* `targetCapacityReservationGroupId()` was added
* `withTargetCapacityReservationGroupId(java.lang.String)` was added

#### `models.VMwareCbtMigrationDetails` was modified

* `targetCapacityReservationGroupId()` was added

#### `models.Job` was modified

* `systemData()` was added

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

