# Release History

## 1.0.0-beta.29 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.28 (2024-08-08)

### Other Changes
- [Migrate LiveMetric and VM metadata data models to azure-json](https://github.com/Azure/azure-sdk-for-java/pull/41458)
- [Enable spotless code formatting](https://github.com/Azure/azure-sdk-for-java/pull/41462)

## 1.0.0-beta.27 (2024-08-05)

### Other Changes
- [Migrate from Jackson to Azure Json](https://github.com/Azure/azure-sdk-for-java/pull/41106)

## 1.0.0-beta.26 (2024-07-24)

### Bugs Fixed
- [Fix pre-aggregated standard HTTP server metric success calculation](https://github.com/Azure/azure-sdk-for-java/pull/40599)
- [OOM error caused by AzureMonitorLogRecordExporter](https://github.com/Azure/azure-sdk-for-java/issues/40546)
  
### Other Changes
- [Update OpenTelemetry to 1.40.0](https://github.com/Azure/azure-sdk-for-java/pull/39843)
- [Improve connection string message](https://github.com/Azure/azure-sdk-for-java/pull/40922)

## 1.0.0-beta.25 (2024-06-28)

### Other changes
- [ResourceAttributes has been deprecated upstream](https://github.com/Azure/azure-sdk-for-java/pull/40504)
- [Handle 206](https://github.com/Azure/azure-sdk-for-java/pull/40416)

## 1.0.0-beta.24 (2024-05-29)

### Features Added
- [Support explicit fixed sampling rate of 100% without ingestion sampling](https://github.com/Azure/azure-sdk-for-java/pull/40338)

## 1.0.0-beta.23 (2024-05-23)

### Other Changes
- [Only emit `_APPRESOURCEPREVIEW_` custom metric in AKS preview integration](https://github.com/Azure/azure-sdk-for-java/pull/40312)
- [Update OpenTelemetry to 2.4.0](https://github.com/Azure/azure-sdk-for-java/pull/40289)

## 1.0.0-beta.22 (2024-05-09)

### Bugs Fixed
- [Fix _OTELRESOURCE_ custom metrics with default resources](https://github.com/Azure/azure-sdk-for-java/pull/39380)

### Other Changes
- [Update OpenTelemetry to 2.3.0](https://github.com/Azure/azure-sdk-for-java/pull/39843)
- [Add attach type to sdkVersion](https://github.com/Azure/azure-sdk-for-java/pull/39883)
- [Emit stable HTTP OTel metrics](https://github.com/Azure/azure-sdk-for-java/pull/39960)

## 1.0.0-beta.21 (2024-03-11)

### Bugs Fixed
- [Support stable HTTP semconv url.query](https://github.com/Azure/azure-sdk-for-java/pull/39133)

## 1.0.0-beta.20 (2024-03-07)

### Features Added
- [Support ingestion sampling](https://github.com/Azure/azure-sdk-for-java/pull/39103)

### Bugs Fixed
- [Avoid warning on sporadic connection failures](https://github.com/Azure/azure-sdk-for-java/pull/39021)

## 1.0.0-beta.19 (2024-02-23)

### Bugs Fixed
- [Always send item count for logs](https://github.com/Azure/azure-sdk-for-java/pull/38930)

## 1.0.0-beta.18 (2024-02-23)

### Bugs Fixed
- [Fix min and max values for pre-aggregated metrics](https://github.com/Azure/azure-sdk-for-java/pull/38516)
- [Fix duplicate exceptions](https://github.com/Azure/azure-sdk-for-java/pull/38687)
- [Always send item count](https://github.com/Azure/azure-sdk-for-java/pull/38737)

### Other Changes
- [Update OpenTelemetry to 2.1.0](https://github.com/Azure/azure-sdk-for-java/pull/38808)

## 1.0.0-beta.17 (2024-01-25)
- [Fix pre-aggregated metrics with stable http semantic convention](https://github.com/Azure/azure-sdk-for-java/pull/38497)

## 1.0.0-beta.16 (2024-01-23)

### Features Added

- [Update OpenTelemetry to 2.0.0](https://github.com/Azure/azure-sdk-for-java/pull/38360)
- [Support for stable HTTP semconv](https://github.com/Azure/azure-sdk-for-java/pull/37899)
- [Update otel schema to 1.22.0](https://github.com/Azure/azure-sdk-for-java/pull/38246)

## 1.0.0-beta.15 (2023-12-18)

### Bugs Fixed
- [Fix flush on JVM shutdown](https://github.com/Azure/azure-sdk-for-java/pull/37618)
- [Fix metric names that violate OpenTelemetry spec](https://github.com/Azure/azure-sdk-for-java/pull/37947)

### Other Changes
- [Update OpenTelemetry to 1.32.0](https://github.com/Azure/azure-sdk-for-java/pull/37819)

## 1.0.0-beta.14 (2023-11-09)

### Breaking Changes
- [Rename build to install](https://github.com/Azure/azure-sdk-for-java/pull/37602)

### Bugs Fixed
- [Fix null HttpPipeline](https://github.com/Azure/azure-sdk-for-java/pull/37574)

## 1.0.0-beta.13 (2023-10-24)

### Bugs Fixed
- [Fix null ikey for _otelresource_ custom metrics](https://github.com/Azure/azure-sdk-for-java/pull/37352)
- [Fix no network statsbeat](https://github.com/Azure/azure-sdk-for-java/pull/37360)

## 1.0.0-beta.12 (2023-10-23)

### Features Added
- [Enable Statsbeat](https://github.com/Azure/azure-sdk-for-java/pull/37032)

### Other Changes
- [Update OpenTelemetry to 1.31.0](https://github.com/Azure/azure-sdk-for-java/pull/37209)
- [Use OpenTelemetry AutoConfigure Module](https://github.com/Azure/azure-sdk-for-java/pull/36230)
- [Don't drop known attributes on custom metrics](https://github.com/Azure/azure-sdk-for-java/pull/37175)
- [Remove RetryPolicy from metadata service call](https://github.com/Azure/azure-sdk-for-java/pull/37031)

## 1.0.0-beta.11 (2023-07-27)

### Other Changes
- [Fix RP Attach Type](https://github.com/Azure/azure-sdk-for-java/pull/36121)

## 1.0.0-beta.10 (2023-07-26)

### Other Changes
- [Update vm prefix](https://github.com/Azure/azure-sdk-for-java/pull/36059)
- [Better log messages](https://github.com/Azure/azure-sdk-for-java/pull/36064)

## 1.0.0-beta.9 (2023-07-14)

### Dependency Update
- Update OpenTelemetry Java Instrumentation to 1.28.0

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
