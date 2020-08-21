// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusReceiveLinkProcessor}.
 */
class ServiceBusReceiveLinkProcessorTest {
    private static final int PREFETCH = 5;

    @Mock
    private ServiceBusReceiveLink link1;
    @Mock
    private ServiceBusReceiveLink link2;
    @Mock
    private ServiceBusReceiveLink link3;
    @Mock
    private AmqpRetryPolicy retryPolicy;
    @Mock
    private Message message1;
    @Mock
    private Message message2;

    @Captor
    private ArgumentCaptor<Supplier<Integer>> creditSupplierCaptor;

    private final EmitterProcessor<AmqpEndpointState> endpointProcessor = EmitterProcessor.create();
    private final EmitterProcessor<Message> messageProcessor = EmitterProcessor.create();
    private final FluxSink<Message> messageProcessorSink = messageProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private ServiceBusReceiveLinkProcessor linkProcessor;

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

        linkProcessor = new ServiceBusReceiveLinkProcessor(PREFETCH, retryPolicy, ReceiveMode.PEEK_LOCK);

        when(link1.getEndpointStates()).thenReturn(endpointProcessor);
        when(link1.receive()).thenReturn(messageProcessor);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void constructor() {
        assertThrows(NullPointerException.class, () -> new ServiceBusReceiveLinkProcessor(PREFETCH, null,
            ReceiveMode.PEEK_LOCK));
        assertThrows(IllegalArgumentException.class, () -> new ServiceBusReceiveLinkProcessor(-1, retryPolicy,
            ReceiveMode.PEEK_LOCK));
        assertThrows(NullPointerException.class, () -> new ServiceBusReceiveLinkProcessor(PREFETCH, retryPolicy,
            null));
    }

    /**
     * Verifies that we can get a new AMQP receive link and fetch a few messages.
     */
    @Test
    void createNewLink() {
        // Arrange
        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
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

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting 5 because it is Long.MAX_VALUE and there are no credits (since we invoked the empty credit
        // listener).
        assertEquals(PREFETCH, creditValue);
    }

    /**
     * Verifies that we respect the back pressure request when it is in range 1 - 100.
     */
    @Test
    void respectsBackpressureInRange() {
        // Arrange
        final int backpressure = 15;
        // Because one message was emitted.
        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> messageProcessorSink.next(message1))
            .expectNext(message1)
            .thenCancel()
            .verify();

        verify(link1).addCredits(eq(backpressure));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();
        final int emittedOne = backpressure - 1;
        assertTrue(creditValue == emittedOne || creditValue == backpressure);
    }

    /**
     * Verifies we don't set the back pressure when it is too low.
     */
    @Test
    void respectsBackpressureLessThanMinimum() throws InterruptedException {
        // Arrange
        final Semaphore semaphore = new Semaphore(1);
        final int backpressure = -1;
        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);
        when(link1.getCredits()).thenReturn(1);

        // Act
        semaphore.acquire();
        processor.subscribe(
            e -> System.out.println("message: " + e),
            Assertions::fail,
            () -> System.out.println("Complete."),
            s -> {
                s.request(backpressure);
                semaphore.release();
            });

        // Assert
        assertTrue(semaphore.tryAcquire(10, TimeUnit.SECONDS));

        verify(link1, never()).addCredits(anyInt());
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting 0 because we wouldn't have any requested set.
        assertEquals(0, creditValue);
    }

    /**
     * Verifies that we can only subscribe once.
     */
    @Test
    void onSubscribingTwiceThrowsException() {
        // Arrange
        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
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
        final ServiceBusReceiveLink[] connections = new ServiceBusReceiveLink[]{link1, link2, link3};

        final Message message3 = mock(Message.class);
        final Message message4 = mock(Message.class);

        final ServiceBusReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
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

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());
    }

    /**
     * Verifies that we can get the next AMQP link when the first one encounters a retryable error.
     */
    @Disabled("Fails on Ubuntu 18")
    @Test
    void newLinkOnRetryableError() {
        // Arrange
        final ServiceBusReceiveLink[] connections = new ServiceBusReceiveLink[]{link1, link2};

        final ServiceBusReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink();

        when(link2.getEndpointStates()).thenReturn(Flux.defer(() -> Flux.create(e -> {
            e.next(AmqpEndpointState.ACTIVE);
        })));
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
        final ServiceBusReceiveLink[] connections = new ServiceBusReceiveLink[]{link1, link2};

        final ServiceBusReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
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
                assertTrue(error instanceof AmqpException);
                AmqpException exception = (AmqpException) error;

                assertFalse(exception.isTransient());
                assertEquals(amqpException.getErrorCondition(), exception.getErrorCondition());
                assertEquals(amqpException.getMessage(), exception.getMessage());
            })
            .verify();

        assertTrue(processor.isTerminated());
        assertTrue(processor.hasError());
        assertSame(amqpException, processor.getError());
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
     * Verifies it keeps trying to get a link and stops after retries are exhausted.
     */
    @Disabled("Fails on Ubuntu 18")
    @Test
    void retriesUntilExhausted() {
        // Arrange
        final Duration delay = Duration.ofSeconds(1);
        final ServiceBusReceiveLink[] connections = new ServiceBusReceiveLink[]{link1, link2, link3};

        final ServiceBusReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
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
            .expectErrorSatisfies(error -> assertSame(amqpException2, error))
            .verify();

        assertTrue(processor.isTerminated());
        assertTrue(processor.hasError());
        assertSame(amqpException2, processor.getError());
    }

    /**
     * Does not request another link when upstream is closed.
     */
    @Test
    void doNotRetryWhenParentConnectionIsClosed() {
        // Arrange
        final TestPublisher<ServiceBusReceiveLink> linkGenerator = TestPublisher.create();
        final ServiceBusReceiveLinkProcessor processor = linkGenerator.flux().subscribeWith(linkProcessor);
        final TestPublisher<AmqpEndpointState> endpointStates = TestPublisher.create();
        final TestPublisher<Message> messages = TestPublisher.create();

        when(link1.getEndpointStates()).thenReturn(endpointStates.flux());
        when(link1.receive()).thenReturn(messages.flux());

        final TestPublisher<AmqpEndpointState> endpointStates2 = TestPublisher.create();
        when(link2.getEndpointStates()).thenReturn(endpointStates2.flux());
        when(link2.receive()).thenReturn(Flux.never());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                linkGenerator.next(link1);
                endpointStates.next(AmqpEndpointState.ACTIVE);
                messages.next(message1);
            })
            .expectNext(message1)
            .then(linkGenerator::complete)
            .then(endpointStates::complete)
            .expectComplete()
            .verify();

        assertTrue(processor.isTerminated());
    }

    @Test
    void requiresNonNull() {
        assertThrows(NullPointerException.class,
            () -> linkProcessor.onNext(null));

        assertThrows(NullPointerException.class,
            () -> linkProcessor.onError(null));
    }

    /**
     * Verifies that we respect the back pressure request and stop emitting.
     */
    @Test
    void stopsEmittingAfterBackPressure() {
        // Arrange
        final int backpressure = 5;
        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
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
        ServiceBusReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
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

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting 5 because it is Long.MAX_VALUE and there are no credits (since we invoked the empty credit
        // listener).
        assertEquals(PREFETCH, creditValue);
    }

    @Test
    void receivesFromFirstLink() {
        // Arrange
        ServiceBusReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
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

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();
        // Expecting 5 because it is Long.MAX_VALUE and there are no credits (since we invoked the empty credit
        // listener).
        assertEquals(PREFETCH, creditValue);
    }

    /**
     * Verifies that when we request back pressure amounts, if it only requests a certain number of events, only that
     * number is consumed.
     */
    @Test
    void backpressureRequestOnlyEmitsThatAmount() {
        // Arrange
        final int backpressure = 10;
        final int existingCredits = 1;
        final int expectedCredits = backpressure - existingCredits;
        ServiceBusReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);
        FluxSink<AmqpEndpointState> sink = endpointProcessor.sink();

        when(link1.getCredits()).thenReturn(existingCredits);

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

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        verify(link1).addCredits(expectedCredits);
        verify(link1).setEmptyCreditListener(any());
    }

    private static Flux<ServiceBusReceiveLink> createSink(ServiceBusReceiveLink[] links) {
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
