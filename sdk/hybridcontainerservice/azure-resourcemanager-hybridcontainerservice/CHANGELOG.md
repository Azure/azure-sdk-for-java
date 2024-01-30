# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-01-24)

- Azure Resource Manager HybridContainerService client library for Java. This package contains Microsoft Azure SDK for HybridContainerService Management SDK. The Microsoft.HybridContainerService Rest API spec. Package tag package-2024-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.KubernetesVersionCapabilities` was removed

* `models.ProvisionedClustersListResult` was removed

* `models.LinuxProfile` was removed

* `models.VirtualNetworkPropertiesInfraVnetProfileVmware` was removed

* `models.AgentPoolProvisioningStatusOperationStatusError` was removed

* `models.ControlPlaneEndpointProfile` was removed

* `models.AgentPoolProvisioningStatusOperationStatus` was removed

* `models.ControlPlaneEndpointProfileControlPlaneEndpoint` was removed

* `models.ProvisionedClusterPropertiesStatusOperationStatus` was removed

* `models.ProvisionedClusters` was removed

* `models.AgentPoolPatch` was removed

* `models.ProvisionedClusterPropertiesStatusOperationStatusError` was removed

#### `models.AgentPool$DefinitionStages` was modified

* `withRegion(com.azure.core.management.Region)` was removed in stage 1
* `withRegion(java.lang.String)` was removed in stage 1

#### `models.VirtualNetworkPropertiesStatusOperationStatus` was modified

* `phase()` was removed
* `withPhase(java.lang.String)` was removed

#### `models.AgentPool$Definition` was modified

* `withRegion(com.azure.core.management.Region)` was removed
* `withStatus(models.AgentPoolProvisioningStatusStatus)` was removed
* `withOsType(models.OsType)` was removed
* `withOsSku(models.Ossku)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `withRegion(java.lang.String)` was removed
* `withVmSize(java.lang.String)` was removed
* `withAvailabilityZones(java.util.List)` was removed
* `withCount(java.lang.Integer)` was removed

#### `models.ProvisionedClusterInstances` was modified

* `models.ProvisionedClusters get(java.lang.String)` -> `models.ProvisionedCluster get(java.lang.String)`
* `createOrUpdate(java.lang.String,fluent.models.ProvisionedClustersInner,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,fluent.models.ProvisionedClustersInner)` was removed

#### `models.VirtualNetworkProperties` was modified

* `dhcpServers()` was removed
* `withDhcpServers(java.util.List)` was removed

#### `models.AgentPools` was modified

* `listByProvisionedClusterWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `models.AgentPoolListResult listByProvisionedCluster(java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listByProvisionedCluster(java.lang.String)`

#### `models.AgentPoolProvisioningStatusStatus` was modified

* `withOperationStatus(models.AgentPoolProvisioningStatusOperationStatus)` was removed
* `operationStatus()` was removed

#### `models.KubernetesVersionProperties` was modified

* `capabilities()` was removed

#### `models.VirtualNetworkPropertiesInfraVnetProfile` was modified

* `withVmware(models.VirtualNetworkPropertiesInfraVnetProfileVmware)` was removed
* `vmware()` was removed

#### `models.VirtualNetworkExtendedLocation` was modified

* `java.lang.String type()` -> `models.ExtendedLocationTypes type()`
* `withType(java.lang.String)` was removed

#### `models.AgentPoolProfile` was modified

* `withAvailabilityZones(java.util.List)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `nodeImageVersion()` was removed
* `availabilityZones()` was removed

#### `models.ProvisionedClusterPropertiesStatus` was modified

* `withOperationStatus(models.ProvisionedClusterPropertiesStatusOperationStatus)` was removed
* `operationStatus()` was removed

#### `models.AgentPool` was modified

* `osType()` was removed
* `provisioningState()` was removed
* `vmSize()` was removed
* `availabilityZones()` was removed
* `count()` was removed
* `location()` was removed
* `regionName()` was removed
* `nodeImageVersion()` was removed
* `region()` was removed
* `osSku()` was removed
* `status()` was removed

#### `models.NamedAgentPoolProfile` was modified

* `withNodeImageVersion(java.lang.String)` was removed
* `withAvailabilityZones(java.util.List)` was removed

#### `models.ProvisionedClusterUpgradeProfile` was modified

* `controlPlaneProfile()` was removed
* `provisioningState()` was removed
* `agentPoolProfiles()` was removed

#### `models.HybridIdentityMetadata` was modified

* `resourceUid()` was removed
* `provisioningState()` was removed
* `publicKey()` was removed

#### `models.ControlPlaneProfile` was modified

* `withCount(java.lang.Integer)` was removed
* `withAvailabilityZones(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `withOsSku(models.Ossku)` was removed
* `linuxProfile()` was removed
* `withOsType(models.OsType)` was removed
* `withLinuxProfile(models.LinuxProfileProperties)` was removed
* `withControlPlaneEndpoint(models.ControlPlaneEndpointProfileControlPlaneEndpoint)` was removed
* `models.ControlPlaneEndpointProfileControlPlaneEndpoint controlPlaneEndpoint()` -> `models.ControlPlaneProfileControlPlaneEndpoint controlPlaneEndpoint()`
* `withVmSize(java.lang.String)` was removed

#### `models.AgentPoolListResult` was modified

* `java.lang.String nextLink()` -> `java.lang.String nextLink()`
* `innerModel()` was removed
* `java.util.List value()` -> `java.util.List value()`

#### `models.ProvisionedClusterPoolUpgradeProfile` was modified

* `name()` was removed

### Features Added

* `models.StorageProfile` was added

* `models.ProvisionedClusterListResult` was added

* `models.ProvisionedClusterPropertiesAutoScalerProfile` was added

* `models.ProvisionedClusterUpgradeProfileProperties` was added

* `models.StorageProfileNfsCsiDriver` was added

* `models.AgentPoolProperties` was added

* `models.ClusterVMAccessProfile` was added

* `models.Expander` was added

* `models.StorageProfileSmbCsiDriver` was added

* `models.ProvisionedCluster` was added

* `models.ControlPlaneProfileControlPlaneEndpoint` was added

* `models.HybridIdentityMetadataProperties` was added

#### `models.AgentPool$Update` was modified

* `withProperties(models.AgentPoolProperties)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.AgentPool$Definition` was modified

* `withProperties(models.AgentPoolProperties)` was added

#### `models.ProvisionedClusterInstances` was modified

* `createOrUpdate(java.lang.String,fluent.models.ProvisionedClusterInner,com.azure.core.util.Context)` was added
* `createOrUpdate(java.lang.String,fluent.models.ProvisionedClusterInner)` was added

#### `models.ProvisionedClusterProperties` was modified

* `autoScalerProfile()` was added
* `clusterVMAccessProfile()` was added
* `storageProfile()` was added
* `withAutoScalerProfile(models.ProvisionedClusterPropertiesAutoScalerProfile)` was added
* `withStorageProfile(models.StorageProfile)` was added
* `withClusterVMAccessProfile(models.ClusterVMAccessProfile)` was added

#### `models.AgentPools` was modified

* `listByProvisionedCluster(java.lang.String,com.azure.core.util.Context)` was added

#### `models.AgentPoolProvisioningStatusStatus` was modified

* `currentState()` was added

#### `models.VirtualNetworkExtendedLocation` was modified

* `withType(models.ExtendedLocationTypes)` was added

#### `models.AgentPoolProfile` was modified

* `maxCount()` was added
* `withMinCount(java.lang.Integer)` was added
* `maxPods()` was added
* `nodeTaints()` was added
* `withEnableAutoScaling(java.lang.Boolean)` was added
* `withNodeTaints(java.util.List)` was added
* `withNodeLabels(java.util.Map)` was added
* `minCount()` was added
* `withMaxCount(java.lang.Integer)` was added
* `enableAutoScaling()` was added
* `nodeLabels()` was added
* `withMaxPods(java.lang.Integer)` was added

#### `models.ProvisionedClusterPropertiesStatus` was modified

* `currentState()` was added

#### `models.AgentPool` was modified

* `properties()` was added

#### `models.NamedAgentPoolProfile` was modified

* `withMinCount(java.lang.Integer)` was added
* `withMaxCount(java.lang.Integer)` was added
* `withNodeLabels(java.util.Map)` was added
* `withMaxPods(java.lang.Integer)` was added
* `withEnableAutoScaling(java.lang.Boolean)` was added
* `kubernetesVersion()` was added
* `withNodeTaints(java.util.List)` was added

#### `models.ProvisionedClusterUpgradeProfile` was modified

* `properties()` was added

#### `models.HybridIdentityMetadata` was modified

* `properties()` was added

#### `models.AgentPoolUpdateProfile` was modified

* `kubernetesVersion()` was added

#### `models.ControlPlaneProfile` was modified

* `withControlPlaneEndpoint(models.ControlPlaneProfileControlPlaneEndpoint)` was added
* `vmSize()` was added
* `count()` was added

#### `models.AgentPoolListResult` was modified

* `validate()` was added
* `withNextLink(java.lang.String)` was added
* `withValue(java.util.List)` was added

## 1.0.0-beta.3 (2023-11-23)

- Azure Resource Manager HybridContainerService client library for Java. This package contains Microsoft Azure SDK for HybridContainerService Management SDK. The Microsoft.HybridContainerService Rest API spec. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.StorageSpaces` was removed

* `models.StorageSpacesListResult` was removed

* `models.StorageSpacesPropertiesStatus` was removed

* `models.ProvisionedClustersAllProperties` was removed

* `models.AadProfileResponse` was removed

* `models.WindowsProfileResponse` was removed

* `models.ResourceProviderOperation` was removed

* `models.WindowsProfile` was removed

* `models.VirtualNetworksProperties` was removed

* `models.OrchestratorVersionProfile` was removed

* `models.AgentPoolProvisioningStatusError` was removed

* `models.ProvisionedClustersCommonPropertiesStatusProvisioningStatus` was removed

* `models.VirtualNetworksPropertiesStatus` was removed

* `models.ProvisionedClustersCommonPropertiesStatusProvisioningStatusError` was removed

* `models.CloudProviderProfileInfraStorageProfile` was removed

* `models.AadProfile` was removed

* `models.VirtualNetworksPropertiesInfraVnetProfileNetworkCloud` was removed

* `models.AddonProfiles` was removed

* `models.AgentPoolExtendedLocation` was removed

* `models.HybridIdentityMetadata$UpdateStages` was removed

* `models.StorageSpacesExtendedLocation` was removed

* `models.VirtualNetworks$UpdateStages` was removed

* `models.AutoUpgradeOptions` was removed

* `models.ProvisionedClustersPatch` was removed

* `models.HybridIdentityMetadata$Definition` was removed

* `models.VirtualNetworksPropertiesInfraVnetProfileVmware` was removed

* `models.ProvisionedClustersResponse` was removed

* `models.LicenseType` was removed

* `models.ProvisionedClustersResponse$DefinitionStages` was removed

* `models.ProvisionedClustersResponseListResult` was removed

* `models.ProvisionedClustersCommonPropertiesStatus` was removed

* `models.ProvisionedClustersResponse$Update` was removed

* `models.VirtualNetworksPropertiesInfraVnetProfileHci` was removed

* `models.ProvisionedClustersCommonProperties` was removed

* `models.ProvisionedClustersExtendedLocation` was removed

* `models.VirtualNetworksPropertiesInfraVnetProfile` was removed

* `models.VirtualNetworks$Definition` was removed

* `models.VirtualNetworksPropertiesVmipPoolItem` was removed

* `models.HybridContainerServices` was removed

* `models.StorageSpacesOperations` was removed

* `models.VirtualNetworks$Update` was removed

* `models.ProvisionedClustersResponse$UpdateStages` was removed

* `models.HttpProxyConfigResponse` was removed

* `models.ProvisionedClustersResponseProperties` was removed

* `models.StorageSpaces$Update` was removed

* `models.Mode` was removed

* `models.VirtualNetworksPropertiesVipPoolItem` was removed

* `models.ProvisionedClustersPropertiesWithoutSecrets` was removed

* `models.VirtualNetworksExtendedLocation` was removed

* `models.ProvisionedClustersResponseExtendedLocation` was removed

* `models.ArcAgentProfile` was removed

* `models.ResourceIdentityType` was removed

* `models.StorageSpaces$DefinitionStages` was removed

* `models.StorageSpacesProperties` was removed

* `models.HybridIdentityMetadata$DefinitionStages` was removed

* `models.VirtualNetworksPropertiesStatusProvisioningStatus` was removed

* `models.HybridIdentityMetadata$Update` was removed

* `models.LoadBalancerSku` was removed

* `models.StorageSpaces$UpdateStages` was removed

* `models.ProvisionedClustersCommonPropertiesStatusFeaturesStatus` was removed

* `models.LoadBalancerProfile` was removed

* `models.WindowsProfilePassword` was removed

* `models.ArcAgentStatus` was removed

* `models.ProvisionedClustersPropertiesWithSecrets` was removed

* `models.StorageSpacesPropertiesStatusProvisioningStatusError` was removed

* `models.HttpProxyConfigPassword` was removed

* `models.DeploymentState` was removed

* `models.AadProfileSecret` was removed

* `models.StorageSpaces$Definition` was removed

* `models.StorageSpacesPropertiesStatusProvisioningStatus` was removed

* `models.AddonStatus` was removed

* `models.ResourceProviderOperationList` was removed

* `models.VirtualNetworks$DefinitionStages` was removed

* `models.AgentPoolProvisioningState` was removed

* `models.VirtualNetworksPropertiesStatusProvisioningStatusError` was removed

* `models.AgentPoolProvisioningStatusStatusProvisioningStatus` was removed

* `models.VMSkuListResult` was removed

* `models.StorageSpacesPropertiesVmwareStorageProfile` was removed

* `models.VirtualNetworksOperations` was removed

* `models.ResourceProviderOperationDisplay` was removed

* `models.HttpProxyConfig` was removed

* `models.OrchestratorVersionProfileListResult` was removed

* `models.OrchestratorProfile` was removed

* `models.StorageSpacesPatch` was removed

* `models.ProvisionedClustersCommonPropertiesFeatures` was removed

* `models.ProvisionedClustersResponse$Definition` was removed

* `models.ProvisionedClusterIdentity` was removed

* `models.StorageSpacesPropertiesHciStorageProfile` was removed

* `models.ProvisionedClustersOperations` was removed

#### `models.AgentPool$DefinitionStages` was modified

* `withExistingProvisionedCluster(java.lang.String,java.lang.String)` was removed in stage 2

#### `models.AgentPool$Update` was modified

* `withCloudProviderProfile(models.CloudProviderProfile)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withOsType(models.OsType)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withCount(java.lang.Integer)` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `withVmSize(java.lang.String)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withExtendedLocation(models.AgentPoolExtendedLocation)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `withAvailabilityZones(java.util.List)` was removed
* `withStatus(models.AgentPoolProvisioningStatusStatus)` was removed
* `withMode(models.Mode)` was removed

#### `models.LinuxProfileProperties` was modified

* `withAdminUsername(java.lang.String)` was removed
* `adminUsername()` was removed

#### `models.NetworkProfile` was modified

* `withServiceCidrs(java.util.List)` was removed
* `withPodCidrs(java.util.List)` was removed
* `serviceCidrs()` was removed
* `podCidrs()` was removed
* `models.LoadBalancerProfile loadBalancerProfile()` -> `models.NetworkProfileLoadBalancerProfile loadBalancerProfile()`
* `serviceCidr()` was removed
* `withServiceCidr(java.lang.String)` was removed
* `dnsServiceIp()` was removed
* `withLoadBalancerProfile(models.LoadBalancerProfile)` was removed
* `withLoadBalancerSku(models.LoadBalancerSku)` was removed
* `withDnsServiceIp(java.lang.String)` was removed
* `loadBalancerSku()` was removed

#### `models.AgentPoolProvisioningStatusStatus` was modified

* `withReadyReplicas(java.lang.Integer)` was removed
* `provisioningStatus()` was removed
* `withReplicas(java.lang.Integer)` was removed
* `java.lang.Integer readyReplicas()` -> `java.util.List readyReplicas()`
* `withProvisioningStatus(models.AgentPoolProvisioningStatusStatusProvisioningStatus)` was removed
* `replicas()` was removed

#### `models.CloudProviderProfile` was modified

* `infraStorageProfile()` was removed
* `withInfraStorageProfile(models.CloudProviderProfileInfraStorageProfile)` was removed

#### `models.ControlPlaneEndpointProfileControlPlaneEndpoint` was modified

* `java.lang.String port()` -> `java.lang.Integer port()`
* `withPort(java.lang.String)` was removed

#### `models.AgentPool` was modified

* `nodeTaints()` was removed
* `mode()` was removed
* `resourceGroupName()` was removed
* `models.AgentPoolProvisioningState provisioningState()` -> `models.ResourceProvisioningState provisioningState()`
* `cloudProviderProfile()` was removed
* `maxPods()` was removed
* `minCount()` was removed
* `nodeLabels()` was removed
* `maxCount()` was removed
* `models.AgentPoolExtendedLocation extendedLocation()` -> `models.ExtendedLocation extendedLocation()`

#### `models.ProvisionedClusterUpgradeProfile` was modified

* `java.lang.String provisioningState()` -> `models.ResourceProvisioningState provisioningState()`

#### `models.HybridIdentityMetadata` was modified

* `update()` was removed
* `resourceGroupName()` was removed
* `refresh(com.azure.core.util.Context)` was removed
* `refresh()` was removed
* `identity()` was removed
* `java.lang.String provisioningState()` -> `models.ResourceProvisioningState provisioningState()`

#### `models.ProvisionedClusters` was modified

* `models.ProvisionedClustersExtendedLocation extendedLocation()` -> `models.ExtendedLocation extendedLocation()`
* `withLocation(java.lang.String)` was removed
* `validate()` was removed
* `identity()` was removed
* `withExtendedLocation(models.ProvisionedClustersExtendedLocation)` was removed
* `models.ProvisionedClustersAllProperties properties()` -> `models.ProvisionedClusterProperties properties()`
* `com.azure.core.management.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`
* `withProperties(models.ProvisionedClustersAllProperties)` was removed
* `withTags(java.util.Map)` was removed
* `withIdentity(models.ProvisionedClusterIdentity)` was removed

#### `models.ControlPlaneProfile` was modified

* `withMaxCount(java.lang.Integer)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withMode(models.Mode)` was removed
* `withVmSize(java.lang.String)` was removed
* `withCount(java.lang.Integer)` was removed

#### `models.AgentPool$Definition` was modified

* `withMaxPods(java.lang.Integer)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withExtendedLocation(models.AgentPoolExtendedLocation)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withExistingProvisionedCluster(java.lang.String,java.lang.String)` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withMode(models.Mode)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed

#### `models.AgentPools` was modified

* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByProvisionedClusterWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `listByProvisionedCluster(java.lang.String,java.lang.String)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AgentPoolProfile` was modified

* `withNodeLabels(java.util.Map)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed
* `mode()` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `minCount()` was removed
* `nodeLabels()` was removed
* `maxPods()` was removed
* `nodeTaints()` was removed
* `cloudProviderProfile()` was removed
* `count()` was removed
* `vmSize()` was removed
* `withMode(models.Mode)` was removed
* `withVmSize(java.lang.String)` was removed
* `maxCount()` was removed
* `withNodeTaints(java.util.List)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withCount(java.lang.Integer)` was removed

#### `models.AgentPoolProvisioningStatus` was modified

* `models.AgentPoolProvisioningState provisioningState()` -> `models.ResourceProvisioningState provisioningState()`

#### `HybridContainerServiceManager` was modified

* `hybridContainerServices()` was removed
* `provisionedClustersOperations()` was removed
* `virtualNetworksOperations()` was removed
* `storageSpacesOperations()` was removed

#### `models.NamedAgentPoolProfile` was modified

* `withNodeTaints(java.util.List)` was removed
* `withVmSize(java.lang.String)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withMode(models.Mode)` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `withCount(java.lang.Integer)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed

#### `models.VirtualNetworks` was modified

* `innerModel()` was removed
* `name()` was removed
* `region()` was removed
* `refresh()` was removed
* `id()` was removed
* `systemData()` was removed
* `location()` was removed
* `properties()` was removed
* `extendedLocation()` was removed
* `type()` was removed
* `resourceGroupName()` was removed
* `update()` was removed
* `refresh(com.azure.core.util.Context)` was removed
* `tags()` was removed
* `regionName()` was removed

#### `models.HybridIdentityMetadatas` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByCluster(java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `define(java.lang.String)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed

### Features Added

* `models.VmSkuProperties` was added

* `models.ActionType` was added

* `models.VirtualNetworkPropertiesStatusOperationStatusError` was added

* `models.VmSkuProfileList` was added

* `models.VirtualNetworkPropertiesVipPoolItem` was added

* `models.VirtualNetwork$DefinitionStages` was added

* `models.VirtualNetworkPropertiesInfraVnetProfileVmware` was added

* `models.ProvisionedClusterProperties` was added

* `models.KubernetesVersions` was added

* `models.KubernetesVersionProfileProperties` was added

* `models.VmSkuCapabilities` was added

* `models.AgentPoolProvisioningStatusOperationStatusError` was added

* `models.VMSkus` was added

* `models.ResourceProvisioningState` was added

* `models.VirtualNetworkPropertiesVmipPoolItem` was added

* `models.VirtualNetworkPropertiesInfraVnetProfileHci` was added

* `models.VirtualNetworkPropertiesInfraVnetProfile` was added

* `models.ProvisionedClusterPropertiesStatus` was added

* `models.AgentPoolProvisioningStatusOperationStatus` was added

* `models.ListCredentialResponseProperties` was added

* `models.OperationDisplay` was added

* `models.ExtendedLocation` was added

* `models.KubernetesVersionReadiness` was added

* `models.ProvisionedClusterPropertiesStatusOperationStatus` was added

* `models.ResourceProviders` was added

* `models.VirtualNetwork` was added

* `models.AgentPoolUpdateProfile` was added

* `models.VirtualNetwork$UpdateStages` was added

* `models.VmSkuProfile` was added

* `models.CredentialResult` was added

* `models.AgentPoolPatch` was added

* `models.ProvisionedClusterPropertiesStatusOperationStatusError` was added

* `models.AzureHybridBenefit` was added

* `models.AddonPhase` was added

* `models.Ossku` was added

* `models.KubernetesVersionCapabilities` was added

* `models.VirtualNetworkPropertiesStatusOperationStatus` was added

* `models.OperationListResult` was added

* `models.ProvisionedClustersListResult` was added

* `models.VmSkuProfileProperties` was added

* `models.VirtualNetworkPropertiesStatus` was added

* `models.ProvisionedClusterInstances` was added

* `models.VirtualNetworkProperties` was added

* `models.KubernetesVersionProfileList` was added

* `models.ListCredentialResponseError` was added

* `models.Origin` was added

* `models.ListCredentialResponse` was added

* `models.ExtendedLocationTypes` was added

* `models.ProvisionedClusterLicenseProfile` was added

* `models.KubernetesVersionProperties` was added

* `models.VirtualNetworkExtendedLocation` was added

* `models.NetworkProfileLoadBalancerProfile` was added

* `models.AddonStatusProfile` was added

* `models.KubernetesVersionProfile` was added

* `models.VirtualNetwork$Definition` was added

* `models.Operation` was added

* `models.KubernetesPatchVersions` was added

* `models.VirtualNetwork$Update` was added

#### `models.NetworkProfile` was modified

* `withLoadBalancerProfile(models.NetworkProfileLoadBalancerProfile)` was added

#### `models.AgentPoolProvisioningStatusStatus` was modified

* `withReadyReplicas(java.util.List)` was added
* `withOperationStatus(models.AgentPoolProvisioningStatusOperationStatus)` was added
* `operationStatus()` was added

#### `models.ControlPlaneEndpointProfileControlPlaneEndpoint` was modified

* `withPort(java.lang.Integer)` was added

#### `models.AgentPool` was modified

* `osSku()` was added

#### `models.ProvisionedClusterUpgradeProfile` was modified

* `systemData()` was added

#### `models.ProvisionedClusters` was modified

* `type()` was added
* `innerModel()` was added
* `name()` was added
* `id()` was added

#### `models.ControlPlaneProfile` was modified

* `withOsSku(models.Ossku)` was added

#### `models.AgentPool$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `withOsSku(models.Ossku)` was added
* `withExistingConnectedClusterResourceUri(java.lang.String)` was added

#### `models.AgentPools` was modified

* `listByProvisionedCluster(java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `listByProvisionedClusterWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AgentPoolProfile` was modified

* `withOsSku(models.Ossku)` was added
* `osSku()` was added

#### `HybridContainerServiceManager` was modified

* `kubernetesVersions()` was added
* `vMSkus()` was added
* `virtualNetworks()` was added
* `resourceProviders()` was added
* `provisionedClusterInstances()` was added

#### `models.NamedAgentPoolProfile` was modified

* `withOsSku(models.Ossku)` was added
* `vmSize()` was added
* `count()` was added

#### `models.VirtualNetworks` was modified

* `getByResourceGroup(java.lang.String,java.lang.String)` was added
* `list()` was added
* `define(java.lang.String)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `getById(java.lang.String)` was added
* `listByResourceGroup(java.lang.String)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `list(com.azure.core.util.Context)` was added

#### `models.HybridIdentityMetadatas` was modified

* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listByCluster(java.lang.String)` was added
* `put(java.lang.String,fluent.models.HybridIdentityMetadataInner)` was added
* `get(java.lang.String)` was added
* `delete(java.lang.String,com.azure.core.util.Context)` was added
* `putWithResponse(java.lang.String,fluent.models.HybridIdentityMetadataInner,com.azure.core.util.Context)` was added
* `listByCluster(java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String)` was added

## 1.0.0-beta.2 (2023-03-14)

- Azure Resource Manager HybridContainerService client library for Java. This package contains Microsoft Azure SDK for HybridContainerService Management SDK. The Microsoft.HybridContainerService Rest API spec. Package tag package-preview-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.VirtualNetworksPropertiesInfraVnetProfileKubevirt` was removed

#### `models.VirtualNetworksPropertiesInfraVnetProfile` was modified

* `withKubevirt(models.VirtualNetworksPropertiesInfraVnetProfileKubevirt)` was removed
* `kubevirt()` was removed

### Features Added

* `models.VirtualNetworksPropertiesInfraVnetProfileNetworkCloud` was added

* `models.ProvisionedClusterUpgradeProfile` was added

* `models.ProvisionedClusterPoolUpgradeProfileProperties` was added

* `models.ProvisionedClusterPoolUpgradeProfile` was added

#### `models.VirtualNetworksProperties` was modified

* `withIpAddressPrefix(java.lang.String)` was added
* `withDnsServers(java.util.List)` was added
* `withGateway(java.lang.String)` was added

#### `models.ProvisionedClustersResponse` was modified

* `upgradeNodeImageVersionForEntireCluster(com.azure.core.util.Context)` was added
* `upgradeNodeImageVersionForEntireCluster()` was added

#### `models.VirtualNetworksPropertiesInfraVnetProfile` was modified

* `networkCloud()` was added
* `withNetworkCloud(models.VirtualNetworksPropertiesInfraVnetProfileNetworkCloud)` was added

#### `models.ProvisionedClustersOperations` was modified

* `upgradeNodeImageVersionForEntireCluster(java.lang.String,java.lang.String)` was added
* `upgradeNodeImageVersionForEntireCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getUpgradeProfileWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getUpgradeProfile(java.lang.String,java.lang.String)` was added

## 1.0.0-beta.1 (2022-10-12)

- Azure Resource Manager HybridContainerService client library for Java. This package contains Microsoft Azure SDK for HybridContainerService Management SDK. The Microsoft.HybridContainerService Rest API spec. Package tag package-2022-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
