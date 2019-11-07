# Publish events with Azure Core Tracing Opentelemetry
 
Following documentation describes instructions to run a sample program for publishing multiple events with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** for implementation and **[LoggingExporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependencies>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-sdk</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-messaging-eventhubs</artifactId>
        <version>5.0.0-preview.5</version>
    </dependency>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-tracing-opentelemetry</artifactId>
        <version>1.0.0-preview.1</version>
    </dependency>
    <dependency>
        <groupId>io.opentelemetry</groupId>
        <artifactId>opentelemetry-exporters-logging</artifactId>
        <version>0.2.0-SNAPSHOT</version>
    </dependency>
</dependencies>

```
#### Sample demonstrates tracing when publishing multiple events to an eventhub instance using [azure-messaging-eventhubs][azure_messaging_eventhubs] client library.
```java
import com.azure.core.util.Context;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.TracerSdkFactory;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import reactor.core.publisher.Flux;

import java.util.logging.Logger;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Logger.getLogger;

public class Sample {
    final static String VAULT_URL = "<YOUR_VAULT_URL>";
    private static final Logger LOGGER = getLogger("Sample");
    private static  final Tracer TRACER;
    private static final TracerSdkFactory TRACER_SDK_FACTORY;

    static {
        TRACER_SDK_FACTORY = configureOpenTelemetryAndLoggingExporter();
        TRACER = TRACER_SDK_FACTORY.get("Azure-OpenTelemetry");
    }

    public static void main(String[] args) {
        doClientWork();
        TRACER_SDK_FACTORY.shutdown();
        LOGGER.info("=== Tracer Shutdown  ===");
    }

    private static TracerSdkFactory configureOpenTelemetryAndLoggingExporter() {
        LOGGER.info("=== Running With LoggingExporter ===");

        LoggingExporter exporter = new LoggingExporter();
        TracerSdkFactory tracerSdkFactory = (TracerSdkFactory) OpenTelemetry.getTracerFactory();
        tracerSdkFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());

        return tracerSdkFactory;
    }

    private static void doClientWork() {
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildProducer();

        final int count = 2;
        final byte[] body = "Hello World!".getBytes(UTF_8);
    
        LOGGER.info("=== Start user scoped span  ===");
    
        Span span = TRACER.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = TRACER.withSpan(span)) {
            final Context traceContext = new Context(PARENT_SPAN_KEY, TRACER.getCurrentSpan());
            final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
                final EventData data = new EventData(body, traceContext);
                return Flux.just(data);
            });
            producer.send(testData.toIterable(1));
        } finally {
            span.end();
            producer.close();
            LOGGER.info("=== End user scoped span  ===");
        }
    }
}
```

<!-- Links -->
[azure_messaging_eventhubs]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
