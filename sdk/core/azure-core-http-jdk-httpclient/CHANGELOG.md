# Release History

## 1.0.0-beta.17 (Unreleased)

### Features Added

### Breaking Changes

- Change the default `ExecutorService` used by `JdkHttpClientBuilder` from using what the JDK `HttpClient` instatiates
  to using `SharedExecutorService` if `JdkHttpClientBuilder.executor` isn't set.

### Bugs Fixed

### Other Changes

## 1.0.0-beta.16 (2024-09-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to `1.52.0`.

## 1.0.0-beta.15 (2024-07-31)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to `1.51.0`.

## 1.0.0-beta.14 (2024-06-06)

### Features Added

- Added support for per-request response timeout. ([#40017](https://github.com/Azure/azure-sdk-for-java/pull/40017))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.

## 1.0.0-beta.13 (2024-05-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.

## 1.0.0-beta.12 (2024-04-05)

### Features Added

- Added support for write, response, and read timeout in `JdkHttpClientBuilder`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.

## 1.0.0-beta.11 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.

## 1.0.0-beta.10 (2024-02-02)

### Other Changes

- Optimized performance of handling HTTP headers. ([#38285](https://github.com/Azure/azure-sdk-for-java/pull/38285))

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 1.0.0-beta.9 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.

## 1.0.0-beta.8 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 1.0.0-beta.7 (2023-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.0.0-beta.6 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.0.0-beta.5 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 1.0.0-beta.4 (2023-07-06)

### Bugs Fixed

- Removed needlessly wrapping with an `UncheckedIOException` on `IOExpection` in the method of 
  `writeBodyTo(WritableByteChannel channel)` of class `JdkHttpResponseSync`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.0.0-beta.3 (2023-06-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.0.0-beta.2 (2023-05-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.0.0-beta.1 (2023-04-21)
This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-http-jdk-httpclient/README.md)

Initial release. Please see the README and wiki for information on the new design.


