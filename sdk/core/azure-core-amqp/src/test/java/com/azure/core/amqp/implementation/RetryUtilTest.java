// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ExponentialAmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
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
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryUtilTest {
    @Test
    void getCorrectModeFixed() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED);
        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        // Assert
        Assertions.assertNotNull(retryPolicy);
        assertEquals(FixedAmqpRetryPolicy.class, retryPolicy.getClass());
    }

    @Test
    void getCorrectModeExponential() {
        // Act
        final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.EXPONENTIAL);
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
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds())
            .plus(timeout);

        final AtomicInteger resubscribe = new AtomicInteger();
        final Flux<AmqpTransportType> neverFlux = Flux.<AmqpTransportType>never()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertTrue(error.getCause() instanceof TimeoutException))
            .verify();

        assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }

    /**
     * Tests a retry that times out on a Flux.
     */
    @Test
    void withRetryFluxEmitsItemsLaterThanTimeout() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofMillis(500);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        final AtomicInteger resubscribe = new AtomicInteger();
        final TestPublisher<AmqpTransportType> singleItem = TestPublisher.create();

        final Flux<AmqpTransportType> flux = singleItem.flux()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(flux, options, timeoutMessage))
            .expectSubscription()
            .then(() -> singleItem.next(AmqpTransportType.AMQP_WEB_SOCKETS))
            .expectNext(AmqpTransportType.AMQP_WEB_SOCKETS)
            .expectNoEvent(totalWaitTime)
            .thenCancel()
            .verify();

        assertEquals(1, resubscribe.get());
    }

    /**
     * Tests a retry that times out on a Mono.
     */
    @Test
    void withRetryMono() {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofMillis(500);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        final AtomicInteger resubscribe = new AtomicInteger();
        final Mono<AmqpTransportType> neverFlux = TestPublisher.<AmqpTransportType>create().mono()
            .doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertTrue(error.getCause() instanceof TimeoutException))
            .verify();

        assertEquals(options.getMaxRetries() + 1, resubscribe.get());
    }

    static Stream<Throwable> withTransientError() {
        return Stream.of(
            new AmqpException(true, "Test-exception", new AmqpErrorContext("test-ns")),
            new TimeoutException("Test-timeout")
        );
    }

    @ParameterizedTest
    @MethodSource
    void withTransientError(Throwable transientError) {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofSeconds(30);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(1)
            .setTryTimeout(timeout);
        final AtomicBoolean wasSent = new AtomicBoolean();

        final Flux<Integer> stream = Flux.concat(
            Flux.just(0, 1),
            Flux.create(sink -> {
                if (wasSent.getAndSet(true)) {
                    sink.next(10);
                    sink.complete();
                } else {
                    sink.error(transientError);
                }
            }),
            Flux.just(3, 4));

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

    static Stream<Throwable> withNonTransientError() {
        return Stream.of(
            new AmqpException(false, "Test-exception", new AmqpErrorContext("test-ns")),
            new IllegalStateException("Some illegal State")
        );
    }

    @ParameterizedTest
    @MethodSource
    void withNonTransientError(Throwable nonTransientError) {
        // Arrange
        final String timeoutMessage = "Operation timed out.";
        final Duration timeout = Duration.ofSeconds(30);
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(1)
            .setTryTimeout(timeout);

        final Flux<Integer> stream = Flux.concat(
            Flux.just(0, 1, 2),
            Flux.error(nonTransientError),
            Flux.just(3, 4));

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(stream, options, timeoutMessage))
            .expectNext(0, 1, 2)
            .expectErrorMatches(error -> error.equals(nonTransientError))
            .verify();
    }

    static Stream<AmqpRetryOptions> createRetry() {
        final AmqpRetryOptions fixed = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(10))
            .setMaxRetries(2)
            .setMaxDelay(Duration.ofSeconds(90));
        final AmqpRetryOptions exponential = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.EXPONENTIAL)
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
        return Stream.of(
            Arguments.of(new TimeoutException("Something"), true),
            Arguments.of(new AmqpException(true, "foo message", new AmqpErrorContext("test-namespace")), true),
            Arguments.of(new AmqpException(false, "foo message", new AmqpErrorContext("test-ns")), false),
            Arguments.of(new IllegalArgumentException("invalid"), false)
        );
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
}
