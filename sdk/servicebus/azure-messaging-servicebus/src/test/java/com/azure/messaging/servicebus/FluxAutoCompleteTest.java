// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests {@link FluxAutoComplete} for abandoning messages.
 */
class FluxAutoCompleteTest {
    @Mock
    private Function<ServiceBusReceivedMessageContext, Mono<Void>> onComplete;
    @Mock
    private Function<ServiceBusReceivedMessageContext, Mono<Void>> onAbandon;
    @Mock
    private CoreSubscriber<ServiceBusReceivedMessageContext> downstreamSubscriber;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void constructor() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessageContext> testPublisher = TestPublisher.create();
        when(onComplete.apply(any())).thenReturn(Mono.empty());
        when(onAbandon.apply(any())).thenReturn(Mono.empty());

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new FluxAutoComplete(null, onComplete, onAbandon));
        assertThrows(NullPointerException.class,
            () -> new FluxAutoComplete(testPublisher.flux(), null, onAbandon));
        assertThrows(NullPointerException.class,
            () -> new FluxAutoComplete(testPublisher.flux(), onComplete, null));
    }

    @Test
    void completesOnSuccess() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context2 = new ServiceBusReceivedMessageContext(message2);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), onComplete, onAbandon);

        when(onComplete.apply(any())).thenReturn(Mono.empty());
        when(onAbandon.apply(any())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.emit(context, context2))
            .expectNext(context, context2)
            .verifyComplete();

        // Assert
        verify(onComplete).apply(context);
        verify(onComplete).apply(context2);
        verifyNoInteractions(onAbandon);
    }

    @Test
    void abandonsOnFailure() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context2 = new ServiceBusReceivedMessageContext(message2);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), onComplete, onAbandon);

        when(onComplete.apply(any())).thenReturn(Mono.empty());
        when(onAbandon.apply(any())).thenReturn(Mono.empty());

        doAnswer(invocation -> {
            throw new IllegalArgumentException("Dummy message.");
        }).when(downstreamSubscriber).onNext(context2);

        // Act
        autoComplete.subscribe(downstreamSubscriber);
        testPublisher.emit(context, context2);

        // Assert
        verify(downstreamSubscriber).onNext(context);
        verify(downstreamSubscriber).onNext(context2);
        verify(downstreamSubscriber).onComplete();

        verify(onComplete).apply(context);
        verify(onAbandon).apply(context2);
    }

    @Test
    void passesErrorDownstream() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context2 = new ServiceBusReceivedMessageContext(message2);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), onComplete, onAbandon);
        final Throwable testError = new IllegalArgumentException("Dummy exception");

        when(onComplete.apply(any())).thenReturn(Mono.empty());
        when(onAbandon.apply(any())).thenReturn(Mono.empty());

        // Act
        autoComplete.subscribe(downstreamSubscriber);
        testPublisher.next(context, context2);
        testPublisher.error(testError);

        // Assert
        verify(downstreamSubscriber).onNext(context);
        verify(downstreamSubscriber).onNext(context2);
        verify(downstreamSubscriber, never()).onComplete();
        verify(downstreamSubscriber).onError(testError);

        verify(onComplete).apply(context);
        verify(onComplete).apply(context2);
    }

    @Test
    void doesNotContinueOnCancellation() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context2 = new ServiceBusReceivedMessageContext(message2);
        final ServiceBusReceivedMessage message3 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context3 = new ServiceBusReceivedMessageContext(message3);
        final ServiceBusReceivedMessage message4 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context4 = new ServiceBusReceivedMessageContext(message4);
        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), onComplete, onAbandon);

        when(onComplete.apply(any())).thenReturn(Mono.empty());
        when(onAbandon.apply(any())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.next(context, context2, context3, context4))
            .thenConsumeWhile(m -> m != context2)
            .thenCancel()
            .verify();

        // Assert
        verify(onComplete).apply(context);
        verify(onComplete).apply(context2);

        verify(onComplete, never()).apply(context3);
        verify(onComplete, never()).apply(context4);

        verifyNoInteractions(onAbandon);
        testPublisher.assertWasCancelled();
    }

    /**
     * Verifies that if onComplete errors, we log but continue consuming.
     */
    @SuppressWarnings("unchecked")
    @Test
    void onCompleteErrors() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessageContext> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context = new ServiceBusReceivedMessageContext(message);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessageContext context2 = new ServiceBusReceivedMessageContext(message2);

        final FluxAutoComplete autoComplete = new FluxAutoComplete(testPublisher.flux(), onComplete, onAbandon);
        final Throwable testError = new IllegalArgumentException("Dummy error");

        when(onComplete.apply(any())).thenReturn(Mono.empty(), Mono.error(testError), Mono.empty(), Mono.empty());
        when(onAbandon.apply(any())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.next(context, context2))
            .expectNext(context, context2)
            .expectErrorSatisfies(e -> Assertions.assertEquals(testError, e))
            .verify();

        // Assert
        verify(onComplete).apply(context);
        verify(onComplete).apply(context2);
        verifyNoInteractions(onAbandon);
    }
}
