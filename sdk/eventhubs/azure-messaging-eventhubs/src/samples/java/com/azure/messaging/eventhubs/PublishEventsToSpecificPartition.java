// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.BatchOptions;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to sent events to specific event hub by define partition ID in producer option only.
 */
public class PublishEventsToSpecificPartition {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to send a batch of events with partition ID configured in producer
     * option to an Azure Event Hub instance.
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

        // Instantiate a client that will be used to call the service.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncProducer();

        // To send our events, we need to know what partition to send it to. For the sake of this example, we take the
        // first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = producer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // We will publish three events based on simple sentences.
        Flux<EventData> data = Flux.just(
            new EventData("EventData Sample 1".getBytes(UTF_8)),
            new EventData("EventData Sample 2".getBytes(UTF_8)),
            new EventData("EventData Sample 3".getBytes(UTF_8)));

        // Create a batch to send the events.
        final BatchOptions options = new BatchOptions()
            .setPartitionId(firstPartition)
            .setMaximumSizeInBytes(256);
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        data.subscribe(event -> {
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

                // Disposing of our producer.
                producer.close();
            });
    }
}
