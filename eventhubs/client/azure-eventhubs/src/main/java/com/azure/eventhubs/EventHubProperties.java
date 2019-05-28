// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import java.time.Instant;
import java.util.Arrays;

/**
 * Holds information about Event Hubs which can come handy while performing data-plane operations like
 * {@link EventHubClient#createReceiver(String)} and {@link EventHubClient#createReceiver(String, ReceiverOptions)}.
 */
public final class EventHubProperties {
    private final String path;
    private final Instant createdAt;
    private final String[] partitionIds;
    private Instant propertyRetrievalTimeUtc;

    EventHubProperties(
            final String path,
            final Instant createdAtUtc,
            final String[] partitionIds,
            final Instant propertyRetrievalTimeUtc) {
        this.path = path;
        this.createdAt = createdAtUtc;
        this.partitionIds = partitionIds != null
            ? Arrays.copyOf(partitionIds, partitionIds.length)
            : new String[0];
        this.propertyRetrievalTimeUtc = propertyRetrievalTimeUtc;
    }

    /**
     * Gets the Event Hub name
     *
     * @return Name of the Event Hub.
     */
    public String path() {
        return path;
    }

    /**
     * Gets the time at which Event Hub was created at.
     *
     * @return The time at which the Event Hub was created.
     */
    public Instant createdAtUtc() {
        return createdAt;
    }

    /**
     * Gets the list of partition identifiers of the Event Hub.
     *
     * @return The list of partition identifiers of the Event Hub.
     */
    public String[] partitionIds() {
        return partitionIds;
    }
}
