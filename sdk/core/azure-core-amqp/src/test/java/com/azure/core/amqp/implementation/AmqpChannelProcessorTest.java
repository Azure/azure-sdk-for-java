// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.test.scheduler.VirtualTimeScheduler;

import java.time.Duration;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AmqpChannelProcessor}.
 */
class AmqpChannelProcessorTest {
    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(30);
    private final TestObject connection1 = new TestObject();
    private final TestObject connection2 = new TestObject();
    private final TestObject connection3 = new TestObject();

    @Mock
    private AmqpRetryPolicy retryPolicy;
    private AmqpChannelProcessor<TestObject> channelProcessor;
    private AutoCloseable mocksCloseable;

    private VirtualTimeScheduler virtualTimeScheduler;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
        virtualTimeScheduler = VirtualTimeScheduler.create();
        channelProcessor = new AmqpChannelProcessor<>("namespace-test", TestObject::getStates, retryPolicy, new HashMap<>());
    }

    @AfterEach
    void teardown() throws Exception {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);
        virtualTimeScheduler.dispose();
        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    /**
     * Verifies that we can get a new connection. This new connection is only emitted when the endpoint state is
     * active.
     */
    @Test
    void createsNewConnection() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor = publisher.flux().subscribeWith(channelProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> publisher.next(connection1))
            .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        publisher.assertMaxRequested(1L);
    }

    /**
     * Verifies that we can get the same, open connection when subscribing twice.
     */
    @Test
    void sameConnectionReturned() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor = publisher.next(connection1)
            .flux().subscribeWith(channelProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        StepVerifier.create(processor)
            .expectNext(connection1)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);
    }

    /**
     * Verifies that we can get the next connection when the first one is closed.
     */
    @Test
    void newConnectionOnClose() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor = publisher.next(connection1)
            .flux().subscribeWith(channelProcessor);

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
        StepVerifier.create(processor)
            .then(() -> {
                publisher.next(connection2);
                connection2.getSink().next(AmqpEndpointState.ACTIVE);
            })
            .expectNext(connection2)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Close connection 2
        connection2.getSink().complete();

        // Expect that the new connection is returned again.
        StepVerifier.create(processor)
            .then(() -> {
                publisher.next(connection3);
                connection3.getSink().next(AmqpEndpointState.ACTIVE);
            })
            .expectNext(connection3)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Expect that the new connection is returned again.
        StepVerifier.create(processor)
            .expectNext(connection3)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);
    }

    public static Stream<Throwable> newConnectionOnRetriableError() {
        return Stream.of(
            new RejectedExecutionException("Rejected test execution"),
            new IllegalStateException("Dispatcher was closed. Please try again."),
            new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
                new AmqpErrorContext("test-namespace"))
        );
    }

    /**
     * Verifies that we can get the next connection when the first one encounters a retryable error.
     */
    @MethodSource
    @ParameterizedTest
    @Disabled("Disable test until fixed. https://github.com/Azure/azure-sdk-for-java/issues/29239")
    void newConnectionOnRetriableError(Throwable exception) {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        publisher.next(connection1);
        publisher.next(connection2);
        final AmqpChannelProcessor<TestObject> processor = publisher.flux().subscribeWith(channelProcessor);
        final long request = 1;

        when(retryPolicy.calculateRetryDelay(exception, 1)).thenReturn(Duration.ofSeconds(1));
        when(retryPolicy.getMaxRetries()).thenReturn(3);

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor, request)
            .thenAwait(Duration.ofMillis(400))
            .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        connection1.getSink().error(exception);

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor, request)
            .thenAwait(Duration.ofMillis(400))
            .then(() -> connection2.getSink().next(AmqpEndpointState.ACTIVE))
            .expectNext(connection2)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor, request)
            .expectNext(connection2)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);
    }

    public static Stream<Throwable> nonRetriableError() {
        return Stream.of(
            new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "Test-error",
                new AmqpErrorContext("test-namespace")),
            new NullPointerException("Another exception"));
    }

    /**
     * Verifies that an error is propagated when the first connection encounters a non-retriable error.
     */
    @MethodSource
    @ParameterizedTest
    void nonRetriableError(Throwable exception) {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor = publisher.next(connection1).flux()
            .subscribeWith(channelProcessor);

        /*
         * Beginning in Mockito 3.4.0+ the default value for duration changed from null to Duration.ZERO
         */
        when(retryPolicy.calculateRetryDelay(any(), anyInt())).thenReturn(null);

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
    }

    /**
     * Verifies that on initial subscribe, one item is requested.
     */
    @Test
    void noSubscribers() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        // Act
        channelProcessor.onSubscribe(subscription);

        // Assert
        verify(subscription).request(eq(1L));
    }

    /**
     * Verifies that when the processor has completed (ie. the instance is closed), and we try to subscribe, an error is
     * thrown.
     */
    @Test
    void errorsWhenResubscribingOnTerminated() {
        // Arrange
        final TestPublisher<TestObject> publisher = TestPublisher.createCold();
        final AmqpChannelProcessor<TestObject> processor = publisher.next(connection1).flux()
            .subscribeWith(channelProcessor);

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .expectComplete()
            .verify(VERIFY_TIMEOUT);

        processor.dispose();

        // Verify that it errors without emitting a connection.
        StepVerifier.create(processor)
            .expectError(IllegalStateException.class)
            .verify(VERIFY_TIMEOUT);

        assertTrue(channelProcessor.isChannelClosed());
    }

    @Test
    void requiresNonNull() {
        Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onNext(null));

        Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onError(null));
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
