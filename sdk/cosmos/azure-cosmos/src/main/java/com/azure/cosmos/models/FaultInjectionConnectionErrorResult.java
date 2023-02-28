// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.time.Duration;

public class FaultInjectionConnectionErrorResult implements IFaultInjectionResult{
    private final FaultInjectionConnectionErrorType errorTypes;
    private Duration interval;
    private double threshold;

    FaultInjectionConnectionErrorResult(
        FaultInjectionConnectionErrorType errorTypes,
        Duration interval,
        double threshold) {
        this.errorTypes = errorTypes;
        this.interval = interval;
        this.threshold = threshold;
    }

    public FaultInjectionConnectionErrorType getErrorTypes() {
        return errorTypes;
    }

    public Duration getInterval() {
        return interval;
    }

    public double getThreshold() {
        return threshold;
    }
}
