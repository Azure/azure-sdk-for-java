# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2022-04-11)

- Azure Resource Manager IotCentral client library for Java. This package contains Microsoft Azure SDK for IotCentral Management SDK. Use this API to manage IoT Central Applications in your Azure subscription. Package tag package-preview-2021-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.PrivateEndpointConnectionsCreateResponse` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.AppsDeleteHeaders` was added

* `models.AppsCreateOrUpdateHeaders` was added

* `models.NetworkAction` was added

* `models.PrivateLinks` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpointConnections` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.NetworkRuleSetIpRule` was added

* `models.PrivateEndpointConnectionsDeleteHeaders` was added

* `models.PrivateEndpoint` was added

* `models.PrivateEndpointConnectionsCreateHeaders` was added

* `models.AppsUpdateResponse` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateLinkResourceListResult` was added

* `models.AppsCreateOrUpdateResponse` was added

* `models.ProvisioningState` was added

* `models.NetworkRuleSets` was added

* `models.AppsUpdateHeaders` was added

* `models.PrivateEndpointConnectionsDeleteResponse` was added

* `models.AppsDeleteResponse` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.PublicNetworkAccess` was added

#### `models.App$Definition` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withNetworkRuleSets(models.NetworkRuleSets)` was added

#### `IotCentralManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.App` was modified

* `publicNetworkAccess()` was added
* `networkRuleSets()` was added
* `privateEndpointConnections()` was added
* `provisioningState()` was added

#### `models.AppPatch` was modified

* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `withNetworkRuleSets(models.NetworkRuleSets)` was added
* `networkRuleSets()` was added
* `provisioningState()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `IotCentralManager` was modified

* `privateLinks()` was added
* `privateEndpointConnections()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Apps` was modified

* `update(java.lang.String,java.lang.String,models.AppPatch,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,models.AppPatch)` was added

#### `models.App$Update` was modified

* `withNetworkRuleSets(models.NetworkRuleSets)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

## 1.0.0 (2021-11-15)

- Azure Resource Manager IotCentral client library for Java. This package contains Microsoft Azure SDK for IotCentral Management SDK. Use this API to manage IoT Central Applications in your Azure subscription. Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-management`.

## 1.0.0-beta.2 (2021-10-18)

- Azure Resource Manager IotCentral client library for Java. This package contains Microsoft Azure SDK for IotCentral Management SDK. Use this API to manage IoT Central Applications in your Azure subscription. Package tag package-2021-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.CloudErrorBody` was removed

#### `models.App$Update` was modified

* `withTemplate(java.lang.String)` was removed

### Features Added

* `models.SystemAssignedServiceIdentityType` was added

* `models.SystemAssignedServiceIdentity` was added

* `models.AppState` was added

#### `models.App$Definition` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was added

#### `IotCentralManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.App` was modified

* `state()` was added
* `identity()` was added

#### `models.AppPatch` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was added
* `identity()` was added
* `state()` was added

#### `models.App$Update` was modified

* `withIdentity(models.SystemAssignedServiceIdentity)` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager IotCentral client library for Java. This package contains Microsoft Azure SDK for IotCentral Management SDK. Use this API to manage IoT Central Applications in your Azure subscription. Package tag package-2018-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
