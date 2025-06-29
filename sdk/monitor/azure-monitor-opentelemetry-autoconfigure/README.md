# Azure Monitor OpenTelemetry SDK Autoconfigure Distro for Application Insights in Java applications client library for Java

This client library customizes the [OpenTelemetry SDK autoconfiguration][opentelemetry_autoconfiguration] for Azure Monitor.

[Source code][source_code] | [Package (Maven)][package_mvn] | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- [Azure Subscription][azure_subscription]
- [Application Insights resource][application_insights_resource]

For more information, please read [introduction to Application Insights][application_insights_intro].

### Include the dependency

Add the [Azure Monitor OpenTelemetry SDK Autoconfigure Distro](https://central.sonatype.com/artifact/com.azure/azure-monitor-opentelemetry-autoconfigure) dependency.

### Authentication

#### Get the connection string from the portal

In order to export telemetry data to Azure Monitor, you will need the instrumentation key to your [Application
Insights resource][application_insights_resource]. To get your instrumentation key, go to [Azure Portal][azure_portal],
search for your resource. On the overview page of your resource, you will find the instrumentation key in the top
right corner.


### Setup the Azure Monitor OpenTelemetry SDK Autoconfigure Distro

If you have set the Application Insights connection string with the `APPLICATIONINSIGHTS_CONNECTION_STRING` environment variable, you can configure OpenTelemetry SDK auto-configuration for Azure in the following way:

```java readme-sample-autoconfigure-env-variable
AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
AzureMonitorAutoConfigure.customize(sdkBuilder);
OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();
```

You can also set the connection string in the code:
```java readme-sample-autoconfigure
AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
AzureMonitorAutoConfigure.customize(sdkBuilder, "{connection-string}");
OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();
```

## Examples

The following sections provide code samples using the Azure Monitor OpenTelemetry SDK Autoconfigure Distro and OpenTelemetry SDK.

The following example shows how create a span:

```java readme-sample-create-span
AutoConfiguredOpenTelemetrySdkBuilder otelSdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

AzureMonitorAutoConfigure.customize(otelSdkBuilder, "{connection-string}");

OpenTelemetry openTelemetry = otelSdkBuilder.build().getOpenTelemetrySdk();
Tracer tracer = openTelemetry.getTracer("Sample");

Span span = tracer.spanBuilder("spanName").startSpan();

// Make the span the current span
try (Scope scope = span.makeCurrent()) {
    // Your application logic here
    applicationLogic();
} catch (Throwable t) {
    span.recordException(t);
    throw t;
} finally {
    span.end();
}
```
The following example demonstrates how to add a span processor to the OpenTelemetry SDK autoconfiguration.

```java readme-sample-span-processor
private static final AttributeKey<String> ATTRIBUTE_KEY = AttributeKey.stringKey("attributeKey");

public void spanProcessor() {
    AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

    AzureMonitorAutoConfigure.customize(sdkBuilder);

    SpanProcessor spanProcessor = new SpanProcessor() {

        @Override
        public void onStart(Context context, ReadWriteSpan span) {
            span.setAttribute(ATTRIBUTE_KEY, "attributeValue");
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
}
```
More advanced examples with OpenTelemetry APIs:
* [Advanced examples - 1][advanced_examples_1]
* [Advanced examples - 2][advanced_examples_2]
* [Event Hubs example][event_hubs_example]

## Key concepts

Some key concepts for the Azure Monitor OpenTelemetry SDK Autoconfigure Distro include:

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

You can leverage [Azure SDK logging][logging].

Examples:
* [Log4j][log4j]
* [Logback][logback]

Learn more about [OpenTelemetry SDK logging][logging_otel_sdk].

### Disable live metrics

You can disable the [live metrics][live_metrics] by setting the `APPLICATIONINSIGHTS_LIVE_METRICS_ENABLED` environment variable to false, the `applicationinsights.live.metrics.enabled` Java system property to false,
or programmatically with a properties supplier: `sdkBuilder.addPropertiesSupplier(() -> Collections.singletonMap("applicationinsights.live.metrics.enabled", "false"))`.

## Next steps
Learn more about [OpenTelemetry][opentelemetry_io]

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
[jdk]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-opentelemetry-autoconfigure/src
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://learn.microsoft.com/azure/azure-monitor/overview
[package_mvn]: https://central.sonatype.com/artifact/com.azure/azure-monitor-opentelemetry-autoconfigure
[product_documentation]: https://learn.microsoft.com/azure/azure-monitor/overview
[azure_cli]: https://learn.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[custom_subdomain]: https://learn.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[log4j]: https://github.com/Azure-Samples/ApplicationInsights-Java-Samples/blob/9a7344eeb44525dfc83df3a1bd59460b8a7d93c6/opentelemetry-api/exporter/TrackTrace/Log4j2/src/main/resources/log4j2.xml#L16
[logback]: https://github.com/Azure-Samples/ApplicationInsights-Java-Samples/blob/9a7344eeb44525dfc83df3a1bd59460b8a7d93c6/opentelemetry-api/exporter/TrackTrace/Logback/src/main/resources/logback.xml#L22
[logging_otel_sdk]: https://opentelemetry.io/docs/languages/java/sdk/#internal-logging
[live_metrics]: https://learn.microsoft.com/azure/azure-monitor/app/live-stream
[opentelemetry_autoconfiguration]: https://opentelemetry.io/docs/languages/java/configuration/#zero-code-sdk-autoconfigure
[application_insights_resource]: https://learn.microsoft.com/azure/azure-monitor/app/create-new-resource
[application_insights_intro]: https://learn.microsoft.com/azure/azure-monitor/app/app-insights-overview
[azure_portal]: https://ms.portal.azure.com/#blade/HubsExtension/BrowseResource/resourceType/microsoft.insights%2Fcomponents
[opentelemetry_io]: https://opentelemetry.io/
[span_data]: https://opentelemetry.lightstep.com/spans
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/monitor/azure-monitor-opentelemetry-autoconfigure/src/samples
[opentelemetry_spec]: https://opentelemetry.io/
[instrumentation_library]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/overview.md#instrumentation-libraries
[tracer_provider]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#tracer-provider
[span_processor]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#span-processor
[sampler_ref]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/trace/sdk.md#sampling
[trace_concept]: https://github.com/open-telemetry/opentelemetry-specification/blob/master/specification/overview.md#trace
[advanced_examples_1]: https://github.com/Azure-Samples/ApplicationInsights-Java-Samples/tree/main/opentelemetry-api/exporter/
[advanced_examples_2]: https://github.com/open-telemetry/opentelemetry-java-examples/tree/main/sdk-usage/src/main/java/io/opentelemetry/sdk/example
[event_hubs_example]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/monitor/azure-monitor-opentelemetry-autoconfigure/src/samples/java/com/azure/monitor/opentelemetry/autoconfigure/EventHubsAzureMonitorExporterSample.java
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

