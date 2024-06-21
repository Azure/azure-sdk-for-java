# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2024-06-21)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ImageTemplateAutoRun` was added

* `models.AutoRunState` was added

#### `models.ImageTemplateDistributor` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateVhdDistributor` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.ImageTemplateShellValidator` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateFileCustomizer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ImageTemplateUpdateParametersProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withVmProfile(models.ImageTemplateVmProfile)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `vmProfile()` was added

#### `models.ImageTemplateUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VirtualNetworkConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withContainerInstanceSubnetId(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `containerInstanceSubnetId()` was added

#### `models.DistributeVersionerSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `scheme()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TargetRegion` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplatePowerShellCustomizer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PlatformImagePurchasePlan` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DistributeVersionerLatest` was modified

* `scheme()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateSharedImageDistributor` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ImageTemplatePlatformImageSource` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateVmProfile` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DistributeVersioner` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `scheme()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateManagedImageDistributor` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RunOutputCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateSharedImageVersionSource` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateRestartCustomizer` was modified

* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateInVMValidator` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ImageTemplatePropertiesOptimize` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplatePropertiesOptimizeVmBoot` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplate` was modified

* `managedResourceTags()` was added
* `autoRun()` was added

#### `models.ImageTemplate$Definition` was modified

* `withManagedResourceTags(java.util.Map)` was added
* `withAutoRun(models.ImageTemplateAutoRun)` was added

#### `models.ImageTemplateFileValidator` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateManagedImageSource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.ImageTemplateShellCustomizer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateLastRunStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceImageTriggerProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `status()` was added
* `provisioningState()` was added
* `kind()` was added

#### `models.ImageTemplateCustomizer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TriggerStatus` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplatePropertiesValidate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ImageTemplateSource` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProvisioningError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TriggerProperties` was modified

* `kind()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplatePowerShellValidator` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added

#### `models.ImageTemplatePropertiesErrorHandling` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImageTemplateWindowsUpdateCustomizer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

## 1.0.0 (2024-01-23)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2023-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ImageTemplateUpdateParametersProperties` was added

* `models.OnBuildError` was added

* `models.ImageTemplatePropertiesErrorHandling` was added

#### `models.ImageTemplateUpdateParameters` was modified

* `properties()` was added
* `withProperties(models.ImageTemplateUpdateParametersProperties)` was added

#### `models.ImageTemplate$Update` was modified

* `withProperties(models.ImageTemplateUpdateParametersProperties)` was added

#### `models.ImageTemplate` was modified

* `errorHandling()` was added

#### `models.ImageTemplate$Definition` was modified

* `withErrorHandling(models.ImageTemplatePropertiesErrorHandling)` was added

## 1.0.0-beta.4 (2023-03-21)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2022-07. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ImageTemplateIdentityUserAssignedIdentities` was removed

### Features Added

* `models.DistributeVersionerSource` was added

* `models.TargetRegion` was added

* `models.DistributeVersionerLatest` was added

* `models.DistributeVersioner` was added

* `models.Trigger` was added

* `models.Triggers` was added

* `models.Trigger$Update` was added

* `models.ImageTemplatePropertiesOptimize` was added

* `models.ImageTemplatePropertiesOptimizeVmBoot` was added

* `models.ImageTemplateFileValidator` was added

* `models.UserAssignedIdentity` was added

* `models.SourceImageTriggerProperties` was added

* `models.TriggerCollection` was added

* `models.TriggerStatus` was added

* `models.Trigger$DefinitionStages` was added

* `models.TriggerProperties` was added

* `models.Trigger$Definition` was added

* `models.Trigger$UpdateStages` was added

* `models.VMBootOptimizationState` was added

#### `ImageBuilderManager` was modified

* `triggers()` was added

#### `models.ImageTemplateVhdDistributor` was modified

* `withUri(java.lang.String)` was added
* `uri()` was added

#### `models.ImageTemplateSharedImageDistributor` was modified

* `withTargetRegions(java.util.List)` was added
* `withVersioning(models.DistributeVersioner)` was added
* `versioning()` was added
* `targetRegions()` was added

#### `models.ImageTemplateSharedImageVersionSource` was modified

* `exactVersion()` was added

#### `models.ImageTemplate` was modified

* `optimize()` was added
* `systemData()` was added

#### `models.ImageTemplate$Definition` was modified

* `withOptimize(models.ImageTemplatePropertiesOptimize)` was added

#### `models.RunOutput` was modified

* `systemData()` was added

## 1.0.0-beta.3 (2022-05-27)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2022-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ImageTemplate` was modified

* `systemData()` was removed

### Features Added

* `models.ImageTemplateShellValidator` was added

* `models.ImageTemplateInVMValidator` was added

* `models.ImageTemplatePropertiesValidate` was added

* `models.ImageTemplatePowerShellValidator` was added

#### `ImageBuilderManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `ImageBuilderManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.ImageTemplate` was modified

* `resourceGroupName()` was added
* `stagingResourceGroup()` was added
* `validation()` was added
* `exactStagingResourceGroup()` was added

#### `models.ImageTemplate$Definition` was modified

* `withStagingResourceGroup(java.lang.String)` was added
* `withValidation(models.ImageTemplatePropertiesValidate)` was added

## 1.0.0-beta.2 (2021-12-07)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2020-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).





* `withScope(java.lang.String)` was added

### Breaking Changes

* `models.ApiError` was removed

* `models.InnerError` was removed

* `models.ApiErrorException` was removed

### Features Added

#### `models.VirtualNetworkConfig` was modified

* `withProxyVmSize(java.lang.String)` was added
* `proxyVmSize()` was added

#### `models.ImageTemplatePlatformImageSource` was modified

* `exactVersion()` was added

#### `models.ImageTemplateVmProfile` was modified

* `userAssignedIdentities()` was added
* `withUserAssignedIdentities(java.util.List)` was added

#### `ImageBuilderManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.ImageTemplate` was modified

* `systemData()` was added

## 1.0.0-beta.1 (2021-05-17)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2020-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

