// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

public class PointOperationContextForCircuitBreaker {

    private final AtomicBoolean hasOperationSeenSuccess;
    private final boolean isThresholdBasedAvailabilityStrategyEnabled;
    private boolean isRequestHedged;
    private final String collectionLink;
    private final MetadataDiagnosticsContext metadataDiagnosticsContext;

    public PointOperationContextForCircuitBreaker(
        AtomicBoolean hasOperationSeenSuccess,
        boolean isThresholdBasedAvailabilityStrategyEnabled,
        String collectionLink,
        MetadataDiagnosticsContext metadataDiagnosticsContext) {

        this.hasOperationSeenSuccess = hasOperationSeenSuccess;
        this.isThresholdBasedAvailabilityStrategyEnabled = isThresholdBasedAvailabilityStrategyEnabled;
        this.collectionLink = collectionLink;
        this.metadataDiagnosticsContext = metadataDiagnosticsContext;
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

    public MetadataDiagnosticsContext getMetadataDiagnosticsContext() {
        return this.metadataDiagnosticsContext;
    }
}
