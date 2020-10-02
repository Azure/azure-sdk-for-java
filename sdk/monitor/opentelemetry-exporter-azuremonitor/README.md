# OpenTelemetry Azure Monitor Exporter client library for Java

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

[//]: # ({x-version-update-start;com.azure:opentelemetry-exporter-azuremonitor;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>opentelemetry-exporter-azuremonitor</artifactId>
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

### Creating an Async client
<!-- embedme ./src/samples/java/com/azure/opentelemetry/exporter/azuremonitor/ReadmeSamples.java#L32-L33 -->
```java
MonitorExporterAsyncClient monitorExporterAsyncClient = new MonitorExporterClientBuilder()
    .buildAsyncClient();
```

#### Creating a Sync client
<!-- embedme ./src/samples/java/com/azure/opentelemetry/exporter/azuremonitor/ReadmeSamples.java#L40-L40 -->
```java
MonitorExporterClient monitorExporterClient = new MonitorExporterClientBuilder().buildClient();
```

#### Creating a telemetry data instance

The following example shows how to create a telemetry item for exporting request data. Similarly, other types of data
 like exception data, event data, metrics data etc. can be created.

<!-- embedme ./src/samples/java/com/azure/opentelemetry/exporter/azuremonitor/ReadmeSamples.java#L43-L80 -->
```java
/**
 * Create a telemetry item of type {@link RequestData}.
 *
 * @param responseCode The response code.
 * @param requestName The name of the request.
 * @param success The completion status of the request.
 * @param duration The duration for completing the request.
 * @return The telemetry event representing the provided request data.
 */
private TelemetryItem createRequestData(String responseCode, String requestName, boolean success,
                                               Duration duration) {
    MonitorDomain requestData = new RequestData()
        .setId(UUID.randomUUID().toString())
        .setDuration(getFormattedDuration(duration))
        .setResponseCode(responseCode)
        .setSuccess(success)
        .setUrl("http://localhost:8080/")
        .setName(requestName)
        .setVersion(2);

    MonitorBase monitorBase = new MonitorBase()
        .setBaseType("RequestData")
        .setBaseData(requestData);

    TelemetryItem telemetryItem = new TelemetryItem()
        .setVersion(1)
        .setInstrumentationKey("{instrumentation-key}")
        .setName("test-event-name")
        .setSampleRate(100.0f)
        .setTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
        .setData(monitorBase);
    return telemetryItem;
}

private static String getFormattedDuration(Duration duration) {
    return duration.toDays() + "." + duration.toHours() + ":" + duration.toMinutes() + ":" + duration.getSeconds()
        + "." + duration.toMillis();
}
```
#### Exporting telemetry data
<!-- embedme ./src/samples/java/com/azure/opentelemetry/exporter/azuremonitor/ReadmeSamples.java#L83-L97 -->
```java
MonitorExporterClient monitorExporterClient = new MonitorExporterClientBuilder().buildClient();

List<TelemetryItem> telemetryItems = new ArrayList<>();
telemetryItems.add(createRequestData("200", "GET /service/resource-name", true, Duration.ofMillis(100)));
telemetryItems.add(createRequestData("400", "GET /service/resource-name", false, Duration.ofMillis(50)));
telemetryItems.add(createRequestData("202", "GET /service/resource-name", true, Duration.ofMillis(125)));

ExportResult result = monitorExporterClient.export(telemetryItems);
System.out.println("Items received " + result.getItemsReceived());
System.out.println("Items accepted " + result.getItemsAccepted());
System.out.println("Errors " + result.getErrors().size());
result.getErrors()
    .forEach(
        error -> System.out.println(error.getStatusCode() + " " + error.getMessage()
            + " " + error.getIndex()));
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
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/monitor/opentelemetry-exporter-azuremonitor/src/samples/java/
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/monitor/opentelemetry-exporter-azuremonitor/src
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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%monitor%2Fopentelemetry-exporter-azuremonitor%2FREADME.png)
