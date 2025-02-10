# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-12-06)

- Azure Resource Manager MigrationDiscoverySap client library for Java. This package contains Microsoft Azure SDK for MigrationDiscoverySap Management SDK. Migration Discovery SAP Client. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.ExcelPerformanceData` was modified

* `dataSource()` was added

#### `models.PerformanceData` was modified

* `dataSource()` was added

#### `models.NativePerformanceData` was modified

* `dataSource()` was added

## 1.0.0-beta.1 (2024-04-09)

- Azure Resource Manager MigrationDiscoverySap client library for Java. This package contains Microsoft Azure SDK for MigrationDiscoverySap Management SDK. Migration Discovery SAP Client. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

