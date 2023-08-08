// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.time.Instant;

/**
 * Holds information about Event Hubs which can come handy while performing data-plane operations
 * like {@link EventHubClient#createPartitionSender(String)} and {@link EventHubClient#createReceiver(String, String, EventPosition)}
 */
public final class EventHubRuntimeInformation {

    final String path;
    final Instant createdAt;
    final int partitionCount;
    final String[] partitionIds;

    public EventHubRuntimeInformation(
            final String path,
            final Instant createdAt,
            final int partitionCount,
            final String[] partitionIds) {
        this.path = path;
        this.createdAt = createdAt;
        this.partitionCount = partitionCount;
        this.partitionIds = partitionIds;
    }

    /**
     * Event Hub name
     *
     * @return name
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Time at which Event Hub was created at.
     *
     * @return created time
     */
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    /**
     * Number of partitions in the Event Hub.
     *
     * @return partition count
     */
    public int getPartitionCount() {
        return this.partitionCount;
    }

    /**
     * List of Partition identifiers of the Event Hub.
     *
     * @return partition identifiers
     */
    public String[] getPartitionIds() {
        return this.partitionIds;
    }
}
