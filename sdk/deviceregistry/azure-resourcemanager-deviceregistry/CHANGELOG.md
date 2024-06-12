# Release History

## 1.0.0-beta.2 (2024-06-12)

- Azure Resource Manager Device Registry client library for Java. This package contains Microsoft Azure SDK for Device Registry Management SDK. Microsoft.DeviceRegistry Resource Provider management API. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `implementation.models.PagedOperation` was removed

* `implementation.models.AssetListResult` was removed

* `implementation.models.AssetEndpointProfileListResult` was removed

#### `models.OperationStatusResult` was modified

* `java.lang.Integer percentComplete()` -> `java.lang.Float percentComplete()`

#### `models.AssetEndpointProfileUpdateProperties` was modified

* `models.TransportAuthentication transportAuthentication()` -> `models.TransportAuthenticationUpdate transportAuthentication()`
* `models.UserAuthentication userAuthentication()` -> `models.UserAuthenticationUpdate userAuthentication()`
* `withTransportAuthentication(models.TransportAuthentication)` was removed
* `withUserAuthentication(models.UserAuthentication)` was removed

#### `DeviceRegistryManager` was modified

* `fluent.DeviceRegistryClient serviceClient()` -> `fluent.DeviceRegistryMgmtClient serviceClient()`

### Features Added

* `models.OperationListResult` was added

* `models.AssetEndpointProfileListResult` was added

* `models.UsernamePasswordCredentialsUpdate` was added

* `models.TransportAuthenticationUpdate` was added

* `models.UserAuthenticationUpdate` was added

* `models.AssetListResult` was added

* `models.X509CredentialsUpdate` was added

#### `models.AssetStatus` was modified

* `withErrors(java.util.List)` was added
* `withVersion(java.lang.Integer)` was added

#### `models.AssetEndpointProfileUpdateProperties` was modified

* `withUserAuthentication(models.UserAuthenticationUpdate)` was added
* `withTransportAuthentication(models.TransportAuthenticationUpdate)` was added

#### `models.AssetStatusError` was modified

* `withMessage(java.lang.String)` was added
* `withCode(java.lang.Integer)` was added

## 1.0.0-beta.1 (2024-04-26)

- Azure Resource Manager Device Registry client library for Java. This package contains Microsoft Azure SDK for Device Registry Management SDK. Microsoft.DeviceRegistry Resource Provider management API. Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
