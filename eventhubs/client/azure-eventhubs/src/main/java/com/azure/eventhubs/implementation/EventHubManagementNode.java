// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.eventhubs.EventHubProperties;
import com.azure.eventhubs.PartitionProperties;
import reactor.core.publisher.Mono;

import java.io.Closeable;

/**
 * The management node for fetching metadata about the Event Hub and its partitions.
 */
public interface EventHubManagementNode extends Closeable {
    /**
     * Gets the metadata associated with the Event Hub.
     *
     * @return Metadata associated with the Event Hub.
     */
    Mono<EventHubProperties> getEventHubProperties();

    /**
     * Gets the metadata associated with a particular partition in the Event Hub.
     *
     * @param partitionId The identifier of the partition.
     * @return The metadata associated with the partition.
     */
    Mono<PartitionProperties> getPartitionProperties(String partitionId);
}
