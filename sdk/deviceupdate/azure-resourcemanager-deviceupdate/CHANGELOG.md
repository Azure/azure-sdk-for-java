# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-12-13)

- Azure Resource Manager DeviceUpdate client library for Java. This package contains Microsoft Azure SDK for DeviceUpdate Management SDK. Microsoft Device Update resource provider. Package tag package-2023-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.PrivateLinkServiceProxyRemotePrivateEndpointConnection` was modified

* `id()` was added

## 1.0.0 (2023-12-19)

- Azure Resource Manager DeviceUpdate client library for Java. This package contains Microsoft Azure SDK for DeviceUpdate Management SDK. Microsoft Device Update resource provider. Package tag package-2023-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.Encryption` was added

#### `models.Account` was modified

* `encryption()` was added
* `systemData()` was added

#### `models.Account$Definition` was modified

* `withEncryption(models.Encryption)` was added

#### `models.GroupInformation` was modified

* `systemData()` was added

## 1.0.0-beta.2 (2022-08-17)

- Azure Resource Manager DeviceUpdate client library for Java. This package contains Microsoft Azure SDK for DeviceUpdate Management SDK. Microsoft Device Update resource provider. Package tag package-2022-10-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2022-05-25)

- Azure Resource Manager DeviceUpdate client library for Java. This package contains Microsoft Azure SDK for DeviceUpdate Management SDK. Microsoft Device Update resource provider. Package tag package-2022-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
