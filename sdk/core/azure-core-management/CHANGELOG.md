# Release History

## 1.4.2 (2021-10-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.

## 1.4.1 (2021-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.

## 1.4.0 (2021-08-06)

### Features Added

- Added new Azure region `Region.US_WEST3`.

## 1.3.1 (2021-07-01)

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.3.0 (2021-06-07)

### Features Added

- Added Support for Challenge Based Authentication in `ArmChallengeAuthenticationPolicy`.
  
### Fixed

- Fixed bug in `ManagementErrorDeserializer`. ([#21615](https://github.com/Azure/azure-sdk-for-java/issues/21615))

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.

## 1.2.2 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.

## 1.2.1 (2021-04-02)

### Dependency Updates

- Upgraded `azure-core` from `1.14.0` to `1.15.0`.

## 1.2.0 (2021-03-08)

### New Features

- Added `SystemData`.

### Dependency Updates

- Upgraded `azure-core` from `1.13.0` to `1.14.0`.

## 1.1.1 (2021-02-05)

- Fixed long-running operation, PUT method, response 200 and Azure-AsyncOperation.

## 1.1.0 (2021-01-11)

### New Features

- Added `MICROSOFT_GRAPH` to `AzureEnvironment`.

### Bug Fixes

- Fixed long-running operation, PUT method, response 201 and Location, succeeded without poll.

## 1.0.0 (2020-09-24)

- Updated class method names.
- Fixed long-running operation, PUT method, response 201 and Azure-AsyncOperation, succeeded without poll.
- Added `Region` class.

## 1.0.0-beta.3 (2020-08-07)

- Added optional `Context` parameter to methods in `PollerFactory` class, which will be shared for all polling requests.
- Added `getResponseHeaders()` method to `PollResult.Error` class.
- Added `AzureProfile` class.
- Added `IdentifierProvider` and `DelayProvider` interfaces.
- Fixed polling status HTTP status code check to include `202`.

## 1.0.0-beta.2 (2020-07-09)

- Added additional `ManagementError` constructors.
- Added additional `PollingState` checks.
- Fixed polling status HTTP status code check to include `204`.

## 1.0.0-beta.1 (2020-06-17)
- `PollerFactory` for polling of long-running operation.
- `ManagementException` and `ManagementError` for exception and error handling.

## 1.0.0-preview.4 (2019-09-09)

## Version 1.0.0-preview.3 (2019-08-05)

## Version 1.0.0-preview.1 (2019-06-28)
- Initial release. This package contains Microsoft Azure SDK for Template.
