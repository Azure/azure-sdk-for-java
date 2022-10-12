# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
