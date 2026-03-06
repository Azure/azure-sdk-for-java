# Release History

## 1.1.0 (2026-03-06)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package api-version 2024-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RelayNamespaceListResult` was removed

#### `models.HybridConnectionListResult` was removed

#### `models.WcfRelaysListResult` was removed

#### `models.ProvisioningStateEnum` was removed

#### `models.AuthorizationRuleListResult` was removed

#### `models.OperationListResult` was removed

#### `models.WcfRelays` was removed

#### `models.AccessRights` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.AccessRights[] values()` -> `java.util.Collection values()`

#### `models.Sku` was modified

* `java.lang.String name()` -> `models.SkuName name()`
* `validate()` was removed

#### `models.ResourceNamespacePatch` was modified

* `validate()` was removed

#### `models.RelayNamespace` was modified

* `models.ProvisioningStateEnum provisioningState()` -> `java.lang.String provisioningState()`

#### `models.UnavailableReason` was modified

* `models.UnavailableReason[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed
* `toString()` was removed

#### `models.CheckNameAvailability` was modified

* `validate()` was removed

#### `models.RegenerateAccessKeyParameters` was modified

* `validate()` was removed

#### `models.SkuTier` was modified

* `models.SkuTier[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed
* `toString()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.RelayUpdateParameters` was modified

* `models.ProvisioningStateEnum provisioningState()` -> `java.lang.String provisioningState()`
* `validate()` was removed

#### `models.KeyType` was modified

* `toString()` was removed
* `models.KeyType[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `RelayManager` was modified

* `wcfRelays()` was removed
* `fluent.RelayApi serviceClient()` -> `fluent.RelayManagementClient serviceClient()`

### Features Added

* `models.NetworkRuleSet` was added

* `models.NWRuleSetIpRules` was added

* `models.PrivateLinkConnectionStatus` was added

* `models.DefaultAction` was added

* `models.PublicNetworkAccess` was added

* `models.PrivateLinkResource` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.NetworkRuleIPAction` was added

* `models.ActionType` was added

* `models.WCFRelays` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.EndPointProvisioningState` was added

* `models.PrivateLinkResources` was added

* `models.ConnectionState` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpoint` was added

* `models.PrivateLinkResourcesListResult` was added

* `models.SkuName` was added

* `models.Origin` was added

* `models.PrivateEndpointConnection` was added

* `models.PrivateEndpointConnections` was added

#### `models.AccessRights` was modified

* `AccessRights()` was added

#### `models.Namespaces` was modified

* `getNetworkRuleSetWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateNetworkRuleSet(java.lang.String,java.lang.String,fluent.models.NetworkRuleSetInner)` was added
* `createOrUpdateNetworkRuleSetWithResponse(java.lang.String,java.lang.String,fluent.models.NetworkRuleSetInner,com.azure.core.util.Context)` was added
* `getNetworkRuleSet(java.lang.String,java.lang.String)` was added

#### `models.Sku` was modified

* `withName(models.SkuName)` was added

#### `models.ResourceNamespacePatch` was modified

* `systemData()` was added

#### `models.RelayNamespace` was modified

* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `systemData()` was added
* `status()` was added

#### `models.UnavailableReason` was modified

* `UnavailableReason()` was added

#### `models.HybridConnection` was modified

* `region()` was added
* `regionName()` was added
* `location()` was added
* `systemData()` was added

#### `models.SkuTier` was modified

* `SkuTier()` was added

#### `models.OperationDisplay` was modified

* `description()` was added

#### `models.Operation` was modified

* `actionType()` was added
* `origin()` was added
* `isDataAction()` was added

#### `models.WcfRelay` was modified

* `location()` was added
* `systemData()` was added
* `region()` was added
* `regionName()` was added

#### `models.RelayUpdateParameters` was modified

* `privateEndpointConnections()` was added
* `withPrivateEndpointConnections(java.util.List)` was added
* `publicNetworkAccess()` was added
* `systemData()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `status()` was added

#### `models.KeyType` was modified

* `KeyType()` was added

#### `RelayManager` was modified

* `wCFRelays()` was added
* `privateLinkResources()` was added
* `privateEndpointConnections()` was added

#### `models.RelayNamespace$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withPrivateEndpointConnections(java.util.List)` was added

#### `models.RelayNamespace$Definition` was modified

* `withPrivateEndpointConnections(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.AuthorizationRule` was modified

* `location()` was added
* `regionName()` was added
* `systemData()` was added
* `region()` was added

## 1.0.0 (2024-12-20)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Sku` was modified

* `withName(java.lang.String)` was removed

## 1.0.0-beta.3 (2024-10-10)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.RelayUpdateParameters` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `name()` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceNamespacePatch` was modified

* `id()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.RelayNamespaceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WcfRelaysListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckNameAvailability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegenerateAccessKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthorizationRuleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.2 (2022-07-19)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.RelayNamespace` was modified

* `resourceGroupName()` was added

#### `RelayManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.HybridConnection` was modified

* `resourceGroupName()` was added

#### `RelayManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.AuthorizationRule` was modified

* `regenerateKeys(models.RegenerateAccessKeyParameters)` was added
* `resourceGroupName()` was added
* `listKeys()` was added
* `listKeysWithResponse(com.azure.core.util.Context)` was added
* `regenerateKeysWithResponse(models.RegenerateAccessKeyParameters,com.azure.core.util.Context)` was added

#### `models.WcfRelay` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2020-12-18)

- Azure Resource Manager Relay client library for Java. This package contains Microsoft Azure SDK for Relay Management SDK. Use these API to manage Azure Relay resources through Azure Resource Manager. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
