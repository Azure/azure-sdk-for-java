# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.4 (2024-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.11` to version `1.14.0`.
- Upgraded `azure-core` from `1.45.1` to version `1.46.0`.


## 1.1.3 (2023-12-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.10` to version `1.13.11`.
- Upgraded `azure-core` from `1.45.0` to version `1.45.1`.


## 1.1.2 (2023-11-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to version `1.45.0`.
- Upgraded `azure-core-http-netty` from `1.13.9` to version `1.13.10`.


## 1.1.1 (2023-10-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to version `1.44.1`.
- Upgraded `azure-core-http-netty` from `1.13.7` to version `1.13.9`.


## 1.1.0 (2023-09-13)

### Features Added
- `LogsIngestionClient` now implements `AutoCloseable` interface and can be used in try-with-resources block.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to version `1.43.0`.
- Upgraded `azure-core-http-netty` from `1.13.6` to version `1.13.7`.

## 1.0.6 (2023-08-18)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to version `1.42.0`.
- Upgraded `azure-core-http-netty` from `1.13.5` to version `1.13.6`.

## 1.0.5 (2023-07-25)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to version `1.41.0`.
- Upgraded `azure-core-http-netty` from `1.13.4` to version `1.13.5`.

## 1.0.4 (2023-06-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to version `1.40.0`.
- Upgraded `azure-core-http-netty` from `1.13.3` to version `1.13.4`.

## 1.0.3 (2023-05-23)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core-http-netty` from `1.13.2` to version `1.13.3`.
- Upgraded `azure-core` from `1.38.0` to version `1.39.0`.

## 1.0.2 (2023-04-26)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to version `1.38.0`.
- Upgraded `azure-core-http-netty` from `1.13.1` to version `1.13.2`.

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
