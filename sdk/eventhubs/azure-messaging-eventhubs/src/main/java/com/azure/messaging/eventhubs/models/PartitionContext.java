// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.annotation.Immutable;

/**
 * A model class to contain partition information of an Event Hub.
 */
@Immutable
public class PartitionContext {

    private final String fullyQualifiedNamespace;
    private final String partitionId;
    private final String eventHubName;
    private final String consumerGroup;

    /**
     * Creates an instance of PartitionContext that contains partition information.
     *
     * @param fullyQualifiedNamespace The fully qualified namespace of the Event Hub.
     * @param eventHubName The Event Hub name.
     * @param consumerGroup The consumer group name associated with the consumer of an Event Hub.
     * @param partitionId The partition id of the partition.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code partitionId}, {@code eventHubName} or
     * {@code consumerGroup} or is {@code null}.
     */
    public PartitionContext(String fullyQualifiedNamespace, String eventHubName, String consumerGroup,
        String partitionId) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.consumerGroup = consumerGroup;
        this.partitionId = partitionId;
    }

    /**
     * Returns the fully qualified namespace of the Event Hub.
     *
     * @return the fully qualified namespace of the Event Hub.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the partition id of the Event Hub.
     *
     * @return the partition id of the Event Hub.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Gets the Event Hub name.
     *
     * @return The Event Hub name.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Gets the consumer group name associated with the consumer of an Event Hub.
     *
     * @return The consumer group name associated with the consumer of an Event Hub.
     */
    public String getConsumerGroup() {
        return consumerGroup;
    }
}
