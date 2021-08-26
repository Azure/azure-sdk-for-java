# Release History

## 1.0.0-beta.2 (2021-08-26)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2020-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.IssueUpdateContractProperties` was removed

* `models.BackendUpdateParameterProperties` was removed

* `models.NamedValueContractProperties` was removed

* `models.UserContractProperties` was removed

* `models.TagDescriptionContractProperties` was removed

* `models.ApiCreateOrUpdateProperties` was removed

* `models.AuthorizationServerContractProperties` was removed

* `models.OperationUpdateContractProperties` was removed

* `models.ApiContractProperties` was removed

* `models.GroupContractProperties` was removed

* `models.IdentityProviderCreateContractProperties` was removed

* `models.ProductUpdateProperties` was removed

* `models.ApiVersionSetContractProperties` was removed

* `models.TagDescriptionBaseProperties` was removed

* `models.IssueContractProperties` was removed

* `models.ApiVersionSetUpdateParametersProperties` was removed

* `models.NamedValueCreateContractProperties` was removed

* `models.AuthorizationServerUpdateContractProperties` was removed

* `models.ProductContractProperties` was removed

* `models.OperationContractProperties` was removed

* `models.ApiManagementServiceProperties` was removed

* `models.BackendContractProperties` was removed

* `models.QuotaCounterValueContractProperties` was removed

* `models.IdentityProviderUpdateProperties` was removed

* `models.ApiContractUpdateProperties` was removed

* `models.ErrorResponseBody` was removed

* `models.IdentityProviderContractProperties` was removed

* `models.ErrorFieldContract` was removed

* `models.ApiManagementServiceUpdateProperties` was removed

* `models.NamedValueUpdateParameterProperties` was removed

* `models.UserCreateParameterProperties` was removed

* `models.UserUpdateParametersProperties` was removed

#### `models.OperationResultContract` was modified

* `models.ErrorResponseBody error()` -> `com.azure.core.management.exception.ManagementError error()`

#### `models.QuotaCounterContract` was modified

* `models.QuotaCounterValueContractProperties value()` -> `fluent.models.QuotaCounterValueContractProperties value()`

### Features Added

#### `models.TenantConfigurationSyncStateContract` was modified

* `lastOperationId()` was added

#### `ApiManagementManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.OperationResultContract` was modified

* `name()` was added
* `idPropertiesId()` was added
* `type()` was added

## 1.0.0-beta.1 (2021-03-23)

- Azure Resource Manager ApiManagement client library for Java. This package contains Microsoft Azure SDK for ApiManagement Management SDK. ApiManagement Client. Package tag package-2020-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

