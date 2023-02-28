// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class FaultInjectionConnectionErrorResultBuilder {
    private static final double DEFAULT_CONNECTION_THRESHOLD = 1.0;
    private final FaultInjectionConnectionErrorType connectionErrorType;
    private Duration interval;
    private Double threshold = DEFAULT_CONNECTION_THRESHOLD;

    public FaultInjectionConnectionErrorResultBuilder(FaultInjectionConnectionErrorType connectionErrorType) {
        checkNotNull(connectionErrorType, "Argument 'connectionErrorType' can not be null");
        this.connectionErrorType = connectionErrorType;
    }

    /**
     * Indicates how often the connection error will be injected.
     *
     * @param interval the interval of triggering the connection error.
     * @return the builder.
     */
    public FaultInjectionConnectionErrorResultBuilder interval(Duration interval) {
        checkNotNull(interval, "Argument 'interval' can not be null");
        this.interval = interval;
        return this;
    }

    /***
     * Indicates the percentage of total established connections will be impacted when the connection error is injected.
     *
     * @param threshold the percentage of established connection will be impacted when the connection error is injected.
     * @return the builder.
     */
    public FaultInjectionConnectionErrorResultBuilder threshold(double threshold) {
        checkArgument(threshold > 0 && threshold <= 1, "Argument 'threshold' should be between [0, 1)");
        this.threshold = threshold;
        return this;
    }

    public FaultInjectionConnectionErrorResult build() {
        checkNotNull(this.connectionErrorType, "Argument 'connectionErrorType' can not be null");
        checkNotNull(this.interval, "Argument 'interval' can not be null");

        return new FaultInjectionConnectionErrorResult(this.connectionErrorType, this.interval, this.threshold);
    }
}
