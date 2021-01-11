# Release History

## 1.6.0-beta.1 (Unreleased)


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
