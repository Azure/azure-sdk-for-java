// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link AmqpChannelProcessor}.
 */
class AmqpChannelProcessorTest {
    private final TestObject connection1 = new TestObject();
    private final TestObject connection2 = new TestObject();
    private final TestObject connection3 = new TestObject();

    @Mock
    private AmqpRetryPolicy retryPolicy;
    private AmqpChannelProcessor<TestObject> channelProcessor;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(100));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        channelProcessor = new AmqpChannelProcessor<>("connection-test", "test-path",
            TestObject::getStates, retryPolicy,
            new ClientLogger(AmqpChannelProcessor.class + "<TestObject>"));
    }

    /**
     * Verifies that we can get a new connection.
     */
    @Test
    void createsNewConnection() {
        // Arrange
        final AmqpChannelProcessor<TestObject> processor = createSink(connection1, connection2)
            .subscribeWith(channelProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(connection1)
            .verifyComplete();
    }

    /**
     * Verifies that we can get the same, open connection when subscribing twice.
     */
    @Test
    void sameConnectionReturned() {
        // Arrange
        final AmqpChannelProcessor<TestObject> processor = createSink(connection1, connection2)
            .subscribeWith(channelProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(connection1)
            .verifyComplete();

        StepVerifier.create(processor)
            .expectNext(connection1)
            .verifyComplete();
    }

    /**
     * Verifies that we can get the next connection when the first one is closed.
     */
    @Test
    void newConnectionOnClose() {
        // Arrange
        final AmqpChannelProcessor<TestObject> processor = createSink(connection1, connection2, connection3)
            .subscribeWith(channelProcessor);

        final FluxSink<AmqpEndpointState> endpointSink = connection1.getSink();

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .verifyComplete();

        // Close that connection.
        endpointSink.complete();

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor)
            .expectNext(connection2)
            .verifyComplete();

        // Close connection 2
        connection2.getSink().complete();

        // Expect that the new connection is returned again.
        StepVerifier.create(processor)
            .expectNext(connection3)
            .verifyComplete();

        // Expect that the new connection is returned again.
        StepVerifier.create(processor)
            .expectNext(connection3)
            .verifyComplete();
    }

    /**
     * Verifies that we can get the next connection when the first one encounters a retryable error.
     */
    @Test
    void newConnectionOnRetryableError() {
        // Arrange
        final AmqpException amqpException = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));

        final AmqpChannelProcessor<TestObject> processor = createSink(connection1, connection2)
            .subscribeWith(channelProcessor);

        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(Duration.ofSeconds(10));

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> connection1.getSink().next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .verifyComplete();

        connection1.getSink().error(amqpException);

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor)
            .expectNext(connection2)
            .verifyComplete();

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor)
            .expectNext(connection2)
            .verifyComplete();
    }

    /**
     * Verifies that an error is propagated when the first connection encounters a non-retryable error.
     */
    @Test
    void nonRetryableError() {
        // Arrange
        final AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "Test-error",
            new AmqpErrorContext("test-namespace"));

        final AmqpChannelProcessor<TestObject> processor = createSink(connection1, connection2)
            .subscribeWith(channelProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = connection1.getSink();

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .expectComplete()
            .verify();

        endpointSink.error(amqpException);

        // Expect that the error is returned to us.
        StepVerifier.create(processor)
            .expectErrorMatches(error -> Objects.equals(amqpException, error))
            .verify();

        // Expect that the error is returned to us again.
        StepVerifier.create(processor)
            .expectErrorMatches(error -> Objects.equals(amqpException, error))
            .verify();
    }

    /**
     * Verifies that when there are no subscribers, no request is fetched from upstream.
     */
    @Test
    void noSubscribers() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        // Act
        channelProcessor.onSubscribe(subscription);

        // Assert
        verify(subscription).request(eq(0L));
    }

    /**
     * Verifies that when the processor has completed (ie. the instance is closed), no more connections are emitted.
     */
    @Test
    void completesWhenTerminated() {
        // Arrange
        final AmqpChannelProcessor<TestObject> processor = createSink(connection1, connection2)
            .subscribeWith(channelProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = connection1.getSink();


        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection1)
            .expectComplete()
            .verify();

        processor.dispose();

        // Verify that it completes without emitting a connection.
        StepVerifier.create(processor)
            .expectComplete()
            .verify();
    }

    @Test
    void requiresNonNull() {
        Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onNext(null));

        Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onError(null));
    }

    private static Flux<TestObject> createSink(TestObject... connections) {
        return Flux.create(emitter -> {
            final AtomicInteger counter = new AtomicInteger();

            emitter.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int index = counter.getAndIncrement();

                    if (index == connections.length) {
                        emitter.error(new RuntimeException(String.format(
                            "Cannot emit more. Index: %s. # of Connections: %s",
                            index, connections.length)));
                        break;
                    }

                    emitter.next(connections[index]);
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    static final class TestObject {
        private final DirectProcessor<AmqpEndpointState> processor = DirectProcessor.create();
        private final FluxSink<AmqpEndpointState> sink = processor.sink(FluxSink.OverflowStrategy.BUFFER);

        public Flux<AmqpEndpointState> getStates() {
            return processor;
        }

        public FluxSink<AmqpEndpointState> getSink() {
            return sink;
        }
    }
}
