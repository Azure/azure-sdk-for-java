# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2024-12-03)

- Azure Resource Manager StorageActions client library for Java. This package contains Microsoft Azure SDK for StorageActions Management SDK. The Azure Storage Actions Management API. Package tag package-2023-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

#### `models.StorageTask$DefinitionStages` was modified

* Required stage 3, 4 was added

#### `models.StorageTasksReports` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.StorageTaskAssignments` was modified

* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.StorageTasksReports` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.StorageTaskAssignments` was modified

* `list(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2024-03-21)

- Azure Resource Manager StorageActions client library for Java. This package contains Microsoft Azure SDK for StorageActions Management SDK. The Azure Storage Actions Management API. Package tag package-2023-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

