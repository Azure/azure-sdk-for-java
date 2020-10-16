# Azure Monitor OpenTelemetry Exporter client library for Java

This client library provides support for exporting OpenTelemetry data to Azure Monitor. This package assumes your
 application is already instrumented with the [OpenTelemetry SDK][opentelemetry_sdk] following the [OpenTelemetry
 Specification][opentelemetry_specification].
  
[Source code][source_code] | Package (Maven) | [API reference documentation][api_reference_doc] | [Product Documentation][product_documentation] | [Samples][sample_readme]

## Getting started

### Prerequisites

- [Java Development Kit (JDK) with version 8 or above][jdk]
- [Azure Subscription][azure_subscription]
- [Application Insights resource][application_insights_resource]

For more information, please read [introduction to Application Insights][application_insights_intro].

### Include the Package

[//]: # ({x-version-update-start;com.azure:opentelemetry-exporters-azuremonitor;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>opentelemetry-exporters-azuremonitor</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

#### Get the instrumentation key from the portal

In order to export telemetry data to Azure Monitor, you will need the instrumentation key to your [Application
 Insights resource][application_insights_resource]. To get your instrumentation key, go to [Azure Portal][azure_portal], 
search for your resource. On the overview page of your resource, you will find the instrumentation key on the top
right corner.

### Creating exporter for Azure Monitor
<!-- embedme ./src/samples/java/com/azure/opentelemetry/exporters/azuremonitor/ReadmeSamples.java#L26-L28 -->
```java
AzureMonitorExporter azureMonitorExporter = new AzureMonitorExporterBuilder()
    .instrumentationKey("{instrumentation-key}")
    .buildExporter();
```

#### Exporting span data

The following example shows how to export a collection of available [Spans][span_data] to Azure Monitor through the
 `AzureMonitorExporter`

<!-- embedme ./src/samples/java/com/azure/opentelemetry/exporters/azuremonitor/ReadmeSamples.java#L35-L40 -->
```java
AzureMonitorExporter azureMonitorExporter = new AzureMonitorExporterBuilder()
    .instrumentationKey("{instrumentation-key}")
    .buildExporter();

CompletableResultCode resultCode = azureMonitorExporter.export(getSpanDataCollection());
System.out.println(resultCode.isSuccess());
```

## Key concepts
For more information on the OpenTelemetry project, please review the [OpenTelemetry Specifications
][opentelemetry_specification].

## Examples

More examples can be found in [samples][samples_code].

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
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/monitor/opentelemetry-exporters-azuremonitor/src/samples/java/
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/monitor/opentelemetry-exporters-azuremonitor/src
[azure_subscription]: https://azure.microsoft.com/free/
[api_reference_doc]: https://docs.microsoft.com/azure/azure-monitor/overview
[product_documentation]: https://docs.microsoft.com/azure/azure-monitor/overview
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_portal]: https://portal.azure.com
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md#defaultazurecredential
[custom_subdomain]: https://docs.microsoft.com/azure/cognitive-services/authentication#create-a-resource-with-a-custom-subdomain
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/blob/master/QUICKSTART.md
[opentelemetry_specification]: https://github.com/open-telemetry/opentelemetry-specification
[application_insights_resource]: https://docs.microsoft.com/azure/azure-monitor/app/create-new-resource
[application_insights_intro]: https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview
[azure_portal]: https://ms.portal.azure.com/#blade/HubsExtension/BrowseResource/resourceType/microsoft.insights%2Fcomponents
[opentelemetry_io]: https://opentelemetry.io/ 
[span_data]: https://opentelemetry.lightstep.com/spans
[sample_readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/monitor/opentelemetry-exporters-azuremonitor/src/samples/README.md

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%monitor%2Fopentelemetry-exporters-azuremonitor%2FREADME.png)
