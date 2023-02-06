# Release History

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

