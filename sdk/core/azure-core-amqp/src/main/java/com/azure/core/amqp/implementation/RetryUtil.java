// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Publisher;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

/**
 * Helper class to help with retry policies.
 */
public final class RetryUtil {
    private static final ClientLogger LOGGER = new ClientLogger(RetryUtil.class);

    // So this class can't be instantiated.
    private RetryUtil() {
    }

    /**
     * Check if the existing exception is a retriable exception.
     *
     * @param exception An exception that was observed for the operation to be retried.
     * @return true if the exception is a retriable exception, otherwise false.
     */
    public static boolean isRetriableException(Throwable exception) {
        return (exception instanceof AmqpException) && ((AmqpException) exception).isTransient();
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
     * Given a {@link Mono} will apply the retry policy to it when the operation times out or throws a transient error.
     *
     * @param <T> Type of value in the {@link Mono}.
     * @param source The publisher to apply the retry policy to.
     * @param retryOptions A {@link AmqpRetryOptions}.
     * @param errorMessage Text added to error logs.
     * @param allowsLongOperation A boolean value indicating whether to allow the {@param source} to run long time
     *  and not to timeout it. If it's false, a {@link TimeoutException} will be thrown if the {@param source} doesn't
     *  complete before the {@code getTryTimeout()} of {@param retryOptions}.
     *
     * @return A publisher that returns the results of the {@link Mono} if any of the retry attempts are successful.
     *     Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Mono<T> withRetry(Mono<T> source, AmqpRetryOptions retryOptions, String errorMessage,
        boolean allowsLongOperation) {
        if (!allowsLongOperation) {
            source = source.timeout(retryOptions.getTryTimeout());
        }
        return source.retryWhen(createRetry(retryOptions)).doOnError(error -> LOGGER.error(errorMessage, error));
    }

    /**
     * Given a {@link Flux} will apply the retry policy to it when the operation times out.
     *
     * @param <T> Type of value in the {@link Flux}.
     * @param source The publisher to apply the retry policy to.
     * @param retryOptions A {@link AmqpRetryOptions}.
     * @param timeoutMessage Text added to error logs.
     * @return A publisher that returns the results of the {@link Flux} if any of the retry attempts are successful.
     *     Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Flux<T> withRetry(Flux<T> source, AmqpRetryOptions retryOptions, String timeoutMessage) {
        return source.timeout(retryOptions.getTryTimeout())
            .retryWhen(createRetry(retryOptions))
            .doOnError(error -> LOGGER.error(timeoutMessage, error));
    }

    /**
     * Given a {@link Mono} will apply the retry policy to it when the operation times out.
     *
     * @param <T> Type of value in the {@link Mono}.
     * @param source The publisher to apply the retry policy to.
     * @param retryOptions A {@link AmqpRetryOptions}.
     * @param timeoutMessage Text added to error logs.
     * @return A publisher that returns the results of the {@link Flux} if any of the retry attempts are successful.
     *     Otherwise, propagates a {@link TimeoutException}.
     */
    public static <T> Mono<T> withRetry(Mono<T> source, AmqpRetryOptions retryOptions, String timeoutMessage) {
        return withRetry(source, retryOptions, timeoutMessage, false);
    }

    /**
     * Creates the Retry strategy from the AmqpRetryOptions.
     *
     * @param options AmqpRetryOptions.
     * @return The retry strategy.
     */
    static Retry createRetry(AmqpRetryOptions options) {
        return new AmqpRetrySpec(options);
    }

    /**
     * {@link AmqpRetryPolicy} wrapped as a Retry.
     */
    static class AmqpRetrySpec extends Retry {
        private final AmqpRetryPolicy retryPolicy;

        AmqpRetrySpec(AmqpRetryOptions options) {
            switch (options.getMode()) {
                case FIXED:
                    retryPolicy = new FixedAmqpRetryPolicy(options);
                    break;

                case EXPONENTIAL:
                    retryPolicy = new ExponentialAmqpRetryPolicy(options);
                    break;

                default:
                    LOGGER.atWarning()
                        .addKeyValue("retryMode", options.getMode())
                        .addKeyValue("delay", options.getDelay())
                        .addKeyValue("maxDelay", options.getMaxDelay())
                        .addKeyValue("maxRetries", options.getMaxRetries())
                        .log("Unknown retry mode. Using Exponential.");

                    retryPolicy = new ExponentialAmqpRetryPolicy(options);
                    break;
            }
        }

        @Override
        public Publisher<?> generateCompanion(Flux<RetrySignal> retrySignals) {
            return retrySignals.concatMap(retrySignal -> {
                final RetrySignal copy = retrySignal.copy();
                final Throwable currentFailure = copy.failure();

                final Duration retryDelay
                    = retryPolicy.calculateRetryDelay(currentFailure, (int) copy.totalRetriesInARow());

                if (retryDelay != null) {
                    return Mono.delay(retryDelay);
                }

                if (isRetriableException(currentFailure)) {
                    return Mono.error(Exceptions.retryExhausted("Retries exhausted.", currentFailure));
                } else {
                    return Mono.error(currentFailure);
                }
            }).onErrorStop();
        }
    }
}
