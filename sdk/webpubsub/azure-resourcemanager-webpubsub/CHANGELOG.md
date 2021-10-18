# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
