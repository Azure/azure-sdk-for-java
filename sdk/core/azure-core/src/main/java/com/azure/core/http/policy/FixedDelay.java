// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.implementation.accesshelpers.FixedDelayAccessHelper;
import com.azure.core.util.logging.ClientLogger;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * The {@code FixedDelay} class is an implementation of the {@link RetryStrategy} interface. This strategy uses a
 * fixed delay duration between each retry attempt.
 *
 * <p>This class is useful when you need to handle retries for operations that may transiently fail. It ensures that
 * the retries are performed with a fixed delay to provide a consistent delay between retries.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code FixedDelay} is created with a maximum of 3 retry attempts and a delay of 1 second
 * between each attempt. The strategy is then used in a {@code RetryPolicy} which can then be added to the pipeline.
 * For a request then sent by the pipeline, if the server responds with a transient error, the request will be retried
 * with a fixed delay of 1 second between each attempt.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.FixedDelay.constructor -->
 * <pre>
 * FixedDelay retryStrategy = new FixedDelay&#40;3, Duration.ofSeconds&#40;1&#41;&#41;;
 * RetryPolicy policy = new RetryPolicy&#40;retryStrategy&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.FixedDelay.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.RetryStrategy
 * @see com.azure.core.http.policy.RetryPolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class FixedDelay implements RetryStrategy {
    private static final ClientLogger LOGGER = new ClientLogger(FixedDelay.class);

    static {
        FixedDelayAccessHelper.setAccessor(FixedDelay::new);
    }

    private final int maxRetries;
    private final Duration delay;
    private final Predicate<RequestRetryCondition> shouldRetryCondition;

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param maxRetries The max number of retry attempts that can be made.
     * @param delay The fixed delay duration between retry attempts.
     * @throws IllegalArgumentException If {@code maxRetries} is negative.
     * @throws NullPointerException If {@code delay} is {@code null}.
     */
    public FixedDelay(int maxRetries, Duration delay) {
        this(maxRetries, delay, null);
    }

    /**
     * Creates an instance of {@link FixedDelay}.
     *
     * @param fixedDelayOptions The {@link FixedDelayOptions}.
     */
    public FixedDelay(FixedDelayOptions fixedDelayOptions) {
        this(Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getMaxRetries(),
            Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getDelay());
    }

    private FixedDelay(FixedDelayOptions fixedDelayOptions, Predicate<RequestRetryCondition> shouldRetryCondition) {
        this(Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getMaxRetries(),
            Objects.requireNonNull(fixedDelayOptions, "'fixedDelayOptions' cannot be null.").getDelay(),
            shouldRetryCondition);
    }

    private FixedDelay(int maxRetries, Duration delay, Predicate<RequestRetryCondition> shouldRetryCondition) {
        if (maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        this.maxRetries = maxRetries;
        this.delay = Objects.requireNonNull(delay, "'delay' cannot be null.");
        this.shouldRetryCondition = shouldRetryCondition;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        return delay;
    }

    @Override
    public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
        return shouldRetryCondition == null
            ? RetryStrategy.super.shouldRetryCondition(requestRetryCondition)
            : shouldRetryCondition.test(requestRetryCondition);
    }
}
