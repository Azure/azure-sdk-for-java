// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import java.time.Instant;
import java.util.Arrays;

/**
 * Holds information about Event Hubs which can come handy while performing data-plane operations
 * like {@link EventHubClient#createReceiver(String, EventPosition)} and
 * {@link EventHubClient#createReceiver(ReceiverOptions)}.
 */
public final class EventHubRuntimeInformation {
    private final String path;
    private final Instant createdAt;
    private final int partitionCount;
    private final String[] partitionIds;

    EventHubRuntimeInformation(
            final String path,
            final Instant createdAt,
            final int partitionCount,
            final String[] partitionIds) {
        this.path = path;
        this.createdAt = createdAt;
        this.partitionCount = partitionCount;
        this.partitionIds = partitionIds != null
            ? Arrays.copyOf(partitionIds, partitionIds.length)
            : new String[0];
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
    public Instant createdAt() {
        return createdAt;
    }

    /**
     * Gets the number of partitions in the Event Hub.
     *
     * @return The number of partitions in the Event Hub.
     */
    public int partitionCount() {
        return partitionCount;
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
