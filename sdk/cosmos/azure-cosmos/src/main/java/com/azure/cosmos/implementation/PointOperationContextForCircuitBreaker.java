// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

public class PointOperationContextForCircuitBreaker {

    private final AtomicBoolean hasOperationSeenSuccess;

    private final boolean isThresholdBasedAvailabilityStrategyEnabled;

    private boolean isRequestHedged;

    public PointOperationContextForCircuitBreaker(AtomicBoolean hasOperationSeenSuccess, boolean isThresholdBasedAvailabilityStrategyEnabled) {
        this.hasOperationSeenSuccess = hasOperationSeenSuccess;
        this.isThresholdBasedAvailabilityStrategyEnabled = isThresholdBasedAvailabilityStrategyEnabled;
    }

    public void setIsRequestHedged(boolean isRequestHedged) {
        this.isRequestHedged = isRequestHedged;
    }

    public boolean getIsRequestHedged() {
        return this.isRequestHedged;
    }

    public void setHasOperationSeenSuccess() {
        this.hasOperationSeenSuccess.set(true);
    }

    public boolean getHasOperationSeenSuccess() {
        return hasOperationSeenSuccess.get();
    }

    public boolean isThresholdBasedAvailabilityStrategyEnabled() {
        return isThresholdBasedAvailabilityStrategyEnabled;
    }
}
