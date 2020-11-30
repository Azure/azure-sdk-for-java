// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.util.List;

/**
 * Contains information about test events pushed to Azure Event Hubs.
 */
public class IntegrationTestEventData {
    private final String partitionId;
    private final PartitionProperties partitionProperties;
    private final List<EventData> events;
    private final String messageId;

    IntegrationTestEventData(String partitionId, PartitionProperties partitionProperties, String messageId,
        List<EventData> events) {
        this.partitionId = partitionId;
        this.partitionProperties = partitionProperties;
        this.events = events;
        this.messageId = messageId;
    }

    /**
     * Gets the last enqueued sequence number to the partition.
     *
     * @return The last enqueued sequence number to the partition.
     */
    public PartitionProperties getPartitionProperties() {
        return partitionProperties;
    }

    /**
     * Gets the partition id these events were pushed to.
     *
     * @return Gets the partition id these events were pushed to. {@code null} if they were sent round-robin.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the message identifier set in application property, {@link TestUtils#MESSAGE_ID}. Useful for
     * identifying that a set of events belong ot a test.
     *
     * @return The message identifier set in {@link EventData#getProperties()}.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the events that were pushed to the service.
     *
     * @return Events that were pushed to the service.
     */
    public List<EventData> getEvents() {
        return events;
    }
}
