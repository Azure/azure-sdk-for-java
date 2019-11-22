# Publishing Events with Azure Core Tracing OpenCensus 

Publishing events is one of the common use-cases for event publishers. 
Following documentation describes instructions to run a sample program for publishing events to an event hub with tracing instrumentation.

## Getting Started
Sample uses **[opencensus-impl][opencensus_impl]** as implementation package and **[Zipkin Exporter][zipkin_exporter]** as exporter.

### Adding dependencies to your project:

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs;current})
```xml
<!-- Adding Azure Event Hubs dependency to your project -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-messaging-eventhubs</artifactId>
  <version>5.0.0-beta.5</version>
</dependency>
 ```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opencensus;current})
```xml
<!-- Adding Azure core tracing OpenCensus plugin package to your project -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-tracing-opencensus</artifactId>
  <version>1.0.0-beta.5</version>
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

#### Sample demonstrates tracing when publishing multiple events to an eventhub using [azure-messaging-eventhubs][azure_messaging_eventhubs] client library.
```java

import com.azure.core.util.Context;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.opencensus.common.Scope;
import io.opencensus.exporter.trace.zipkin.ZipkinExporterConfiguration;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import reactor.core.publisher.Flux;

public class PublishEvents {
    private static final String CONNECTION_STRING = System.getenv("AZURE_EVENTHUBS_CONNECTION_STRING");
    
    private static final Tracer TRACER = Tracing.getTracer();

    static {
      setupOpenCensusAndZipkinExporter();
    }
    
    public static void main(String[] args) {
        final int count = 2;
        final byte[] body = "Hello World!".getBytes(UTF_8);

        try(Scope scope = TRACER.spanBuilder("user-parent-span").startScopedSpan()) {

            Context traceContext = new Context(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
           
            EventHubProducerClient producer = new EventHubClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildProducer();
    
            final Context traceContext = new Context(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
            final Flux<EventData> testData = Flux.range(0, count)
                    .flatMap(number -> {
                    final EventData data = new EventData(body, traceContext);
                    return Flux.just(data);
            });
            
            producer.send(testData.toIterable(1));
        } finally {
            producer.close();            
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
[azure_messaging_eventhubs]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[opencensus_impl]: https://mvnrepository.com/artifact/io.opencensus/opencensus-impl/
[zipkin_exporter]: https://mvnrepository.com/artifact/io.opencensus/opencensus-exporter-trace-zipkin
