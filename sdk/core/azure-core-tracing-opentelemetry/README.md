# Azure Tracing client library for Java
This package enables distributed tracing across Azure SDK Java libraries through [opentelemetry][opentelemetry]. OpenTelemetry is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services. 
The Azure core tracing package provides:
- Context propagation, in order to correlate activities and requests between services with an initial customer action.
- Tracing user requests to the system, allowing to pinpoint failures and performance issues.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][api_documentation] | [Samples][samples]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]

### Adding the package to your product
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opentelemetry</artifactId>
  <version>1.0.0-preview.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
Tracing OpenTelemetry to use Netty HTTP client. 

### Alternate HTTP Client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<!-- Add Tracing OpenTelemetry without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-preview.1</version>
    <exclusions>
      <exclusion>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-http-netty</artifactId>
      </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add OkHTTP client to use with Tracing OpenTelemetry package -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders][create-eventhubs-builders], unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

## Key concepts
### Trace
A trace is a tree of spans showing the path of work through a system. A trace on its own is distinguishable by a unique 16 byte sequence called a TraceID.
### Span
A span represents a single operation in a trace. A span could be representative of an HTTP request, a remote procedure call (RPC), a database query, or even the path that a code takes.

## Examples
The following sections provides examples of using the azure-core-tracing-opentelemetry plugin with some of the Azure Java SDK libraries:
### Using the plugin package with HTTP client libraries
- Synchronously create a secret using [azure-security-keyvault-secrets][azure-security-keyvault-secrets] with tracing enabled.
    
    Users can additionally pass the value of the current tracing span to the SDKs using key **"opentelemetry-span"** on the [Context][context] parameter of the calling method:
    ```java
    import com.azure.identity.credential.DefaultAzureCredentialBuilder;
    import com.azure.security.keyvault.secrets.SecretClientBuilder;
    import com.azure.security.keyvault.secrets.SecretClient;
    import com.azure.security.keyvault.secrets.models.Secret;
    import io.opentelemetry.OpenTelemetry;
    import io.opentelemetry.context.Scope;
    import io.opentelemetry.trace.Span;
    import io.opentelemetry.trace.Tracer;
    import com.azure.core.util.Context;

    import static com.azure.core.implementation.tracing.Tracer.PARENT_SPAN_KEY;
    
    static {
      TracerSdkFactory tracerSdkFactory = setupOpenTelemetryAndExporter();
      Tracer tracer = tracerSdkFactory.get("Azure-OpenTelemetry");
      doClientWork(tracer);
      tracerSdkFactory.shutdown();   
    }

    public static void doClientWork(Tracer tracer) {
      SecretClient client = new SecretClientBuilder()
        .endpoint("<your-vault-url>")
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
      
      try (final Scope scope = tracer.withSpan(tracer.spanBuilder("user-parent-span").startSpan())) {
          final Context traceContext = new Context(PARENT_SPAN_KEY, tracer.getCurrentSpan());
          // send user parent span to client call in context object
          client.setConfigurationSettingWithResponse(new ConfigurationSetting().setKey("hello").setValue("world"), true, traceContext);
      } finally {
          span.end();
      }
    }
    ```

### Using the plugin package with AMQP client libraries
Async send single event using [azure-messaging-eventhubs][azure-messaging-eventhubs] with tracing.
    
Users can additionally pass the value of the current tracing span to the EventData object with key **"opentelemetry-span"** on the [Context][context] object:

```java
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import com.azure.core.util.Context;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubAsyncClient;
import com.azure.messaging.eventhubs.EventHubAsyncProducer;
import com.azure.messaging.eventhubs.EventHubClientBuilder;

import static com.azure.core.implementation.tracing.Tracer.PARENT_SPAN_KEY;
    
  static {
    TracerSdkFactory tracerSdkFactory = setupOpenTelemetryAndExporter();
    Tracer tracer = tracerSdkFactory.get("Azure-OpenTelemetry");
    doClientWork(tracer);
    tracerSdkFactory.shutdown();   
  }

  public static void doClientWork(Tracer tracer) { 
    EventHubClient client = new EventHubClientBuilder()
    .connectionString("<< CONNECTION STRING FOR SPECIFIC EVENT HUB INSTANCE >>")
    .buildClient();
    EventHubProducer producer = client.createProducer();

    try (final Scope scope = tracer.withSpan(tracer.spanBuilder("user-parent-span").startSpan())) {
      final Context traceContext = new Context(PARENT_SPAN_KEY, tracer.getCurrentSpan());
      // Create an event to send
      final EventData eventData = new EventData("Hello world!".getBytes(UTF_8));
      // Add tracing context to the event
      eventData.context(tracingContext);
      producer.send(eventData); 
    } finally {
        span.end();
      }
    }
```

## Troubleshooting
### General

For more information on opentelemetry Java support for tracing, see [opentelemetry Java Quickstart][opentelemetry-quickstart].

## Next steps
### Samples
Several Java SDK samples are available to you in the SDKs GitHub repository. 
These following samples provide example code for additional scenarios commonly encountered while working with Tracing:

#### Enqueue and dequeue messages using Storage Queue client
* [QueueClientEnqueueMessages][sample_helloWorld] and [AsyncQueueClientEnqueueMessages][sample_helloWorldAsync] - Contains samples for following scenarios with tracing support:
    * Create a Queue client using [azure-storage-queue][azure-storage-queue] Java SDK.
    * Enqueue and dequeue messages using the created Queue client.
#### List Operations for secrets in a Key Vault
* [ListKeyVaultSecrets][sample_list] and [AsyncListKeyVaultSecrets][sample_list_async] - Contains samples for following scenarios with tracing support:
    * Create a secret.
    * List secrets.
    * Create new version of an existing secret.
    * List versions of an existing secret.
#### Publish multiple Events to an Event Hub
* [PublishEvents][sample_publish_events] - Contains sample for publishing multiple events with tracing support.

### Additional Documentation
For more extensive documentation on OpenTelemetry, see the [API reference documentation][opentelemetry].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java/track2reports/index.html
[azure-security-keyvault-secrets]: ../keyvault/azure-security-keyvault-secrets
[azure-messaging-eventhubs]: ../eventhubs/azure-messaging-eventhubs
[azure-storage-queue]: ../storage/azure-storage-queue
[context]: ../core/azure-core/src/main/java/com/azure/core/util/Context.java
[create-eventhubs-builders]: ../eventhubs/azure-messaging-eventhubs#create-an-event-hub-client-using-a-connection-string
[maven]: https://maven.apache.org/
[source_code]:  src
[api_documentation]: https://aka.ms/java-docs
[sample_helloWorld]: ./src/samples/QueueClientEnqueueMessages.md
[sample_helloWorldAsync]: ./src/samples/AsyncQueueClientEnqueueMessages.md
[sample_list]: ./src/samples/ListeKeyVaultSecrets.md
[sample_list_async]: ./src/samples/AsyncListKeyVaultSecrets.md
[sample_publish_events]: ./src/samples/PublishEvents.md
[samples]: ./src/samples/
[opentelemetry]: https://github.com/open-telemetry/opentelemetry-java
[opentelemetry-quickstart]: https://github.com/open-telemetry/opentelemetry-java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/core/azure-core-tracing-opentelemetry/README.png)
