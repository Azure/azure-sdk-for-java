# Release History

## 2.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.1.0 (2025-12-17)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the Azure Operator Nexus compute resources such as on-premises clusters, hardware resources, and workload infrastructure resources. Package tag package-2025-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BareMetalMachineKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Racks` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.StorageAppliances` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.Clusters` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.MetricsConfigurations` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Volumes` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.KubernetesClusters` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.KubernetesClusterFeatures` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Consoles` was modified

* `listByVirtualMachine(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.L3Networks` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.CloudServicesNetworks` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.ClusterManagers` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.AgentPools` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BmcKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BareMetalMachines` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.VirtualMachines` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.TrunkedNetworks` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.L2Networks` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

### Features Added

* `models.VirtualMachineAssignRelayParameters` was added

* `models.CloudServicesNetworkStorageStatus` was added

* `models.ActionStateStatus` was added

* `models.CommandOutputType` was added

* `models.StepStateStatus` was added

* `models.RelayType` was added

* `models.CloudServicesNetworkStorageOptionsPatch` was added

* `models.ActionState` was added

* `models.CommandOutputOverride` was added

* `models.CloudServicesNetworkStorageStatusStatus` was added

* `models.BareMetalMachineReplaceSafeguardMode` was added

* `models.StepState` was added

* `models.BareMetalMachineReplaceStoragePolicy` was added

* `models.StorageApplianceRunReadCommandsParameters` was added

* `models.CertificateInfo` was added

* `models.CloudServicesNetworkStorageMode` was added

* `models.StorageApplianceCommandSpecification` was added

* `models.CloudServicesNetworkStorageOptions` was added

#### `models.VirtualMachinePatchParameters` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `identity()` was added

#### `models.BareMetalMachine` was modified

* `runDataExtractsRestricted(models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)` was added
* `actionStates()` was added
* `caCertificate()` was added
* `runDataExtractsRestricted(models.BareMetalMachineRunDataExtractsParameters)` was added

#### `models.BareMetalMachineKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Racks` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.StorageAppliances` was modified

* `runReadCommands(java.lang.String,java.lang.String,models.StorageApplianceRunReadCommandsParameters)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `runReadCommands(java.lang.String,java.lang.String,models.StorageApplianceRunReadCommandsParameters,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Cluster` was modified

* `actionStates()` was added

#### `models.Clusters` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachineKeySet$Definition` was modified

* `withPrivilegeLevelName(java.lang.String)` was added

#### `models.Volume` was modified

* `allocatedSizeMiB()` was added
* `storageApplianceId()` was added

#### `models.BareMetalMachineReplaceParameters` was modified

* `withSafeguardMode(models.BareMetalMachineReplaceSafeguardMode)` was added
* `safeguardMode()` was added
* `storagePolicy()` was added
* `withStoragePolicy(models.BareMetalMachineReplaceStoragePolicy)` was added

#### `models.StorageAppliance` was modified

* `runReadCommands(models.StorageApplianceRunReadCommandsParameters,com.azure.core.util.Context)` was added
* `runReadCommands(models.StorageApplianceRunReadCommandsParameters)` was added
* `caCertificate()` was added

#### `models.Volume$Definition` was modified

* `withStorageApplianceId(java.lang.String)` was added

#### `models.CloudServicesNetworkPatchParameters` was modified

* `withStorageOptions(models.CloudServicesNetworkStorageOptionsPatch)` was added
* `storageOptions()` was added

#### `models.MetricsConfigurations` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Volumes` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusters` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusterFeatures` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Consoles` was modified

* `listByVirtualMachine(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CloudServicesNetwork` was modified

* `storageOptions()` was added
* `storageStatus()` was added

#### `models.L3Networks` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachine$Definition` was modified

* `withNetworkDataContent(java.lang.String)` was added
* `withUserDataContent(java.lang.String)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.VirtualMachine$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CloudServicesNetworks` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ClusterManagers` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AgentPools` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachine` was modified

* `assignRelay()` was added
* `userDataContent()` was added
* `networkDataContent()` was added
* `assignRelay(models.VirtualMachineAssignRelayParameters,com.azure.core.util.Context)` was added
* `identity()` was added

#### `models.SecretArchiveReference` was modified

* `keyVaultUri()` was added

#### `models.CommandOutputSettings` was modified

* `withOverrides(java.util.List)` was added
* `overrides()` was added

#### `models.CloudServicesNetwork$Definition` was modified

* `withStorageOptions(models.CloudServicesNetworkStorageOptions)` was added

#### `models.BmcKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachines` was modified

* `runDataExtractsRestricted(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters)` was added
* `runDataExtractsRestricted(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachines` was modified

* `assignRelay(java.lang.String,java.lang.String)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `assignRelay(java.lang.String,java.lang.String,models.VirtualMachineAssignRelayParameters,com.azure.core.util.Context)` was added

#### `models.BareMetalMachineKeySet` was modified

* `privilegeLevelName()` was added

#### `models.TrunkedNetworks` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CloudServicesNetwork$Update` was modified

* `withStorageOptions(models.CloudServicesNetworkStorageOptionsPatch)` was added

#### `models.L2Networks` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

## 2.1.0-beta.1 (2025-11-18)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the Azure Operator Nexus compute resources such as on-premises clusters, hardware resources, and workload infrastructure resources. Package tag package-2025-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BareMetalMachineKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Racks` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.StorageAppliances` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Clusters` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.MetricsConfigurations` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Volumes` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.KubernetesClusters` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.KubernetesClusterFeatures` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Consoles` was modified

* `listByVirtualMachine(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.L3Networks` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CloudServicesNetworks` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ClusterManagers` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AgentPools` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BmcKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BareMetalMachines` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.VirtualMachines` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.TrunkedNetworks` was modified

* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed
* `list(com.azure.core.util.Context)` was removed

#### `models.L2Networks` was modified

* `list(com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.VirtualMachineAssignRelayParameters` was added

* `models.CloudServicesNetworkStorageStatus` was added

* `models.ActionStateStatus` was added

* `models.CommandOutputType` was added

* `models.StepStateStatus` was added

* `models.RelayType` was added

* `models.CloudServicesNetworkStorageOptionsPatch` was added

* `models.ActionState` was added

* `models.CommandOutputOverride` was added

* `models.CloudServicesNetworkStorageStatusStatus` was added

* `models.BareMetalMachineReplaceSafeguardMode` was added

* `models.StepState` was added

* `models.BareMetalMachineReplaceStoragePolicy` was added

* `models.StorageApplianceRunReadCommandsParameters` was added

* `models.CertificateInfo` was added

* `models.CloudServicesNetworkStorageMode` was added

* `models.StorageApplianceCommandSpecification` was added

* `models.CloudServicesNetworkStorageOptions` was added

#### `models.VirtualMachinePatchParameters` was modified

* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.BareMetalMachine` was modified

* `runDataExtractsRestricted(models.BareMetalMachineRunDataExtractsParameters)` was added
* `actionStates()` was added
* `caCertificate()` was added
* `runDataExtractsRestricted(models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)` was added

#### `models.BareMetalMachineKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Racks` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.StorageAppliances` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `runReadCommands(java.lang.String,java.lang.String,models.StorageApplianceRunReadCommandsParameters)` was added
* `runReadCommands(java.lang.String,java.lang.String,models.StorageApplianceRunReadCommandsParameters,com.azure.core.util.Context)` was added

#### `models.Cluster` was modified

* `actionStates()` was added

#### `models.Clusters` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachineKeySet$Definition` was modified

* `withPrivilegeLevelName(java.lang.String)` was added

#### `models.Volume` was modified

* `allocatedSizeMiB()` was added
* `storageApplianceId()` was added

#### `models.BareMetalMachineReplaceParameters` was modified

* `safeguardMode()` was added
* `storagePolicy()` was added
* `withStoragePolicy(models.BareMetalMachineReplaceStoragePolicy)` was added
* `withSafeguardMode(models.BareMetalMachineReplaceSafeguardMode)` was added

#### `models.StorageAppliance` was modified

* `caCertificate()` was added
* `runReadCommands(models.StorageApplianceRunReadCommandsParameters,com.azure.core.util.Context)` was added
* `runReadCommands(models.StorageApplianceRunReadCommandsParameters)` was added

#### `models.Volume$Definition` was modified

* `withStorageApplianceId(java.lang.String)` was added

#### `models.CloudServicesNetworkPatchParameters` was modified

* `storageOptions()` was added
* `withStorageOptions(models.CloudServicesNetworkStorageOptionsPatch)` was added

#### `models.MetricsConfigurations` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Volumes` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusters` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusterFeatures` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Consoles` was modified

* `listByVirtualMachine(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CloudServicesNetwork` was modified

* `storageOptions()` was added
* `storageStatus()` was added

#### `models.L3Networks` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachine$Definition` was modified

* `withUserDataContent(java.lang.String)` was added
* `withNetworkDataContent(java.lang.String)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.VirtualMachine$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.CloudServicesNetworks` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ClusterManagers` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AgentPools` was modified

* `listByKubernetesCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.VirtualMachine` was modified

* `assignRelay()` was added
* `identity()` was added
* `assignRelay(models.VirtualMachineAssignRelayParameters,com.azure.core.util.Context)` was added
* `networkDataContent()` was added
* `userDataContent()` was added

#### `models.SecretArchiveReference` was modified

* `keyVaultUri()` was added

#### `models.CommandOutputSettings` was modified

* `overrides()` was added
* `withOverrides(java.util.List)` was added

#### `models.CloudServicesNetwork$Definition` was modified

* `withStorageOptions(models.CloudServicesNetworkStorageOptions)` was added

#### `models.BmcKeySets` was modified

* `listByCluster(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachines` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `runDataExtractsRestricted(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters,com.azure.core.util.Context)` was added
* `runDataExtractsRestricted(java.lang.String,java.lang.String,models.BareMetalMachineRunDataExtractsParameters)` was added

#### `models.VirtualMachines` was modified

* `assignRelay(java.lang.String,java.lang.String)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `assignRelay(java.lang.String,java.lang.String,models.VirtualMachineAssignRelayParameters,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachineKeySet` was modified

* `privilegeLevelName()` was added

#### `models.TrunkedNetworks` was modified

* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CloudServicesNetwork$Update` was modified

* `withStorageOptions(models.CloudServicesNetworkStorageOptionsPatch)` was added

#### `models.L2Networks` was modified

* `listByResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

## 2.0.0 (2025-06-17)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the Azure Operator Nexus compute resources such as on-premises clusters, hardware resources, and workload infrastructure resources. Package tag package-2025-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BareMetalMachineKeySets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Racks` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.StorageAppliances` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Clusters` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.MetricsConfigurations` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Volumes` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.KubernetesClusters` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.KubernetesClusterFeatures` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Consoles` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.L3Networks` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CloudServicesNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ClusterManagers` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AgentPools` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BmcKeySets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BareMetalMachines` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.VirtualMachines` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.TrunkedNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.L2Networks` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.VulnerabilityScanningSettingsPatch` was added

* `models.VulnerabilityScanningSettingsContainerScan` was added

* `models.VulnerabilityScanningSettings` was added

* `models.SecretArchiveSettings` was added

* `models.AnalyticsOutputSettings` was added

#### `models.L3Network$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.Rack` was modified

* `etag()` was added

#### `models.AgentPool$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.BareMetalMachine` was modified

* `etag()` was added

#### `models.BmcKeySet$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.L2Network$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.KubernetesCluster$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.AgentPool` was modified

* `etag()` was added

#### `models.L3Network$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.BareMetalMachineKeySets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.L2Network$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.AgentPool$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.Console$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.Racks` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Console$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.KubernetesClusterFeature$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.StorageAppliances` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Cluster` was modified

* `secretArchiveSettings()` was added
* `vulnerabilityScanningSettings()` was added
* `etag()` was added
* `analyticsOutputSettings()` was added

#### `models.Clusters` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachineKeySet$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.Volume` was modified

* `etag()` was added

#### `models.StorageAppliance$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.StorageAppliance` was modified

* `etag()` was added

#### `models.Volume$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.ClusterMetricsConfiguration$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.MetricsConfigurations` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Console` was modified

* `etag()` was added

#### `models.TrunkedNetwork` was modified

* `etag()` was added

#### `models.L3Network` was modified

* `etag()` was added

#### `models.Volumes` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusters` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachine$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.BareMetalMachine$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.KubernetesClusterFeatures` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusterFeature$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.Consoles` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.L2Network` was modified

* `etag()` was added

#### `models.CloudServicesNetwork` was modified

* `etag()` was added

#### `models.L3Networks` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ClusterPatchParameters` was modified

* `secretArchiveSettings()` was added
* `vulnerabilityScanningSettings()` was added
* `withSecretArchiveSettings(models.SecretArchiveSettings)` was added
* `withAnalyticsOutputSettings(models.AnalyticsOutputSettings)` was added
* `withVulnerabilityScanningSettings(models.VulnerabilityScanningSettingsPatch)` was added
* `analyticsOutputSettings()` was added

#### `models.VirtualMachine$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withConsoleExtendedLocation(models.ExtendedLocation)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.ClusterManager$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.VirtualMachine$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.ClusterMetricsConfiguration$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.CloudServicesNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ClusterManagers` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AgentPools` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesClusterFeature` was modified

* `etag()` was added

#### `models.ClusterManager$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.VirtualMachine` was modified

* `consoleExtendedLocation()` was added
* `etag()` was added

#### `models.BmcKeySet` was modified

* `etag()` was added

#### `models.KubernetesCluster$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.Cluster$Definition` was modified

* `withAnalyticsOutputSettings(models.AnalyticsOutputSettings)` was added
* `withVulnerabilityScanningSettings(models.VulnerabilityScanningSettings)` was added
* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added
* `withSecretArchiveSettings(models.SecretArchiveSettings)` was added

#### `models.Cluster$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withVulnerabilityScanningSettings(models.VulnerabilityScanningSettingsPatch)` was added
* `withIfNoneMatch(java.lang.String)` was added
* `withAnalyticsOutputSettings(models.AnalyticsOutputSettings)` was added
* `withSecretArchiveSettings(models.SecretArchiveSettings)` was added

#### `models.ClusterMetricsConfiguration` was modified

* `etag()` was added

#### `models.CloudServicesNetwork$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.BmcKeySets` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BareMetalMachines` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Rack$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.VirtualMachines` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.KubernetesCluster` was modified

* `etag()` was added

#### `models.BareMetalMachineKeySet` was modified

* `etag()` was added

#### `models.TrunkedNetwork$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.TrunkedNetwork$Update` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.ClusterManager` was modified

* `etag()` was added

#### `models.BareMetalMachineKeySet$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.BmcKeySet$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.Rack$Definition` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.TrunkedNetworks` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Volume$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.StorageAppliance$Definition` was modified

* `withIfMatch(java.lang.String)` was added
* `withIfNoneMatch(java.lang.String)` was added

#### `models.CloudServicesNetwork$Update` was modified

* `withIfNoneMatch(java.lang.String)` was added
* `withIfMatch(java.lang.String)` was added

#### `models.L2Networks` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.2.0-beta.1 (2025-04-21)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the Azure Operator Nexus compute resources such as on-premises clusters, hardware resources, and workload infrastructure resources. Package tag package-2024-10-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.VulnerabilityScanningSettingsPatch` was added

* `models.VulnerabilityScanningSettingsContainerScan` was added

* `models.VulnerabilityScanningSettings` was added

* `models.SecretArchiveSettings` was added

* `models.AnalyticsOutputSettings` was added

#### `models.Cluster` was modified

* `analyticsOutputSettings()` was added
* `secretArchiveSettings()` was added
* `vulnerabilityScanningSettings()` was added

#### `models.ClusterPatchParameters` was modified

* `vulnerabilityScanningSettings()` was added
* `withVulnerabilityScanningSettings(models.VulnerabilityScanningSettingsPatch)` was added
* `withAnalyticsOutputSettings(models.AnalyticsOutputSettings)` was added
* `secretArchiveSettings()` was added
* `analyticsOutputSettings()` was added
* `withSecretArchiveSettings(models.SecretArchiveSettings)` was added

#### `models.VirtualMachine$Definition` was modified

* `withConsoleExtendedLocation(models.ExtendedLocation)` was added

#### `models.VirtualMachine` was modified

* `consoleExtendedLocation()` was added

#### `models.Cluster$Definition` was modified

* `withAnalyticsOutputSettings(models.AnalyticsOutputSettings)` was added
* `withVulnerabilityScanningSettings(models.VulnerabilityScanningSettings)` was added
* `withSecretArchiveSettings(models.SecretArchiveSettings)` was added

#### `models.Cluster$Update` was modified

* `withVulnerabilityScanningSettings(models.VulnerabilityScanningSettingsPatch)` was added
* `withSecretArchiveSettings(models.SecretArchiveSettings)` was added
* `withAnalyticsOutputSettings(models.AnalyticsOutputSettings)` was added

## 1.1.0 (2025-02-21)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the Azure Operator Nexus compute resources such as on-premises clusters, hardware resources, and workload infrastructure resources. Package tag package-2024-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BareMetalMachineKeySets` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.Racks` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.StorageAppliances` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.Clusters` was modified

* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.MetricsConfigurations` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.Volumes` was modified

* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.KubernetesClusters` was modified

* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.Consoles` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`

#### `models.L3Networks` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.CloudServicesNetworks` was modified

* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.ClusterManagers` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.AgentPools` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.BmcKeySets` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.BareMetalMachines` was modified

* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.VirtualMachines` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.TrunkedNetworks` was modified

* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.L2Networks` was modified

* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

### Features Added

* `models.ClusterContinueUpdateVersionParameters` was added

* `models.KubernetesClusterFeatureProvisioningState` was added

* `models.ClusterUpdateStrategyType` was added

* `models.RuntimeProtectionConfiguration` was added

* `models.NodePoolAdministratorConfigurationPatch` was added

* `models.KubernetesClusterFeature$Update` was added

* `models.UserAssignedIdentity` was added

* `models.KubernetesClusterFeatureAvailabilityLifecycle` was added

* `models.AdministratorConfigurationPatch` was added

* `models.IdentitySelector` was added

* `models.ClusterScanRuntimeParametersScanActivity` was added

* `models.L2ServiceLoadBalancerConfiguration` was added

* `models.KubernetesClusterFeatureRequired` was added

* `models.KubernetesClusterFeatureDetailedStatus` was added

* `models.KubernetesClusterFeatures` was added

* `models.ClusterUpdateStrategy` was added

* `models.KubernetesClusterFeaturePatchParameters` was added

* `models.KubernetesClusterFeature$Definition` was added

* `models.KubernetesClusterFeature$UpdateStages` was added

* `models.KubernetesClusterFeature$DefinitionStages` was added

* `models.ManagedServiceIdentitySelectorType` was added

* `models.ManagedServiceIdentity` was added

* `models.RuntimeProtectionEnforcementLevel` was added

* `models.ClusterScanRuntimeParameters` was added

* `models.SecretRotationStatus` was added

* `models.KubernetesClusterFeature` was added

* `models.StringKeyValuePair` was added

* `models.SecretArchiveReference` was added

* `models.KubernetesClusterFeatureList` was added

* `models.CommandOutputSettings` was added

* `models.ClusterContinueUpdateVersionMachineGroupTargetingMode` was added

* `models.RuntimeProtectionStatus` was added

* `models.ClusterSecretArchive` was added

* `models.ManagedServiceIdentityType` was added

* `models.ClusterSecretArchiveEnabled` was added

#### `models.VirtualMachinePatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EndpointDependency` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineKeySetList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachinePowerOffParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineCordonParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Nic` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgentPool$Update` was modified

* `withAdministratorConfiguration(models.NodePoolAdministratorConfigurationPatch)` was added

#### `models.ServicePrincipalInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterMetricsConfigurationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachine` was modified

* `secretRotationStatus()` was added
* `machineClusterVersion()` was added
* `machineRoles()` was added
* `runtimeProtectionStatus()` was added

#### `models.TrunkedNetworkAttachmentConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BmcKeySetList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageApplianceEnableRemoteVendorManagementParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterManagerList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KeySetUserStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterDeployParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterCapacity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterAvailableUpgradeVersion` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MachineDisk` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudServicesNetworkList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineKeySetPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkConfiguration` was modified

* `l2ServiceLoadBalancerConfiguration()` was added
* `withL2ServiceLoadBalancerConfiguration(models.L2ServiceLoadBalancerConfiguration)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IpAddressPool` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgentOptions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RackPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterManagerPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `identity()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.BareMetalMachineCommandSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster` was modified

* `identity()` was added
* `continueUpdateVersion(models.ClusterContinueUpdateVersionParameters)` was added
* `runtimeProtectionConfiguration()` was added
* `updateStrategy()` was added
* `scanRuntime()` was added
* `commandOutputSettings()` was added
* `continueUpdateVersion(models.ClusterContinueUpdateVersionParameters,com.azure.core.util.Context)` was added
* `scanRuntime(models.ClusterScanRuntimeParameters,com.azure.core.util.Context)` was added
* `secretArchive()` was added

#### `models.AadConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineRunReadCommandsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConsolePatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Clusters` was modified

* `scanRuntime(java.lang.String,java.lang.String)` was added
* `scanRuntime(java.lang.String,java.lang.String,models.ClusterScanRuntimeParameters,com.azure.core.util.Context)` was added
* `continueUpdateVersion(java.lang.String,java.lang.String,models.ClusterContinueUpdateVersionParameters,com.azure.core.util.Context)` was added
* `continueUpdateVersion(java.lang.String,java.lang.String,models.ClusterContinueUpdateVersionParameters)` was added

#### `models.HardwareValidationStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ControlPlaneNodeConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BgpAdvertisement` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesLabel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineReplaceParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationStatusResult` was modified

* `outputHead()` was added
* `exitCode()` was added
* `resultRef()` was added
* `resultUrl()` was added

#### `models.BareMetalMachineList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LldpNeighbor` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageAppliance` was modified

* `manufacturer()` was added
* `secretRotationStatus()` was added
* `model()` was added
* `version()` was added

#### `models.AgentPoolPatchParameters` was modified

* `administratorConfiguration()` was added
* `withAdministratorConfiguration(models.NodePoolAdministratorConfigurationPatch)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachinePowerOffParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudServicesNetworkPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageApplianceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidationThreshold` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeatureStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachinePatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtendedLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachine$Definition` was modified

* `withMachineClusterVersion(java.lang.String)` was added

#### `models.TrunkedNetworkList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedResourceGroupConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrunkedNetworkPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HardwareInventory` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageRepositoryCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineRunDataExtractsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineRunCommandParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `secretArchive()` was added
* `withRuntimeProtectionConfiguration(models.RuntimeProtectionConfiguration)` was added
* `withUpdateStrategy(models.ClusterUpdateStrategy)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `commandOutputSettings()` was added
* `updateStrategy()` was added
* `identity()` was added
* `runtimeProtectionConfiguration()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withCommandOutputSettings(models.CommandOutputSettings)` was added
* `withSecretArchive(models.ClusterSecretArchive)` was added

#### `models.OsDisk` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterManager$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.ControlPlaneNodePatchConfiguration` was modified

* `withAdministratorConfiguration(models.AdministratorConfigurationPatch)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `administratorConfiguration()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RackList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.L3NetworkPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `NetworkCloudManager` was modified

* `kubernetesClusterFeatures()` was added

#### `models.BgpServiceLoadBalancerConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdministratorConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumeList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterAvailableVersion` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageApplianceSkuSlot` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AdministrativeCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineConfigurationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BmcKeySetPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SshPublicKey` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RackSkuList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterUpdateVersionParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConsoleList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgentPoolUpgradeSettings` was modified

* `drainTimeout()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withDrainTimeout(java.lang.Long)` was added
* `withMaxUnavailable(java.lang.String)` was added
* `maxUnavailable()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterManager$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.KeySetUser` was modified

* `withUserPrincipalName(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `userPrincipalName()` was added

#### `models.VolumePatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgentPoolList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkAttachment` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HardwareInventoryNetworkInterface` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesCluster$Update` was modified

* `withAdministratorConfiguration(models.AdministratorConfigurationPatch)` was added

#### `models.AvailableUpgrade` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Definition` was modified

* `withRuntimeProtectionConfiguration(models.RuntimeProtectionConfiguration)` was added
* `withCommandOutputSettings(models.CommandOutputSettings)` was added
* `withSecretArchive(models.ClusterSecretArchive)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withUpdateStrategy(models.ClusterUpdateStrategy)` was added

#### `models.KubernetesClusterRestartNodeParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EgressEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.L3NetworkAttachmentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Update` was modified

* `withSecretArchive(models.ClusterSecretArchive)` was added
* `withUpdateStrategy(models.ClusterUpdateStrategy)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withCommandOutputSettings(models.CommandOutputSettings)` was added
* `withRuntimeProtectionConfiguration(models.RuntimeProtectionConfiguration)` was added

#### `models.StorageApplianceConfigurationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AttachedNetworkConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.L3NetworkList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineSkuSlot` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L2NetworkPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageAppliancePatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterPatchParameters` was modified

* `withAdministratorConfiguration(models.AdministratorConfigurationPatch)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `administratorConfiguration()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RackDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachinePlacementHint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterMetricsConfigurationPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.L2NetworkAttachmentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L2NetworkList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterNode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterface` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterManager` was modified

* `identity()` was added

#### `models.ServiceLoadBalancerBgpPeer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InitialAgentPoolConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.1.0-beta.1 (2024-11-21)

- Azure Resource Manager NetworkCloud client library for Java. This package contains Microsoft Azure SDK for NetworkCloud Management SDK. The Network Cloud APIs provide management of the Azure Operator Nexus compute resources such as on-premises clusters, hardware resources, and workload infrastructure resources. Package tag package-2024-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BareMetalMachineKeySets` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.Racks` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.StorageAppliances` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.Clusters` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.MetricsConfigurations` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.Volumes` was modified

* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.KubernetesClusters` was modified

* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.Consoles` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.L3Networks` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.CloudServicesNetworks` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.ClusterManagers` was modified

* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.AgentPools` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

#### `models.BmcKeySets` was modified

* `void delete(java.lang.String,java.lang.String,java.lang.String)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String)`
* `void delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.BareMetalMachines` was modified

* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.VirtualMachines` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`

#### `models.TrunkedNetworks` was modified

* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`
* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`

#### `models.L2Networks` was modified

* `void deleteByResourceGroup(java.lang.String,java.lang.String)` -> `models.OperationStatusResult deleteByResourceGroup(java.lang.String,java.lang.String)`
* `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `void delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.OperationStatusResult delete(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `void deleteById(java.lang.String)` -> `models.OperationStatusResult deleteById(java.lang.String)`

### Features Added

* `models.ClusterContinueUpdateVersionParameters` was added

* `models.KubernetesClusterFeatureProvisioningState` was added

* `models.ClusterUpdateStrategyType` was added

* `models.RuntimeProtectionConfiguration` was added

* `models.NodePoolAdministratorConfigurationPatch` was added

* `models.KubernetesClusterFeature$Update` was added

* `models.UserAssignedIdentity` was added

* `models.KubernetesClusterFeatureAvailabilityLifecycle` was added

* `models.AdministratorConfigurationPatch` was added

* `models.IdentitySelector` was added

* `models.ClusterScanRuntimeParametersScanActivity` was added

* `models.L2ServiceLoadBalancerConfiguration` was added

* `models.KubernetesClusterFeatureRequired` was added

* `models.KubernetesClusterFeatureDetailedStatus` was added

* `models.KubernetesClusterFeatures` was added

* `models.ClusterUpdateStrategy` was added

* `models.KubernetesClusterFeaturePatchParameters` was added

* `models.KubernetesClusterFeature$Definition` was added

* `models.KubernetesClusterFeature$UpdateStages` was added

* `models.KubernetesClusterFeature$DefinitionStages` was added

* `models.ManagedServiceIdentitySelectorType` was added

* `models.ManagedServiceIdentity` was added

* `models.RuntimeProtectionEnforcementLevel` was added

* `models.ClusterScanRuntimeParameters` was added

* `models.SecretRotationStatus` was added

* `models.KubernetesClusterFeature` was added

* `models.StringKeyValuePair` was added

* `models.SecretArchiveReference` was added

* `models.KubernetesClusterFeatureList` was added

* `models.CommandOutputSettings` was added

* `models.ClusterContinueUpdateVersionMachineGroupTargetingMode` was added

* `models.RuntimeProtectionStatus` was added

* `models.ClusterSecretArchive` was added

* `models.ManagedServiceIdentityType` was added

* `models.ClusterSecretArchiveEnabled` was added

#### `models.VirtualMachinePatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EndpointDependency` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineKeySetList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachinePowerOffParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineCordonParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Nic` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentPool$Update` was modified

* `withAdministratorConfiguration(models.NodePoolAdministratorConfigurationPatch)` was added

#### `models.ServicePrincipalInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterMetricsConfigurationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachine` was modified

* `runtimeProtectionStatus()` was added
* `machineRoles()` was added
* `secretRotationStatus()` was added
* `machineClusterVersion()` was added

#### `models.TrunkedNetworkAttachmentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BmcKeySetList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageApplianceEnableRemoteVendorManagementParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterManagerList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KeySetUserStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterDeployParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterCapacity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterAvailableUpgradeVersion` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineDisk` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KubernetesClusterList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudServicesNetworkList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineKeySetPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `l2ServiceLoadBalancerConfiguration()` was added
* `withL2ServiceLoadBalancerConfiguration(models.L2ServiceLoadBalancerConfiguration)` was added

#### `models.IpAddressPool` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RackPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterManagerPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `identity()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.BareMetalMachineCommandSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Cluster` was modified

* `continueUpdateVersion(models.ClusterContinueUpdateVersionParameters)` was added
* `updateStrategy()` was added
* `continueUpdateVersion(models.ClusterContinueUpdateVersionParameters,com.azure.core.util.Context)` was added
* `runtimeProtectionConfiguration()` was added
* `scanRuntime(models.ClusterScanRuntimeParameters,com.azure.core.util.Context)` was added
* `scanRuntime()` was added
* `identity()` was added
* `commandOutputSettings()` was added
* `secretArchive()` was added

#### `models.AadConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachineRunReadCommandsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConsolePatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Clusters` was modified

* `continueUpdateVersion(java.lang.String,java.lang.String,models.ClusterContinueUpdateVersionParameters,com.azure.core.util.Context)` was added
* `scanRuntime(java.lang.String,java.lang.String,models.ClusterScanRuntimeParameters,com.azure.core.util.Context)` was added
* `continueUpdateVersion(java.lang.String,java.lang.String,models.ClusterContinueUpdateVersionParameters)` was added
* `scanRuntime(java.lang.String,java.lang.String)` was added

#### `models.HardwareValidationStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ControlPlaneNodeConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BgpAdvertisement` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesLabel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineReplaceParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationStatusResult` was modified

* `outputHead()` was added
* `resultUrl()` was added
* `exitCode()` was added
* `resultRef()` was added

#### `models.BareMetalMachineList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LldpNeighbor` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageAppliance` was modified

* `version()` was added
* `manufacturer()` was added
* `model()` was added
* `secretRotationStatus()` was added

#### `models.AgentPoolPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `administratorConfiguration()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withAdministratorConfiguration(models.NodePoolAdministratorConfigurationPatch)` was added

#### `models.VirtualMachinePowerOffParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CloudServicesNetworkPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageApplianceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidationThreshold` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FeatureStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachinePatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExtendedLocation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BareMetalMachine$Definition` was modified

* `withMachineClusterVersion(java.lang.String)` was added

#### `models.TrunkedNetworkList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedResourceGroupConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TrunkedNetworkPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HardwareInventory` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageRepositoryCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineRunDataExtractsParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineRunCommandParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterPatchParameters` was modified

* `runtimeProtectionConfiguration()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `identity()` was added
* `withRuntimeProtectionConfiguration(models.RuntimeProtectionConfiguration)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `commandOutputSettings()` was added
* `withSecretArchive(models.ClusterSecretArchive)` was added
* `withUpdateStrategy(models.ClusterUpdateStrategy)` was added
* `secretArchive()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `updateStrategy()` was added
* `withCommandOutputSettings(models.CommandOutputSettings)` was added

#### `models.OsDisk` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterManager$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.ControlPlaneNodePatchConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `administratorConfiguration()` was added
* `withAdministratorConfiguration(models.AdministratorConfigurationPatch)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RackList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.L3NetworkPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `NetworkCloudManager` was modified

* `kubernetesClusterFeatures()` was added

#### `models.BgpServiceLoadBalancerConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdministratorConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VolumeList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterAvailableVersion` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.StorageApplianceSkuSlot` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdministrativeCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VirtualMachineList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BareMetalMachineConfigurationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BmcKeySetPatchParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SshPublicKey` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RackSkuList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterUpdateVersionParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConsoleList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentPoolUpgradeSettings` was modified

* `withMaxUnavailable(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withDrainTimeout(java.lang.Long)` was added
* `drainTimeout()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `maxUnavailable()` was added

#### `models.ClusterManager$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.KeySetUser` was modified

* `userPrincipalName()` was added
* `withUserPrincipalName(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumePatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AgentPoolList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkAttachment` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HardwareInventoryNetworkInterface` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesCluster$Update` was modified

* `withAdministratorConfiguration(models.AdministratorConfigurationPatch)` was added

#### `models.AvailableUpgrade` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Definition` was modified

* `withSecretArchive(models.ClusterSecretArchive)` was added
* `withCommandOutputSettings(models.CommandOutputSettings)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withUpdateStrategy(models.ClusterUpdateStrategy)` was added
* `withRuntimeProtectionConfiguration(models.RuntimeProtectionConfiguration)` was added

#### `models.KubernetesClusterRestartNodeParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EgressEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L3NetworkAttachmentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Cluster$Update` was modified

* `withSecretArchive(models.ClusterSecretArchive)` was added
* `withCommandOutputSettings(models.CommandOutputSettings)` was added
* `withUpdateStrategy(models.ClusterUpdateStrategy)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withRuntimeProtectionConfiguration(models.RuntimeProtectionConfiguration)` was added

#### `models.StorageApplianceConfigurationData` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AttachedNetworkConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L3NetworkList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MachineSkuSlot` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L2NetworkPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageAppliancePatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `administratorConfiguration()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withAdministratorConfiguration(models.AdministratorConfigurationPatch)` was added

#### `models.RackDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachinePlacementHint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ClusterMetricsConfigurationPatchParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.L2NetworkAttachmentConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.L2NetworkList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.KubernetesClusterNode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NetworkInterface` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClusterManager` was modified

* `identity()` was added

#### `models.ServiceLoadBalancerBgpPeer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InitialAgentPoolConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

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
