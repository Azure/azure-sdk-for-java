// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests auto disposition feature of {@link AutoDispositionLockRenew} operator.
 */
class FluxAutoCompleteTest {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private final Semaphore completionLock = new Semaphore(1);
    private AutoCloseable mocksCloseable;

    @BeforeEach
    void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void afterEach() throws Exception {
        mocksCloseable.close();
        Mockito.framework().clearInlineMock(this);
    }

    @Test
    void completesOnSuccess() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messagesCaptor
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.complete(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        final AutoDispositionLockRenew autoComplete = autoCompleteOperator(publisher.flux(), client);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> publisher.emit(message1, message2))
            .expectNext(message1, message2)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        // Assert
        verify(client, times(2)).complete(messagesCaptor.capture());
        final List<ServiceBusReceivedMessage> messages = messagesCaptor.getAllValues();
        Assertions.assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals(message1, messages.get(0));
        assertEquals(message2, messages.get(1));
    }

    @Test
    void abandonsOnFailure() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final TestCoreSubscriber subscriber = new TestCoreSubscriber(2); // fail on the 2nd message.
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messageCaptor1
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messageCaptor2
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.complete(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        when(client.abandon(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        final AutoDispositionLockRenew autoComplete = autoCompleteOperator(publisher.flux(), client);

        // Act
        autoComplete.subscribe(subscriber);
        publisher.emit(message1, message2);

        // Assert
        verifyLists(subscriber.onNextInvocations, message1, message2);
        assertTrue(subscriber.onCompleteInvocation.get(), "Should have been completed.");

        verify(client, times(1)).complete(messageCaptor1.capture());
        final ServiceBusReceivedMessage m1 = messageCaptor1.getValue();
        Assertions.assertNotNull(m1);
        assertEquals(message1, m1);

        verify(client, times(1)).abandon(messageCaptor2.capture());
        final ServiceBusReceivedMessage m2 = messageCaptor2.getValue();
        Assertions.assertNotNull(m2);
        assertEquals(message2, m2);
    }

    @Test
    void propagatesUpstreamError() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final TestCoreSubscriber subscriber = new TestCoreSubscriber(2); // throws on the 2nd message.
        final Throwable error = new IllegalArgumentException("error");
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messageCaptor1
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messageCaptor2
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.complete(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        when(client.abandon(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        final AutoDispositionLockRenew autoComplete = autoCompleteOperator(publisher.flux(), client);

        // Act
        autoComplete.subscribe(subscriber);
        publisher.next(message1, message2);
        publisher.error(error);

        // Assert
        verifyLists(subscriber.onNextInvocations, message1, message2);
        assertFalse(subscriber.onCompleteInvocation.get(), "'onComplete' should not have been invoked.");
        assertEquals(error, subscriber.onErrorInvocations.get(0));

        verify(client, times(1)).complete(messageCaptor1.capture());
        final ServiceBusReceivedMessage m1 = messageCaptor1.getValue();
        Assertions.assertNotNull(m1);
        assertEquals(message1, m1);

        verify(client, times(1)).abandon(messageCaptor2.capture());
        final ServiceBusReceivedMessage m2 = messageCaptor2.getValue();
        Assertions.assertNotNull(m2);
        assertEquals(message2, m2);
    }

    @Test
    void doesNotContinueOnCancellation() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message4 = mock(ServiceBusReceivedMessage.class);
        final ArgumentCaptor<ServiceBusReceivedMessage> messagesCaptor
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.complete(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        final AutoDispositionLockRenew autoComplete = autoCompleteOperator(publisher.flux(), client);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> publisher.next(message1, message2, message3, message4))
            .thenConsumeWhile(m -> m != message2)
            .thenCancel()
            .verify(DEFAULT_TIMEOUT);

        // Assert
        verify(client, times(2)).complete(messagesCaptor.capture());
        final List<ServiceBusReceivedMessage> messages = messagesCaptor.getAllValues();
        Assertions.assertNotNull(messages);
        assertEquals(2, messages.size());
        assertEquals(message1, messages.get(0));
        assertEquals(message2, messages.get(1));
        publisher.assertWasCancelled();
    }

    @Test
    void propagatesDispositionError() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final Throwable error = new RuntimeException("lock expired.");
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.complete(message1)).thenReturn(Mono.empty());
        when(client.complete(message2)).thenReturn(Mono.error(error));
        final AutoDispositionLockRenew autoComplete = autoCompleteOperator(publisher.flux(), client);

        // Act and Assert
        StepVerifier.create(autoComplete)
            .then(() -> publisher.emit(message1, message2))
            .expectNext(message1, message2)
            // assert disposition error propagated
            .expectErrorSatisfies(e -> Assertions.assertEquals(error, e))
            .verify(DEFAULT_TIMEOUT);
        verify(client, times(2)).complete(any(ServiceBusReceivedMessage.class));
        // assert the MessageFlux will be 'cancelled' on error.
        publisher.assertCancelled();
    }

    @Test
    void doesNotCompleteOnSettledMessage() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> publisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message1 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        when(message2.isSettled()).thenReturn(true); // already settled message.
        final ArgumentCaptor<ServiceBusReceivedMessage> messagesCaptor
            = ArgumentCaptor.forClass(ServiceBusReceivedMessage.class);
        final ServiceBusReceiverAsyncClient client = mock(ServiceBusReceiverAsyncClient.class);
        when(client.complete(any(ServiceBusReceivedMessage.class))).thenReturn(Mono.empty());
        final AutoDispositionLockRenew autoComplete = autoCompleteOperator(publisher.flux(), client);

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> publisher.emit(message1, message2))
            .expectNext(message1, message2)
            .expectComplete()
            .verify(DEFAULT_TIMEOUT);

        // Assert
        verify(client, times(1)).complete(messagesCaptor.capture());
        final ServiceBusReceivedMessage m = messagesCaptor.getValue();
        Assertions.assertNotNull(m);
        assertEquals(message1, m);
    }

    private AutoDispositionLockRenew autoCompleteOperator(Flux<ServiceBusReceivedMessage> messageFlux,
        ServiceBusReceiverAsyncClient client) {
        return new AutoDispositionLockRenew(messageFlux, client, true, false, completionLock);
    }

    private void verifyLists(ArrayList<ServiceBusReceivedMessage> actual, ServiceBusReceivedMessage... messages) {
        assertEquals(messages.length, actual.size());
        for (int i = 0; i < messages.length; i++) {
            ServiceBusReceivedMessage expected = messages[i];
            assertTrue(actual.contains(expected), "invocation " + i + " was not expected. Actual: " + actual);
        }
    }

    /**
     * Mockito fails with org.mockito.exceptions.misusing.NotAMockException if you try to mock CoreSubscriberT
     * periodically.
     */
    static class TestCoreSubscriber implements CoreSubscriber<ServiceBusReceivedMessage> {
        private final AtomicReference<Subscription> upstream = new AtomicReference<>();
        final ArrayList<ServiceBusReceivedMessage> onNextInvocations = new ArrayList<>();
        final AtomicBoolean onCompleteInvocation = new AtomicBoolean();
        final ArrayList<Throwable> onErrorInvocations = new ArrayList<>();

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
        public void onNext(ServiceBusReceivedMessage message) {
            onNextInvocations.add(message);
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
