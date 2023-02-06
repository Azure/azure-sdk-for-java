// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.FaultInjectionServerErrorResult;
import com.azure.cosmos.models.FaultInjectionServerErrorType;

import java.time.Duration;

public class FaultInjectionServerErrorResultBuilder {
    private final FaultInjectionServerErrorType serverErrorType;
    private int times = Integer.MAX_VALUE;
    private Duration delay;

    public FaultInjectionServerErrorResultBuilder(FaultInjectionServerErrorType serverErrorType) {
        this.serverErrorType = serverErrorType;
    }

    /***
     * This is not used for Server_Dely error type
     *
     * @param times
     * @return
     */
    public FaultInjectionServerErrorResultBuilder times(int times) {
        if (this.serverErrorType == FaultInjectionServerErrorType.SERVER_DELAY) {
            throw new IllegalArgumentException("Argument 'times' is not support for error type " + this.serverErrorType);
        }

        this.times = times;
        return this;
    }

    /***
     * This is only used for Server_Delay error tye
     *
     * @param delay the delay added before returning the reponse
     * @return the {@link FaultInjectionServerErrorResult}.
     */
    public FaultInjectionServerErrorResultBuilder delay(Duration delay) {
        if (this.serverErrorType != FaultInjectionServerErrorType.SERVER_DELAY) {
            throw new IllegalArgumentException("Argument 'delay' is not supported for error type " + this.serverErrorType);
        }
        this.delay = delay;
        return this;
    }

    public FaultInjectionServerErrorResult build() {
        if (this.serverErrorType == FaultInjectionServerErrorType.SERVER_DELAY && this.delay == null) {
            throw new IllegalArgumentException("Argument 'delay' is required for server error type " + this.serverErrorType);
        }

        return new FaultInjectionServerErrorResult(
            this.serverErrorType,
            this.times,
            this.delay);
    }
}
