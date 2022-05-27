# Release History

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

