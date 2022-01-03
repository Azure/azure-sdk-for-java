// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
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

    private final TestPublisher<AmqpEndpointState> endpointProcessor = TestPublisher.createCold();
    private final TestPublisher<Message> messageProcessor = TestPublisher.createCold();
    private AmqpReceiveLinkProcessor linkProcessor;
    private AutoCloseable mockCloseable;

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
        mockCloseable = MockitoAnnotations.openMocks(this);

        when(retryPolicy.getRetryOptions()).thenReturn(new AmqpRetryOptions());

        linkProcessor = new AmqpReceiveLinkProcessor("entity-path", PREFETCH, parentConnection);

        when(link1.getEndpointStates()).thenReturn(endpointProcessor.flux());
        when(link1.receive()).thenReturn(messageProcessor.flux());
        when(link1.addCredits(anyInt())).thenReturn(Mono.empty());
    }

    @AfterEach
    void teardown() throws Exception {
        if (mockCloseable != null) {
            mockCloseable.close();
        }

        Mockito.framework().clearInlineMock(this);
    }

    @Test
    void constructor() {
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpReceiveLinkProcessor(
            "entity-path", PREFETCH, null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AmqpReceiveLinkProcessor(
            "ENTITY", -1, parentConnection));
        Assertions.assertThrows(NullPointerException.class, () -> new AmqpReceiveLinkProcessor(
            null, PREFETCH, parentConnection));
    }

    /**
     * Verifies that we can get a new AMQP receive link and fetch a few messages.
     */
    @Test
    void createNewLink() {
        // Arrange
        TestPublisher<AmqpEndpointState> endpoints = TestPublisher.createCold();
        when(link1.getEndpointStates()).thenReturn(endpoints.flux());
        when(link1.getCredits()).thenReturn(1);

        AmqpReceiveLinkProcessor processor = Flux.<AmqpReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpoints.next(AmqpEndpointState.ACTIVE);
                messageProcessor.next(message1);
                messageProcessor.next(message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting PREFETCH because it is Long.MAX_VALUE.
        Assertions.assertEquals(PREFETCH, creditValue);
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
            .then(() -> messageProcessor.next(message1))
            .expectNext(message1)
            .thenCancel()
            .verify();

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
        final TestPublisher<AmqpEndpointState> connection2Endpoints = TestPublisher.createCold();

        when(link2.getEndpointStates()).thenReturn(connection2Endpoints.flux());
        when(link2.receive()).thenReturn(Flux.create(sink -> sink.next(message2)));
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.receive()).thenReturn(Flux.create(sink -> {
            sink.next(message3);
            sink.next(message4);
        }));
        when(link3.addCredits(anyInt())).thenReturn(Mono.empty());

        when(link1.getCredits()).thenReturn(1);
        when(link2.getCredits()).thenReturn(1);
        when(link3.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> messageProcessor.next(message1))
            .expectNext(message1)
            .then(() -> {
                // Close that first link.
                endpointProcessor.complete();
            })
            .expectNext(message2)
            .then(() -> {
                // Close connection 2
                connection2Endpoints.complete();
            })
            .expectNext(message3)
            .expectNext(message4)
            .then(() -> {
                processor.cancel();
            })
            .verifyComplete();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());
    }

    /**
     * Verifies that an error is propagated when the first connection encounters a non-retryable error.
     */
    @Test
    void nonRetryableError() {
        // Arrange
        final AmqpReceiveLink[] connections = new AmqpReceiveLink[]{link1, link2};

        final AmqpReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final Message message3 = mock(Message.class);

        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.receive()).thenReturn(Flux.just(message2, message3));
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        final AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.ARGUMENT_ERROR, "Non"
            + "-retryable-error",
            new AmqpErrorContext("test-namespace"));
        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(null);

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messageProcessor.next(message1);
            })
            .expectNext(message1)
            .then(() -> {
                endpointProcessor.error(amqpException);
            })
            .expectErrorSatisfies(error -> {
                assertTrue(error instanceof AmqpException);
                AmqpException exception = (AmqpException) error;

                assertFalse(exception.isTransient());
                Assertions.assertEquals(amqpException.getErrorCondition(), exception.getErrorCondition());
                Assertions.assertEquals(amqpException.getMessage(), exception.getMessage());
            })
            .verify();

        assertTrue(processor.isTerminated());
        assertTrue(processor.hasError());
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

        final TestPublisher<AmqpEndpointState> link2StateProcessor = TestPublisher.createCold();

        when(parentConnection.isDisposed()).thenReturn(true);

        when(link2.getEndpointStates()).thenReturn(link2StateProcessor.flux());
        when(link2.receive()).thenReturn(Flux.never());
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messageProcessor.next(message1);
            })
            .expectNext(message1)
            .then(() -> endpointProcessor.complete())
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
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
                    messageProcessor.next(message2);
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

        when(link1.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messageProcessor.next(message1);
                messageProcessor.next(message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .then(() -> endpointProcessor.complete())
            .expectComplete()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting PREFETCH because it is Long.MAX_VALUE.
        Assertions.assertEquals(PREFETCH, creditValue);
    }

    @Test
    void receivesFromFirstLink() {
        // Arrange
        AmqpReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);

        when(link1.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messageProcessor.next(message1);
                messageProcessor.next(message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        Assertions.assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting PREFETCH because it is Long.MAX_VALUE.
        Assertions.assertEquals(PREFETCH, creditValue);
    }

    /**
     * Verifies that when we request back pressure amounts, if it only requests a certain number of events, only that
     * number is consumed.
     */
    @Test
    void backpressureRequestOnlyEmitsThatAmount() {
        // Arrange
        final int backpressure = 10;
        AmqpReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);

        when(link1.getCredits()).thenReturn(1);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                final int emitted = backpressure + 5;
                for (int i = 0; i < emitted; i++) {
                    messageProcessor.next(mock(Message.class));
                }
            })
            .expectNextCount(backpressure)
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // Once when the user initially makes a backpressure request and a second time when the link is active.
        verify(link1, atLeastOnce()).addCredits(eq(backpressure));
        verify(link1).setEmptyCreditListener(any());
    }

    @Test
    void onlyRequestsWhenNoCredits() {
        // Arrange
        final AtomicReference<Supplier<Integer>> creditListener = new AtomicReference<>();

        when(link1.getCredits()).thenReturn(1);

        doAnswer(invocationOnMock -> {
            assertTrue(creditListener.compareAndSet(null, invocationOnMock.getArgument(0)));
            return null;
        }).when(link1).setEmptyCreditListener(any());

        final int backpressure = 10;
        final AmqpReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
        final int extra = 4;
        final int nextRequest = 11;

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                for (int i = 0; i < backpressure; i++) {
                    messageProcessor.next(mock(Message.class));
                }
            })
            .expectNextCount(backpressure)
            .then(() -> {
                final Supplier<Integer> integerSupplier = creditListener.get();
                assertNotNull(integerSupplier);
                // Invoking this once. Should return a value and notify that there are no credits left on the link.
                final int messages = integerSupplier.get();
                System.out.println("Messages: " + messages);
            })
            .expectNoEvent(Duration.ofSeconds(1))
            .thenRequest(nextRequest)
            .then(() -> {
                for (int i = 0; i < nextRequest; i++) {
                    messageProcessor.next(mock(Message.class));
                }
            })
            .expectNextCount(nextRequest)
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // Once when the user initially makes a backpressure request and a second time when the link is active.
        verify(link1).addCredits(eq(backpressure));
        verify(link1).addCredits(eq(nextRequest));
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
