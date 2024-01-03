// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.logging.ClientLogger;

import java.time.Duration;

/**
 * <p>The {@code ExponentialBackoffOptions} class provides configuration options for the {@link ExponentialBackoff}
 * retry strategy. This strategy uses a delay duration that exponentially increases with each retry attempt until an
 * upper bound is reached. After reaching the upper bound, every retry attempt is delayed by the provided max delay
 * duration.</p>
 *
 * <p>This class is useful when you need to customize the behavior of the exponential backoff strategy. It allows you
 * to specify the maximum number of retry attempts, the base delay duration, and the maximum delay duration.</p>
 *
 * <p>Here's a code sample of how to use this class:</p>
 *
 * <pre>
 * {@code
 * ExponentialBackoffOptions options = new ExponentialBackoffOptions()
 *     .setMaxRetries(5)
 *     .setBaseDelay(Duration.ofSeconds(1))
 *     .setMaxDelay(Duration.ofSeconds(10));
 *
 * ExponentialBackoff retryStrategy = new ExponentialBackoff(options);
 *
 * HttpPipeline pipeline = new HttpPipelineBuilder()
 *     .policies(new RetryPolicy(retryStrategy), new CustomPolicy())
 *     .build();
 *
 * HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("http://example.com"));
 * HttpResponse response = pipeline.send(request).block();
 * }
 * </pre>
 *
 * <p>In this example, an {@code ExponentialBackoffOptions} is created and used to configure an
 * {@code ExponentialBackoff} retry strategy. The strategy is then used in a {@code RetryPolicy} which is added to the
 * pipeline. The pipeline is used to send an HTTP request, and the response is retrieved. If the server responds with a
 * transient error, the request will be retried with an exponentially increasing delay.</p>
 *
 * @see com.azure.core.http.policy.ExponentialBackoff
 * @see com.azure.core.http.policy.RetryPolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class ExponentialBackoffOptions {
    private static final ClientLogger LOGGER = new ClientLogger(ExponentialBackoffOptions.class);

    private Integer maxRetries;
    private Duration baseDelay;
    private Duration maxDelay;

    /**
     * Creates a new instance of {@link ExponentialBackoffOptions}.
     */
    public ExponentialBackoffOptions() {
    }

    /**
     * Gets the max retry attempts that can be made.
     *
     * @return The max retry attempts that can be made.
     */
    public Integer getMaxRetries() {
        return maxRetries;
    }

    /**
     * Sets the max retry attempts that can be made.
     *
     * @param maxRetries the max retry attempts that can be made.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0.
     * @return The updated {@link ExponentialBackoffOptions}
     */
    public ExponentialBackoffOptions setMaxRetries(Integer maxRetries) {
        if (maxRetries != null && maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        return this;
    }

    /**
     * Gets the base delay duration for retry.
     *
     * @return The base delay duration for retry.
     */
    public Duration getBaseDelay() {
        return baseDelay;
    }

    /**
     * Sets the base delay duration for retry.
     *
     * @param baseDelay the base delay duration for retry.
     * @throws IllegalArgumentException if {@code baseDelay} is less than or equal
     * to 0 or {@code maxDelay} has been set and is less than {@code baseDelay}.
     * @return The updated {@link ExponentialBackoffOptions}
     */
    public ExponentialBackoffOptions setBaseDelay(Duration baseDelay) {
        validateDelays(baseDelay, maxDelay);
        this.baseDelay = baseDelay;
        return this;
    }

    /**
     * Gets the max delay duration for retry.
     *
     * @return The max delay duration for retry.
     */
    public Duration getMaxDelay() {
        return maxDelay;
    }

    /**
     * Sets the max delay duration for retry.
     *
     * @param maxDelay the max delay duration for retry.
     * @throws IllegalArgumentException if {@code maxDelay} is less than or equal
     * to 0 or {@code baseDelay} has been set and is more than {@code maxDelay}.
     * @return The updated {@link ExponentialBackoffOptions}
     */
    public ExponentialBackoffOptions setMaxDelay(Duration maxDelay) {
        validateDelays(baseDelay, maxDelay);
        this.maxDelay = maxDelay;
        return this;
    }

    private void validateDelays(Duration baseDelay, Duration maxDelay) {
        if (baseDelay != null && (baseDelay.isZero() || baseDelay.isNegative())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be negative or 0."));
        }
        if (maxDelay != null && (maxDelay.isZero() || maxDelay.isNegative())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'maxDelay' cannot be negative or 0."));
        }

        if (baseDelay != null && maxDelay != null && baseDelay.compareTo(maxDelay) > 0) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
        }
    }
}
