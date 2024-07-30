# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2024-05-20)

- Azure Resource Manager Maintenance client library for Java. This package contains Microsoft Azure SDK for Maintenance Management SDK. Azure Maintenance Management Client. Package tag package-preview-2023-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ScheduledEventApproveResponse` was added

* `models.ScheduledEvents` was added

#### `MaintenanceManager` was modified

* `scheduledEvents()` was added

#### `models.ApplyUpdates` was modified

* `createOrUpdateOrCancelWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.ApplyUpdateInner,com.azure.core.util.Context)` was added
* `createOrUpdateOrCancel(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.ApplyUpdateInner)` was added

## 1.0.0 (2023-08-22)

- Azure Resource Manager Maintenance client library for Java. This package contains Microsoft Azure SDK for Maintenance Management SDK. Azure Maintenance Management Client. Package tag package-2023-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.TagOperators` was added

* `models.ConfigurationAssignment$DefinitionStages` was added

* `models.InputPatchConfiguration` was added

* `models.TagSettingsProperties` was added

* `models.ConfigurationAssignment$Update` was added

* `models.ConfigurationAssignmentsWithinSubscriptions` was added

* `models.ConfigurationAssignmentFilterProperties` was added

* `models.ConfigurationAssignment$UpdateStages` was added

* `models.RebootOptions` was added

* `models.ConfigurationAssignmentsForResourceGroups` was added

* `models.ConfigurationAssignmentsForSubscriptions` was added

* `models.InputLinuxParameters` was added

* `models.ConfigurationAssignment$Definition` was added

* `models.InputWindowsParameters` was added

#### `models.MaintenanceConfiguration$Definition` was modified

* `withInstallPatches(models.InputPatchConfiguration)` was added

#### `models.ConfigurationAssignments` was modified

* `getParent(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `getParentWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `MaintenanceManager` was modified

* `configurationAssignmentsForResourceGroups()` was added
* `configurationAssignmentsForSubscriptions()` was added
* `configurationAssignmentsWithinSubscriptions()` was added

#### `models.ConfigurationAssignment` was modified

* `filter()` was added
* `refresh(com.azure.core.util.Context)` was added
* `regionName()` was added
* `refresh()` was added
* `region()` was added
* `update()` was added

#### `models.MaintenanceConfiguration` was modified

* `installPatches()` was added

#### `models.MaintenanceConfiguration$Update` was modified

* `withInstallPatches(models.InputPatchConfiguration)` was added

## 1.0.0-beta.3 (2023-01-18)

- Azure Resource Manager Maintenance client library for Java. This package contains Microsoft Azure SDK for Maintenance Management SDK. Azure Maintenance Management Client. Package tag package-2021-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.MaintenanceConfigurations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.MaintenanceConfigurations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `MaintenanceManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `MaintenanceManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `models.MaintenanceConfiguration` was modified

* `resourceGroupName()` was added

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
