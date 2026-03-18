// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryUtilTest {
    @Test
    void getCorrectModeFixed() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMode(AmqpRetryMode.FIXED);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        assertEquals(FixedAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    void getCorrectModeExponential() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions().setMode(AmqpRetryMode.EXPONENTIAL);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        assertEquals(ExponentialAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    /**
     * Tests a retry that times out on a Flux.
     */
    @Test
    void withRetryFlux() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofMillis(1500);
        final AmqpRetryOptions options
            = new AmqpRetryOptions().setDelay(Duration.ofSeconds(1)).setMaxRetries(2).setTryTimeout(timeout);
        final Duration totalWaitTime
            = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds()).plus(timeout);

        final AtomicInteger resubscribe = new AtomicInteger();
        final Flux<AmqpTransportType> neverFlux
            = Flux.<AmqpTransportType>never().doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertTrue(error.getCause() instanceof TimeoutException))
            .verify();

        assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }

    /**
     * Tests a retry that times out on a Mono.
     */
    @Test
    void withRetryMono() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofMillis(500);
        final AmqpRetryOptions options
            = new AmqpRetryOptions().setDelay(Duration.ofSeconds(1)).setMaxRetries(2).setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        final AtomicInteger resubscribe = new AtomicInteger();
        final Mono<AmqpTransportType> neverFlux
            = TestPublisher.<AmqpTransportType>create().mono().doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertTrue(error.getCause() instanceof TimeoutException))
            .verify();

        assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }

    static Stream<Throwable> withTransientError() {
        return Stream.of(new AmqpException(true, "Test-exception", new AmqpErrorContext("test-ns")),
            new TimeoutException("Test-timeout"));
    }

    @ParameterizedTest
    @MethodSource
    void withTransientError(Throwable transientError) {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofSeconds(30);
        final AmqpRetryOptions options = new AmqpRetryOptions().setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(1)
            .setTryTimeout(timeout);
        final AtomicBoolean wasSent = new AtomicBoolean();

        final Flux<Integer> stream = Flux.concat(Flux.just(0, 1), Flux.create(sink -> {
            if (wasSent.getAndSet(true)) {
                sink.next(10);
                sink.complete();
            } else {
                sink.error(transientError);
            }
        }), Flux.just(3, 4));

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(stream, options, timeoutMessage))
            .expectNext(0, 1)
            // AmqpException occurs and then we have the retry.
            .expectNext(0, 1)
            .expectNext(10)
            .expectNext(3, 4)
            .expectComplete()
            .verify();
    }

    static Stream<AmqpRetryOptions> createRetry() {
        final AmqpRetryOptions fixed = new AmqpRetryOptions().setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(10))
            .setMaxRetries(2)
            .setMaxDelay(Duration.ofSeconds(90));
        final AmqpRetryOptions exponential = new AmqpRetryOptions().setMode(AmqpRetryMode.EXPONENTIAL)
            .setDelay(Duration.ofSeconds(5))
            .setMaxRetries(5)
            .setMaxDelay(Duration.ofSeconds(35));

        return Stream.of(fixed, exponential);
    }

    /**
     * Verifies retry options are correctly mapped to a retry spec.
     */
    @MethodSource
    @ParameterizedTest
    void createRetry(AmqpRetryOptions options) {
        // Act
        final Retry actual = RetryUtil.createRetry(options);

        // Assert
        assertTrue(actual instanceof RetryBackoffSpec);

        final RetryBackoffSpec retrySpec = (RetryBackoffSpec) actual;
        assertEquals(options.getMaxRetries(), retrySpec.maxAttempts);
        assertEquals(options.getMaxDelay(), retrySpec.maxBackoff);
        assertTrue(options.getDelay().compareTo(retrySpec.minBackoff) < 0);
        assertTrue(retrySpec.jitterFactor > 0);
    }

    static Stream<Arguments> retryFilter() {
        return Stream.of(Arguments.of(new TimeoutException("Something"), true),
            Arguments.of(new AmqpException(true, "foo message", new AmqpErrorContext("test-namespace")), true),
            Arguments.of(new AmqpException(false, "foo message", new AmqpErrorContext("test-ns")), false),
            Arguments.of(new IllegalArgumentException("invalid"), false));
    }

    @MethodSource
    @ParameterizedTest
    void retryFilter(Throwable throwable, boolean expected) {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions().setMode(AmqpRetryMode.EXPONENTIAL);
        final Retry retry = RetryUtil.createRetry(options);

        assertTrue(retry instanceof RetryBackoffSpec);

        final RetryBackoffSpec retrySpec = (RetryBackoffSpec) retry;
        final Predicate<Throwable> errorFilter = retrySpec.errorFilter;

        // Act
        final boolean actual = errorFilter.test(throwable);

        // Assert
        assertEquals(expected, actual);
    }

    // ---- createRetryWithRecovery tests ----

    /**
     * FATAL errors must not be retried and must not invoke the recovery callback.
     */
    @Test
    void createRetryWithRecovery_fatalErrorTerminatesImmediately() {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions().setMaxRetries(3).setDelay(Duration.ofMillis(100));
        final AtomicInteger recoveryCount = new AtomicInteger();
        final Retry retry = RetryUtil.createRetryWithRecovery(options, kind -> recoveryCount.incrementAndGet());
        final AmqpException fatalError = new AmqpException(false, AmqpErrorCondition.NOT_FOUND, "not found", null);

        // Act & Assert
        StepVerifier.create(Mono.<Integer>error(fatalError).retryWhen(retry))
            .expectErrorSatisfies(e -> assertEquals(fatalError, e))
            .verify(Duration.ofSeconds(5));

        assertEquals(0, recoveryCount.get(), "Recovery callback must not be called for FATAL errors");
    }

    /**
     * LINK errors must invoke the recovery callback and retry. The first occurrence uses
     * the quick-retry path (no backoff delay).
     */
    @Test
    void createRetryWithRecovery_linkErrorInvokesRecoveryAndRetries() {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions().setMaxRetries(3).setDelay(Duration.ofMillis(100));
        final List<RecoveryKind> recoveries = new ArrayList<>();
        final Retry retry = RetryUtil.createRetryWithRecovery(options, recoveries::add);
        final AtomicInteger attempt = new AtomicInteger();
        final Mono<Integer> source = Mono.defer(() -> {
            if (attempt.getAndIncrement() < 2) {
                return Mono.error(new AmqpException(true, AmqpErrorCondition.LINK_DETACH_FORCED, "detach", null));
            }
            return Mono.just(42);
        });

        // Act & Assert — use virtual time because the second retry applies a SERVER_BUSY backoff delay.
        StepVerifier.withVirtualTime(() -> source.retryWhen(retry))
            .expectSubscription()
            .thenAwait(Duration.ofMinutes(1))
            .expectNext(42)
            .expectComplete()
            .verify(Duration.ofSeconds(5));

        assertEquals(2, recoveries.size(), "Recovery callback called once per LINK error");
        assertTrue(recoveries.stream().allMatch(k -> k == RecoveryKind.LINK));
    }

    /**
     * CONNECTION errors must invoke the recovery callback with CONNECTION kind.
     */
    @Test
    void createRetryWithRecovery_connectionErrorInvokesRecovery() {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions().setMaxRetries(2).setDelay(Duration.ofMillis(100));
        final AtomicReference<RecoveryKind> capturedKind = new AtomicReference<>();
        final Retry retry = RetryUtil.createRetryWithRecovery(options, capturedKind::set);
        final AtomicInteger attempt = new AtomicInteger();
        final Mono<Integer> source = Mono.defer(() -> {
            if (attempt.getAndIncrement() == 0) {
                return Mono.error(new AmqpException(true, AmqpErrorCondition.CONNECTION_FORCED, "forced", null));
            }
            return Mono.just(1);
        });

        // Act & Assert
        StepVerifier.create(source.retryWhen(retry)).expectNext(1).expectComplete().verify(Duration.ofSeconds(5));

        assertEquals(RecoveryKind.CONNECTION, capturedKind.get(),
            "Recovery callback must receive CONNECTION kind for connection errors");
    }

    /**
     * After the retry budget is exhausted the error must propagate without further retries.
     */
    @Test
    void createRetryWithRecovery_exhaustedRetriesTerminateWithError() {
        // Arrange
        final int maxRetries = 2;
        final AmqpRetryOptions options
            = new AmqpRetryOptions().setMaxRetries(maxRetries).setDelay(Duration.ofMillis(10));
        final AtomicInteger recoveryCount = new AtomicInteger();
        final Retry retry = RetryUtil.createRetryWithRecovery(options, kind -> recoveryCount.incrementAndGet());
        final AmqpException transientError
            = new AmqpException(true, AmqpErrorCondition.LINK_DETACH_FORCED, "detach", null);

        // Act & Assert — use virtual time because the second retry applies a SERVER_BUSY backoff delay.
        StepVerifier.withVirtualTime(() -> Mono.<Integer>error(transientError).retryWhen(retry))
            .expectSubscription()
            .thenAwait(Duration.ofMinutes(1))
            .expectError(AmqpException.class)
            .verify(Duration.ofSeconds(5));

        // Recovery called on each retry attempt (not the final one which terminates)
        assertEquals(maxRetries, recoveryCount.get(), "Recovery callback called once per non-terminal retry");
    }

    /**
     * A NONE-kind failure (server-busy) before the first LINK failure must not consume the
     * quick-retry flag. The first LINK failure should still trigger the quick-retry optimization
     * (no backoff delay). Prior to the T13 fix, {@code didQuickRetry.getAndSet(true)} was evaluated
     * unconditionally; this test verifies the kind check comes first.
     */
    @Test
    void createRetryWithRecovery_noneFailureBeforeLinkPreservesQuickRetry() {
        // Arrange
        final AmqpRetryOptions options = new AmqpRetryOptions().setMaxRetries(3).setDelay(Duration.ofMillis(100));
        final List<RecoveryKind> recoveries = new ArrayList<>();
        final Retry retry = RetryUtil.createRetryWithRecovery(options, recoveries::add);
        final AtomicInteger attempt = new AtomicInteger();
        final Mono<Integer> source = Mono.defer(() -> {
            switch (attempt.getAndIncrement()) {
                case 0:
                    // NONE kind — server-busy; should not consume the quick-retry flag.
                    return Mono.error(new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "busy", null));

                case 1:
                    // LINK kind — first occurrence; flag must still be available → quick-retry fires.
                    return Mono.error(new AmqpException(true, AmqpErrorCondition.LINK_DETACH_FORCED, "detach", null));

                default:
                    return Mono.just(99);
            }
        });

        // Act & Assert — virtual time to skip the SERVER_BUSY backoff; LINK quick-retry fires without delay.
        StepVerifier.withVirtualTime(() -> source.retryWhen(retry))
            .expectSubscription()
            .thenAwait(Duration.ofMinutes(1))
            .expectNext(99)
            .expectComplete()
            .verify(Duration.ofSeconds(5));

        // NONE failures do not invoke recovery; only LINK does.
        assertEquals(1, recoveries.size(), "Only the LINK failure should invoke recovery");
        assertEquals(RecoveryKind.LINK, recoveries.get(0),
            "Recovery callback must be called with LINK kind for the detach error");
    }
}
