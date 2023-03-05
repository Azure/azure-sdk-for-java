// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import java.time.Duration;

/***
 * Fault injection server error result.
 */
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
    public FaultInjectionServerErrorType getServerErrorType() {
        return serverErrorType;
    }

    /***
     * Get the number of how many times the rule can be applied on a single operation.
     *
     * @return the times.
     */
    public Integer getTimes() {
        return times;
    }

    /***
     * Get the injected delay for the server error.
     * Will be required for SERVER_RESPONSE_DELAY and SERVER_CONNECTION_DELAY, will be ignored for others.
     *
     * @return the injected delay.
     */
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
