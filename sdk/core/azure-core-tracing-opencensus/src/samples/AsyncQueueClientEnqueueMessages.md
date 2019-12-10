# Asynchronously enqueue and dequeue messages Azure Core Tracing OpenCensus
 
Following documentation describes instructions to run a sample program for asynchronously enqueueing and dequeuing 
messages on queue client with tracing instrumentation.

## Getting Started
Sample uses **[opencensus-impl][opencensus_impl]** as implementation package and **[Zipkin Exporter][zipkin_exporter]** as exporter.

### Adding dependencies to your project:
[//]: # ({x-version-update-start;com.azure:azure-storage-queue;current})
```xml
<!-- Add Storage Queue dependency -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-queue</artifactId>
  <version>12.1.0</version> <!-- {x-version-update;com.azure:azure-storage-queue;current} -->
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<!-- Add Azure core tracing OpenCensus plugin package to your project -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-beta.5</version> <!-- {x-version-update;com.azure:azure-core-tracing-opencensus;current} -->
</dependency>
```
[//]: # ({x-version-update-end})
```xml
<!-- Add opencensus-impl and opencensus-zipkin-exporter to your project -->

<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-exporter-trace-zipkin</artifactId>
  <version>0.24.0</version>
</dependency>
<dependency>
  <groupId>io.opencensus</groupId>
  <artifactId>opencensus-impl</artifactId>
  <version>0.24.0</version>
</dependency>
```
#### Sample demonstrates tracing when asynchronously queueing and dequeuing of messages using [azure-storage-queue][azure_storage_queue] client library.
```java
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueAsyncClient;
import com.azure.storage.queue.QueueClientBuilder;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinExporterConfiguration;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

public class Sample {

    private static final String ACCOUNT_NAME = System.getenv("AZURE_STORAGE_ACCOUNT_NAME");
    private static final String QUEUE_NAME = "queue_name";

    private static final Tracer TRACER = Tracing.getTracer();
    
    static {
      setupOpenCensusAndZipkinExporter();
    }
    
    public static void main(String[] args) {
      Semaphore semaphore = new Semaphore(1);
      Scope scope = TRACER.spanBuilder("user-parent-span").startScopedSpan();

      semaphore.acquire();
      String queueURL = String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, "<GENERATED_SAS_TOKEN>");
      QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueURL).buildAsyncClient();

      // Create a queue, enqueue two messages.
      queueAsyncClient.create()
          .doOnSuccess(response -> queueAsyncClient.sendMessage("This is message 1"))
          .then(queueAsyncClient.sendMessage("This is message 2"))
          .subscriberContext(Context.of(PARENT_SPAN_KEY, TRACER.getCurrentSpan()))
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

    /**
     * Please refer to the <a href=https://zipkin.io/pages/quickstart>Quickstart Zipkin</a> for more documentation on
     * using a Zipkin exporter.
     */
    private static void setupOpenCensusAndZipkinExporter() {
        TraceConfig traceConfig = Tracing.getTraceConfig();
        traceConfig.updateActiveTraceParams(
            traceConfig.getActiveTraceParams().toBuilder().setSampler(Samplers.alwaysSample()).build());

        ZipkinExporterConfiguration configuration =
            ZipkinExporterConfiguration.builder()
                .setServiceName("sample-service")
                .setV2Url("http://localhost:9411/api/v2/spans")
                .build();

        ZipkinTraceExporter.createAndRegister(configuration);
    }
}
```

<!-- Links -->
[azure_storage_queue]: https://mvnrepository.com/artifact/com.azure/azure-storage-queue
[opencensus_impl]: https://mvnrepository.com/artifact/io.opencensus/opencensus-impl/
[zipkin_exporter]: https://mvnrepository.com/artifact/io.opencensus/opencensus-exporter-trace-zipkin
