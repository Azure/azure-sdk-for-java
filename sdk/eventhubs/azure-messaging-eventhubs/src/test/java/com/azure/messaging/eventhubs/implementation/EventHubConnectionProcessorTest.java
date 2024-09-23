// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link EventHubConnectionProcessor}.
 */
class EventHubConnectionProcessorTest {
    private static final String NAMESPACE = "test-namespace.eventhubs.com";
    private static final String EVENT_HUB_NAME = "test-event-hub-name";
    private static final AmqpRetryOptions AMQP_RETRY_OPTIONS = new AmqpRetryOptions()
        .setMaxRetries(2)
        .setMode(AmqpRetryMode.FIXED)
        .setDelay(Duration.ofSeconds(2));

    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private EventHubAmqpConnection connection2;
    @Mock
    private EventHubAmqpConnection connection3;

    private final Duration timeout = Duration.ofSeconds(15);
    private DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private DirectProcessor<AmqpShutdownSignal> shutdownSignalProcessor = DirectProcessor.create();
    private EventHubConnectionProcessor eventHubConnectionProcessor = new EventHubConnectionProcessor(NAMESPACE,
        EVENT_HUB_NAME, AMQP_RETRY_OPTIONS);

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        when(connection.getShutdownSignals()).thenReturn(shutdownSignalProcessor);
        when(connection.closeAsync()).thenReturn(Mono.empty());
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Verifies that we can get a new connection.
     */
    @Test
    void createsNewConnection() {
        EventHubConnectionProcessor processor = Mono.fromCallable(() -> connection).repeat()
            .subscribeWith(eventHubConnectionProcessor);

        StepVerifier.create(processor)
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);
    }

    /**
     * Verifies that we can get the same, open connection when subscribing twice.
     */
    @Test
    void sameConnectionReturned() {
        // Arrange
        EventHubConnectionProcessor processor = Mono.fromCallable(() -> connection).repeat()
            .subscribeWith(eventHubConnectionProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        StepVerifier.create(processor)
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);
    }

    /**
     * Verifies that we can get the next connection when the first one is closed.
     */
    @Test
    void newConnectionOnClose() {
        // Arrange
        final EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{
            connection,
            connection2,
            connection3
        };

        final Flux<EventHubAmqpConnection> connectionsSink = createSink(connections);
        final EventHubConnectionProcessor processor = connectionsSink.subscribeWith(eventHubConnectionProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();
        final DirectProcessor<AmqpEndpointState> connection2EndpointProcessor = DirectProcessor.create();
        final FluxSink<AmqpEndpointState> connection2Endpoint = connection2EndpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        connection2Endpoint.next(AmqpEndpointState.ACTIVE);

        when(connection2.getEndpointStates()).thenReturn(connection2EndpointProcessor);
        when(connection2.closeAsync()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        // Close that connection.
        endpointSink.complete();

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor)
            .expectNext(connection2)
            .expectComplete()
            .verify(timeout);

        // Close connection 2
        connection2Endpoint.complete();

        // Expect that the new connection is returned again.
        StepVerifier.create(processor)
            .expectNext(connection3)
            .expectComplete()
            .verify(timeout);

        // Expect that the new connection is returned again.
        StepVerifier.create(processor)
            .expectNext(connection3)
            .expectComplete()
            .verify(timeout);
    }

    /**
     * Verifies that we can get the next connection when the first one encounters a retryable error.
     */
    @Test
    void newConnectionOnRetryableError() {
        // Arrange
        final EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{
            connection,
            connection2
        };
        final AmqpException amqpException = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));

        final Flux<EventHubAmqpConnection> connectionsSink = createSink(connections);
        final EventHubConnectionProcessor processor = connectionsSink.subscribeWith(eventHubConnectionProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        endpointSink.error(amqpException);

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor)
            .expectNext(connection2)
            .expectComplete()
            .verify(timeout);

        // Expect that the next connection is returned to us.
        StepVerifier.create(processor)
            .expectNext(connection2)
            .expectComplete()
            .verify(timeout);
    }

    /**
     * Verifies that an error is propagated when the first connection encounters a non-retryable error.
     */
    @Test
    void nonRetryableError() {
        // Arrange
        final EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{
            connection,
            connection2
        };
        final AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "Test-error",
            new AmqpErrorContext("test-namespace"));

        final Flux<EventHubAmqpConnection> connectionsSink = createSink(connections);
        final EventHubConnectionProcessor processor = connectionsSink.subscribeWith(eventHubConnectionProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        endpointSink.error(amqpException);

        // Expect that the error is returned to us.
        StepVerifier.create(processor)
            .expectErrorMatches(error -> Objects.equals(amqpException, error))
            .verify(timeout);

        // Expect that the error is returned to us again.
        StepVerifier.create(processor)
            .expectErrorMatches(error -> Objects.equals(amqpException, error))
            .verify(timeout);
    }

    /**
     * Verifies that when there are no subscribers, no request is fetched from upstream.
     */
    @Test
    void noSubscribers() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        // Act
        eventHubConnectionProcessor.onSubscribe(subscription);

        // Assert
        verify(subscription).request(eq(1L));
    }

    /**
     * Verifies that when the processor has completed (ie. the instance is closed), and we try to subscribe, an error
     * is thrown.
     */
    @Test
    void errorsWhenResubscribingOnTerminated() {
        // Arrange
        final EventHubAmqpConnection[] connections = new EventHubAmqpConnection[]{
            connection,
        };

        final Flux<EventHubAmqpConnection> connectionsSink = createSink(connections);
        final EventHubConnectionProcessor processor = connectionsSink.subscribeWith(eventHubConnectionProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(eventHubConnectionProcessor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        eventHubConnectionProcessor.onComplete();

        // Verify that it completes without emitting a connection.
        StepVerifier.create(processor)
            .expectError(IllegalStateException.class)
            .verify(timeout);
    }

    @Test
    void requiresNonNull() {
        Assertions.assertThrows(NullPointerException.class,
            () -> eventHubConnectionProcessor.onNext(null));

        Assertions.assertThrows(NullPointerException.class,
            () -> eventHubConnectionProcessor.onError(null));
    }

    private static Flux<EventHubAmqpConnection> createSink(EventHubAmqpConnection[] connections) {
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
}
