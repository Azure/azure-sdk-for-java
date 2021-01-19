# Publish events with Azure Core Tracing OpenTelemetry

Following documentation describes instructions to run a sample program for publishing multiple events with tracing instrumentation.

## Getting Started
Sample uses **[opentelemetry-sdk][opentelemetry_sdk]** for implementation and **[Logging Exporter][logging_exporter]** as exporter.
### Adding dependencies to your project:
```xml
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>0.14.1</version>
</dependency>
<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporters-logging</artifactId>
    <version>0.14.1</version>
</dependency>
```

[//]: # ({x-version-update-start;com.azure:azure-messaging-eventhubs;current})
```xml
<!-- SDK dependencies   -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-messaging-eventhubs</artifactId>
    <version>5.3.1</version>
</dependency>
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-tracing-opentelemetry</artifactId>
    <version>1.0.0-beta.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

#### Sample demonstrates tracing when publishing multiple events to an eventhub instance using [azure-messaging-eventhubs][azure_messaging_eventhubs] client library.
```java
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.OPERATION_TIMEOUT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Sample {
    private static final Tracer TRACER = configureOpenTelemetryAndLoggingExporter();
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    public static void main(String[] args) {
        doClientWork();
    }

    private static Tracer configureOpenTelemetryAndLoggingExporter() {
        LoggingSpanExporter exporter = new LoggingSpanExporter();
        TracerSdkProvider tracerSdkProvider = OpenTelemetrySdk.getTracerProvider();
        tracerSdkProvider.addSpanProcessor(SimpleSpanProcessor.newBuilder(exporter).build());
        return tracerSdkProvider.get("Sample");
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
                    producer.createBatch(options).block(OPERATION_TIMEOUT));

            data.flatMap(event -> {
                final EventDataBatch batch = currentBatch.get();
                if (batch.tryAdd(event)) {
                    return Mono.empty();
                }

                // The batch is full, so we create a new batch and send the batch. Mono.when completes when both
                // operations
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
                            () -> {
                                System.out.println("Completed sending events.");
                                span.end();
                            });


            // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
            // the thread so the program does not end before the send operation is complete. Using .block() instead of
            // .subscribe() will turn this into a synchronous call.
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            } finally {
                // Disposing of our producer.
                producer.close();
            }
        }
    }
}
```

<!-- Links -->
[azure_messaging_eventhubs]: https://mvnrepository.com/artifact/com.azure/azure-messaging-eventhubs/
[opentelemetry_sdk]: https://github.com/open-telemetry/opentelemetry-java/tree/master/sdk
[logging_exporter]: https://github.com/open-telemetry/opentelemetry-java/tree/master/exporters/logging
