# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2022-05-19)

- Azure Resource Manager ServiceLinker client library for Java. This package contains Microsoft Azure SDK for ServiceLinker Management SDK. Microsoft.ServiceLinker provider. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Type` was removed

* `models.ValidateResult` was removed

#### `models.Linkers` was modified

* `models.ValidateResult validate(java.lang.String,java.lang.String,com.azure.core.util.Context)` -> `models.ValidateOperationResult validate(java.lang.String,java.lang.String,com.azure.core.util.Context)`
* `models.ValidateResult validate(java.lang.String,java.lang.String)` -> `models.ValidateOperationResult validate(java.lang.String,java.lang.String)`

#### `models.LinkerResource` was modified

* `models.ValidateResult validate()` -> `models.ValidateOperationResult validate()`
* `models.ValidateResult validate(com.azure.core.util.Context)` -> `models.ValidateOperationResult validate(com.azure.core.util.Context)`

### Features Added

* `models.ValidateOperationResult` was added

* `models.TargetServiceType` was added

* `models.AzureResourceType` was added

## 1.0.0-beta.1 (2022-04-15)

- Azure Resource Manager ServiceLinker client library for Java. This package contains Microsoft Azure SDK for ServiceLinker Management SDK. Microsoft.ServiceLinker provider. Package tag package-2022-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
