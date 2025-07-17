# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2025-06-30)

- Azure Resource Manager StorageActions client library for Java. This package contains Microsoft Azure SDK for StorageActions Management SDK. The Azure Storage Actions Management API. Package api-version 2023-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.StorageTaskReportSummary` was removed

#### `models.StorageTasksListResult` was removed

#### `models.StorageTaskAssignmentsListResult` was removed

#### `models.OperationListResult` was removed

#### `models.StorageTask$Update` was modified

* `withProperties(models.StorageTaskProperties)` was removed

#### `models.StorageTaskUpdateParameters` was modified

* `models.StorageTaskProperties properties()` -> `models.StorageTaskUpdateProperties properties()`
* `withProperties(models.StorageTaskProperties)` was removed

#### `models.UserAssignedIdentity` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `java.util.UUID principalId()` -> `java.lang.String principalId()`

#### `StorageActionsManager` was modified

* `fluent.StorageActionsMgmtClient serviceClient()` -> `fluent.StorageActionsManagementClient serviceClient()`

#### `models.ManagedServiceIdentity` was modified

* `java.util.UUID principalId()` -> `java.lang.String principalId()`
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

### Features Added

* `implementation.models.OperationListResult` was added

* `implementation.models.StorageTaskReportSummary` was added

* `models.StorageTaskUpdateProperties` was added

* `implementation.models.StorageTasksListResult` was added

* `implementation.models.StorageTaskAssignmentsListResult` was added

#### `models.StorageTask$Update` was modified

* `withProperties(models.StorageTaskUpdateProperties)` was added

#### `models.StorageTaskUpdateParameters` was modified

* `withProperties(models.StorageTaskUpdateProperties)` was added

## 1.0.0-beta.3 (2025-04-18)

- Azure Resource Manager StorageActions client library for Java. This package contains Microsoft Azure SDK for StorageActions Management SDK. The Azure Storage Actions Management API. Package tag package-2023-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ProvisioningState` was modified

* `models.ProvisioningState[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed
* `toString()` was removed

#### `models.OnFailure` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.OnFailure[] values()` -> `java.util.Collection values()`

#### `models.OnSuccess` was modified

* `models.OnSuccess[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

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

