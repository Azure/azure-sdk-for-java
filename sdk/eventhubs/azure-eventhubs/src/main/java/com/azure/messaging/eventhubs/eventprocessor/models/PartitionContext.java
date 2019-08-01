// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Immutable;
import com.azure.messaging.eventhubs.PartitionProcessor;

/**
 * A model class to contain partition information.
 */
@Immutable
public class PartitionContext {

    private String partitionId;
    private String eventHubName;
    private String consumerGroupName;

    /**
     * Creates an immutable instance containing the information for processing a partition
     *
     * @param partitionId The partition id
     * @param eventHubName The event hub name
     * @param consumerGroupName The consumer group name
     */
    public PartitionContext(String partitionId, String eventHubName, String consumerGroupName) {
        this.partitionId = partitionId;
        this.eventHubName = eventHubName;
        this.consumerGroupName = consumerGroupName;
    }

    /**
     * Gets the partition id associated to an instance of {@link PartitionProcessor}.
     *
     * @return The partition id associated to an instance of {@link PartitionProcessor}.
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Gets the Event Hub name associated to an instance of {@link PartitionProcessor}.
     *
     * @return The Event Hub name associated to an instance of {@link PartitionProcessor}.
     */
    public String eventHubName() {
        return eventHubName;
    }

    /**
     * Gets the consumer group name associated to an instance of {@link PartitionProcessor}.
     *
     * @return The consumer group name associated to an instance of {@link PartitionProcessor}.
     */
    public String consumerGroupName() {
        return consumerGroupName;
    }
}
