// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link FluxAutoComplete} for abandoning messages.
 */
class FluxAutoCompleteTest {

    private final Semaphore completionLock = new Semaphore(1);

    private final ArrayList<ServiceBusMessageContext> onCompleteInvocations = new ArrayList<>();
    private final ArrayList<ServiceBusMessageContext> onAbandonInvocations = new ArrayList<>();

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @AfterEach
    public void afterEach() {
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    void constructor() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.create();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new FluxAutoComplete(null, completionLock,
            this::onComplete, this::onAbandon));
        assertThrows(NullPointerException.class,
            () -> new FluxAutoComplete(testPublisher.flux(), completionLock, null, this::onAbandon));
        assertThrows(NullPointerException.class,
            () -> new FluxAutoComplete(testPublisher.flux(), completionLock, this::onComplete, null));
    }

    @Test
    void completesOnSuccess() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock,
            this::onComplete, this::onAbandon);

        // Act

        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.emit(context, context2))
            .expectNext(context, context2)
            .verifyComplete();

        // Assert
        verifyLists(onCompleteInvocations, context, context2);
        verifyLists(onAbandonInvocations);
    }

    @Test
    void abandonsOnFailure() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final TestCoreSubscriber downstream = new TestCoreSubscriber(2);

        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock,
            this::onComplete, this::onAbandon);

        // Act
        autoComplete.subscribe(downstream);
        testPublisher.emit(context, context2);

        // Assert
        verifyLists(downstream.onNextInvocations, context, context2);
        assertTrue(downstream.onCompleteInvocation.get(), "Should have been completed.");

        verifyLists(onCompleteInvocations, context);
        verifyLists(onAbandonInvocations, context2);
    }

    @Test
    void passesErrorDownstream() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final TestCoreSubscriber downstream = new TestCoreSubscriber(2);

        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock,
            this::onComplete, this::onAbandon);

        final Throwable testError = new IllegalArgumentException("Dummy exception");

        // Act
        autoComplete.subscribe(downstream);
        testPublisher.next(context, context2);
        testPublisher.error(testError);

        // Assert
        verifyLists(downstream.onNextInvocations, context, context2);

        assertFalse(downstream.onCompleteInvocation.get(), "'onComplete' should not have been invoked.");
        assertEquals(1, downstream.onErrorInvocations.size());
        assertEquals(testError, downstream.onErrorInvocations.get(0));

        verifyLists(onCompleteInvocations, context);
    }

    @Test
    void doesNotContinueOnCancellation() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context3 = new ServiceBusMessageContext(message3);
        final ServiceBusReceivedMessage message4 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context4 = new ServiceBusMessageContext(message4);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock, this::onComplete, this::onAbandon);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.next(context, context2, context3, context4))
            .thenConsumeWhile(m -> m != context2)
            .thenCancel()
            .verify();

        // Assert
        verifyLists(onCompleteInvocations, context, context2);
        verifyLists(onAbandonInvocations);

        testPublisher.assertWasCancelled();
    }

    /**
     * Verifies that if onComplete errors, we log but continue consuming.
     */
    @SuppressWarnings("unchecked")
    @Test
    void onCompleteErrors() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);

        final Throwable testError = new IllegalArgumentException("Dummy error");
        final Function<ServiceBusMessageContext, Mono<Void>> onCompleteErrorFunction =
            new Function<ServiceBusMessageContext, Mono<Void>>() {
                private final AtomicInteger iteration = new AtomicInteger();

                @Override
                public Mono<Void> apply(ServiceBusMessageContext messageContext) {
                    if (iteration.getAndIncrement() == 1) {
                        return Mono.error(testError);
                    } else {
                        return Mono.empty();
                    }
                }
            };

        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock,
            onCompleteErrorFunction, this::onAbandon);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.next(context, context2))
            .expectNext(context, context2)
            .expectErrorSatisfies(e -> Assertions.assertEquals(testError, e))
            .verify();

        // Assert
        verifyLists(onAbandonInvocations);
    }

    /**
     * Verifies that if a message has been settled, we will not try to complete it.
     */
    @Test
    void doesNotCompleteOnSettledMessage() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(message.isSettled()).thenReturn(true);

        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);

        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock,
            this::onComplete, this::onAbandon);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.next(context, context2))
            .expectNext(context, context2)
            .then(() -> testPublisher.complete())
            .expectComplete()
            .verify();

        // Assert
        verifyLists(onCompleteInvocations, context2);
        verifyLists(onAbandonInvocations);
    }

    @SuppressWarnings("unchecked")
    @Test
    void onErrorCancelsUpstream() {
        // Arrange
        final TestPublisher<ServiceBusMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(message.isSettled()).thenReturn(false);
        final ServiceBusMessageContext context = new ServiceBusMessageContext(message);

        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        when(message2.isSettled()).thenReturn(false);
        final ServiceBusMessageContext context2 = new ServiceBusMessageContext(message2);

        final CloneNotSupportedException testError = new CloneNotSupportedException("TEST error");

        final Function<ServiceBusMessageContext, Mono<Void>> errorOnComplete =
            new Function<ServiceBusMessageContext, Mono<Void>>() {
                private final AtomicBoolean isFirst = new AtomicBoolean(true);

                @Override
                public Mono<Void> apply(ServiceBusMessageContext messageContext) {
                    onCompleteInvocations.add(messageContext);
                    return isFirst.getAndSet(false) ? Mono.error(testError) : Mono.empty();
                }
            };

        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), completionLock,
            errorOnComplete, this::onAbandon);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.next(context, context2))
            .expectNext(context)
            .consumeErrorWith(error -> {
                final Throwable cause = error.getCause();
                assertNotNull(cause);
                assertEquals(testError, cause);
            })
            .verify();

        // Assert
        verifyLists(onCompleteInvocations, context);
        verifyLists(onAbandonInvocations);

        testPublisher.assertCancelled();
    }

    private void verifyLists(ArrayList<ServiceBusMessageContext> actual,
        ServiceBusMessageContext... messageContexts) {

        assertEquals(messageContexts.length, actual.size());

        for (int i = 0; i < messageContexts.length; i++) {
            ServiceBusMessageContext expected = messageContexts[i];
            assertTrue(actual.contains(expected),
                "invocation " + i + " was not expected. Actual: " + actual);
        }
    }

    private Mono<Void> onComplete(ServiceBusMessageContext messageContext) {
        onCompleteInvocations.add(messageContext);
        return Mono.empty();
    }

    private Mono<Void> onAbandon(ServiceBusMessageContext messageContext) {
        onAbandonInvocations.add(messageContext);
        return Mono.empty();
    }

    /**
     * Mockito fails with org.mockito.exceptions.misusing.NotAMockException if you try to mock CoreSubscriberT
     * periodically.
     */
    private static class TestCoreSubscriber implements CoreSubscriber<ServiceBusMessageContext> {
        private final ArrayList<ServiceBusMessageContext> onNextInvocations = new ArrayList<>();
        private final ArrayList<Throwable> onErrorInvocations = new ArrayList<>();

        private final AtomicReference<Subscription> upstream = new AtomicReference<>();
        private final AtomicBoolean onCompleteInvocation = new AtomicBoolean();

        // Index for the onNext method to throw an error.
        private final int errorIndex;
        private int current = 0;

        TestCoreSubscriber(int errorIndex) {
            this.errorIndex = errorIndex;
        }

        @Override
        public void onSubscribe(Subscription s) {
            if (!upstream.compareAndSet(null, s)) {
                throw new IllegalStateException("Did not expect to be subscribed to, twice.");
            }

            s.request(10);
        }

        @Override
        public void onNext(ServiceBusMessageContext messageContext) {
            onNextInvocations.add(messageContext);

            current = current + 1;
            if (current == errorIndex) {
                throw new IllegalArgumentException("Dummy message.");
            }
        }

        @Override
        public void onError(Throwable t) {
            onErrorInvocations.add(t);
        }

        @Override
        public void onComplete() {
            if (onCompleteInvocation.getAndSet(true)) {
                throw new IllegalArgumentException("Did not expect to complete twice.");
            }
        }
    }
}
