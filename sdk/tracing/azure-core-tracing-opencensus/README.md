# Azure Tracing client library for Java
This package enables distributed tracing across Azure SDK Java libraries through [Opencensus][opencensus]. OpenCensus is an open source, vendor-agnostic, single distribution of libraries to provide metrics collection and distributed tracing for services. 
The Azure core tracing package provides:
- Context propogation, in order to correlate activities and requests between services with an initial customer action.
- Tracing user requests to the system, allowing to pinpoint failures and performance issues.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product
documentation][aztracing_docs] | [Samples][samples]

## Getting started
### Prerequisites
- Java Development Kit (JDK) with version 8 or above
- [Maven][maven]
```xml
<dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-api</artifactId>
    <version>${opencensus.version}</version>
</dependency>

<dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-impl</artifactId>
    <version>${opencensus.version}</version>
</dependency>

<dependency>
    <groupId>io.opencensus</groupId>
    <artifactId>opencensus-exporter-trace-zipkin</artifactId>
    <version>${opencensus.version}</version>
</dependency>
```

### Adding the package to your product
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-preview.3</version>
</dependency>
```
## Key concepts
### Trace
A trace is a tree of spans showing the path of work through a system. A trace on its own is distinguishable by a unique 16 byte sequence called a TraceID.
### Span
A span represents a single operation in a trace. A span could be representative of an HTTP request, a remote procedure call (RPC), a database query, or even the path that a code takes.

## Examples
The following sections provides examples of using tracing-opencensus plugin with some of the Azure Java SDK libraries:
### Using the plugin package with Http Client libraries
- Sync create a Secret using [azure-keyvault-secrets][azure-keyvault-secrets] with tracing enabled.
    
    Users can additionally pass the value of the current tracing span to the sdk's using key **"opencensus-span"** on the [Context][context] object as shown below:
      and value of current tracing span.
	```java
	try (Scope scope = tracer.spanBuilder("tracing-user-parent").startScopedSpan()) { 
		secretClient.setSecretWithResponse(new Secret("BankAccountPassword", "f4G34fMh8v"),
	    		new Context("opencensus-span", tracer.getCurrentSpan()));
	}
	```
- Async Create a Secret using [azure-keyvault-secrets][azure-keyvault-secrets] with tracing enabled
    
    Users can additionally pass the value of the current tracing span to the sdk's using key **"opencensus-span"** on the subscriberContext of the calling method:
	```java
	try (Scope scope = tracer.spanBuilder("tracing-user-parent").startScopedSpan()){
	    Secret secretResponse = secretAsyncClient.setSecret(new Secret("BankAccountPassword", "f4G34fMh8v")
						.expires(OffsetDateTime.now().plusYears(1)))
						.subscriberContext(Context.of("opencensus-span", tracer.getCurrentSpan()))
						.block();
	}
	```

### Using the plugin package with AMQP Client libraries
Sync send single event using [azure-messaging-eventhubs][azure-messaging-eventhubs] with tracing.
    
Users can additionally pass the value of the current tracing span to the EventData object with key **"opencensus-span"** on the [Context][context] object:
	```java
	try (Scope scope = tracer.spanBuilder("tracing-amqp").startScopedSpan()) {
		// Create an event to send.
		final EventData eventData = new EventData("Hello world!".getBytes(UTF_8));
		eventData.context(new Context("opencensus-span", tracer.getCurrentSpan()));
		producer.send(eventData).doOnSuccess((ignored) -> System.out.println("Event sent.")).block();
	}
	```
## Troubleshooting

## Next steps
Several Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Tracing:

#### Hello World Samples
* [HelloWorld.java][sample_helloWorld] - and [HelloWorldAsync.java][sample_helloWorldAsync] - Contains samples with tracing enabled for following scenarios:
    * Create a queue client using [azure-storage-queue][azure-storage-queue] Java sdk
    * Enqueue and dequeue messages using the created QueueClient
#### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_list_async] - Contains samples with tracing enabled for following scenarios:
    * Create a Key
    * List Keys
    * Create new version of existing key.
    * List versions of an existing key.
#### Publish multiple events Sample
* [PublishEvents.java][sample_publish_events] - Contains samples with tracing enabled for publishing multiple events with tracing support.

###  Additional Documentation
For more extensive documentation on OpenCensus, see the [API reference documentation][opencensus].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- Links -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java/track2reports/index.html
[azure-keyvault-secrets]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/keyvault/azure-keyvault-secrets
[azure-storage-queue]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-queue
[maven]: https://maven.apache.org/
[source_code]:  src
[aztracing_docs]: TODO
[sample_helloWorld]: ./src/samples/java/com/azure/core/tracing//HelloWorld.java
[sample_helloWorldAsync]: ./src/samples/java/com/azure/core/tracing/HelloWorldAsync.java
[sample_list]: ./src/samples/java/com/azure/core/tracing//ListOperationsTracing.java
[sample_list_async]: ./src/samples/java/com/azure/core/tracing//ListOperationsAsync.java
[sample_publish_events]: ./src/samples/java/com/azure/core/tracing/PublishEvents.java
[samples]: ./src/samples/java/com/azure/core/tracing/
[opencensus]: https://opencensus.io/quickstart/java/tracing/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/tracing/README.png)
