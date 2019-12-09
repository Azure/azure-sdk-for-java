# Enqueue and Dequeue messages with Azure Core Tracing OpenCensus
 
Following documentation describes instructions to run a sample program for basic operations of enqueueing and dequeuing 
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

#### Sample demonstrates tracing when queueing and dequeuing of messages using [azure-storage-queue][azure_storage_queue] client library.
```java
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
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
        Scope scope = TRACER.spanBuilder("user-parent-span").startScopedSpan();
        try {
            QueueClient queueClient = new QueueClientBuilder()
                .endpoint(String.format("https://%s.queue.core.windows.net/%s%s", ACCOUNT_NAME, QUEUE_NAME, "<GENERATED_SAS_TOKEN>"))
                .buildClient();

            // Create a queue, enqueue two messages.
            Context tracingContext = new Context(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
            queueClient.createWithResponse(null, null, tracingContext);
            queueClient.sendMessageWithResponse("This is message 1", null, null, null, tracingContext);
            queueClient.sendMessageWithResponse("This is message 2", null, null, null, tracingContext);
            System.out.println("Enqueuing of messages has completed!");

        } finally {
            scope.close();
            Tracing.getExportComponent().shutdown();
        }
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
