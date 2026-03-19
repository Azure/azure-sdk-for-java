# Release History

## 1.2.0-beta.2 (2026-03-19)

- Azure Resource Manager Compute Schedule client library for Java. This package contains Microsoft Azure SDK for Compute Schedule Management SDK. Microsoft.ComputeSchedule Resource Provider management API. Package api-version 2026-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SubmitDeallocateRequest` was removed

#### `models.ExecuteDeleteRequest` was removed

#### `models.ExecuteStartRequest` was removed

#### `models.ExecuteDeallocateRequest` was removed

#### `models.SubmitHibernateRequest` was removed

#### `models.CancelOperationsRequest` was removed

#### `models.ExecuteCreateRequest` was removed

#### `models.GetOperationErrorsRequest` was removed

#### `models.GetOperationStatusRequest` was removed

#### `models.ExecuteHibernateRequest` was removed

#### `models.SubmitStartRequest` was removed

#### `models.ResourceProvisionPayload` was modified

* `validate()` was removed

#### `models.OccurrenceProperties` was modified

* `validate()` was removed

#### `models.OccurrenceResultSummary` was modified

* `validate()` was removed

#### `models.ScheduledActionUpdateProperties` was modified

* `validate()` was removed

#### `models.OperationErrorDetails` was modified

* `validate()` was removed

#### `models.RetryPolicy` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.ResourceOperation` was modified

* `validate()` was removed

#### `models.OccurrenceExtensionProperties` was modified

* `validate()` was removed

#### `models.DelayRequest` was modified

* `validate()` was removed

#### `models.ScheduledActionsSchedule` was modified

* `validate()` was removed

#### `models.ResourceAttachRequest` was modified

* `validate()` was removed

#### `models.ResourceDetachRequest` was modified

* `validate()` was removed

#### `models.Resources` was modified

* `validate()` was removed

#### `models.CancelOccurrenceRequest` was modified

* `validate()` was removed

#### `models.ResourceStatus` was modified

* `validate()` was removed

#### `models.ExecutionParameters` was modified

* `validate()` was removed

#### `models.InnerError` was modified

* `validate()` was removed

#### `models.ResourceResultSummary` was modified

* `validate()` was removed

#### `models.ScheduledActions` was modified

* `virtualMachinesExecuteStart(java.lang.String,models.ExecuteStartRequest)` was removed
* `virtualMachinesExecuteDeallocateWithResponse(java.lang.String,models.ExecuteDeallocateRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesExecuteDeallocate(java.lang.String,models.ExecuteDeallocateRequest)` was removed
* `virtualMachinesSubmitHibernate(java.lang.String,models.SubmitHibernateRequest)` was removed
* `virtualMachinesExecuteCreate(java.lang.String,models.ExecuteCreateRequest)` was removed
* `virtualMachinesGetOperationErrors(java.lang.String,models.GetOperationErrorsRequest)` was removed
* `virtualMachinesExecuteDelete(java.lang.String,models.ExecuteDeleteRequest)` was removed
* `virtualMachinesExecuteHibernate(java.lang.String,models.ExecuteHibernateRequest)` was removed
* `virtualMachinesSubmitDeallocateWithResponse(java.lang.String,models.SubmitDeallocateRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesExecuteHibernateWithResponse(java.lang.String,models.ExecuteHibernateRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesSubmitStart(java.lang.String,models.SubmitStartRequest)` was removed
* `virtualMachinesSubmitStartWithResponse(java.lang.String,models.SubmitStartRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesExecuteCreateWithResponse(java.lang.String,models.ExecuteCreateRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesGetOperationStatus(java.lang.String,models.GetOperationStatusRequest)` was removed
* `virtualMachinesCancelOperations(java.lang.String,models.CancelOperationsRequest)` was removed
* `virtualMachinesGetOperationErrorsWithResponse(java.lang.String,models.GetOperationErrorsRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesSubmitHibernateWithResponse(java.lang.String,models.SubmitHibernateRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesExecuteDeleteWithResponse(java.lang.String,models.ExecuteDeleteRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesCancelOperationsWithResponse(java.lang.String,models.CancelOperationsRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesSubmitDeallocate(java.lang.String,models.SubmitDeallocateRequest)` was removed
* `virtualMachinesExecuteStartWithResponse(java.lang.String,models.ExecuteStartRequest,com.azure.core.util.Context)` was removed
* `virtualMachinesGetOperationStatusWithResponse(java.lang.String,models.GetOperationStatusRequest,com.azure.core.util.Context)` was removed

#### `models.Error` was modified

* `validate()` was removed

#### `models.ScheduledActionResources` was modified

* `models.ScheduledActionProperties properties()` -> `models.ScheduledActionsExtensionProperties properties()`

#### `models.ResourceOperationError` was modified

* `validate()` was removed

#### `models.OperationErrorsResult` was modified

* `validate()` was removed

#### `models.NotificationProperties` was modified

* `validate()` was removed

#### `models.ScheduledActionProperties` was modified

* `validate()` was removed

#### `models.ResourceOperationDetails` was modified

* `validate()` was removed

#### `models.ResourcePatchRequest` was modified

* `validate()` was removed

#### `models.Schedule` was modified

* `validate()` was removed

#### `models.ScheduledActionUpdate` was modified

* `validate()` was removed

### Features Added

* `models.CreateFlexResourceOperationResponse` was added

* `models.ExecuteCreateContent` was added

* `models.GetOperationErrorsContent` was added

* `models.PriorityProfile` was added

* `models.ExecuteCreateFlexContent` was added

* `models.SubmitStartContent` was added

* `models.ExecuteHibernateContent` was added

* `models.FallbackOperationInfo` was added

* `models.SubmitDeallocateContent` was added

* `models.VmSizeProfile` was added

* `models.GetOperationStatusContent` was added

* `models.FlexProperties` was added

* `models.ZoneAllocationPolicy` was added

* `models.AllocationStrategy` was added

* `models.ScheduledActionsExtensionProperties` was added

* `models.ExecuteDeleteContent` was added

* `models.PriorityType` was added

* `models.ExecuteStartContent` was added

* `models.CancelOperationsContent` was added

* `models.ExecuteDeallocateContent` was added

* `models.ResourceProvisionFlexPayload` was added

* `models.ZonePreference` was added

* `models.SubmitHibernateContent` was added

* `models.DistributionStrategy` was added

* `models.OsType` was added

#### `models.RetryPolicy` was modified

* `onFailureAction()` was added
* `withOnFailureAction(models.ResourceOperationType)` was added

#### `models.ResourceOperationType` was modified

* `DELETE` was added
* `CREATE` was added

#### `models.ScheduledActions` was modified

* `virtualMachinesSubmitHibernateWithResponse(java.lang.String,models.SubmitHibernateContent,com.azure.core.util.Context)` was added
* `virtualMachinesGetOperationErrors(java.lang.String,models.GetOperationErrorsContent)` was added
* `virtualMachinesExecuteHibernate(java.lang.String,models.ExecuteHibernateContent)` was added
* `virtualMachinesSubmitStart(java.lang.String,models.SubmitStartContent)` was added
* `virtualMachinesSubmitDeallocateWithResponse(java.lang.String,models.SubmitDeallocateContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteDeallocateWithResponse(java.lang.String,models.ExecuteDeallocateContent,com.azure.core.util.Context)` was added
* `virtualMachinesSubmitHibernate(java.lang.String,models.SubmitHibernateContent)` was added
* `virtualMachinesSubmitDeallocate(java.lang.String,models.SubmitDeallocateContent)` was added
* `virtualMachinesExecuteDelete(java.lang.String,models.ExecuteDeleteContent)` was added
* `virtualMachinesExecuteStartWithResponse(java.lang.String,models.ExecuteStartContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteCreate(java.lang.String,models.ExecuteCreateContent)` was added
* `virtualMachinesExecuteCreateFlex(java.lang.String,models.ExecuteCreateFlexContent)` was added
* `virtualMachinesExecuteCreateFlexWithResponse(java.lang.String,models.ExecuteCreateFlexContent,com.azure.core.util.Context)` was added
* `virtualMachinesGetOperationErrorsWithResponse(java.lang.String,models.GetOperationErrorsContent,com.azure.core.util.Context)` was added
* `virtualMachinesCancelOperations(java.lang.String,models.CancelOperationsContent)` was added
* `virtualMachinesSubmitStartWithResponse(java.lang.String,models.SubmitStartContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteDeallocate(java.lang.String,models.ExecuteDeallocateContent)` was added
* `virtualMachinesGetOperationStatus(java.lang.String,models.GetOperationStatusContent)` was added
* `virtualMachinesGetOperationStatusWithResponse(java.lang.String,models.GetOperationStatusContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteDeleteWithResponse(java.lang.String,models.ExecuteDeleteContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteHibernateWithResponse(java.lang.String,models.ExecuteHibernateContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteCreateWithResponse(java.lang.String,models.ExecuteCreateContent,com.azure.core.util.Context)` was added
* `virtualMachinesExecuteStart(java.lang.String,models.ExecuteStartContent)` was added
* `virtualMachinesCancelOperationsWithResponse(java.lang.String,models.CancelOperationsContent,com.azure.core.util.Context)` was added

#### `models.ResourceOperationDetails` was modified

* `fallbackOperationInfo()` was added

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
