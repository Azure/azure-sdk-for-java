// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

/**
 * Represents a supplier of partition id or key. When sending messages with specific partition information,
 * {@link PartitionSupplier} can be provided to specify the partition information of the messages to be sent.
 *
 */
public class PartitionSupplier {

    /**
     * Create an instance of {@link PartitionSupplier}.
     */
    public PartitionSupplier() {
    }

    private String partitionKey;

    private String partitionId;

    /**
     * Get the partition key.
     * @return the partition key.
     */
    public String getPartitionKey() {
        return partitionKey;
    }

    /**
     * Set the partition key.
     * @param partitionKey the partition key.
     */
    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Get the partition id.
     * @return the partition id.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     * Set the partition id.
     * @param partitionId the partition id.
     */
    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
}
