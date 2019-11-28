// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Helper class to help with retry policies.
 */
public class RetryUtil {
    // So this class can't be instantiated.
    private RetryUtil() {
    }

    /**
     * Given a set of {@link AmqpRetryOptions options}, creates the appropriate retry policy.
     *
     * @param options A set of options used to configure the retry policy.
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
     * @return A publisher that returns the results of the {@link Flux} if any of the retry attempts are successful.
     *         Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Flux<T> withRetry(Flux<T> source, Duration operationTimeout, AmqpRetryPolicy retryPolicy) {
        return Flux.defer(() -> source.timeout(operationTimeout))
            .retryWhen(errors -> retry(errors, retryPolicy));
    }

    /**
     * Given a {@link Mono} will apply the retry policy to it when the operation times out.
     *
     * @param source The publisher to apply the retry policy to.
     * @return A publisher that returns the results of the {@link Flux} if any of the retry attempts are successful.
     *         Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Mono<T> withRetry(Mono<T> source, Duration operationTimeout, AmqpRetryPolicy retryPolicy) {
        return Mono.defer(() -> source.timeout(operationTimeout))
            .retryWhen(errors -> retry(errors, retryPolicy));
    }

    private static Flux<Long> retry(Flux<Throwable> source, AmqpRetryPolicy retryPolicy) {
        return source.zipWith(Flux.range(1, retryPolicy.getMaxRetries() + 1),
            (error, attempt) -> {
                if (!(error instanceof TimeoutException) || attempt > retryPolicy.getMaxRetries()) {
                    throw Exceptions.propagate(error);
                }

                //TODO (conniey): is it possible to add a logger here even though it is static? :/
                return retryPolicy.calculateRetryDelay((TimeoutException) error, attempt);
            })
            .flatMap(Mono::delay);
    }
}
