// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class AvailabilityStrategyContext {

    private final boolean isAvailabilityStrategyEnabled;

    private final boolean isHedgedRequest;

    public AvailabilityStrategyContext(boolean isAvailabilityStrategyEnabled, boolean isHedgedRequest) {
        this.isAvailabilityStrategyEnabled = isAvailabilityStrategyEnabled;
        this.isHedgedRequest = isHedgedRequest;
    }

    public boolean isHedgedRequest() {
        return this.isHedgedRequest;
    }

    public boolean isAvailabilityStrategyEnabled() {
        return this.isAvailabilityStrategyEnabled;
    }
}
