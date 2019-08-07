// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.models;

import com.azure.core.implementation.annotation.Immutable;
import com.azure.messaging.eventhubs.PartitionProcessor;
import java.util.Objects;

/**
 * A model class to contain partition information that will be provided to each instance of {@link PartitionProcessor}.
 */
@Immutable
public class PartitionContext {

    private String partitionId;
    private String eventHubName;
    private String consumerGroupName;

    /**
     * Creates an immutable instance containing the information for processing a partition.
     *
     * @param partitionId The partition id.
     * @param eventHubName The Event Hub name.
     * @param consumerGroupName The consumer group name.
     */
    public PartitionContext(String partitionId, String eventHubName, String consumerGroupName) {
        this.partitionId = Objects.requireNonNull(partitionId, "partitionId cannot be null");
        this.eventHubName = Objects.requireNonNull(eventHubName, "eventHubName cannot be null");
        this.consumerGroupName = Objects.requireNonNull(consumerGroupName, "consumerGroupName cannot be null");
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
