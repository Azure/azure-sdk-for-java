# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-12-12)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.OutboundEnvironmentEndpointCollection` was removed

#### `models.QuotaResourceList` was removed

#### `models.LoadTestResourcePageList` was removed

### Features Added

* `models.LoadTestResourceListResult` was added

* `models.QuotaResourceListResult` was added

* `models.PagedOutboundEnvironmentEndpoint` was added

## 1.0.0 (2023-02-01)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.1 (2023-01-10)

- Azure Resource Manager LoadTest client library for Java. This package contains Microsoft Azure SDK for LoadTest Management SDK. LoadTest client provides access to LoadTest Resource and it's status operations. Package tag package-2022-12-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
