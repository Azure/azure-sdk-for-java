// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.provider;

import java.time.Duration;

/**
 * Interface to be implemented by classes that wish to provide the retry options.
 */
public interface RetryOptionsProvider {

    /**
     * Get the retry configuration.
     *
     * @return the retry configuration.
     */
    RetryOptions getRetry();

    /**
     * Interface to be implemented by classes that wish to describe retry options.
     */
    interface RetryOptions {


        /**
         * Get the retry backoff mode.
         *
         * @return the retry backoff mode.
         */
        RetryMode getMode();

        /**
         * Get the retry options of the fixed retry mode.
         *
         * @return the retry options of fixed retry mode.
         */
        FixedRetryOptions getFixed();

        /**
         * Get the retry options of the exponential retry mode.
         *
         * @return the retry options of exponential retry mode.
         */
        ExponentialRetryOptions getExponential();

        /**
         * Interface to be implemented by classes that wish to describe retry options of fixed retry mode.
         */
        interface FixedRetryOptions {

            /**
             * The maximum number of attempts.
             * @return the max attempts.
             */
            Integer getMaxRetries();

            /**
             * Get the delay duration.
             * @return the delay duration.
             */
            Duration getDelay();

        }

        /**
         * Interface to be implemented by classes that wish to describe retry options of exponential retry mode.
         */
        interface ExponentialRetryOptions {

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

        }
    }

    /**
     * Interface to be implemented by classes that wish to describe amqp related retry options.
     */
    interface AmqpRetryOptions extends RetryOptions {

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

        /**
         * Fixed backoff retry mode.
         */
        FIXED,

        /**
         * Exponential backoff retry mode.
         */
        EXPONENTIAL
    }
}
