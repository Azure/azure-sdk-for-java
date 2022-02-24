// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Interface to be implemented by classes that wish to be aware of the retry properties.
 */
public interface RetryOptionsAware {

    /**
     * Get the retry configuration.
     * @return the retry configuration.
     */
    Retry getRetry();

    /**
     * Interface to be implemented by classes that wish to describe retry operations.
     */
    interface Retry {

        /**
         * The maximum number of attempts.
         * @return the max attempts.
         */
        Integer getMaxAttempts();

        /**
         * Amount of time to wait until a timeout.
         * @return the timeout.
         */
        Duration getTimeout();

        /**
         * Get the backoff for retry configuration.
         * @return the backoff configuration.
         */
        Backoff getBackoff();
    }

    /**
     * Interface to be implemented by classes that wish to describe http related retry operations.
     */
    interface HttpRetry extends Retry {

        /**
         * Get the http header.
         * @return herder name
         */
        String getRetryAfterHeader();

        /**
         * Get the time unit to use when applying the retry delay
         * @return the time unit.
         */
        ChronoUnit getRetryAfterTimeUnit();
    }

    /**
     * Interface to be implemented by classes that wish to describe the backoff when retrying.
     */
    interface Backoff {

        /**
         * Get the delay duration.
         * @return the delay duration.
         */
        Duration getDelay();

        /**
         * Get the max delay duration.
         * @return the max delay duration.
         */
        Duration getMaxDelay();

        /**
         * Get the multiplier.
         * @return the multiplier.
         */
        Double getMultiplier();
    }
}
