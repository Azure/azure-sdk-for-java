# Publishing Events with Azure Core Tracing OpenCensus 

Publishing events is one of the common use-cases for event publishers. 
Following documentation describes instructions to run a sample program for publishing events with tracing instrumentation for Java SDK libraries.

## Getting Started

### Adding the Azure client library for Event Hubs package to your project:
[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-messaging-eventhubs</artifactId>
  <version>5.0.0-preview.5</version>
</dependency>
 ```
[//]: # ({x-version-update-end})

### Adding the Azure core tracing opencensus plugin package to your project:
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

Azure Core Tracing OpenCensus library uses the **opencensus-api** which exposes the means for recording stats or traces and propagating context. Besides recording tracing events the application would also need to link the implementation and setup exporters to gather the tracing information.
In our example we will focus on using the  **opencensus-impl** as implementation package and  **Zipkin** exporter.

### Add the dependencies to your project:

```xml
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-exporter-trace-zipkin</artifactId>
  <version>0.20.0</version>
</dependency>
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-impl</artifactId>
  <version>0.20.0</version>
</dependency>
```

Program to demonstrate publishing multiple events with tracing support:
```java
/**
 * Sample demonstrates how to send a message to an Azure Event Hub with tracing support.
 */
public class PublishEvents {
    private static final String CONNECTION_STRING = System.getenv("AZURE_EVENTHUBS_CONNECTION_STRING");

    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub with trace spans exported to
     * Zipkin.
     *
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     */
    public static void main(String[] args) throws InterruptedException {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();
        Semaphore semaphore = new Semaphore(1);
        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();

        semaphore.acquire();
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.

        // Instantiate a client that will be used to call the service.
        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildAsyncClient();

        Context context = new Context(PARENT_SPAN_KEY, tracer.getCurrentSpan());
        final int count = 2;
        final byte[] body = "Hello World!".getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(body, context);
            return Flux.just(data);
        });

        // Create a producer. This overload of `createProducer` does not accept any arguments. Consequently, events
        // sent from this producer are load balanced between all available partitions in the Event Hub instance.
        EventHubAsyncProducer producer = client.createProducer();

        // Send those events. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(testData).subscribe(
            response -> System.out.println("Message successfully sent"),
            err -> {
                System.out.printf("Error thrown when sending the message. Error message: %s%n",
                    err.getMessage());
                scope.close();
                semaphore.release();
            },
            () -> {
                semaphore.release();
                System.out.println("The process has been completed.");
            });
        semaphore.acquire();
        try {
            producer.close();
        } catch (IOException e) {
            System.err.println("Error encountered while closing producer: " + e.toString());
        }

        client.close();
        scope.close();
        Tracing.getExportComponent().shutdown();
    }
}
```
