// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.implementation.accesshelpers.ExponentialBackoffAccessHelper;
import com.azure.core.implementation.util.ObjectsUtil;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import static com.azure.core.util.Configuration.PROPERTY_AZURE_REQUEST_RETRY_COUNT;

/**
 * <p>The {@code ExponentialBackoff} class is an implementation of the {@link RetryStrategy} interface. This strategy uses
 * a delay duration that exponentially increases with each retry attempt until an upper bound is reached, after which
 * every retry attempt is delayed by the provided max delay duration.</p>
 *
 * <p>This class is useful when you need to handle retries for operations that may transiently fail. It ensures that
 * the retries are performed with an increasing delay to avoid overloading the system.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, an {@code ExponentialBackoff} is created and used in a {@code RetryPolicy} which can be added to
 * a pipeline. For a request sent by the pipeline, if the server responds with a transient error, the request will be
 * retried with an exponentially increasing delay.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.ExponentialBackoff.constructor -->
 * <pre>
 * ExponentialBackoff retryStrategy = new ExponentialBackoff&#40;&#41;;
 * RetryPolicy policy = new RetryPolicy&#40;retryStrategy&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.ExponentialBackoff.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.RetryStrategy
 * @see com.azure.core.http.policy.RetryPolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 */
public class ExponentialBackoff implements RetryStrategy {
    private static final double JITTER_FACTOR = 0.05;
    private static final int DEFAULT_MAX_RETRIES;
    private static final Duration DEFAULT_BASE_DELAY = Duration.ofMillis(800);
    private static final Duration DEFAULT_MAX_DELAY = Duration.ofSeconds(8);
    private static final ClientLogger LOGGER = new ClientLogger(ExponentialBackoff.class);

    static {
        String envDefaultMaxRetries = Configuration.getGlobalConfiguration().get(PROPERTY_AZURE_REQUEST_RETRY_COUNT);

        int defaultMaxRetries = 3;
        if (!CoreUtils.isNullOrEmpty(envDefaultMaxRetries)) {
            try {
                defaultMaxRetries = Integer.parseInt(envDefaultMaxRetries);
                if (defaultMaxRetries < 0) {
                    defaultMaxRetries = 3;
                }
            } catch (NumberFormatException ignored) {
                LOGGER.log(LogLevel.VERBOSE, () -> PROPERTY_AZURE_REQUEST_RETRY_COUNT + " was loaded but is an invalid "
                    + "number. Using 3 retries as the maximum.");
            }
        }

        DEFAULT_MAX_RETRIES = defaultMaxRetries;

        ExponentialBackoffAccessHelper.setAccessor(ExponentialBackoff::new);
    }

    private final int maxRetries;
    private final long baseDelayNanos;
    private final long maxDelayNanos;
    private final Predicate<RequestRetryCondition> shouldRetryCondition;

    /**
     * Creates an instance of {@link ExponentialBackoff} with a maximum number of retry attempts configured by the
     * environment property {@link Configuration#PROPERTY_AZURE_REQUEST_RETRY_COUNT}, or three if it isn't configured or
     * is less than or equal to 0. This strategy starts with a delay of 800 milliseconds and exponentially increases
     * with each additional retry attempt to a maximum of 8 seconds.
     */
    public ExponentialBackoff() {
        this(DEFAULT_MAX_RETRIES, DEFAULT_BASE_DELAY, DEFAULT_MAX_DELAY);
    }

    /**
     * Creates an instance of {@link ExponentialBackoff}.
     *
     * @param options The {@link ExponentialBackoffOptions}.
     * @throws NullPointerException if {@code options} is {@code null}.
     */
    public ExponentialBackoff(ExponentialBackoffOptions options) {
        this(
            ObjectsUtil.requireNonNullElse(Objects.requireNonNull(options, "'options' cannot be null.").getMaxRetries(),
                DEFAULT_MAX_RETRIES),
            ObjectsUtil.requireNonNullElse(Objects.requireNonNull(options, "'options' cannot be null.").getBaseDelay(),
                DEFAULT_BASE_DELAY),
            ObjectsUtil.requireNonNullElse(Objects.requireNonNull(options, "'options' cannot be null.").getMaxDelay(),
                DEFAULT_MAX_DELAY));
    }

    private ExponentialBackoff(ExponentialBackoffOptions options,
        Predicate<RequestRetryCondition> shouldRetryCondition) {
        this(
            ObjectsUtil.requireNonNullElse(Objects.requireNonNull(options, "'options' cannot be null.").getMaxRetries(),
                DEFAULT_MAX_RETRIES),
            ObjectsUtil.requireNonNullElse(Objects.requireNonNull(options, "'options' cannot be null.").getBaseDelay(),
                DEFAULT_BASE_DELAY),
            ObjectsUtil.requireNonNullElse(Objects.requireNonNull(options, "'options' cannot be null.").getMaxDelay(),
                DEFAULT_MAX_DELAY),
            shouldRetryCondition);
    }

    /**
     * Creates an instance of {@link ExponentialBackoff}.
     *
     * @param maxRetries The max retry attempts that can be made.
     * @param baseDelay The base delay duration for retry.
     * @param maxDelay The max delay duration for retry.
     * @throws IllegalArgumentException if {@code maxRetries} is less than 0 or {@code baseDelay} is less than or equal
     * to 0 or {@code maxDelay} is less than {@code baseDelay}.
     */
    public ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay) {
        this(maxRetries, baseDelay, maxDelay, null);
    }

    private ExponentialBackoff(int maxRetries, Duration baseDelay, Duration maxDelay,
        Predicate<RequestRetryCondition> shouldRetryCondition) {
        if (maxRetries < 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Max retries cannot be less than 0."));
        }
        Objects.requireNonNull(baseDelay, "'baseDelay' cannot be null.");
        Objects.requireNonNull(maxDelay, "'maxDelay' cannot be null.");

        if (baseDelay.isZero() || baseDelay.isNegative()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be negative or 0."));
        }

        if (baseDelay.compareTo(maxDelay) > 0) {
            throw LOGGER
                .logExceptionAsError(new IllegalArgumentException("'baseDelay' cannot be greater than 'maxDelay'."));
        }
        this.maxRetries = maxRetries;
        this.baseDelayNanos = baseDelay.toNanos();
        this.maxDelayNanos = maxDelay.toNanos();
        this.shouldRetryCondition = shouldRetryCondition;
    }

    @Override
    public int getMaxRetries() {
        return maxRetries;
    }

    @Override
    public Duration calculateRetryDelay(int retryAttempts) {
        // Introduce a small amount of jitter to base delay
        long delayWithJitterInNanos = ThreadLocalRandom.current()
            .nextLong((long) (baseDelayNanos * (1 - JITTER_FACTOR)), (long) (baseDelayNanos * (1 + JITTER_FACTOR)));
        return Duration.ofNanos(Math.min((1L << retryAttempts) * delayWithJitterInNanos, maxDelayNanos));
    }

    @Override
    public boolean shouldRetryCondition(RequestRetryCondition requestRetryCondition) {
        return shouldRetryCondition == null
            ? RetryStrategy.super.shouldRetryCondition(requestRetryCondition)
            : shouldRetryCondition.test(requestRetryCondition);
    }
}
