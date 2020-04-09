// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusReceiverClient}.
 */
class ServiceBusReceiverClientTest {
    private static final String NAMESPACE = "test-namespace";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final String LOCK_TOKEN = UUID.randomUUID().toString();
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(5);

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverClientTest.class);
    private ServiceBusReceiverClient client;

    @Mock
    private ServiceBusReceiverAsyncClient asyncClient;
    @Mock
    private MessageLockToken messageLockToken;
    @Mock
    private Map<String, Object> propertiesToModify;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        when(asyncClient.getEntityPath()).thenReturn(ENTITY_PATH);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);

        when(messageLockToken.getLockToken()).thenReturn(LOCK_TOKEN);

        client = new ServiceBusReceiverClient(asyncClient, OPERATION_TIMEOUT);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void nullConstructor() {
        assertThrows(NullPointerException.class, () -> new ServiceBusReceiverClient(null, OPERATION_TIMEOUT));
        assertThrows(NullPointerException.class, () -> new ServiceBusReceiverClient(asyncClient, null));
    }

    @Test
    void properties() {
        assertEquals(NAMESPACE, client.getFullyQualifiedNamespace());
        assertEquals(ENTITY_PATH, client.getEntityPath());
    }

    @Test
    void abandonMessage() {
        // Arrange
        when(asyncClient.abandon(any(MessageLockToken.class))).thenReturn(Mono.empty());

        // Act
        client.abandon(messageLockToken);

        // Assert
        verify(asyncClient).abandon(argThat(ServiceBusReceiverClientTest::lockTokenEquals));
    }

    @Test
    void abandonMessageWithProperties() {
        // Arrange
        when(asyncClient.abandon(any(MessageLockToken.class), any())).thenReturn(Mono.empty());

        // Act
        client.abandon(messageLockToken, propertiesToModify);

        // Assert
        verify(asyncClient).abandon(argThat(ServiceBusReceiverClientTest::lockTokenEquals), eq(propertiesToModify));
    }

    @Test
    void completeMessage() {
        // Arrange
        when(asyncClient.complete(any(MessageLockToken.class))).thenReturn(Mono.empty());

        // Act
        client.complete(messageLockToken);

        // Assert
        verify(asyncClient).complete(argThat(ServiceBusReceiverClientTest::lockTokenEquals));
    }

    @Test
    void deferMessage() {
        // Arrange
        when(asyncClient.defer(any(MessageLockToken.class))).thenReturn(Mono.empty());

        // Act
        client.defer(messageLockToken);

        // Assert
        verify(asyncClient).defer(argThat(ServiceBusReceiverClientTest::lockTokenEquals));
    }

    @Test
    void deferMessageWithProperties() {
        // Arrange
        when(asyncClient.defer(any(MessageLockToken.class), any())).thenReturn(Mono.empty());

        // Act
        client.defer(messageLockToken, propertiesToModify);

        // Assert
        verify(asyncClient).defer(argThat(ServiceBusReceiverClientTest::lockTokenEquals), eq(propertiesToModify));
    }

    @Test
    void deadLetterMessage() {
        // Arrange
        when(asyncClient.deadLetter(any(MessageLockToken.class))).thenReturn(Mono.empty());

        // Act
        client.deadLetter(messageLockToken);

        // Assert
        verify(asyncClient).deadLetter(argThat(ServiceBusReceiverClientTest::lockTokenEquals));
    }

    @Test
    void deadLetterMessageWithOptions() {
        // Arrange
        final DeadLetterOptions options = new DeadLetterOptions()
            .setDeadLetterErrorDescription("foo")
            .setDeadLetterReason("bar")
            .setPropertiesToModify(propertiesToModify);

        when(asyncClient.deadLetter(any(MessageLockToken.class), any())).thenReturn(Mono.empty());

        // Act
        client.deadLetter(messageLockToken, options);

        // Assert
        verify(asyncClient).deadLetter(argThat(ServiceBusReceiverClientTest::lockTokenEquals), eq(options));
    }

    @Test
    void peekMessage() {
        // Arrange
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.peek()).thenReturn(Mono.just(message));

        // Act
        final ServiceBusReceivedMessage actual = client.peek();

        // Assert
        assertEquals(message, actual);
    }

    @Test
    void peekMessageFromSequence() {
        // Arrange
        final long sequenceNumber = 154;
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.peekAt(sequenceNumber)).thenReturn(Mono.just(message));

        // Act
        final ServiceBusReceivedMessage actual = client.peekAt(sequenceNumber);

        // Assert
        assertEquals(message, actual);
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void peekBatchMessagesMax() {
        // Arrange
        final int maxMessages = 10;
        Flux<ServiceBusReceivedMessage> messages = Flux.create(sink -> {
            final AtomicInteger emittedMessages = new AtomicInteger();

            sink.onRequest(number -> {
                logger.info("Requesting {} messages.", number);
                if (emittedMessages.get() >= maxMessages) {
                    logger.info("Completing sink.");
                    sink.complete();
                    return;
                }

                for (int i = 0; i < number; i++) {
                    sink.next(mock(ServiceBusReceivedMessage.class));

                    final int emit = emittedMessages.incrementAndGet();
                    if (emit >= maxMessages) {
                        logger.info("Completing sink.");
                        sink.complete();
                        break;
                    }
                }
            });
        });
        when(asyncClient.peekBatch(maxMessages)).thenReturn(messages);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekBatch(maxMessages);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(maxMessages, collected.size());
    }

    /**
     * Verifies that the messages completes when time has elapsed.
     */
    @Test
    void peekBatchMessagesLessThan() {
        // Arrange
        final int maxMessages = 10;
        final int returnedMessages = 7;
        Flux<ServiceBusReceivedMessage> messages = Flux.create(sink -> {
            final AtomicInteger emittedMessages = new AtomicInteger();

            sink.onRequest(number -> {
                logger.info("Requesting {} messages.", number);
                if (emittedMessages.get() >= returnedMessages) {
                    logger.info("Completing sink. Max: {}", returnedMessages);
                    sink.complete();
                    return;
                }

                for (int i = 0; i < number; i++) {
                    sink.next(mock(ServiceBusReceivedMessage.class));

                    final int emit = emittedMessages.incrementAndGet();
                    if (emit >= returnedMessages) {
                        logger.info("Completing sink.", returnedMessages);
                        sink.complete();
                        break;
                    }
                }
            });
        });

        when(asyncClient.peekBatch(maxMessages)).thenReturn(messages);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekBatch(maxMessages);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(returnedMessages, collected.size());
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void peekBatchMessagesMaxSequenceNumber() {
        // Arrange
        final int maxMessages = 10;
        final long sequenceNumber = 100;
        final Flux<ServiceBusReceivedMessage> messages = Flux.create(sink -> {
            sink.onRequest(number -> {
                for (int i = 0; i < maxMessages; i++) {
                    sink.next(mock(ServiceBusReceivedMessage.class));
                }

                sink.complete();
            });
        });
        when(asyncClient.peekBatchAt(maxMessages, sequenceNumber)).thenReturn(messages);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekBatchAt(maxMessages, sequenceNumber);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(maxMessages, collected.size());
    }


    /**
     * Verifies we cannot pass null value for maxWaitTime while receiving.
     */
    @Test
    void receiveMessageNullWaitTime() {
        // Arrange
        final int maxMessages = 10;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> client.receive(maxMessages, null));
    }

    /**
     * Verifies we cannot pass negative value for maxWaitTime while receiving.
     */
    @Test
    void receiveMessageNegativeWaitTime() {
        // Arrange
        final int maxMessages = 10;
        Duration negativeReceiveWaitTime =  Duration.ofSeconds(-10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> client.receive(maxMessages, negativeReceiveWaitTime));
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void receiveMessagesWithUserSpecifiedTimeout() {
        // Arrange
        final int maxMessages = 10;
        final int numberToEmit = 5;
        final Duration receiveTimeout = Duration.ofSeconds(2);
        Flux<ServiceBusReceivedMessage> messageSink = Flux.create(sink -> {
            sink.onRequest(e -> {
                final AtomicInteger emittedMessages = new AtomicInteger();
                if (emittedMessages.get() >= numberToEmit) {
                    logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}",
                        emittedMessages.get(), numberToEmit);
                    return;
                }

                for (int i = 0; i < numberToEmit; i++) {
                    sink.next(mock(ServiceBusReceivedMessage.class));

                    final int emit = emittedMessages.incrementAndGet();
                    if (emit >= numberToEmit) {
                        logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}", emit, maxMessages);
                        break;
                    }
                }
            });

            sink.onCancel(() -> {
                logger.info("Cancelled. Completing sink.");
                sink.complete();
            });
        });
        when(asyncClient.receive()).thenReturn(messageSink);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receive(maxMessages, receiveTimeout);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(numberToEmit, collected.size());
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void receiveMessagesMax() {
        // Arrange
        final int maxMessages = 10;
        final int numberToEmit = maxMessages + 5;
        Flux<ServiceBusReceivedMessage> messageSink = Flux.create(sink -> {
            sink.onRequest(e -> {
                final AtomicInteger emittedMessages = new AtomicInteger();
                if (emittedMessages.get() >= numberToEmit) {
                    logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}",
                        emittedMessages.get(), numberToEmit);
                    return;
                }

                for (int i = 0; i < numberToEmit; i++) {
                    sink.next(mock(ServiceBusReceivedMessage.class));

                    final int emit = emittedMessages.incrementAndGet();
                    if (emit >= numberToEmit) {
                        logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}", emit, maxMessages);
                        break;
                    }
                }
            });

            sink.onCancel(() -> {
                logger.info("Cancelled. Completing sink.");
                sink.complete();
            });
        });
        when(asyncClient.receive()).thenReturn(messageSink);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receive(maxMessages);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(maxMessages, collected.size());
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void receiveMessagesTimeout() {
        // Arrange
        final int maxMessages = 10;
        final int numberToEmit = 5;
        Flux<ServiceBusReceivedMessage> messageSink = Flux.create(sink -> {
            sink.onRequest(e -> {
                final AtomicInteger emittedMessages = new AtomicInteger();
                if (emittedMessages.get() >= numberToEmit) {
                    logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}",
                        emittedMessages.get(), numberToEmit);
                    return;
                }

                for (int i = 0; i < numberToEmit; i++) {
                    sink.next(mock(ServiceBusReceivedMessage.class));

                    final int emit = emittedMessages.incrementAndGet();
                    if (emit >= numberToEmit) {
                        logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}", emit, maxMessages);
                        break;
                    }
                }
            });

            sink.onCancel(() -> {
                logger.info("Cancelled. Completing sink.");
                sink.complete();
            });
        });
        when(asyncClient.receive()).thenReturn(messageSink);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receive(maxMessages);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(numberToEmit, collected.size());
    }

    @Test
    void receiveDeferredMessage() {
        // Arrange
        final long sequenceNumber = 231412;
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.receiveDeferredMessage(anyLong())).thenReturn(Mono.just(message));

        // Act
        final ServiceBusReceivedMessage actual = client.receiveDeferredMessage(sequenceNumber);

        // Assert
        assertEquals(message, actual);

        verify(asyncClient).receiveDeferredMessage(sequenceNumber);
    }

    @Test
    void receiveDeferredMessageBatch() {
        // Arrange
        final long sequenceNumber = 154;
        final long sequenceNumber2 = 13124;
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.receiveDeferredMessageBatch(any())).thenReturn(Flux.just(message, message2));

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receiveDeferredMessageBatch(sequenceNumber, sequenceNumber2);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(2, collected.size());
        assertEquals(message, collected.get(0));
        assertEquals(message2, collected.get(1));
    }

    @Test
    void renewMessageLock() {
        // Arrange
        final Instant response = Instant.ofEpochSecond(1585259339);
        when(asyncClient.renewMessageLock(messageLockToken)).thenReturn(Mono.just(response));

        // Act
        final Instant actual = client.renewMessageLock(messageLockToken);

        // Assert
        assertEquals(response, actual);
    }

    private static boolean lockTokenEquals(MessageLockToken compared) {
        return compared != null && LOCK_TOKEN.equals(compared.getLockToken());
    }
}
