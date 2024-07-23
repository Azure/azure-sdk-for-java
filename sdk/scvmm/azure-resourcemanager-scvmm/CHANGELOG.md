# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-06-26)

- Azure Resource Manager Scvmm client library for Java. This package contains Microsoft Azure SDK for Scvmm Management SDK. SCVMM Client. Package tag package-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VirtualMachines` was removed

* `models.VirtualMachine$Definition` was removed

* `models.VirtualMachine` was removed

* `models.ResourceProviderOperation` was removed

* `models.StorageQoSPolicyDetails` was removed

* `models.VmmServerPropertiesCredentials` was removed

* `models.VirtualMachineUpdate` was removed

* `models.StorageQoSPolicy` was removed

* `models.VirtualMachineUpdateProperties` was removed

* `models.VirtualMachineListResult` was removed

* `models.VirtualMachine$Update` was removed

* `models.ResourceProviderOperationDisplay` was removed

* `models.InventoryItemsList` was removed

* `models.OsProfile` was removed

* `models.ResourceProviderOperationList` was removed

* `models.NetworkInterfaces` was removed

* `models.VirtualMachine$DefinitionStages` was removed

* `models.VirtualMachine$UpdateStages` was removed

* `models.NetworkInterfacesUpdate` was removed

* `models.ResourcePatch` was removed

#### `models.AvailabilitySet$DefinitionStages` was modified

* Stage 3 was added

#### `models.VirtualMachineTemplate` was modified

* `isCustomizable()` was removed
* `isHighlyAvailable()` was removed
* `dynamicMemoryMinMB()` was removed
* `cpuCount()` was removed
* `osType()` was removed
* `uuid()` was removed
* `disks()` was removed
* `memoryMB()` was removed
* `networkInterfaces()` was removed
* `computerName()` was removed
* `osName()` was removed
* `vmmServerId()` was removed
* `dynamicMemoryMaxMB()` was removed
* `dynamicMemoryEnabled()` was removed
* `inventoryItemId()` was removed
* `generation()` was removed
* `provisioningState()` was removed
* `limitCpuForMigration()` was removed

#### `models.StopVirtualMachineOptions` was modified

* `java.lang.Boolean skipShutdown()` -> `models.SkipShutdown skipShutdown()`
* `withSkipShutdown(java.lang.Boolean)` was removed

#### `models.InventoryItem` was modified

* `inventoryItemName()` was removed
* `provisioningState()` was removed
* `managedResourceId()` was removed
* `uuid()` was removed

#### `models.VirtualNetworkListResult` was modified

* `withValue(java.util.List)` was removed
* `withNextLink(java.lang.String)` was removed

#### `models.Clouds` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.AvailabilitySets` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.AvailabilitySetListResult` was modified

* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed

#### `models.VmmServerListResult` was modified

* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed

#### `models.CloudCapacity` was modified

* `withCpuCount(java.lang.Long)` was removed
* `withVmCount(java.lang.Long)` was removed
* `withMemoryMB(java.lang.Long)` was removed

#### `models.VmmServer$Definition` was modified

* `withPort(java.lang.Integer)` was removed
* `withCredentials(models.VmmServerPropertiesCredentials)` was removed
* `withFqdn(java.lang.String)` was removed

#### `models.VirtualDisk` was modified

* `storageQoSPolicy()` was removed
* `withStorageQoSPolicy(models.StorageQoSPolicyDetails)` was removed

#### `models.HardwareProfile` was modified

* `withIsHighlyAvailable(java.lang.String)` was removed
* `java.lang.String isHighlyAvailable()` -> `models.IsHighlyAvailable isHighlyAvailable()`

#### `models.Cloud$Definition` was modified

* `withUuid(java.lang.String)` was removed
* `withVmmServerId(java.lang.String)` was removed
* `withInventoryItemId(java.lang.String)` was removed

#### `models.AvailabilitySet` was modified

* `provisioningState()` was removed
* `availabilitySetName()` was removed
* `vmmServerId()` was removed

#### `models.VirtualNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.VirtualMachineTemplate$Definition` was modified

* `withInventoryItemId(java.lang.String)` was removed
* `withVmmServerId(java.lang.String)` was removed
* `withUuid(java.lang.String)` was removed

#### `models.VirtualNetwork$Definition` was modified

* `withVmmServerId(java.lang.String)` was removed
* `withUuid(java.lang.String)` was removed
* `withInventoryItemId(java.lang.String)` was removed

#### `models.VirtualMachineTemplateListResult` was modified

* `withValue(java.util.List)` was removed
* `withNextLink(java.lang.String)` was removed

#### `models.Cloud` was modified

* `cloudName()` was removed
* `cloudCapacity()` was removed
* `vmmServerId()` was removed
* `inventoryItemId()` was removed
* `provisioningState()` was removed
* `uuid()` was removed
* `storageQoSPolicies()` was removed

#### `models.VirtualMachineTemplates` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.CloudListResult` was modified

* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed

#### `ScvmmManager` was modified

* `virtualMachines()` was removed

#### `models.VmmServers` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.VirtualDiskUpdate` was modified

* `storageQoSPolicy()` was removed
* `withStorageQoSPolicy(models.StorageQoSPolicyDetails)` was removed

#### `models.VmmServer` was modified

* `connectionStatus()` was removed
* `errorMessage()` was removed
* `credentials()` was removed
* `uuid()` was removed
* `fqdn()` was removed
* `version()` was removed
* `provisioningState()` was removed
* `port()` was removed

#### `models.VirtualNetwork` was modified

* `inventoryItemId()` was removed
* `networkName()` was removed
* `provisioningState()` was removed
* `uuid()` was removed
* `vmmServerId()` was removed

#### `models.AvailabilitySet$Definition` was modified

* `withVmmServerId(java.lang.String)` was removed
* `withAvailabilitySetName(java.lang.String)` was removed

### Features Added

* `models.InventoryItemListResult` was added

* `models.ProvisioningState` was added

* `models.VmInstanceHybridIdentityMetadataListResult` was added

* `models.ActionType` was added

* `models.GuestAgent` was added

* `models.NetworkInterfaceUpdate` was added

* `models.VirtualMachineInstance` was added

* `models.GuestAgentProperties` was added

* `models.VmInstanceHybridIdentityMetadatas` was added

* `models.VirtualMachineInstances` was added

* `models.Origin` was added

* `models.HttpProxyConfiguration` was added

* `models.Operation` was added

* `models.VmInstanceHybridIdentityMetadata` was added

* `models.VirtualMachineInstanceListResult` was added

* `models.VirtualMachineTemplateTagsUpdate` was added

* `models.VmInstanceHybridIdentityMetadataProperties` was added

* `models.VirtualMachineInstanceUpdate` was added

* `models.ForceDelete` was added

* `models.VirtualMachineTemplateProperties` was added

* `models.SkipShutdown` was added

* `models.VmmServerTagsUpdate` was added

* `models.AvailabilitySetTagsUpdate` was added

* `models.InfrastructureProfileUpdate` was added

* `models.InfrastructureProfile` was added

* `models.CloudTagsUpdate` was added

* `models.OperationDisplay` was added

* `models.InventoryItemProperties` was added

* `models.CloudProperties` was added

* `models.VirtualMachineInstanceProperties` was added

* `models.NetworkInterface` was added

* `models.AvailabilitySetProperties` was added

* `models.GuestAgents` was added

* `models.IsHighlyAvailable` was added

* `models.OperationListResult` was added

* `models.DeleteFromHost` was added

* `models.StorageQosPolicyDetails` was added

* `models.VmmCredential` was added

* `models.StorageQosPolicy` was added

* `models.GuestCredential` was added

* `models.VirtualMachineInstanceUpdateProperties` was added

* `models.VmmServerProperties` was added

* `models.OsProfileForVmInstance` was added

* `models.ProvisioningAction` was added

* `models.VirtualNetworkTagsUpdate` was added

* `models.GuestAgentListResult` was added

* `models.VirtualNetworkProperties` was added

#### `models.VirtualNetworkInventoryItem` was modified

* `provisioningState()` was added
* `uuid()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `inventoryType()` was added
* `managedResourceId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `inventoryItemName()` was added

#### `models.VirtualMachineTemplate` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `models.ExtendedLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StopVirtualMachineOptions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withSkipShutdown(models.SkipShutdown)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InventoryItem` was modified

* `properties()` was added

#### `models.NetworkProfileUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudInventoryItem` was modified

* `inventoryType()` was added
* `uuid()` was added
* `managedResourceId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `inventoryItemName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `provisioningState()` was added

#### `models.VirtualNetworkListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Clouds` was modified

* `deleteByIdWithResponse(java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added

#### `models.AvailabilitySets` was modified

* `deleteByIdWithResponse(java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added

#### `models.VirtualMachineInventoryItem` was modified

* `managedResourceId()` was added
* `provisioningState()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `uuid()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `inventoryType()` was added
* `inventoryItemName()` was added
* `biosGuid()` was added
* `osVersion()` was added
* `managedMachineResourceId()` was added

#### `models.AvailabilitySetListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VmmServerListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudCapacity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VmmServer$Definition` was modified

* `withProperties(models.VmmServerProperties)` was added

#### `models.VirtualDisk` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withStorageQosPolicy(models.StorageQosPolicyDetails)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `storageQosPolicy()` was added

#### `models.HardwareProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageProfileUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cloud$Definition` was modified

* `withProperties(models.CloudProperties)` was added

#### `models.AvailabilitySet` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `models.HardwareProfileUpdate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added

#### `models.VirtualMachineTemplate$Definition` was modified

* `withProperties(models.VirtualMachineTemplateProperties)` was added

#### `models.InventoryItemDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineTemplateInventoryItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `managedResourceId()` was added
* `provisioningState()` was added
* `inventoryType()` was added
* `uuid()` was added
* `inventoryItemName()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetwork$Definition` was modified

* `withProperties(models.VirtualNetworkProperties)` was added

#### `models.VirtualMachineTemplateListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineCreateCheckpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cloud` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `models.VirtualMachineTemplates` was modified

* `delete(java.lang.String,java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added

#### `models.VirtualMachineRestoreCheckpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Checkpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineDeleteCheckpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AvailabilitySetListItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `ScvmmManager` was modified

* `virtualMachineInstances()` was added
* `guestAgents()` was added
* `vmInstanceHybridIdentityMetadatas()` was added

#### `models.VmmServers` was modified

* `delete(java.lang.String,java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.ForceDelete,com.azure.core.util.Context)` was added

#### `models.VirtualDiskUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withStorageQosPolicy(models.StorageQosPolicyDetails)` was added
* `storageQosPolicy()` was added

#### `models.InventoryItem$Definition` was modified

* `withProperties(models.InventoryItemProperties)` was added

#### `models.VmmServer` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `models.VirtualNetwork` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `models.AvailabilitySet$Definition` was modified

* `withProperties(models.AvailabilitySetProperties)` was added

## 1.0.0-beta.1 (2022-05-09)

- Azure Resource Manager Scvmm client library for Java. This package contains Microsoft Azure SDK for Scvmm Management SDK. SCVMM Client. Package tag package-2020-06-05-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

