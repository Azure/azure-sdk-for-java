// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import java.time.Duration;

/**
 * The interface for determining the retry strategy used in {@link RetryPolicy}.
 */
public interface RetryStrategy {

    /**
     * Max number of retry attempts to be make.
     * @return The max number of retry attempts.
     */
    int getMaxRetries();

    /**
     * Computes the delay between each retry.
     *
     * @param retryAttempts The number of retry attempts completed so far.
     * @return The delay duration before the next retry.
     */
    Duration calculateRetryDelay(int retryAttempts);
}
