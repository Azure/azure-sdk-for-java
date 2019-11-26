# Release History
## Version 1.0.0-beta.5 (2019-11-26)
For details on the Azure SDK for Java (December 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview6-java).
- Extended support for 0.24.0 opencensus-api package version.
- Added tracing instrumentation for Batch send operation in Eventhubs.

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opencensus_1.0.0-beta.5/sdk/core/azure-core-tracing-opencensus/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/blob/azure-core-tracing-opencensus_1.0.0-beta.5/sdk/core/azure-core-tracing-opencensus/src/samples).

## Version 1.0.0-preview.4 (2019-10-31)

For details on the Azure SDK for Java (November 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview5-java).
Version 1.0.0-preview.4 added tracing support for AMQP and HTTP client libraries across different languages and platforms as possible.
- Fixed Service loader provider configuration file to correctly trace HTTP requests.
- Moved package under azure-core directory.
- Removed client libraries dependencies from the package.
- Moved ProcessKind enum to public tracing folder under azure-core.
- Updated samples to be markdown files.
See this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/core/azure-core-tracing-opencensus/README.md) and
  [samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-tracing-opencensus/src/samples) demonstrate the new API.

## Version 1.0.0-preview.3 (2019-10-07)
For details on the Azure SDK for Java (October 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview4-java).

Version 1.0.0-preview.3 added tracing support for AMQP client libraries across different languages and platforms as possible.
See this package's
  [documentation](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/tracing/azure-core-tracing-opencensus/README.md) and
  [samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/tracing/azure-core-tracing-opencensus/src/samples/java/com/azure/core/tracing/opencensus) demonstrate the new API.

## Version 1.0.0-preview.2 (2019-09-09)
For details on the Azure SDK for Java (August 2019 Preview) release refer to the [release announcement](https://aka.ms/azure-sdk-preview3-java).

Version 1.0.0-preview.2 added tracing support to all of the HTTP based Azure SDK Java libraries.

## Version 1.0.0-preview.1 (2019-08-06)
Version 1.0.0-preview.1 is the first preview of our efforts to provide low level interfaces and helper methods to support tracing for Java client libraries. This library includes [OpenTelemetry](https://opentelemetry.io/) implementation of the interface,
    For more information about this, and preview releases of other Azure SDK libraries, please visit
https://aka.ms/azure-sdk-preview2-java.
