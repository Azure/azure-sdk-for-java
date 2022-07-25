# Release History

## 2.17.0-beta.1 (Unreleased)

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
