// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

public class CrossRegionAvailabilityContextForRxDocumentServiceRequest {

    private final AtomicBoolean shouldUsePerPartitionAutomaticFailoverOverride = new AtomicBoolean(false);

    private final FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker;

    private final PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker;

    private final AvailabilityStrategyContext availabilityStrategyContext;

    public CrossRegionAvailabilityContextForRxDocumentServiceRequest(
        FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker,
        PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker,
        AvailabilityStrategyContext availabilityStrategyContext) {

        this.feedOperationContextForCircuitBreaker = feedOperationContextForCircuitBreaker;
        this.pointOperationContextForCircuitBreaker = pointOperationContextForCircuitBreaker;
        this.availabilityStrategyContext = availabilityStrategyContext;
    }

    public FeedOperationContextForCircuitBreaker getFeedOperationContextForCircuitBreaker() {
        return this.feedOperationContextForCircuitBreaker;
    }

    public AvailabilityStrategyContext getAvailabilityStrategyContext() {
        return this.availabilityStrategyContext;
    }

    public PointOperationContextForCircuitBreaker getPointOperationContextForCircuitBreaker() {
        return this.pointOperationContextForCircuitBreaker;
    }

    public boolean shouldUsePerPartitionAutomaticFailoverOverride() {
        return shouldUsePerPartitionAutomaticFailoverOverride.get();
    }

    public void setShouldUsePerPartitionAutomaticFailoverOverride(boolean shouldUsePerPartitionAutomaticFailoverOverride) {
        this.shouldUsePerPartitionAutomaticFailoverOverride.set(true);
    }
}
