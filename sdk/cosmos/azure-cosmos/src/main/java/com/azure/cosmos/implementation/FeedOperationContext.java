// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.Set;

public class FeedOperationContext {

    private final Set<PartitionKeyRange> partitionKeyRangesWithSuccess;

    private final boolean isThresholdBasedAvailabilityStrategyEnabled;

    private boolean isRequestHedged;

    public FeedOperationContext(Set<PartitionKeyRange> partitionKeyRangesWithSuccess, boolean isThresholdBasedAvailabilityStrategyEnabled) {
        this.partitionKeyRangesWithSuccess = partitionKeyRangesWithSuccess;
        this.isThresholdBasedAvailabilityStrategyEnabled = isThresholdBasedAvailabilityStrategyEnabled;
    }

    public void setIsRequestHedged(boolean isRequestHedged) {
        this.isRequestHedged = isRequestHedged;
    }

    public boolean getIsRequestHedged() {
        return this.isRequestHedged;
    }

    public void addPartitionKeyRangeWithSuccess(PartitionKeyRange partitionKeyRange) {
        this.partitionKeyRangesWithSuccess.add(partitionKeyRange);
    }

    public boolean hasPartitionKeyRangeSeenSuccess(PartitionKeyRange partitionKeyRange) {
        return this.partitionKeyRangesWithSuccess.contains(partitionKeyRange);
    }

    public boolean isThresholdBasedAvailabilityStrategyEnabled() {
        return isThresholdBasedAvailabilityStrategyEnabled;
    }
}
