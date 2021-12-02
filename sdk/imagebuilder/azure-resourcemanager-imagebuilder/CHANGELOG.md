# Release History

## 1.0.0-beta.2 (2021-12-02)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ApiError` was removed

* `models.InnerError` was removed

* `models.ApiErrorException` was removed

### Features Added

#### `models.VirtualNetworkConfig` was modified

* `proxyVmSize()` was added
* `withProxyVmSize(java.lang.String)` was added

#### `models.ImageTemplatePlatformImageSource` was modified

* `exactVersion()` was added

#### `models.ImageTemplateVmProfile` was modified

* `withUserAssignedIdentities(java.util.List)` was added
* `userAssignedIdentities()` was added

#### `ImageBuilderManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.ImageTemplate` was modified

* `systemData()` was added

## 1.0.0-beta.1 (2021-05-17)

- Azure Resource Manager ImageBuilder client library for Java. This package contains Microsoft Azure SDK for ImageBuilder Management SDK. Azure Virtual Machine Image Builder Client. Package tag package-2020-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

