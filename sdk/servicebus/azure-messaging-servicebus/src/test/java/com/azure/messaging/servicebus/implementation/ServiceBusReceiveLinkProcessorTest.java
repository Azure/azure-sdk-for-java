// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
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
import static org.mockito.Mockito.times;
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

    private final TestPublisher<AmqpEndpointState> endpointProcessor = TestPublisher.create();
    private final TestPublisher<Message> messagePublisher = TestPublisher.create();
    private ServiceBusReceiveLinkProcessor linkProcessor;
    private ServiceBusReceiveLinkProcessor linkProcessorNoPrefetch;

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

        linkProcessor = new ServiceBusReceiveLinkProcessor(PREFETCH, retryPolicy);
        linkProcessorNoPrefetch = new ServiceBusReceiveLinkProcessor(0, retryPolicy);

        when(link1.getEndpointStates()).thenReturn(endpointProcessor.flux());
        when(link1.receive()).thenReturn(messagePublisher.flux());
        when(link1.addCredits(anyInt())).thenReturn(Mono.empty());
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void constructor() {
        assertThrows(NullPointerException.class, () -> new ServiceBusReceiveLinkProcessor(PREFETCH, null));
        assertThrows(IllegalArgumentException.class, () -> new ServiceBusReceiveLinkProcessor(-1, retryPolicy));
    }

    /**
     * Verifies that we can get a new AMQP receive link and fetch a few messages.
     */
    @Test
    void createNewLink() throws InterruptedException {
        // Arrange
        final CountDownLatch countDownLatch = new CountDownLatch(2);
        when(link1.getCredits()).thenReturn(1);
        when(link1.addCredits(eq(PREFETCH - 1))).thenAnswer(invocation -> {
            countDownLatch.countDown();
            return Mono.empty();
        });

        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                messagePublisher.next(message1, message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // dispose the processor
        processor.dispose();

        // Add credit for each time 'onNext' is called, plus once when publisher is subscribed.
        final boolean awaited = countDownLatch.await(5, TimeUnit.SECONDS);
        Assertions.assertTrue(awaited);
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
            .subscribeWith(linkProcessorNoPrefetch);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> messagePublisher.next(message1))
            .expectNext(message1)
            .thenCancel()
            .verify();

        verify(link1).addCredits(backpressure);  // request up to PREFETCH
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
        final int count = 4;
        final Message message3 = mock(Message.class);
        final Message message4 = mock(Message.class);

        final TestPublisher<AmqpEndpointState> connection2EndpointProcessor = TestPublisher.create();

        when(link2.getEndpointStates()).thenReturn(connection2EndpointProcessor.flux());
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

        when(link1.closeAsync()).thenReturn(Mono.empty());
        when(link2.closeAsync()).thenReturn(Mono.empty());
        when(link3.closeAsync()).thenReturn(Mono.empty());

        final ServiceBusReceiveLink[] connections = new ServiceBusReceiveLink[]{link1, link2, link3};
        final ServiceBusReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor.take(count))
            .then(() -> messagePublisher.next(message1))
            .expectNext(message1)
            .then(() -> {
                // Close that first link.
                endpointProcessor.complete();
            })
            .expectNext(message2)
            .then(() -> {
                // Close connection 2
                connection2EndpointProcessor.complete();
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

        when(link2.getEndpointStates()).thenReturn(Flux.defer(() -> Flux.create(e -> {
            e.next(AmqpEndpointState.ACTIVE);
        })));
        when(link2.receive()).thenReturn(Flux.just(message2));
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        final AmqpException amqpException = new AmqpException(true, AmqpErrorCondition.SERVER_BUSY_ERROR, "Test-error",
            new AmqpErrorContext("test-namespace"));
        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(Duration.ofSeconds(1));

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1);
            })
            .expectNext(message1)
            .then(() -> {
                endpointProcessor.error(amqpException);
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
        TestPublisher<AmqpEndpointState> endpointStates = TestPublisher.createCold();
        endpointStates.next(AmqpEndpointState.ACTIVE);

        final ServiceBusReceiveLinkProcessor processor = createSink(connections).subscribeWith(linkProcessor);
        final Message message3 = mock(Message.class);

        when(link2.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link2.receive()).thenReturn(Flux.just(message2, message3));
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        final AmqpException amqpException = new AmqpException(false, AmqpErrorCondition.ARGUMENT_ERROR,
            "Non-retryable-error",
            new AmqpErrorContext("test-namespace"));
        when(retryPolicy.calculateRetryDelay(amqpException, 1)).thenReturn(null);

        // Act & Assert
        // Verify that we get the first connection.
        StepVerifier.create(processor)
            .then(() -> {
                System.out.println("Outputting exception.");
                endpointProcessor.error(amqpException);
            })
            .expectErrorSatisfies(error -> {
                System.out.println("Asserting exception.");
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

        final DirectProcessor<AmqpEndpointState> link2StateProcessor = DirectProcessor.create();
        final FluxSink<AmqpEndpointState> link2StateSink = link2StateProcessor.sink();

        when(link2.getEndpointStates()).thenReturn(link2StateProcessor);
        when(link2.receive()).thenReturn(Flux.never());
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        when(link3.getEndpointStates()).thenReturn(Flux.create(sink -> sink.next(AmqpEndpointState.ACTIVE)));
        when(link3.receive()).thenReturn(Flux.never());
        when(link3.addCredits(anyInt())).thenReturn(Mono.empty());

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
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1);
            })
            .expectNext(message1)
            .then(() -> endpointProcessor.error(amqpException))
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
        final TestPublisher<ServiceBusReceiveLink> linkGenerator = TestPublisher.createCold();
        final ServiceBusReceiveLinkProcessor processor = linkGenerator.flux().subscribeWith(linkProcessor);
        final TestPublisher<AmqpEndpointState> endpointStates = TestPublisher.createCold();
        final TestPublisher<Message> messages = TestPublisher.createCold();

        when(link1.getEndpointStates()).thenReturn(endpointStates.flux());
        when(link1.receive()).thenReturn(messages.flux());

        final TestPublisher<AmqpEndpointState> endpointStates2 = TestPublisher.createCold();
        when(link2.getEndpointStates()).thenReturn(endpointStates2.flux());
        when(link2.receive()).thenReturn(Flux.never());
        when(link2.addCredits(anyInt())).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                linkGenerator.next(link1);
                endpointStates.next(AmqpEndpointState.ACTIVE);
            })
            .then(() -> {
                linkGenerator.complete();
                endpointStates.complete();
            })
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
                    messagePublisher.next(message2);
                }
            })
            .expectNextCount(backpressure)
            .thenAwait(Duration.ofSeconds(2))
            .thenCancel()
            .verify();
    }

    @Test
    void receivesUntilFirstLinkClosed() throws InterruptedException {
        // Arrange
        ServiceBusReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);

        final Duration shortWait = Duration.ofSeconds(5);

        when(link1.getCredits()).thenReturn(0);
        when(link1.closeAsync()).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1);
            })
            .expectNext(message1)
            .then(() -> messagePublisher.next(message2))
            .expectNext(message2)
            .then(() -> endpointProcessor.complete())
            .expectComplete()
            .verify(shortWait);

        TimeUnit.SECONDS.sleep(shortWait.getSeconds());

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // Add credit for each time 'onNext' is called, plus once when publisher is subscribed.
        verify(link1, times(3)).addCredits(eq(PREFETCH));
        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());  // Add 0

        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();

        assertEquals(0, creditValue);
    }

    @Test
    void receivesFromFirstLink() throws InterruptedException {
        // Arrange
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        when(link1.getCredits()).thenReturn(0);
        when(link1.addCredits(eq(PREFETCH))).thenAnswer(invocation -> {
            countDownLatch.countDown();
            return Mono.empty();
        });

        ServiceBusReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                messagePublisher.next(message1, message2);
            })
            .expectNext(message1)
            .expectNext(message2)
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // dispose the processor
        processor.dispose();

        verify(link1).setEmptyCreditListener(creditSupplierCaptor.capture());  // Add 0.
        Supplier<Integer> value = creditSupplierCaptor.getValue();
        assertNotNull(value);

        final Integer creditValue = value.get();

        assertEquals(0, creditValue);

        // Add credit for each time 'onNext' is called, plus once when publisher is subscribed.
        final boolean awaited = countDownLatch.await(5, TimeUnit.SECONDS);
        Assertions.assertTrue(awaited);

    }

    /**
     * Verifies that when we request back pressure amounts, if it only requests a certain number of events, only that
     * number is consumed.
     */
    @Test
    void backpressureRequestOnlyEmitsThatAmount() {
        // Arrange
        final int backpressure = PREFETCH;
        final int existingCredits = 1;
        final int expectedCredits = backpressure - existingCredits;
        ServiceBusReceiveLinkProcessor processor = Flux.just(link1).subscribeWith(linkProcessor);

        when(link1.getCredits()).thenReturn(existingCredits);

        // Act & Assert
        StepVerifier.create(processor, backpressure)
            .then(() -> {
                endpointProcessor.next(AmqpEndpointState.ACTIVE);
                final int emitted = backpressure + 5;
                for (int i = 0; i < emitted; i++) {
                    Message message = mock(Message.class);
                    messagePublisher.next(message);
                }
            })
            .expectNextCount(backpressure)
            .thenAwait(Duration.ofSeconds(1))
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // Add credit for each time 'onNext' is called, plus once when publisher is subscribed.
        verify(link1, times(backpressure + 1)).addCredits(expectedCredits);
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

    @Test
    void updateDispositionDoesNotAddCredit() {
        // Arrange
        ServiceBusReceiveLinkProcessor processor = Flux.<ServiceBusReceiveLink>create(sink -> sink.next(link1))
            .subscribeWith(linkProcessor);
        final String lockToken = "lockToken";
        final DeliveryState deliveryState = mock(DeliveryState.class);

        when(link1.getCredits()).thenReturn(0);
        when(link1.updateDisposition(eq(lockToken), eq(deliveryState))).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(processor)
            .then(() -> processor.updateDisposition(lockToken, deliveryState))
            .thenCancel()
            .verify();

        assertTrue(processor.isTerminated());
        assertFalse(processor.hasError());
        assertNull(processor.getError());

        // This 'addCredits' is added when we subscribe to the 'ServiceBusReceiveLinkProcessor'
        verify(link1).addCredits(eq(PREFETCH));
        verify(link1).updateDisposition(eq(lockToken), eq(deliveryState));
    }
}
