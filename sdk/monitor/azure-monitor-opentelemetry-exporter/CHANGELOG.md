# Release History

## 1.0.0-beta.5 (Unreleased)


## 1.0.0-beta.4 (2021-03-10)

### New Features
- `AzureMonitorExporterBuilder` now supports reading connection string from `APPLICATIONINSIGHTS_CONNECTION_STRING
` environment variable.

### Dependency Updates
- Updated versions of `opentelemetry-api` and `opentelemetry-sdk` to `1.0.0` version.
  More detailed information about the new OpenTelemetry API version can be found in [OpenTelemetry changelog](https://github.com/open-telemetry/opentelemetry-java/blob/main/CHANGELOG.md#version-100---2021-02-26).
- Updated `azure-core` version to 1.14.0.
- Updated `azure-core-http-netty` version to 1.9.0.

## 1.0.0-beta.3 (2021-02-09)

### Breaking changes
- Renamed artifact to `azure-monitor-opentelemetry-exporter`.

### Dependency Updates
- Updated versions of `opentelemetry-api` and `opentelemetry-sdk` to `0.14.1` version.

## 1.0.0-beta.2 (2021-01-12)
### Breaking changes
- Renamed artifact to `azure-opentelemetry-exporter-azuremonitor`.
- Replaced `instrumentationKey()` with `connectionString()` in the `AzureMonitorExporterBuilder`.

## 1.0.0-beta.1 (2020-10-06)

### New Features
- Initial release. Please see the README and wiki for information on the new design.
