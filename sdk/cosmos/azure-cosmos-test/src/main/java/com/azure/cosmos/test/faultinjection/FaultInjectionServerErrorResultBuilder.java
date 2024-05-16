// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.faultinjection;

import java.time.Duration;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/***
 * Fault injection server error result builder.
 */
public final class FaultInjectionServerErrorResultBuilder {
    private static final double DEFAULT_INJECTION_RATE = 1.0;
    private final FaultInjectionServerErrorType serverErrorType;
    private int times = Integer.MAX_VALUE;
    private Duration delay;

    private Boolean suppressServiceRequests = null;
    private double injectionRate = DEFAULT_INJECTION_RATE;

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
     * This is only used for Server_Response_Delay error tye.
     *
     * For SERVER_RESPONSE_DELAY, it controls whether the original request should be sent to the service.
     * The default is that the request will be sent when the delay is lower than the network request timeout, but
     * if the delay exceeds the network request timeout the request is not even sent to the service. This property
     * can be used to override the behavior.
     *
     * NOTE: If suppressServiceRequests == true the injected delay will not be reported in the transit time
     * request pipeline step but in the received pipeline step.
     *
     * @param suppressServiceRequests a flag indicating whether to send the requests to the service.
     * @return the builder.
     */
    public FaultInjectionServerErrorResultBuilder suppressServiceRequests(boolean suppressServiceRequests) {
        this.suppressServiceRequests = suppressServiceRequests;

        return this;
    }

    /***
     * What percent of times the fault injection rule will be applied.
     *
     * @param injectionRate a double between (0,1] representing the percent of times that the rule will be applied.
     *                        default value is 1.0 or 100%
     * @return the builder
     */
    public FaultInjectionServerErrorResultBuilder injectionRate(double injectionRate) {
        checkArgument(injectionRate > 0 && injectionRate <= 1, "Argument 'injectionRate' should be between (0, 1]");
        this.injectionRate = injectionRate;

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

        if (this.serverErrorType == FaultInjectionServerErrorType.STALED_ADDRESSES_SERVER_GONE) {
            // for staled addresses errors, the error can only be cleared if forceRefresh address refresh request happened
            // so default the times to max value
            this.times = Integer.MAX_VALUE;
        }

        return new FaultInjectionServerErrorResult(
            this.serverErrorType,
            this.times,
            this.delay,
            this.suppressServiceRequests,
            this.injectionRate);
    }
}
