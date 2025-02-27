// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

public class CrossRegionAvailabilityContextForRxDocumentServiceRequest {

    private final AtomicBoolean shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable = new AtomicBoolean(false);

    private final AtomicBoolean hasPerPartitionAutomaticFailoverBeenAppliedForReads = new AtomicBoolean(false);

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

    public boolean shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable() {
        return shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable.get();
    }

    public void shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable(boolean shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable) {
        this.shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable.set(shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable);
    }

    public void setPerPartitionAutomaticFailoverAppliedStatusForReads(boolean perPartitionAutomaticFailoverAppliedStatus) {
        this.hasPerPartitionAutomaticFailoverBeenAppliedForReads.set(perPartitionAutomaticFailoverAppliedStatus);
    }

    public boolean hasPerPartitionAutomaticFailoverBeenAppliedForReads() {
        return this.hasPerPartitionAutomaticFailoverBeenAppliedForReads.get();
    }
}
