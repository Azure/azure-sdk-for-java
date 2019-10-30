// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;
import com.azure.messaging.eventhubs.EventData;

import java.util.Objects;

/**
 * An event received within the context of an Event Hub partition.
 */
@Immutable
public class PartitionEvent {
    private final PartitionContext partitionContext;
    private final EventData eventData;

    /**
     * Creates a new instance with the context and event data set.
     *
     * @param partitionContext Information about the partition the event was in.
     * @param eventData Event received from the partition.
     * @throws NullPointerException if {@code partitionContext} or {@code eventData} is null.
     */
    public PartitionEvent(PartitionContext partitionContext, EventData eventData) {
        this.partitionContext = Objects.requireNonNull(partitionContext, "'partitionContext' cannot be null.");
        this.eventData = Objects.requireNonNull(eventData, "'eventData' cannot be null.");;
    }

    /**
     * Gets information about the partition the received event was in.
     *
     * @return Information about the partition the received event was in.
     */
    public PartitionContext getPartitionContext() {
        return partitionContext;
    }

    /**
     * Gets the event received from the partition.
     *
     * @return Event received from the partition.
     */
    public EventData getEventData() {
        return eventData;
    }
}
