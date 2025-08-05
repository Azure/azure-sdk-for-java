# Release History

## 1.2.0-beta.2 (2025-07-21)

- Azure Resource Manager Dashboard client library for Java. This package contains Microsoft Azure SDK for Dashboard Management SDK. The Microsoft.Dashboard Rest API spec. Package api-version 2024-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Bugs Fixed

- Fixed bug on update of `ManagedGrafana`.

## 1.2.0-beta.1 (2025-07-14)

- Azure Resource Manager Dashboard client library for Java. This package contains Microsoft Azure SDK for Dashboard Management SDK. The Microsoft.Dashboard Rest API spec. Package api-version 2024-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.GrafanasUpdateResponse` was removed

#### `models.GrafanasUpdateHeaders` was removed

#### `models.ManagedPrivateEndpointModelListResponse` was removed

#### `models.ManagedGrafanaListResponse` was removed

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.OperationListResult` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.SaasSubscriptionDetails` was modified

* `withPublisherId(java.lang.String)` was removed
* `withOfferId(java.lang.String)` was removed
* `withTerm(models.SubscriptionTerm)` was removed
* `withPlanId(java.lang.String)` was removed

#### `models.MarketplaceTrialQuota` was modified

* `withAvailablePromotion(models.AvailablePromotion)` was removed
* `withTrialStartAt(java.time.OffsetDateTime)` was removed
* `withTrialEndAt(java.time.OffsetDateTime)` was removed
* `withGrafanaResourceId(java.lang.String)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID clientId()` -> `java.lang.String clientId()`

#### `models.SubscriptionTerm` was modified

* `withStartDate(java.time.OffsetDateTime)` was removed
* `withEndDate(java.time.OffsetDateTime)` was removed
* `withTermUnit(java.lang.String)` was removed

### Features Added

* `models.UnifiedAlertingScreenshots` was added

* `models.ManagedDashboards` was added

* `models.IntegrationFabricProperties` was added

* `models.ManagedDashboard$DefinitionStages` was added

* `models.IntegrationFabricPropertiesUpdateParameters` was added

* `models.IntegrationFabric$DefinitionStages` was added

* `models.Security` was added

* `models.IntegrationFabric$Update` was added

* `models.Users` was added

* `models.ManagedDashboard$UpdateStages` was added

* `models.IntegrationFabric$Definition` was added

* `models.ManagedDashboard` was added

* `models.ManagedDashboard$Definition` was added

* `models.IntegrationFabricUpdateParameters` was added

* `models.IntegrationFabrics` was added

* `models.ManagedDashboardUpdateParameters` was added

* `models.IntegrationFabric` was added

* `models.Snapshots` was added

* `models.IntegrationFabric$UpdateStages` was added

* `models.ManagedDashboard$Update` was added

#### `DashboardManager` was modified

* `managedDashboards()` was added
* `integrationFabrics()` was added

#### `models.GrafanaConfigurations` was modified

* `withSecurity(models.Security)` was added
* `withUsers(models.Users)` was added
* `snapshots()` was added
* `unifiedAlertingScreenshots()` was added
* `withUnifiedAlertingScreenshots(models.UnifiedAlertingScreenshots)` was added
* `withSnapshots(models.Snapshots)` was added
* `security()` was added
* `users()` was added

## 1.1.0 (2024-12-13)

- Azure Resource Manager Dashboard client library for Java. This package contains Microsoft Azure SDK for Dashboard Management SDK. The Microsoft.Dashboard Rest API spec. Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2023-11-16)

- Azure Resource Manager Dashboard client library for Java. This package contains Microsoft Azure SDK for Dashboard Management SDK. The Microsoft.Dashboard Rest API spec. Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.GrafanasUpdateResponse` was added

* `models.ManagedPrivateEndpointConnectionState` was added

* `models.Smtp` was added

* `models.GrafanasUpdateHeaders` was added

* `models.EnterpriseConfigurations` was added

* `models.ManagedPrivateEndpointModelListResponse` was added

* `models.ManagedPrivateEndpointModel$DefinitionStages` was added

* `models.ManagedPrivateEndpointModel$UpdateStages` was added

* `models.MarketplaceAutoRenew` was added

* `models.SaasSubscriptionDetails` was added

* `models.ManagedPrivateEndpointModel$Update` was added

* `models.MarketplaceTrialQuota` was added

* `models.GrafanaAvailablePluginListResponse` was added

* `models.ManagedPrivateEndpointUpdateParameters` was added

* `models.ManagedPrivateEndpointModel` was added

* `models.EnterpriseDetails` was added

* `models.ManagedPrivateEndpoints` was added

* `models.GrafanaPlugin` was added

* `models.ManagedPrivateEndpointConnectionStatus` was added

* `models.StartTlsPolicy` was added

* `models.AvailablePromotion` was added

* `models.GrafanaAvailablePlugin` was added

* `models.GrafanaConfigurations` was added

* `models.ManagedPrivateEndpointModel$Definition` was added

* `models.SubscriptionTerm` was added

#### `models.ManagedGrafana$Update` was modified

* `withSku(models.ResourceSku)` was added

#### `DashboardManager` was modified

* `managedPrivateEndpoints()` was added

#### `models.Grafanas` was modified

* `fetchAvailablePluginsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `checkEnterpriseDetailsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `fetchAvailablePlugins(java.lang.String,java.lang.String)` was added
* `checkEnterpriseDetails(java.lang.String,java.lang.String)` was added

#### `models.ManagedGrafana` was modified

* `fetchAvailablePlugins()` was added
* `checkEnterpriseDetails()` was added
* `fetchAvailablePluginsWithResponse(com.azure.core.util.Context)` was added
* `checkEnterpriseDetailsWithResponse(com.azure.core.util.Context)` was added

#### `models.ManagedGrafanaProperties` was modified

* `grafanaConfigurations()` was added
* `withGrafanaMajorVersion(java.lang.String)` was added
* `enterpriseConfigurations()` was added
* `grafanaPlugins()` was added
* `withEnterpriseConfigurations(models.EnterpriseConfigurations)` was added
* `withGrafanaConfigurations(models.GrafanaConfigurations)` was added
* `grafanaMajorVersion()` was added
* `withGrafanaPlugins(java.util.Map)` was added

#### `models.ManagedGrafanaUpdateParameters` was modified

* `sku()` was added
* `withSku(models.ResourceSku)` was added

#### `models.ManagedGrafanaPropertiesUpdateParameters` was modified

* `enterpriseConfigurations()` was added
* `grafanaMajorVersion()` was added
* `grafanaPlugins()` was added
* `withGrafanaMajorVersion(java.lang.String)` was added
* `grafanaConfigurations()` was added
* `withGrafanaConfigurations(models.GrafanaConfigurations)` was added
* `withGrafanaPlugins(java.util.Map)` was added
* `withEnterpriseConfigurations(models.EnterpriseConfigurations)` was added

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
