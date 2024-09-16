# Azure Monitor OpenTelemetry Exporter client library for Java

This client library provides support for exporting OpenTelemetry data to Azure Monitor. This package assumes your
 application is already instrumented with the [OpenTelemetry SDK][opentelemetry_sdk] following the [OpenTelemetry
 Specification][opentelemetry_specification].
  
[Source code][source_code] | [Package (Maven)][package_mvn] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- [Azure Subscription][azure_subscription]
- [Application Insights resource][application_insights_resource]

For more information, please read [introduction to Application Insights][application_insights_intro].

### Include the dependency

Add the [Azure Monitor OpenTelemetry Exporter](https://central.sonatype.com/artifact/com.azure/azure-monitor-opentelemetry-exporter) dependency.

### Authentication

#### Get the connection string from the portal

In order to export telemetry data to Azure Monitor, you will need the instrumentation key to your [Application
 Insights resource][application_insights_resource]. To get your instrumentation key, go to [Azure Portal][azure_portal], 
search for your resource. On the overview page of your resource, you will find the instrumentation key in the top
right corner.


### Setup the OpenTelemetry SDK to work with Azure Monitor exporter

If you have set the Application Insights connection string with the `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable, you configure OpenTelemetry SDK auto-configuration for Azure in the following way:

```java readme-sample-autoconfigure-env-variable
AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
AzureMonitor.configure(sdkBuilder);
OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();
```

You can also se the connection string in the code:
```java readme-sample-autoconfigure
AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
AzureMonitor.configure(sdkBuilder, "{connection-string}");
OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();
```

## Examples

The following sections provide code samples using the OpenTelemetry Azure Monitor Exporter client library and OpenTelemetry API.


The following example shows how create a span:

```java readme-sample-create-span
AutoConfiguredOpenTelemetrySdkBuilder otelSdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

AzureMonitor.configure(otelSdkBuilder, "{connection-string}");

OpenTelemetry openTelemetry = otelSdkBuilder.build().getOpenTelemetrySdk();
Tracer tracer = openTelemetry.getTracer("Sample");

Span span = tracer.spanBuilder("spanName").startSpan();

// Make the span the current span
try (Scope scope = span.makeCurrent()) {
    // Your application logic here
} catch (Throwable t) {
    span.recordException(t);
    throw t;
} finally {
    span.end();
}
```
The following example demonstrates how to add a span processor to the OpenTelemetry SDK autoconfiguration.

```java readme-sample-span-processor
AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

AzureMonitor.configure(sdkBuilder);

SpanProcessor spanProcessor = new SpanProcessor() {
    @Override
    public void onStart(Context context, ReadWriteSpan span) {
        span.setAttribute(AttributeKey.stringKey("attributeKey"), "attributeValue");
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
};

sdkBuilder.addTracerProviderCustomizer(
    (sdkTracerProviderBuilder, configProperties) -> sdkTracerProviderBuilder
        .addSpanProcessor(spanProcessor));
```
More advanced examples with OpenTelemetry APIs:
* [Advanced examples - 1][advanced_examples_1]
* [Advanced examples - 2][advanced_examples_2]
* [Event Hubs example][event_hubs_example]

## Key concepts

Some key concepts for the Azure Monitor exporter include:

* [OpenTelemetry][opentelemetry_spec]: OpenTelemetry is a set of libraries used to collect and export telemetry data
 (metrics, logs, and traces) for analysis in order to understand your software's performance and behavior.

* [Instrumentation][instrumentation_library]: The ability to call the OpenTelemetry API directly by any application is
 facilitated by instrumentation. A library that enables OpenTelemetry observability for another library is called an Instrumentation Library.

* [Trace][trace_concept]: Trace refers to distributed tracing. It can be thought of as a directed acyclic graph (DAG) of Spans, where the edges between Spans are defined as parent/child relationship.

* [Tracer Provider][tracer_provider]: Provides a `Tracer` for use by the given instrumentation library.

* [Span Processor][span_processor]: A span processor allows hooks for SDK's `Span` start and end method invocations. Follow the link for more information.

* [Sampling][sampler_ref]: Sampling is a mechanism to control the noise and overhead introduced by OpenTelemetry by reducing the number of samples of traces collected and sent to the backend.

For more information on the OpenTelemetry project, please review the [OpenTelemetry Specifications][opentelemetry_specification].


## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps
Learn more about [Open Telemetry][opentelemetry_io]

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a
[Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights
to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the
[Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[jdk]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-opentelemetry-exporter/src
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://docs.microsoft.com/azure/azure-monitor/overview
[package_mvn]: https://central.sonatype.com/artifact/com.azure/azure-monitor-opentelemetry-exporter
[product_documentation]: https://docs.microsoft.com/azure/azure-monitor/overview
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/blob/master/QUICKSTART.md
[opentelemetry_specification]: https://github.com/open-telemetry/opentelemetry-specification
[application_insights_resource]: https://docs.microsoft.com/azure/azure-monitor/app/create-new-resource
[application_insights_intro]: https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview
[azure_portal]: https://ms.portal.azure.com/#blade/HubsExtension/BrowseResource/resourceType/microsoft.insights%2Fcomponents
[opentelemetry_io]: https://opentelemetry.io/ 
[span_data]: https://opentelemetry.lightstep.com/spans
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-opentelemetry-exporter/src/samples
[opentelemetry_spec]: https://opentelemetry.io/
[instrumentation_library]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/overview.md#instrumentation-libraries
[tracer_provider]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#tracer-provider
[span_processor]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#span-processor
[sampler_ref]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#sampling
[trace_concept]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/overview.md#trace
[advanced_examples_1]: https://github.com/Azure-Samples/ApplicationInsights-Java-Samples/tree/main/opentelemetry-api/exporter/
[advanced_examples_2]: https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/sdk-usage/src/main/java/io/opentelemetry/sdk/example
[event_hubs_example]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-opentelemetry-exporter/src/samples/java/com/azure/monitor/opentelemetry/exporter/EventHubsAzureMonitorExporterSample.java
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%monitor%2Fazure-monitor-opentelemetry-exporter%2FREADME.png)
