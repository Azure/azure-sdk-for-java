# Release History

## 1.1.0 (2025-06-04)

- Azure Resource Manager Compute Schedule client library for Java. This package contains Microsoft Azure SDK for Compute Schedule Management SDK. Microsoft.ComputeSchedule Resource Provider management API. Package api-version 2025-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ResourceProvisionPayload` was added

* `models.CreateResourceOperationResponse` was added

* `models.ExecuteDeleteRequest` was added

* `models.ExecuteCreateRequest` was added

* `models.DeleteResourceOperationResponse` was added

#### `models.ScheduledActions` was modified

* `virtualMachinesExecuteCreate(java.lang.String,models.ExecuteCreateRequest)` was added
* `virtualMachinesExecuteCreateWithResponse(java.lang.String,models.ExecuteCreateRequest,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteDelete(java.lang.String,models.ExecuteDeleteRequest)` was added
* `virtualMachinesExecuteDeleteWithResponse(java.lang.String,models.ExecuteDeleteRequest,com.azure.core.util.Context)` was added

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
