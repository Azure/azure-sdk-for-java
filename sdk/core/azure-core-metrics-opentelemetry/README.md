# Azure OpenTelemetry Metrics plugin library for Java

This package enables  metrics from Azure SDK Java libraries through [OpenTelemetry][OpenTelemetry]. OpenTelemetry is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][OpenTelemetry] | [Samples][samples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-core-metrics-opentelemetry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-metrics-opentelemetry</artifactId>
  <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

Check out [Metrics in OpenTelemetry](https://opentelemetry.io/docs/concepts/signals/metrics/) for all the details on metrics. 

## Examples

The following sections provide several code snippets covering some of the most common client configuration scenarios.

- [Default configuration: agent](#default-configuration-agent)
- [Default configuration: Opentelemtery SDK](#default-configuration-agent)
- [Custom configuration](#custom-configuration)

You can find more samples [here](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-metrics-opentelemetry/src/samples/).

### Default configuration: agent

If you use OpenTelemetry Java agent or Application Insights Java agent version 3.3.0-BETA or higher, no additional Azure SDK configuration is needed.

### Default configuration: OpenTelemetry SDK

Azure SDK uses global OpenTelemetry instance by default. You can use [OpenTelemetry SDK Autoconfigure](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
package to configure OpenTelemetry using environment variables (or system properties).

```xml
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-sdk-extension-autoconfigure</artifactId>
</dependency>
```


```java readme-sample-defaultConfiguration

// configure OpenTelemetry SDK using OpenTelemetry SDK Autoconfigure
AutoConfiguredOpenTelemetrySdk.initialize();

// configure Azure Client, no metric configuration needed
// client will use global OTel configured by OpenTelemetry autoconfigure package.
AzureClient sampleClient = new AzureClientBuilder()
    .endpoint("https://my-client.azure.com")
    .build();

// use client as usual, if it emits metric, they will be exported
sampleClient.methodCall("get items", Context.NONE);

```

### Custom configuration

If you want to pass `MeterProvider` explicitly, you can do it using `MetricsOptions` and passing them to Azure Clients. `MetricsOptions` can also be used to disable metrics from specific client. 

```java readme-sample-customConfiguration

// configure OpenTelemetry SDK explicitly per https://opentelemetry.io/docs/instrumentation/java/manual/
SdkMeterProvider meterProvider = SdkMeterProvider.builder()
    .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
    .build();

// Pass OTel meterProvider to MetricsOptions.
MetricsOptions customMetricsOptions = new MetricsOptions()
    .setProvider(meterProvider);

// configure Azure Client to use customMetricsOptions - it will use meterProvider
// to create meters and instruments
AzureClient sampleClient = new AzureClientBuilder()
    .endpoint("https://my-client.azure.com")
    .clientOptions(new ClientOptions().setMetricsOptions(customMetricsOptions))
    .build();

// use client as usual, if it emits metric, they will be exported
sampleClient.methodCall("get items");

```

## Troubleshooting

### General

For more information on OpenTelemetry, see [OpenTelemetry documentation](https://opentelemetry.io/docs/instrumentation/java/getting-started/) and [OpenTelemetry Java](https://github.com/open-telemetry/opentelemetry-java).

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

Get started with Azure libraries that are [built using Azure Core](https://azure.github.io/azure-sdk/releases/latest/#java).

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit [cla](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://aka.ms/java-docs
[context]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/Context.java
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-metrics-opentelemetry/src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-metrics-opentelemetry%2FREADME.png)
