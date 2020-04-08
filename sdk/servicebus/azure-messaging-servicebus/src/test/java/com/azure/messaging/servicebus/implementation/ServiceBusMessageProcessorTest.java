// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.MessageLockToken;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.azure.messaging.servicebus.TestUtils.createMessageSink;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class ServiceBusMessageProcessorTest {
    @Mock
    private ServiceBusReceivedMessage message1;
    @Mock
    private ServiceBusReceivedMessage message2;
    @Mock
    private ServiceBusReceivedMessage message3;
    @Mock
    private ServiceBusReceivedMessage message4;

    @Mock
    private Function<MessageLockToken, Mono<Void>> onComplete;
    @Mock
    private Function<MessageLockToken, Mono<Void>> onAbandon;
    @Mock
    private Function<MessageLockToken, Mono<Instant>> onRenewLock;

    private final AmqpErrorContext errorContext = new LinkErrorContext("foo", "bar", "link-name", 10);
    private final ClientLogger logger = new ClientLogger(ServiceBusMessageProcessorTest.class);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();
    private final MessageLockContainer messageContainer = new MessageLockContainer();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(onComplete.apply(any())).thenReturn(Mono.empty());
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verifies that all messages are emitted downstream.
     */
    @Test
    void autoCompletesNoAutoRenew() {
        // Arrange
        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        final String lock3 = UUID.randomUUID().toString();
        final String lock4 = UUID.randomUUID().toString();

        when(message1.getLockToken()).thenReturn(lock1);
        when(message2.getLockToken()).thenReturn(lock2);
        when(message3.getLockToken()).thenReturn(lock3);
        when(message4.getLockToken()).thenReturn(lock4);

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2, message3, message4)
            .subscribeWith(new ServiceBusMessageProcessor(true, false, Duration.ZERO,
                retryOptions, messageContainer, errorContext, onComplete, onAbandon, onRenewLock));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1, message2, message3, message4)
            .verifyComplete();

        verify(onComplete).apply(message1);
        verify(onComplete).apply(message2);
        verify(onComplete).apply(message3);
        verify(onComplete).apply(message4);
    }

    /**
     * Verifies that all messages are emitted downstream.
     */
    @Test
    void autoCompletesAndAutoRenews() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(60);

        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(1));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(onRenewLock.apply(message1)).thenAnswer(invocationOnMock -> Mono.just(Instant.now().plusSeconds(3)));

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(true, true, maxRenewDuration,
                retryOptions, messageContainer, errorContext, onComplete, onAbandon, onRenewLock));

        // Act & Assert
        StepVerifier.create(processor)
            .assertNext(m -> {
                Assertions.assertSame(message1, m);

                logger.info("Now: {}", Instant.now());
                try {
                    TimeUnit.SECONDS.sleep(8);
                } catch (InterruptedException ignored) {
                }
                logger.info("After: {}", Instant.now());
            })
            .expectNext(message2)
            .verifyComplete();

        verify(onRenewLock, times(3)).apply(message1);

        verify(onComplete).apply(message1);
        verify(onComplete).apply(message2);
    }

    /**
     * Verifies that all messages are emitted downstream and auto complete is not invoked.
     */
    @Test
    void emitsDoesNotAutoCompleteOrRenew() {
        // Arrange
        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2, message3, message4)
            .subscribeWith(new ServiceBusMessageProcessor(false, false, Duration.ZERO,
                retryOptions, messageContainer, errorContext, onComplete, onAbandon, onRenewLock));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1, message2, message3, message4)
            .verifyComplete();

        verifyZeroInteractions(onComplete);
    }

    /**
     * When the max auto-renewal time has elapsed, we throw an error.
     */
    @Test
    void autoRenewExpires() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(4);
        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(1));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(onComplete.apply(any())).thenReturn(Mono.empty());

        when(onRenewLock.apply(message1)).thenAnswer(invocationOnMock -> Mono.just(Instant.now().plusSeconds(7)));

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(true, true, maxRenewDuration,
                retryOptions, messageContainer, errorContext, onComplete, onAbandon, onRenewLock));

        // Act & Assert
        StepVerifier.create(processor)
            .assertNext(m -> {
                Assertions.assertSame(message1, m);

                logger.info("Now: {}", Instant.now());
                try {
                    TimeUnit.SECONDS.sleep(6);
                } catch (InterruptedException ignored) {
                }
                logger.info("After: {}", Instant.now());
            })
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof AmqpException);
                Assertions.assertEquals(AmqpErrorCondition.TIMEOUT_ERROR, ((AmqpException) error).getErrorCondition());
            })
            .verify();

        verify(onRenewLock).apply(message1);
        verifyZeroInteractions(message2);
        verifyZeroInteractions(onComplete);
    }

    /**
     * When an error occurs in auto-renew lock we stop processing the next items.
     */
    @Test
    void autoRenewOperationErrors() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(10);
        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(1));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(onComplete.apply(any())).thenReturn(Mono.empty());

        when(onRenewLock.apply(message1)).thenAnswer(invocationOnMock -> Mono.error(new IllegalArgumentException("Test error occurred.")));

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(true, true, maxRenewDuration,
                retryOptions, messageContainer, errorContext, onComplete, onAbandon, onRenewLock));

        // Act & Assert
        StepVerifier.create(processor)
            .assertNext(m -> {
                Assertions.assertSame(message1, m);

                logger.info("Now: {}", Instant.now());
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException ignored) {
                }
                logger.info("After: {}", Instant.now());
            })
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof IllegalArgumentException);
            })
            .verify();

        verify(onRenewLock).apply(message1);
        verifyZeroInteractions(message2);
        verifyZeroInteractions(onComplete);
    }

    /**
     * When an error occurs in complete, we stop processing the next items.
     */
    @Test
    void completeOperationErrors() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(10);
        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(1));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(onComplete.apply(message1)).thenAnswer(
            invocationOnMock -> {
                return Mono.error(new IllegalArgumentException("Test error occurred."));
            });

        when(onRenewLock.apply(any())).thenReturn(Mono.empty());

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(true, true, maxRenewDuration,
                retryOptions, messageContainer, errorContext, onComplete, onAbandon, onRenewLock));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1)
            .expectErrorSatisfies(error -> {
                Assertions.assertTrue(error instanceof IllegalArgumentException);
            })
            .verify();

        verify(onComplete).apply(message1);

        verifyZeroInteractions(message2);
        verifyZeroInteractions(onRenewLock);
    }
}
