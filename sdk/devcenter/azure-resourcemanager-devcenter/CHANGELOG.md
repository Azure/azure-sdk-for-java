# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2022-11-24)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-preview-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ErrorAdditionalInfo` was removed

* `models.ErrorDetail` was removed

#### `models.DevBoxDefinition` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.DevCenter` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.EnvironmentType` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Image` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Catalog` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.ImageVersion` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Schedule` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.OperationStatusResult` was modified

* `models.ErrorDetail error()` -> `com.azure.core.management.exception.ManagementError error()`
* `withError(models.ErrorDetail)` was removed

#### `models.Project` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.OperationStatus` was modified

* `models.ErrorDetail error()` -> `com.azure.core.management.exception.ManagementError error()`

#### `models.AttachedNetworkConnection` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.NetworkConnection` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`
* `runHealthChecksWithResponse(com.azure.core.util.Context)` was removed

#### `models.Pool` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.Gallery` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.AllowedEnvironmentType` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

#### `models.NetworkConnections` was modified

* `runHealthChecksWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ProjectEnvironmentType` was modified

* `java.lang.String provisioningState()` -> `models.ProvisioningState provisioningState()`

### Features Added

* `models.ProvisioningState` was added

* `models.CheckNameAvailabilityReason` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.HibernateSupport` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.CheckNameAvailabilities` was added

#### `models.DevBoxDefinition` was modified

* `hibernateSupport()` was added

#### `models.DevCenter` was modified

* `devCenterUri()` was added

#### `models.DevBoxDefinitionUpdate` was modified

* `withHibernateSupport(models.HibernateSupport)` was added
* `hibernateSupport()` was added

#### `models.DevBoxDefinition$Update` was modified

* `withHibernateSupport(models.HibernateSupport)` was added

#### `models.DevBoxDefinition$Definition` was modified

* `withHibernateSupport(models.HibernateSupport)` was added

#### `models.OperationStatusResult` was modified

* `withError(com.azure.core.management.exception.ManagementError)` was added

#### `models.Project` was modified

* `devCenterUri()` was added

#### `models.NetworkConnection` was modified

* `runHealthChecks(com.azure.core.util.Context)` was added

#### `DevCenterManager` was modified

* `checkNameAvailabilities()` was added

#### `models.NetworkConnections` was modified

* `runHealthChecks(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.3 (2022-11-18)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-preview-2022-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.OperationStatusError` was removed

#### `models.Schedules` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate,java.lang.Integer,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate)` was removed

#### `models.Schedule$Update` was modified

* `withTypePropertiesType(models.ScheduledType)` was removed

#### `models.OperationStatus` was modified

* `models.OperationStatusError error()` -> `models.ErrorDetail error()`

#### `DevCenterManager` was modified

* `fluent.DevCenterClient serviceClient()` -> `fluent.DevCenterManagementClient serviceClient()`

### Features Added

* `models.ErrorAdditionalInfo` was added

* `models.CatalogSyncState` was added

* `models.OperationStatusResult` was added

* `models.ErrorDetail` was added

#### `models.Catalog` was modified

* `syncState()` was added

#### `models.Schedule$Update` was modified

* `withTags(java.util.Map)` was added
* `withType(models.ScheduledType)` was added

#### `models.OperationStatus` was modified

* `operations()` was added

## 1.0.0-beta.2 (2022-10-12)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2022-09-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Schedules` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,java.lang.String,models.ScheduleUpdate,java.lang.Integer)` was removed
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer)` was removed

### Features Added

* `models.ProjectAllowedEnvironmentTypes` was added

* `models.AllowedEnvironmentTypeListResult` was added

* `models.AllowedEnvironmentType` was added

#### `models.DevCenter` was modified

* `systemData()` was added

#### `models.Image` was modified

* `systemData()` was added

#### `models.OperationStatus` was modified

* `resourceId()` was added

#### `DevCenterManager` was modified

* `projectAllowedEnvironmentTypes()` was added

## 1.0.0-beta.1 (2022-08-19)

- Azure Resource Manager DevCenter client library for Java. This package contains Microsoft Azure SDK for DevCenter Management SDK. DevCenter Management API. Package tag package-2022-08-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
