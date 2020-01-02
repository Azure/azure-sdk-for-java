/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
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
public class EventHubConnectionProcessorTest {
    @Mock
    private EventHubAmqpConnection connection;
    @Mock
    private EventHubAmqpConnection connection2;
    @Mock
    private EventHubAmqpConnection connection3;

    private final Duration timeout = Duration.ofSeconds(10);
    private DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private DirectProcessor<AmqpShutdownSignal> shutdownSignalProcessor = DirectProcessor.create();

    private EventHubConnectionProcessor eventHubConnectionProcessor = new EventHubConnectionProcessor();

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        when(connection.getShutdownSignals()).thenReturn(shutdownSignalProcessor);
    }

    /**
     * Verifies that we can get a new connection.
     */
    @Test
    public void createsNewConnection() {
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
    public void sameConnectionReturned() {
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
    public void newConnectionOnClose() {
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

        // Act & Assert

        // Verify that we get the first connection.
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
    public void newConnectionOnRetryableError() {
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
    public void nonRetryableError() {
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
    public void noSubscribers() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        // Act
        eventHubConnectionProcessor.onSubscribe(subscription);

        // Assert
        verify(subscription).request(eq(0L));
    }

    /**
     * Verifies that when the processor has completed (ie. the instance is closed), no more connections are emitted.
     */
    @Test
    public void completesWhenTerminated() {
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
            .expectComplete()
            .verify(timeout);
    }

    @Test
    public void requiresNonNull() {
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
