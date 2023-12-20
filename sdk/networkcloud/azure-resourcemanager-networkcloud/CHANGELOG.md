# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2023-08-25)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the on-premises clusters and their resources, such as, racks, bare metal hosts, virtual machines, workload networks and more. Package tag package-2023-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.BareMetalMachineHardwareValidationCategory` was removed

* `models.VirtualMachineVolumeParameters` was removed

* `models.BareMetalMachineValidateHardwareParameters` was removed

* `models.StorageApplianceRunReadCommandsParameters` was removed

* `models.StorageApplianceCommandSpecification` was removed

#### `models.BareMetalMachine` was modified

* `void powerOff(models.BareMetalMachinePowerOffParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult powerOff(models.BareMetalMachinePowerOffParameters,com.azure.core.util.Context)`
* `void reimage(com.azure.core.util.Context)` -> `models.OperationStatusResult reimage(com.azure.core.util.Context)`
* `void uncordon(com.azure.core.util.Context)` -> `models.OperationStatusResult uncordon(com.azure.core.util.Context)`
* `void runReadCommands(models.BareMetalMachineRunReadCommandsParameters)` -> `models.OperationStatusResult runReadCommands(models.BareMetalMachineRunReadCommandsParameters)`
* `void restart(com.azure.core.util.Context)` -> `models.OperationStatusResult restart(com.azure.core.util.Context)`
* `void runDataExtracts(models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult runDataExtracts(models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)`
* `void runCommand(models.BareMetalMachineRunCommandParameters)` -> `models.OperationStatusResult runCommand(models.BareMetalMachineRunCommandParameters)`
* `void runCommand(models.BareMetalMachineRunCommandParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult runCommand(models.BareMetalMachineRunCommandParameters,com.azure.core.util.Context)`
* `void cordon(models.BareMetalMachineCordonParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult cordon(models.BareMetalMachineCordonParameters,com.azure.core.util.Context)`
* `void runReadCommands(models.BareMetalMachineRunReadCommandsParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult runReadCommands(models.BareMetalMachineRunReadCommandsParameters,com.azure.core.util.Context)`
* `void start()` -> `models.OperationStatusResult start()`
* `void replace()` -> `models.OperationStatusResult replace()`
* `void uncordon()` -> `models.OperationStatusResult uncordon()`
* `void reimage()` -> `models.OperationStatusResult reimage()`
* `validateHardware(models.BareMetalMachineValidateHardwareParameters,com.azure.core.util.Context)` was removed
* `void replace(models.BareMetalMachineReplaceParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult replace(models.BareMetalMachineReplaceParameters,com.azure.core.util.Context)`
* `void start(com.azure.core.util.Context)` -> `models.OperationStatusResult start(com.azure.core.util.Context)`
* `void cordon()` -> `models.OperationStatusResult cordon()`
* `void runDataExtracts(models.BareMetalMachineRunDataExtractsParameters)` -> `models.OperationStatusResult runDataExtracts(models.BareMetalMachineRunDataExtractsParameters)`
* `validateHardware(models.BareMetalMachineValidateHardwareParameters)` was removed
* `void restart()` -> `models.OperationStatusResult restart()`
* `void powerOff()` -> `models.OperationStatusResult powerOff()`

#### `models.StorageAppliances` was modified

* `runReadCommands(java.lang.String,java.lang.String,models.StorageApplianceRunReadCommandsParameters)` was removed
* `void disableRemoteVendorManagement(java.lang.String,java.lang.String)` -> `models.OperationStatusResult disableRemoteVendorManagement(java.lang.String,java.lang.String)`
* `void enableRemoteVendorManagement(java.lang.String,java.lang.String)` -> `models.OperationStatusResult enableRemoteVendorManagement(java.lang.String,java.lang.String)`
* `void enableRemoteVendorManagement(java.lang.String,java.lang.String,models.StorageApplianceEnableRemoteVendorManagementParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult enableRemoteVendorManagement(java.lang.String,java.lang.String,models.StorageApplianceEnableRemoteVendorManagementParameters,com.azure.core.util.Context)`
* `void disableRemoteVendorManagement(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult disableRemoteVendorManagement(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `runReadCommands(java.lang.String,java.lang.String,models.StorageApplianceRunReadCommandsParameters,com.azure.core.util.Context)` was removed

#### `models.Cluster` was modified

* `void updateVersion(models.ClusterUpdateVersionParameters)` -> `models.OperationStatusResult updateVersion(models.ClusterUpdateVersionParameters)`
* `void deploy()` -> `models.OperationStatusResult deploy()`
* `void deploy(models.ClusterDeployParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult deploy(models.ClusterDeployParameters,com.azure.core.util.Context)`
* `void updateVersion(models.ClusterUpdateVersionParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult updateVersion(models.ClusterUpdateVersionParameters,com.azure.core.util.Context)`

#### `models.Clusters` was modified

* `void deploy(java.lang.String,java.lang.String,models.ClusterDeployParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult deploy(java.lang.String,java.lang.String,models.ClusterDeployParameters,com.azure.core.util.Context)`
* `void updateVersion(java.lang.String,java.lang.String,models.ClusterUpdateVersionParameters)` -> `models.OperationStatusResult updateVersion(java.lang.String,java.lang.String,models.ClusterUpdateVersionParameters)`
* `void updateVersion(java.lang.String,java.lang.String,models.ClusterUpdateVersionParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult updateVersion(java.lang.String,java.lang.String,models.ClusterUpdateVersionParameters,com.azure.core.util.Context)`
* `void deploy(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deploy(java.lang.String,java.lang.String)`

#### `models.StorageAppliance` was modified

* `void enableRemoteVendorManagement()` -> `models.OperationStatusResult enableRemoteVendorManagement()`
* `runReadCommands(models.StorageApplianceRunReadCommandsParameters)` was removed
* `void disableRemoteVendorManagement(com.azure.core.util.Context)` -> `models.OperationStatusResult disableRemoteVendorManagement(com.azure.core.util.Context)`
* `runReadCommands(models.StorageApplianceRunReadCommandsParameters,com.azure.core.util.Context)` was removed
* `void enableRemoteVendorManagement(models.StorageApplianceEnableRemoteVendorManagementParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult enableRemoteVendorManagement(models.StorageApplianceEnableRemoteVendorManagementParameters,com.azure.core.util.Context)`
* `void disableRemoteVendorManagement()` -> `models.OperationStatusResult disableRemoteVendorManagement()`

#### `models.KubernetesClusters` was modified

* `void restartNode(java.lang.String,java.lang.String,models.KubernetesClusterRestartNodeParameters)` -> `models.OperationStatusResult restartNode(java.lang.String,java.lang.String,models.KubernetesClusterRestartNodeParameters)`
* `void restartNode(java.lang.String,java.lang.String,models.KubernetesClusterRestartNodeParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult restartNode(java.lang.String,java.lang.String,models.KubernetesClusterRestartNodeParameters,com.azure.core.util.Context)`

#### `models.VirtualMachine` was modified

* `void powerOff(models.VirtualMachinePowerOffParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult powerOff(models.VirtualMachinePowerOffParameters,com.azure.core.util.Context)`
* `attachVolume(models.VirtualMachineVolumeParameters,com.azure.core.util.Context)` was removed
* `void start(com.azure.core.util.Context)` -> `models.OperationStatusResult start(com.azure.core.util.Context)`
* `void powerOff()` -> `models.OperationStatusResult powerOff()`
* `void restart(com.azure.core.util.Context)` -> `models.OperationStatusResult restart(com.azure.core.util.Context)`
* `void reimage()` -> `models.OperationStatusResult reimage()`
* `detachVolume(models.VirtualMachineVolumeParameters,com.azure.core.util.Context)` was removed
* `void reimage(com.azure.core.util.Context)` -> `models.OperationStatusResult reimage(com.azure.core.util.Context)`
* `detachVolume(models.VirtualMachineVolumeParameters)` was removed
* `void restart()` -> `models.OperationStatusResult restart()`
* `void start()` -> `models.OperationStatusResult start()`
* `attachVolume(models.VirtualMachineVolumeParameters)` was removed

#### `models.BareMetalMachines` was modified

* `void reimage(java.lang.String,java.lang.String)` -> `models.OperationStatusResult reimage(java.lang.String,java.lang.String)`
* `void powerOff(java.lang.String,java.lang.String)` -> `models.OperationStatusResult powerOff(java.lang.String,java.lang.String)`
* `void start(java.lang.String,java.lang.String)` -> `models.OperationStatusResult start(java.lang.String,java.lang.String)`
* `validateHardware(java.lang.String,java.lang.String,models.BareMetalMachineValidateHardwareParameters,com.azure.core.util.Context)` was removed
* `void replace(java.lang.String,java.lang.String)` -> `models.OperationStatusResult replace(java.lang.String,java.lang.String)`
* `void runReadCommands(java.lang.String,java.lang.String,models.BareMetalMachineRunReadCommandsParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult runReadCommands(java.lang.String,java.lang.String,models.BareMetalMachineRunReadCommandsParameters,com.azure.core.util.Context)`
* `validateHardware(java.lang.String,java.lang.String,models.BareMetalMachineValidateHardwareParameters)` was removed
* `void runCommand(java.lang.String,java.lang.String,models.BareMetalMachineRunCommandParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult runCommand(java.lang.String,java.lang.String,models.BareMetalMachineRunCommandParameters,com.azure.core.util.Context)`
* `void runDataExtracts(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult runDataExtracts(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)`
* `void powerOff(java.lang.String,java.lang.String,models.BareMetalMachinePowerOffParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult powerOff(java.lang.String,java.lang.String,models.BareMetalMachinePowerOffParameters,com.azure.core.util.Context)`
* `void runCommand(java.lang.String,java.lang.String,models.BareMetalMachineRunCommandParameters)` -> `models.OperationStatusResult runCommand(java.lang.String,java.lang.String,models.BareMetalMachineRunCommandParameters)`
* `void restart(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult restart(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void runReadCommands(java.lang.String,java.lang.String,models.BareMetalMachineRunReadCommandsParameters)` -> `models.OperationStatusResult runReadCommands(java.lang.String,java.lang.String,models.BareMetalMachineRunReadCommandsParameters)`
* `void uncordon(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult uncordon(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void reimage(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult reimage(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void restart(java.lang.String,java.lang.String)` -> `models.OperationStatusResult restart(java.lang.String,java.lang.String)`
* `void cordon(java.lang.String,java.lang.String,models.BareMetalMachineCordonParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult cordon(java.lang.String,java.lang.String,models.BareMetalMachineCordonParameters,com.azure.core.util.Context)`
* `void cordon(java.lang.String,java.lang.String)` -> `models.OperationStatusResult cordon(java.lang.String,java.lang.String)`
* `void start(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult start(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void replace(java.lang.String,java.lang.String,models.BareMetalMachineReplaceParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult replace(java.lang.String,java.lang.String,models.BareMetalMachineReplaceParameters,com.azure.core.util.Context)`
* `void runDataExtracts(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters)` -> `models.OperationStatusResult runDataExtracts(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters)`
* `void uncordon(java.lang.String,java.lang.String)` -> `models.OperationStatusResult uncordon(java.lang.String,java.lang.String)`

#### `models.VirtualMachines` was modified

* `attachVolume(java.lang.String,java.lang.String,models.VirtualMachineVolumeParameters)` was removed
* `void reimage(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult reimage(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void start(java.lang.String,java.lang.String)` -> `models.OperationStatusResult start(java.lang.String,java.lang.String)`
* `void powerOff(java.lang.String,java.lang.String)` -> `models.OperationStatusResult powerOff(java.lang.String,java.lang.String)`
* `void reimage(java.lang.String,java.lang.String)` -> `models.OperationStatusResult reimage(java.lang.String,java.lang.String)`
* `void restart(java.lang.String,java.lang.String)` -> `models.OperationStatusResult restart(java.lang.String,java.lang.String)`
* `void restart(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult restart(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `detachVolume(java.lang.String,java.lang.String,models.VirtualMachineVolumeParameters,com.azure.core.util.Context)` was removed
* `detachVolume(java.lang.String,java.lang.String,models.VirtualMachineVolumeParameters)` was removed
* `void powerOff(java.lang.String,java.lang.String,models.VirtualMachinePowerOffParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult powerOff(java.lang.String,java.lang.String,models.VirtualMachinePowerOffParameters,com.azure.core.util.Context)`
* `void start(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult start(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `attachVolume(java.lang.String,java.lang.String,models.VirtualMachineVolumeParameters,com.azure.core.util.Context)` was removed

#### `models.KubernetesCluster` was modified

* `void restartNode(models.KubernetesClusterRestartNodeParameters)` -> `models.OperationStatusResult restartNode(models.KubernetesClusterRestartNodeParameters)`
* `void restartNode(models.KubernetesClusterRestartNodeParameters,com.azure.core.util.Context)` -> `models.OperationStatusResult restartNode(models.KubernetesClusterRestartNodeParameters,com.azure.core.util.Context)`

### Features Added

* `models.OperationStatusResult` was added

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
