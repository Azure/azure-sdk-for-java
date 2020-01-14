// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class AmqpReceiveLinkProcessorTest {
    private static final int PREFETCH = 5;

    @Mock
    private AmqpReceiveLink link1;
    @Mock
    private AmqpReceiveLink link2;
    @Mock
    private AmqpReceiveLink link3;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private Message message1;
    @Mock
    private Message message2;
    @Captor
    private ArgumentCaptor<Supplier<Integer>> creditSupplierCaptor;


    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final FluxSink<Message> messageProcessorSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private AmqpReceiveLinkProcessor linkProcessor;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        linkProcessor = new AmqpReceiveLinkProcessor(PREFETCH, retryPolicy);

        when(link1.getEndpointStates()).thenReturn(endpointProcessor);
        when(link1.receive()).thenReturn(messageProcessor);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void constructor() {
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpReceiveLinkProcessor(PREFETCH, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpReceiveLinkProcessor(-1, retryPolicy));
    }

    /**
     * Verifies that we can get a new AMQP receive link and fetch a few messages.
     */
    @Test
    void createNewLink() {
        // Arrange
        final int maximumPrefetchValue = 100;
        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                messageProcessorSink.next(message1);
                messageProcessorSink.next(message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .thenCancel()
            .verify();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertFalse(processor.hasError());
        Assertions.assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        Assertions.assertEquals(maximumPrefetchValue, creditValue);
    }

    /**
     * Verifies that we respect the back pressure request when it is in range 1 - 100.
     */
    @Test
    void respectsBackpressureInRange() {
        // Arrange
        final int backpressure = 15;
        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> messageProcessorSink.next(message1))
            .expectNext(message1)
            .thenCancel()
            .verify();

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        Assertions.assertEquals(backpressure, creditValue);
    }

    /**
     * Verifies we don't set the back pressure when it is too low.
     */
    @Test
    void respectsBackpressureLessThanMinimum() {
        // Arrange
        final int backpressure = -1;
        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Act
        processor.subscribe(
            e -> System.out.println("message: " + e),
            Assertions::fail,
            () -> System.out.println("Complete."),
            s -> s.request(backpressure));

        // Assert
        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        Assertions.assertEquals(1, creditValue);
    }

    /**
     * Verifies that we can only subscribe once.
     */
    @Test
    void onSubscribingTwiceThrowsException() {
        // Arrange
        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Subscribing first time
        processor.subscribe();

        // Act & Assert
        // The second time we subscribe, we expect that it'll throw.
        StepVerifier.create(processor)
            .expectError(IllegalStateException.class)
            .verify();
    }

    /**
     * Verifies that we can get subsequent AMQP links when the first one is closed.
     */
    @Test
    void newLinkOnClose() {
        // Arrange
        final AmqpReceiveLink[] connections = new AmqpReceiveLink[]{link1, link2, link3};

        final Message message3 = mock(Message.class);
        final Message message4 = mock(Message.class);

        final AmqpReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();
        final DirectProcessor<AmqpEndpointState> connection2EndpointProcessor = DirectProcessor.create();
        final FluxSink<AmqpEndpointState> connection2Endpoint =
            connection2EndpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
        final DirectProcessor<Message> link2Receive = DirectProcessor.create();
        final FluxSink<Message> link2ReceiveSink = link2Receive.sink();

        when(link2.getEndpointStates()).thenReturn(connection2EndpointProcessor);
        when(link2.receive()).thenReturn(Flux.create(sink -> sink.next(message2)));

        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.receive()).thenReturn(Flux.create(sink -> {
            sink.next(message3);
            sink.next(message4);
        }));

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> messageProcessorSink.next(message1))
            .expectNext(message1)
            .then(() -> {
                // Close that first link.
                endpointSink.complete();
            })
            .expectNext(message2)
            .then(() -> {
                // Close connection 2
                connection2Endpoint.complete();
            })
            .expectNext(message3)
            .expectNext(message4)
            .then(() -> {
                processor.cancel();
            })
            .verifyComplete();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertFalse(processor.hasError());
        Assertions.assertNull(processor.getError());
    }

    /**
     * Verifies that we can get the next AMQP link when the first one encounters a retryable error.
     */
    @Test
    void newLinkOnRetryableError() {
        // Arrange
        final AmqpReceiveLink[] connections = new AmqpReceiveLink[]{link1, link2};

        final AmqpReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.receive()).thenReturn(Flux.just(message2));

        final AmqpException amqpException = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));
        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(Duration.ofSeconds(1));

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> {
                endpointSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
            })
            .expectNext(message1)
            .then(() -> {
                endpointSink.error(amqpException);
            })
            .expectNext(message2)
            .thenCancel()
            .verify();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertFalse(processor.hasError());
        Assertions.assertNull(processor.getError());
    }

    /**
     * Verifies that an error is propagated when the first connection encounters a non-retryable error.
     */
    @Test
    void nonRetryableError() {
        // Arrange
        final AmqpReceiveLink[] connections = new AmqpReceiveLink[]{link1, link2};

        final AmqpReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();
        final Message message3 = mock(Message.class);

        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.receive()).thenReturn(Flux.just(message2, message3));

        final AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.ARGUMENT_ERROR, "Non"
            + "-retryable-error",
            new AmqpErrorContext("test-namespace"));
        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(null);

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> {
                endpointSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
            })
            .expectNext(message1)
            .then(() -> {
                endpointSink.error(amqpException);
            })
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);
                AmqpException exception = (AmqpException) error;

                Assertions.assertFalse(exception.isTransient());
                Assertions.assertEquals(amqpException.getErrorCondition(), exception.getErrorCondition());
                Assertions.assertEquals(amqpException.getMessage(), exception.getMessage());
            })
            .verify();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertTrue(processor.hasError());
        Assertions.assertSame(amqpException, processor.getError());
    }

    /**
     * Verifies that when there are no subscribers, no request is fetched from upstream.
     */
    @Test
    void noSubscribers() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        // Act
        linkProcessor.onSubscribe(subscription);

        // Assert
        verify(subscription).request(eq(0L));
    }

    /**
     * Verifies that when the instance is terminated, no request is fetched from upstream.
     */
    @Test
    void noSubscribersWhenTerminated() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        linkProcessor.cancel();

        // Act
        linkProcessor.onSubscribe(subscription);

        // Assert
        verifyZeroInteractions(subscription);
    }

    /**
     * Verifies it keeps trying to get a link and stops after retries are exhausted.
     */
    @Test
    void retriesUntilExhausted() {
        // Arrange
        final Duration delay = Duration.ofSeconds(1);
        final AmqpReceiveLink[] connections = new AmqpReceiveLink[]{link1, link2, link3};
        final Message message3 = mock(Message.class);

        final AmqpReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        final DirectProcessor<AmqpEndpointState> link2StateProcessor = DirectProcessor.create();
        final FluxSink<AmqpEndpointState> link2StateSink = link2StateProcessor.sink();

        when(link2.getEndpointStates()).thenReturn(link2StateProcessor);
        when(link2.receive()).thenReturn(Flux.never());

        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.receive()).thenReturn(Flux.never());

        // Simulates two busy signals, but our retry policy says to try only once.
        final AmqpException amqpException = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));
        final AmqpException amqpException2 = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));
        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(delay);
        when(retryPolicy.calculateRetryDelay(amqpException2, 2)).thenReturn(null);

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> {
                endpointSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
            })
            .expectNext(message1)
            .then(() -> endpointSink.error(amqpException))
            .thenAwait(delay)
            .then(() -> link2StateSink.error(amqpException2))
            .expectErrorSatisfies(error -> Assertions.assertSame(amqpException2, error))
            .verify();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertTrue(processor.hasError());
        Assertions.assertSame(amqpException2, processor.getError());
    }

    @Test
    void requiresNonNull() {
        Assertions.assertThrows(NullPointerException.class,
            () -> linkProcessor.onNext(null));

        Assertions.assertThrows(NullPointerException.class,
            () -> linkProcessor.onError(null));
    }

    private static Flux<AmqpReceiveLink> createSink(AmqpReceiveLink[] links) {
        return Flux.create(emitter -> {
            final AtomicInteger counter = new AtomicInteger();

            emitter.onRequest(request -> {
                for (int i = 0; i < request; i++) {
                    final int index = counter.getAndIncrement();

                    if (index == links.length) {
                        emitter.error(new RuntimeException(String.format(
                            "Cannot emit more. Index: %s. # of Connections: %s",
                            index, links.length)));
                        break;
                    }

                    emitter.next(links[index]);
                }
            });
        }, FluxSink.OverflowStrategy.BUFFER);
    }
}
