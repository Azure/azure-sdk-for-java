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
     * @param instanceId The instance identifier
     * @param partitionId The partition id
     * @param eventHubName The event hub name
     * @param consumerGroupName The consumer group name
     */
    public PartitionContext(String instanceId, String partitionId, String eventHubName,
        String consumerGroupName) {
        this.instanceId = instanceId;
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
     * Gets the instance identifier.
     *
     * @return The instance identifier.
     */
    public String instanceId() {
        return this.instanceId;
    }

    /**
     * Gets the event hub name.
     *
     * @return The event hub name.
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
