// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.BatchOptions;
import java.util.concurrent.atomic.AtomicReference;
import reactor.core.publisher.Flux;

import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to sent events to a specific event hub by defining partition ID in producer option only.
 */
public class PublishEventsWithCustomMetadata {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to send a custom event list to an Azure Event Hub instance.
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
            .buildAsyncProducer();

        // Because an event consists mainly of an opaque set of bytes, it may be difficult for consumers of those events
        // to make informed decisions about how to process them.
        //
        // In order to allow event publishers to offer better context for consumers, event data may also contain custom metadata,
        // in the form of a set of key/value pairs. This metadata is not used by, or in any way meaningful to, the Event Hubs
        // service; it exists only for coordination between event publishers and consumers.
        //
        // One common scenario for the inclusion of metadata is to provide a hint about the type of data contained by an event,
        // so that consumers understand its format and can deserialize it appropriately.
        //
        // We will publish two events based on simple sentences, but will attach some custom metadata with
        // pretend type names and other hints. Note that the set of metadata is unique to an event; there is no need for every
        // event in a batch to have the same metadata properties available nor the same data type for those properties.
        EventData firstEvent = new EventData("EventData Sample 1".getBytes(UTF_8));
        firstEvent.getProperties().put("EventType", "com.microsoft.samples.hello-event");
        firstEvent.getProperties().put("priority", 1);
        firstEvent.getProperties().put("score", 9.0);

        EventData secEvent = new EventData("EventData Sample 2".getBytes(UTF_8));
        secEvent.getProperties().put("EventType", "com.microsoft.samples.goodbye-event");
        secEvent.getProperties().put("priority", "17");
        secEvent.getProperties().put("blob", 10);

        final Flux<EventData> data = Flux.just(firstEvent, secEvent);

        // We want to send events to the a specific partition. For the sake of this sample, we take the first partition
        // identifier.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = producer.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

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
