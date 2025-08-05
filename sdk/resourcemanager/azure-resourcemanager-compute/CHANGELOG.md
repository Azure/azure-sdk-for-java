# Release History

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
