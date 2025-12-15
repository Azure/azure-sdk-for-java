# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2025-07-24)

- Azure Resource Manager Compute Schedule client library for Java. This package contains Microsoft Azure SDK for Compute Schedule Management SDK. Microsoft.ComputeSchedule Resource Provider management API. Package api-version 2025-04-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.OccurrenceProperties` was added

* `models.InnerError` was added

* `models.OccurrenceExtensions` was added

* `models.OccurrenceResultSummary` was added

* `models.ScheduledActionUpdateProperties` was added

* `models.ResourceResultSummary` was added

* `models.ResourceType` was added

* `models.Occurrence` was added

* `models.WeekDay` was added

* `models.Error` was added

* `models.Language` was added

* `models.ScheduledActionResources` was added

* `models.Month` was added

* `models.OccurrenceExtensionResource` was added

* `models.ScheduledAction$UpdateStages` was added

* `models.OccurrenceResource` was added

* `models.ResourceOperationStatus` was added

* `models.NotificationProperties` was added

* `models.ResourceProvisioningState` was added

* `models.ScheduledActionProperties` was added

* `models.Occurrences` was added

* `models.ScheduledAction` was added

* `models.ScheduledActionType` was added

* `models.ScheduledAction$Update` was added

* `models.ScheduledActionExtensions` was added

* `models.OccurrenceExtensionProperties` was added

* `models.DelayRequest` was added

* `models.NotificationType` was added

* `models.ResourcePatchRequest` was added

* `models.ScheduledAction$Definition` was added

* `models.ScheduledActionsSchedule` was added

* `models.ResourceAttachRequest` was added

* `models.OccurrenceState` was added

* `models.ProvisioningState` was added

* `models.ResourceDetachRequest` was added

* `models.CancelOccurrenceRequest` was added

* `models.ResourceStatus` was added

* `models.ScheduledAction$DefinitionStages` was added

* `models.ScheduledActionResource` was added

* `models.ScheduledActionUpdate` was added

* `models.RecurringActionsResourceOperationResult` was added

#### `models.ScheduledActions` was modified

* `cancelNextOccurrence(java.lang.String,java.lang.String,models.CancelOccurrenceRequest)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByResourceGroup(java.lang.String)` was added
* `patchResources(java.lang.String,java.lang.String,models.ResourcePatchRequest)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `list()` was added
* `getByResourceGroup(java.lang.String,java.lang.String)` was added
* `patchResourcesWithResponse(java.lang.String,java.lang.String,models.ResourcePatchRequest,com.azure.core.util.Context)` was added
* `list(com.azure.core.util.Context)` was added
* `listResources(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `triggerManualOccurrenceWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `triggerManualOccurrence(java.lang.String,java.lang.String)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `attachResources(java.lang.String,java.lang.String,models.ResourceAttachRequest)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `disable(java.lang.String,java.lang.String)` was added
* `listByResourceGroup(java.lang.String,com.azure.core.util.Context)` was added
* `disableWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `attachResourcesWithResponse(java.lang.String,java.lang.String,models.ResourceAttachRequest,com.azure.core.util.Context)` was added
* `enable(java.lang.String,java.lang.String)` was added
* `detachResourcesWithResponse(java.lang.String,java.lang.String,models.ResourceDetachRequest,com.azure.core.util.Context)` was added
* `detachResources(java.lang.String,java.lang.String,models.ResourceDetachRequest)` was added
* `enableWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `cancelNextOccurrenceWithResponse(java.lang.String,java.lang.String,models.CancelOccurrenceRequest,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listResources(java.lang.String,java.lang.String)` was added

#### `ComputeScheduleManager` was modified

* `occurrences()` was added
* `occurrenceExtensions()` was added
* `scheduledActionExtensions()` was added

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
