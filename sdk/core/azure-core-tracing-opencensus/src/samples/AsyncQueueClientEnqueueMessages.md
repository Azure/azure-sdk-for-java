# Enqueue and Dequeue messages asynchronously with Azure Storage Queues and Azure Core Tracing OpenCensus
 
Following documentation describes instructions to run a sample program for basic operations of enqueueing and dequeueing 
messages asynchronously on queue client with tracing instrumentation for Java SDK libraries.

## Getting Started

### Adding the Azure client library for Azure storage queue package to your project:
[//]: # ({x-version-update-start;com.azure:azure-storage-queue;current})
```xml
<!-- Add Storage Queue dependency -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-queue</artifactId>
  <version>12.0.0-preview.4</version>
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
 *  This example shows tracing support in azure-storage-queue SDK using azure-core-tracing plugin package.
 */
public class HelloWorldAsync {
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
        String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, "<GENERATED_SAS_TOKEN>");
        QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueURL).buildAsyncClient();

        // Create a queue, enqueue two messages.
        queueAsyncClient.create()
            .doOnSuccess(response -> queueAsyncClient.enqueueMessage("This is message 1"))
            .then(queueAsyncClient.enqueueMessage("This is message 2"))
            .subscriberContext(Context.of(PARENT_SPAN_KEY, tracer.getCurrentSpan()))
            .subscribe(
                response -> System.out.printf("Message successfully enqueued by queueAsyncClient. Message id: %s%n",
                    response.getMessageId()),
                err -> {
                    System.out.printf("Error thrown when enqueue the message. Error message: %s%n",
                        err.getMessage());
                    scope.close();
                    semaphore.release();
                },
                () -> {
                    scope.close();
                    semaphore.release();
                    System.out.println("The enqueue has been completed.");
                    Tracing.getExportComponent().shutdown();
                });
    }
}
```
