// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.EventData;

import java.util.Objects;

/**
 * A container for {@link EventData} along with the partition information for this event data.
 */
@Immutable
public class PartitionEvent {

    private final PartitionContext partitionContext;
    private final EventData eventData;
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties;

    /**
     * Creates an instance of PartitionEvent.
     *
     * @param partitionContext The partition information associated with the event data.
     * @param eventData The event data received from the Event Hub.
     * @param lastEnqueuedEventProperties The properties of the last enqueued event in the partition.
     * @throws NullPointerException if {@code partitionContext} or {@code eventData} is {@code null}.
     */
    public PartitionEvent(final PartitionContext partitionContext, final EventData eventData,
        LastEnqueuedEventProperties lastEnqueuedEventProperties) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");
        this.eventData = Objects.requireNonNull(eventData, "'eventData' cannot be null");
        this.lastEnqueuedEventProperties = lastEnqueuedEventProperties;
    }

    /**
     * Returns the partition information associated with the event data.
     *
     * @return The partition information associated with the event data.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Gets the event received from the partition.
     *
     * @return Event received from the partition.
     */
    public EventData getData() {
        return eventData;
    }

    /**
     * Gets the properties of the last enqueued event in this partition.
     *
     * @return The properties of the last enqueued event in this partition.
     */
    public LastEnqueuedEventProperties getLastEnqueuedEventProperties() {
        return lastEnqueuedEventProperties;
    }
}
