# Azure OpenTelemetry Tracing plugin library for Java

This package enables distributed tracing across Azure SDK Java libraries through [OpenTelemetry][OpenTelemetry]. OpenTelemetry is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services.
The Azure core tracing package provides:

- Context propagation, used to correlate activities and requests between services with an initial customer action.
- Tracing user requests to the system, allowing to pinpoint failures and performance issues.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][OpenTelemetry] | [Samples][samples]

## Getting started

You can enable tracing in Azure client libraries by using and configuring the OpenTelemetry SDK or using an OpenTelemetry-compatible agent.

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.

## Key concepts

### Trace

A trace is a tree of spans showing the path of work through a system. A trace on its own is distinguishable by a unique 16 byte sequence called a TraceID.

### Span

A span represents a single operation in a trace. A span could be representative of an HTTP request, a remote procedure call (RPC), a database query, or even the path that a code takes.
Azure SDK produces span for public client calls such as `SecretClient.getSecret` and HTTP spans for each underlying call to Azure service.

## Azure SDK tracing with Azure Monitor Java agent

By using an Azure Monitor Java in-process agent, you can enable monitoring of your applications without any code changes. For more information, see [Azure Monitor OpenTelemetry-based auto-instrumentation for Java applications](https://docs.microsoft.com/azure/azure-monitor/app/java-in-process-agent). Azure SDK support is enabled by default starting with agent version 3.2.

## Tracing Azure SDK calls with OpenTelemetry agent

If you use [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation/), Azure SDK instrumentation is enabled out-of-the-box starting from version 1.12.0.

For more details on how to configure exporters, add manual instrumentation, or enrich telemetry, see [OpenTelemetry Instrumentation for Java](https://github.com/open-telemetry/opentelemetry-java-instrumentation).

Note: OpenTelemetry agent artifact is stable, but does not provide over-the-wire telemetry stability guarantees - attribute or span names produced by Azure SDK might change over time when you update the agent. Check out [agent stability and versioning](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/VERSIONING.md#compatibility-requirements) for more details.

## Manually instrument the application with OpenTelemetry SDK

If you use OpenTelemetry SDK directly, make sure to configure SDK and exporter for the backend of your choice. For more information, see [OpenTelemetry documentation](https://opentelemetry.io/docs/instrumentation/java/manual_instrumentation/).

To enable Azure SDK tracing, add the latest `com.azure:azure-core-tracing-opentelemetry` packages to your application. For example, in Maven, add the following entry to your *pom.xml* file:

[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opentelemetry</artifactId>
  <version>1.0.0-beta.25</version>
</dependency>
```
[//]: # ({x-version-update-end})

You don't need this package if you use ApplicationInsights Java agent or OpenTelemetry agent.

### Examples

The following sections provides examples of using the `azure-core-tracing-opentelemetry` plugin with a few Azure Java SDK libraries:

### Using the plugin package with HTTP client libraries

Synchronously create a secret using [azure-security-keyvault-secrets][azure-security-keyvault-secrets] with tracing enabled.

The plugin package creates a logical span representing public API call to encapsulate all the underlying HTTP calls. By default OpenTelemetry
`Context.current()` will be used as a parent context - check out [OpenTelemetry documentation](https://opentelemetry.io/docs/java/manual_instrumentation/#tracing) for more info.
Users can *optionally* pass the instance of `io.opentelemetry.context.Context` to the SDKs using key **PARENT_TRACE_CONTEXT_KEY** on the [Context][context] parameter of the calling method
to provide explicit parent context.
This [sample][sample_key_vault] provides an example when parent span is picked up automatically.

```java readme-sample-context-auto-propagation
SecretClient secretClient = new SecretClientBuilder()
    .vaultUrl(VAULT_URL)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

Span span = tracer.spanBuilder("my-span").startSpan();
try (Scope s = span.makeCurrent()) {
    // ApplicationInsights or OpenTelemetry agent propagate context through async reactor calls.
    // So SecretClient here creates spans that are children of my-span
    System.out.printf("Secret with name: %s%n", secretClient.setSecret(new KeyVaultSecret("Secret1", "password1")).getName());
    secretClient.listPropertiesOfSecrets().forEach(secretBase ->
        System.out.printf("Secret with name: %s%n", secretClient.getSecret(secretBase.getName())));
} finally {
    span.end();
}

```

When using async clients without Application Insights Java agent or OpenTelemetry agent, please do context propagation manually:

```java  readme-sample-context-manual-propagation
SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
    .vaultUrl(VAULT_URL)
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();

Span span = tracer.spanBuilder("my-span").startSpan();
// when using async clients and instrumenting without ApplicationInsights or OpenTelemetry agent, context needs to be propagated manually
Context traceContext = Context.of(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current().with(span));
try {
    secretAsyncClient.setSecret(new KeyVaultSecret("Secret1", "password1"))
        .contextWrite(traceContext)
        .subscribe(secretResponse -> System.out.printf("Secret with name: %s%n", secretResponse.getName()));
    secretAsyncClient.listPropertiesOfSecrets()
        .contextWrite(traceContext)
        .doOnNext(secretBase -> secretAsyncClient.getSecret(secretBase.getName())
            .contextWrite(traceContext)
            .doOnNext(secret -> System.out.printf("Secret with name: %s%n", secret.getName())))
        .blockLast();
} finally {
    span.end();
}

```

### Using the plugin package with AMQP client libraries

Send a single event/message using [azure-messaging-eventhubs][azure-messaging-eventhubs] with tracing enabled.

Users can additionally pass the value of the current tracing span to the EventData object with key **PARENT_TRACE_CONTEXT_KEY** on the [Context][context] object:

```java readme-sample-context-manual-propagation-amqp
Flux<EventData> events = Flux.just(
    new EventData("EventData Sample 1"),
    new EventData("EventData Sample 2"));

// Create a batch to send the events.
final AtomicReference<EventDataBatch> batchRef = new AtomicReference<>(
    producer.createBatch().block());

final AtomicReference<io.opentelemetry.context.Context> traceContextRef = new AtomicReference<>(io.opentelemetry.context.Context.current());

// when using async clients and instrumenting without ApplicationInsights or OpenTelemetry agent, context needs to be propagated manually
// you would also want to propagate it manually when not making spans current.
// we'll propagate context to events (to propagate it over to consumer)
events.collect(batchRef::get, (b, e) ->
        b.tryAdd(e.addContext(PARENT_TRACE_CONTEXT_KEY, traceContextRef.get())))
    .flatMap(b -> producer.send(b))
    .doFinally(i -> Span.fromContext(traceContextRef.get()).end())
    .contextWrite(ctx -> {
        // this block is executed first, we'll create an outer span, which usually represents incoming request
        // or some logical operation
        Span span = TRACER.spanBuilder("my-span").startSpan();

        // and pass the new context with span to reactor for EventHubs producer client to pick it up.
        return ctx.put(PARENT_TRACE_CONTEXT_KEY, traceContextRef.updateAndGet(traceContext -> traceContext.with(span)));
    })
    .block();
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
[sample_async_key_vault]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/ListKeyVaultSecretsAutoConfigurationSample.java
[sample_eventhubs]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/PublishEventsJaegerExporterSample.java
[sample_key_vault]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/java/com/azure/core/tracing/opentelemetry/ListKeyVaultSecretsJaegerExporterSample.java
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src/samples/
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/core/azure-core-tracing-opentelemetry/src

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-tracing-opentelemetry%2FREADME.png)
