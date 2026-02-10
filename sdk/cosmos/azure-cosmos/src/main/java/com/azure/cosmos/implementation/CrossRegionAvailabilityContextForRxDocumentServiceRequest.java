// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.perPartitionAutomaticFailover.PartitionLevelAutomaticFailoverInfo;
import com.azure.cosmos.implementation.perPartitionAutomaticFailover.PerPartitionAutomaticFailoverInfoHolder;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.LocationSpecificHealthContext;
import com.azure.cosmos.implementation.perPartitionCircuitBreaker.PerPartitionCircuitBreakerInfoHolder;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CrossRegionAvailabilityContextForRxDocumentServiceRequest {

    private final AtomicBoolean shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable = new AtomicBoolean(false);

    private final AtomicBoolean hasPerPartitionAutomaticFailoverBeenAppliedForReads = new AtomicBoolean(false);

    private final AtomicBoolean shouldAddHubRegionProcessingOnlyHeader;

    private final FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker;

    private final PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker;

    private final AvailabilityStrategyContext availabilityStrategyContext;

    private final PerPartitionCircuitBreakerInfoHolder perPartitionCircuitBreakerInfoHolder;

    private final PerPartitionAutomaticFailoverInfoHolder perPartitionAutomaticFailoverInfoHolder;

    public CrossRegionAvailabilityContextForRxDocumentServiceRequest(
        FeedOperationContextForCircuitBreaker feedOperationContextForCircuitBreaker,
        PointOperationContextForCircuitBreaker pointOperationContextForCircuitBreaker,
        AvailabilityStrategyContext availabilityStrategyContext,
        AtomicBoolean shouldAddHubRegionProcessingOnlyHeader,
        PerPartitionCircuitBreakerInfoHolder perPartitionCircuitBreakerInfoHolder,
        PerPartitionAutomaticFailoverInfoHolder perPartitionAutomaticFailoverInfoHolder) {

        this.feedOperationContextForCircuitBreaker = feedOperationContextForCircuitBreaker;
        this.pointOperationContextForCircuitBreaker = pointOperationContextForCircuitBreaker;
        this.availabilityStrategyContext = availabilityStrategyContext;
        this.shouldAddHubRegionProcessingOnlyHeader = shouldAddHubRegionProcessingOnlyHeader;
        this.perPartitionCircuitBreakerInfoHolder = perPartitionCircuitBreakerInfoHolder;
        this.perPartitionAutomaticFailoverInfoHolder = perPartitionAutomaticFailoverInfoHolder;
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

    public void setShouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable(boolean shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable) {
        this.shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable.set(shouldUsePerPartitionAutomaticFailoverOverrideForReadsIfApplicable);
    }

    public void setShouldAddHubRegionProcessingOnlyHeader(boolean shouldAddHubRegionProcessingOnlyHeader) {
        this.shouldAddHubRegionProcessingOnlyHeader.set(shouldAddHubRegionProcessingOnlyHeader);
    }

    public boolean shouldAddHubRegionProcessingOnlyHeader() {
        return this.shouldAddHubRegionProcessingOnlyHeader.get();
    }

    public void setPerPartitionAutomaticFailoverAppliedStatusForReads(boolean perPartitionAutomaticFailoverAppliedStatus) {
        this.hasPerPartitionAutomaticFailoverBeenAppliedForReads.set(perPartitionAutomaticFailoverAppliedStatus);
    }

    public boolean hasPerPartitionAutomaticFailoverBeenAppliedForReads() {
        return this.hasPerPartitionAutomaticFailoverBeenAppliedForReads.get();
    }

    public void setPerPartitionCircuitBreakerInfo(Map<String, LocationSpecificHealthContext> locationToLocationSpecificHealthContext) {
        this.perPartitionCircuitBreakerInfoHolder.setPerPartitionCircuitBreakerInfoHolder(locationToLocationSpecificHealthContext);
    }

    public PerPartitionCircuitBreakerInfoHolder getPerPartitionCircuitBreakerInfoHolder() {
        return this.perPartitionCircuitBreakerInfoHolder;
    }

    public void setPerPartitionFailoverInfo(PartitionLevelAutomaticFailoverInfo partitionLevelAutomaticFailoverInfo) {
        this.perPartitionAutomaticFailoverInfoHolder.setPartitionLevelFailoverInfo(partitionLevelAutomaticFailoverInfo);
    }

    public PerPartitionAutomaticFailoverInfoHolder getPerPartitionAutomaticFailoverInfoHolder() {
        return this.perPartitionAutomaticFailoverInfoHolder;
    }
}
