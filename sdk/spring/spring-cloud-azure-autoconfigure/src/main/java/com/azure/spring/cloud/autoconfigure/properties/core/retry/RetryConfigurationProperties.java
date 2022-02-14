// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties.core.retry;

import com.azure.spring.core.aware.RetryAware;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * Http based client related retry properties.
 */
public class RetryConfigurationProperties implements RetryAware.Retry {

    @NestedConfigurationProperty
    private final Backoff backoff = new Backoff();
    /**
     * The maximum number of attempts.
     */
    private Integer maxAttempts;
    /**
     * Amount of time to wait until a timeout.
     */
    private Duration timeout;

    public Backoff getBackoff() {
        return backoff;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    /**
     * Backoff properties when a retry fails.
     */
    public static class Backoff implements RetryAware.Backoff {
        /**
         * Amount of time to wait between retry attempts.
         */
        private Duration delay;
        /**
         * Maximum permissible amount of time between retry attempts.
         */

        // TODO (properties) doesn't work with
        private Duration maxDelay;
        /**
         * Multiplier used to calculate the next backoff delay. If positive, then used as a multiplier for generating
         * the next delay for backoff.
         */
        private Double multiplier;

        public Duration getDelay() {
            return delay;
        }

        public void setDelay(Duration delay) {
            this.delay = delay;
        }

        public Duration getMaxDelay() {
            return maxDelay;
        }

        public void setMaxDelay(Duration maxDelay) {
            this.maxDelay = maxDelay;
        }

        public Double getMultiplier() {
            return multiplier;
        }

        public void setMultiplier(Double multiplier) {
            this.multiplier = multiplier;
        }
    }
}
