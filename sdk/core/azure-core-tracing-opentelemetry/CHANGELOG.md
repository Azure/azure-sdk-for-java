# Release History

## 1.0.0-beta.4 (Unreleased)
- Keyvault: add az namespace info attribute to all outgoing spans.


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
