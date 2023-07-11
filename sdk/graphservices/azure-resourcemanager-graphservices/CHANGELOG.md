# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2023-06-26)

- Azure Resource Manager GraphServices client library for Java. This package contains Microsoft Azure SDK for GraphServices Management SDK. Self service experience for Microsoft Graph metered services. Package tag package-2023-04-13. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AccountOperations` was removed

#### `GraphServicesManager` was modified

* `accountOperations()` was removed

### Features Added

#### `models.Accounts` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `getByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `getByResourceGroup(java.lang.String,java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2023-03-16)

- Azure Resource Manager GraphServices client library for Java. This package contains Microsoft Azure SDK for GraphServices Management SDK. Self service experience for Microsoft Graph metered services. Package tag package-2022-09-22-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
