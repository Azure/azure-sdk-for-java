# Release History

## 1.0.0-beta.4 (Unreleased)


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
