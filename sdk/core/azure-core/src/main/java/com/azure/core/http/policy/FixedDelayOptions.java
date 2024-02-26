// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;
import java.util.Objects;

/**
 * The {@code FixedDelayOptions} class provides configuration options for the {@link FixedDelay} retry strategy.
 * This strategy uses a fixed delay duration between each retry attempt.
 *
 * <p>This class is useful when you need to customize the behavior of the fixed delay retry strategy. It allows you
 * to specify the maximum number of retry attempts and the delay duration between each attempt.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code FixedDelayOptions} is created and used to configure a {@code FixedDelay} retry strategy.
 * The strategy is then used in a {@code RetryPolicy} which can then be added to the pipeline. For a request then sent
 * by the pipeline, if the server responds with a transient error, the request will be retried with a fixed delay
 * between each attempt.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.FixedDelayOptions.constructor -->
 * <pre>
 * FixedDelayOptions options = new FixedDelayOptions&#40;3, Duration.ofSeconds&#40;1&#41;&#41;;
 * FixedDelay retryStrategy = new FixedDelay&#40;options&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.FixedDelayOptions.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.FixedDelay
 * @see com.azure.core.http.policy.RetryPolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class FixedDelayOptions {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelayOptions.class);
    private final int maxRetries;
    private final Duration delay;

    /**
     * Creates an instance of {@link FixedDelayOptions}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param delay The fixed delay duration between retry attempts.
     * @throws IllegalArgumentException If {@code maxRetries} is negative.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelayOptions(int maxRetries, Duration delay) {
        if (maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
    }

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public int getMaxRetries() {
        return maxRetries;
    }

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public Duration getDelay() {
        return delay;
    }
}
