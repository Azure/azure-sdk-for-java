// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubProducerClient}.
 */
public class EventHubProducerClientJavaDocCodeSamples {
    private final EventHubClientBuilder builder = new EventHubClientBuilder()
        .connectionString("fake-string");
    /**
     * Code snippet demonstrating how to create an {@link EventHubProducerClient} that automatically routes events to any
     * partition.
     *
     */
    public void instantiate() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString("event-hubs-namespace-connection-string", "event-hub-name")
            .buildProducer();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events to a single partition.
     *
     * @throws IOException if the producer cannot be disposed.
     */
    public void instantiatePartitionProducer() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation#partitionId
        EventData eventData = new EventData("data-to-partition-foo");
        SendOptions options = new SendOptions()
            .setPartitionId("foo");

        EventHubProducerClient producer = builder.buildProducer();
        producer.send(eventData, options);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation#partitionId

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void sendEventsSendOptions() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.send#publisher-sendOptions
        final List<EventData> events = Arrays.asList(
            new EventData("sourdough".getBytes(UTF_8)),
            new EventData("rye".getBytes(UTF_8)),
            new EventData("wheat".getBytes(UTF_8))
        );

        final EventHubProducerClient producer = builder.buildProducer();
        final SendOptions options = new SendOptions()
            .setPartitionKey("bread");

        producer.send(events, options);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.send#publisher-sendOptions
    }

    /**
     * Code snippet demonstrating how to create an {@link EventDataBatch} and send it.
     */
    public void sendEventDataBatch() {
        final EventHubProducerClient producer = builder.buildProducer();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.send#eventDataBatch
        final List<EventData> telemetryEvents = Arrays.asList(
            new EventData("92".getBytes(UTF_8)).addProperty("telemetry", "latency"),
            new EventData("98".getBytes(UTF_8)).addProperty("telemetry", "cpu-temperature"),
            new EventData("120".getBytes(UTF_8)).addProperty("telemetry", "fps")
        );

        final BatchOptions options = new BatchOptions()
            .setPartitionKey("telemetry")
            .setMaximumSizeInBytes(256);

        EventDataBatch currentBatch = producer.createBatch(options);

        // For each telemetry event, we try to add it to the current batch.
        // When the batch is full, send it then create another batch to add more events to.
        for (EventData event : telemetryEvents) {
            if (!currentBatch.tryAdd(event)) {
                producer.send(currentBatch);
                currentBatch = producer.createBatch(options);
            }
        }
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.send#eventDataBatch
    }
}
