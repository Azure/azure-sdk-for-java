# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2026-03-06)

- Azure Resource Manager ManagedOps client library for Java. This package contains Microsoft Azure SDK for ManagedOps Management SDK. Managed Operations API. Package api-version 2025-07-28-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DesiredConfigurationDefenderForServers` was removed

#### `models.ChangeTrackingInformationEnablementStatus` was removed

#### `models.ChangeTrackingInformation` was modified

* `models.ChangeTrackingInformationEnablementStatus enablementStatus()` -> `models.EnablementState enablementStatus()`

#### `models.DefenderForServersInformation` was modified

* `models.ChangeTrackingInformationEnablementStatus enablementStatus()` -> `models.EnablementState enablementStatus()`

#### `models.DesiredConfigurationUpdate` was modified

* `models.DesiredConfigurationDefenderForServers defenderForServers()` -> `models.DesiredEnablementState defenderForServers()`
* `withDefenderCspm(models.DesiredConfigurationDefenderForServers)` was removed
* `withDefenderForServers(models.DesiredConfigurationDefenderForServers)` was removed
* `models.DesiredConfigurationDefenderForServers defenderCspm()` -> `models.DesiredEnablementState defenderCspm()`

#### `models.DesiredConfiguration` was modified

* `withDefenderCspm(models.DesiredConfigurationDefenderForServers)` was removed
* `models.DesiredConfigurationDefenderForServers defenderForServers()` -> `models.DesiredEnablementState defenderForServers()`
* `withDefenderForServers(models.DesiredConfigurationDefenderForServers)` was removed
* `models.DesiredConfigurationDefenderForServers defenderCspm()` -> `models.DesiredEnablementState defenderCspm()`

#### `models.DefenderCspmInformation` was modified

* `models.ChangeTrackingInformationEnablementStatus enablementStatus()` -> `models.EnablementState enablementStatus()`

#### `models.GuestConfigurationInformation` was modified

* `models.ChangeTrackingInformationEnablementStatus enablementStatus()` -> `models.EnablementState enablementStatus()`

#### `models.UpdateManagerInformation` was modified

* `models.ChangeTrackingInformationEnablementStatus enablementStatus()` -> `models.EnablementState enablementStatus()`

#### `models.AzureMonitorInformation` was modified

* `models.ChangeTrackingInformationEnablementStatus enablementStatus()` -> `models.EnablementState enablementStatus()`

### Features Added

* `models.EnablementState` was added

* `models.DesiredEnablementState` was added

#### `models.DesiredConfigurationUpdate` was modified

* `withDefenderCspm(models.DesiredEnablementState)` was added
* `withDefenderForServers(models.DesiredEnablementState)` was added

#### `models.DesiredConfiguration` was modified

* `withDefenderForServers(models.DesiredEnablementState)` was added
* `withDefenderCspm(models.DesiredEnablementState)` was added

## 1.0.0-beta.1 (2026-02-13)

- Azure Resource Manager ManagedOps client library for Java. This package contains Microsoft Azure SDK for ManagedOps Management SDK. Managed Operations API. Package api-version 2025-07-28-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-managedops Java SDK.

