# Release History

## 1.0.0-beta.2 (2026-08-27)

- Azure Resource Manager Azure Terraform client library for Java. This package contains Microsoft Azure SDK for Azure Terraform Management SDK. The Azure Terraform management API provides a RESTful set of web services that used to manage your Azure Terraform resources. Package api-version 2026-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Terraforms` was modified

* `void exportTerraform(models.BaseExportModel)` -> `models.TerraformOperationStatus exportTerraform(models.BaseExportModel)`
* `void exportTerraform(models.BaseExportModel,com.azure.core.util.Context)` -> `models.TerraformOperationStatus exportTerraform(models.BaseExportModel,com.azure.core.util.Context)`

#### `models.ExportResourceGroup` was modified

* `validate()` was removed

#### `models.ExportQuery` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.ExportResource` was modified

* `validate()` was removed

#### `AzureTerraformManager` was modified

* `fluent.AzureTerraformClient serviceClient()` -> `fluent.AzureTerraformManagementClient serviceClient()`

#### `models.BaseExportModel` was modified

* `validate()` was removed

### Features Added

* `models.ExportResult` was added

* `models.TerraformOperationStatus` was added

* `models.AuthorizationScopeFilter` was added

* `models.ResourceProvisioningState` was added

* `models.AzureExtensionResourceType` was added

#### `models.ExportResourceGroup` was modified

* `withIncludeExtensions(java.util.List)` was added
* `withExcludeAzureResource(java.util.List)` was added
* `withIncludeRoleAssignment(java.lang.Boolean)` was added
* `withExcludeTerraformResource(java.util.List)` was added
* `withIncludeManagedResource(java.lang.Boolean)` was added

#### `models.ExportQuery` was modified

* `withIncludeManagedResource(java.lang.Boolean)` was added
* `withExcludeAzureResource(java.util.List)` was added
* `withIncludeRoleAssignment(java.lang.Boolean)` was added
* `withIncludeResourceGroup(java.lang.Boolean)` was added
* `withIncludeExtensions(java.util.List)` was added
* `withExcludeTerraformResource(java.util.List)` was added
* `authorizationScopeFilter()` was added
* `withAuthorizationScopeFilter(models.AuthorizationScopeFilter)` was added
* `table()` was added
* `withTable(java.lang.String)` was added
* `includeResourceGroup()` was added

#### `models.ExportResource` was modified

* `withIncludeManagedResource(java.lang.Boolean)` was added
* `withExcludeAzureResource(java.util.List)` was added
* `withIncludeExtensions(java.util.List)` was added
* `recursive()` was added
* `includeResourceGroup()` was added
* `withIncludeResourceGroup(java.lang.Boolean)` was added
* `withIncludeRoleAssignment(java.lang.Boolean)` was added
* `withRecursive(java.lang.Boolean)` was added
* `withExcludeTerraformResource(java.util.List)` was added

#### `models.BaseExportModel` was modified

* `includeManagedResource()` was added
* `withIncludeExtensions(java.util.List)` was added
* `withIncludeRoleAssignment(java.lang.Boolean)` was added
* `includeExtensions()` was added
* `withExcludeAzureResource(java.util.List)` was added
* `withIncludeManagedResource(java.lang.Boolean)` was added
* `excludeTerraformResource()` was added
* `includeRoleAssignment()` was added
* `withExcludeTerraformResource(java.util.List)` was added
* `excludeAzureResource()` was added

## 1.0.0-beta.1 (2024-11-20)

- Azure Resource Manager Azure Terraform client library for Java. This package contains Microsoft Azure SDK for Azure Terraform Management SDK. The Azure Terraform management API provides a RESTful set of web services that used to manage your Azure Terraform resources. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- Initial release for the azure-resourcemanager-terraform Java SDK.
