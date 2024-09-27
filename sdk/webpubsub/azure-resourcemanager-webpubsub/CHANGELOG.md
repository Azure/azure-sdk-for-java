# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-09-26)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2024-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.IpRule` was added

* `models.ServiceKind` was added

* `models.Replica$UpdateStages` was added

* `models.Replica` was added

* `models.ReplicaList` was added

* `models.Replica$DefinitionStages` was added

* `models.WebPubSubSocketIOSettings` was added

* `models.WebPubSubReplicaSharedPrivateLinkResources` was added

* `models.Replica$Update` was added

* `models.WebPubSubReplicas` was added

* `models.Replica$Definition` was added

#### `models.Dimension` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EventHandler` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetricSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomCertificateList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LiveTraceCategory` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebPubSubHubList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointAcl` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogSpecification` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIdentitySettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentityProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EventListenerEndpoint` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.ShareablePrivateLinkResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkServiceConnectionState` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebPubSubResource` was modified

* `socketIO()` was added
* `kind()` was added
* `resourceStopped()` was added
* `regionEndpointEnabled()` was added

#### `models.ResourceSku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `WebPubSubManager` was modified

* `webPubSubReplicaSharedPrivateLinkResources()` was added
* `webPubSubReplicas()` was added

#### `models.PrivateEndpoint` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NameAvailabilityParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpstreamAuthSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NetworkAcl` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EventNameFilter` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomDomainList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ShareablePrivateLinkResourceType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceLogConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebPubSubResource$Definition` was modified

* `withSocketIO(models.WebPubSubSocketIOSettings)` was added
* `withResourceStopped(java.lang.String)` was added
* `withKind(models.ServiceKind)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.WebPubSubs` was modified

* `listReplicaSkus(java.lang.String,java.lang.String,java.lang.String)` was added
* `listReplicaSkusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.EventHubEndpoint` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LiveTraceConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebPubSubHubProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `webSocketKeepAliveIntervalInSeconds()` was added
* `withWebSocketKeepAliveIntervalInSeconds(java.lang.Integer)` was added

#### `models.SkuCapacity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegenerateKeyParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebPubSubResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EventListener` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SignalRServiceUsageName` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebPubSubNetworkACLs` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `ipRules()` was added
* `withIpRules(java.util.List)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceLogCategory` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SignalRServiceUsageList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EventListenerFilter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.PrivateEndpointConnectionList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SharedPrivateLinkResourceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebPubSubResource$Update` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added
* `withSocketIO(models.WebPubSubSocketIOSettings)` was added

#### `models.WebPubSubTlsSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.ServiceSpecification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.1.0-beta.2 (2023-10-26)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2023-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.IpRule` was added

#### `models.CustomCertificate` was modified

* `systemData()` was added

#### `models.Replica` was modified

* `regionEndpointEnabled()` was added
* `resourceStopped()` was added

#### `models.WebPubSubResource` was modified

* `regionEndpointEnabled()` was added
* `resourceStopped()` was added

#### `models.WebPubSubHub` was modified

* `systemData()` was added

#### `models.WebPubSubResource$Definition` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.CustomDomain` was modified

* `systemData()` was added

#### `models.Replica$Update` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.SharedPrivateLinkResource` was modified

* `systemData()` was added

#### `models.WebPubSubNetworkACLs` was modified

* `withIpRules(java.util.List)` was added
* `ipRules()` was added

#### `models.WebPubSubResource$Update` was modified

* `withRegionEndpointEnabled(java.lang.String)` was added
* `withResourceStopped(java.lang.String)` was added

#### `models.Replica$Definition` was modified

* `withResourceStopped(java.lang.String)` was added
* `withRegionEndpointEnabled(java.lang.String)` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

## 1.1.0-beta.1 (2023-07-21)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2023-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CustomCertificate` was modified

* `systemData()` was removed

#### `models.WebPubSubHub` was modified

* `systemData()` was removed

#### `models.CustomDomain` was modified

* `systemData()` was removed

#### `models.SharedPrivateLinkResource` was modified

* `systemData()` was removed

### Features Added

* `models.ServiceKind` was added

* `models.Replica$UpdateStages` was added

* `models.Replica` was added

* `models.ReplicaList` was added

* `models.Replica$DefinitionStages` was added

* `models.Replica$Update` was added

* `models.WebPubSubReplicas` was added

* `models.Replica$Definition` was added

#### `models.WebPubSubResource` was modified

* `kind()` was added

#### `WebPubSubManager` was modified

* `webPubSubReplicas()` was added

#### `models.WebPubSubResource$Definition` was modified

* `withKind(models.ServiceKind)` was added

#### `models.WebPubSubs` was modified

* `listReplicaSkusWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listReplicaSkus(java.lang.String,java.lang.String,java.lang.String)` was added

## 1.0.0 (2023-03-23)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.4 (2023-03-22)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2023-02-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.3 (2022-11-18)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2022-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CustomCertificate` was added

* `models.CustomCertificateList` was added

* `models.EventListenerEndpointDiscriminator` was added

* `models.EventListenerFilterDiscriminator` was added

* `models.EventListenerEndpoint` was added

* `models.CustomDomain$Update` was added

* `models.CustomCertificate$DefinitionStages` was added

* `models.EventNameFilter` was added

* `models.CustomDomain$Definition` was added

* `models.CustomDomainList` was added

* `models.CustomDomain$DefinitionStages` was added

* `models.CustomCertificate$Definition` was added

* `models.EventHubEndpoint` was added

* `models.CustomDomain` was added

* `models.ResourceReference` was added

* `models.CustomDomain$UpdateStages` was added

* `models.CustomCertificate$Update` was added

* `models.CustomCertificate$UpdateStages` was added

* `models.WebPubSubCustomCertificates` was added

* `models.EventListener` was added

* `models.WebPubSubCustomDomains` was added

* `models.EventListenerFilter` was added

#### `models.WebPubSubResource` was modified

* `resourceGroupName()` was added

#### `WebPubSubManager` was modified

* `webPubSubCustomDomains()` was added
* `webPubSubCustomCertificates()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.WebPubSubHub` was modified

* `resourceGroupName()` was added

#### `WebPubSubManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.WebPubSubHubProperties` was modified

* `eventListeners()` was added
* `withEventListeners(java.util.List)` was added

#### `models.SharedPrivateLinkResource` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.2 (2021-10-14)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.EventHandlerTemplate` was removed

* `models.DiagnosticConfiguration` was removed

* `models.EventHandlerSettings` was removed

#### `models.WebPubSubResource` was modified

* `eventHandler()` was removed
* `diagnosticConfiguration()` was removed

#### `models.WebPubSubResource$Definition` was modified

* `withDiagnosticConfiguration(models.DiagnosticConfiguration)` was removed
* `withEventHandler(models.EventHandlerSettings)` was removed

#### `models.WebPubSubResource$Update` was modified

* `withEventHandler(models.EventHandlerSettings)` was removed
* `withDiagnosticConfiguration(models.DiagnosticConfiguration)` was removed

### Features Added

* `models.EventHandler` was added

* `models.LiveTraceCategory` was added

* `models.WebPubSubHubs` was added

* `models.WebPubSubHubList` was added

* `models.WebPubSubHub$UpdateStages` was added

* `models.ScaleType` was added

* `models.WebPubSubHub$Update` was added

* `models.WebPubSubHub$DefinitionStages` was added

* `models.WebPubSubHub` was added

* `models.ResourceLogConfiguration` was added

* `models.SkuList` was added

* `models.LiveTraceConfiguration` was added

* `models.WebPubSubHubProperties` was added

* `models.SkuCapacity` was added

* `models.WebPubSubHub$Definition` was added

* `models.Sku` was added

* `models.ResourceLogCategory` was added

#### `models.WebPubSubResource` was modified

* `hostnamePrefix()` was added
* `liveTraceConfiguration()` was added
* `resourceLogConfiguration()` was added

#### `WebPubSubManager` was modified

* `webPubSubHubs()` was added

#### `models.WebPubSubResource$Definition` was modified

* `withResourceLogConfiguration(models.ResourceLogConfiguration)` was added
* `withLiveTraceConfiguration(models.LiveTraceConfiguration)` was added

#### `models.WebPubSubs` was modified

* `listSkusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSkus(java.lang.String,java.lang.String)` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `models.WebPubSubResource$Update` was modified

* `withLiveTraceConfiguration(models.LiveTraceConfiguration)` was added
* `withResourceLogConfiguration(models.ResourceLogConfiguration)` was added

## 1.0.0-beta.1 (2021-07-09)

- Azure Resource Manager WebPubSub client library for Java. This package contains Microsoft Azure SDK for WebPubSub Management SDK. REST API for Azure WebPubSub Service. Package tag package-2021-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
