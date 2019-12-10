// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;

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
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send a batch that automatically routes events to any partition.
     *
     * @throws IllegalArgumentException if an event is too large for an empty batch.
     */
    public void batchAutomaticRouting() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildProducerClient();
        List<EventData> events = Arrays.asList(new EventData("test-event-1"), new EventData("test-event-2"));

        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        EventDataBatch batch = producer.createBatch();
        for (EventData event : events) {
            if (batch.tryAdd(event)) {
                continue;
            }

            producer.send(batch);
            batch = producer.createBatch();
            if (!batch.tryAdd(event)) {
                throw new IllegalArgumentException("Event is too large for an empty batch.");
            }
        }
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch

        producer.close();
    }

    /**
     * Code snippet demonstrating how to create an EventDataBatch at routes events to a single partition.
     */
    public void batchPartitionId() {
        final EventHubProducerClient producer = builder.buildProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId
        // Creating a batch with partitionId set will route all events in that batch to partition `foo`.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionId("foo");

        EventDataBatch batch = producer.createBatch(options);
        batch.tryAdd(new EventData("data-to-partition-foo"));
        producer.send(batch);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void batchPartitionKey() {
        final EventHubProducerClient producer = builder.buildProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey
        List<EventData> events = Arrays.asList(new EventData("sourdough"), new EventData("rye"),
            new EventData("wheat"));

        // Creating a batch with partitionKey set will tell the service to hash the partitionKey and decide which
        // partition to send the events to. Events with the same partitionKey are always routed to the same partition.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("bread");
        EventDataBatch batch = producer.createBatch(options);

        events.forEach(event -> batch.tryAdd(event));
        producer.send(batch);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey
    }

    /**
     * Code snippet demonstrating how to create a size-limited {@link EventDataBatch} and send it.
     *
     * @throws IllegalArgumentException if an event is too large for an empty batch.
     */
    public void batchSizeLimited() {
        final EventHubProducerClient producer = builder.buildProducerClient();
        final EventData firstEvent = new EventData("92".getBytes(UTF_8));
        firstEvent.getProperties().put("telemetry", "latency");
        final EventData secondEvent = new EventData("98".getBytes(UTF_8));
        secondEvent.getProperties().put("telemetry", "cpu-temperature");
        final EventData thirdEvent = new EventData("120".getBytes(UTF_8));
        thirdEvent.getProperties().put("telemetry", "fps");

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int


        final List<EventData> telemetryEvents = Arrays.asList(firstEvent, secondEvent, thirdEvent);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setMaximumSizeInBytes(256);

        EventDataBatch currentBatch = producer.createBatch(options);

        // For each telemetry event, we try to add it to the current batch.
        // When the batch is full, send it then create another batch to add more events to.
        for (EventData event : telemetryEvents) {
            if (!currentBatch.tryAdd(event)) {
                producer.send(currentBatch);
                currentBatch = producer.createBatch(options);

                // Add the event we couldn't before.
                if (!currentBatch.tryAdd(event)) {
                    throw new IllegalArgumentException("Event is too large for an empty batch.");
                }
            }
        }
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int
    }
}
