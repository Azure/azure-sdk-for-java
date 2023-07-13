# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-07-03)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the on-premises clusters and their resources, such as, racks, bare metal hosts, virtual machines, workload networks and more. Package tag package-2023-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.HybridAksCluster$Update` was removed

* `models.HybridAksClusters` was removed

* `models.HybridAksCluster$DefinitionStages` was removed

* `models.HybridAksClusterProvisioningState` was removed

* `models.HybridAksClusterDetailedStatus` was removed

* `models.DefaultCniNetworkDetailedStatus` was removed

* `models.DefaultCniNetwork$UpdateStages` was removed

* `models.NodeConfiguration` was removed

* `models.StorageApplianceHardwareValidationCategory` was removed

* `models.DefaultCniNetworkPatchParameters` was removed

* `models.BgpPeer` was removed

* `models.Node` was removed

* `models.DefaultCniNetwork$DefinitionStages` was removed

* `models.DefaultCniNetwork$Definition` was removed

* `models.HybridAksCluster$UpdateStages` was removed

* `models.CniBgpConfiguration` was removed

* `models.HybridAksClusterList` was removed

* `models.HybridAksClusterRestartNodeParameters` was removed

* `models.HybridAksClusterPatchParameters` was removed

* `models.StorageApplianceValidateHardwareParameters` was removed

* `models.CommunityAdvertisement` was removed

* `models.DefaultCniNetworkProvisioningState` was removed

* `models.HybridAksCluster` was removed

* `models.HybridAksClusterMachinePowerState` was removed

* `models.HybridAksCluster$Definition` was removed

* `models.DefaultCniNetwork$Update` was removed

* `models.DefaultCniNetworkList` was removed

* `models.DefaultCniNetwork` was removed

* `models.DefaultCniNetworks` was removed

#### `models.Cluster$DefinitionStages` was modified

* `withAnalyticsWorkspaceId(java.lang.String)` was removed in stage 5
* `withClusterType(models.ClusterType)` was removed in stage 6
* `withClusterVersion(java.lang.String)` was removed in stage 7

#### `models.BareMetalMachineKeySets` was modified

* `listByResourceGroup(java.lang.String,java.lang.String)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.StorageAppliances` was modified

* `validateHardware(java.lang.String,java.lang.String,models.StorageApplianceValidateHardwareParameters,com.azure.core.util.Context)` was removed
* `validateHardware(java.lang.String,java.lang.String,models.StorageApplianceValidateHardwareParameters)` was removed

#### `models.StorageAppliance` was modified

* `validateHardware(models.StorageApplianceValidateHardwareParameters,com.azure.core.util.Context)` was removed
* `validateHardware(models.StorageApplianceValidateHardwareParameters)` was removed

#### `models.MetricsConfigurations` was modified

* `listByResourceGroup(java.lang.String,java.lang.String)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Consoles` was modified

* `listByResourceGroup(java.lang.String,java.lang.String)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `NetworkCloudManager` was modified

* `hybridAksClusters()` was removed
* `defaultCniNetworks()` was removed

#### `models.BmcKeySets` was modified

* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String)` was removed

### Features Added

* `models.AgentPool$Update` was added

* `models.TrunkedNetworkAttachmentConfiguration` was added

* `models.KubernetesCluster$Definition` was added

* `models.AgentPool` was added

* `models.AgentPool$DefinitionStages` was added

* `models.KubernetesCluster$DefinitionStages` was added

* `models.KubernetesClusterList` was added

* `models.NetworkConfiguration` was added

* `models.AgentPool$Definition` was added

* `models.AgentPoolProvisioningState` was added

* `models.IpAddressPool` was added

* `models.AgentPool$UpdateStages` was added

* `models.AgentOptions` was added

* `models.KubernetesPluginType` was added

* `models.KubernetesNodePowerState` was added

* `models.AadConfiguration` was added

* `models.ControlPlaneNodeConfiguration` was added

* `models.BgpAdvertisement` was added

* `models.KubernetesLabel` was added

* `models.HugepagesSize` was added

* `models.AgentPoolPatchParameters` was added

* `models.BgpMultiHop` was added

* `models.FabricPeeringEnabled` was added

* `models.KubernetesClusterDetailedStatus` was added

* `models.KubernetesClusterProvisioningState` was added

* `models.KubernetesClusters` was added

* `models.FeatureStatus` was added

* `models.FeatureDetailedStatus` was added

* `models.AgentPoolMode` was added

* `models.KubernetesClusterNodeDetailedStatus` was added

* `models.ControlPlaneNodePatchConfiguration` was added

* `models.BgpServiceLoadBalancerConfiguration` was added

* `models.AdministratorConfiguration` was added

* `models.AgentPools` was added

* `models.AgentPoolUpgradeSettings` was added

* `models.KubernetesNodeRole` was added

* `models.AgentPoolList` was added

* `models.BfdEnabled` was added

* `models.KubernetesCluster$Update` was added

* `models.AvailabilityLifecycle` was added

* `models.AvailableUpgrade` was added

* `models.KubernetesClusterRestartNodeParameters` was added

* `models.L3NetworkAttachmentConfiguration` was added

* `models.AttachedNetworkConfiguration` was added

* `models.KubernetesCluster` was added

* `models.AdvertiseToFabric` was added

* `models.L3NetworkConfigurationIpamEnabled` was added

* `models.KubernetesClusterPatchParameters` was added

* `models.L2NetworkAttachmentConfiguration` was added

* `models.KubernetesClusterNode` was added

* `models.ServiceLoadBalancerBgpPeer` was added

* `models.InitialAgentPoolConfiguration` was added

* `models.AgentPoolDetailedStatus` was added

* `models.KubernetesCluster$UpdateStages` was added

#### `models.BareMetalMachine` was modified

* `associatedResourceIds()` was added

#### `models.BareMetalMachineKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCluster(java.lang.String,java.lang.String)` was added

#### `models.MetricsConfigurations` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCluster(java.lang.String,java.lang.String)` was added

#### `models.TrunkedNetwork` was modified

* `associatedResourceIds()` was added

#### `models.L3Network` was modified

* `associatedResourceIds()` was added

#### `models.Consoles` was modified

* `listByVirtualMachine(java.lang.String,java.lang.String)` was added
* `listByVirtualMachine(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.L2Network` was modified

* `associatedResourceIds()` was added

#### `models.CloudServicesNetwork` was modified

* `associatedResourceIds()` was added

#### `NetworkCloudManager` was modified

* `agentPools()` was added
* `kubernetesClusters()` was added

#### `models.VirtualMachine` was modified

* `availabilityZone()` was added

#### `models.BmcKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCluster(java.lang.String,java.lang.String)` was added

## 1.0.0-beta.1 (2023-05-18)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the on-premises clusters and their resources, such as, racks, bare metal hosts, virtual machines, workload networks and more. Package tag package-2022-12-12-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
