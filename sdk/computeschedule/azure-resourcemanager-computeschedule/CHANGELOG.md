# Release History

## 1.2.0-beta.2 (2026-06-04)

- Azure Resource Manager Compute Schedule client library for Java. This package contains Microsoft Azure SDK for Compute Schedule Management SDK. Microsoft.ComputeSchedule Resource Provider management API. Package api-version 2026-04-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.OccurrenceResultSummary` was modified

* `validate()` was removed

#### `models.ScheduledActionUpdateProperties` was modified

* `validate()` was removed

#### `models.RetryPolicy` was modified

* `validate()` was removed

#### `models.SubmitDeallocateRequest` was modified

* `validate()` was removed

#### `models.ExecuteDeallocateRequest` was modified

* `validate()` was removed

#### `models.OccurrenceExtensionProperties` was modified

* `validate()` was removed

#### `models.DelayRequest` was modified

* `validate()` was removed

#### `models.ResourceAttachRequest` was modified

* `validate()` was removed

#### `models.ExecuteCreateRequest` was modified

* `validate()` was removed

#### `models.CancelOccurrenceRequest` was modified

* `validate()` was removed

#### `models.InnerError` was modified

* `validate()` was removed

#### `models.ResourceResultSummary` was modified

* `validate()` was removed

#### `models.GetOperationErrorsRequest` was modified

* `validate()` was removed

#### `models.ScheduledActionProperties` was modified

* `validate()` was removed

#### `models.ResourceProvisionPayload` was modified

* `withBaseProfile(java.util.Map)` was removed
* `baseProfile()` was removed
* `validate()` was removed
* `withResourceOverrides(java.util.List)` was removed
* `resourceOverrides()` was removed

#### `models.OccurrenceProperties` was modified

* `validate()` was removed

#### `models.OperationErrorDetails` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.ExecuteDeleteRequest` was modified

* `validate()` was removed

#### `models.ExecuteStartRequest` was modified

* `validate()` was removed

#### `models.ResourceOperation` was modified

* `validate()` was removed

#### `models.ScheduledActionsSchedule` was modified

* `validate()` was removed

#### `models.SubmitHibernateRequest` was modified

* `validate()` was removed

#### `models.CancelOperationsRequest` was modified

* `validate()` was removed

#### `models.ResourceDetachRequest` was modified

* `validate()` was removed

#### `models.Resources` was modified

* `validate()` was removed

#### `models.ResourceStatus` was modified

* `validate()` was removed

#### `models.ExecutionParameters` was modified

* `validate()` was removed

#### `models.Error` was modified

* `validate()` was removed

#### `models.GetOperationStatusRequest` was modified

* `validate()` was removed

#### `models.ExecuteHibernateRequest` was modified

* `validate()` was removed

#### `models.ScheduledActionResources` was modified

* `models.ScheduledActionProperties properties()` -> `models.ScheduledActionsExtensionProperties properties()`

#### `models.ResourceOperationError` was modified

* `validate()` was removed

#### `models.OperationErrorsResult` was modified

* `validate()` was removed

#### `models.NotificationProperties` was modified

* `validate()` was removed

#### `models.SubmitStartRequest` was modified

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

* `models.StorageProfile` was added

* `models.VirtualMachineNetworkInterfaceConfiguration` was added

* `models.VaultSecretGroup` was added

* `models.VmSizeProperties` was added

* `models.SecurityEncryptionTypes` was added

* `models.VMGalleryApplication` was added

* `models.CapacityReservationProfile` was added

* `models.DiffDiskOptions` was added

* `models.WindowsVMGuestPatchAutomaticByPlatformSettings` was added

* `models.UserInitiatedRedeploy` was added

* `models.VirtualMachinePublicIPAddressConfigurationProperties` was added

* `models.SshPublicKey` was added

* `models.TerminateNotificationProfile` was added

* `models.ZonePlacementPolicyType` was added

* `models.DeleteOptions` was added

* `models.FallbackOperationInfo` was added

* `models.DiffDiskPlacement` was added

* `models.ScheduledEventsAdditionalPublishingTargets` was added

* `models.BulkActionVmExtensionProperties` was added

* `models.PublicIPAddressSkuName` was added

* `models.Modes` was added

* `models.VirtualMachineNetworkInterfaceConfigurationProperties` was added

* `models.FlexProperties` was added

* `models.AllInstancesDown` was added

* `models.SecurityProfile` was added

* `models.ZoneAllocationPolicy` was added

* `models.LinuxConfiguration` was added

* `models.ScheduledEventsProfile` was added

* `models.HostEndpointSettings` was added

* `models.VirtualMachineNetworkInterfaceDnsSettingsConfiguration` was added

* `models.WinRMListener` was added

* `models.ScheduledActionsExtensionProperties` was added

* `models.PriorityType` was added

* `models.ApiEntityReference` was added

* `models.WindowsConfiguration` was added

* `models.OSProfile` was added

* `models.StorageAccountTypes` was added

* `models.ExtendedLocationType` was added

* `models.SecurityTypes` was added

* `models.UefiSettings` was added

* `models.ResourceIdentityType` was added

* `models.NetworkApiVersion` was added

* `models.DiagnosticsProfile` was added

* `models.VirtualMachineIdentity` was added

* `models.ImageReference` was added

* `models.NetworkInterfaceAuxiliarySku` was added

* `models.ScheduledEventsPolicy` was added

* `models.VaultCertificate` was added

* `models.ProtocolTypes` was added

* `models.NetworkProfile` was added

* `models.Mode` was added

* `models.VMDiskSecurityProfile` was added

* `models.NetworkInterfaceReference` was added

* `models.UserInitiatedReboot` was added

* `models.ProxyAgentSettings` was added

* `models.CachingTypes` was added

* `models.VirtualMachineNetworkInterfaceIPConfiguration` was added

* `models.BulkVMConfiguration` was added

* `models.PublicIPAllocationMethod` was added

* `models.CreateFlexResourceOperationResponse` was added

* `models.DiffDiskSettings` was added

* `models.DomainNameLabelScopeTypes` was added

* `models.AdditionalUnattendContent` was added

* `models.KeyVaultSecretReference` was added

* `models.KeyVaultKeyReference` was added

* `models.EventGridAndResourceGraph` was added

* `models.LinuxVMGuestPatchAutomaticByPlatformRebootSetting` was added

* `models.OperatingSystemTypes` was added

* `models.AdditionalUnattendContentComponentName` was added

* `models.DiskEncryptionSettings` was added

* `models.DiskDetachOptionTypes` was added

* `models.BootDiagnostics` was added

* `models.PriorityProfile` was added

* `models.HardwareProfile` was added

* `models.NetworkInterfaceAuxiliaryMode` was added

* `models.WinRMConfiguration` was added

* `models.PublicIPAddressSku` was added

* `models.Placement` was added

* `models.LinuxVMGuestPatchAutomaticByPlatformSettings` was added

* `models.SshConfiguration` was added

* `models.UserAssignedIdentitiesValue` was added

* `models.BulkActionVMExtension` was added

* `models.OSDisk` was added

* `models.VmSizeProfile` was added

* `models.NetworkInterfaceReferenceProperties` was added

* `models.Plan` was added

* `models.ManagedDiskParameters` was added

* `models.DiskDeleteOptionTypes` was added

* `models.LinuxPatchSettings` was added

* `models.BulkActionVMProperties` was added

* `models.AdditionalCapabilities` was added

* `models.ApplicationProfile` was added

* `models.PatchSettings` was added

* `models.AllocationStrategy` was added

* `models.PublicIPAddressSkuTier` was added

* `models.EncryptionIdentity` was added

* `models.OSImageNotificationProfile` was added

* `models.VirtualMachinePublicIPAddressDnsSettingsConfiguration` was added

* `models.WindowsVMGuestPatchMode` was added

* `models.LinuxVMGuestPatchMode` was added

* `models.WindowsPatchAssessmentMode` was added

* `models.IPVersions` was added

* `models.LinuxPatchAssessmentMode` was added

* `models.VirtualMachineIpTag` was added

* `models.DiskCreateOptionTypes` was added

* `models.SettingNames` was added

* `models.AdditionalUnattendContentPassName` was added

* `models.ResourceProvisionFlexPayload` was added

* `models.VirtualMachineNetworkInterfaceIPConfigurationProperties` was added

* `models.ExecuteCreateFlexRequest` was added

* `models.VirtualMachinePublicIPAddressConfiguration` was added

* `models.DiskEncryptionSetParameters` was added

* `models.ZonePreference` was added

* `models.VirtualHardDisk` was added

* `models.DiskControllerTypes` was added

* `models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting` was added

* `models.DataDisk` was added

* `models.DistributionStrategy` was added

* `models.OsType` was added

* `models.ExtendedLocation` was added

#### `models.RetryPolicy` was modified

* `onFailureAction()` was added
* `withOnFailureAction(models.ResourceOperationType)` was added

#### `models.ResourceOperationType` was modified

* `DELETE` was added
* `CREATE` was added

#### `models.ScheduledActions` was modified

* `virtualMachinesExecuteCreateFlex(java.lang.String,models.ExecuteCreateFlexRequest)` was added
* `virtualMachinesExecuteCreateFlexWithResponse(java.lang.String,models.ExecuteCreateFlexRequest,com.azure.core.util.Context)` was added

#### `models.ResourceProvisionPayload` was modified

* `withVirtualMachineBaseProfile(models.BulkVMConfiguration)` was added
* `virtualMachineBaseProfile()` was added
* `virtualMachineOverrides()` was added
* `withVirtualMachineOverrides(java.util.List)` was added

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
