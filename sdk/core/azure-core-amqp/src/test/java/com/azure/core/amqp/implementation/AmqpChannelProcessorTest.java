// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.FixedAmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AmqpChannelProcessor}.
 */
class AmqpChannelProcessorTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(30);
    private final TestObject connection1 = new TestObject();
    private final TestObject connection2 = new TestObject();
    private final TestObject connection3 = new TestObject();

    /**
     * Verifies that we can get a new connection. This new connection is only emitted when the endpoint state is
     * active.
     */
    @Test
    void createsNewConnection() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor = publisher.flux().subscribeWith(createChannelProcessor());

        try {
            // Act & Assert
            StepVerifier.create(processor)
                .then(() -> publisher.next(connection1))
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(connection1)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            publisher.assertMaxRequested(1L);
        } finally {
            processor.dispose();
        }
    }

    /**
     * Verifies that we can get the same, open connection when subscribing twice.
     */
    @Test
    void sameConnectionReturned() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor
            = publisher.next(connection1).flux().subscribeWith(createChannelProcessor());

        try {
            // Act & Assert
            StepVerifier.create(processor)
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(connection1)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            StepVerifier.create(processor).expectNext(connection1).expectComplete().verify(VERIFY_TIMEOUT);
        } finally {
            processor.dispose();
        }
    }

    /**
     * Verifies that we can get the next connection when the first one is closed.
     */
    @Test
    void newConnectionOnClose() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor
            = publisher.next(connection1).flux().subscribeWith(createChannelProcessor());

        try {
            // Act & Assert
            // Verify that we get the first connection.
            StepVerifier.create(processor)
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(connection1)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            // Close that connection.
            connection1.getSink().complete();

            // Expect that the next connection is returned to us.
            StepVerifier.create(processor).then(() -> {
                publisher.next(connection2);
                connection2.getSink().next(AmqpEndpointState.ACTIVE);
            }).expectNext(connection2).expectComplete().verify(VERIFY_TIMEOUT);

            // Close connection 2
            connection2.getSink().complete();

            // Expect that the new connection is returned again.
            StepVerifier.create(processor).then(() -> {
                publisher.next(connection3);
                connection3.getSink().next(AmqpEndpointState.ACTIVE);
            }).expectNext(connection3).expectComplete().verify(VERIFY_TIMEOUT);

            // Expect that the new connection is returned again.
            StepVerifier.create(processor).expectNext(connection3).expectComplete().verify(VERIFY_TIMEOUT);
        } finally {
            processor.dispose();
        }
    }

    public static Stream<Throwable> newConnectionOnRetriableError() {
        return Stream.of(new RejectedExecutionException("Rejected test execution"),
            new IllegalStateException("Dispatcher was closed. Please try again."), new AmqpException(true,
                AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error", new AmqpErrorContext("test-namespace")));
    }

    /**
     * Verifies that we can get the next connection when the first one encounters a retryable error.
     */
    @MethodSource("newConnectionOnRetriableError")
    @ParameterizedTest
    void newConnectionOnRetriableErrorTest(Throwable exception) {
        // Arrange
        final AtomicInteger failedTries = new AtomicInteger();

        AmqpRetryPolicy retryPolicy = new AmqpRetryPolicy(new AmqpRetryOptions()) {
            @Override
            protected Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter,
                ThreadLocalRandom random) {
                failedTries.incrementAndGet();
                throw new RuntimeException("Unexpected call to calculateRetryDelay");
            }

            @Override
            public Duration calculateRetryDelay(Throwable lastException, int retryCount) {
                // Check if either the lastException or the cause for the lastException is the same instance as the
                // exception. Both the lastException and its cause needs to be checked as RejectedExecutionException and
                // IllegalStateException are wrapped in an AmqpException.
                // If the retryCount is 0, then we return a delay of 1ms.
                // Otherwise, an exception is thrown to include a stack trace and to clearly indicate we reached a state
                // that wasn't expected. Previously, this was returning a delay longer than StepVerifer's timeout which
                // failed the test but didn't provide a clear indication of why it failed (specifically why or what
                // caused an additional retry).
                failedTries.getAndIncrement();
                if ((lastException == exception || lastException.getCause() == exception) && retryCount == 0) {
                    return Duration.ofMillis(1);
                } else {
                    throw new RuntimeException("Unexpected call to calculateRetryDelay", lastException);
                }
            }
        };

        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor
            = publisher.flux().subscribeWith(createChannelProcessor(retryPolicy));

        try {
            // Act & Assert
            // Verify that we get the first connection even though endpoints states are failing.
            StepVerifier.create(processor, 1).then(() -> {
                publisher.next(connection1);
                connection1.getSink().error(exception);
            }).expectNext(connection1).expectComplete().verify(VERIFY_TIMEOUT);

            // Expect that the next connection is returned to us.
            StepVerifier.create(processor, 1).then(() -> {
                publisher.next(connection2);
                connection2.getSink().next(AmqpEndpointState.ACTIVE);
            }).expectNext(connection2).expectComplete().verify(VERIFY_TIMEOUT);

            // Expect that the next connection is returned to us.
            StepVerifier.create(processor, 1).expectNext(connection2).expectComplete().verify(VERIFY_TIMEOUT);

            // sanity check - we had one failure and should have called retry policy exactly once.
            assertEquals(1, failedTries.get());
        } finally {
            processor.dispose();
        }
    }

    public static Stream<Throwable> nonRetriableError() {
        return Stream.of(new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "Test-error",
            new AmqpErrorContext("test-namespace")), new NullPointerException("Another exception"));
    }

    /**
     * Verifies that an error is propagated when the first connection encounters a non-retriable error.
     */
    @MethodSource
    @ParameterizedTest
    void nonRetriableError(Throwable exception) {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        AmqpRetryPolicy retryPolicy = new AmqpRetryPolicy(new AmqpRetryOptions()) {
            @Override
            protected Duration calculateRetryDelay(int retryCount, Duration baseDelay, Duration baseJitter,
                ThreadLocalRandom random) {
                return null;
            }

            @Override
            public Duration calculateRetryDelay(Throwable lastException, int retryCount) {
                return null;
            }
        };

        final AmqpChannelProcessor<TestObject> processor
            = publisher.next(connection1).flux().subscribeWith(createChannelProcessor(retryPolicy));

        try {
            // Act & Assert
            // Verify that we get the first connection.
            StepVerifier.create(processor)
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(connection1)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            connection1.getSink().error(exception);

            // Expect that the error is returned to us.
            StepVerifier.create(processor)
                .expectErrorMatches(error -> Objects.equals(exception, error))
                .verify(VERIFY_TIMEOUT);

            // Expect that the error is returned to us again.
            StepVerifier.create(processor)
                .expectErrorMatches(error -> Objects.equals(exception, error))
                .verify(VERIFY_TIMEOUT);
        } finally {
            processor.dispose();
        }
    }

    /**
     * Verifies that on initial subscribe, one item is requested.
     */
    @Test
    void noSubscribers() {
        // Arrange
        Map<Long, AtomicInteger> requests = new ConcurrentHashMap<>();
        final Subscription subscription = new Subscription() {
            @Override
            public void request(long n) {
                requests.compute(n, (key, value) -> {
                    if (value == null) {
                        return new AtomicInteger(1);
                    } else {
                        value.incrementAndGet();
                        return value;
                    }
                });
            }

            @Override
            public void cancel() {

            }
        };

        final AmqpChannelProcessor<TestObject> processor = createChannelProcessor();
        try {
            // Act
            processor.onSubscribe(subscription);

            // Assert
            assertEquals(1, requests.get(1L).get());
        } finally {
            subscription.cancel();
            processor.dispose();
        }
    }

    /**
     * Verifies that when the processor has completed (ie. the instance is closed), and we try to subscribe, an error is
     * thrown.
     */
    @Test
    void errorsWhenResubscribingOnTerminated() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> channelProcessor = createChannelProcessor();
        final AmqpChannelProcessor<TestObject> processor
            = publisher.next(connection1).flux().subscribeWith(channelProcessor);

        try {
            // Act & Assert
            // Verify that we get the first connection.
            StepVerifier.create(processor)
                .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
                .expectNext(connection1)
                .expectComplete()
                .verify(VERIFY_TIMEOUT);

            processor.dispose();

            // Verify that it errors without emitting a connection.
            StepVerifier.create(processor).expectError(IllegalStateException.class).verify(VERIFY_TIMEOUT);

            assertTrue(channelProcessor.isChannelClosed());
        } finally {
            processor.dispose();
        }
    }

    @Test
    void requiresNonNullNext() {
        final AmqpChannelProcessor<TestObject> channelProcessor = createChannelProcessor();
        try {
            Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onNext(null));
        } finally {
            channelProcessor.dispose();
        }
    }

    @Test
    void requiresNonNullError() {
        final AmqpChannelProcessor<TestObject> channelProcessor = createChannelProcessor();
        try {
            Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onError(null));
        } finally {
            channelProcessor.dispose();
        }
    }

    private static AmqpChannelProcessor<TestObject> createChannelProcessor() {
        return createChannelProcessor(new FixedAmqpRetryPolicy(new AmqpRetryOptions()));
    }

    private static AmqpChannelProcessor<TestObject> createChannelProcessor(AmqpRetryPolicy retryPolicy) {
        return new AmqpChannelProcessor<>("namespace-test", TestObject::getStates, retryPolicy, new HashMap<>());
    }

    static final class TestObject {
        private final TestPublisher<AmqpEndpointState> processor = TestPublisher.createCold();

        public Flux<AmqpEndpointState> getStates() {
            return processor.flux();
        }

        public TestPublisher<AmqpEndpointState> getSink() {
            return processor;
        }
    }
}
