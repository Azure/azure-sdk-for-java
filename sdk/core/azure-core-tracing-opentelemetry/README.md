# Azure Tracing OpenTelemetry client library for Java
This package enables distributed tracing across Azure SDK Java libraries through [OpenTelemetry][OpenTelemetry]. OpenTelemetry is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services. 
The Azure core tracing package provides:
- Context propagation, used to correlate activities and requests between services with an initial customer action.
- Tracing user requests to the system, allowing to pinpoint failures and performance issues.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][api_documentation] | [Samples][samples]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]

### Adding package to your product
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opentelemetry</artifactId>
  <version>1.0.0-beta.1</version> <!-- {x-version-update;com.azure:azure-core-tracing-opentelemetry;current} -->
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
    <version>1.0.0-beta.1</version> <!-- {x-version-update;com.azure:azure-core-tracing-opentelemetry;current} -->
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
  <version>1.0.0</version> <!-- {x-version-update;com.azure:azure-core-http-okhttp;current} -->
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
    
    Users can additionally pass the value of the current tracing span to the SDKs using key **PARENT_SPAN_KEY** on the [Context][context] parameter of the calling method.
    The plugin package creates a root span to encapsulate all the child spans created in the calling methods when no parent span is passed in the context.
    This [sample][sample_key_vault] provides an example when no user parent span is passed.
    ```java
    private static  final Tracer TRACER;
    private static final TracerSdkFactory TRACER_SDK_FACTORY;
        
        static {
            TRACER_SDK_FACTORY = configureOpenTelemetryAndJaegerExporter();
            TRACER = TRACER_SDK_FACTORY.get("Sample");
        }
    
        public static void main(String[] args) {
            doClientWork();
            TRACER_SDK_FACTORY.shutdown();
        }

        public static void doClientWork() {
          SecretClient client = new SecretClientBuilder()
            .endpoint("<your-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
                    
          Span span = TRACER.spanBuilder("user-parent-span").startSpan();
          try (Scope scope = TRACER.withSpan(span)) {
              final Context traceContext = new Context(PARENT_SPAN_KEY, span);
              secretClient.setSecretWithResponse(new Secret("secret_name", "secret_value", traceContext));
          } finally {
              span.end();
          }
        }
    ```

### Using the plugin package with AMQP client libraries
Send a single event/message using [azure-messaging-eventhubs][azure-messaging-eventhubs] with tracing enabled.
    
Users can additionally pass the value of the current tracing span to the EventData object with key **PARENT_SPAN_KEY** on the [Context][context] object:

```java
private static final Tracer TRACER;
private static final TracerSdkFactory TRACER_SDK_FACTORY;
    
    static {
        TRACER_SDK_FACTORY = configureOpenTelemetryAndJaegerExporter();
        TRACER = TRACER_SDK_FACTORY.get("Sample");
    }

    public static void main(String[] args) {
        doClientWork();
        TRACER_SDK_FACTORY.shutdown();
    }

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

## Next steps
### Samples
Several Java SDK samples are available to you in the SDKs GitHub repository. 
These following samples provide example code for additional scenarios commonly encountered while working with Tracing:
* [Set Configuration Setting][sample_app_config] - Tracing enabled Sample for setting a configuration setting using [azure-data-app-configuration][azure_data_app_configuration].
* [List Key Vault Secrets][sample_key_vault] - Tracing enabled sample for creating and listing secrets of a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets].
* [Publish Events][sample_eventhubs] - Tracing enabled sample for publishing multiple events using [azure-messaging-eventhubs][azure_messaging_eventhubs_mvn].
* [Async List Key Vault Secrets][sample_async_key_vault] - Tracing enabled sample for asynchronously creating and listing secrets of a Key Vault using [azure-security-keyvault-secrets][azure_keyvault_secrets].

### Additional Documentation
For more extensive documentation on OpenTelemetry, see the [API reference documentation][OpenTelemetry].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java/track2reports/index.html
[azure_data_app_configuration]: https://mvnrepository.com/artifact/com.azure/azure-data-appconfiguration/
[azure-security-keyvault-secrets]: ../../keyvault/azure-security-keyvault-secrets
[azure_keyvault_secrets]: https://mvnrepository.com/artifact/com.azure/azure-security-keyvault-secrets
[azure-messaging-eventhubs]: ../../eventhubs/azure-messaging-eventhubs
[azure_messaging_eventhubs_mvn]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[context]: ../azure-core/src/main/java/com/azure/core/util/Context.java
[create-eventhubs-builders]: ../../eventhubs/azure-messaging-eventhubs#create-an-event-hub-client-using-a-connection-string
[maven]: https://maven.apache.org/
[source_code]:  src
[api_documentation]: https://aka.ms/java-docs
[sample_app_config]: ./src/samples/CreateConfigurationSettingTracingSample.md
[sample_key_vault]: ./src/samples/ListKeyVaultSecretsTracingSample.md
[sample_async_key_vault]: ./src/samples/AsyncListKeyVaultSecretsSample.md
[sample_eventhubs]: ./src/samples/PublishEventsTracingSample.md
[samples]: ./src/samples/
[OpenTelemetry]: https://github.com/open-telemetry/opentelemetry-java
[OpenTelemetry-quickstart]: https://github.com/open-telemetry/opentelemetry-java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/core/azure-core-tracing-opentelemetry/README.png)
