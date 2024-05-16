// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

/**
 * The type of approach to apply when calculating the delay between retry attempts.
 */
public enum AmqpRetryMode {
    /**
     * Retry attempts happen at fixed intervals; each delay is a consistent duration.
     */
    FIXED,
    /**
     * Retry attempts will delay based on a backoff strategy, where each attempt will increase the duration that it
     * waits before retrying.
     */
    EXPONENTIAL,
}
