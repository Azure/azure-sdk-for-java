# Release History

## 1.0.0 (2026-08-18)

- Azure Resource Manager Service Groups client library for Java. This package contains Microsoft Azure SDK for Service Groups Management SDK.  Package api-version 2026-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ServiceGroupCollectionResponse` was removed

#### `models.ServiceGroups` was modified

* `listAncestorsWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `listAncestors(java.lang.String)` was removed

### Features Added

* `models.OperationDisplay` was added

* `models.ActionType` was added

* `models.Operation` was added

* `models.Operations` was added

* `models.Origin` was added

* `models.ServiceGroupAttributes` was added

#### `ServiceGroupsManager` was modified

* `operations()` was added

#### `models.ServiceGroupProperties` was modified

* `withAttributes(models.ServiceGroupAttributes)` was added
* `attributes()` was added

## 1.0.0-beta.2 (2026-03-27)

- Azure Resource Manager Service Groups client library for Java. This package contains Microsoft Azure SDK for Service Groups Management SDK. The Groups RP provides Service Groups as a construct to group multiple resources, resource groups, subscriptions and other service groups into an organizational hierarchy and centrally manage access control, policies, alerting and reporting for those resources. Package api-version 2024-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ServiceGroupProperties` was modified

* `validate()` was removed

#### `models.ParentServiceGroupProperties` was modified

* `validate()` was removed

## 1.0.0-beta.1 (2026-03-25)

- Azure Resource Manager Service Groups client library for Java. This package contains Microsoft Azure SDK for Service Groups Management SDK. The Groups RP provides Service Groups as a construct to group multiple resources, resource groups, subscriptions and other service groups into an organizational hierarchy and centrally manage access control, policies, alerting and reporting for those resources. Package tag package-2024-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-servicegroups Java SDK.
