# Release History

## 1.0.0-beta.3 (2026-08-14)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-07-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.RecurringScheduledActionsExecutionParameters` was removed

#### `models.ScheduledActionResources` was removed

#### `models.ResourceIdentityType` was removed

#### `models.RecurringScheduledActionsProvisioningState` was removed

#### `models.ScheduledActionExtensions` was removed

#### `models.ScheduledActionOperationStatus` was removed

#### `models.ScheduledAction$DefinitionStages` was removed

#### `models.ScheduledAction$UpdateStages` was removed

#### `models.OccurrenceResource` was removed

#### `models.RecurringScheduledActionsDeadlineType` was removed

#### `models.BulkInstancesInnerError` was removed

#### `models.OccurrenceProperties` was removed

#### `models.VirtualMachineIdentity` was removed

#### `models.LocationBasedBulkCreateCustom$Definition` was removed

#### `models.BulkCreateCustomPriorityProfile` was removed

#### `models.OccurrenceState` was removed

#### `models.ScheduledAction$Definition` was removed

#### `models.BulkCreateCustoms` was removed

#### `models.OccurrenceResultSummary` was removed

#### `models.ResourceOperationResponse` was removed

#### `models.LocationBasedBulkCreateCustom$DefinitionStages` was removed

#### `models.BulkCreateCustomOverrideBase` was removed

#### `models.LocationBasedBulkCreateCustom` was removed

#### `models.ResourcePatchRequest` was removed

#### `models.ResourceAttachRequest` was removed

#### `models.UserAssignedIdentitiesValue` was removed

#### `models.OccurrenceExtensions` was removed

#### `models.ResourceStatus` was removed

#### `models.Language` was removed

#### `models.OccurrenceExtensionProperties` was removed

#### `models.NotificationType` was removed

#### `models.ScheduledActionProperties` was removed

#### `models.ScheduledAction` was removed

#### `models.DelayRequest` was removed

#### `models.ScheduledActionResourceInput` was removed

#### `models.BulkCreateCustomOverridesProfile` was removed

#### `models.BulkCreateCustomDistributionStrategy` was removed

#### `models.RecurringScheduledActionsRetryPolicy` was removed

#### `models.OptimizationPreference` was removed

#### `models.ResourceProvisioningState` was removed

#### `models.NotificationProperties` was removed

#### `models.CancelOccurrenceRequest` was removed

#### `models.ScheduledActionUpdateProperties` was removed

#### `models.BulkCreateCustomOverride` was removed

#### `models.ScheduledAction$Update` was removed

#### `models.ResourceOperationStatus` was removed

#### `models.ResourceResultSummary` was removed

#### `models.WeekDay` was removed

#### `models.LocationBasedBulkCreateCustom$Update` was removed

#### `models.Error` was removed

#### `models.BulkCreateCustomProperties` was removed

#### `models.ScheduledActionsScheduleUpdate` was removed

#### `models.BulkCreateCustomAllocationStrategy` was removed

#### `models.ScheduledActionType` was removed

#### `models.ResourceType` was removed

#### `models.Occurrence` was removed

#### `models.LocationBasedBulkCreateCustom$UpdateStages` was removed

#### `models.BulkCreateCustomZoneAllocationPolicy` was removed

#### `models.RecurringScheduledActionsResourceOperationType` was removed

#### `models.ScheduledActionUpdate` was removed

#### `models.Month` was removed

#### `models.ScheduledActionResource` was removed

#### `models.ResourceDetachRequest` was removed

#### `models.ScheduledActions` was removed

#### `models.ScheduledActionsSchedule` was removed

#### `models.OccurrenceExtensionResource` was removed

#### `models.Occurrences` was removed

#### `models.ScheduledActionsExtensionProperties` was removed

#### `models.BulkCreateCustomVmSizeProfile` was removed

#### `models.ApiError` was modified

* `models.BulkInstancesInnerError innererror()` -> `models.InnerError innererror()`

#### `models.ExecutionParameters` was modified

* `optimizationPreference()` was removed
* `withOptimizationPreference(models.OptimizationPreference)` was removed

#### `models.InnerError` was modified

* `InnerError()` was changed to private access
* `innererror()` was removed
* `withCode(java.lang.String)` was removed
* `code()` was removed
* `withInnererror(models.InnerError)` was removed

#### `ComputeBulkActionsManager` was modified

* `bulkCreateCustoms()` was removed
* `scheduledActions()` was removed
* `scheduledActionExtensions()` was removed
* `occurrences()` was removed
* `occurrenceExtensions()` was removed
* `scheduledActionOperationStatus()` was removed

### Features Added

#### `models.InnerError` was modified

* `errorDetail()` was added
* `exceptionType()` was added

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

