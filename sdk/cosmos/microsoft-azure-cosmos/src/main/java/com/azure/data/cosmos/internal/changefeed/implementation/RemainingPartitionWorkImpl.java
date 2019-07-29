// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.internal.changefeed.RemainingPartitionWork;

/**
 * Implements the {@link RemainingPartitionWork} interface.
 */
class RemainingPartitionWorkImpl implements RemainingPartitionWork {
    private final String partitionKeyRangeId;
    private final long remainingWork;

    /**
     * Initializes a new instance of the {@link RemainingPartitionWork} object.
     *
     * @param partitionKeyRangeId the partition key range ID for which the remaining work is calculated.
     * @param remainingWork the amount of documents remaining to be processed.
     */
    public RemainingPartitionWorkImpl(String partitionKeyRangeId, long remainingWork) {
        if (partitionKeyRangeId == null || partitionKeyRangeId.isEmpty()) throw new IllegalArgumentException("partitionKeyRangeId");

        this.partitionKeyRangeId = partitionKeyRangeId;
        this.remainingWork = remainingWork;
    }


    @Override
    public String getPartitionKeyRangeId() {
        return this.partitionKeyRangeId;
    }

    @Override
    public long getRemainingWork() {
        return this.remainingWork;
    }
}
