# Release History

## 1.0.0 (2023-11-15)

- Azure Resource Manager Scvmm client library for Java. This package contains Microsoft Azure SDK for Scvmm Management SDK. SCVMM Client. Package tag package-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VirtualMachines` was removed

* `models.VirtualMachine$Definition` was removed

* `models.VirtualMachine` was removed

* `models.ResourceProviderOperation` was removed

* `models.VmmServerPropertiesCredentials` was removed

* `models.VirtualMachineUpdate` was removed

* `models.VirtualMachineUpdateProperties` was removed

* `models.VirtualMachineListResult` was removed

* `models.VirtualMachine$Update` was removed

* `models.ResourceProviderOperationDisplay` was removed

* `models.OsProfile` was removed

* `models.ResourceProviderOperationList` was removed

* `models.NetworkInterfaces` was removed

* `models.VirtualMachine$DefinitionStages` was removed

* `models.VirtualMachine$UpdateStages` was removed

* `models.NetworkInterfacesUpdate` was removed

#### `models.AvailabilitySet$DefinitionStages` was modified

* Stage 3 was added

#### `models.InventoryItem$DefinitionStages` was modified

* Stage 2 was added

#### `models.VirtualMachineTemplate` was modified

* `java.lang.String isHighlyAvailable()` -> `models.IsHighlyAvailable isHighlyAvailable()`
* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.StopVirtualMachineOptions` was modified

* `withSkipShutdown(java.lang.Boolean)` was removed
* `java.lang.Boolean skipShutdown()` -> `models.SkipShutdown skipShutdown()`

#### `models.InventoryItem` was modified

* `provisioningState()` was removed
* `uuid()` was removed
* `inventoryItemName()` was removed
* `managedResourceId()` was removed

#### `models.VirtualNetworkListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Clouds` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

#### `models.AvailabilitySets` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.AvailabilitySetListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.VmmServerListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.VmmServer$Definition` was modified

* `withCredentials(models.VmmServerPropertiesCredentials)` was removed

#### `models.HardwareProfile` was modified

* `java.lang.String isHighlyAvailable()` -> `models.IsHighlyAvailable isHighlyAvailable()`
* `withIsHighlyAvailable(java.lang.String)` was removed

#### `models.AvailabilitySet` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.VirtualNetworks` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.InventoryItemsList` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.VirtualMachineTemplateListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `models.Cloud` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.VirtualMachineTemplates` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.CloudListResult` was modified

* `withNextLink(java.lang.String)` was removed

#### `ScvmmManager` was modified

* `virtualMachines()` was removed

#### `models.VmmServers` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed

#### `models.VmmServer` was modified

* `models.VmmServerPropertiesCredentials credentials()` -> `models.VmmCredential credentials()`
* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.VirtualNetwork` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

### Features Added

* `models.ProvisioningState` was added

* `models.ActionType` was added

* `models.GuestAgentList` was added

* `models.GuestAgent` was added

* `models.NetworkInterfaceUpdate` was added

* `models.VirtualMachineInstance` was added

* `models.VirtualMachineInstances` was added

* `models.Origin` was added

* `models.HttpProxyConfiguration` was added

* `models.Operation` was added

* `models.OsProfileForVMInstance` was added

* `models.VmInstanceHybridIdentityMetadata` was added

* `models.VirtualMachineInstanceListResult` was added

* `models.VMInstanceGuestAgents` was added

* `models.VirtualMachineInstanceUpdate` was added

* `models.SkipShutdown` was added

* `models.InfrastructureProfileUpdate` was added

* `models.InfrastructureProfile` was added

* `models.VmInstanceHybridIdentityMetadataList` was added

* `models.OperationDisplay` was added

* `models.InventoryItemProperties` was added

* `models.NetworkInterface` was added

* `models.IsHighlyAvailable` was added

* `models.OperationListResult` was added

* `models.DeleteFromHost` was added

* `models.Force` was added

* `models.VmmCredential` was added

* `models.VirtualMachineInstanceHybridIdentityMetadatas` was added

* `models.GuestCredential` was added

* `models.ProvisioningAction` was added

#### `models.VirtualMachineTemplate` was modified

* `resourceGroupName()` was added

#### `models.StopVirtualMachineOptions` was modified

* `withSkipShutdown(models.SkipShutdown)` was added

#### `models.InventoryItem` was modified

* `properties()` was added

#### `models.Clouds` was modified

* `delete(java.lang.String,java.lang.String,models.Force,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.Force,com.azure.core.util.Context)` was added

#### `models.AvailabilitySets` was modified

* `deleteByIdWithResponse(java.lang.String,models.Force,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.Force,com.azure.core.util.Context)` was added

#### `models.VirtualMachineInventoryItem` was modified

* `osVersion()` was added
* `biosGuid()` was added
* `managedMachineResourceId()` was added

#### `models.VmmServer$Definition` was modified

* `withCredentials(models.VmmCredential)` was added

#### `models.AvailabilitySet` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,models.Force,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.Force,com.azure.core.util.Context)` was added

#### `models.Cloud` was modified

* `resourceGroupName()` was added

#### `models.VirtualMachineTemplates` was modified

* `deleteByIdWithResponse(java.lang.String,models.Force,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,models.Force,com.azure.core.util.Context)` was added

#### `ScvmmManager` was modified

* `virtualMachineInstanceHybridIdentityMetadatas()` was added
* `virtualMachineInstances()` was added
* `vMInstanceGuestAgents()` was added

#### `models.VmmServers` was modified

* `delete(java.lang.String,java.lang.String,models.Force,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,models.Force,com.azure.core.util.Context)` was added

#### `models.InventoryItem$Definition` was modified

* `withProperties(models.InventoryItemProperties)` was added

#### `models.VmmServer` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetwork` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2022-05-09)

- Azure Resource Manager Scvmm client library for Java. This package contains Microsoft Azure SDK for Scvmm Management SDK. SCVMM Client. Package tag package-2020-06-05-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

