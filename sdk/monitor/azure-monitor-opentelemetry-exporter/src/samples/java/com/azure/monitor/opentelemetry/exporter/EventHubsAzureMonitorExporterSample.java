// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.OPERATION_TIMEOUT;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample to demontrate using {@link AzureMonitorTraceExporter} to export telemetry events when sending events to Event Hubs
 * using {@link EventHubProducerAsyncClient}.
 */
public class EventHubsAzureMonitorExporterSample {
    private static final Tracer TRACER = configureAzureMonitorExporter();
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    /**
     * The main method to run the application.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link AzureMonitorTraceExporter} to enable tracing.
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureAzureMonitorExporter() {
        AzureMonitorTraceExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildTraceExporter();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();

        return openTelemetrySdk.getTracer("Sample");
    }

    /**
     * Method that creates {@link EventHubProducerAsyncClient} to send events to Event Hubs with distributed
     * telemetry enabled and using Azure Monitor exporter to export telemetry events.
     */
    private static void doClientWork() {
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(CONNECTION_STRING, "<eventHub Name>")
            .buildAsyncProducerClient();

        Span span = TRACER.spanBuilder("user-parent-span").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            String firstPartition = producer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

            final byte[] body = "EventData Sample 1".getBytes(UTF_8);
            final byte[] body2 = "EventData Sample 2".getBytes(UTF_8);

            // We will publish three events based on simple sentences.
            Flux<EventData> data = Flux.just(
                new EventData(body).addContext(PARENT_SPAN_KEY, Span.current()),
                new EventData(body2).addContext(PARENT_SPAN_KEY, Span.current()));

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
        } finally {
            scope.close();
        }
    }
}
