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
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @Mock
    private Disposable parentConnection;

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

        linkProcessor = new AmqpReceiveLinkProcessor(PREFETCH, retryPolicy, parentConnection);

        when(link1.getEndpointStates()).thenReturn(endpointProcessor);
        when(link1.receive()).thenReturn(messageProcessor);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void constructor() {
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpReceiveLinkProcessor(PREFETCH, null,
            parentConnection));
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpReceiveLinkProcessor(PREFETCH, retryPolicy,
            null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpReceiveLinkProcessor(-1, retryPolicy,
            parentConnection));
    }

    /**
     * Verifies that we can get a new AMQP receive link and fetch a few messages.
     */
    @Test
    void createNewLink() {
        // Arrange
        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        when(link1.getCredits()).thenReturn(1);

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
        // Expecting 1 because it is Long.MAX_VALUE.
        Assertions.assertEquals(1, creditValue);
    }

    /**
     * Verifies that we respect the back pressure request when it is in range 1 - 100.
     */
    @Test
    void respectsBackpressureInRange() {
        // Arrange
        final int backpressure = 15;
        // Because one message was emitted.
        final int expected = backpressure - 1;
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
        Assertions.assertEquals(expected, creditValue);
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
        when(link1.getCredits()).thenReturn(1);

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
        // Expecting 0 because we wouldn't have any requested set.
        Assertions.assertEquals(0, creditValue);
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

        when(link2.getEndpointStates()).thenReturn(connection2EndpointProcessor);
        when(link2.receive()).thenReturn(Flux.create(sink -> sink.next(message2)));

        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.receive()).thenReturn(Flux.create(sink -> {
            sink.next(message3);
            sink.next(message4);
        }));

        when(link1.getCredits()).thenReturn(1);
        when(link2.getCredits()).thenReturn(1);
        when(link3.getCredits()).thenReturn(1);

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
     * Verifies that when there are no subscribers, one request is fetched up stream.
     */
    @Test
    void noSubscribers() {
        // Arrange
        final Subscription subscription = mock(Subscription.class);

        // Act
        linkProcessor.onSubscribe(subscription);

        // Assert
        verify(subscription).request(eq(1L));
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
        verifyNoInteractions(subscription);
    }


    /**
     * Does not request another link when parent connection is closed.
     */
    @Test
    void doNotRetryWhenParentConnectionIsClosed() {
        // Arrange
        final AmqpReceiveLink[] connections = new AmqpReceiveLink[]{link1, link2};

        final AmqpReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        final DirectProcessor<AmqpEndpointState> link2StateProcessor = DirectProcessor.create();

        when(parentConnection.isDisposed()).thenReturn(true);

        when(link2.getEndpointStates()).thenReturn(link2StateProcessor);
        when(link2.receive()).thenReturn(Flux.never());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpointSink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
            })
            .expectNext(message1)
            .then(() -> endpointSink.complete())
            .thenCancel()
            .verify();

        Assertions.assertTrue(processor.isTerminated());
    }

    @Test
    void requiresNonNull() {
        Assertions.assertThrows(NullPointerException.class,
            () -> linkProcessor.onNext(null));

        Assertions.assertThrows(NullPointerException.class,
            () -> linkProcessor.onError(null));
    }

    /**
     * Verifies that we respect the back pressure request and stop emitting.
     */
    @Test
    void stopsEmittingAfterBackPressure() {
        // Arrange
        final int backpressure = 5;
        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        when(link1.getCredits()).thenReturn(0, 5, 4, 3, 2, 1);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> {
                for (int i = 0; i < backpressure + 2; i++) {
                    messageProcessorSink.next(message2);
                }
            })
            .expectNextCount(backpressure)
            .thenAwait(Duration.ofSeconds(2))
            .thenCancel()
            .verify();
    }

    @Test
    void receivesUntilFirstLinkClosed() {
        // Arrange
        AmqpReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
        FluxSink<AmqpEndpointState> sink = endpointProcessor.sink();

        when(link1.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                sink.next(AmqpEndpointState.ACTIVE);
                messageProcessorSink.next(message1);
                messageProcessorSink.next(message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .then(() -> sink.complete())
            .expectComplete()
            .verify();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertFalse(processor.hasError());
        Assertions.assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting 1 because it is Long.MAX_VALUE.
        Assertions.assertEquals(1, creditValue);
    }

    @Test
    void receivesFromFirstLink() {
        // Arrange
        AmqpReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
        FluxSink<AmqpEndpointState> sink = endpointProcessor.sink();

        when(link1.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                sink.next(AmqpEndpointState.ACTIVE);
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
        // Expecting 1 because it is Long.MAX_VALUE.
        Assertions.assertEquals(1, creditValue);
    }
    /**
     * Verifies that when we request back pressure amounts, if it only requests a certain number of events, only
     * that number is consumed.
     */
    @Test
    void backpressureRequestOnlyEmitsThatAmount() {
        // Arrange
        final int backpressure = 10;
        AmqpReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
        FluxSink<AmqpEndpointState> sink = endpointProcessor.sink();

        when(link1.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> {
                sink.next(AmqpEndpointState.ACTIVE);
                final int emitted = backpressure + 5;
                for (int i = 0; i < emitted; i++) {
                    messageProcessorSink.next(mock(Message.class));
                }
            })
            .expectNextCount(backpressure)
            .thenAwait(Duration.ofSeconds(1))
            .thenCancel()
            .verify();

        Assertions.assertTrue(processor.isTerminated());
        Assertions.assertFalse(processor.hasError());
        Assertions.assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(any());
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
