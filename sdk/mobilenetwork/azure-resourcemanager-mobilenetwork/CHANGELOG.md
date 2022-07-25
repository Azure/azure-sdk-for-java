# Release History

## 1.0.0-beta.3 (2022-07-25)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Sim$DefinitionStages` was modified

* `withRegion(java.lang.String)` was removed in stage 1
* `withRegion(com.azure.core.management.Region)` was removed in stage 1
* `withExistingResourceGroup(java.lang.String)` was removed in stage 2

#### `models.PacketCoreControlPlane$DefinitionStages` was modified

* Stage 5 was added

#### `models.PacketCoreControlPlane` was modified

* `customLocation()` was removed

#### `models.Sim` was modified

* `regionName()` was removed
* `mobileNetwork()` was removed
* `region()` was removed
* `location()` was removed
* `systemData()` was removed
* `tags()` was removed

#### `models.Sims` was modified

* `deleteByResourceGroup(java.lang.String,java.lang.String)` was removed
* `getByResourceGroup(java.lang.String,java.lang.String)` was removed
* `listByResourceGroup(java.lang.String)` was removed
* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `list()` was removed

#### `models.Sim$Definition` was modified

* `withRegion(java.lang.String)` was removed
* `withMobileNetwork(models.MobileNetworkResourceId)` was removed
* `withExistingResourceGroup(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `withRegion(com.azure.core.management.Region)` was removed

#### `models.PacketCoreControlPlane$Definition` was modified

* `withCustomLocation(models.CustomLocationResourceId)` was removed

#### `models.Sim$Update` was modified

* `withTags(java.util.Map)` was removed

### Features Added

* `models.ManagedServiceIdentity` was added

* `models.RecommendedVersion` was added

* `models.ConnectedClusterResourceId` was added

* `models.BillingSku` was added

* `models.SimGroupListResult` was added

* `models.PacketCoreControlPlaneVersions` was added

* `models.UserAssignedIdentity` was added

* `models.PacketCoreControlPlaneVersion` was added

* `models.SimGroups` was added

* `models.ManagedServiceIdentityType` was added

* `models.KeyVaultCertificate` was added

* `models.KeyVaultKey` was added

* `models.SimGroup$Definition` was added

* `models.PacketCoreControlPlaneVersionListResult` was added

* `models.LocalDiagnosticsAccessConfiguration` was added

* `models.SimGroup$UpdateStages` was added

* `models.SimGroup$Update` was added

* `models.PlatformType` was added

* `models.AzureStackEdgeDeviceResourceId` was added

* `models.PlatformConfiguration` was added

* `models.SimGroup$DefinitionStages` was added

* `models.SimGroup` was added

* `models.VersionState` was added

#### `models.DataNetwork` was modified

* `resourceGroupName()` was added

#### `models.Slice` was modified

* `resourceGroupName()` was added

#### `models.PacketCoreControlPlane` was modified

* `resourceGroupName()` was added
* `localDiagnosticsAccess()` was added
* `sku()` was added
* `interopSettings()` was added
* `platform()` was added
* `identity()` was added

#### `models.Site` was modified

* `resourceGroupName()` was added

#### `models.Sim` was modified

* `resourceGroupName()` was added

#### `models.Sims` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listBySimGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listBySimGroup(java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.AttachedDataNetwork$Definition` was modified

* `withDnsAddresses(java.util.List)` was added

#### `models.SimPolicy` was modified

* `resourceGroupName()` was added

#### `models.MobileNetwork` was modified

* `resourceGroupName()` was added

#### `models.AttachedDataNetwork` was modified

* `resourceGroupName()` was added
* `dnsAddresses()` was added

#### `models.PacketCoreDataPlane` was modified

* `resourceGroupName()` was added

#### `models.Sim$Definition` was modified

* `withExistingSimGroup(java.lang.String,java.lang.String)` was added

#### `MobileNetworkManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `simGroups()` was added
* `packetCoreControlPlaneVersions()` was added

#### `models.Service` was modified

* `resourceGroupName()` was added

#### `MobileNetworkManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.PacketCoreControlPlane$Definition` was modified

* `withPlatform(models.PlatformConfiguration)` was added
* `withLocalDiagnosticsAccess(models.LocalDiagnosticsAccessConfiguration)` was added
* `withInteropSettings(java.lang.Object)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withSku(models.BillingSku)` was added

#### `models.Sim$Update` was modified

* `withAuthenticationKey(java.lang.String)` was added
* `withSimPolicy(models.SimPolicyResourceId)` was added
* `withStaticIpConfiguration(java.util.List)` was added
* `withDeviceType(java.lang.String)` was added
* `withIntegratedCircuitCardIdentifier(java.lang.String)` was added
* `withOperatorKeyCode(java.lang.String)` was added

## 1.0.0-beta.2 (2022-03-25)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ConfigurationState` was removed

#### `models.Sim` was modified

* `configurationState()` was removed

### Features Added

* `models.SimState` was added

#### `models.DataNetwork` was modified

* `systemData()` was added

#### `models.Slice` was modified

* `systemData()` was added

#### `models.PacketCoreControlPlane` was modified

* `systemData()` was added

#### `models.Site` was modified

* `systemData()` was added

#### `models.Sim` was modified

* `simState()` was added
* `systemData()` was added

#### `models.SimPolicy` was modified

* `systemData()` was added

#### `models.MobileNetwork` was modified

* `systemData()` was added

#### `models.PacketCoreDataPlane` was modified

* `systemData()` was added

#### `models.Service` was modified

* `systemData()` was added

#### `models.InterfaceProperties` was modified

* `ipv4Subnet()` was added
* `withIpv4Subnet(java.lang.String)` was added
* `withIpv4Gateway(java.lang.String)` was added
* `ipv4Gateway()` was added
* `ipv4Address()` was added
* `withIpv4Address(java.lang.String)` was added

## 1.0.0-beta.1 (2022-02-28)

- Azure Resource Manager MobileNetwork client library for Java. This package contains Microsoft Azure SDK for MobileNetwork Management SDK. The resources in this swagger specification will be used to manage attached data network resources in mobile network attached to a particular packet core instance. Package tag package-2022-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
