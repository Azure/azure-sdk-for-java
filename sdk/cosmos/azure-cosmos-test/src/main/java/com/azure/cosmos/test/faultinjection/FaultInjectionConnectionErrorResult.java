// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import java.time.Duration;

/***
 * Fault injection connection error result.
 */
public final class FaultInjectionConnectionErrorResult implements IFaultInjectionResult {
    private final FaultInjectionConnectionErrorType errorType;
    private final Duration interval;
    private final double threshold;

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

    @Override
    public String toString() {
        return "FaultInjectionConnectionErrorResult{" +
            "errorType=" + errorType +
            ", interval=" + interval +
            ", threshold=" + threshold +
            '}';
    }
}
