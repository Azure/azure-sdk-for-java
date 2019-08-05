// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.implementation.annotation.Immutable;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;

import java.time.Instant;
import java.util.Arrays;

/**
 * Holds information about Event Hubs which can come handy while performing data-plane operations like
 * {@link EventHubAsyncClient#createConsumer(String, String, EventPosition)} and
 * {@link EventHubAsyncClient#createConsumer(String, String, EventPosition, EventHubConsumerOptions)}.
 *
 * @see EventHubAsyncClient
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
    public String name() {
        return name;
    }

    /**
     * Gets the instant, in UTC, at which Event Hub was created at.
     *
     * @return The instant, in UTC, at which the Event Hub was created.
     */
    public Instant createdAt() {
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
