// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.faultinjection.model;

import com.azure.cosmos.models.FaultInjectionConnectionErrorType;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionConnectionErrorResultInternal {
    private final FaultInjectionConnectionErrorType errorType;
    private Duration interval;
    private double threshold;

    public FaultInjectionConnectionErrorResultInternal(
        FaultInjectionConnectionErrorType errorType,
        Duration interval,
        double threshold) {

        checkNotNull(errorType, "Argument 'errorType' can not be null");

        this.errorType = errorType;
        this.interval = interval;
        this.threshold = threshold;
    }

    public FaultInjectionConnectionErrorType getErrorType() {
        return errorType;
    }

    public Duration getInterval() {
        return interval;
    }

    public double getThreshold() {
        return threshold;
    }
}
