# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
