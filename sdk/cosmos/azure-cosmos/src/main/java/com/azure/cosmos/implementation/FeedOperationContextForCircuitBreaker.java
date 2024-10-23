// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.Map;

public class FeedOperationContextForCircuitBreaker {

    private final Map<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionKeyRangesWithSuccess;
    private final boolean isThresholdBasedAvailabilityStrategyEnabled;
    private final String collectionLink;
    private boolean isRequestHedged;

    public FeedOperationContextForCircuitBreaker(
        Map<PartitionKeyRangeWrapper, PartitionKeyRangeWrapper> partitionKeyRangesWithSuccess,
        boolean isThresholdBasedAvailabilityStrategyEnabled,
        String collectionLink) {

        this.partitionKeyRangesWithSuccess = partitionKeyRangesWithSuccess;
        this.isThresholdBasedAvailabilityStrategyEnabled = isThresholdBasedAvailabilityStrategyEnabled;
        this.collectionLink = collectionLink;
    }

    public void setIsRequestHedged(boolean isRequestHedged) {
        this.isRequestHedged = isRequestHedged;
    }

    public boolean getIsRequestHedged() {
        return this.isRequestHedged;
    }

    public void addPartitionKeyRangeWithSuccess(PartitionKeyRange partitionKeyRange, String resourceId) {
        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);
        this.partitionKeyRangesWithSuccess.put(partitionKeyRangeWrapper, partitionKeyRangeWrapper);
    }

    public boolean hasPartitionKeyRangeSeenSuccess(PartitionKeyRange partitionKeyRange, String resourceId) {
        PartitionKeyRangeWrapper partitionKeyRangeWrapper = new PartitionKeyRangeWrapper(partitionKeyRange, resourceId);
        return this.partitionKeyRangesWithSuccess.containsKey(partitionKeyRangeWrapper);
    }

    public boolean isThresholdBasedAvailabilityStrategyEnabled() {
        return isThresholdBasedAvailabilityStrategyEnabled;
    }

    public String getCollectionLink() {
        return collectionLink;
    }
}
