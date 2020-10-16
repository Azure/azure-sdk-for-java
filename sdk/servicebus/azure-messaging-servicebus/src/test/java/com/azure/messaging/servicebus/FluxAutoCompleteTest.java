// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link FluxAutoComplete} for abandoning messages.
 */
class FluxAutoCompleteTest {
    @Mock
    private Function<ServiceBusReceivedMessage, Mono<Void>> onComplete;
    @Mock
    private Function<ServiceBusReceivedMessage, Mono<Void>> onAbandon;

    private Mono<Void> onCompleteResult;
    private Mono<Void> onAbandonResult;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);

        when(onComplete.apply(any())).thenReturn(Mono.defer(() -> onCompleteResult));
        when(onAbandon.apply(any())).thenReturn(Mono.defer(() -> onAbandonResult));
    }

    @Test
    void constructor() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> testPublisher = TestPublisher.create();
        onCompleteResult = Mono.empty();
        onAbandonResult = Mono.empty();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new FluxAutoComplete<>(null, onComplete, onAbandon));
        assertThrows(NullPointerException.class,
            () -> new FluxAutoComplete<>(testPublisher.flux(), null, onAbandon));
        assertThrows(NullPointerException.class,
            () -> new FluxAutoComplete<>(testPublisher.flux(), onComplete, null));
    }

    @Test
    void completesOnSuccess() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final FluxAutoComplete<ServiceBusReceivedMessage> autoComplete = new FluxAutoComplete<>(testPublisher.flux(),
            onComplete, onAbandon);

        onCompleteResult = Mono.empty();

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.emit(message))
            .expectNext(message)
            .verifyComplete();

        // Assert
        verify(onComplete).apply(message);
    }

    @Test
    void abandonsOnFailure() {
        // Arrange
        final TestPublisher<ServiceBusReceivedMessage> testPublisher = TestPublisher.createCold();
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        final FluxAutoComplete<ServiceBusReceivedMessage> autoComplete = new FluxAutoComplete<>(testPublisher.flux(),
            onComplete, onAbandon);

        onCompleteResult = Mono.empty();
        onAbandonResult = Mono.empty();

        // Act
        StepVerifier.create(autoComplete)
            .then(() -> testPublisher.emit(message, message2))
            .assertNext(m -> {
                throw new IllegalArgumentException("Some dummy exception.");
            })
            .expectNext(message2)
            .verifyComplete();

        // Assert
        verify(onAbandon).apply(message);
        verify(onComplete).apply(message2);
    }
}
