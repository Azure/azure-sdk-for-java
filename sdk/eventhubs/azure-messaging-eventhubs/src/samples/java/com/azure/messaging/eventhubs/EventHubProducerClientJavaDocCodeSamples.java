// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;

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
     * Code snippet demonstrating how to create an {@link EventHubProducerClient}.
     */
    public void instantiate() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString("event-hubs-namespace-connection-string", "event-hub-name")
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events to a single partition.
     *
     */
    public void instantiatePartitionProducer() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation#partitionId
        CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionId("foo");

        EventHubProducerClient producer = builder.buildProducerClient();
        EventDataBatch batch = producer.createBatch(options);
        batch.tryAdd(new EventData("data-to-partition-foo"));
        producer.send(batch);
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

        final EventHubProducerClient producer = builder.buildProducerClient();
        final SendOptions options = new SendOptions()
            .setPartitionKey("bread");

        producer.send(events, options);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.send#publisher-sendOptions
    }

    /**
     * Code snippet demonstrating how to create an {@link EventDataBatch} and send it.
     */
    public void sendEventDataBatch() {
        final EventHubProducerClient producer = builder.buildProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.send#eventDataBatch
        final EventData firstEvent = new EventData("92".getBytes(UTF_8));
        firstEvent.getProperties().put("telemetry", "latency");

        final EventData secondEvent = new EventData("98".getBytes(UTF_8));
        secondEvent.getProperties().put("telemetry", "cpu-temperature");

        final EventData thirdEvent = new EventData("120".getBytes(UTF_8));
        thirdEvent.getProperties().put("telemetry", "fps");

        final List<EventData> telemetryEvents = Arrays.asList(firstEvent, secondEvent, thirdEvent);
        final CreateBatchOptions options = new CreateBatchOptions()
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
