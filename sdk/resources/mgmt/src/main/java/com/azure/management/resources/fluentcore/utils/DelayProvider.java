/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.fluentcore.utils;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * A wrapper class for thread sleep.
 */
public class DelayProvider {
    /**
     * Puts current thread on sleep for passed milliseconds.
     *
     * @param milliseconds time to sleep for
     */

    private int longRunningOperationRetryTimeout = 30;

    /**
     * Wrapper for long-running operation retry timeout.
     *
     * @param lroRetryTimeout timeout value in seconds
     */
    public void setLroRetryTimeout(int lroRetryTimeout) {
        this.longRunningOperationRetryTimeout = lroRetryTimeout;
    }

    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * @return the current time.
     */
    public OffsetDateTime now() {
        return OffsetDateTime.now();
    }

    /**
     * Creates an observable that emits the given item after the specified time in milliseconds.
     *
     * @return delayed observable
     */
    public Duration getLroRetryTimeout() {
        return Duration.ofSeconds(this.longRunningOperationRetryTimeout);
    }

    /**
     * Wrapper for the duration for delay.
     *
     * @param delay the duration of proposed delay.
     * @return the duration of delay.
     */
    public Duration getDelayDuration(Duration delay) {
        return delay;
    }
}
