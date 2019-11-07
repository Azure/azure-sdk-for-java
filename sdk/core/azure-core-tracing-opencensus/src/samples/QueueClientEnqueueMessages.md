# Enqueue and Dequeue messages with Azure Storage Queues and Azure Core Tracing OpenCensus
 
Following documentation describes instructions to run a sample program for basic operations of enqueueing and dequeueing 
messages on queue client with tracing instrumentation for Java SDK libraries.

## Getting Started

### Adding the Azure client library for Azure storage queue package to your project:
[//]: # ({x-version-update-start;com.azure:azure-storage-queue;current})
```xml
<!-- Add Storage Queue dependency -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-queue</artifactId>
  <version>12.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Adding the Azure core tracing OpenCensus plugin package to your project:
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
/*
 * This example shows tracing support in azure-storage-queue SDK using azure-core-tracing plugin package.
 */
public class HelloWorld {
    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String QUEUE_NAME = "queue_name";

    /**
     * The main method shows how we do the basic operations of enqueueing and dequeueing messages on async queue client
     * with tracing enabled support. This example relies on user the having started a Zipkin exporter localhost on port
     * 9411.
     *
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     *
     * @param args No args needed for main method.
     */
    public static void main(String[] args) {
        ZipkinTraceExporter.createAndRegister("http://localhost:9411/api/v2/spans", "tracing-to-zipkin-service");

        TraceConfig traceConfig = Tracing.getTraceConfig();
        TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
        traceConfig.updateActiveTraceParams(activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build());

        Tracer tracer = Tracing.getTracer();

        Scope scope = tracer.spanBuilder("user-parent-span").startScopedSpan();
        try {
            QueueClient queueClient = new QueueClientBuilder()
                .endpoint(String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, "<GENERATED_SAS_TOKEN>"))
                .buildClient();

            // Create a queue, enqueue two messages.
            Context tracingContext = new Context(PARENT_SPAN_KEY, tracer.getCurrentSpan());
            queueClient.createWithResponse(null, null, tracingContext);
            queueClient.sendMessageWithResponse("This is message 1", null, null, null, tracingContext);
            queueClient.sendMessageWithResponse("This is message 2", null, null, null, tracingContext);
            System.out.println("Enqueuing of messages has completed!");

        } finally {
            scope.close();
            Tracing.getExportComponent().shutdown();
        }
    }
}
```
