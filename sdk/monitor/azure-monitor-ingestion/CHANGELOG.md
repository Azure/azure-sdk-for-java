# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.1 (2023-03-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to version `1.37.0`.
- Upgraded `azure-core-http-netty` from `1.13.0` to version `1.13.1`.

## 1.0.0 (2023-02-15)

### Features Added
- Added `logsUploadErrorConsumer` to `LogsUploadOptions` that is called when there's any service error while uploading logs.
- `upload` methods on `LogsIngestionClient` now take an `Iterable` of logs instead of `List`.

### Breaking Changes
- Removed `UploadLogsResult` from the response of `upload` methods in `LogsIngestionClient`

### Other Changes

#### Dependency Updates
- Upgraded `azure-core` to `1.36.0`.
- Upgraded `azure-core-http-netty` to `1.13.0`.

## 1.0.0-beta.2 (2022-08-17)

### Other Changes

#### Dependency updates
- Upgraded `azure-core` to version `1.31.0`.
- Upgraded `azure-core-http-netty` to version `1.12.4`.

## 1.0.0-beta.1 (2022-06-17)
Version 1.0.0-beta.1 is a preview of our efforts in creating a client library for Azure Logs Ingestion that is
developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as
possible. The principles that guide our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

## Features Added
- Initial release. Please see the README for information on using the new library.
