// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.util.Beta;

import java.time.Duration;

/***
 * Fault injection connection error result.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class FaultInjectionConnectionErrorResult implements IFaultInjectionResult {
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
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionConnectionErrorType getErrorType() {
        return errorType;
    }

    /***
     * Get the fault injection error rule apply interval.
     *
     * @return the interval.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getInterval() {
        return interval;
    }

    /***
     * Get the threshold of connections to be closed when the rule is applied.
     *
     * @return the threshold.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
