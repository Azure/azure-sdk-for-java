// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.Map;

public class FeedOperationContextForCircuitBreaker {

    private final Map<GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper, GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper> partitionKeyRangesWithSuccess;
    private final boolean isThresholdBasedAvailabilityStrategyEnabled;
    private boolean isRequestHedged;

    public FeedOperationContextForCircuitBreaker(Map<GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper, GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper> partitionKeyRangesWithSuccess, boolean isThresholdBasedAvailabilityStrategyEnabled) {
        this.partitionKeyRangesWithSuccess = partitionKeyRangesWithSuccess;
        this.isThresholdBasedAvailabilityStrategyEnabled = isThresholdBasedAvailabilityStrategyEnabled;
    }

    public void setIsRequestHedged(boolean isRequestHedged) {
        this.isRequestHedged = isRequestHedged;
    }

    public boolean getIsRequestHedged() {
        return this.isRequestHedged;
    }

    public void addPartitionKeyRangeWithSuccess(PartitionKeyRange partitionKeyRange, String resourceId) {
        GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper partitionKeyRangeWrapper
            = new GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper(partitionKeyRange, resourceId);
        this.partitionKeyRangesWithSuccess.put(partitionKeyRangeWrapper, partitionKeyRangeWrapper);
    }

    public boolean hasPartitionKeyRangeSeenSuccess(PartitionKeyRange partitionKeyRange, String resourceId) {
        GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper partitionKeyRangeWrapper
            = new GlobalPartitionEndpointManagerForCircuitBreaker.PartitionKeyRangeWrapper(partitionKeyRange, resourceId);
        return this.partitionKeyRangesWithSuccess.containsKey(partitionKeyRangeWrapper);
    }

    public boolean isThresholdBasedAvailabilityStrategyEnabled() {
        return isThresholdBasedAvailabilityStrategyEnabled;
    }
}
