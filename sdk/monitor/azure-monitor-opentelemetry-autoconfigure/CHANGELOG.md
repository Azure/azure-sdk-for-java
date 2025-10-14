# Release History

## 1.5.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.4.0 (2025-09-24)

### Features Added

- Added mapping for `enduser.pseudo.id` attribute to `user_Id` ([#46506](https://github.com/Azure/azure-sdk-for-java/pull/46506)

### Breaking Changes

- Updated mapping for `enduser.id` attribute from `user_Id` to `user_AuthenticatedId` ([#46506](https://github.com/Azure/azure-sdk-for-java/pull/46506)

## 1.3.0 (2025-08-01)

### Bugs Fixed
- Don't call .toString() or .length() on null strings/urls ([#44933](https://github.com/Azure/azure-sdk-for-java/pull/44933))
- Fix null exception message error ([#46001](https://github.com/Azure/azure-sdk-for-java/pull/46001))
- Fix live metrics race condition ([#45944](https://github.com/Azure/azure-sdk-for-java/pull/45944))

## 1.2.0 (2025-04-11)

### Bugs Fixed
- Fixed a bug causing logs to be instrumented. Verbose logs would lead to recursive logging. ([#44828](https://github.com/Azure/azure-sdk-for-java/pull/44828))

### Other Changes
- Update OpenTelemetry SDK to 1.49.0 ([#44950](https://github.com/Azure/azure-sdk-for-java/pull/44950))

## 1.1.0 (2025-03-20)

### Features Added
- Support for parsing AAD Audience from the connection string ([#44482](https://github.com/Azure/azure-sdk-for-java/pull/44482))
- Support for custom events ([#44262](https://github.com/Azure/azure-sdk-for-java/pull/44262))
- Support for live metrics ([#44653](https://github.com/Azure/azure-sdk-for-java/pull/44653))
- Update OpenTelemetry SDK to 1.48.0 ([#44675](https://github.com/Azure/azure-sdk-for-java/pull/44675))

## 1.0.0 (2025-03-07)

### Other Changes
- General availability release.

## 1.0.0-beta.3 (2025-02-24)

### Bugs Fixed
- Fixed redirection logic for QuickPulse service calls when only the instrumentation key is provided in the connection string. ([#44211](https://github.com/Azure/azure-sdk-for-net/pull/44211))

## 1.0.0-beta.2 (2025-01-31)

### Other Changes
- [Update OpenTelemetry SDK to 1.46.0](https://github.com/Azure/azure-sdk-for-java/pull/43974)

## 1.0.0-beta.1 (2025-01-16)

### Features Added
- Initial release. This library replaces Azure Monitor OpenTelemetry Exporter.
