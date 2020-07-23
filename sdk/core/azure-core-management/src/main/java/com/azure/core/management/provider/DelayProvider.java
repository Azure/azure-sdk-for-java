// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.management.provider;

import java.time.Duration;

/**
 * The DelayProvider to help thread sleep.
 */
public interface DelayProvider {

    /**
     * Thread sleep.
     *
     * @param milliseconds time for sleep
     */
    void sleep(int milliseconds);

    /**
     * Get the retry timeout for long running operation.
     *
     * @return the timeout value.
     */
    Duration getLroRetryTimeout();

    /**
     * Get the duration for delay.
     *
     * @param delay the duration of proposed delay.
     * @return the duration of delay.
     */
    Duration getDelayDuration(Duration delay);
}
