# Publish events with Azure Core Tracing OpenTelemetry
 
Following documentation describes instructions to run a sample program for publishing multiple events with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** for implementation and **[Logging Exporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>0.2.0</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporters-logging</artifactId>
    <version>0.2.0</version>
</dependency>
```

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>5.0.0-beta.6</version>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-tracing-opentelemetry;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

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
        TRACER = TRACER_SDK_FACTORY.get("Sample");
    }

    public static void main(String[] args) {
        doClientWork();
        TRACER_SDK_FACTORY.shutdown();
    }

    private static TracerSdkFactory configureOpenTelemetryAndLoggingExporter() {
        LoggingExporter exporter = new LoggingExporter();
        TracerSdkFactory tracerSdkFactory = (TracerSdkFactory) OpenTelemetry.getTracerFactory();
        tracerSdkFactory.addSpanProcessor(SimpleSpansProcessor.newBuilder(exporter).build());

        return tracerSdkFactory;
    }

    private static void doClientWork() {
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildAsyncProducerClient();
        
        Span span = TRACER.spanBuilder("user-parent-span").startSpan();
        try (final Scope scope = TRACER.withSpan(span)) {
            String firstPartition = producer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);
            
            final byte[] body = "EventData Sample 1".getBytes(UTF_8);
            final byte[] body2 = "EventData Sample 2".getBytes(UTF_8);

            // We will publish three events based on simple sentences.
            Flux<EventData> data = Flux.just(
                new EventData(body).addContext(PARENT_SPAN_KEY, TRACER.getCurrentSpan()),
                new EventData(body2).addContext(PARENT_SPAN_KEY, TRACER.getCurrentSpan()));

            // Create a batch to send the events.
            final CreateBatchOptions options = new CreateBatchOptions()
                .setPartitionId(firstPartition)
                .setMaximumSizeInBytes(256);

            final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
                producer.createBatch(options).block());
            
            data.flatMap(event -> {
                final EventDataBatch batch = currentBatch.get();
                if (batch.tryAdd(event)) {
                    return Mono.empty();
                }

                // The batch is full, so we create a new batch and send the batch. Mono.when completes when both operations
                // have completed.
                return Mono.when(
                        producer.send(batch),
                        producer.createBatch(options).map(newBatch -> {
                            currentBatch.set(newBatch);

                            // Add that event that we couldn't before.
                            if (!newBatch.tryAdd(event)) {
                                throw Exceptions.propagate(new IllegalArgumentException(String.format(
                                        "Event is too large for an empty batch. Max size: %s. Event: %s",
                                        newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                            }

                            return newBatch;
                        }));
            }).then()
                    .doFinally(signal -> {
                        final EventDataBatch batch = currentBatch.getAndSet(null);
                        if (batch != null) {
                            producer.send(batch).block(OPERATION_TIMEOUT);
                        }
                    })
                    .subscribe(unused -> System.out.println("Complete"),
                            error -> System.out.println("Error sending events: " + error),
                            () -> System.out.println("Completed sending events."));
            span.end();
        }
    }
}
```

<!-- Links -->
[azure_messaging_eventhubs]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
