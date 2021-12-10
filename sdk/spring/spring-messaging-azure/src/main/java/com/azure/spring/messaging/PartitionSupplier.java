// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging;

/**
 * Represents a supplier of partition id or key. When sending messages with specific partition information,
 * {@link PartitionSupplier} can be provided to specify the partition information of the messages to be sent.
 *
 */
public class PartitionSupplier {
    private String partitionKey;

    private String partitionId;

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
}
