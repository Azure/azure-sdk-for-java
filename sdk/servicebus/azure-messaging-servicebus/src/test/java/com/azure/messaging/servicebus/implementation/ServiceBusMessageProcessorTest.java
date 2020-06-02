// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import static com.azure.messaging.servicebus.TestUtils.createMessageSink;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

class ServiceBusMessageProcessorTest {
    private static final String LINK_NAME = "some-link";
    @Mock
    private ServiceBusReceivedMessage message1;
    @Mock
    private ServiceBusReceivedMessage message2;
    @Mock
    private ServiceBusReceivedMessage message3;
    @Mock
    private ServiceBusReceivedMessage message4;

    @Mock
    private MessageManagementOperations messageManagementOperations;

    private final AmqpErrorContext errorContext = new LinkErrorContext("foo", "bar", "link-name", 10);
    private final ClientLogger logger = new ClientLogger(ServiceBusMessageProcessorTest.class);
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions();

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(messageManagementOperations.updateDisposition(anyString(), any(DeliveryState.class)))
            .thenReturn(Mono.empty());
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
            .subscribeWith(new ServiceBusMessageProcessor(LINK_NAME, true, false, Duration.ZERO,
                retryOptions, errorContext, messageManagementOperations));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1, message2, message3, message4)
            .verifyComplete();

        verify(messageManagementOperations).updateDisposition(lock1, Accepted.getInstance());
        verify(messageManagementOperations).updateDisposition(lock2, Accepted.getInstance());
        verify(messageManagementOperations).updateDisposition(lock3, Accepted.getInstance());
    }

    /**
     * Verifies that all messages are emitted downstream.
     */
    @Disabled("Fails on Ubuntu 18")
    @Test
    void autoCompletesAndAutoRenews() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(60);

        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(3));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(messageManagementOperations.renewMessageLock(lock1, LINK_NAME))
            .thenAnswer(invocationOnMock -> Mono.just(Instant.now().plusSeconds(3)));

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(LINK_NAME, true, true, maxRenewDuration,
                retryOptions, errorContext, messageManagementOperations));

        // Act & Assert
        StepVerifier.create(processor)
            .assertNext(m -> {
                Assertions.assertSame(message1, m);

                logger.info("Now: {}", Instant.now());
                try {
                    TimeUnit.SECONDS.sleep(15);
                } catch (InterruptedException ignored) {
                }
                logger.info("After: {}", Instant.now());
            })
            .expectNext(message2)
            .verifyComplete();

        verify(messageManagementOperations, atLeast(3)).renewMessageLock(lock1, LINK_NAME);
        verify(messageManagementOperations).updateDisposition(lock1, Accepted.getInstance());
        verify(messageManagementOperations).updateDisposition(lock2, Accepted.getInstance());
    }

    /**
     * Verifies that all messages are emitted downstream and auto complete is not invoked.
     */
    @Test
    void emitsDoesNotAutoCompleteOrRenew() {
        // Arrange
        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2, message3, message4)
            .subscribeWith(new ServiceBusMessageProcessor(LINK_NAME, false, false, Duration.ZERO,
                retryOptions, errorContext, messageManagementOperations));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1, message2, message3, message4)
            .verifyComplete();

        verifyZeroInteractions(messageManagementOperations);
    }

    /**
     * When the max auto-renewal time has elapsed, we throw an error.
     */
    @Disabled("Fails on Ubuntu 18")
    @Test
    void autoRenewExpires() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(4);
        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(3));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(messageManagementOperations.renewMessageLock(lock1, LINK_NAME))
            .thenAnswer(invocationOnMock -> Mono.just(Instant.now().plusSeconds(7)));

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(LINK_NAME, true, true, maxRenewDuration,
                retryOptions, errorContext, messageManagementOperations));

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

        verify(messageManagementOperations).renewMessageLock(lock1, LINK_NAME);
        verifyZeroInteractions(message2);
        verify(messageManagementOperations, never()).updateDisposition(anyString(), any());
    }

    /**
     * When an error occurs in auto-renew lock we stop processing the next items.
     */
    @Disabled("Fails on Ubuntu 18")
    @Test
    void autoRenewOperationErrors() {
        // Arrange
        final Duration maxRenewDuration = Duration.ofSeconds(20);
        final String lock1 = UUID.randomUUID().toString();
        final String lock2 = UUID.randomUUID().toString();
        when(message1.getLockToken()).thenReturn(lock1);
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(3));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(messageManagementOperations.renewMessageLock(anyString(), any()))
            .thenAnswer(invocationOnMock -> Mono.error(new IllegalArgumentException("Test error occurred.")));

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(LINK_NAME, true, true, maxRenewDuration,
                retryOptions, errorContext, messageManagementOperations));

        // Act & Assert
        StepVerifier.create(processor)
            .assertNext(m -> {
                Assertions.assertSame(message1, m);

                logger.info("Now: {}", Instant.now());
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ignored) {
                }
                logger.info("After: {}", Instant.now());
            })
            .expectError(IllegalArgumentException.class)
            .verify();

        verifyZeroInteractions(message2);
        verify(messageManagementOperations, never()).updateDisposition(lock1, Accepted.getInstance());
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
        when(message1.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(3));

        when(message2.getLockToken()).thenReturn(lock2);
        when(message2.getLockedUntil()).thenAnswer(invocationOnMock -> Instant.now().plusSeconds(5));

        when(messageManagementOperations.updateDisposition(lock1, Accepted.getInstance())).thenAnswer(
            invocationOnMock -> Mono.error(new IllegalArgumentException("Test error occurred.")));

        when(messageManagementOperations.renewMessageLock(anyString(), anyString())).thenReturn(Mono.empty());

        final ServiceBusMessageProcessor processor = createMessageSink(message1, message2)
            .subscribeWith(new ServiceBusMessageProcessor(LINK_NAME, true, true, maxRenewDuration,
                retryOptions, errorContext, messageManagementOperations));

        // Act & Assert
        StepVerifier.create(processor)
            .expectNext(message1)
            .expectError(IllegalArgumentException.class)
            .verify();

        verify(messageManagementOperations).updateDisposition(lock1, Accepted.getInstance());

        verifyZeroInteractions(message2);
        verify(messageManagementOperations, never()).renewMessageLock(anyString(), anyString());
    }
}
