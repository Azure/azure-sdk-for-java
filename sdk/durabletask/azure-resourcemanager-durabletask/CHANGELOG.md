# Release History

## 1.0.0 (2025-09-25)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK.  Package api-version 2025-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SchedulerSku` was modified

* `validate()` was removed
* `java.lang.String name()` -> `models.SchedulerSkuName name()`
* `withName(java.lang.String)` was removed

#### `models.RetentionPolicyDetails` was modified

* `validate()` was removed

#### `models.SchedulerPropertiesUpdate` was modified

* `validate()` was removed

#### `models.RetentionPolicyProperties` was modified

* `validate()` was removed

#### `models.SchedulerProperties` was modified

* `validate()` was removed

#### `models.TaskHubProperties` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.SchedulerSkuUpdate` was modified

* `validate()` was removed
* `withName(java.lang.String)` was removed
* `java.lang.String name()` -> `models.SchedulerSkuName name()`

#### `models.SchedulerUpdate` was modified

* `validate()` was removed

### Features Added

* `models.SchedulerSkuName` was added

#### `models.SchedulerSku` was modified

* `withName(models.SchedulerSkuName)` was added

#### `models.SchedulerSkuUpdate` was modified

* `withName(models.SchedulerSkuName)` was added

## 1.0.0-beta.2 (2025-04-24)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.RetentionPolicyProperties` was added

* `models.RetentionPolicies` was added

* `models.RetentionPolicyDetails` was added

* `implementation.models.RetentionPolicyListResult` was added

* `models.RetentionPolicy` was added

* `models.PurgeableOrchestrationState` was added

#### `DurableTaskManager` was modified

* `retentionPolicies()` was added

## 1.0.0-beta.1 (2025-03-25)

- Azure Resource Manager Durable Task client library for Java. This package contains Microsoft Azure SDK for Durable Task Management SDK. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-durabletask Java SDK.
