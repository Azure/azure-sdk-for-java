package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AmqpChannelProcessorTest {
    @Mock
    private MockChannel connection;
    @Mock
    private MockChannel connection2;
    @Mock
    private MockChannel connection3;
    @Mock
    private AmqpRetryPolicy retryPolicy;

    private final ClientLogger logger = new ClientLogger(AmqpChannelProcessor.class);
    private final Duration timeout = Duration.ofSeconds(10);
    private DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();

    private AmqpChannelProcessor<MockChannel> channelProcessor;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        channelProcessor = new AmqpChannelProcessor<>(retryPolicy, channel -> channel.getEndpoints(), logger);

        when(connection.getEndpoints()).thenReturn(endpointProcessor);

        when(retryPolicy.calculateRetryDelay(
            argThat(error -> (error instanceof AmqpException) && ((AmqpException) error).isTransient()), anyInt()))
            .thenReturn(Duration.ofSeconds(1));
    }

    /**
     * Verifies that we can get a new connection.
     */
    @Test
    void createsNewConnection() {
        AmqpChannelProcessor<MockChannel> processor =
            Flux.<MockChannel>create(sink -> sink.next(connection)).subscribeWith(channelProcessor);

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
        AmqpChannelProcessor<MockChannel> processor = Flux.<MockChannel>create(sink -> sink.next(connection))
            .subscribeWith(channelProcessor);

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
        final MockChannel[] connections = new MockChannel[]{
            connection,
            connection2,
            connection3
        };

        final Flux<MockChannel> connectionsSink = createSink(connections);
        final AmqpChannelProcessor<MockChannel> processor = connectionsSink.subscribeWith(channelProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();
        final DirectProcessor<AmqpEndpointState> connection2EndpointProcessor = DirectProcessor.create();
        final FluxSink<AmqpEndpointState> connection2Endpoint = connection2EndpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        connection2Endpoint.next(AmqpEndpointState.ACTIVE);

        when(connection2.getEndpoints()).thenReturn(connection2EndpointProcessor);

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
    void newConnectionOnRetryableError() {
        // Arrange
        final MockChannel[] connections = new MockChannel[]{
            connection,
            connection2
        };
        final AmqpException amqpException = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));

        final Flux<MockChannel> connectionsSink = createSink(connections);
        final AmqpChannelProcessor<MockChannel> processor = connectionsSink.subscribeWith(channelProcessor);
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
        final MockChannel[] connections = new MockChannel[]{
            connection,
            connection2
        };
        final AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.ILLEGAL_STATE, "Test-error",
            new AmqpErrorContext("test-namespace"));

        final Flux<MockChannel> connectionsSink = createSink(connections);
        final AmqpChannelProcessor<MockChannel> processor = connectionsSink.subscribeWith(channelProcessor);
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
        final MockChannel[] connections = new MockChannel[]{
            connection,
        };

        final Flux<MockChannel> connectionsSink = createSink(connections);
        final AmqpChannelProcessor<MockChannel> processor = connectionsSink.subscribeWith(channelProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(channelProcessor)
            .then(() -> endpointSink.next(AmqpEndpointState.ACTIVE))
            .expectNext(connection)
            .expectComplete()
            .verify(timeout);

        channelProcessor.onComplete();

        // Verify that it completes without emitting a connection.
        StepVerifier.create(processor)
            .expectComplete()
            .verify(timeout);
    }

    @Test
    void requiresNonNull() {
        Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onNext(null));
        Assertions.assertThrows(NullPointerException.class, () -> channelProcessor.onError(null));
    }

    private static Flux<MockChannel> createSink(MockChannel[] connections) {
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

    private interface MockChannel extends AutoCloseable {
        String getId();
        Flux<AmqpEndpointState> getEndpoints();
    }
}
