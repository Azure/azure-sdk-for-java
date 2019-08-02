// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubProducer}.
 */
public class EventHubProducerJavaDocCodeSamples {
    private final EventHubAsyncClient client = new EventHubClientBuilder().connectionString("fake-string").buildAsyncClient();

    /**
     * Code snippet demonstrating how to create an EventHubProducer that automatically routes events to any partition.
     *
     * @throws IOException if the producer cannot be disposed.
     */
    public void instantiate() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.instantiate
        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString("event-hubs-namespace-connection-string", "event-hub-name")
            .buildAsyncClient();

        EventHubProducer producer = client.createProducer();
        // END: com.azure.messaging.eventhubs.eventhubproducer.instantiate

        producer.close();
    }

    /**
     * Code snippet demonstrating how to create an EventHubProducer that routes events to a single partition.
     *
     * @throws IOException if the producer cannot be disposed.
     */
    public void instantiatePartitionProducer() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.instantiatePartitionProducer
        RetryOptions retryOptions = new RetryOptions()
            .tryTimeout(Duration.ofSeconds(45));
        EventHubProducerOptions options = new EventHubProducerOptions()
            .partitionId("foo")
            .retry(retryOptions);

        EventHubProducer producer = client.createProducer(options);
        // END: com.azure.messaging.eventhubs.eventhubproducer.instantiatePartitionProducer

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void sendEventsFluxSendOptions() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.send#publisher-sendOptions
        Flux<EventData> events = Flux.just(
            new EventData("sourdough".getBytes(UTF_8)),
            new EventData("rye".getBytes(UTF_8)),
            new EventData("wheat".getBytes(UTF_8))
        );

        EventHubProducer producer = client.createProducer();
        SendOptions options = new SendOptions()
            .partitionKey("bread");

        producer.send(events, options).subscribe(ignored -> System.out.println("sent"),
            error -> System.err.println("Error received:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubproducer.send#publisher-sendOptions
    }

    /**
     * Code snippet demonstrating how to create an {@link EventDataBatch} and send it.
     */
    public void sendEventDataBatch() {
        final EventHubProducer producer = client.createProducer();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducer.send#eventdatabatch
        final Flux<EventData> telemetryEvents = Flux.just(
            new EventData("92".getBytes(UTF_8)).addProperty("telemetry", "latency"),
            new EventData("98".getBytes(UTF_8)).addProperty("telemetry", "cpu-temperature"),
            new EventData("120".getBytes(UTF_8)).addProperty("telemetry", "fps")
        );

        final BatchOptions options = new BatchOptions()
            .partitionKey("telemetry")
            .maximumSizeInBytes(256);
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // The sample Flux contains three events, but it could be an infinite stream of telemetry events.
        telemetryEvents.subscribe(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (!batch.tryAdd(event)) {
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);
                    return producer.send(batch);
                }).block();
            }
        }, error -> System.err.println("Error received:" + error),
            () -> {
                final EventDataBatch batch = currentBatch.getAndSet(null);
                if (batch != null) {
                    producer.send(batch).block();
                }
            });
        // END: com.azure.messaging.eventhubs.eventhubproducer.send#eventdatabatch
    }
}
