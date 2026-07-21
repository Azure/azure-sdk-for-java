# Release History

## 1.0.0-beta.2 (2026-07-21)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-07-06-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ResourceOperation` was modified

* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String errorDetails()` -> `java.lang.String errorDetails()`
* `models.ResourceOperationDetails operation()` -> `models.ResourceOperationDetails operation()`
* `java.lang.String resourceId()` -> `java.lang.String resourceId()`
* `java.lang.String errorCode()` -> `java.lang.String errorCode()`
* `fromJson(com.azure.json.JsonReader)` was removed

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

* `models.EvictionPolicy` was added

* `models.LocationBasedLaunchBulkInstancesOperation$UpdateStages` was added

* `models.WindowsVMGuestPatchMode` was added

* `models.LinuxPatchAssessmentMode` was added

* `models.DiagnosticsProfile` was added

* `models.ExecuteCreateContent` was added

* `models.WindowsConfiguration` was added

* `models.ProtocolTypes` was added

* `models.WindowsPatchAssessmentMode` was added

* `models.DeleteOptions` was added

* `models.AdditionalUnattendContent` was added

* `models.VirtualMachinePublicIPAddressConfiguration` was added

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

* `models.DiffDiskPlacement` was added

* `models.KeyVaultKeyReference` was added

* `models.ResourcesWithContext` was added

* `models.DiskCreateOptionTypes` was added

* `models.SecurityTypes` was added

* `models.ReimageResourceOverride` was added

* `models.FlexProperties` was added

* `models.WindowsVMGuestPatchAutomaticByPlatformRebootSetting` was added

* `models.ProxyAgentSettings` was added

* `models.ExecuteVdiCreateRequest` was added

* `models.LinuxVMGuestPatchAutomaticByPlatformSettings` was added

* `models.ImageReference` was added

* `models.StorageProfile` was added

* `models.ZonePreference` was added

* `models.VMDiskSecurityProfile` was added

* `models.PublicIPAddressSku` was added

* `models.ApiErrorBase` was added

* `models.VMGalleryApplication` was added

* `models.SecurityEncryptionTypes` was added

* `models.KeyVaultSecretReference` was added

* `models.LocationBasedLaunchBulkInstancesOperation` was added

* `models.PatchSettings` was added

* `models.HardwareProfile` was added

* `models.BulkActionVmExtensionProperties` was added

* `models.OSProfile` was added

* `models.UefiSettings` was added

* `models.Mode` was added

* `models.OperationStatusResult` was added

* `models.AcceleratorManufacturer` was added

* `models.PriorityType` was added

* `models.VirtualMachineInfo` was added

* `models.ExecuteReimageRequest` was added

* `models.VirtualMachineNetworkInterfaceDnsSettingsConfiguration` was added

* `models.LinuxConfiguration` was added

* `models.ApplicationProfile` was added

* `models.NetworkInterfaceReference` was added

* `models.ResourceNotificationDetails` was added

* `models.DiskDetachOptionTypes` was added

* `models.LocationBasedLaunchBulkInstancesOperation$Definition` was added

* `models.ComputeProfile` was added

* `models.AllInstancesDown` was added

* `models.NetworkApiVersion` was added

* `models.VirtualMachineNetworkInterfaceConfiguration` was added

* `models.DataDisk` was added

* `models.SshConfiguration` was added

* `models.Modes` was added

* `models.ScheduledEventsPolicy` was added

* `models.DistributionStrategy` was added

* `models.ManagedServiceIdentityType` was added

* `models.ProvisioningState` was added

* `models.VMAttributeSupport` was added

* `models.IPVersions` was added

* `models.ResourceProvisionVdiPayload` was added

* `models.AdditionalCapabilities` was added

* `models.SshPublicKey` was added

* `models.InnerError` was added

* `models.PublicIPAddressSkuTier` was added

* `models.NetworkInterfaceAuxiliarySku` was added

* `models.BulkactionVMExtension` was added

* `models.SettingNames` was added

* `models.LocationBasedLaunchBulkInstancesOperation$Update` was added

* `models.Plan` was added

* `models.DiffDiskSettings` was added

* `models.NetworkInterfaceAuxiliaryMode` was added

* `models.DiskDeleteOptionTypes` was added

* `models.UserInitiatedRedeploy` was added

* `models.EncryptionIdentity` was added

* `models.NetworkProfile` was added

* `models.VirtualMachineReimageParameters` was added

* `models.SecurityProfile` was added

* `models.VirtualMachine` was added

* `models.HostEndpointSettings` was added

* `models.AdditionalUnattendContentPassName` was added

* `models.ApiEntityReference` was added

* `models.ZoneAllocationPolicy` was added

* `models.VirtualMachineNetworkInterfaceConfigurationProperties` was added

* `models.VMCategory` was added

* `models.VirtualMachineNetworkInterfaceIPConfiguration` was added

* `models.CachingTypes` was added

* `models.VirtualMachineNetworkInterfaceIPConfigurationProperties` was added

* `models.DiskEncryptionSettings` was added

* `models.AdditionalUnattendContentComponentName` was added

* `models.ReimageResourceOperationResponse` was added

* `models.LocalStorageDiskType` was added

* `models.VaultCertificate` was added

* `models.TerminateNotificationProfile` was added

* `models.LocationBasedLaunchBulkInstancesOperation$DefinitionStages` was added

* `models.CapacityReservationProfile` was added

* `models.CapacityType` was added

* `models.CpuManufacturer` was added

* `models.ScheduledEventsAdditionalPublishingTargets` was added

* `models.VMOperationStatus` was added

* `models.VMAttributeMinMaxInteger` was added

* `models.VmSizeProfile` was added

* `models.ResourceProvisionPayload` was added

* `models.PublicIPAddressSkuName` was added

* `models.DiskEncryptionSetParametersContent` was added

* `models.CreateResourceOperationResponse` was added

* `models.OperatingSystemTypes` was added

* `models.AllocationStrategy` was added

* `models.ArchitectureType` was added

* `models.AcknowledgeBulkOperationErrorsResponse` was added

* `models.LaunchBulkInstancesOperations` was added

* `models.VirtualHardDisk` was added

* `models.VMAttributeMinMaxDouble` was added

* `models.LinuxVMGuestPatchMode` was added

* `models.UserAssignedIdentity` was added

* `models.ManagedDiskParametersContent` was added

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

* `models.VMAttributes` was added

* `models.HyperVGeneration` was added

#### `models.VirtualMachineBulkOperations` was modified

* `bulkAcknowledgeOperationErrors(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest)` was added
* `bulkReimageOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteReimageRequest,com.azure.core.util.Context)` was added
* `bulkAcknowledgeOperationErrorsWithResponse(java.lang.String,java.lang.String,models.AcknowledgeBulkOperationErrorsRequest,com.azure.core.util.Context)` was added
* `bulkReimageOperation(java.lang.String,java.lang.String,models.ExecuteReimageRequest)` was added
* `bulkListOperationErrors(java.lang.String,java.lang.String,java.lang.Integer,com.azure.core.util.Context)` was added
* `bulkVdiFlexCreateOperation(java.lang.String,java.lang.String,models.ExecuteVdiCreateRequest)` was added
* `bulkListOperationErrors(java.lang.String,java.lang.String)` was added
* `bulkCreateOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteCreateContent,com.azure.core.util.Context)` was added
* `bulkVdiFlexCreateOperationWithResponse(java.lang.String,java.lang.String,models.ExecuteVdiCreateRequest,com.azure.core.util.Context)` was added
* `bulkCreateOperation(java.lang.String,java.lang.String,models.ExecuteCreateContent)` was added

#### `models.ExecuteDeallocateContent` was modified

* `resourcesWithContext()` was added
* `withResourcesWithContext(models.ResourcesWithContext)` was added

#### `models.ResourceOperation` was modified

* `innerModel()` was added
* `virtualMachineInfo()` was added

#### `models.ResourceOperationDetails` was modified

* `resourceNotificationDetails()` was added

#### `models.ExecutionParameters` was modified

* `verifyVmAgentHealth()` was added
* `withVerifyVmAgentHealth(java.lang.Boolean)` was added

#### `models.ResourceOperationType` was modified

* `GET_INSTANCE_VIEW` was added

#### `models.ExecuteStartContent` was modified

* `withResourcesWithContext(models.ResourcesWithContext)` was added
* `resourcesWithContext()` was added

#### `ComputeBulkActionsManager` was modified

* `launchBulkInstancesOperations()` was added

#### `models.ExecuteHibernateContent` was modified

* `resourcesWithContext()` was added
* `withResourcesWithContext(models.ResourcesWithContext)` was added

#### `models.ExecuteDeleteContent` was modified

* `withResourcesWithContext(models.ResourcesWithContext)` was added
* `resourcesWithContext()` was added

## 1.0.0-beta.1 (2026-07-01)

- Azure Resource Manager Compute BulkActions client library for Java. This package contains Microsoft Azure SDK for Compute BulkActions Management SDK.  Package api-version 2026-06-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-compute-bulkactions Java SDK.
