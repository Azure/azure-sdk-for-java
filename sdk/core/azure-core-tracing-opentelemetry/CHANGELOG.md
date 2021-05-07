# Release History

## 1.0.0-beta.11 (Unreleased)


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

For details on the Azure SDK for Java (Decemeber 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview5-java).
Version 1.0.0-beta.1 is the first preview of our efforts to provide low level interfaces and helper methods to support tracing for Java client libraries.
This library includes [OpenTelemetry](https://opentelemetry.io/) implementation of the interface.
This library added tracing instrumentation for AMQP and HTTP Java SDK client libraries across different languages and platforms.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opentelemetry_1.0.0-beta.1/sdk/core/azure-core-tracing-opentelemetry/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opentelemetry_1.0.0-beta.1/sdk/core/azure-core-tracing-opentelemetry/src/samples).
