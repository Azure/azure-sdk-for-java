# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2026-05-08)

- Azure Resource Manager Edge Zones client library for Java. This package contains Microsoft Azure SDK for Edge Zones Management SDK.  Package api-version 2024-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ExtendedZoneListResult` was removed

#### `models.OperationListResult` was removed

#### `EdgeZonesManager` was modified

* `fluent.MicrosoftEdgeZones serviceClient()` -> `fluent.EdgeZonesManagementClient serviceClient()`

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.ExtendedZoneProperties` was modified

* `ExtendedZoneProperties()` was changed to private access
* `validate()` was removed

## 1.0.0-beta.2 (2024-12-04)

- Azure Resource Manager Edge Zones client library for Java. This package contains Microsoft Azure SDK for Edge Zones Management SDK.  Package tag package-2024-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0-beta.1 (2024-04-24)

- Azure Resource Manager Edge Zones client library for Java. This package contains Microsoft Azure SDK for Edge Zones Management SDK.  Package tag package-2024-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

