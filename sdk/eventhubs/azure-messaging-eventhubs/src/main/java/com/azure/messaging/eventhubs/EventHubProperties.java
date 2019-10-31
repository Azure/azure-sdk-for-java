// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;

import java.time.Instant;
import java.util.Arrays;

/**
 * Holds information about an Event Hub which can come handy while performing operations like
 * {@link EventHubConsumerAsyncClient#receive(String) receiving events from a specific partition}.
 *
 * @see EventHubConsumerAsyncClient
 * @see EventHubConsumerClient
 */
@Immutable
public final class EventHubProperties {
    private final String name;
    private final Instant createdAt;
    private final String[] partitionIds;

    EventHubProperties(
        final String name,
        final Instant createdAt,
        final String[] partitionIds) {
        this.name = name;
        this.createdAt = createdAt;
        this.partitionIds = partitionIds != null
            ? Arrays.copyOf(partitionIds, partitionIds.length)
            : new String[0];
    }

    /**
     * Gets the Event Hub name
     *
     * @return Name of the Event Hub.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the instant, in UTC, at which Event Hub was created at.
     *
     * @return The instant, in UTC, at which the Event Hub was created.
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Gets the list of partition identifiers of the Event Hub.
     *
     * @return The list of partition identifiers of the Event Hub.
     */
    public String[] getPartitionIds() {
        return partitionIds;
    }
}
