# Release History

## 1.0.0-beta.22 (2024-09-12)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.51.0` to `1.52.0`.
- Upgraded `opentelemetry-api` from `1.40.0` to `1.41.0`.

## 1.0.0-beta.21 (2024-07-31)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to `1.51.0`.
- Upgraded `opentelemetry-api` from `1.38.0` to `1.40.0`.

## 1.0.0-beta.20 (2024-06-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.
- Upgraded `opentelemetry-api` from `1.37.0` to `1.38.0`.

## 1.0.0-beta.19 (2024-05-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.
- Upgraded `opentelemetry-api` from `1.36.0` to `1.37.0`.

## 1.0.0-beta.18 (2024-04-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.
- Upgraded `opentelemetry-api` from `1.35.0` to `1.36.0`.

## 1.0.0-beta.17 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.
- Upgraded `opentelemetry-api` from `1.34.1` to `1.35.0`.

## 1.0.0-beta.16 (2024-02-02)

### Features Added

- Updated OpenTelemetry Semantic Conventions to version 1.23.1.

### Breaking Changes

- Renamed attributes according to OpenTelemetry semantic conventions changes:
    - `net.peer.name` -> `server.address`
    - `otel.status_code` -> `error.type`

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 1.0.0-beta.15 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.
- Upgraded `opentelemetry-api` from `1.28.0` to `1.31.0`.

## 1.0.0-beta.14 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 1.0.0-beta.13 (2023-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.0.0-beta.12 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.0.0-beta.11 (2023-08-04)

### Other Changes

#### Dependency Updates
- Upgraded `opentelemetry-api` from `1.27.0` to `1.28.0`.
- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 1.0.0-beta.10 (2023-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.0.0-beta.9 (2023-06-02)

### Breaking Changes

- Replaced `OpenTelemetryMetricsOptions.setProvider` with `OpenTelemetryMetricsOptions.setOpenTelemetry` method. Instead of `io.opentelemetry.api.metrics.MeterProvider` instance 
  it now takes `io.opentelemetry.api.OpenTelemetry` container.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.0.0-beta.8 (2023-05-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.0.0-beta.7 (2023-04-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 1.0.0-beta.6 (2023-03-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.
- Upgraded OpenTelemetry from `1.20.0` to `1.23.0`.

## 1.0.0-beta.5 (2023-02-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.
- - Upgraded OpenTelemetry from `1.14.0` to `1.20.0`.

## 1.0.0-beta.4 (2023-01-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.

## 1.0.0-beta.3 (2022-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 1.0.0-beta.2 (2022-10-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to `1.33.0`.

## 1.0.0-beta.1 (2022-09-01)

- Initial release. Please see the README for more information.
