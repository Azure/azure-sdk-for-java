// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Code snippets demonstrating various {@link EventHubConsumer} scenarios.
 */
public class EventHubConsumerJavaDocCodeSamples {
    private final EventHubClient client = new EventHubClientBuilder().connectionString("fake-string").buildClient();

    /**
     * Code snippet for creating an EventHubConsumer
     *
     * @throws IOException IO exception when the consumer cannot be disposed of.
     */
    public void instantiate() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumer.instantiation
        EventHubClient client = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .buildClient();

        String partitionId = "0";
        String consumerGroup = "$DEFAULT";
        EventHubConsumer consumer = client.createConsumer(consumerGroup, partitionId, EventPosition.latest());
        // END: com.azure.messaging.eventhubs.eventhubconsumer.instantiation

        consumer.close();
    }

    /**
     * Receives event data
     */
    public void receive() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumer.receive#int-duration
        // Obtain partitionId from EventHubClient.getPartitionIds().
        String partitionId = "0";
        Instant twelveHoursAgo = Instant.now().minus(Duration.ofHours(12));
        EventHubConsumer consumer = client.createConsumer(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME, partitionId,
            EventPosition.fromEnqueuedTime(twelveHoursAgo));

        IterableStream<EventData> events = consumer.receive(100, Duration.ofSeconds(30));

        for (EventData event : events) {
            // For each event, perform some sort of processing.
            System.out.print("Event received: " + event.getSequenceNumber());
        }

        // Gets the next set of events to consume and process.
        IterableStream<EventData> nextEvents = consumer.receive(100, Duration.ofSeconds(30));
        // END: com.azure.messaging.eventhubs.eventhubconsumer.receive#int-duration

        for (EventData event : nextEvents) {
            // For each event, perform some sort of processing.
            System.out.print("Event received: " + event.getSequenceNumber());
        }
    }
}
