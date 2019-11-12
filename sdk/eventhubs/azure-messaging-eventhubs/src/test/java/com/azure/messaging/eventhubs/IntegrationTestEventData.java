// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.time.Instant;
import java.util.List;

/**
 * Contains information about test events pushed to Azure Event Hubs.*
 */
public class IntegrationTestEventData {
    private final String partitionId;
    private final List<EventData> events;
    private final String messageTrackingId;
    private final Instant enqueuedTime;

    IntegrationTestEventData(String partitionId, String messageTrackingId, Instant enqueuedTime,
            List<EventData> events) {
        this.partitionId = partitionId;
        this.events = events;
        this.messageTrackingId = messageTrackingId;
        this.enqueuedTime = enqueuedTime;
    }

    /**
     * The time at which the events were pushed to Event Hubs.
     *
     * @return The time when the test events were sent to Event Hubs.
     */
    public Instant getEnqueuedTime() {
        return enqueuedTime;
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
     * Gets the message identifier set in application property, {@link TestUtils#MESSAGE_TRACKING_ID}. Useful for
     * identifying that a set of events belong ot a test.
     *
     * @return The message identifier set in {@link EventData#getProperties()}.
     */
    public String getMessageTrackingId() {
        return messageTrackingId;
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
