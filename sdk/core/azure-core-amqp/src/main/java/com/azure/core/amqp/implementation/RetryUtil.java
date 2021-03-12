// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Helper class to help with retry policies.
 */
public final class RetryUtil {
    private static final double JITTER_FACTOR = 0.08;
    // Base sleep wait time.
    private static final Duration SERVER_BUSY_WAIT_TIME = Duration.ofSeconds(4);

    private static final ClientLogger LOGGER = new ClientLogger(RetryUtil.class);

    // So this class can't be instantiated.
    private RetryUtil() {
    }

    /**
     * Given a set of {@link AmqpRetryOptions options}, creates the appropriate retry policy.
     *
     * @param options A set of options used to configure the retry policy.
     *
     * @return A new retry policy configured with the given {@code options}.
     * @throws IllegalArgumentException If {@link AmqpRetryOptions#getMode()} is not a supported mode.
     */
    public static AmqpRetryPolicy getRetryPolicy(AmqpRetryOptions options) {
        switch (options.getMode()) {
            case FIXED:
                return new FixedAmqpRetryPolicy(options);
            case EXPONENTIAL:
                return new ExponentialAmqpRetryPolicy(options);
            default:
                throw new IllegalArgumentException(
                    String.format(Locale.ROOT, "Mode is not supported: %s", options.getMode()));
        }
    }

    /**
     * Given a {@link Flux} will apply the retry policy to it when the operation times out.
     *
     * @param source The publisher to apply the retry policy to.
     *
     * @return A publisher that returns the results of the {@link Flux} if any of the retry attempts are successful.
     *     Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Flux<T> withRetry(Flux<T> source, AmqpRetryOptions retryOptions, String timeoutMessage) {
        return source
            .timeout(Mono.delay(retryOptions.getTryTimeout()))
            .retryWhen(createRetry(retryOptions))
            .doOnError(error -> LOGGER.error(timeoutMessage, error));
    }

    /**
     * Given a {@link Mono} will apply the retry policy to it when the operation times out.
     *
     * @param source The publisher to apply the retry policy to.
     *
     * @return A publisher that returns the results of the {@link Flux} if any of the retry attempts are successful.
     *     Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Mono<T> withRetry(Mono<T> source, AmqpRetryOptions retryOptions, String timeoutMessage) {
        return source
            .timeout(Mono.delay(retryOptions.getTryTimeout()))
            .retryWhen(createRetry(retryOptions))
            .doOnError(error -> LOGGER.error(timeoutMessage, error));
    }

    static Retry createRetry(AmqpRetryOptions options) {
        final Duration delay = options.getDelay().plus(SERVER_BUSY_WAIT_TIME);
        final RetryBackoffSpec retrySpec;
        switch (options.getMode()) {
            case FIXED:
                retrySpec = Retry.fixedDelay(options.getMaxRetries(), delay);
                break;
            case EXPONENTIAL:
                retrySpec = Retry.backoff(options.getMaxRetries(), delay);
                break;
            default:
                LOGGER.warning("Unknown: '{}'. Using exponential delay. Delay: {}. Max Delay: {}. Max Retries: {}.",
                    options.getMode(), options.getDelay(), options.getMaxDelay(), options.getMaxRetries());
                retrySpec = Retry.backoff(options.getMaxRetries(), delay);
                break;
        }

        return retrySpec.jitter(JITTER_FACTOR)
            .maxBackoff(options.getMaxDelay())
            .filter(error -> error instanceof TimeoutException
                || (error instanceof AmqpException && ((AmqpException) error).isTransient()));
    }
}
