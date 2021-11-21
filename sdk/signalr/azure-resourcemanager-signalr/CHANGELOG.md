# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2021-11-11)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2021-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ResourceLogConfiguration` was added

* `models.Sku` was added

* `models.SkuCapacity` was added

* `models.ResourceLogCategory` was added

* `models.SkuList` was added

* `models.ScaleType` was added

#### `models.SignalRs` was modified

* `listSkusWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listSkus(java.lang.String,java.lang.String)` was added

#### `models.PrivateEndpointConnection` was modified

* `groupIds()` was added

#### `models.SignalRResource$Definition` was modified

* `withResourceLogConfiguration(models.ResourceLogConfiguration)` was added

#### `models.SignalRResource` was modified

* `hostnamePrefix()` was added
* `resourceLogConfiguration()` was added

#### `models.SignalRResource$Update` was modified

* `withResourceLogConfiguration(models.ResourceLogConfiguration)` was added

## 1.0.0-beta.2 (2021-07-09)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2021-06-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `SignalRManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.SignalRResource$Definition` was modified

* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withPublicNetworkAccess(java.lang.String)` was added
* `withDisableAadAuth(java.lang.Boolean)` was added

#### `models.SignalRResource` was modified

* `disableLocalAuth()` was added
* `publicNetworkAccess()` was added
* `disableAadAuth()` was added

#### `models.SignalRResource$Update` was modified

* `withDisableAadAuth(java.lang.Boolean)` was added
* `withPublicNetworkAccess(java.lang.String)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager SignalR client library for Java. This package contains Microsoft Azure SDK for SignalR Management SDK. REST API for Azure SignalR Service. Package tag package-2021-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
