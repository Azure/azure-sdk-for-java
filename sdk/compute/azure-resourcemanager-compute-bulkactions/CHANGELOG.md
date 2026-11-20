# Release History

## 1.0.0 (2026-11-20)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-10-16. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ScheduledActionResources` was removed

#### `models.ScheduledActionExtensions` was removed

#### `models.ScheduledActionOperationStatus` was removed

#### `models.ScheduledAction$DefinitionStages` was removed

#### `models.ScheduledAction$UpdateStages` was removed

#### `models.OccurrenceResource` was removed

#### `models.OSProfileProvisioningData` was removed

#### `models.ReimagePayload` was removed

#### `models.AcknowledgeBulkOperationErrorsRequest` was removed

#### `models.ResourcesWithContext` was removed

#### `models.OccurrenceProperties` was removed

#### `models.ScheduledActionsResourceOperationType` was removed

#### `models.OccurrenceState` was removed

#### `models.ReimageResourceOverride` was removed

#### `models.ScheduledActionsDeadlineType` was removed

#### `models.ScheduledAction$Definition` was removed

#### `models.OccurrenceResultSummary` was removed

#### `models.ResourceOperationResponse` was removed

#### `models.ScheduledActionsExecutionParameters` was removed

#### `models.ResourcePatchRequest` was removed

#### `models.ResourceAttachRequest` was removed

#### `models.ExecuteReimageRequest` was removed

#### `models.OccurrenceExtensions` was removed

#### `models.ResourceStatus` was removed

#### `models.Language` was removed

#### `models.OccurrenceExtensionProperties` was removed

#### `models.NotificationType` was removed

#### `models.ScheduledActionProperties` was removed

#### `models.ScheduledAction` was removed

#### `models.DelayRequest` was removed

#### `models.ScheduledActionResourceInput` was removed

#### `models.LocationBasedBulkCreate$UpdateStages` was removed

#### `models.InnerError` was removed

#### `models.ScheduledActionsRetryPolicy` was removed

#### `models.OccurrenceResourceProvisioningState` was removed

#### `models.NotificationProperties` was removed

#### `models.CancelOccurrenceRequest` was removed

#### `models.ScheduledActionUpdateProperties` was removed

#### `models.ScheduledAction$Update` was removed

#### `models.VirtualMachineReimageParameters` was removed

#### `models.ResourceOperationStatus` was removed

#### `models.ResourceResultSummary` was removed

#### `models.WeekDay` was removed

#### `models.LocationBasedBulkCreateCustom$Update` was removed

#### `models.Error` was removed

#### `models.ScheduledActionsScheduleUpdate` was removed

#### `models.ScheduledActionType` was removed

#### `models.ResourceType` was removed

#### `models.ReimageResourceOperationResponse` was removed

#### `models.Occurrence` was removed

#### `models.LocationBasedBulkCreateCustom$UpdateStages` was removed

#### `models.ScheduledActionUpdate` was removed

#### `models.Month` was removed

#### `models.ScheduledActionResource` was removed

#### `models.ResourceDetachRequest` was removed

#### `models.AcknowledgeBulkOperationErrorsResponse` was removed

#### `models.ScheduledActions` was removed

#### `models.ScheduledActionsSchedule` was removed

#### `models.OccurrenceExtensionResource` was removed

#### `models.Occurrences` was removed

#### `models.ResourceWithContext` was removed

#### `models.ScheduledActionsProvisioningState` was removed

#### `models.LocationBasedBulkCreate$Update` was removed

#### `models.ScheduledActionsExtensionProperties` was removed

#### `models.LocationBasedBulkCreate$Definition` was modified

* `withTags(java.util.Map)` was removed

#### `models.VirtualMachineBulkOperations` was modified

* `bulkListOperationErrors(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `bulkListOperationErrors(java.lang.String,java.lang.String)` was removed
* `bulkAcknowledgeOperationErrorsWithResponse(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest,com.azure.core.util.Context)` was removed
* `bulkReimageOperation(java.lang.String,java.lang.String,models.ExecuteReimageRequest)` was removed
* `bulkAcknowledgeOperationErrors(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest)` was removed
* `bulkReimageOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteReimageRequest,com.azure.core.util.Context)` was removed

#### `models.LocationBasedBulkCreateCustom$Definition` was modified

* `withTags(java.util.Map)` was removed

#### `models.BulkCreateCustoms` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `cancel(java.lang.String,java.lang.String,java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed
* `cancel(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ExecuteDeallocateContent` was modified

* `withResourcesWithContext(models.ResourcesWithContext)` was removed
* `resourcesWithContext()` was removed

#### `models.ResourceOperation` was modified

* Changed from an interface to a class.
* `innerModel()` was removed

#### `models.LocationBasedBulkCreate` was modified

* `cancel(com.azure.core.util.Context)` was removed
* `cancel()` was removed
* `update()` was removed
* `resourceGroupName()` was removed
* `tags()` was removed

#### `models.LocationBasedBulkCreateCustom` was modified

* `cancel()` was removed
* `cancel(com.azure.core.util.Context)` was removed
* `update()` was removed
* `tags()` was removed
* `resourceGroupName()` was removed

#### `models.ExecuteStartContent` was modified

* `resourcesWithContext()` was removed
* `withResourcesWithContext(models.ResourcesWithContext)` was removed

#### `ComputeBulkActionsManager` was modified

* `occurrenceExtensions()` was removed
* `scheduledActionExtensions()` was removed
* `occurrences()` was removed
* `scheduledActionOperationStatus()` was removed
* `scheduledActions()` was removed

#### `models.BulkCreates` was modified

* `cancel(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `deleteById(java.lang.String)` was removed
* `cancel(java.lang.String,java.lang.String,java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,java.lang.Boolean,com.azure.core.util.Context)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.ExecuteHibernateContent` was modified

* `withResourcesWithContext(models.ResourcesWithContext)` was removed
* `resourcesWithContext()` was removed

#### `models.ExecuteDeleteContent` was modified

* `resourcesWithContext()` was removed
* `withResourcesWithContext(models.ResourcesWithContext)` was removed

### Features Added

#### `models.ResourceOperation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.8 (2026-10-13)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-10-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BulkCreateCustomPriorityProfile` was removed

#### `models.PartialFulfillmentReason` was removed

#### `models.PartialFulfillmentPolicy` was removed

#### `models.PartialFulfillmentMode` was removed

#### `models.PriorityProfile` was removed

#### `models.BulkCreateProperties` was modified

* `withPriorityProfile(models.PriorityProfile)` was removed
* `minCapacity()` was removed
* `withPartialFulfillmentPolicy(models.PartialFulfillmentPolicy)` was removed
* `withMinCapacity(java.lang.Integer)` was removed
* `partialFulfillmentPolicy()` was removed
* `priorityProfile()` was removed

#### `models.BulkCreateCustomProperties` was modified

* `withPriorityProfile(models.BulkCreateCustomPriorityProfile)` was removed
* `partialFulfillmentPolicy()` was removed
* `withMinCapacity(java.lang.Integer)` was removed
* `withPartialFulfillmentPolicy(models.PartialFulfillmentPolicy)` was removed
* `priorityProfile()` was removed
* `minCapacity()` was removed

### Features Added

* `models.ZonePlacementPolicyType` was added

* `models.ExtendedLocation` was added

* `models.BillingProfile` was added

* `models.Placement` was added

* `models.ExtendedLocationType` was added

#### `models.LocationBasedBulkCreate$Definition` was modified

* `withPlacement(models.Placement)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.LocationBasedBulkCreateCustom$Definition` was modified

* `withPlacement(models.Placement)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.LocationBasedBulkCreate` was modified

* `placement()` was added
* `extendedLocation()` was added

#### `models.LocationBasedBulkCreateCustom` was modified

* `extendedLocation()` was added
* `placement()` was added

#### `models.BulkCreateCustomOverride` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `placement()` was added
* `zone()` was added
* `withZone(java.lang.String)` was added
* `extendedLocation()` was added
* `withPlacement(models.Placement)` was added

#### `models.BulkactionVMProperties` was modified

* `priority()` was added
* `evictionPolicy()` was added
* `withBillingProfile(models.BillingProfile)` was added
* `withEvictionPolicy(models.EvictionPolicy)` was added
* `withPriority(models.PriorityType)` was added
* `billingProfile()` was added

## 1.0.0-beta.7 (2026-09-29)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-10-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.AcknowledgeBulkOperationErrorsRequest` was added

* `models.AcknowledgeBulkOperationErrorsResponse` was added

#### `models.VirtualMachineBulkOperations` was modified

* `bulkAcknowledgeOperationErrorsWithResponse(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest,com.azure.core.util.Context)` was added
* `bulkAcknowledgeOperationErrors(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest)` was added

## 1.0.0-beta.6 (2026-09-25)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-10-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ResourceOperation` was modified

* `fromJson(com.azure.json.JsonReader)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String errorDetails()` -> `java.lang.String errorDetails()`
* `java.lang.String resourceId()` -> `java.lang.String resourceId()`
* `models.ResourceOperationDetails operation()` -> `models.ResourceOperationDetails operation()`
* `models.VirtualMachineInfo virtualMachineInfo()` -> `models.VirtualMachineInfo virtualMachineInfo()`
* `java.lang.String errorCode()` -> `java.lang.String errorCode()`

### Features Added

#### `models.VirtualMachineBulkOperations` was modified

* `bulkListOperationErrors(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `bulkListOperationErrors(java.lang.String,java.lang.String)` was added

#### `models.ResourceOperation` was modified

* `innerModel()` was added

## 1.0.0-beta.5 (2026-09-24)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-10-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BulkCreateVmSizeProfile` was removed

#### `models.ZonePreference` was removed

#### `models.BulkCreateCustomOverrideBase` was removed

#### `models.DistributionStrategy` was removed

#### `models.BulkCreateCustomDistributionStrategy` was removed

#### `models.ZoneAllocationPolicy` was removed

#### `models.BulkCreateCustomAllocationStrategy` was removed

#### `models.BulkCreateCustomZoneAllocationPolicy` was removed

#### `models.AllocationStrategy` was removed

#### `models.BulkCreateCustomVmSizeProfile` was removed

#### `models.BulkCreateCustomPriorityProfile` was modified

* `allocationStrategy()` was removed
* `withAllocationStrategy(models.BulkCreateCustomAllocationStrategy)` was removed

#### `models.BulkCreateProperties` was modified

* `vmSizesProfile()` was removed
* `zoneAllocationPolicy()` was removed
* `withZoneAllocationPolicy(models.ZoneAllocationPolicy)` was removed
* `withVmSizesProfile(java.util.List)` was removed

#### `models.BulkCreateCustomProperties` was modified

* `vmSizesProfile()` was removed
* `withZoneAllocationPolicy(models.BulkCreateCustomZoneAllocationPolicy)` was removed
* `zoneAllocationPolicy()` was removed
* `withVmSizesProfile(java.util.List)` was removed

#### `models.PriorityProfile` was modified

* `allocationStrategy()` was removed
* `withAllocationStrategy(models.AllocationStrategy)` was removed

### Features Added

#### `models.ExecutionParameters` was modified

* `withAdditionalCreateParameters(java.util.Map)` was added
* `additionalCreateParameters()` was added

## 1.0.0-beta.4 (2026-09-11)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-09-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.LaunchBulkInstancesOperationProperties` was removed

#### `models.LocationBasedLaunchBulkInstancesOperation$UpdateStages` was removed

#### `models.ExecuteCreateContent` was removed

#### `models.ApiError` was removed

#### `models.AcceleratorType` was removed

#### `models.AcknowledgeBulkOperationErrorsRequest` was removed

#### `models.BulkInstancesInnerError` was removed

#### `models.FlexProperties` was removed

#### `models.ExecuteVdiCreateRequest` was removed

#### `models.ApiErrorBase` was removed

#### `models.LocationBasedLaunchBulkInstancesOperation` was removed

#### `models.AcceleratorManufacturer` was removed

#### `models.LocationBasedLaunchBulkInstancesOperation$Definition` was removed

#### `models.VMAttributeSupport` was removed

#### `models.ResourceProvisionVdiPayload` was removed

#### `models.OptimizationPreference` was removed

#### `models.LocationBasedLaunchBulkInstancesOperation$Update` was removed

#### `models.VirtualMachine` was removed

#### `models.VMCategory` was removed

#### `models.LocalStorageDiskType` was removed

#### `models.LocationBasedLaunchBulkInstancesOperation$DefinitionStages` was removed

#### `models.CpuManufacturer` was removed

#### `models.VMOperationStatus` was removed

#### `models.VMAttributeMinMaxInteger` was removed

#### `models.VmSizeProfile` was removed

#### `models.ResourceProvisionPayload` was removed

#### `models.CreateResourceOperationResponse` was removed

#### `models.ArchitectureType` was removed

#### `models.AcknowledgeBulkOperationErrorsResponse` was removed

#### `models.LaunchBulkInstancesOperations` was removed

#### `models.VMAttributeMinMaxDouble` was removed

#### `models.OsType` was removed

#### `models.VMAttributes` was removed

#### `models.HyperVGeneration` was removed

#### `models.VirtualMachineBulkOperations` was modified

* `bulkCreateOperation(java.lang.String,java.lang.String,models.ExecuteCreateContent)` was removed
* `bulkAcknowledgeOperationErrors(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest)` was removed
* `bulkListOperationErrors(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was removed
* `bulkCreateOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteCreateContent,com.azure.core.util.Context)` was removed
* `bulkVdiFlexCreateOperation(java.lang.String,java.lang.String,models.ExecuteVdiCreateRequest)` was removed
* `bulkVdiFlexCreateOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteVdiCreateRequest,com.azure.core.util.Context)` was removed
* `bulkAcknowledgeOperationErrorsWithResponse(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest,com.azure.core.util.Context)` was removed
* `bulkListOperationErrors(java.lang.String,java.lang.String)` was removed

#### `models.ScheduledActionsResourceOperationType` was modified

* `UNKNOWN` was removed

#### `models.ScheduledActionsDeadlineType` was modified

* `UNKNOWN` was removed

#### `models.ResourceOperation` was modified

* `models.VirtualMachineInfo virtualMachineInfo()` -> `models.VirtualMachineInfo virtualMachineInfo()`
* `innerModel()` was removed
* `models.ResourceOperationDetails operation()` -> `models.ResourceOperationDetails operation()`
* `java.lang.String resourceId()` -> `java.lang.String resourceId()`
* `java.lang.String errorDetails()` -> `java.lang.String errorDetails()`
* `java.lang.String errorCode()` -> `java.lang.String errorCode()`

#### `models.ScheduledActionsExecutionParameters` was modified

* `optimizationPreference()` was removed
* `withOptimizationPreference(models.OptimizationPreference)` was removed

#### `models.ExecutionParameters` was modified

* `optimizationPreference()` was removed
* `withOptimizationPreference(models.OptimizationPreference)` was removed

#### `models.ResourceOperationType` was modified

* `GET_INSTANCE_VIEW` was removed
* `UNKNOWN` was removed

#### `models.OperationState` was modified

* `PENDING_SCHEDULING` was removed
* `PENDING_EXECUTION` was removed
* `UNKNOWN` was removed

#### `ComputeBulkActionsManager` was modified

* `launchBulkInstancesOperations()` was removed

#### `models.LocationBasedBulkCreateCustom$Update` was modified

* `withZones(java.util.List)` was removed

#### `models.DeadlineType` was modified

* `UNKNOWN` was removed

### Features Added

* `models.LocationBasedBulkCreate$Definition` was added

* `models.BulkCreateVmSizeProfile` was added

* `models.LocationBasedBulkCreate` was added

* `models.BulkCreateProperties` was added

* `models.LocationBasedBulkCreate$DefinitionStages` was added

* `models.LocationBasedBulkCreate$UpdateStages` was added

* `models.BulkCreates` was added

* `models.LocationBasedBulkCreate$Update` was added

#### `models.ZonePreference` was modified

* `withTargetMaxCapacity(java.lang.Integer)` was added
* `targetMaxCapacity()` was added

#### `models.ResourceOperation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualMachineInfo` was modified

* `name()` was added

#### `ComputeBulkActionsManager` was modified

* `bulkCreates()` was added

#### `models.ScheduledActionsProvisioningState` was modified

* `UPDATING` was added

## 1.0.0-beta.3 (2026-08-21)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-08-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RecurringScheduledActionsExecutionParameters` was removed

#### `models.RecurringScheduledActionsProvisioningState` was removed

#### `models.RecurringScheduledActionsDeadlineType` was removed

#### `models.RecurringScheduledActionsRetryPolicy` was removed

#### `models.ResourceProvisioningState` was removed

#### `models.RecurringScheduledActionsResourceOperationType` was removed

#### `models.OccurrenceResource` was modified

* `models.ResourceProvisioningState provisioningState()` -> `models.OccurrenceResourceProvisioningState provisioningState()`

#### `models.OccurrenceExtensionProperties` was modified

* `models.ResourceProvisioningState provisioningState()` -> `models.OccurrenceResourceProvisioningState provisioningState()`

#### `models.ScheduledActionProperties` was modified

* `models.RecurringScheduledActionsProvisioningState provisioningState()` -> `models.ScheduledActionsProvisioningState provisioningState()`

#### `models.ScheduledActionsScheduleUpdate` was modified

* `withExecutionParameters(models.RecurringScheduledActionsExecutionParameters)` was removed
* `models.RecurringScheduledActionsDeadlineType deadlineType()` -> `models.ScheduledActionsDeadlineType deadlineType()`
* `models.RecurringScheduledActionsExecutionParameters executionParameters()` -> `models.ScheduledActionsExecutionParameters executionParameters()`
* `withDeadlineType(models.RecurringScheduledActionsDeadlineType)` was removed

#### `models.ScheduledActionsSchedule` was modified

* `models.RecurringScheduledActionsExecutionParameters executionParameters()` -> `models.ScheduledActionsExecutionParameters executionParameters()`
* `withDeadlineType(models.RecurringScheduledActionsDeadlineType)` was removed
* `models.RecurringScheduledActionsDeadlineType deadlineType()` -> `models.ScheduledActionsDeadlineType deadlineType()`
* `withExecutionParameters(models.RecurringScheduledActionsExecutionParameters)` was removed

#### `models.ScheduledActionsExtensionProperties` was modified

* `models.RecurringScheduledActionsProvisioningState provisioningState()` -> `models.ScheduledActionsProvisioningState provisioningState()`

### Features Added

* `models.ScheduledActionsResourceOperationType` was added

* `models.ScheduledActionsDeadlineType` was added

* `models.ScheduledActionsExecutionParameters` was added

* `models.CapacityRecommendationDetails` was added

* `models.BulkCreateCustomVirtualMachineInfo` was added

* `models.CapacityRecommendationStatus` was added

* `models.CapacityRecommendation` was added

* `models.PartialFulfillmentReason` was added

* `models.CapacityRecommendationParameters` was added

* `models.ScheduledActionsRetryPolicy` was added

* `models.CapacityRecommendationPlacementScore` was added

* `models.OccurrenceResourceProvisioningState` was added

* `models.BulkCreateCustomResource` was added

* `models.CapacityRecommendationSize` was added

* `models.PartialFulfillmentPolicy` was added

* `models.PartialFulfillmentMode` was added

* `models.ScheduledActionsProvisioningState` was added

#### `models.BulkCreateCustoms` was modified

* `virtualMachinesGetOperationStatus(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `virtualMachinesGetOperationStatus(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.LocationBasedBulkCreateCustom` was modified

* `virtualMachinesGetOperationStatus(com.azure.core.util.Context)` was added
* `virtualMachinesGetOperationStatus()` was added

#### `models.ResourceOperationDetails` was modified

* `capacityRecommendation()` was added

#### `models.ExecutionParameters` was modified

* `capacityRecommendationParameters()` was added
* `withCapacityRecommendationParameters(models.CapacityRecommendationParameters)` was added

#### `models.BulkCreateCustomProperties` was modified

* `withPartialFulfillmentPolicy(models.PartialFulfillmentPolicy)` was added
* `minCapacity()` was added
* `withMinCapacity(java.lang.Integer)` was added
* `resources()` was added
* `partialFulfillmentPolicy()` was added

#### `models.ScheduledActionsScheduleUpdate` was modified

* `withExecutionParameters(models.ScheduledActionsExecutionParameters)` was added
* `withDeadlineType(models.ScheduledActionsDeadlineType)` was added

#### `models.ScheduledActionsSchedule` was modified

* `withDeadlineType(models.ScheduledActionsDeadlineType)` was added
* `withExecutionParameters(models.ScheduledActionsExecutionParameters)` was added

## 1.0.0-beta.2 (2026-07-22)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-07-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ResourceOperation` was modified

* `java.lang.String errorCode()` -> `java.lang.String errorCode()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `models.ResourceOperationDetails operation()` -> `models.ResourceOperationDetails operation()`
* `java.lang.String errorDetails()` -> `java.lang.String errorDetails()`
* `java.lang.String resourceId()` -> `java.lang.String resourceId()`
* `toJson(com.azure.json.JsonWriter)` was removed

### Features Added

* `models.EventGridAndResourceGraph` was added

* `models.VirtualMachinePublicIPAddressDnsSettingsConfiguration` was added

* `models.WinRMConfiguration` was added

* `models.ScheduledEventsProfile` was added

* `models.LaunchBulkInstancesOperationProperties` was added

* `models.StorageAccountTypes` was added

* `models.DomainNameLabelScopeTypes` was added

* `models.DiffDiskOptions` was added

* `models.VirtualMachineIpTag` was added

* `models.RecurringScheduledActionsExecutionParameters` was added

* `models.EvictionPolicy` was added

* `models.LocationBasedLaunchBulkInstancesOperation$UpdateStages` was added

* `models.WindowsVMGuestPatchMode` was added

* `models.ScheduledActionResources` was added

* `models.LinuxPatchAssessmentMode` was added

* `models.DiagnosticsProfile` was added

* `models.ResourceIdentityType` was added

* `models.ExecuteCreateContent` was added

* `models.WindowsConfiguration` was added

* `models.ProtocolTypes` was added

* `models.WindowsPatchAssessmentMode` was added

* `models.RecurringScheduledActionsProvisioningState` was added

* `models.ScheduledActionExtensions` was added

* `models.ScheduledActionOperationStatus` was added

* `models.DeleteOptions` was added

* `models.ScheduledAction$DefinitionStages` was added

* `models.AdditionalUnattendContent` was added

* `models.ScheduledAction$UpdateStages` was added

* `models.OccurrenceResource` was added

* `models.VirtualMachinePublicIPAddressConfiguration` was added

* `models.RecurringScheduledActionsDeadlineType` was added

* `models.ApiError` was added

* `models.LinuxPatchSettings` was added

* `models.UserInitiatedReboot` was added

* `models.VaultSecretGroup` was added

* `models.DiskControllerTypes` was added

* `models.ManagedServiceIdentity` was added

* `models.WindowsVMGuestPatchAutomaticByPlatformSettings` was added

* `models.OSDisk` was added

* `models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting` was added

* `models.VmSizeProperties` was added

* `models.OSProfileProvisioningData` was added

* `models.ReimagePayload` was added

* `models.AcceleratorType` was added

* `models.AcknowledgeBulkOperationErrorsRequest` was added

* `models.BulkInstancesInnerError` was added

* `models.DiffDiskPlacement` was added

* `models.KeyVaultKeyReference` was added

* `models.ResourcesWithContext` was added

* `models.OccurrenceProperties` was added

* `models.VirtualMachineIdentity` was added

* `models.DiskCreateOptionTypes` was added

* `models.LocationBasedBulkCreateCustom$Definition` was added

* `models.BulkCreateCustomPriorityProfile` was added

* `models.OccurrenceState` was added

* `models.SecurityTypes` was added

* `models.ReimageResourceOverride` was added

* `models.FlexProperties` was added

* `models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting` was added

* `models.ProxyAgentSettings` was added

* `models.ExecuteVdiCreateRequest` was added

* `models.LinuxVMGuestPatchAutomaticByPlatformSettings` was added

* `models.ScheduledAction$Definition` was added

* `models.BulkCreateCustoms` was added

* `models.ImageReference` was added

* `models.OccurrenceResultSummary` was added

* `models.StorageProfile` was added

* `models.ZonePreference` was added

* `models.VMDiskSecurityProfile` was added

* `models.ResourceOperationResponse` was added

* `models.PublicIPAddressSku` was added

* `models.ApiErrorBase` was added

* `models.VMGalleryApplication` was added

* `models.LocationBasedBulkCreateCustom$DefinitionStages` was added

* `models.SecurityEncryptionTypes` was added

* `models.BulkCreateCustomOverrideBase` was added

* `models.LocationBasedBulkCreateCustom` was added

* `models.KeyVaultSecretReference` was added

* `models.LocationBasedLaunchBulkInstancesOperation` was added

* `models.ResourcePatchRequest` was added

* `models.PatchSettings` was added

* `models.HardwareProfile` was added

* `models.BulkActionVmExtensionProperties` was added

* `models.OSProfile` was added

* `models.UefiSettings` was added

* `models.Mode` was added

* `models.OperationStatusResult` was added

* `models.ResourceAttachRequest` was added

* `models.AcceleratorManufacturer` was added

* `models.PriorityType` was added

* `models.VirtualMachineInfo` was added

* `models.ExecuteReimageRequest` was added

* `models.VirtualMachineNetworkInterfaceDnsSettingsConfiguration` was added

* `models.UserAssignedIdentitiesValue` was added

* `models.OccurrenceExtensions` was added

* `models.ResourceStatus` was added

* `models.LinuxConfiguration` was added

* `models.ApplicationProfile` was added

* `models.NetworkInterfaceReference` was added

* `models.ResourceNotificationDetails` was added

* `models.Language` was added

* `models.DiskDetachOptionTypes` was added

* `models.LocationBasedLaunchBulkInstancesOperation$Definition` was added

* `models.ComputeProfile` was added

* `models.OccurrenceExtensionProperties` was added

* `models.NotificationType` was added

* `models.AllInstancesDown` was added

* `models.ScheduledActionProperties` was added

* `models.NetworkApiVersion` was added

* `models.ScheduledAction` was added

* `models.DelayRequest` was added

* `models.VirtualMachineNetworkInterfaceConfiguration` was added

* `models.DataDisk` was added

* `models.ScheduledActionResourceInput` was added

* `models.SshConfiguration` was added

* `models.Modes` was added

* `models.BulkCreateCustomOverridesProfile` was added

* `models.ScheduledEventsPolicy` was added

* `models.DistributionStrategy` was added

* `models.ManagedServiceIdentityType` was added

* `models.BulkCreateCustomDistributionStrategy` was added

* `models.ProvisioningState` was added

* `models.VMAttributeSupport` was added

* `models.IPVersions` was added

* `models.ResourceProvisionVdiPayload` was added

* `models.AdditionalCapabilities` was added

* `models.RecurringScheduledActionsRetryPolicy` was added

* `models.OptimizationPreference` was added

* `models.SshPublicKey` was added

* `models.InnerError` was added

* `models.PublicIPAddressSkuTier` was added

* `models.NetworkInterfaceAuxiliarySku` was added

* `models.BulkactionVMExtension` was added

* `models.ResourceProvisioningState` was added

* `models.SettingNames` was added

* `models.LocationBasedLaunchBulkInstancesOperation$Update` was added

* `models.Plan` was added

* `models.NotificationProperties` was added

* `models.CancelOccurrenceRequest` was added

* `models.DiffDiskSettings` was added

* `models.NetworkInterfaceAuxiliaryMode` was added

* `models.DiskDeleteOptionTypes` was added

* `models.UserInitiatedRedeploy` was added

* `models.ScheduledActionUpdateProperties` was added

* `models.BulkCreateCustomOverride` was added

* `models.EncryptionIdentity` was added

* `models.NetworkProfile` was added

* `models.ScheduledAction$Update` was added

* `models.VirtualMachineReimageParameters` was added

* `models.ResourceOperationStatus` was added

* `models.ResourceResultSummary` was added

* `models.SecurityProfile` was added

* `models.VirtualMachine` was added

* `models.WeekDay` was added

* `models.HostEndpointSettings` was added

* `models.AdditionalUnattendContentPassName` was added

* `models.ApiEntityReference` was added

* `models.LocationBasedBulkCreateCustom$Update` was added

* `models.ZoneAllocationPolicy` was added

* `models.Error` was added

* `models.VirtualMachineNetworkInterfaceConfigurationProperties` was added

* `models.VMCategory` was added

* `models.VirtualMachineNetworkInterfaceIPConfiguration` was added

* `models.CachingTypes` was added

* `models.BulkCreateCustomProperties` was added

* `models.ScheduledActionsScheduleUpdate` was added

* `models.BulkCreateCustomAllocationStrategy` was added

* `models.VirtualMachineNetworkInterfaceIPConfigurationProperties` was added

* `models.ScheduledActionType` was added

* `models.DiskEncryptionSettings` was added

* `models.AdditionalUnattendContentComponentName` was added

* `models.ResourceType` was added

* `models.ReimageResourceOperationResponse` was added

* `models.LocalStorageDiskType` was added

* `models.VaultCertificate` was added

* `models.TerminateNotificationProfile` was added

* `models.LocationBasedLaunchBulkInstancesOperation$DefinitionStages` was added

* `models.CapacityReservationProfile` was added

* `models.Occurrence` was added

* `models.CapacityType` was added

* `models.CpuManufacturer` was added

* `models.LocationBasedBulkCreateCustom$UpdateStages` was added

* `models.BulkCreateCustomZoneAllocationPolicy` was added

* `models.ScheduledEventsAdditionalPublishingTargets` was added

* `models.VMOperationStatus` was added

* `models.VMAttributeMinMaxInteger` was added

* `models.RecurringScheduledActionsResourceOperationType` was added

* `models.ScheduledActionUpdate` was added

* `models.VmSizeProfile` was added

* `models.Month` was added

* `models.ResourceProvisionPayload` was added

* `models.ScheduledActionResource` was added

* `models.PublicIPAddressSkuName` was added

* `models.DiskEncryptionSetParametersContent` was added

* `models.CreateResourceOperationResponse` was added

* `models.OperatingSystemTypes` was added

* `models.AllocationStrategy` was added

* `models.ResourceDetachRequest` was added

* `models.ArchitectureType` was added

* `models.AcknowledgeBulkOperationErrorsResponse` was added

* `models.LaunchBulkInstancesOperations` was added

* `models.ScheduledActions` was added

* `models.VirtualHardDisk` was added

* `models.ScheduledActionsSchedule` was added

* `models.OccurrenceExtensionResource` was added

* `models.VMAttributeMinMaxDouble` was added

* `models.LinuxVMGuestPatchMode` was added

* `models.UserAssignedIdentity` was added

* `models.ManagedDiskParametersContent` was added

* `models.Occurrences` was added

* `models.BootDiagnostics` was added

* `models.BulkactionVMProperties` was added

* `models.PriorityProfile` was added

* `models.NetworkInterfaceReferenceProperties` was added

* `models.OSImageNotificationProfile` was added

* `models.VirtualMachinePublicIPAddressConfigurationProperties` was added

* `models.ResourceWithContext` was added

* `models.PublicIPAllocationMethod` was added

* `models.WinRMListener` was added

* `models.OsType` was added

* `models.ScheduledActionsExtensionProperties` was added

* `models.BulkCreateCustomVmSizeProfile` was added

* `models.VMAttributes` was added

* `models.HyperVGeneration` was added

#### `models.VirtualMachineBulkOperations` was modified

* `bulkListOperationErrors(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `bulkAcknowledgeOperationErrors(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest)` was added
* `bulkListOperationErrors(java.lang.String,java.lang.String)` was added
* `bulkReimageOperation(java.lang.String,java.lang.String,models.ExecuteReimageRequest)` was added
* `bulkAcknowledgeOperationErrorsWithResponse(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest,com.azure.core.util.Context)` was added
* `bulkCreateOperation(java.lang.String,java.lang.String,models.ExecuteCreateContent)` was added
* `bulkReimageOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteReimageRequest,com.azure.core.util.Context)` was added
* `bulkVdiFlexCreateOperation(java.lang.String,java.lang.String,models.ExecuteVdiCreateRequest)` was added
* `bulkCreateOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteCreateContent,com.azure.core.util.Context)` was added
* `bulkVdiFlexCreateOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteVdiCreateRequest,com.azure.core.util.Context)` was added

#### `models.ExecuteDeallocateContent` was modified

* `withResourcesWithContext(models.ResourcesWithContext)` was added
* `resourcesWithContext()` was added

#### `models.ResourceOperation` was modified

* `innerModel()` was added
* `virtualMachineInfo()` was added

#### `models.ResourceOperationDetails` was modified

* `resourceNotificationDetails()` was added

#### `models.ExecutionParameters` was modified

* `withVerifyVmAgentHealth(java.lang.Boolean)` was added
* `optimizationPreference()` was added
* `verifyVmAgentHealth()` was added
* `withOptimizationPreference(models.OptimizationPreference)` was added

#### `models.ResourceOperationType` was modified

* `GET_INSTANCE_VIEW` was added

#### `models.ExecuteStartContent` was modified

* `resourcesWithContext()` was added
* `withResourcesWithContext(models.ResourcesWithContext)` was added

#### `ComputeBulkActionsManager` was modified

* `launchBulkInstancesOperations()` was added
* `bulkCreateCustoms()` was added
* `occurrenceExtensions()` was added
* `scheduledActions()` was added
* `scheduledActionExtensions()` was added
* `occurrences()` was added
* `scheduledActionOperationStatus()` was added

#### `models.ExecuteHibernateContent` was modified

* `resourcesWithContext()` was added
* `withResourcesWithContext(models.ResourcesWithContext)` was added

#### `models.ExecuteDeleteContent` was modified

* `resourcesWithContext()` was added
* `withResourcesWithContext(models.ResourcesWithContext)` was added

## 1.0.0-beta.1 (2026-07-01)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-06-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-compute-bulkactions Java SDK.

