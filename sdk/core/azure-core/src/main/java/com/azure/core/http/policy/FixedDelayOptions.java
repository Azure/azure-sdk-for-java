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
 * <p>Here's a code sample of how to use this class:</p>
 *
 * <pre>
 * {@code
 * FixedDelayOptions options = new FixedDelayOptions(3, Duration.ofSeconds(1));
 *
 * FixedDelay retryStrategy = new FixedDelay(options);
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
 * <p>In this example, a {@code FixedDelayOptions} is created and used to configure a {@code FixedDelay} retry strategy.
 * The strategy is then used in a {@code RetryPolicy} which is added to the pipeline. The pipeline is used to send an
 * HTTP request, and the response is retrieved. If the server responds with a transient error, the request will be
 * retried with a fixed delay between each attempt.</p>
 *
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
