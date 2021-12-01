// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

/**
 * Provide partition id or key
 *
 * @author Warren Zhu
 */
public class PartitionSupplier {
    private String partitionKey;

    private String partitionId;

    /**
     *
     * @return The partition key.
     */
    public String getPartitionKey() {
        return partitionKey;
    }

    /**
     *
     * @param partitionKey The partitionKey.
     */
    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     *
     * @return The partitionId.
     */
    public String getPartitionId() {
        return partitionId;
    }

    /**
     *
     * @param partitionId The partitionId.
     */
    public void setPartitionId(String partitionId) {
        this.partitionId = partitionId;
    }
}
