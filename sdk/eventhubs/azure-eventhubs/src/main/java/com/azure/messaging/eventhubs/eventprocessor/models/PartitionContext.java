// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Immutable;

/**
 * A model class to contain partition information.
 */
@Immutable
public class PartitionContext {

    private String instanceId;
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
     * Gets the partition id.
     *
     * @return The partition id.
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Gets the Event Hub name.
     *
     * @return The Event Hub name.
     */
    public String eventHubName() {
        return eventHubName;
    }

    /**
     * Gets the consumer group name.
     *
     * @return The consumer group name.
     */
    public String consumerGroupName() {
        return consumerGroupName;
    }
}
