// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.ExponentialRetryPolicy;
import com.azure.core.amqp.FixedRetryPolicy;
import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.RetryPolicy;

import java.util.Locale;

/**
 * Helper class to help with retry policies.
 */
public class RetryOptionsUtil {
    /**
     * Given a set of {@link RetryOptions options}, creates the appropriate retry policy.
     *
     * @param options A set of options used to configure the retry policy.
     * @return A new retry policy configured with the given {@code options}.
     */
    public static RetryPolicy getRetryPolicy(RetryOptions options) {
        switch (options.retryMode()) {
            case FIXED:
                return new FixedRetryPolicy(options);
            case EXPONENTIAL:
                return new ExponentialRetryPolicy(options);
            default:
                throw new IllegalArgumentException(
                    String.format(Locale.ROOT, "Mode is not supported: %s", options.retryMode()));
        }
    }
}
