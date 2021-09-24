# Release History

## 1.0.0-beta.2 (2021-09-24)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourceIdentityType` was removed

#### `models.SqlManagedInstances` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.SqlServerInstances` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.DataControllers` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.SqlManagedInstances` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SqlServerInstances` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DataControllers` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2021-07-21)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
