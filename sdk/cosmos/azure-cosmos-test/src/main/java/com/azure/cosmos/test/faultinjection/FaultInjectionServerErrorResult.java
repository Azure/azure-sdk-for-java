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

    private final Boolean suppressServiceRequests;

    FaultInjectionServerErrorResult(
        FaultInjectionServerErrorType serverErrorTypes,
        Integer times,
        Duration delay,
        Boolean suppressServiceRequests) {

        this.serverErrorType = serverErrorTypes;
        this.times = times;
        this.delay = delay;
        this.suppressServiceRequests = suppressServiceRequests;
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

    /***
     * Get a flag indicating whether service requests should be suppressed. If not specified (null) the default
     * behavior is applied - only sending the request to the service when the delay is lower
     * than the network request timeout.
     * @return a flag indicating whether service requests should be suppressed.
     */
    public Boolean getSuppressServiceRequests() {
        return this.suppressServiceRequests;
    }

    @Override
    public String toString() {
        return String.format(
            "FaultInjectionServerErrorResult{ serverErrorType=%s, times=%s, delay=%s }",
            this.serverErrorType,
            this.times,
            this.delay);
    }
}
