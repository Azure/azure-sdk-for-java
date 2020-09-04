// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import java.util.concurrent.Semaphore;

public final class PartitionPublishingState {
    private Short ownerLevel;
    private Long producerGroupId;
    private Integer sequenceNumber;

    // Idempotent producer requires all event data batches of a partition are sent out sequentially.
    private Semaphore sendingSemaphore = new Semaphore(1);

    public PartitionPublishingState() {
    }

    public PartitionPublishingState(Long producerGroupId, Short ownerLevel, Integer sequenceNumber) {
        this.ownerLevel = ownerLevel;
        this.producerGroupId = producerGroupId;
        this.sequenceNumber = sequenceNumber;
    }

    public Short getOwnerLevel() {
        return ownerLevel;
    }

    public Long getProducerGroupId() {
        return producerGroupId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setOwnerLevel(Short ownerLevel) { this.ownerLevel = ownerLevel; }

    public void setProducerGroupId(Long producerGroupId) { this.producerGroupId = producerGroupId; }

    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    Semaphore getSendingSemaphore() {
        return sendingSemaphore;
    }
}
