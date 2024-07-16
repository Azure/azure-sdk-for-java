# Release History

## 1.0.0-beta.48 (2024-07-12)
### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.1` to `1.50.0`.

## 1.0.0-beta.47 (2024-06-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.49.0` to `1.49.1`.
- Upgraded OpenTelemetry from `1.37.0` to `1.38.0`.

## 1.0.0-beta.46 (2024-05-01)

### Breaking Changes

- Added default query params sanitization for HTTP spans.

### Bugs Fixed

- Fixed explicit context propagation when running in javaagent. ([#39602](https://github.com/Azure/azure-sdk-for-java/pull/39602))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.48.0` to `1.49.0`.
- Upgraded OpenTelemetry from `1.36.0` to `1.37.0`.

## 1.0.0-beta.45 (2024-04-05)

### Bugs Fixed

- Fixed unreliable HTTP span reporting when response is not closed.
 
### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.47.0` to `1.48.0`.
- Upgraded OpenTelemetry from `1.35.0` to `1.36.0`.

## 1.0.0-beta.44 (2024-03-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.46.0` to `1.47.0`.
- Upgraded OpenTelemetry from `1.34.1` to `1.35.0`.

## 1.0.0-beta.43 (2024-02-02)

### Features Added

- Updated HTTP and messaging instrumentation to OpenTelemetry Semantic Conventions version 1.23.1.

### Breaking Changes

- Renamed attributes according to OpenTelemetry semantic conventions changes:
    - `net.peer.name` -> `server.address`
    - `http.method` -> `http.request.method`
    - `http.status_code` -> `http.response.status_code`
    - `http.url` -> `url.full`
- Removed `http.user_agent` optional attribute since the same information is reported in the instrumentation scope via library name and version.  
- Removed `OpenTelemetrySchemaVersion` and it's setter method on `OpenTelemetryTracingOptions` since we're not allowing to change the schema version for now.
- Removed exception event reporting - exceptions are reported as logs already.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.45.1` to `1.46.0`.

## 1.0.0-beta.42 (2023-11-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.1` to `1.45.0`.
- Upgraded OpenTelemetry from `1.28.0` to `1.31.0`.

## 1.0.0-beta.41 (2023-10-17)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.44.0` to `1.44.1`.

## 1.0.0-beta.40 (2023-10-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.43.0` to `1.44.0`.

## 1.0.0-beta.39 (2023-09-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.42.0` to `1.43.0`.

## 1.0.0-beta.38 (2023-08-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.41.0` to `1.42.0`.

## 1.0.0-beta.37 (2023-07-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.40.0` to `1.41.0`.

## 1.0.0-beta.36 (2023-06-02)

### Breaking Changes

- Replaced `OpenTelemetryTracingOptions.setProvider` with `OpenTelemetryTracingOptions.setOpenTelemetry` method. Instead of `io.opentelemetry.api.trace.TracerProvider` instance
  it now takes `io.opentelemetry.api.OpenTelemetry` container.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.39.0` to `1.40.0`.

## 1.0.0-beta.35 (2023-05-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.38.0` to `1.39.0`.

## 1.0.0-beta.34 (2023-04-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.37.0` to `1.38.0`.

## 1.0.0-beta.33 (2023-03-02)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.36.0` to `1.37.0`.
- Upgraded OpenTelemetry from `1.20.0` to `1.23.0`.

## 1.0.0-beta.32 (2023-02-01)

### Features Added

- Added support for custom tracer providers.
- Switched to OpenTelemetry messaging semantic conventions and started reporting schema version.

### Breaking Changes

- Renamed attributes to converge with OpenTelemetry semantic conventions:
  - `messaging_bus.destination` -> `messaging.destination.name`
  - `peer.address` -> `net.peer.name`
  - `clientRequestId` -> `az.client_request_id`
  - `service.request.id` -> `az.service_request_id`
- Removed `OpenTelemetryHttpPolicy`.

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.35.0` to `1.36.0`.
- Upgraded OpenTelemetry from `1.14.0` to `1.20.0`.

## 1.0.0-beta.31 (2023-01-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.34.0` to `1.35.0`.

## 1.0.0-beta.30 (2022-11-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.33.0` to `1.34.0`.

## 1.0.0-beta.29 (2022-10-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.32.0` to `1.33.0`.

## 1.0.0-beta.28 (2022-09-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.31.0` to `1.32.0`.

## 1.0.0-beta.27 (2022-08-05)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.30.0` to `1.31.0`.
- Upgraded `opentelemtry-api` from `1.11.0` to `1.14.0`.

## 1.0.0-beta.26 (2022-06-30)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.1` to `1.30.0`.

## 1.0.0-beta.25 (2022-06-03)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.29.0` to `1.29.1`.

## 1.0.0-beta.24 (2022-06-03)

### Breaking Changes
- Started suppressing spans for nested Azure client libraries public API calls with `INTERNAL` or `CLIENT` kind. ([#28998](https://github.com/Azure/azure-sdk-for-java/pull/28998))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.28.0` to `1.29.0`.

## 1.0.0-beta.23 (2022-05-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.27.0` to `1.28.0`.

## 1.0.0-beta.22 (2022-04-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.26.0` to `1.27.0`.

## 1.0.0-beta.21 (2022-03-04)

### Other Changes

- Updated all `ClientLogger`s to be static constants instead of instance variables. ([#27339](https://github.com/Azure/azure-sdk-for-java/pull/27339))

#### Dependency Updates

- Upgraded `azure-core` from `1.25.0` to `1.26.0`.
- Upgraded OpenTelemetry from `1.0.0` to `1.11.0`.

## 1.0.0-beta.20 (2022-02-04)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.1` to `1.25.0`.

## 1.0.0-beta.19 (2022-01-11)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.24.0` to `1.24.1`.

## 1.0.0-beta.18 (2022-01-06)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.23.1` to `1.24.0`.

## 1.0.0-beta.17 (2021-12-07)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.22.0` to `1.23.1`.

## 1.0.0-beta.16 (2021-11-05)

### Features Added

- Provide HTTP URL and method before span is started to allow for sampling decisions to be based on them. ([#24996](https://github.com/Azure/azure-sdk-for-java/pull/24996))

### Bugs Fixed

- Fixed OpenTelemetry context propagation and span duplication. ([#25012](https://github.com/Azure/azure-sdk-for-java/pull/25012))
- Fixed inconsistencies in span creation and the OpenTelemetry specification. ([#24954](https://github.com/Azure/azure-sdk-for-java/pull/24954))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.21.0` to `1.22.0`.

## 1.0.0-beta.15 (2021-10-01)

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.20.0` to `1.21.0`.

## 1.0.0-beta.14 (2021-09-07)

### Fixed

- Fixed a case where AMQP span context should set remote parent. ([#21667](https://github.com/Azure/azure-sdk-for-java/pull/21667))

### Other Changes

#### Dependency Updates

- Upgraded `azure-core` from `1.19.0` to `1.20.0`.

## 1.0.0-beta.13 (2021-08-06)

### Features Added

- Added support to create tracing spans with customizations. ([#23159](https://github.com/Azure/azure-sdk-for-java/pull/23159))

### Dependency Updates

- Upgraded `azure-core` from `1.18.0` to `1.19.0`.

## 1.0.0-beta.12 (2021-07-01)

### Key Bugs Fixed

- Fixed `addEvent` API to use the span provided in the context rather than `Span.current()`.

### Dependency Updates

- Upgraded `azure-core` from `1.17.0` to `1.18.0`.

## 1.0.0-beta.11 (2021-06-07)

### Dependency Updates

- Upgraded `azure-core` from `1.16.0` to `1.17.0`.

## 1.0.0-beta.10 (2021-05-07)

### Dependency Updates

- Upgraded `azure-core` from `1.15.0` to `1.16.0`.

## 1.0.0-beta.9 (2021-04-02)

### Dependency Updates

- Upgraded `azure-core` from `1.14.0` to `1.15.0`.

## 1.0.0-beta.8 (2021-03-08)

### Dependency Updates

- Updated `azure-core` from `1.13.0` to `1.14.0`.
- Updated versions of `opentelemetry-api` to `1.0.0` version.
  More detailed information about the new OpenTelemetry API version can be found in [OpenTelemetry changelog](https://github.com/open-telemetry/opentelemetry-java/blob/main/CHANGELOG.md#version-100---2021-02-26).

## 1.0.0-beta.7 (2021-02-05)

### Dependency Updates
- Updated versions of `opentelemetry-api` to `0.14.1` version.
  More detailed information about the new OpenTelemetry API version can be found in [OpenTelemetry changelog](https://github.com/open-telemetry/opentelemetry-java/blob/main/CHANGELOG.md#version-0141---2021-01-14)

## 1.0.0-beta.6 (2020-08-07)
- Update `opentelemetry-api` dependency version to `0.6.0` and included `io.grpc:grpc-context[1.30.0]` external
 dependency .

## 1.0.0-beta.5 (2020-06-08)

- Changed `Tracer` loading logic to only use first in classpath instead of all in classpath.
- Updated Azure Core dependency.

## 1.0.0-beta.4 (2020-04-02)

- Added az namespace info attribute to all outgoing spans for Http Libraries.
- `io.opentelemetry` version update to `0.2.4` API changes.

## 1.0.0-beta.3 (2020-03-06)

- EventHubs: add enqueueTime to Process span links.
- EventHubs: add az namespace attribute to all outgoing spans. 

## 1.0.0-beta.2 (2020-01-07)

- Add `EventHubs.*` properties to attributes of processing spans.
- Remove `Azure` prefix from convenience layer span names.
- Add links for batch send operation in Event Hubs client library. 

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opentelemetry_1.0.0-beta.2/sdk/core/azure-core-tracing-opentelemetry/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opentelemetry_1.0.0-beta.2/sdk/core/azure-core-tracing-opentelemetry/src/samples).

## 1.0.0-beta.1 (2019-11-26)

Version 1.0.0-beta.1 is the first preview of our efforts to provide low level interfaces and helper methods to support tracing for Java client libraries.
This library includes [OpenTelemetry](https://opentelemetry.io/) implementation of the interface.
This library added tracing instrumentation for AMQP and HTTP Java SDK client libraries across different languages and platforms.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opentelemetry_1.0.0-beta.1/sdk/core/azure-core-tracing-opentelemetry/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opentelemetry_1.0.0-beta.1/sdk/core/azure-core-tracing-opentelemetry/src/samples).
