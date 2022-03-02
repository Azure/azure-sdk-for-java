// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.aware;

import java.time.Duration;

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
     * Interface to be implemented by classes that wish to describe retry options.
     */
    interface Retry {

        /**
         * The maximum number of attempts.
         * @return the max attempts.
         */
        Integer getMaxRetries();

        /**
         * Get the delay duration.
         * @return the delay duration.
         */
        Duration getBaseDelay();

        /**
         * Get the max delay duration.
         * @return the max delay duration.
         */
        Duration getMaxDelay();

        /**
         * Get the retry backoff mode.
         * @return the retry backoff mode.
         */
        RetryMode getMode();
    }

    /**
     * Interface to be implemented by classes that wish to describe amqp related retry options.
     */
    interface AmqpRetry extends Retry {

        /**
         * Amount of time to wait until a timeout.
         * @return the timeout.
         */
        Duration getTryTimeout();

    }

    /**
     * The retry backoff mode when retrying.
     */
    enum RetryMode {

        FIXED,
        EXPONENTIAL
    }
}
