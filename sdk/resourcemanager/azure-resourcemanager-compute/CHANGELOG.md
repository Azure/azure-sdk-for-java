# Release History

## 2.8.0-beta.1 (Unreleased)

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
