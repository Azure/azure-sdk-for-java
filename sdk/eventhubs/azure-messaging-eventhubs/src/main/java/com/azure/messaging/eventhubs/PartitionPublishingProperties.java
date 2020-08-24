// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.annotation.Immutable;

/**
 * The partition publishing properties of an {@link EventHubProducerClient} or {@link EventHubProducerAsyncClient}.
 */
@Immutable
public final class PartitionPublishingProperties {
    private final Short ownerLevel;
    private final Long producerGroupId;
    private final Integer lastSequenceNumber;

    PartitionPublishingProperties(Long producerGroupId, Short ownerLevel, Integer lastSequenceNumber) {
        this.ownerLevel = ownerLevel;
        this.producerGroupId = producerGroupId;
        this.lastSequenceNumber = lastSequenceNumber;
    }

    /**
     * @see PartitionPublishingOptions#getOwnerLevel()
     * @return The owner level of this producer for publishing to the associated partition.
     * {@link null} if the producer is not idempotent.
     */
    public Short getOwnerLevel() {
        return ownerLevel;
    }

    /**
     * @see PartitionPublishingOptions#getProducerGroupId()
     * @return The identifier of the producer group for which this producer is publishing to the associated partition.
     * {@link null} if the producer is not idempotent.
     */
    public Long getProducerGroupId() {
        return producerGroupId;
    }

    /**
     *
     * @see PartitionPublishingOptions#getStartingSequenceNumber()
     * @return The sequence number assigned to the event that was most recently published to the associated partition
     * successfully.
     * {@link null} if the producer is not idempotent.
     */
    public Integer getLastSequenceNumber() {
        return lastSequenceNumber;
    }
}
