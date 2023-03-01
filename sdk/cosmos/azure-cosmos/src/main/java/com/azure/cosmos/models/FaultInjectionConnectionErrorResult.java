// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import java.time.Duration;

/***
 * Fault injection connection error result.
 */
public class FaultInjectionConnectionErrorResult implements IFaultInjectionResult{
    private final FaultInjectionConnectionErrorType errorType;
    private Duration interval;
    private double threshold;

    FaultInjectionConnectionErrorResult(
        FaultInjectionConnectionErrorType errorType,
        Duration interval,
        double threshold) {
        this.errorType = errorType;
        this.interval = interval;
        this.threshold = threshold;
    }

    /***
     * Get the fault injection connection error type.
     *
     * @return the {@link FaultInjectionConnectionErrorType}.
     */
    public FaultInjectionConnectionErrorType getErrorType() {
        return errorType;
    }

    /***
     * Get the fault injection error rule apply interval.
     *
     * @return the interval.
     */
    public Duration getInterval() {
        return interval;
    }

    /***
     * Get the threshold of connections to be closed when the rule is applied.
     *
     * @return the threshold.
     */
    public double getThreshold() {
        return threshold;
    }
}
