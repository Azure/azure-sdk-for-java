// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.faultinjection;

import com.azure.cosmos.util.Beta;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injection server error result builder.
 */
@Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionServerErrorResultBuilder delay(Duration delay) {
        checkNotNull(delay, "Argument 'delay' can not be null");
        if (this.serverErrorType == FaultInjectionServerErrorType.SERVER_RESPONSE_DELAY
            || this.serverErrorType == FaultInjectionServerErrorType.SERVER_CONNECTION_DELAY) {
            this.delay = delay;
        }
        return this;
    }

    /***
     * Create a new fault injection server error result.
     *
     * @return the {@link FaultInjectionServerErrorResult}.
     */
    @Beta(value = Beta.SinceVersion.V4_42_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public FaultInjectionServerErrorResult build() {
        if ((this.serverErrorType == FaultInjectionServerErrorType.SERVER_RESPONSE_DELAY
            || this.serverErrorType == FaultInjectionServerErrorType.SERVER_CONNECTION_DELAY) && this.delay == null) {
            throw new IllegalArgumentException("Argument 'delay' is required for server error type " + this.serverErrorType);
        }

        return new FaultInjectionServerErrorResult(
            this.serverErrorType,
            this.times,
            this.delay);
    }
}
