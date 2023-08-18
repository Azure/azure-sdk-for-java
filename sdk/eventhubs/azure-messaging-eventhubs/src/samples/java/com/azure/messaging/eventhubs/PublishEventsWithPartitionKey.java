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
 * Send a Flux of events using a partition key.
 */
public class PublishEventsWithPartitionKey {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to send a list of events with partition key configured in
     * CreateBatchOptions to an Azure Event Hub instance.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Create a producer.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncProducerClient();

        // We will publish three events based on simple sentences.
        Flux<EventData> data = Flux.just(
            new EventData("Ball".getBytes(UTF_8)),
            new EventData("Net".getBytes(UTF_8)),
            new EventData("Players".getBytes(UTF_8)));

        // When an Event Hub producer is not associated with any specific partition, it may be desirable to request that
        // the Event Hubs service keep different batches of events together on the same partition. This can be
        // accomplished by setting a partition key when publishing the events.
        //
        // The partition key is NOT the identifier of a specific partition. Rather, it is an arbitrary piece of string
        // data that Event Hubs uses as the basis to compute a hash value. Event Hubs will associate the hash value with
        // a specific partition, ensuring that any events published with the same partition key are rerouted to the same
        // partition.
        //
        // All the event data with partition key 'basketball' end up in the same partition.
        //
        // Note that there is no means of accurately predicting which partition will be associated with a given
        // partition key; we can only be assured that it will be a consistent choice of partition. If you have a need to
        // understand which exact partition an event is published to, you will need to use
        // CreateBatchOptions.setPartitionId(String) when creating the EventDataBatch.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionKey("basketball");
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        final Mono<Void> sendOperation = data.flatMap(event -> {
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
