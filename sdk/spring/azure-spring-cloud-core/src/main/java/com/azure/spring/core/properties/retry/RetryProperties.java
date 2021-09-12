// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.properties.retry;

import java.time.Duration;

/**
 * Common retry properties for all Azure SDKs.
 */
public class RetryProperties {

    private BackoffProperties backoff = new BackoffProperties();
    private Integer maxAttempts;
    private Duration timeout;

    public BackoffProperties getBackoff() {
        return backoff;
    }

    public void setBackoff(BackoffProperties backoff) {
        this.backoff = backoff;
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
    public static class BackoffProperties {

        private Duration delay;
        private Duration maxDelay;
        /**
         * If positive, then used as a multiplier for generating the next delay for backoff.
         *
         * @return a multiplier to use to calculate the next backoff delay
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
