// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;

/**
 * This class contains configuration for applying the HTTP header {@code Expect: 100-continue} to requests that carry a
 * body.
 */
@Fluent
public final class ExpectContinueOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ExpectContinueOptions.class);

    private static final Duration DEFAULT_THROTTLE_INTERVAL = Duration.ofMinutes(1);

    private ExpectContinueMode mode = ExpectContinueMode.APPLY_ON_THROTTLE;
    private Long contentLengthThreshold;
    private Duration throttleInterval = DEFAULT_THROTTLE_INTERVAL;

    /**
     * Creates a new {@link ExpectContinueOptions} with default parameters applied.
     */
    public ExpectContinueOptions() {
    }

    /**
     * Gets the mode determining when {@code Expect: 100-continue} is applied.
     *
     * @return The mode. Defaults to {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     */
    public ExpectContinueMode getMode() {
        return mode;
    }

    /**
     * Sets the mode determining when {@code Expect: 100-continue} is applied.
     *
     * @param mode The mode. A null value resets this to {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     * @return The updated options.
     */
    public ExpectContinueOptions setMode(ExpectContinueMode mode) {
        this.mode = mode == null ? ExpectContinueMode.APPLY_ON_THROTTLE : mode;
        return this;
    }

    /**
     * Gets the minimum request {@code Content-Length} for applying {@code Expect: 100-continue}. Requests whose body
     * length is known and smaller than this value will not have the header applied. Requests whose body length cannot
     * be determined ahead of time are always eligible for the header.
     *
     * @return The threshold in bytes, or null if every request with a body is eligible.
     */
    public Long getContentLengthThreshold() {
        return contentLengthThreshold;
    }

    /**
     * Sets the minimum request {@code Content-Length} for applying {@code Expect: 100-continue}. Requests whose body
     * length is known and smaller than this value will not have the header applied. Requests whose body length cannot
     * be determined ahead of time are always eligible for the header.
     *
     * @param contentLengthThreshold The threshold in bytes. A null value means every request with a body is eligible.
     * @return The updated options.
     * @throws IllegalArgumentException If the threshold is negative.
     */
    public ExpectContinueOptions setContentLengthThreshold(Long contentLengthThreshold) {
        if (contentLengthThreshold != null && contentLengthThreshold < 0) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'contentLengthThreshold' cannot be negative."));
        }

        this.contentLengthThreshold = contentLengthThreshold;
        return this;
    }

    /**
     * Gets the interval for which {@code Expect: 100-continue} is applied after a triggering response is received from
     * the service. Only used in mode {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     *
     * @return The interval. Defaults to one minute.
     */
    public Duration getThrottleInterval() {
        return throttleInterval;
    }

    /**
     * Sets the interval for which {@code Expect: 100-continue} is applied after a triggering response is received from
     * the service. Only used in mode {@link ExpectContinueMode#APPLY_ON_THROTTLE}.
     *
     * @param throttleInterval The interval. A null value resets this to the default of one minute.
     * @return The updated options.
     * @throws IllegalArgumentException If the interval is negative.
     */
    public ExpectContinueOptions setThrottleInterval(Duration throttleInterval) {
        if (throttleInterval != null && throttleInterval.isNegative()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'throttleInterval' cannot be negative."));
        }

        this.throttleInterval = throttleInterval == null ? DEFAULT_THROTTLE_INTERVAL : throttleInterval;
        return this;
    }
}
