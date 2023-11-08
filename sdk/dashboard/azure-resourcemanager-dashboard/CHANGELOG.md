# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-08-19)

- Azure Resource Manager Dashboard client library for Java. This package contains Microsoft Azure SDK for Dashboard Management SDK. The Microsoft.Dashboard Rest API spec. Package tag package-2022-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.IdentityType` was removed

* `models.OperationResult` was removed

* `models.ManagedIdentity` was removed

#### `models.ManagedGrafana$Definition` was modified

* `withIdentity(models.ManagedIdentity)` was removed

#### `models.ManagedGrafana$Update` was modified

* `withIdentity(models.ManagedIdentity)` was removed

#### `models.ManagedGrafana` was modified

* `models.ManagedIdentity identity()` -> `models.ManagedServiceIdentity identity()`

#### `models.ManagedGrafanaProperties` was modified

* `withProvisioningState(models.ProvisioningState)` was removed

#### `models.ManagedGrafanaUpdateParameters` was modified

* `models.ManagedIdentity identity()` -> `models.ManagedServiceIdentity identity()`
* `withIdentity(models.ManagedIdentity)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.lang.String principalId()` -> `java.util.UUID principalId()`
* `java.lang.String clientId()` -> `java.util.UUID clientId()`

### Features Added

* `models.AzureMonitorWorkspaceIntegration` was added

* `models.PrivateEndpointConnections` was added

* `models.GrafanaIntegrations` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.PublicNetworkAccess` was added

* `models.ApiKey` was added

* `models.PrivateEndpoint` was added

* `models.Operation` was added

* `models.PrivateLinkResource` was added

* `models.ManagedServiceIdentity` was added

* `models.PrivateEndpointConnection` was added

* `models.ManagedServiceIdentityType` was added

* `models.PrivateLinkResourceListResult` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.DeterministicOutboundIp` was added

* `models.ManagedGrafanaPropertiesUpdateParameters` was added

#### `DashboardManager` was modified

* `privateLinkResources()` was added
* `privateEndpointConnections()` was added

#### `models.ManagedGrafana$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.ManagedGrafana$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withProperties(models.ManagedGrafanaPropertiesUpdateParameters)` was added

#### `models.ManagedGrafana` was modified

* `resourceGroupName()` was added

#### `models.ManagedGrafanaProperties` was modified

* `deterministicOutboundIp()` was added
* `grafanaIntegrations()` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `apiKey()` was added
* `withApiKey(models.ApiKey)` was added
* `withDeterministicOutboundIp(models.DeterministicOutboundIp)` was added
* `withGrafanaIntegrations(models.GrafanaIntegrations)` was added
* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `outboundIPs()` was added

#### `models.ManagedGrafanaUpdateParameters` was modified

* `withProperties(models.ManagedGrafanaPropertiesUpdateParameters)` was added
* `properties()` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 1.0.0-beta.1 (2022-04-11)

- Azure Resource Manager Dashboard client library for Java. This package contains Microsoft Azure SDK for Dashboard Management SDK. The Microsoft.Dashboard Rest API spec. Package tag package-2021-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
