// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

public class PointOperationContextForCircuitBreaker {

    private final AtomicBoolean hasOperationSeenSuccess;
    private final boolean isThresholdBasedAvailabilityStrategyEnabled;
    private boolean isRequestHedged;
    private final String collectionLink;
    private final SerializationDiagnosticsContext serializationDiagnosticsContext;
    private final AtomicBoolean shouldUsePerPartitionAutomaticFailoverOverride;

    public PointOperationContextForCircuitBreaker(
        AtomicBoolean hasOperationSeenSuccess,
        boolean isThresholdBasedAvailabilityStrategyEnabled,
        String collectionLink,
        SerializationDiagnosticsContext serializationDiagnosticsContext) {

        this.hasOperationSeenSuccess = hasOperationSeenSuccess;
        this.isThresholdBasedAvailabilityStrategyEnabled = isThresholdBasedAvailabilityStrategyEnabled;
        this.collectionLink = collectionLink;
        this.serializationDiagnosticsContext = serializationDiagnosticsContext;
        this.shouldUsePerPartitionAutomaticFailoverOverride = new AtomicBoolean(false);
    }

    public void setIsRequestHedged(boolean isRequestHedged) {
        this.isRequestHedged = isRequestHedged;
    }

    public boolean isRequestHedged() {
        return this.isRequestHedged;
    }

    public void setHasOperationSeenSuccess() {
        this.hasOperationSeenSuccess.set(true);
    }

    public boolean getHasOperationSeenSuccess() {
        return hasOperationSeenSuccess.get();
    }

    public boolean isThresholdBasedAvailabilityStrategyEnabled() {
        return this.isThresholdBasedAvailabilityStrategyEnabled;
    }

    public String getCollectionLink() {
        return this.collectionLink;
    }

    public SerializationDiagnosticsContext getSerializationDiagnosticsContext() {
        return serializationDiagnosticsContext;
    }

    public boolean shouldUsePerPartitionAutomaticFailoverOverride() {
        return this.shouldUsePerPartitionAutomaticFailoverOverride.get();
    }

    public void setShouldUsePerPartitionAutomaticFailover(boolean shouldUsePerPartitionAutomaticFailoverOverride) {
        this.shouldUsePerPartitionAutomaticFailoverOverride.set(shouldUsePerPartitionAutomaticFailoverOverride);
    }
}
