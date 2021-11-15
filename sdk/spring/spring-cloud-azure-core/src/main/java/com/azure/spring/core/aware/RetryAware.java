// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Interface to be implemented by classes that wish to be aware of the retry properties.
 */
public interface RetryAware {

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

        Backoff getBackoff();
    }

    /**
     * Interface to be implemented by classes that wish to describe http related retry operations.
     */
    interface HttpRetry extends Retry {

        String getRetryAfterHeader();

        ChronoUnit getRetryAfterTimeUnit();
    }

    /**
     * Interface to be implemented by classes that wish to describe the backoff when retrying.
     */
    interface Backoff {

        Duration getDelay();

        Duration getMaxDelay();

        Double getMultiplier();
    }
}
