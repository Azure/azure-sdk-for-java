// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Demonstrates how to publish events when there is a size constraint on batch size using
 * {@link CreateBatchOptions#setMaximumSizeInBytes(int)}.
 */
public class PublishEventsWithSizeLimitedBatches {
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
        telemetryEvents.subscribe(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (!batch.tryAdd(event)) {
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Adding that event we couldn't add before.
                    newBatch.tryAdd(event);
                    return producer.send(batch);
                }).block();
            }
        }, error -> System.err.println("Error received:" + error), () -> {
            final EventDataBatch batch = currentBatch.getAndSet(null);
            if (batch != null) {
                producer.send(batch).block();
            }
        });

        // Sleeping this thread because we want to wait for all the events to send before ending the program.
        // `.subscribe` is not a blocking call. It coordinates the callbacks and starts the send operation, but does not
        // wait for it to complete.
        // Customers can chain together Reactor operators like .then() and `.doFinally` if they want an operation to run
        // after all the events have been transmitted. .block() can also be used to turn the call into a synchronous
        // operation.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Disposing of our producer.
            producer.close();
        }
    }
}


