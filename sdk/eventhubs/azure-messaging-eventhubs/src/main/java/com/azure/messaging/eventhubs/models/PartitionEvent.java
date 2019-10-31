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

    /**
     * Creates an instance of PartitionEvent
     *
     * @param partitionContext The partition information associated with the event data.
     * @param eventData The event data received from the Event Hub.
     */
    public PartitionEvent(final PartitionContext partitionContext, final EventData eventData) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null");;
        this.eventData = Objects.requireNonNull(eventData, "'eventData' cannot be null");;
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
     * Returns the event data received from the Event Hub.
     *
     * @return The event data received from the Event Hub.
     */
    public EventData getEventData() {
        return eventData;
    }
}
