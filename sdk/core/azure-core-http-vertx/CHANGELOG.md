# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.1 (2025-09-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.56.0` to `1.56.1`.
- Upgraded Vert.x from `4.5.15` to `4.5.17`. ([#46430](https://github.com/Azure/azure-sdk-for-java/pull/46430))

## 1.1.0 (2025-08-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.5` to `1.56.0`.
- Upgraded Reactor from `3.4.41` to `3.7.8`. ([#46207](https://github.com/Azure/azure-sdk-for-java/pull/46207))

## 1.0.5 (2025-06-26)

### Bugs Fixed

- Fixed a bug where Vert.x 5.x would throw an error when attempting to create a `VertxHttpClient`. ([#45709](https://github.com/Azure/azure-sdk-for-java/pull/45709))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.4` to `1.55.5`.

## 1.0.4 (2025-06-05)

### Bugs 

- Fixed a bug where `TooLongHttpHeaderException` would be thrown if an Azure service returned HTTP headers that exceeded
  the default 8 KB limit. The new limit is now 256 KB when the default of 8 KB is seen. ([#45291](https://github.com/Azure/azure-sdk-for-java/pull/45291))

### Other Changes

- Few small changes to implementation to improve support for Vert.x 5.x.

#### Dependency Updates

- Upgraded `azure-core` from `1.55.3` to `1.55.4`.
- Upgraded Vertx from `4.5.13` to `4.5.15`.

## 1.0.3 (2025-03-10)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.2` to `1.55.3`.
- Upgraded `vertx-codegen` from `4.5.10` to `4.5.13`.

## 1.0.2 (2025-02-20)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.1` to `1.55.2`.

## 1.0.1 (2025-02-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.55.0` to `1.55.1`.

## 1.0.0 (2025-02-06)

Initial GA of `azure-core-http-vertx`.

### Bugs Fixed

- Fixed a bug where a request would fail with `IllegalStateException("Result is already complete")` when multiple
  exceptions happened during the request. When multiple exceptions happen, the first exception is now thrown and 
  subsequent exceptions are added to the suppressed exceptions of the first exception. ([#43402](https://github.com/Azure/azure-sdk-for-java/pull/43402))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.54.1` to `1.55.0`.

## 1.0.0-beta.24 (2024-11-14)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.54.0` to `1.54.1`.

## 1.0.0-beta.23 (2024-11-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.53.0` to `1.54.0`.

## 1.0.0-beta.22 (2024-10-01)

### Breaking Changes

- Renamed `VertxAsyncHttpClient` to `VertxHttpClient`, `VertxAsyncHttpClientBuilder` to `VertxHttpClientBuilder`, and
  `VertxAsyncHttpClientProvider` to `VertxHttpClientProvider`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.52.0` to `1.53.0`.
- Upgraded Vert.x from `4.5.8` to `4.5.10`.

## 1.0.0-beta.21 (2024-09-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to `1.52.0`.

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
