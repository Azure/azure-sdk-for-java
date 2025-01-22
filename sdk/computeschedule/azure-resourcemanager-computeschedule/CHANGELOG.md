# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2025-01-22)

- Azure Resource Manager Compute Schedule client library for Java. This package contains Microsoft Azure SDK for Compute Schedule Management SDK. Microsoft.ComputeSchedule Resource Provider management API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OperationErrorDetails` was modified

* `java.time.OffsetDateTime errorDetails()` -> `java.lang.String errorDetails()`

#### `ComputeScheduleManager` was modified

* `fluent.ComputeScheduleClient serviceClient()` -> `fluent.ComputeScheduleMgmtClient serviceClient()`

### Features Added

#### `models.OperationErrorDetails` was modified

* `azureOperationName()` was added
* `timestamp()` was added

#### `models.ResourceOperationDetails` was modified

* `timezone()` was added

#### `models.Schedule` was modified

* `timezone()` was added
* `withTimezone(java.lang.String)` was added
* `withDeadline(java.time.OffsetDateTime)` was added
* `deadline()` was added

## 1.0.0-beta.1 (2024-09-25)

- Azure Resource Manager Compute Schedule client library for Java. This package contains Microsoft Azure SDK for Compute Schedule Management SDK. Microsoft.ComputeSchedule Resource Provider management API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-computeschedule Java SDK.
