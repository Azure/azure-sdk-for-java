// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send an {@link EventDataBatch} to an Azure Event Hub.
 */
public class PublishEventDataBatch {
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
        final String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Create a producer.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncProducer();

        // Creating a batch where we want the events ending up in the same partition by setting the partition key.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionKey("sandwiches")
            .setMaximumSizeInBytes(256);
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

                // Disposing of our producer.
                producer.close();
            });
    }
}
