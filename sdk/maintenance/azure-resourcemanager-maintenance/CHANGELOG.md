# Release History

## 1.0.0-beta.3 (Unreleased)


## 1.0.0-beta.2 (2021-05-13)

- Azure Resource Manager Maintenance client library for Java. This package contains Microsoft Azure SDK for Maintenance Management SDK. Azure Maintenance Management Client. Package tag package-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Change

#### `MaintenanceManager` was modified

* `fluent.MaintenanceClient serviceClient()` -> `fluent.MaintenanceManagementClient serviceClient()`

### New Feature

* `models.PublicMaintenanceConfigurations` was added

* `models.ListApplyUpdate` was added

* `models.Visibility` was added

* `models.ApplyUpdateForResourceGroups` was added

* `models.MaintenanceConfigurationsForResourceGroups` was added

#### `models.ApplyUpdate` was modified

* `systemData()` was added

#### `models.MaintenanceConfiguration$Definition` was modified

* `withTimeZone(java.lang.String)` was added
* `withVisibility(models.Visibility)` was added
* `withDuration(java.lang.String)` was added
* `withRecurEvery(java.lang.String)` was added
* `withExpirationDateTime(java.lang.String)` was added
* `withStartDateTime(java.lang.String)` was added

#### `MaintenanceManager` was modified

* `maintenanceConfigurationsForResourceGroups()` was added
* `publicMaintenanceConfigurations()` was added
* `applyUpdateForResourceGroups()` was added

#### `models.ConfigurationAssignment` was modified

* `systemData()` was added

#### `models.Operation` was modified

* `isDataAction()` was added

#### `models.ApplyUpdates` was modified

* `list()` was added
* `list(com.azure.core.util.Context)` was added

#### `models.MaintenanceConfiguration` was modified

* `expirationDateTime()` was added
* `timeZone()` was added
* `duration()` was added
* `systemData()` was added
* `visibility()` was added
* `recurEvery()` was added
* `startDateTime()` was added

#### `models.MaintenanceConfiguration$Update` was modified

* `withDuration(java.lang.String)` was added
* `withRecurEvery(java.lang.String)` was added
* `withStartDateTime(java.lang.String)` was added
* `withExpirationDateTime(java.lang.String)` was added
* `withVisibility(models.Visibility)` was added
* `withTimeZone(java.lang.String)` was added

## 1.0.0-beta.1 (2021-04-16)

- Azure Resource Manager Maintenance client library for Java. This package contains Microsoft Azure SDK for Maintenance Management SDK. Maintenance Client. Package tag package-2020-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
