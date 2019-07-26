// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.eventprocessor.models;

import com.azure.core.implementation.annotation.Fluent;

/**
 * A model class to contain partition information
 */
@Fluent
public class PartitionContext {
    private String instanceId;
    private String partitionId;
    private String eventHubName;
    private String consumerGroupName;

    /**
     * Gets the partition id
     * @return The partition id
     */
    public String partitionId() {
        return partitionId;
    }

    /**
     * Sets the partition id
     * @param partitionId The partition id
     * @return The updated {@link PartitionContext} instance
     */
    public PartitionContext partitionId(String partitionId) {
        this.partitionId = partitionId;
        return this;
    }

    /**
     * Sets the instance identifier
     * @param instanceId The instance identifier
     * @return The updated {@link PartitionContext} instance
     */
    public PartitionContext instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Gets the instance identifier
     * @return The instance identifier
     */
    public String instanceId() {
        return this.instanceId;
    }

    /**
     * Gets the event hub name
     * @return The event hub name
     */
    public String eventHubName() {
        return eventHubName;
    }

    /**
     * Sets the event hub name
     * @param eventHubName The event hub name
     * @return The updated {@link PartitionContext} instance
     */
    public PartitionContext eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    /**
     * Gets the consumer group name
     * @return The consumer group name
     */
    public String consumerGroupName() {
        return consumerGroupName;
    }

    /**
     * Sets the consumer group name
     * @param consumerGroupName The consumer group name
     * @return The updated {@link PartitionContext} instance
     */
    public PartitionContext consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        return this;
    }
}
