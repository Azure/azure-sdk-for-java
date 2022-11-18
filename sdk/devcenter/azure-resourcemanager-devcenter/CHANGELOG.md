# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2022-11-18)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-preview-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationStatusError` was removed

#### `models.Schedules` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate,java.lang.Integer,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate)` was removed

#### `models.Schedule$Update` was modified

* `withTypePropertiesType(models.ScheduledType)` was removed

#### `models.OperationStatus` was modified

* `models.OperationStatusError error()` -> `models.ErrorDetail error()`

#### `DevCenterManager` was modified

* `fluent.DevCenterClient serviceClient()` -> `fluent.DevCenterManagementClient serviceClient()`

### Features Added

* `models.ErrorAdditionalInfo` was added

* `models.CatalogSyncState` was added

* `models.OperationStatusResult` was added

* `models.ErrorDetail` was added

#### `models.Catalog` was modified

* `syncState()` was added

#### `models.Schedule$Update` was modified

* `withTags(java.util.Map)` was added
* `withType(models.ScheduledType)` was added

#### `models.OperationStatus` was modified

* `operations()` was added

## 1.0.0-beta.2 (2022-10-12)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2022-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Schedules` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate,java.lang.Integer)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)` was removed

### Features Added

* `models.ProjectAllowedEnvironmentTypes` was added

* `models.AllowedEnvironmentTypeListResult` was added

* `models.AllowedEnvironmentType` was added

#### `models.DevCenter` was modified

* `systemData()` was added

#### `models.Image` was modified

* `systemData()` was added

#### `models.OperationStatus` was modified

* `resourceId()` was added

#### `DevCenterManager` was modified

* `projectAllowedEnvironmentTypes()` was added

## 1.0.0-beta.1 (2022-08-19)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2022-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
