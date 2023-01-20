# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-19)

- Azure Resource Manager StorageImportExport client library for Java. This package contains Microsoft Azure SDK for StorageImportExport Management SDK. The Storage Import/Export Resource Provider API. Package tag package-preview-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorResponse` was removed

* `models.ErrorResponseException` was removed

#### `models.Jobs` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ErrorResponseError` was added

* `models.ErrorResponseErrorException` was added

#### `StorageImportExportManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `StorageImportExportManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Jobs` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.JobResponse` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-20)

- Azure Resource Manager StorageImportExport client library for Java. This package contains Microsoft Azure SDK for StorageImportExport Management SDK. The Storage Import/Export Resource Provider API. Package tag package-preview-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
