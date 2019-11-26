// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Demonstrates how to publish events when there is a size constraint on batch size using
 * {@link CreateBatchOptions#setMaximumSizeInBytes(int)}.
 */
public class PublishEventsWithSizeLimitedBatches {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo on how to send an {@link EventDataBatch} to an Azure Event Hub.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        Flux<EventData> telemetryEvents = Flux.just(
            new EventData("Roast beef".getBytes(UTF_8)),
            new EventData("Cheese".getBytes(UTF_8)),
            new EventData("Tofu".getBytes(UTF_8)),
            new EventData("Turkey".getBytes(UTF_8)));

        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Instantiate a client that will be used to call the service.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncProducerClient();

        // In cases where developers need to size limit their batch size, they can use `setMaximumSizeInBytes` to limit
        // the size of their EventDataBatch. By default, it will be the max size allowed by the underlying link.
        // Since there is no partition id or partition key set, the Event Hubs service will automatically load balance
        // the events between all available partitions.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setMaximumSizeInBytes(256);
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // The sample Flux contains three events, but it could be an infinite stream of telemetry events.
        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        final Mono<Void> sendOperation = telemetryEvents.flatMap(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (batch.tryAdd(event)) {
                return Mono.empty();
            }

            // The batch is full, so we create a new batch and send the batch. Mono.when completes when both operations
            // have completed.
            return Mono.when(
                producer.send(batch),
                producer.createBatch().map(newBatch -> {
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
            });

        // The sendOperation creation and assignment is not a blocking call. It does not get invoked until there is a
        // subscriber to that operation. For the purpose of this example, we block so the program does not end before
        // the send operation is complete. Any of the `.subscribe` overloads also work to start the Mono asynchronously.
        try {
            sendOperation.block(OPERATION_TIMEOUT);
        } finally {
            // Disposing of our producer.
            producer.close();
        }
    }
}


