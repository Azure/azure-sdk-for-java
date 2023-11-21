# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2023-11-09)

This release updates the Azure DevCenter library to use the 2023-04-01 GA API.

### Breaking Changes

 - `EnvironmentsClient` renamed to `DeploymentEnvironmentsClient`
 - `DeploymentEnvironmentsClient` now works with "environment definitions" instead of "catalog items"
 - Creating a new environment requires passing `environmentDefinitionName` instead of `catalogItemName`
 - Creating a new environment requires passing an additional parameter `catalogName`
 - All `actions` methods had `Upcoming` removed from their names. E.g. `delayUpcomingAction` was renamed to `delayAction`
 - All `Schedules` methods had `ByPool` removed from their names. E.g. `getScheduleByPool` was renamed to `getSchedule`
 - `delayActions` renamed to `delayAllActions`
 - `ByUser` was removed from all methods names. E.g. `listDevBoxesByUser` was renamed to `listDevBoxes`. Functionalities and required parameters of the methods remain the same.
 - List dev boxes got moved from `DevCenterClient` to `DevBoxClient`

## 1.0.0-beta.2 (2023-02-07)

This release updates the Azure DevCenter library to use the 2022-11-11-preview API.

### Breaking Changes

- `DevBoxClientBuilder`, `DevCenterClientBuilder`, and `EnvironmentsClientBuilder` now accept an endpoint URI on construction rather than tenant ID + dev center name.

### Features Added

- Added upcoming actions APIs to dev boxes.
    - `delayUpcomingActionWithResponse`
    - `getUpcomingActionWithResponse`
    - `listUpcomingActions`
    - `skipUpcomingActionWithResponse`

### Bugs Fixed

- Invalid `beginDeleteEnvironmentAction` API removed from `EnvironmentsClient`.
- Unimplemented artifacts APIs removed from `EnvironmentsClient`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from version `1.34.0` to version `1.36.0`.
- Upgraded `azure-core-http-netty` from version `1.12.7` to version `1.13.0`.

## 1.0.0-beta.1 (2022-11-11)

- This package contains Microsoft Azure DevCenter client library.

### Features Added
Initial release for the azure-developer-devcenter Java SDK.

