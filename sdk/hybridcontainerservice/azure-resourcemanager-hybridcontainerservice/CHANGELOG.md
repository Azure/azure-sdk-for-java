# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
