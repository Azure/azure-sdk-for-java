# Release History

## 1.8.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.7.1 (2021-09-07)

### Fixed

- Fixed a `NullPointerException` when loading test `HttpClient`. ([#23559](https://github.com/Azure/azure-sdk-for-java/pull/23559))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.

## 1.7.0 (2021-08-06)

### Features Added

- Added `AzureMethodSource` which enables creating test permutations using `HttpClient`, `ServiceVersion`, and a generic
  method source supplier function. ([#20484](https://github.com/Azure/azure-sdk-for-java/pull/20484))
- Added utility methods to `TestBase` to allow setting playback test polling and setting playback or live test `HttpClient`.

### Dependency Updates

- Upgraded `azure-core` from `1.18.0` to `1.19.0`.

## 1.6.4 (2021-07-01)

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.6.3 (2021-06-07)

### Features Added

- Updated JUnit 5 test status logging to only long when `AZURE_TEST_DEBUG` is `true` instead of always logging.

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.
- Upgraded JUnit 5 from `5.7.1` to `5.7.2`.

## 1.6.2 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.

## 1.6.1 (2021-04-02)

### Dependency Updates

- Upgraded `azure-core` from `1.14.0` to `1.15.0`.

## 1.6.0 (2021-03-08)

### Dependency Updates

- Updated `azure-core` from `1.13.0` to `1.14.0`.

## 1.5.3 (2021-02-05)

### Dependency Updates

- Updated `azure-core` from `1.12.0` to `1.13.0`.

## 1.5.2 (2021-01-11)

### Dependency Updates

- Updated `azure-core` from `1.10.0` to `1.12.0`.

## 1.5.1 (2020-10-29)

### Dependency Updates

- Updated `azure-core` to `1.10.0`.

### Bug Fixes

- Fixed a bug in test recording redaction for redacting empty values for json key-value pairs.

## 1.5.0 (2020-10-01)

### New Features

- Enhanced playback recording to use test class name plus test name to identify records.

### Bug Fixes

- Added additional response data redaction.
- Updated handling of `HttpClient` retrieval from the classpath to no longer require dependent libraries to `add-opens` in Java 9+.

## 1.4.2 (2020-09-08)

- Updated `azure-core` version to pickup bug fix.

## 1.4.1 (2020-09-03)

- Updated `azure-core` dependency.

## 1.4.0 (2020-08-07)

- Added `AzureTestWatch` which implements JUnit's `Extension` SPI. It adds test watching extensibility such as logging test completion time.
- Fixed a bug in test recording redaction which was accidentally redacting XML values when they shouldn't be redacted.
- Enhanced recording of `application/octet-stream` and `avro/binary` data to be more space efficient and faster to write and read.

## 1.3.1 (2020-07-02)

- Updated Azure Core dependency.

## 1.3.0 (2020-06-08)

- Added new APIs used to redact sensitive information from playback recordings.
- Updated Azure Core dependency.

## 1.2.1 (2020-05-04)

- Updating dependencies.

## 1.2.0 (2020-04-03)

- Updating dependencies.

## 1.1.0 (2020-01-07)

## 1.1.0-beta.2 (2019-12-18)
- Quick release to have client library depends on the right version of azure-core and azure-core-test changes.

## Version 1.1.0-beta.1 (2019-12-17)
- Added log message if playback json file is missing.
- Fixed bug which record failed for newly added tests.
- Switch to JUnit version 5.
- Allowed to record body when content type is not set.

## Version 1.0.0 (2019-10-29)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-test_1.0.0/sdk/core/azure-core-test/README.md)

- Initial release. Please see the README and wiki for information on the new design.
