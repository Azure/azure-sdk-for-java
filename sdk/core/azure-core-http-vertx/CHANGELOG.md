# Release History

## 1.0.0-beta.21 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.20 (2024-07-31)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to `1.51.0`.

## 1.0.0-beta.19 (2024-06-06)

### Features Added

- Added support for response timeout and per-request response timeout. ([#40017](https://github.com/Azure/azure-sdk-for-java/pull/40017))

### Breaking Changes

- Removed `VertxAsyncHttpClientBuilder.idleTimeout`, renamed `readIdleTimeout` to `readTimeout` and `writeIdleTimeout`
  to `writeTimeout` in `VertxAsyncHttpClientBuilder`. ([#40017](https://github.com/Azure/azure-sdk-for-java/pull/40017))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.
- Upgraded Vertx from `4.5.7` to `4.5.8`.

## 1.0.0-beta.18 (2024-05-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.

## 1.0.0-beta.17 (2024-04-05)

### Bugs Fixed

- Fixed a bug where memory from a shared pool wasn't copied on use, leading to potential memory corruption issues if
  it wasn't consumed before the pool reclaimed it.
- Standardized errors returned by `VertxAsyncHttpClient` to use `IOException` when issues happened in the underlying
  `Vertx` client, instead of the `RuntimeException` used by `Vertx` itself.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.
- Upgraded Vertx from `4.5.4` to `4.5.7`.

## 1.0.0-beta.16 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.
- Upgraded Vertx from `4.5.0` to `4.5.4`.

## 1.0.0-beta.15 (2024-02-02)

### Bugs Fixed

- Removed usage of `Scheduler`, resolving issues with GraalVM. ([#3704](https://github.com/Azure/azure-sdk-for-java/pull/37041)) 

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 1.0.0-beta.14 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.

## 1.0.0-beta.13 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 1.0.0-beta.12 (2023-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.0.0-beta.11 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.0.0-beta.10 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 1.0.0-beta.9 (2023-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.0.0-beta.8 (2023-06-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.0.0-beta.7 (2023-05-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.0.0-beta.6 (2023-04-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 1.0.0-beta.5 (2023-03-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.

## 1.0.0-beta.4 (2023-02-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.

## 1.0.0-beta.3 (2023-01-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.
- Upgraded Vertx from `4.3.3` to `4.3.7`.

## 1.0.0-beta.2 (2022-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 1.0.0-beta.1 (2022-10-07)

### Features Added

- Initial release. Please see the README and wiki for information on the new design.
