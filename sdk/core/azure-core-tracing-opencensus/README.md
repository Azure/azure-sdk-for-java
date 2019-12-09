# Azure OpenCensus Tracing client library for Java
This package enables distributed tracing across Azure SDK Java libraries through [OpenCensus][opencensus]. OpenCensus is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services. 
The Azure core OpenCensus tracing package provides:
- Context propagation used to correlate activities and requests between services with an initial customer action.
- Tracing user requests to the system, allowing to pinpoint failures and performance issues.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][api_documentation] | [Samples][samples]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-beta.5</version> <!-- {x-version-update;com.azure:azure-core-tracing-opencensus;current} -->
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
Tracing OpenCensus to use Netty HTTP client. 

### Alternate HTTP Client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<!-- Add Tracing OpenCensus without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opencensus</artifactId>
    <version>1.0.0-beta.5</version> <!-- {x-version-update;com.azure:azure-core-tracing-opencensus;current} -->
    <exclusions>
      <exclusion>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-http-netty</artifactId> <!-- {x-version-update;com.azure:azure-core-http-netty;current} -->
      </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add OkHTTP client to use with Tracing OpenCensus package -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId> <!-- {x-version-update;com.azure:azure-core-http-okhttp;current} -->
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
The following sections provides examples of using the azure-core-tracing-opencensus plugin with some of the Azure Java SDK libraries:
### Using the plugin package with HTTP client libraries
Synchronously create a secret using [azure-security-keyvault-secrets][azure_security_keyvault_secrets] with tracing enabled.

Users can additionally pass the value of the current tracing span to the calling method using key PARENT_SPAN_KEY on the [Context][context] parameter.

```java
private static  final Tracer TRACER;

    static {
        TRACER = configureOpenCensusAndZipkinExporter();
    }

    public static void main(String[] args) {
        doClientWork();
    }

    public static void doClientWork() {
        SecretClient client = new SecretClientBuilder()
            .endpoint("<your-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    
        try (Scope scope = TRACER.spanBuilder("tracing-user-span").startScopedSpan()) {
        
            // Create context with key PARENT_SPAN_KEY to use current tracing span for encapsulating the children spans
            Context tracingContext = new Context(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
        
            // Set secret and pass the created tracing context to the calling method
            Secret secret = client.setSecretWithResponse(new Secret("Secret1", "password1", tracingContext));
            System.out.printf("Secret is created with name %s and value %s %n", secret.getName(), secret.getValue());
        } finally {
            Tracing.getExportComponent().shutdown();
        }
    }
```

### Using the plugin package with AMQP client libraries
Async send single event using [azure-messaging-eventhubs][azure_messaging_eventhubs] with tracing.
    
Users can additionally pass the value of the current tracing span to the EventData object with key **PARENT_SPAN_KEY** on the [Context][context] object:

```java
private static  final Tracer TRACER;

    static {
        TRACER = configureOpenCensusAndZipkinExporter();
    }

    public static void main(String[] args) {
        doClientWork();
    }

    public static void doClientWork() {
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildProducerClient();

        try (Scope scope = TRACER.spanBuilder("tracing-user-span").startScopedSpan()) {
            EventData event1 = new EventData("1".getBytes(UTF_8));
            event1.addContext(PARENT_SPAN_KEY, span);

            EventDataBatch eventDataBatch = producer.createBatch();

            if (!eventDataBatch.tryAdd(eventData)) {
                producer.send(eventDataBatch);
                eventDataBatch = producer.createBatch();
            }
        } finally {
            Tracing.getExportComponent().shutdown();
        }
    }
```

## Troubleshooting
### General
For more information on OpenCensus Java support for tracing, see [OpenCensus Java Quickstart][opencensus_quickstart].

## Next steps
### Samples
These following samples provide example code for additional scenarios commonly encountered while working with Tracing:

#### Enqueue and dequeue messages with Tracing
* [Queue Client Enqueue Messages][sample_helloWorld] and [Async Queue Client Enqueue Messages][sample_helloWorldAsync] - Tracing enabled sample for
    * Create a Queue client using [azure-storage-queue][azure_storage_queue] client library.
    * Enqueue and dequeue messages using the created Queue client.
#### List Operations for secrets in a Key Vault with Tracing
* [List Key Vault Secrets][sample_list] and [Async List Key Vault Secrets][sample_list_async] - Tracing enabled sample for creating and listing secrets of a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets].
#### Publish multiple Events to an Event Hub with Tracing
* [Publish Events][sample_publish_events] - Tracing enabled sample for publishing multiple events to a specific event hub.

### Additional Documentation
For more extensive documentation on OpenCensus, see the [API reference documentation][opencensus].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[azure_messaging_eventhubs_mvn]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[azure_storage_queue]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[context]: ../azure-core/src/main/java/com/azure/core/util/Context.java
[create-eventhubs-builders]: ../../eventhubs/azure-messaging-eventhubs#create-an-event-hub-client-using-a-connection-string
[maven]: https://maven.apache.org/
[source_code]:  src
[sample_helloWorld]: ./src/samples/QueueClientEnqueueMessages.md
[sample_helloWorldAsync]: ./src/samples/AsyncQueueClientEnqueueMessages.md
[sample_list]: ./src/samples/ListeKeyVaultSecrets.md
[sample_list_async]: ./src/samples/AsyncListKeyVaultSecrets.md
[sample_publish_events]: ./src/samples/PublishEvents.md
[samples]: ./src/samples/
[opencensus]: https://opencensus.io/quickstart/java/tracing/
[opencensus_quickstart]: https://opencensus.io/quickstart/java/tracing/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fcore%2Fazure-core-tracing-opencensus%2FREADME.png)
