# Release History

## 1.0.4 (2024-09-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to version `1.52.0`.
- Upgraded `azure-core-http-netty` from `1.15.3` to version `1.15.4`.


## 1.0.3 (2024-08-24)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.2` to version `1.15.3`.
- Upgraded `azure-core` from `1.50.0` to version `1.51.0`.


## 1.0.2 (2024-07-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.15.1` to version `1.15.2`.
- Upgraded `azure-core` from `1.49.1` to version `1.50.0`.


## 1.0.1 (2024-06-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to version `1.49.1`.
- Upgraded `azure-core-http-netty` from `1.14.2` to version `1.15.1`.

## 1.0.0 (2024-04-30)

This release targets Azure Dev Center 2023-04-01 General Available API, which is the same version as the previous 1.0.0-beta.3 release. The main improvement was the addition of models as Convenience API was enabled for the SDK. 

### Features Added

- Added models and models serialization for each Dev Center concept.
    - DevBox
    - DevBoxAction
    - DevBoxActionDelayResult
    - DevBoxActionDelayStatus
    - DevBoxActionType
    - DevBoxHardwareProfile
    - DevBoxImageReference
    - DevBoxNextAction
    - DevBoxOsType
    - DevBoxPool
    - DevBoxProvisioningState
    - DevBoxSchedule
    - DevBoxStorageProfile
    - DevCenterCatalog
    - DevCenterEnvironment
    - DevCenterEnvironmentType
    - DevCenterOperationDetails
    - DevCenterOperationStatus
    - DevCenterProject
    - EnvironmentDefinition
    - EnvironmentDefinitionParameter
    - EnvironmentDefinitionParameterType
    - EnvironmentProvisioningState
    - EnvironmentTypeStatus
    - HibernateSupport
    - LocalAdministratorStatus
    - OsDisk
    - PoolHealthStatus
    - PowerState
    - RemoteConnection
    - ScheduleFrequency
    - ScheduleType
    - SkuName
    - StopOnDisconnectConfiguration
    - StopOnDisconnectStatus
    
- For each previous client method, added the correspondent method with model return. E.g. for `getDevBoxWithResponse` method, which returns `Response<BinaryData>`, was added a correspondent `getDevBox` method, which returns `DevBox` model.   
- Added `getDevBoxesClient()` and `getDeploymentEnvironmentsClient()` methods in `DevCenterClient` 
- Added `getDevBoxesAsyncClient()` and `getDeploymentEnvironmentsAsyncClient()` methods in `DevCenterAsyncClient`

### Breaking Changes

- Removed `filter` and `top` as optional request parameters to match equivalent update in the API side.

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

