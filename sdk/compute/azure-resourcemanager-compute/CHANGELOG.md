# Release History

## 2.57.0 (2026-04-21)

### Breaking Changes

#### `models.VirtualMachineScaleSetExtensionListResult` was removed

#### `models.VirtualMachineScaleSetVMListResult` was removed

#### `models.DiskEncryptionSetList` was removed

#### `models.VirtualMachineListResult` was removed

#### `models.SnapshotList` was removed

#### `models.GalleryList` was removed

#### `models.CapacityReservationGroupListResult` was removed

#### `models.RestorePointCollectionListResult` was removed

#### `models.SharedGalleryImageVersionList` was removed

#### `models.VirtualMachineScaleSetListResult` was removed

#### `models.CapacityReservationListResult` was removed

#### `models.GalleryScriptVersionList` was removed

#### `models.CommunityGalleryImageList` was removed

#### `models.SshPublicKeysGroupListResult` was removed

#### `models.GalleryImageList` was removed

#### `models.RunCommandListResult` was removed

#### `models.ListUsagesResult` was removed

#### `models.GalleryImageVersionList` was removed

#### `models.GalleryScriptList` was removed

#### `models.CommunityGalleryImageVersionList` was removed

#### `models.DedicatedHostListResult` was removed

#### `models.DedicatedHostGroupListResult` was removed

#### `models.OperationListResult` was removed

#### `models.GallerySoftDeletedResourceList` was removed

#### `models.VirtualMachineScaleSetListSkusResult` was removed

#### `models.GalleryApplicationVersionList` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.DedicatedHostSizeListResult` was removed

#### `models.VirtualMachineSizeListResult` was removed

#### `models.VirtualMachineRunCommandsListResult` was removed

#### `models.VirtualMachineScaleSetListWithLinkResult` was removed

#### `models.GalleryApplicationList` was removed

#### `models.AvailabilitySetListResult` was removed

#### `models.DiskRestorePointList` was removed

#### `models.GalleryInVMAccessControlProfileVersionList` was removed

#### `models.ImageListResult` was removed

#### `models.ResourceSkusResult` was removed

#### `models.GalleryInVMAccessControlProfileList` was removed

#### `models.DiskList` was removed

#### `models.VirtualMachineScaleSetListOSUpgradeHistory` was removed

#### `models.SharedGalleryImageList` was removed

#### `models.DiskAccessList` was removed

#### `models.ResourceUriList` was removed

#### `models.ProximityPlacementGroupListResult` was removed

#### `models.SharedGalleryList` was removed

#### `models.ReplicationStatus` was modified

* `ReplicationStatus()` was changed to private access

#### `models.AutomaticOSUpgradeProperties` was modified

* `AutomaticOSUpgradeProperties()` was changed to private access
* `withAutomaticOSUpgradeSupported(boolean)` was removed

#### `models.VirtualMachineRunCommandInstanceView` was modified

* `VirtualMachineRunCommandInstanceView()` was changed to private access
* `withExitCode(java.lang.Integer)` was removed
* `withExecutionMessage(java.lang.String)` was removed
* `withStatuses(java.util.List)` was removed
* `withError(java.lang.String)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `withOutput(java.lang.String)` was removed
* `withExecutionState(models.ExecutionState)` was removed
* `withEndTime(java.time.OffsetDateTime)` was removed

#### `models.SharingStatus` was modified

* `SharingStatus()` was changed to private access
* `withSummary(java.util.List)` was removed

#### `models.InnerError` was modified

* `InnerError()` was changed to private access
* `withExceptiontype(java.lang.String)` was removed
* `withErrordetail(java.lang.String)` was removed

#### `models.ResourceSkuRestrictions` was modified

* `ResourceSkuRestrictions()` was changed to private access

#### `models.CommunityGalleryMetadata` was modified

* `CommunityGalleryMetadata()` was changed to private access
* `withPrivacyStatementUri(java.lang.String)` was removed
* `withPublisherContact(java.lang.String)` was removed
* `withPublicNames(java.util.List)` was removed
* `withEula(java.lang.String)` was removed
* `withPublisherUri(java.lang.String)` was removed

#### `models.SharedGalleryDataDiskImage` was modified

* `SharedGalleryDataDiskImage()` was changed to private access
* `withHostCaching(models.SharedGalleryHostCaching)` was removed
* `withLun(int)` was removed

#### `models.VirtualMachineScaleSetInstanceViewStatusesSummary` was modified

* `VirtualMachineScaleSetInstanceViewStatusesSummary()` was changed to private access

#### `models.PlatformAttribute` was modified

* `PlatformAttribute()` was changed to private access

#### `models.ShareInfoElement` was modified

* `ShareInfoElement()` was changed to private access

#### `models.PolicyViolation` was modified

* `PolicyViolation()` was changed to private access
* `withDetails(java.lang.String)` was removed
* `withCategory(models.PolicyViolationCategory)` was removed

#### `models.ApiError` was modified

* `ApiError()` was changed to private access

#### `models.ResourceSkuCapacity` was modified

* `ResourceSkuCapacity()` was changed to private access

#### `models.DefaultVirtualMachineScaleSetInfo` was modified

* `DefaultVirtualMachineScaleSetInfo()` was changed to private access

#### `models.DiskRestorePointReplicationStatus` was modified

* `DiskRestorePointReplicationStatus()` was changed to private access
* `withCompletionPercent(java.lang.Integer)` was removed
* `withStatus(models.InstanceViewStatus)` was removed

#### `models.PatchInstallationDetail` was modified

* `PatchInstallationDetail()` was changed to private access

#### `models.UpgradeOperationHistoryStatus` was modified

* `UpgradeOperationHistoryStatus()` was changed to private access

#### `models.OSDiskImage` was modified

* `OSDiskImage()` was changed to private access
* `withOperatingSystem(models.OperatingSystemTypes)` was removed

#### `models.ResourceSkuCapabilities` was modified

* `ResourceSkuCapabilities()` was changed to private access

#### `models.OrchestrationServiceSummary` was modified

* `OrchestrationServiceSummary()` was changed to private access

#### `models.CapacityReservationGroupInstanceView` was modified

* `CapacityReservationGroupInstanceView()` was changed to private access

#### `models.DedicatedHostGroupInstanceView` was modified

* `DedicatedHostGroupInstanceView()` was changed to private access
* `withHosts(java.util.List)` was removed

#### `models.DedicatedHostInstanceView` was modified

* `models.DedicatedHostInstanceView withAvailableCapacity(models.DedicatedHostAvailableCapacity)` -> `models.DedicatedHostInstanceView withAvailableCapacity(models.DedicatedHostAvailableCapacity)`
* `models.DedicatedHostInstanceView withStatuses(java.util.List)` -> `models.DedicatedHostInstanceView withStatuses(java.util.List)`

#### `models.SharedGalleryImageVersionStorageProfile` was modified

* `SharedGalleryImageVersionStorageProfile()` was changed to private access
* `withOsDiskImage(models.SharedGalleryOSDiskImage)` was removed
* `withDataDiskImages(java.util.List)` was removed

#### `models.CapacityReservationInstanceViewWithName` was modified

* `CapacityReservationInstanceViewWithName()` was changed to private access
* `withStatuses(java.util.List)` was removed
* `withUtilizationInfo(models.CapacityReservationUtilization)` was removed

#### `models.RunCommandParameterDefinition` was modified

* `RunCommandParameterDefinition()` was changed to private access
* `withType(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withDefaultValue(java.lang.String)` was removed
* `withRequired(java.lang.Boolean)` was removed

#### `models.SharedGalleryDiskImage` was modified

* `models.SharedGalleryDiskImage withHostCaching(models.SharedGalleryHostCaching)` -> `models.SharedGalleryDiskImage withHostCaching(models.SharedGalleryHostCaching)`

#### `models.RegionalSharingStatus` was modified

* `RegionalSharingStatus()` was changed to private access
* `withDetails(java.lang.String)` was removed
* `withRegion(java.lang.String)` was removed

#### `models.VirtualMachineScaleSetSkuCapacity` was modified

* `VirtualMachineScaleSetSkuCapacity()` was changed to private access

#### `models.CommunityGalleryImageIdentifier` was modified

* `CommunityGalleryImageIdentifier()` was changed to private access
* `withPublisher(java.lang.String)` was removed
* `withOffer(java.lang.String)` was removed
* `withSku(java.lang.String)` was removed

#### `models.VirtualMachineHealthStatus` was modified

* `VirtualMachineHealthStatus()` was changed to private access

#### `models.ValidationsProfile` was modified

* `ValidationsProfile()` was changed to private access
* `withValidationEtag(java.lang.String)` was removed
* `withPlatformAttributes(java.util.List)` was removed
* `withExecutedValidations(java.util.List)` was removed

#### `models.DataDiskImage` was modified

* `DataDiskImage()` was changed to private access

#### `models.SharedGalleryOSDiskImage` was modified

* `SharedGalleryOSDiskImage()` was changed to private access
* `withHostCaching(models.SharedGalleryHostCaching)` was removed

#### `models.ResourceSkuZoneDetails` was modified

* `ResourceSkuZoneDetails()` was changed to private access

#### `models.UpgradeOperationHistoricalStatusInfoProperties` was modified

* `UpgradeOperationHistoricalStatusInfoProperties()` was changed to private access

#### `models.ExecutedValidation` was modified

* `ExecutedValidation()` was changed to private access
* `withType(java.lang.String)` was removed
* `withExecutionTime(java.time.OffsetDateTime)` was removed
* `withVersion(java.lang.String)` was removed

#### `models.CapacityReservationInstanceView` was modified

* `models.CapacityReservationInstanceView withUtilizationInfo(models.CapacityReservationUtilization)` -> `models.CapacityReservationInstanceView withUtilizationInfo(models.CapacityReservationUtilization)`
* `models.CapacityReservationInstanceView withStatuses(java.util.List)` -> `models.CapacityReservationInstanceView withStatuses(java.util.List)`

#### `models.PropertyUpdatesInProgress` was modified

* `PropertyUpdatesInProgress()` was changed to private access
* `withTargetTier(java.lang.String)` was removed

#### `models.RollingUpgradeRunningStatus` was modified

* `RollingUpgradeRunningStatus()` was changed to private access

#### `models.ImageDeprecationStatus` was modified

* `ImageDeprecationStatus()` was changed to private access
* `withAlternativeOption(models.AlternativeOption)` was removed
* `withImageState(models.ImageState)` was removed
* `withScheduledDeprecationTime(java.time.OffsetDateTime)` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access

#### `models.VirtualMachineScaleSetMigrationInfo` was modified

* `VirtualMachineScaleSetMigrationInfo()` was changed to private access

#### `models.PrivateLinkResource` was modified

* `PrivateLinkResource()` was changed to private access
* `withRequiredZoneNames(java.util.List)` was removed

#### `models.VirtualMachineScaleSetVMExtensionsSummary` was modified

* `VirtualMachineScaleSetVMExtensionsSummary()` was changed to private access

#### `models.RollingUpgradeProgressInfo` was modified

* `RollingUpgradeProgressInfo()` was changed to private access

#### `models.LogAnalyticsOutput` was modified

* `LogAnalyticsOutput()` was changed to private access

#### `models.ResourceSkuCosts` was modified

* `ResourceSkuCosts()` was changed to private access

#### `models.RestorePointInstanceView` was modified

* `RestorePointInstanceView()` was changed to private access
* `withStatuses(java.util.List)` was removed
* `withDiskRestorePoints(java.util.List)` was removed

#### `models.VirtualMachineSoftwarePatchProperties` was modified

* `VirtualMachineSoftwarePatchProperties()` was changed to private access

#### `models.DedicatedHostAvailableCapacity` was modified

* `DedicatedHostAvailableCapacity()` was changed to private access
* `withAllocatableVMs(java.util.List)` was removed

#### `models.VirtualMachineImageFeature` was modified

* `VirtualMachineImageFeature()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.RegionalReplicationStatus` was modified

* `RegionalReplicationStatus()` was changed to private access

#### `models.CapacityReservationUtilization` was modified

* `CapacityReservationUtilization()` was changed to private access

#### `models.SubResourceWithColocationStatus` was modified

* `SubResourceWithColocationStatus()` was changed to private access
* `withId(java.lang.String)` was removed
* `withColocationStatus(models.InstanceViewStatus)` was removed

#### `models.DisallowedConfiguration` was modified

* `DisallowedConfiguration()` was changed to private access
* `withVmDiskType(models.VmDiskTypes)` was removed

#### `models.RollbackStatusInfo` was modified

* `RollbackStatusInfo()` was changed to private access

#### `models.PirCommunityGalleryResource` was modified

* `withUniqueId(java.lang.String)` was removed

#### `models.AlternativeOption` was modified

* `AlternativeOption()` was changed to private access
* `withType(models.AlternativeType)` was removed
* `withValue(java.lang.String)` was removed

#### `models.VirtualMachineStatusCodeCount` was modified

* `VirtualMachineStatusCodeCount()` was changed to private access

#### `models.DedicatedHostInstanceViewWithName` was modified

* `DedicatedHostInstanceViewWithName()` was changed to private access
* `withAvailableCapacity(models.DedicatedHostAvailableCapacity)` was removed
* `withStatuses(java.util.List)` was removed

#### `models.UsageName` was modified

* `UsageName()` was changed to private access
* `withValue(java.lang.String)` was removed
* `withLocalizedValue(java.lang.String)` was removed

#### `models.DedicatedHostAllocatableVM` was modified

* `DedicatedHostAllocatableVM()` was changed to private access
* `withVmSize(java.lang.String)` was removed
* `withCount(java.lang.Double)` was removed

#### `models.DiskRestorePointInstanceView` was modified

* `DiskRestorePointInstanceView()` was changed to private access
* `withId(java.lang.String)` was removed
* `withReplicationStatus(models.DiskRestorePointReplicationStatus)` was removed
* `withSnapshotAccessState(models.SnapshotAccessState)` was removed

#### `models.ResourceSkuRestrictionInfo` was modified

* `ResourceSkuRestrictionInfo()` was changed to private access

#### `models.ResourceSkuLocationInfo` was modified

* `ResourceSkuLocationInfo()` was changed to private access

#### `models.PrivateEndpoint` was modified

* `PrivateEndpoint()` was changed to private access

#### `models.PirSharedGalleryResource` was modified

* `withUniqueId(java.lang.String)` was removed

### Features Added

* `models.VMScaleSetLifecycleHookEventTargetResource` was added

* `models.StartRecoveryPolicy` was added

* `models.VMScaleSetLifecycleHookEventAdditionalContext` was added

* `models.RestartRecoveryPolicy` was added

* `models.ReimageRecoveryPolicy` was added

* `models.LifecycleHooksProfile` was added

* `models.StorageFaultDomainAlignmentType` was added

* `models.OperationRecoverySettings` was added

* `models.VMScaleSetLifecycleHookEventState` was added

* `models.LifecycleHookActionState` was added

* `models.VMScaleSetLifecycleHookEventType` was added

* `models.LifecycleHookAction` was added

* `models.VMScaleSetLifecycleHookEventUpdate` was added

* `models.ZoneMovement` was added

* `models.StorageAlignmentStatus` was added

* `models.ExternalHealthPolicy` was added

* `models.LifecycleHook` was added

* `models.ResiliencyProfile` was added

#### `models.SharedGalleryDataDiskImage` was modified

* `hostCaching()` was added

#### `models.ZonalPlatformFaultDomainAlignMode` was modified

* `BEST_EFFORT_ALIGNED` was added

#### `models.VirtualMachineScaleSetDataDisk` was modified

* `storageFaultDomainAlignment()` was added
* `withStorageFaultDomainAlignment(models.StorageFaultDomainAlignmentType)` was added

#### `models.VirtualMachineScaleSetOSDisk` was modified

* `withStorageFaultDomainAlignment(models.StorageFaultDomainAlignmentType)` was added
* `storageFaultDomainAlignment()` was added

#### `models.DiffDiskSettings` was modified

* `withEnableFullCaching(java.lang.Boolean)` was added
* `enableFullCaching()` was added

#### `models.CapacityReservationInstanceViewWithName` was modified

* `utilizationInfo()` was added
* `statuses()` was added

#### `models.OSDisk` was modified

* `storageFaultDomainAlignment()` was added
* `withStorageFaultDomainAlignment(models.StorageFaultDomainAlignmentType)` was added

#### `models.VirtualMachineScaleSetUpdateOSDisk` was modified

* `storageFaultDomainAlignment()` was added
* `withStorageFaultDomainAlignment(models.StorageFaultDomainAlignmentType)` was added

#### `models.VirtualMachineScaleSetUpdate` was modified

* `placement()` was added
* `lifecycleHooksProfile()` was added
* `withLifecycleHooksProfile(models.LifecycleHooksProfile)` was added
* `withPlacement(models.Placement)` was added

#### `models.SharedGalleryOSDiskImage` was modified

* `hostCaching()` was added

#### `models.SecurityTypes` was modified

* `STANDARD` was added

#### `models.ResiliencyPolicy` was modified

* `operationRecoverySettings()` was added
* `withOperationRecoverySettings(models.OperationRecoverySettings)` was added

#### `ComputeManager` was modified

* `cloudServiceClient()` was added

#### `models.DiskInstanceView` was modified

* `storageAlignmentStatus()` was added
* `withStorageAlignmentStatus(models.StorageAlignmentStatus)` was added

#### `models.DedicatedHostInstanceViewWithName` was modified

* `statuses()` was added
* `availableCapacity()` was added

#### `models.DataDisk` was modified

* `storageFaultDomainAlignment()` was added
* `withStorageFaultDomainAlignment(models.StorageFaultDomainAlignmentType)` was added

## 2.56.3 (2026-03-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-resourcemanager-authorization` from `2.53.7` to version `2.53.8`.
- Upgraded `azure-resourcemanager-storage` from `2.55.3` to version `2.55.4`.
- Upgraded `azure-resourcemanager-msi` from `2.53.6` to version `2.53.7`.
- Upgraded `azure-resourcemanager-network` from `2.58.0` to version `2.58.1`.
- Upgraded `azure-resourcemanager-resources` from `2.53.6` to version `2.54.0`.


## 2.56.2 (2026-02-26)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.56.1 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.56.0 (2026-01-12)

### Other Changes

#### Dependency Updates

- Updated `GalleryRP api-version` to `2025-03-03`.

## 2.55.1 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.55.0 (2025-11-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.1 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0 (2025-09-30)

### Other Changes

- Deprecated `KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER`. 
  Refer [New options for Windows Server 2012/R2 end of support from Azure](https://www.microsoft.com/windows-server/blog/2023/07/18/new-options-for-windows-server-2012-r2-end-of-support-from-azure).

#### Dependency Updates

- Updated `api-version` to `2025-04-01`.

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

### Breaking Changes

- `STANDARD_SSD_LRS` is now moved to `EdgeZoneStorageAccountType` from `StorageAccountType`.
- Changed `VirtualMachineScaleSetStorageProfile`'s `diskControllerType` property type from `String` to `DiskControllerTypes`.

### Other Changes

#### Dependency Updates

- Updated `api-version` of DiskRP to `2025-01-02`.

## 2.52.0 (2025-06-27)

### Features Added

- Supported `beginCreate(Context)` in `VirtualMachine`.
- Supported `beginDeleteByResourceGroup(Context)` and `beginDeleteById(Context)` in `VirtualMachines`.
- Supported `beginCreate` in `Snapshot`.
- Supported `beginDeleteByResourceGroup` and `beginDeleteById` in `Snapshots`.
- Supported `beginCreate(Context)` in `Disk`.
- Supported `beginDeleteByResourceGroup(Context)` and `beginDeleteById(Context)` in `Disks`.
- Supported `getPrimaryNetworkInterface(Context)` in `VirtualMachine`.

## 2.51.0 (2025-05-26)

### Features Added

- Supported setting disk IOPS and throughput for `Disk`.
- Supported setting maximum shares for `Disk`.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated `api-version` of ComputeRP to `2024-11-01`.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated `api-version` of CloudserviceRP to `2024-11-04`.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated `api-version` of GalleryRP to `2024-03-03`.

## 2.45.0 (2024-11-28)

### Features Added

- Supported `capacityReservationGroupId` and `withCapacityReservationGroup` methods for `VirtualMachine`.
- Supported enabling write accelerator for OS disk and data disks in `VirtualMachine` class.

## 2.44.0 (2024-10-25)

### Features Added

- Supported creating `GalleryImageVersion` from source virtual machine by specifying `withSourceVirtualMachine`.

### Bugs Fixed

- Fixed a bug that VM state not refreshed after calling `VirtualMachine.deallocate`.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0 (2024-08-23)

### Features Added

- Supported enabling ultra SSD in `VirtualMachine` class.

### Bugs Fixed

- Fixed a bug that `ClassCastException` occurs for some Virtual Machines with extensions.

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

#### Dependency Updates

- Updated `api-version` of ComputeRP to `2024-07-01`.

## 2.41.0 (2024-07-25)

### Breaking Changes

- Changed type from `List<VirtualMachineExtensionInner>` to `List<String>` of `excludeExtensions` property in `SecurityPostureReference` class.

### Other Changes

#### Dependency Updates

- Updated `api-version` of DiskRP to `2024-03-02`.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Features Added

- Supported disabling public network access in `Disk` via `disablePublicNetworkAccess()`, for private link feature.
- Supported disabling public network access in `Snapshot` via `disablePublicNetworkAccess()`, for private link feature.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated `api-version` of ComputeRP to `2024-03-01`.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Features Added

- Supported `withNetworkInterfacesDeleteOptions(DeleteOptions)` and `withDataDisksDeleteOptions(DeleteOptions)` for `VirtualMachine`.

### Other Changes

#### Dependency Updates

- Updated `api-version` of GalleryRP to `2023-07-03`.

## 2.35.0 (2024-01-26)

### Features Added

- Supported `WINDOWS_DESKTOP_10_PRO` in `KnownWindowsVirtualMachineImage`.
- Added `withEncryptionAtHost` in `VirtualMachine`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-10-02`.

## 2.34.0 (2023-12-22)

### Features Added

- Supported `userData` for `VirtualMachine` in create and update.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-09-01`.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.31.0 (2023-09-28)

### Features Added

- Supported `listByVirtualMachineScaleSet` for `VirtualMachines`.

### Bugs Fixed

- Fixed a bug that `VirtualMachineCustomImages.getById()` returns `HyperVGenerationTypes.V1` for all instances. ([#36619](https://github.com/Azure/azure-sdk-for-java/issues/36619))

## 2.30.0 (2023-08-25)

### Features Added

- Supported updating delete options for OS disk, data disks and network interfaces attached to `VirtualMachine`.

### Bugs Fixed

- Fixed bug that create proximity placement group with `VirtualMachineScaleSet` fails. ([#36417](https://github.com/Azure/azure-sdk-for-java/issues/36417))

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-07-01`.

## 2.29.0 (2023-07-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.28.0 (2023-06-25)

### Features Added

- Supported `withLogicalSectorSizeInBytes` in `Disk`.
- Supported `PREMIUM_V2_LRS` in `DiskSkuTypes`.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Features Added

- Supported `timeCreated()` in `VirtualMachineScaleSetVM` and `VirtualMachine`.

### Breaking Changes

- Changed type from `ApiEntityReference` to `DiskRestorePointAttributes` of `diskRestorePoint` property in `RestorePointSourceVMDataDisk` and `RestorePointSourceVmosDisk` class.
- Removed some setters in `RestorePointSourceMetadata`, `RestorePointSourceVmosDisk`, `RestorePointSourceVmosDisk` class. It should not affect customer, as these properties are read-only.
- Removed `id` property from classes. It should not affect customer, as these properties does not exist in runtime.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-03-01`.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Bugs Fixed

- Fixed wrong javadocs of `withSsh()` in `VirtualMachine` and `VirtualMachineScaleSet`.
- Fixed a bug that scaling up scale sets results in outdated models for existing VMs.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-11-01`.

## 2.22.0 (2022-12-23)

### Features Added

- Supported `withHibernationSupport` method in `Disk` to support hibernation for the OS on the disk.

## 2.21.0 (2022-11-24)

### Features Added

- Supported `withCopyStart` method in `Snapshot` for copying incremental snapshot from incremental snapshot.
- Supported `awaitCopyStartCompletion` and `awaitCopyStartCompletionAsync` method in `Snapshot`.
- Supported `copyCompletionPercent` and `copyCompletionError` method in `Snapshot` for retrieving `CopyStart` progress.
- Supported `withTrustedLaunch`, `withSecureBoot` and `withVTpm` methods in `VirtualMachine`.
- Supported `UBUNTU_SERVER_18_04_LTS_GEN2`, `UBUNTU_SERVER_20_04_LTS` and `UBUNTU_SERVER_20_04_LTS_GEN2` in `KnownLinuxVirtualMachineImage`.
- Supported `WINDOWS_SERVER_2019_DATACENTER_GEN2`, `WINDOWS_SERVER_2019_DATACENTER_WITH_CONTAINERS_GEN2`, 
  `WINDOWS_SERVER_2016_DATACENTER_GEN2` and `WINDOWS_DESKTOP_10_21H2_PRO_GEN2` in `KnownWindowsVirtualMachineImage`.
- Supported `withTrustedLaunch` and `withHyperVGeneration` methods in `GalleryImage`.

### Breaking Changes

- Property `uri` removed from class `GalleryArtifactVersionSource`. This property was non-functional.
- Type of property `GalleryImageVersionStorageProfile.source` changed to the class `GalleryArtifactVersionFullSource`, a subclass of `GalleryArtifactVersionSource`.
- Type of property `GalleryOSDiskImage.source` changed to the class `GalleryDiskImageSource`, a subclass of `GalleryArtifactVersionSource`.

### Other Changes

#### Dependency Updates

- Updated Gallery to `2022-03-03`.

## 2.20.0 (2022-10-26)

### Bugs Fixed

- Fixed bug where `getInstanceView()` in `VirtualMachineExtension` throws NPE if the VM is in deallocated state.

## 2.19.0 (2022-09-23)

### Breaking Changes

- Property `protectedSettingsFromKeyVault` in `VirtualMachineExtensionUpdate` changed from `Object` to `KeyVaultSecretReference`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-08-01`.

## 2.18.0 (2022-08-26)

### Bugs Fixed

- Fixed bug where `withDataFromDisk(Disk managedDisk)` in `Snapshot` mistakenly used SKU from Disk. ([#29811](https://github.com/Azure/azure-sdk-for-java/issues/29811))

## 2.17.0 (2022-07-25)

### Breaking Changes

- Property `protectedSettings` in `CloudServiceExtensionProperties` changed from `String` to `Object`.
- Property `settings` in `CloudServiceExtensionProperties` changed from `String` to `Object`.
- Property `communityGalleryInfo` in `SharingProfile` changed from `Object` to `CommunityGalleryInfo`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-04-04`.

## 2.16.0 (2022-06-24)

### Features Added

- Supported swapping OS disk in `VirtualMachine`.

### Breaking Changes

- Provisioning state in gallery changed to `GalleryProvisioningState`.
- User assigned identity in virtual machine scale set changed to `VirtualMachineIdentityUserAssignedIdentities`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-03-02`.

## 2.15.0 (2022-05-25)

### Features Added

- Supported `DiskEncryptionSet` for disk encryption set.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-03-01`.

## 2.14.0 (2022-04-11)

### Features Added

- Supported `virtualMachineSizeType()` in `VirtualMachineSize` as a typed alias of `name`.
- Supported ephemeral OS disk in `VirtualMachineScaleSet`.

## 2.13.0 (2022-03-11)

### Features Added

- Supported disk encryption set in `Disk` and `VirtualMachine`.
- Changed to use PATCH for `GalleryImage` update.
- Supported ephemeral OS disk in `VirtualMachine`.
- Supported creating `VirtualMachine` with existing `VirtualMachineScaleSet`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-12-01`.

## 2.12.0 (2022-02-14)

### Features Added

- Supported option of `filter` and `expand` for list instances of virtual machines by `VirtualMachineScaleSetVMs.list`.
- Changed to include the instance view of the virtual machine, when getting the virtual machine by `VirtualMachineScaleSetVMs.getInstance`.
- Supported batch deallocate, powerOff, start, restart, redeploy for `VirtualMachineScaleSetVMs`.
- Supported deep deletion on virtual machine via `withPrimaryNetworkInterfaceDeleteOptions`, `withOSDiskDeleteOptions`, `withDataDiskDefaultDeleteOptions` in `VirtualMachine` during create.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-11-01`.

## 2.11.0 (2022-01-17)

### Features Added

- Supported Flexible orchestration mode for `VirtualMachineScaleSet` during create.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-08-01`.

## 2.10.0 (2021-11-22)

### Features Added

- Supported hibernation for `VirtualMachine` (preview).
- Supported `withPlan()` for `VirtualMachineScaleSet` during create.
- Supported `getNetworkInterfaceByInstanceIdAsync()` in `VirtualMachineScaleSet`.
- Supported `getNetworkInterfaceAsync()` in `VirtualMachineScaleSetVM`.
- Supported `orchestrationMode()` in `VirtualMachineScaleSet`.

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated `api-version` to `2021-07-01`.

### Breaking Changes

- Removed unused class `ManagedArtifact`.

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated `api-version` to `2021-04-01`.

### Breaking Changes

- Property `publicIpAddressSkuName` in `PublicIpAddressSku` is renamed to `name`.
- Property `publicIpAddressSkuTier` in `PublicIpAddressSku` is renamed to `tier`.
- Enum `SecurityTypes` changed to subclass of `ExpandableStringEnum`.

## 2.6.0 (2021-06-18)

- Supported boot diagnostics with managed storage account.

## 2.5.0 (2021-05-28)
- Refreshed `api-version` `2021-03-01`

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated `api-version` to `2021-03-01`
- Corrected class type for `EncryptionImages`, `GalleryImageVersionStorageProfile`, `GalleryImageVersionUpdate`, `ImageDataDisk`, `ManagedDiskParameters`, `VirtualMachineScaleSetManagedDiskParameters`

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2020-12-01`
- Supported force deletion on virtual machines and virtual machine scale sets
- Removed container service as it is deprecated in compute, please use `KubernetesCluster` from `azure-resourcemanager-containerservice`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Updated core dependency from resources

## 2.0.0-beta.4 (2020-09-02)

- Supported beginCreate/beginDelete for VirtualMachine and Disk
- Updated `runPowerShellScript` parameters in VirtualMachine
