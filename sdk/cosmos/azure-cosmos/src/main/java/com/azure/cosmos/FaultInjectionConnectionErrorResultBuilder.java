// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FaultInjectionConnectionErrorResult;
import com.azure.cosmos.models.FaultInjectionConnectionErrorType;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class FaultInjectionConnectionErrorResultBuilder {
    private final FaultInjectionConnectionErrorType connectionErrorType;
    private Duration interval;
    private Double threshold;

    public FaultInjectionConnectionErrorResultBuilder(FaultInjectionConnectionErrorType connectionErrorType) {
        this.connectionErrorType = connectionErrorType;
    }

    public FaultInjectionConnectionErrorResultBuilder interval(Duration interval) {
        //TODO: Discussion: should we also validate the range of the interval
        this.interval = interval;
        return this;
    }

    public FaultInjectionConnectionErrorResultBuilder threshold(double threshold) {
        checkArgument(threshold > 0 && threshold <= 1, "Argument 'threshold' should be between [0, 1)");
        this.threshold = threshold;
        return this;
    }

    public FaultInjectionConnectionErrorResult build() {
        return new FaultInjectionConnectionErrorResult(this.connectionErrorType, this.interval, this.threshold);
    }
}
