// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.util.Beta;

import java.time.Duration;

/***
 * Fault injection server error result.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class FaultInjectionServerErrorResult implements IFaultInjectionResult {
    private final FaultInjectionServerErrorType serverErrorType;
    private final Integer times;
    private final Duration delay;

    FaultInjectionServerErrorResult(FaultInjectionServerErrorType serverErrorTypes, Integer times, Duration delay) {
        this.serverErrorType = serverErrorTypes;
        this.times = times;
        this.delay = delay;
    }

    /***
     * Get the fault injection server error type.
     *
     * @return the {@link FaultInjectionServerErrorType}.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionServerErrorType getServerErrorType() {
        return serverErrorType;
    }

    /***
     * Get the number of how many times the rule can be applied on a single operation.
     *
     * @return the times.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Integer getTimes() {
        return times;
    }

    /***
     * Get the injected delay for the server error.
     * Will be required for SERVER_RESPONSE_DELAY and SERVER_CONNECTION_DELAY, will be ignored for others.
     *
     * @return the injected delay.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "FaultInjectionServerErrorResult{" +
            "serverErrorType=" + serverErrorType +
            ", times=" + times +
            ", delay=" + delay +
            '}';
    }
}
