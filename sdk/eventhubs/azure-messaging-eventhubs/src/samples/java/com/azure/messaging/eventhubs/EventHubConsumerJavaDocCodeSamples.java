// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.PartitionEvent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Code snippets demonstrating various {@link EventHubConsumerClient} scenarios.
 */
public class EventHubConsumerJavaDocCodeSamples {
    private final EventHubClient client = new EventHubClientBuilder().connectionString("fake-string").buildClient();

    /**
     * Code snippet for creating an EventHubConsumer
     *
     * @throws IOException IO exception when the consumer cannot be disposed of.
     */
    public void instantiate() throws IOException {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation
        String consumerGroup = "$DEFAULT";

        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup(consumerGroup)
            .startingPosition(EventPosition.latest())
            .buildConsumer();
        // END: com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation

        consumer.close();
    }

    /**
     * Receives event data
     */
    public void receive() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerclient.receive#int-duration
        // Obtain partitionId from EventHubClient.getPartitionIds().
        String partitionId = "0";
        Instant twelveHoursAgo = Instant.now().minus(Duration.ofHours(12));
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .startingPosition(EventPosition.fromEnqueuedTime(twelveHoursAgo))
            .buildConsumer();

        IterableStream<PartitionEvent> events = consumer.receive(partitionId, 100, Duration.ofSeconds(30));

        for (PartitionEvent partitionEvent : events) {
            // For each event, perform some sort of processing.
            System.out.print("Event received: " + partitionEvent.getEventData().getSequenceNumber());
        }

        // Gets the next set of events to consume and process.
        IterableStream<PartitionEvent> nextEvents = consumer.receive(partitionId, 100, Duration.ofSeconds(30));
        // END: com.azure.messaging.eventhubs.eventhubconsumerclient.receive#int-duration

        for (PartitionEvent partitionEvent : nextEvents) {
            // For each event, perform some sort of processing.
            System.out.print("Event received: " + partitionEvent.getEventData().getSequenceNumber());
        }
    }
}
