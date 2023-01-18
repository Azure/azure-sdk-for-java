# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-13)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.JobResource$DefinitionStages` was modified

* `withTransferType(models.TransferType)` was removed in stage 3
* `withSku(models.Sku)` was removed in stage 4

#### `models.AddressValidationOutput` was modified

* `alternateAddresses()` was removed
* `error()` was removed
* `validationStatus()` was removed

### Features Added

#### `models.AddressValidationOutput` was modified

* `properties()` was added

#### `DataBoxManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `DataBoxManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.JobResource` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
