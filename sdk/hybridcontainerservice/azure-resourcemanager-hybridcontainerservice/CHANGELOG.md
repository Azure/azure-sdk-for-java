# Release History

## 1.0.0-beta.3 (2023-11-15)

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
* `withStatus(models.AgentPoolProvisioningStatusStatus)` was removed
* `withExtendedLocation(models.AgentPoolExtendedLocation)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withOsType(models.OsType)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withVmSize(java.lang.String)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `withCount(java.lang.Integer)` was removed
* `withMode(models.Mode)` was removed
* `withAvailabilityZones(java.util.List)` was removed

#### `models.LinuxProfileProperties` was modified

* `withAdminUsername(java.lang.String)` was removed
* `adminUsername()` was removed

#### `models.NetworkProfile` was modified

* `withLoadBalancerProfile(models.LoadBalancerProfile)` was removed
* `dnsServiceIp()` was removed
* `withServiceCidrs(java.util.List)` was removed
* `serviceCidrs()` was removed
* `withPodCidrs(java.util.List)` was removed
* `withDnsServiceIp(java.lang.String)` was removed
* `withLoadBalancerSku(models.LoadBalancerSku)` was removed
* `models.LoadBalancerProfile loadBalancerProfile()` -> `models.NetworkProfileLoadBalancerProfile loadBalancerProfile()`
* `serviceCidr()` was removed
* `loadBalancerSku()` was removed
* `podCidrs()` was removed
* `withServiceCidr(java.lang.String)` was removed

#### `models.AgentPoolProvisioningStatusStatus` was modified

* `withReadyReplicas(java.lang.Integer)` was removed
* `withReplicas(java.lang.Integer)` was removed
* `replicas()` was removed
* `withProvisioningStatus(models.AgentPoolProvisioningStatusStatusProvisioningStatus)` was removed
* `provisioningStatus()` was removed
* `java.lang.Integer readyReplicas()` -> `java.util.List readyReplicas()`

#### `models.CloudProviderProfile` was modified

* `withInfraStorageProfile(models.CloudProviderProfileInfraStorageProfile)` was removed
* `infraStorageProfile()` was removed

#### `models.ControlPlaneEndpointProfileControlPlaneEndpoint` was modified

* `withPort(java.lang.String)` was removed
* `java.lang.String port()` -> `java.lang.Integer port()`

#### `models.AgentPool` was modified

* `minCount()` was removed
* `nodeLabels()` was removed
* `mode()` was removed
* `maxPods()` was removed
* `resourceGroupName()` was removed
* `maxCount()` was removed
* `models.AgentPoolExtendedLocation extendedLocation()` -> `models.ExtendedLocation extendedLocation()`
* `cloudProviderProfile()` was removed
* `nodeTaints()` was removed
* `models.AgentPoolProvisioningState provisioningState()` -> `models.ResourceProvisioningState provisioningState()`

#### `models.ProvisionedClusterUpgradeProfile` was modified

* `java.lang.String provisioningState()` -> `models.ResourceProvisioningState provisioningState()`

#### `models.HybridIdentityMetadata` was modified

* `update()` was removed
* `java.lang.String provisioningState()` -> `models.ResourceProvisioningState provisioningState()`
* `resourceGroupName()` was removed
* `refresh(com.azure.core.util.Context)` was removed
* `identity()` was removed
* `refresh()` was removed

#### `models.ProvisionedClusters` was modified

* `withLocation(java.lang.String)` was removed
* `withTags(java.util.Map)` was removed
* `models.ProvisionedClustersAllProperties properties()` -> `models.ProvisionedClusterProperties properties()`
* `validate()` was removed
* `withExtendedLocation(models.ProvisionedClustersExtendedLocation)` was removed
* `identity()` was removed
* `com.azure.core.management.SystemData systemData()` -> `com.azure.core.management.SystemData systemData()`
* `models.ProvisionedClustersExtendedLocation extendedLocation()` -> `models.ExtendedLocation extendedLocation()`
* `withIdentity(models.ProvisionedClusterIdentity)` was removed
* `withProperties(models.ProvisionedClustersAllProperties)` was removed

#### `models.ControlPlaneProfile` was modified

* `withMaxCount(java.lang.Integer)` was removed
* `withMode(models.Mode)` was removed
* `withCount(java.lang.Integer)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withVmSize(java.lang.String)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed

#### `models.AgentPool$Definition` was modified

* `withExistingProvisionedCluster(java.lang.String,java.lang.String)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withExtendedLocation(models.AgentPoolExtendedLocation)` was removed
* `withMode(models.Mode)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `withNodeLabels(java.util.Map)` was removed

#### `models.AgentPools` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `listByProvisionedClusterWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByProvisionedCluster(java.lang.String,java.lang.String)` was removed

#### `models.AgentPoolProfile` was modified

* `withNodeLabels(java.util.Map)` was removed
* `withVmSize(java.lang.String)` was removed
* `maxCount()` was removed
* `withMinCount(java.lang.Integer)` was removed
* `minCount()` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `nodeLabels()` was removed
* `maxPods()` was removed
* `nodeTaints()` was removed
* `withCount(java.lang.Integer)` was removed
* `withMode(models.Mode)` was removed
* `cloudProviderProfile()` was removed
* `mode()` was removed
* `count()` was removed
* `withMaxCount(java.lang.Integer)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed
* `vmSize()` was removed

#### `models.AgentPoolProvisioningStatus` was modified

* `models.AgentPoolProvisioningState provisioningState()` -> `models.ResourceProvisioningState provisioningState()`

#### `HybridContainerServiceManager` was modified

* `hybridContainerServices()` was removed
* `provisionedClustersOperations()` was removed
* `virtualNetworksOperations()` was removed
* `storageSpacesOperations()` was removed

#### `models.NamedAgentPoolProfile` was modified

* `withMaxCount(java.lang.Integer)` was removed
* `withCloudProviderProfile(models.CloudProviderProfile)` was removed
* `withNodeTaints(java.util.List)` was removed
* `withMaxPods(java.lang.Integer)` was removed
* `withNodeLabels(java.util.Map)` was removed
* `withVmSize(java.lang.String)` was removed
* `withMode(models.Mode)` was removed
* `withMinCount(java.lang.Integer)` was removed
* `withCount(java.lang.Integer)` was removed

#### `models.VirtualNetworks` was modified

* `extendedLocation()` was removed
* `resourceGroupName()` was removed
* `refresh()` was removed
* `properties()` was removed
* `tags()` was removed
* `region()` was removed
* `id()` was removed
* `update()` was removed
* `innerModel()` was removed
* `name()` was removed
* `regionName()` was removed
* `type()` was removed
* `location()` was removed
* `systemData()` was removed
* `refresh(com.azure.core.util.Context)` was removed

#### `models.HybridIdentityMetadatas` was modified

* `listByCluster(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `listByCluster(java.lang.String,java.lang.String)` was removed
* `define(java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteById(java.lang.String)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ActionType` was added

* `models.VirtualNetworkPropertiesStatusOperationStatusError` was added

* `models.OsSku` was added

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

* `models.KubernetesVersionProfilePropertiesAutoGenerated` was added

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

* `models.VirtualNetworkExtendedLocation` was added

* `models.NetworkProfileLoadBalancerProfile` was added

* `models.AddonStatusProfile` was added

* `models.KubernetesVersionProfile` was added

* `models.VirtualNetwork$Definition` was added

* `models.VmSkuProfilePropertiesAutoGenerated` was added

* `models.Operation` was added

* `models.KubernetesPatchVersions` was added

* `models.VirtualNetwork$Update` was added

#### `models.NetworkProfile` was modified

* `withLoadBalancerProfile(models.NetworkProfileLoadBalancerProfile)` was added

#### `models.AgentPoolProvisioningStatusStatus` was modified

* `operationStatus()` was added
* `withReadyReplicas(java.util.List)` was added
* `withOperationStatus(models.AgentPoolProvisioningStatusOperationStatus)` was added

#### `models.ControlPlaneEndpointProfileControlPlaneEndpoint` was modified

* `withPort(java.lang.Integer)` was added

#### `models.AgentPool` was modified

* `osSku()` was added

#### `models.ProvisionedClusterUpgradeProfile` was modified

* `systemData()` was added

#### `models.ProvisionedClusters` was modified

* `innerModel()` was added
* `type()` was added
* `id()` was added
* `name()` was added

#### `models.ControlPlaneProfile` was modified

* `withOsSku(models.Ossku)` was added

#### `models.AgentPool$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `withOsSku(models.Ossku)` was added
* `withExistingConnectedClusterResourceUri(java.lang.String)` was added

#### `models.AgentPools` was modified

* `get(java.lang.String,java.lang.String)` was added
* `listByProvisionedClusterWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `listByProvisionedCluster(java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AgentPoolProfile` was modified

* `withOsSku(models.Ossku)` was added
* `osSku()` was added

#### `HybridContainerServiceManager` was modified

* `kubernetesVersions()` was added
* `vMSkus()` was added
* `provisionedClusterInstances()` was added
* `resourceProviders()` was added
* `virtualNetworks()` was added

#### `models.NamedAgentPoolProfile` was modified

* `withOsSku(models.Ossku)` was added
* `count()` was added
* `vmSize()` was added

#### `models.VirtualNetworks` was modified

* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was added
* `getByResourceGroup(java.lang.String,java.lang.String)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `list()` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `list(com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String)` was added

#### `models.HybridIdentityMetadatas` was modified

* `delete(java.lang.String,com.azure.core.util.Context)` was added
* `listByCluster(java.lang.String,com.azure.core.util.Context)` was added
* `putWithResponse(java.lang.String,fluent.models.HybridIdentityMetadataInner,com.azure.core.util.Context)` was added
* `put(java.lang.String,fluent.models.HybridIdentityMetadataInner)` was added
* `listByCluster(java.lang.String)` was added
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String)` was added
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
