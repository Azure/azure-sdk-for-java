# Azure OpenTelemetry Tracing plugin library for Java

This package enables distributed tracing across Azure SDK Java libraries through [OpenTelemetry][OpenTelemetry]. OpenTelemetry is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services.
The Azure core tracing package provides:

- Context propagation, used to correlate activities and requests between services with an initial customer action.
- Tracing user requests to the system, allowing to pinpoint failures and performance issues.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][OpenTelemetry] | [Samples][samples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opentelemetry</artifactId>
  <version>1.0.0-beta.11</version>
</dependency>
```
[//]: # ({x-version-update-end})

## Key concepts

### Trace

A trace is a tree of spans showing the path of work through a system. A trace on its own is distinguishable by a unique 16 byte sequence called a TraceID.

### Span

A span represents a single operation in a trace. A span could be representative of an HTTP request, a remote procedure call (RPC), a database query, or even the path that a code takes.

## Examples

The following sections provides examples of using the azure-core-tracing-opentelemetry plugin with some of the Azure Java SDK libraries:

### Using the plugin package with HTTP client libraries

- Synchronously create a secret using [azure-security-keyvault-secrets][azure-security-keyvault-secrets] with tracing enabled.

    Users can additionally pass the value of the current tracing span to the SDKs using key **PARENT_SPAN_KEY** on the [Context][context] parameter of the calling method.
    The plugin package creates a root span to encapsulate all the child spans created in the calling methods when no parent span is passed in the context.
    This [sample][sample_key_vault] provides an example when no user parent span is passed.

    ```java
    // Get the Tracer Provider
    static TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
    private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();

    public static void main(String[] args) {
       doClientWork();
    }

    public static void doClientWork() {
       SecretClient client = new SecretClientBuilder()
         .endpoint("<your-vault-url>")
         .credential(new DefaultAzureCredentialBuilder().build())
         .buildClient();

       Span span = TRACER.spanBuilder("user-parent-span").startSpan();
       try (Scope scope = TRACER.withSpan(span)) {

           // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
           secretClient.setSecret(new Secret("secret_name", "secret_value));

           // Optionally, to specify the context you can use
           // final Context traceContext = new Context(PARENT_SPAN_KEY, span);
           // secretClient.setSecretWithResponse(new Secret("secret_name", "secret_value", traceContext));
       } finally {
           span.end();
       }
    }
    ```

### Using the plugin package with AMQP client libraries

Send a single event/message using [azure-messaging-eventhubs][azure-messaging-eventhubs] with tracing enabled.

Users can additionally pass the value of the current tracing span to the EventData object with key **PARENT_SPAN_KEY** on the [Context][context] object:

```java
// Get the Tracer Provider
private static TracerSdkProvider tracerProvider = OpenTelemetrySdk.getTracerProvider();
private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();

private static void doClientWork() {
    EventHubProducerClient producer = new EventHubClientBuilder()
        .connectionString(CONNECTION_STRING)
        .buildProducerClient();

    Span span = TRACER.spanBuilder("user-parent-span").startSpan();
    try (Scope scope = TRACER.withSpan(span)) {
        EventData event1 = new EventData("1".getBytes(UTF_8));
        event1.addContext(PARENT_SPAN_KEY, span);

        EventDataBatch eventDataBatch = producer.createBatch();

        if (!eventDataBatch.tryAdd(eventData)) {
            producer.send(eventDataBatch);
            eventDataBatch = producer.createBatch();
        }
    } finally {
        span.end();
    }
}
```

## Troubleshooting

### General

For more information on OpenTelemetry Java support for tracing, see [OpenTelemetry Java][OpenTelemetry-quickstart].

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

### Samples

Several Java SDK samples are available to you in the SDKs GitHub repository.
These following samples provide example code for additional scenarios commonly encountered while working with Tracing:

- [Set Configuration Setting][sample_app_config] - Tracing enabled Sample for setting a configuration setting using [azure-data-app-configuration][azure_data_app_configuration].
- [List Key Vault Secrets][sample_key_vault] - Tracing enabled sample for creating and listing secrets of a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets].
- [Publish Events][sample_eventhubs] - Tracing enabled sample for publishing multiple events using [azure-messaging-eventhubs][azure_messaging_eventhubs_mvn].
- [Async List Key Vault Secrets][sample_async_key_vault] - Tracing enabled sample for asynchronously creating and listing secrets of a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets].

### Additional Documentation

For more extensive documentation on OpenTelemetry, see the [API reference documentation][OpenTelemetry].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit [cla](https://cla.microsoft.com).

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://aka.ms/java-docs
[azure_data_app_configuration]: https://mvnrepository.com/artifact/com.azure/azure-data-appconfiguration/
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[azure_messaging_eventhubs_mvn]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[azure-messaging-eventhubs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/eventhubs/azure-messaging-eventhubs
[azure-security-keyvault-secrets]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-secrets
[context]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core/src/main/java/com/azure/core/util/Context.java
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
[OpenTelemetry-quickstart]: https://github.com/open-telemetry/opentelemetry-java/blob/main/QUICKSTART.md
[OpenTelemetry]: https://github.com/open-telemetry/opentelemetry-java#opentelemetry-for-java
[sample_app_config]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/CreateConfigurationSettingLoggingExporterSample.java
[sample_async_key_vault]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/AsyncListKeyVaultSecretsLoggingExporterSample.java
[sample_eventhubs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/PublishEventsJaegerExporterSample.java
[sample_key_vault]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/ListKeyVaultSecretsJaegerExporterSample.java
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-tracing-opentelemetry%2FREADME.png)
