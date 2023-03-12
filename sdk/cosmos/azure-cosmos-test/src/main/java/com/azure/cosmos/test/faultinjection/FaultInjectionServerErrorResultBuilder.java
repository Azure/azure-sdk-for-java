// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injection server error result builder.
 */
public final class FaultInjectionServerErrorResultBuilder {
    private final FaultInjectionServerErrorType serverErrorType;
    private int times = Integer.MAX_VALUE;
    private Duration delay;

    FaultInjectionServerErrorResultBuilder(FaultInjectionServerErrorType serverErrorType) {
        this.serverErrorType = serverErrorType;
    }

    /**
     * How many times the same fault injection rule can be applied per operation.
     * By default, there is no limit.
     *
     * @param times the max times the same fault injection rule can be applied per operation.
     * @return the builder.
     */
    public FaultInjectionServerErrorResultBuilder times(int times) {
        this.times = times;
        return this;
    }

    /***
     * This is only used for Server_Response_Delay and Server_Connection_Delay error tye.
     *
     * For SERVER_RESPONSE_DELAY, it means the delay added before returning the response.
     * For SERVER_CONNECTION_DELAY, it means the delay added before starting the connection.
     *
     * @param delay the delay.
     * @return the builder.
     */
    public FaultInjectionServerErrorResultBuilder delay(Duration delay) {
        checkNotNull(delay, "Argument 'delay' can not be null");
        if (this.serverErrorType == FaultInjectionServerErrorType.RESPONSE_DELAY
            || this.serverErrorType == FaultInjectionServerErrorType.CONNECTION_DELAY) {
            this.delay = delay;
        }
        return this;
    }

    /***
     * Create a new fault injection server error result.
     *
     * @return the {@link FaultInjectionServerErrorResult}.
     * @throws IllegalArgumentException if server type is response_delay or connection_delay but delay is not defined.
     */
    public FaultInjectionServerErrorResult build() {
        if ((this.serverErrorType == FaultInjectionServerErrorType.RESPONSE_DELAY
            || this.serverErrorType == FaultInjectionServerErrorType.CONNECTION_DELAY) && this.delay == null) {
            throw new IllegalArgumentException("Argument 'delay' is required for server error type " + this.serverErrorType);
        }

        return new FaultInjectionServerErrorResult(
            this.serverErrorType,
            this.times,
            this.delay);
    }
}
