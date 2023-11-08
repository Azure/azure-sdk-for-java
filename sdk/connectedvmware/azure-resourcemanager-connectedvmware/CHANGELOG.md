# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2023-10-26)

- Azure Resource Manager ConnectedVMware client library for Java. This package contains Microsoft Azure SDK for ConnectedVMware Management SDK. Connected VMware Client. Package tag package-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.WindowsParameters` was removed

* `models.MachineExtension$UpdateStages` was removed

* `models.HybridIdentityMetadata$DefinitionStages` was removed

* `models.VMGuestPatchRebootStatus` was removed

* `models.VirtualMachineInstallPatchesParameters` was removed

* `models.GuestAgent$DefinitionStages` was removed

* `models.OsProfile` was removed

* `models.VirtualMachineInstallPatchesResult` was removed

* `models.HybridIdentityMetadata` was removed

* `models.VirtualMachine` was removed

* `models.VirtualMachineAssessPatchesResult` was removed

* `models.OsProfileUpdate` was removed

* `models.GuestAgent$Definition` was removed

* `models.HybridIdentityMetadata$Definition` was removed

* `models.Identity` was removed

* `models.VirtualMachines` was removed

* `models.OsTypeUM` was removed

* `models.VirtualMachine$DefinitionStages` was removed

* `models.StatusTypes` was removed

* `models.HybridIdentityMetadataList` was removed

* `models.OsProfileUpdateWindowsConfiguration` was removed

* `models.VirtualMachinesList` was removed

* `models.VMGuestPatchRebootSetting` was removed

* `models.VirtualMachine$Update` was removed

* `models.MachineExtensionInstanceView` was removed

* `models.MachineExtension` was removed

* `models.PatchOperationStatus` was removed

* `models.HybridIdentityMetadatas` was removed

* `models.MachineExtension$Definition` was removed

* `models.GuestAgents` was removed

* `models.OsProfileLinuxConfiguration` was removed

* `models.VMGuestPatchClassificationLinux` was removed

* `models.MachineExtensions` was removed

* `models.PatchOperationStartedBy` was removed

* `models.VirtualMachineUpdate` was removed

* `models.VirtualMachine$UpdateStages` was removed

* `models.MachineExtensionsListResult` was removed

* `models.GuestAgentProfile` was removed

* `models.MachineExtensionInstanceViewStatus` was removed

* `models.MachineExtension$Update` was removed

* `models.PatchServiceUsed` was removed

* `models.StatusLevelTypes` was removed

* `models.MachineExtension$DefinitionStages` was removed

* `models.VMGuestPatchClassificationWindows` was removed

* `models.VirtualMachine$Definition` was removed

* `models.OsProfileWindowsConfiguration` was removed

* `models.LinuxParameters` was removed

* `models.OsProfileUpdateLinuxConfiguration` was removed

* `models.ErrorDetail` was removed

* `models.MachineExtensionUpdate` was removed

* `models.IdentityType` was removed

* `models.MachineExtensionPropertiesInstanceView` was removed

* `models.AvailablePatchCountByClassification` was removed

#### `models.InventoryItem$DefinitionStages` was modified

* Stage 2 was added

#### `models.Hosts` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.Clusters` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.InventoryItem` was modified

* `provisioningState()` was removed
* `moName()` was removed
* `moRefId()` was removed
* `managedResourceId()` was removed

#### `models.Cluster` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.DatastoreInventoryItem` was modified

* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`

#### `models.Datastores` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.VirtualMachineTemplateInventoryItem` was modified

* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`
* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`

#### `models.ResourcePool` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.VCenters` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.VirtualMachineInventoryItem` was modified

* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`

#### `models.VirtualMachineTemplate` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.VirtualMachineTemplates` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.ResourcePools` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.HostModel` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.GuestAgent` was modified

* `refresh()` was removed
* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`
* `refresh(com.azure.core.util.Context)` was removed

#### `models.VirtualNetworks` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.HostInventoryItem` was modified

* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`
* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`

#### `models.VirtualNetworkInventoryItem` was modified

* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`
* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`

#### `models.ResourcePoolInventoryItem` was modified

* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`

#### `ConnectedVMwareManager` was modified

* `hybridIdentityMetadatas()` was removed
* `machineExtensions()` was removed
* `virtualMachines()` was removed
* `guestAgents()` was removed

#### `models.ClusterInventoryItem` was modified

* `fluent.models.InventoryItemProperties withMoRefId(java.lang.String)` -> `models.InventoryItemProperties withMoRefId(java.lang.String)`
* `fluent.models.InventoryItemProperties withMoName(java.lang.String)` -> `models.InventoryItemProperties withMoName(java.lang.String)`
* `fluent.models.InventoryItemProperties withManagedResourceId(java.lang.String)` -> `models.InventoryItemProperties withManagedResourceId(java.lang.String)`

#### `models.VirtualNetwork` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.VCenter` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.InventoryItem$Definition` was modified

* `withMoRefId(java.lang.String)` was removed
* `withManagedResourceId(java.lang.String)` was removed
* `withMoName(java.lang.String)` was removed

### Features Added

* `models.VmInstanceHybridIdentityMetadatas` was added

* `models.VmInstanceHybridIdentityMetadata` was added

* `models.VirtualMachineInstance` was added

* `models.VMInstanceGuestAgents` was added

* `models.InventoryItemProperties` was added

* `models.VirtualMachineInstanceUpdate` was added

* `models.VmInstanceHybridIdentityMetadataList` was added

* `models.InfrastructureProfile` was added

* `models.OsProfileForVMInstance` was added

* `models.VirtualMachineInstancesList` was added

* `models.VirtualMachineInstances` was added

#### `models.InventoryItem` was modified

* `properties()` was added

#### `models.Cluster` was modified

* `totalCpuMHz()` was added
* `totalMemoryGB()` was added
* `usedMemoryGB()` was added
* `usedCpuMHz()` was added

#### `models.VirtualMachineTemplateInventoryItem` was modified

* `toolsVersionStatus()` was added
* `toolsVersion()` was added

#### `models.ResourcePool` was modified

* `cpuOverallUsageMHz()` was added
* `memCapacityGB()` was added
* `datastoreIds()` was added
* `networkIds()` was added
* `memOverallUsageGB()` was added
* `cpuCapacityMHz()` was added

#### `models.VirtualMachineInventoryItem` was modified

* `cluster()` was added
* `withCluster(models.InventoryItemDetails)` was added

#### `models.HostModel` was modified

* `memorySizeGB()` was added
* `overallCpuUsageMHz()` was added
* `networkIds()` was added
* `cpuMhz()` was added
* `datastoreIds()` was added
* `overallMemoryUsageGB()` was added

#### `models.InventoryItemDetails` was modified

* `inventoryType()` was added
* `withInventoryType(models.InventoryType)` was added

#### `models.GuestAgent` was modified

* `privateLinkScopeResourceId()` was added

#### `ConnectedVMwareManager` was modified

* `vmInstanceHybridIdentityMetadatas()` was added
* `vMInstanceGuestAgents()` was added
* `virtualMachineInstances()` was added

#### `models.Datastore` was modified

* `capacityGB()` was added
* `freeSpaceGB()` was added

#### `models.InventoryItem$Definition` was modified

* `withProperties(models.InventoryItemProperties)` was added

## 1.0.0-beta.1 (2022-08-19)

- Azure Resource Manager ConnectedVMware client library for Java. This package contains Microsoft Azure SDK for ConnectedVMware Management SDK. Connected VMware Client. Package tag package-2022-01-10-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
