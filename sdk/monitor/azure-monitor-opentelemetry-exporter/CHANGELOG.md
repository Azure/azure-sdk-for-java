# Release History

## 1.0.0-beta.8 (2023-02-24)

### Dependency Update
- Update OpenTelemetry Java Instrumentation to 1.23.0

### Bugs Fixed
- [Fix GlobalOpenTelemetry usage](https://github.com/Azure/azure-sdk-for-java/pull/33678)

## 1.0.0-beta.7 (2023-02-09)

### Enhancements
- Updated the OpenTelemetry SDK baseline to version 1.20.0
- Replace System.getenv usage with Azure SDK Configuration

### Breaking Changes
- Change the Exporter implementations from public to package-private

## 1.0.0-beta.6 (2022-09-06)

### New Features
- Add metrics exporter.
- Add logs exporter.
- Add disk persistence and retries for ingestion failures.
- Add heartbeat telemetry.

## 1.0.0-beta.5 (2021-11-12)

### New Features
- Added Azure Active Directory authentication support.
- Added Stamp Specific Endpoint redirect support.
- Added support for EventHubs.
- Add sdk version string to telemetry item.

### Dependency Updates
- Updated versions of `opentelemetry-api` and `opentelemetry-sdk` to `1.7.0` version.

### Bugs Fixed
- Fix formatted duration used in setDuration of RequestData and RemoteDependencyData.
- Minor Fix to ndJsonSerialization issue.


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
