# Release History

## 1.3.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0 (2026-06-11)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package api-version 2018-09-15. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ArmTemplateList` was removed

#### `models.FormulaList` was removed

#### `models.ScheduleList` was removed

#### `models.VirtualNetworkList` was removed

#### `models.UserList` was removed

#### `models.ArtifactList` was removed

#### `models.DtlEnvironmentList` was removed

#### `models.LabList` was removed

#### `models.SecretList` was removed

#### `models.ApplicableScheduleFragment` was removed

#### `models.PolicyList` was removed

#### `models.OperationMetadataDisplay` was removed

#### `models.ServiceFabricList` was removed

#### `models.ProviderOperationResult` was removed

#### `models.GalleryImageList` was removed

#### `models.OperationMetadata` was removed

#### `models.DiskList` was removed

#### `models.LabVirtualMachineList` was removed

#### `models.LabVhdList` was removed

#### `models.ArtifactSourceList` was removed

#### `models.NotificationChannelList` was removed

#### `models.CustomImageList` was removed

#### `models.TargetCostProperties` was modified

* `validate()` was removed

#### `models.PolicyViolation` was modified

* `PolicyViolation()` was changed to private access
* `validate()` was removed
* `withMessage(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed

#### `models.Subnet` was modified

* `validate()` was removed

#### `models.GenerateUploadUriParameter` was modified

* `validate()` was removed

#### `models.ArtifactSourceFragment` was modified

* `validate()` was removed

#### `models.IdentityProperties` was modified

* `validate()` was removed

#### `models.ExternalSubnet` was modified

* `ExternalSubnet()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.GalleryImageReference` was modified

* `validate()` was removed

#### `models.DtlEnvironmentFragment` was modified

* `validate()` was removed

#### `models.HourDetails` was modified

* `validate()` was removed

#### `models.Port` was modified

* `validate()` was removed

#### `models.FormulaFragment` was modified

* `validate()` was removed

#### `models.DetachDiskProperties` was modified

* `validate()` was removed

#### `models.LinuxOsInfo` was modified

* `validate()` was removed

#### `models.LabSupportProperties` was modified

* `validate()` was removed

#### `models.ScheduleFragment` was modified

* `validate()` was removed

#### `models.ParameterInfo` was modified

* `validate()` was removed

#### `models.EnvironmentDeploymentProperties` was modified

* `validate()` was removed

#### `models.ArtifactParameterProperties` was modified

* `validate()` was removed

#### `models.ComputeDataDisk` was modified

* `ComputeDataDisk()` was changed to private access
* `withManagedDiskId(java.lang.String)` was removed
* `withDiskSizeGiB(java.lang.Integer)` was removed
* `withDiskUri(java.lang.String)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed

#### `models.CustomImagePropertiesFromPlan` was modified

* `validate()` was removed

#### `models.WeekDetails` was modified

* `validate()` was removed

#### `models.FormulaPropertiesFromVm` was modified

* `validate()` was removed

#### `models.UserSecretStore` was modified

* `validate()` was removed

#### `models.SecretFragment` was modified

* `validate()` was removed

#### `models.RetargetScheduleProperties` was modified

* `validate()` was removed

#### `models.SubnetOverride` was modified

* `validate()` was removed

#### `models.BulkCreationParameters` was modified

* `validate()` was removed

#### `models.NotificationChannelFragment` was modified

* `validate()` was removed

#### `models.ServiceFabricFragment` was modified

* `validate()` was removed

#### `models.SubnetSharedPublicIpAddressConfiguration` was modified

* `validate()` was removed

#### `models.Event` was modified

* `validate()` was removed

#### `models.PolicySetResult` was modified

* `PolicySetResult()` was changed to private access
* `withHasError(java.lang.Boolean)` was removed
* `withPolicyViolations(java.util.List)` was removed
* `validate()` was removed

#### `models.LabResourceCostProperties` was modified

* `LabResourceCostProperties()` was changed to private access
* `withExternalResourceId(java.lang.String)` was removed
* `validate()` was removed
* `withResourceId(java.lang.String)` was removed
* `withResourceOwner(java.lang.String)` was removed
* `withResourceUId(java.lang.String)` was removed
* `withResourceCost(java.lang.Double)` was removed
* `withResourcename(java.lang.String)` was removed
* `withResourceStatus(java.lang.String)` was removed
* `withResourcePricingTier(java.lang.String)` was removed
* `withResourceType(java.lang.String)` was removed

#### `models.ArmTemplateParameterProperties` was modified

* `validate()` was removed

#### `models.CustomImagePropertiesFromVm` was modified

* `validate()` was removed

#### `models.OperationError` was modified

* `OperationError()` was changed to private access
* `withCode(java.lang.String)` was removed
* `validate()` was removed
* `withMessage(java.lang.String)` was removed

#### `models.DetachDataDiskProperties` was modified

* `validate()` was removed

#### `models.UpdateResource` was modified

* `validate()` was removed

#### `models.DiskFragment` was modified

* `validate()` was removed

#### `models.LabAnnouncementProperties` was modified

* `validate()` was removed

#### `models.ScheduleCreationParameter` was modified

* `validate()` was removed

#### `models.NotificationSettings` was modified

* `validate()` was removed

#### `models.PercentageCostThresholdProperties` was modified

* `validate()` was removed

#### `models.UserFragment` was modified

* `validate()` was removed

#### `models.DataDiskStorageTypeInfo` was modified

* `validate()` was removed

#### `models.VirtualNetworkFragment` was modified

* `validate()` was removed

#### `models.ResizeLabVirtualMachineProperties` was modified

* `validate()` was removed

#### `models.NetworkInterfaceProperties` was modified

* `validate()` was removed

#### `models.NotifyParameters` was modified

* `validate()` was removed

#### `models.EvaluatePoliciesRequest` was modified

* `validate()` was removed

#### `models.SharedPublicIpAddressConfiguration` was modified

* `validate()` was removed

#### `models.CustomImageFragment` was modified

* `validate()` was removed

#### `models.PolicyFragment` was modified

* `validate()` was removed

#### `models.EvaluatePoliciesProperties` was modified

* `validate()` was removed

#### `models.ParametersValueFileInfo` was modified

* `ParametersValueFileInfo()` was changed to private access
* `withFileName(java.lang.String)` was removed
* `validate()` was removed
* `withParametersValueInfo(java.lang.Object)` was removed

#### `models.WindowsOsInfo` was modified

* `validate()` was removed

#### `models.LabCostDetailsProperties` was modified

* `LabCostDetailsProperties()` was changed to private access
* `withCost(java.lang.Double)` was removed
* `validate()` was removed
* `withDate(java.time.OffsetDateTime)` was removed
* `withCostType(models.CostType)` was removed

#### `models.LabCostSummaryProperties` was modified

* `LabCostSummaryProperties()` was changed to private access
* `withEstimatedLabCost(java.lang.Double)` was removed
* `validate()` was removed

#### `models.ComputeVmProperties` was modified

* `ComputeVmProperties()` was changed to private access
* `withDataDiskIds(java.util.List)` was removed
* `withNetworkInterfaceId(java.lang.String)` was removed
* `withDataDisks(java.util.List)` was removed
* `validate()` was removed
* `withOsDiskId(java.lang.String)` was removed
* `withOsType(java.lang.String)` was removed
* `withStatuses(java.util.List)` was removed
* `withVmSize(java.lang.String)` was removed

#### `models.CostThresholdProperties` was modified

* `validate()` was removed

#### `models.UserIdentity` was modified

* `validate()` was removed

#### `models.ArtifactDeploymentStatusProperties` was modified

* `ArtifactDeploymentStatusProperties()` was changed to private access
* `withTotalArtifacts(java.lang.Integer)` was removed
* `withArtifactsApplied(java.lang.Integer)` was removed
* `withDeploymentStatus(java.lang.String)` was removed
* `validate()` was removed

#### `models.LabVirtualMachineFragment` was modified

* `validate()` was removed

#### `models.CustomImagePropertiesCustom` was modified

* `validate()` was removed

#### `models.ComputeVmInstanceViewStatus` was modified

* `ComputeVmInstanceViewStatus()` was changed to private access
* `withDisplayStatus(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed
* `validate()` was removed

#### `models.DayDetails` was modified

* `validate()` was removed

#### `models.LabFragment` was modified

* `validate()` was removed

#### `models.DataDiskProperties` was modified

* `validate()` was removed

#### `models.ApplyArtifactsRequest` was modified

* `validate()` was removed

#### `models.AttachNewDataDiskOptions` was modified

* `validate()` was removed

#### `models.ExportResourceUsageParameters` was modified

* `validate()` was removed

#### `models.AttachDiskProperties` was modified

* `validate()` was removed

#### `models.ArtifactInstallProperties` was modified

* `validate()` was removed

#### `models.LabVirtualMachineCreationParameter` was modified

* `validate()` was removed

#### `models.GenerateArmTemplateRequest` was modified

* `validate()` was removed

#### `models.InboundNatRule` was modified

* `validate()` was removed

#### `DevTestLabsManager` was modified

* `fluent.DevTestLabsClient serviceClient()` -> `fluent.DevTestLabsManagementClient serviceClient()`

#### `models.ImportLabVirtualMachineRequest` was modified

* `validate()` was removed

### Features Added

* `models.Operation` was added

* `models.OperationDisplay` was added

* `models.ActionType` was added

* `models.Origin` was added

#### `models.NotificationChannel` was modified

* `systemData()` was added

#### `models.Artifact` was modified

* `systemData()` was added

#### `models.ApplicableSchedule` was modified

* `systemData()` was added

#### `models.User` was modified

* `systemData()` was added

#### `models.Secret` was modified

* `systemData()` was added

#### `models.ServiceRunner` was modified

* `systemData()` was added

#### `models.CustomImage` was modified

* `systemData()` was added

#### `models.Formula` was modified

* `systemData()` was added

#### `models.ArmTemplate` was modified

* `systemData()` was added

#### `models.LabCost` was modified

* `systemData()` was added

#### `models.Disk` was modified

* `systemData()` was added

#### `models.ServiceFabric` was modified

* `systemData()` was added

#### `models.ArtifactSource` was modified

* `systemData()` was added

#### `models.DtlEnvironment` was modified

* `systemData()` was added

#### `models.Schedule` was modified

* `systemData()` was added

#### `models.GalleryImage` was modified

* `systemData()` was added

#### `models.Policy` was modified

* `systemData()` was added

#### `models.Lab` was modified

* `systemData()` was added

#### `models.LabVirtualMachine` was modified

* `systemData()` was added

#### `models.VirtualNetwork` was modified

* `systemData()` was added

## 1.1.0 (2024-12-13)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package tag package-2018-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

## 1.0.0 (2023-10-27)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package tag package-2018-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package tag package-2018-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ScheduleCreationParameter` was modified

* `withLocation(java.lang.String)` was removed

#### `models.GlobalSchedules` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ServiceFabricProperties` was added

* `models.LabVirtualMachineProperties` was added

* `models.ApplicableScheduleProperties` was added

#### `models.NotificationChannel` was modified

* `resourceGroupName()` was added

#### `models.User` was modified

* `resourceGroupName()` was added

#### `models.Secret` was modified

* `resourceGroupName()` was added

#### `models.ServiceRunner` was modified

* `resourceGroupName()` was added

#### `models.CustomImage` was modified

* `resourceGroupName()` was added

#### `models.Formula` was modified

* `resourceGroupName()` was added

#### `DevTestLabsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.LabCost` was modified

* `resourceGroupName()` was added

#### `models.Disk` was modified

* `resourceGroupName()` was added

#### `models.ServiceFabric` was modified

* `resourceGroupName()` was added

#### `models.ArtifactSource` was modified

* `resourceGroupName()` was added

#### `models.DtlEnvironment` was modified

* `resourceGroupName()` was added

#### `models.Schedule` was modified

* `resourceGroupName()` was added

#### `models.Policy` was modified

* `resourceGroupName()` was added

#### `models.Lab` was modified

* `resourceGroupName()` was added

#### `models.GlobalSchedules` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.LabVirtualMachine` was modified

* `resourceGroupName()` was added

#### `models.VirtualNetwork` was modified

* `resourceGroupName()` was added

#### `DevTestLabsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-15)

- Azure Resource Manager DevTestLabs client library for Java. This package contains Microsoft Azure SDK for DevTestLabs Management SDK. The DevTest Labs Client. Package tag package-2018-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
