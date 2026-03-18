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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
     * Applies the retry policy with tiered recovery between attempts. Before each retry,
     * the error is classified via {@link RecoveryKind#classify(Throwable)} and the recovery
     * callback is invoked so the caller can close the appropriate resources (link or connection).
     *
     * <p>This matches the tiered recovery pattern used by the Go, .NET, Python, and JS SDKs.</p>
     *
     * @param <T> Type of value in the {@link Mono}.
     * @param source The publisher to apply the retry policy to.
     * @param retryOptions A {@link AmqpRetryOptions}.
     * @param errorMessage Text added to error logs.
     * @param recoveryAction Called between retry attempts with the classified {@link RecoveryKind}.
     *     The caller should close the link (for {@link RecoveryKind#LINK}) or connection
     *     (for {@link RecoveryKind#CONNECTION}) so the next retry creates fresh resources.
     *
     * @return A publisher that returns the results of the {@link Mono} if any of the retry attempts
     *     are successful. Otherwise, propagates the last error.
     */
    public static <T> Mono<T> withRetryAndRecovery(Mono<T> source, AmqpRetryOptions retryOptions, String errorMessage,
        Consumer<RecoveryKind> recoveryAction) {
        return withRetryAndRecovery(source, retryOptions, errorMessage, false, recoveryAction);
    }

    /**
     * Like {@link #withRetryAndRecovery(Mono, AmqpRetryOptions, String, Consumer)} but with an option to allow
     * long-running operations that should not be subject to the per-attempt timeout.
     *
     * @param allowsLongOperation If true, the source Mono will not be wrapped with a per-attempt timeout.
     */
    public static <T> Mono<T> withRetryAndRecovery(Mono<T> source, AmqpRetryOptions retryOptions, String errorMessage,
        boolean allowsLongOperation, Consumer<RecoveryKind> recoveryAction) {
        if (!allowsLongOperation) {
            source = source.timeout(retryOptions.getTryTimeout());
        }
        return source.retryWhen(createRetryWithRecovery(retryOptions, recoveryAction))
            .doOnError(error -> LOGGER.error(errorMessage, error));
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

    /**
     * Creates a Reactor {@link Retry} spec that performs tiered recovery between retry attempts.
     * Before each retry, the error is classified and the recovery callback is invoked.
     *
     * <p>Includes a quick-retry optimization matching the Go SDK: on the first LINK or CONNECTION
     * error, the retry fires immediately (no backoff) since the error may come from a previously
     * stale link and recovery has just created a fresh one.</p>
     */
    static Retry createRetryWithRecovery(AmqpRetryOptions options, Consumer<RecoveryKind> recoveryAction) {
        final int maxRetries = options.getMaxRetries();
        final Duration baseDelay = options.getDelay().plus(SERVER_BUSY_WAIT_TIME);
        final Duration maxDelay = options.getMaxDelay();
        final boolean isFixed = options.getMode() == com.azure.core.amqp.AmqpRetryMode.FIXED;
        final AtomicBoolean didQuickRetry = new AtomicBoolean(false);

        return Retry.from(retrySignals -> retrySignals.flatMap(signal -> {
            final Throwable failure = signal.failure();
            final long attempt = signal.totalRetriesInARow();
            final RecoveryKind kind = RecoveryKind.classify(failure);

            // FATAL errors — do not retry.
            if (kind == RecoveryKind.FATAL) {
                return Mono.<Long>error(failure);
            }

            // Check retry budget.
            if (attempt >= maxRetries) {
                return Mono.<Long>error(failure);
            }

            // Perform recovery before retry.
            if (kind != RecoveryKind.NONE && recoveryAction != null) {
                try {
                    recoveryAction.accept(kind);
                } catch (Exception e) {
                    LOGGER.atWarning().log("Recovery action failed.", e);
                }
            }

            // Quick retry: on the FIRST LINK/CONNECTION error, retry immediately (no backoff).
            // Uses didQuickRetry flag to prevent repeated immediate retries under persistent
            // errors — similar to the Go SDK's didQuickRetry pattern. Unlike Go's ResetAttempts(),
            // the attempt counter is not reset here; subsequent retries continue with standard
            // exponential backoff from the current attempt count.
            if (!didQuickRetry.getAndSet(true) && (kind == RecoveryKind.LINK || kind == RecoveryKind.CONNECTION)) {
                LOGGER.atInfo().log("Quick retry after {} recovery (first occurrence).", kind);
                return Mono.just(attempt);
            }

            // Standard backoff delay.
            final Duration delay;
            if (isFixed) {
                // Cap baseDelay to maxDelay so FIXED mode respects retryOptions.getMaxDelay().
                delay = baseDelay.compareTo(maxDelay) > 0 ? maxDelay : baseDelay;
            } else {
                long millis = baseDelay.toMillis() * (1L << Math.min(attempt, 30));
                delay = Duration.ofMillis(Math.min(millis, maxDelay.toMillis()));
            }
            final double jitter = 1.0 + (ThreadLocalRandom.current().nextDouble() * 2 - 1) * JITTER_FACTOR;
            // Clamp the final jittered delay to maxDelay so retryOptions are consistently respected.
            final Duration jitteredDelay
                = Duration.ofMillis(Math.min((long) (delay.toMillis() * jitter), maxDelay.toMillis()));

            return Mono.delay(jitteredDelay).thenReturn(attempt);
        }));
    }
}
