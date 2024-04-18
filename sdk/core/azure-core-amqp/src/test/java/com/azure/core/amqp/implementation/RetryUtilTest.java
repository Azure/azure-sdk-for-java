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
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static com.azure.core.amqp.implementation.ClientConstants.SERVER_BUSY_WAIT_TIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
        final AmqpRetryOptions options = new AmqpRetryOptions()
            .setDelay(Duration.ofSeconds(1))
            .setMode(AmqpRetryMode.FIXED)
            .setMaxRetries(2)
            .setTryTimeout(timeout);
        final Duration totalWaitTime
            = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds()).plus(timeout);

        // + 1: First subscription to the Flux.
        // + 1: AmqpRetryPolicy uses > rather than >=, so there is one more retry than in RetrySpec.
        final int expectedNumberOfSubscribes = options.getMaxRetries() + 1 + 1;

        final AtomicInteger resubscribe = new AtomicInteger();
        final Flux<AmqpTransportType> neverFlux
            = Flux.<AmqpTransportType>never().doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertInstanceOf(TimeoutException.class, error))
            .verify();

        assertEquals(expectedNumberOfSubscribes, resubscribe.get());
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
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(timeout);
        final Duration totalWaitTime = Duration.ofSeconds(options.getMaxRetries() * options.getDelay().getSeconds());

        // + 1: First subscription to the Mono.
        // + 1: AmqpRetryPolicy uses > rather than >=, so there is one more retry than in RetrySpec.
        final int expectedNumberOfSubscribes = options.getMaxRetries() + 1 + 1;

        final AtomicInteger resubscribe = new AtomicInteger();
        final Mono<AmqpTransportType> neverFlux
            = TestPublisher.<AmqpTransportType>create().mono().doOnSubscribe(s -> resubscribe.incrementAndGet());

        // Act & Assert
        StepVerifier.create(RetryUtil.withRetry(neverFlux, options, timeoutMessage))
            .expectSubscription()
            .thenAwait(totalWaitTime)
            .expectErrorSatisfies(error -> assertInstanceOf(TimeoutException.class, error))
            .verify();

        assertEquals(expectedNumberOfSubscribes, resubscribe.get());
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

    /**
     * Verifies that the retry calculated from {@link RetryUtil#withRetry(Flux, AmqpRetryOptions, String)}
     * matches existing behaviour of {@link AmqpRetryPolicy}.
     */
    @Test
    void testRetrySpecServerBusy() {
        // Arrange
        final AmqpRetryOptions fixedOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setMaxDelay(Duration.ofSeconds(15));
        final Retry actualRetry = RetryUtil.createRetry(fixedOptions);

        final Throwable exception = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Message",
            new AmqpErrorContext("namespace-foo-bar"));
        final Retry.RetrySignal retrySignal = new ImmutableRetrySignal(0, 0, exception);

        // Act & Assert
        final TestPublisher<Retry.RetrySignal> publisher = TestPublisher.createCold();

        StepVerifier.create(actualRetry.generateCompanion(publisher.flux()))
            .then(() -> publisher.emit(retrySignal))
            .expectNoEvent(SERVER_BUSY_WAIT_TIME)
            .expectNextCount(1)
            .expectComplete()
            .verify();
    }

    static Stream<Throwable> testRetrySpecNonServerBusyExceptions() {
        return Stream.of(
            new AmqpException(true, AmqpErrorCondition.INTERNAL_ERROR, "Message",
                new AmqpErrorContext("namespace-foo-bar")),
            new TimeoutException("Timeout exception test.")
        );
    }

    /**
     * Verifies errors that are not {@link AmqpErrorCondition#SERVER_BUSY_ERROR} do not have the server busy wait time
     * applied to them.
     */
    @MethodSource
    @ParameterizedTest
    void testRetrySpecNonServerBusyExceptions(Throwable exception) {
        // Arrange
        // The delay is 4x longer than the SERVER_BUSY_WAIT_TIME.
        final AmqpRetryOptions fixedOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setMaxDelay(Duration.ofSeconds(15));
        final Retry actualRetry = RetryUtil.createRetry(fixedOptions);

        final Retry.RetrySignal retrySignal = new ImmutableRetrySignal(0, 0, exception);

        // Act & Assert
        final TestPublisher<Retry.RetrySignal> publisher = TestPublisher.createCold();

        // Set an explicit timeout because the server busy time is 4x longer than the delay.   We shouldn't be
        // adding the SERVER_BUSY_WAIT_TIME to non-server busy errors.
        StepVerifier.create(actualRetry.generateCompanion(publisher.flux()))
            .then(() -> publisher.emit(retrySignal))
            .expectNextCount(1)
            .expectComplete()
            .verify(SERVER_BUSY_WAIT_TIME);
    }

    /**
     * Error is returned if retries are exhausted.
     */
    @Test
    void testRetryExhausted() {
        // Arrange
        final AmqpRetryOptions fixedOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setMaxDelay(Duration.ofSeconds(15));
        final Throwable exception = new AmqpException(true, AmqpErrorCondition.INTERNAL_ERROR, "Message",
            new AmqpErrorContext("namespace-foo-bar"));
        final Retry actualRetry = RetryUtil.createRetry(fixedOptions);
        final int retries = fixedOptions.getMaxRetries() + 1;
        final Retry.RetrySignal retrySignal = new ImmutableRetrySignal(retries, retries, exception);

        // Act & Assert
        final TestPublisher<Retry.RetrySignal> publisher = TestPublisher.createCold();

        // Set an explicit timeout because the server busy time is 4x longer than the delay.   We shouldn't be
        // adding the SERVER_BUSY_WAIT_TIME to non-server busy errors.
        StepVerifier.create(actualRetry.generateCompanion(publisher.flux()))
            .then(() -> publisher.emit(retrySignal))
            .expectError()
            .verify();
    }

    static Stream<Throwable> testRetryWhenExceptionDoesNotMatch() {
        return Stream.of(
            new AmqpException(false, AmqpErrorCondition.INTERNAL_ERROR, "Message",
                new AmqpErrorContext("namespace-foo-bar")),
            new IllegalArgumentException("Illegal state exception")
        );
    }

    /**
     * Error is returned when the exception is an AmqpException that is not retriable nor is it a TimeoutException.
     */
    @MethodSource
    @ParameterizedTest
    void testRetryWhenExceptionDoesNotMatch(Throwable exception) {
        // Arrange
        final AmqpRetryOptions fixedOptions = new AmqpRetryOptions()
            .setMode(AmqpRetryMode.FIXED)
            .setDelay(Duration.ofSeconds(1))
            .setMaxRetries(2)
            .setMaxDelay(Duration.ofSeconds(15));
        final Retry actualRetry = RetryUtil.createRetry(fixedOptions);
        final int retries = fixedOptions.getMaxRetries() + 1;
        final Retry.RetrySignal retrySignal = new ImmutableRetrySignal(retries, retries, exception);

        // Act & Assert
        final TestPublisher<Retry.RetrySignal> publisher = TestPublisher.createCold();

        // Set an explicit timeout because the server busy time is 4x longer than the delay.   We shouldn't be
        // adding the SERVER_BUSY_WAIT_TIME to non-server busy errors.
        StepVerifier.create(actualRetry.generateCompanion(publisher.flux()))
            .then(() -> publisher.emit(retrySignal))
            .expectError()
            .verify();
    }

    private static class ImmutableRetrySignal implements Retry.RetrySignal {
        private final int totalRetries;
        private final int totalRetriesInARow;
        private final Throwable throwable;

        ImmutableRetrySignal(int totalRetries, int totalRetriesInARow, Throwable throwable) {
            this.totalRetries = totalRetries;
            this.totalRetriesInARow = totalRetriesInARow;
            this.throwable = throwable;
        }

        @Override
        public long totalRetries() {
            return totalRetries;
        }

        @Override
        public long totalRetriesInARow() {
            return totalRetriesInARow;
        }

        @Override
        public Throwable failure() {
            return throwable;
        }
    }
}
