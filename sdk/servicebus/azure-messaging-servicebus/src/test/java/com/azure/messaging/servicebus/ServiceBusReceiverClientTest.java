// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServiceBusReceiverClient}.
 */
class ServiceBusReceiverClientTest {
    private static final String NAMESPACE = "test-namespace";
    private static final String ENTITY_PATH = "test-entity-path";
    private static final String LOCK_TOKEN = UUID.randomUUID().toString();
    private static final String SESSION_ID = "test-session-id";

    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(5);

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverClientTest.class);

    private ServiceBusReceiverClient client;

    @Mock
    private ServiceBusReceiverAsyncClient asyncClient;
    @Mock
    private Map<String, Object> propertiesToModify;
    @Mock
    private ServiceBusTransactionContext transactionContext;
    @Mock
    private ServiceBusReceivedMessage message;
    @Mock
    private Consumer<Throwable> onErrorConsumer;
    @Mock
    private ReceiverOptions sessionReceiverOptions;

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(asyncClient.getEntityPath()).thenReturn(ENTITY_PATH);
        when(asyncClient.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        when(asyncClient.getReceiverOptions()).thenReturn(new ReceiverOptions(ServiceBusReceiveMode.PEEK_LOCK, 0, null, false));
        when(sessionReceiverOptions.getSessionId()).thenReturn(SESSION_ID);
        client = new ServiceBusReceiverClient(asyncClient, OPERATION_TIMEOUT);
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMock(this);
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
    void abandonMessageWithTransaction() {
        AbandonOptions options = new AbandonOptions().setTransactionContext(transactionContext);
        // Arrange
        when(asyncClient.abandon(eq(message), eq(options))).thenReturn(Mono.empty());

        // Act
        client.abandon(message, options);

        // Assert
        verify(asyncClient).abandon(eq(message), eq(options));
    }

    @Test
    void abandonMessage() {
        // Arrange
        when(asyncClient.abandon(eq(message))).thenReturn(Mono.empty());

        // Act
        client.abandon(message);

        // Assert
        verify(asyncClient).abandon(eq(message));
    }

    @Test
    void abandonMessageWithProperties() {
        AbandonOptions options = new AbandonOptions().setPropertiesToModify(propertiesToModify);
        // Arrange
        when(asyncClient.abandon(eq(message), eq(options))).thenReturn(Mono.empty());

        // Act
        client.abandon(message, options);

        // Assert
        verify(asyncClient).abandon(eq(message), eq(options));
    }

    /**
     * Verifies that we can auto-renew a message lock.
     */
    @Test
    void autoRenewMessageLock() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final TestPublisher<Void> publisher = TestPublisher.create();

        when(message.getLockToken()).thenReturn(LOCK_TOKEN);

        doAnswer(answer -> {
            fail("On error should not have been invoked.");
            return null;
        }).when(onErrorConsumer).accept(any());
        when(asyncClient.renewMessageLock(message, maxDuration)).thenReturn(publisher.mono());

        // Act
        client.renewMessageLock(message, maxDuration, onErrorConsumer);

        // Assert
        verify(asyncClient).renewMessageLock(message, maxDuration);
    }

    /**
     * Verifies that we can auto-renew a message lock and it calls the error consumer.
     */
    @Test
    void autoRenewMessageLockFails() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final TestPublisher<Void> publisher = TestPublisher.create();
        final Throwable testError = new IllegalAccessException("Some exception");

        when(message.getLockToken()).thenReturn(LOCK_TOKEN);

        when(asyncClient.renewMessageLock(message, maxDuration)).thenReturn(publisher.mono());

        client.renewMessageLock(message, maxDuration, onErrorConsumer);

        // Act
        publisher.error(testError);

        // Assert
        verify(asyncClient).renewMessageLock(message, maxDuration);
        verify(onErrorConsumer).accept(testError);
    }

    /**
     * Verifies that we can auto-renew a message lock and it will not fail with an NPE when we have a null onError.
     */
    @Test
    void autoRenewMessageLockFailsNull() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final TestPublisher<Void> publisher = TestPublisher.create();
        final Throwable testError = new IllegalAccessException("Some exception");

        when(asyncClient.renewMessageLock(message, maxDuration)).thenReturn(publisher.mono());

        client.renewMessageLock(message, maxDuration, null);

        // Act
        publisher.error(testError);

        // Assert
        verify(asyncClient).renewMessageLock(message, maxDuration);
        verify(onErrorConsumer, never()).accept(testError);
    }

    /**
     * Verifies that we can auto-renew a session lock.
     */
    @Test
    void autoRenewSessionLock() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final TestPublisher<Void> publisher = TestPublisher.create();

        doAnswer(answer -> {
            fail("On error should not have been invoked.");
            return null;
        }).when(onErrorConsumer).accept(any());
        when(asyncClient.renewSessionLock(maxDuration)).thenReturn(publisher.mono());

        // Act
        client.renewSessionLock(maxDuration, onErrorConsumer);

        // Assert
        verify(asyncClient).renewSessionLock(maxDuration);
    }

    /**
     * Verifies that we can auto-renew a session lock and it calls the error consumer.
     */
    @Test
    void autoRenewSessionLockFails() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final TestPublisher<Void> publisher = TestPublisher.create();
        final Throwable testError = new IllegalAccessException("Some exception");

        when(asyncClient.renewSessionLock(maxDuration)).thenReturn(publisher.mono());

        client.renewSessionLock(maxDuration, onErrorConsumer);

        // Act
        publisher.error(testError);

        // Assert
        verify(asyncClient).renewSessionLock(maxDuration);
        verify(onErrorConsumer).accept(testError);
    }

    /**
     * Verifies that we can auto-renew a message lock and it will not fail with an NPE when we have a null onError.
     */
    @Test
    void autoRenewSessionLockFailsNull() {
        // Arrange
        final Duration maxDuration = Duration.ofSeconds(8);
        final TestPublisher<Void> publisher = TestPublisher.create();
        final Throwable testError = new IllegalAccessException("Some exception");

        when(asyncClient.renewSessionLock(maxDuration)).thenReturn(publisher.mono());

        client.renewSessionLock(maxDuration, null);

        // Act
        publisher.error(testError);

        // Assert
        verify(asyncClient).renewSessionLock(maxDuration);
        verify(onErrorConsumer, never()).accept(testError);
    }

    @Test
    void completeMessageWithTransaction() {
        CompleteOptions options = new CompleteOptions().setTransactionContext(transactionContext);
        // Arrange
        when(asyncClient.complete(eq(message), eq(options))).thenReturn(Mono.empty());

        // Act
        client.complete(message, options);

        // Assert
        verify(asyncClient).complete(eq(message), eq(options));
    }

    @Test
    void completeMessage() {
        // Arrange
        when(asyncClient.complete(eq(message))).thenReturn(Mono.empty());

        // Act
        client.complete(message);

        // Assert
        verify(asyncClient).complete(eq(message));
    }

    @Test
    void deferMessage() {
        // Arrange
        when(asyncClient.defer(eq(message))).thenReturn(Mono.empty());

        // Act
        client.defer(message);

        // Assert
        verify(asyncClient).defer(eq(message));
    }

    @Test
    void deferMessageWithPropertiesWithTransaction() {
        DeferOptions options = new DeferOptions()
            .setTransactionContext(transactionContext)
            .setPropertiesToModify(propertiesToModify);
        // Arrange
        when(asyncClient.defer(eq(message), eq(options))).thenReturn(Mono.empty());

        // Act
        client.defer(message, options);

        // Assert
        verify(asyncClient).defer(eq(message), eq(options));
    }

    @Test
    void deferMessageWithProperties() {
        DeferOptions options = new DeferOptions()
            .setPropertiesToModify(propertiesToModify);
        // Arrange
        when(asyncClient.defer(eq(message), eq(options))).thenReturn(Mono.empty());

        // Act
        client.defer(message, options);

        // Assert
        verify(asyncClient).defer(eq(message), eq(options));
    }

    @Test
    void deadLetterMessage() {
        // Arrange
        when(asyncClient.deadLetter(eq(message))).thenReturn(Mono.empty());

        // Act
        client.deadLetter(message);

        // Assert
        verify(asyncClient).deadLetter(eq(message));
    }

    @Test
    void deadLetterMessageWithOptionsWithTransaction() {
        // Arrange
        final DeadLetterOptions options = new DeadLetterOptions()
            .setDeadLetterErrorDescription("foo")
            .setDeadLetterReason("bar")
            .setPropertiesToModify(propertiesToModify)
            .setTransactionContext(transactionContext);

        when(asyncClient.deadLetter(eq(message), any(DeadLetterOptions.class)))
            .thenReturn(Mono.empty());

        // Act
        client.deadLetter(message, options);

        // Assert
        verify(asyncClient).deadLetter(eq(message), eq(options));
    }

    @Test
    void deadLetterMessageWithOptions() {
        // Arrange
        final DeadLetterOptions options = new DeadLetterOptions()
            .setDeadLetterErrorDescription("foo")
            .setDeadLetterReason("bar")
            .setPropertiesToModify(propertiesToModify);

        when(asyncClient.deadLetter(eq(message), any(DeadLetterOptions.class)))
            .thenReturn(Mono.empty());

        // Act
        client.deadLetter(message, options);

        // Assert
        verify(asyncClient).deadLetter(eq(message), eq(options));
    }

    @Test
    void getSessionState() {
        // Arrange
        final byte[] contents = new byte[]{10, 111, 23};
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.getSessionState(SESSION_ID)).thenReturn(Mono.just(contents));

        // Act
        final byte[] actual = client.getSessionState();

        // Assert
        assertEquals(contents, actual);
    }

    @Test
    void getSessionStateNull() {
        // Arrange
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.getSessionState(SESSION_ID)).thenReturn(Mono.empty());

        // Act
        final byte[] actual = client.getSessionState();

        // Assert
        assertNull(actual);
    }

    @Test
    void peekMessage() {
        // Arrange
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessage(SESSION_ID)).thenReturn(Mono.just(message));

        // Act
        final ServiceBusReceivedMessage actual = client.peekMessage();

        // Assert
        assertEquals(message, actual);
    }

    @Test
    void peekMessageEmptyEntity() {
        // Arrange
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessage(SESSION_ID)).thenReturn(Mono.empty());

        // Act
        final ServiceBusReceivedMessage actual = client.peekMessage();

        // Assert
        assertNull(actual);
    }

    @Test
    void peekMessageFromSequence() {
        // Arrange
        final long sequenceNumber = 154;
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessage(sequenceNumber, SESSION_ID)).thenReturn(Mono.just(message));

        // Act
        final ServiceBusReceivedMessage actual = client.peekMessage(sequenceNumber);

        // Assert
        assertEquals(message, actual);
    }

    /**
     * Verifies there is no error when there are no messages returned.
     */
    @Test
    void peekMessagesEmptyEntity() {
        // Arrange
        final int maxMessages = 10;
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessages(maxMessages, SESSION_ID)).thenReturn(Flux.empty());

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekMessages(maxMessages);

        // Assert
        assertNotNull(actual);

        final Optional<ServiceBusReceivedMessage> anyMessages = actual.stream().findAny();
        assertFalse(anyMessages.isPresent());
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void peekMessagesMax() {
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

        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessages(maxMessages, SESSION_ID)).thenReturn(messages);


        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekMessages(maxMessages);

        // Assert
        assertNotNull(actual);

        final List<ServiceBusReceivedMessage> collected = actual.stream().collect(Collectors.toList());
        assertEquals(maxMessages, collected.size());
    }

    /**
     * Verifies that the messages completes when time has elapsed.
     */
    @Test
    void peekMessagesLessThan() {
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

        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessages(maxMessages, SESSION_ID)).thenReturn(messages);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekMessages(maxMessages);

        // Assert
        assertNotNull(actual);

        final long collected = actual.stream().count();
        assertEquals(returnedMessages, collected);
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void peekMessagesMaxSequenceNumber() {
        // Arrange
        final int maxMessages = 10;
        final long sequenceNumber = 100;
        final Flux<ServiceBusReceivedMessage> messages = Flux.create(sink -> sink.onRequest(number -> {
            for (int i = 0; i < maxMessages; i++) {
                sink.next(mock(ServiceBusReceivedMessage.class));
            }

            sink.complete();
        }));
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.peekMessages(maxMessages, sequenceNumber, SESSION_ID)).thenReturn(messages);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.peekMessages(maxMessages, sequenceNumber);

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
        assertThrows(NullPointerException.class, () -> client.receiveMessages(maxMessages, null));
    }

    /**
     * Verifies we cannot pass negative value for maxWaitTime while receiving.
     */
    @Test
    void receiveMessageNegativeWaitTime() {
        // Arrange
        final int maxMessages = 10;
        Duration negativeReceiveWaitTime = Duration.ofSeconds(-10);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> client.receiveMessages(maxMessages, negativeReceiveWaitTime));
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
        final AtomicInteger emittedMessages = new AtomicInteger();
        Flux<ServiceBusReceivedMessage> messageSink = Flux.create(sink -> {
            sink.onRequest(e -> {
                if (emittedMessages.get() >= numberToEmit) {
                    logger.info("Cannot emit more. Reached max already. Emitted: {}. Max: {}",
                        emittedMessages.get(), numberToEmit);
                    return;
                }

                for (int i = 0; i < numberToEmit; i++) {
                    ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
                    sink.next(message);

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
        when(asyncClient.receiveMessagesNoBackPressure()).thenReturn(messageSink);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receiveMessages(maxMessages, receiveTimeout);

        // Assert
        assertNotNull(actual);

        final long collected = actual.stream().count();
        assertEquals(numberToEmit, collected);
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

        when(asyncClient.receiveMessagesNoBackPressure()).thenReturn(messageSink);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receiveMessages(maxMessages);

        // Assert
        assertNotNull(actual);

        final long collected = actual.stream().count();
        assertEquals(maxMessages, collected);
    }

    /**
     * Verifies that all requested messages are returned when we can satisfy them all.
     */
    @Test
    void receiveMessagesTimeout() {
        // Arrange
        final int maxMessages = 10;
        final int numberToEmit = 5;

        final AtomicInteger emittedMessages = new AtomicInteger();
        Flux<ServiceBusReceivedMessage> messageSink = Flux.create(sink -> {
            sink.onRequest(e -> {
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
        when(asyncClient.receiveMessagesNoBackPressure()).thenReturn(messageSink);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receiveMessages(maxMessages);

        // Assert
        assertNotNull(actual);

        final long collected = actual.stream().count();
        assertEquals(numberToEmit, collected);
    }

    @Test
    void receiveDeferredMessage() {
        // Arrange
        final long sequenceNumber = 231412;
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.receiveDeferredMessage(sequenceNumber, SESSION_ID)).thenReturn(Mono.just(message));

        // Act
        final ServiceBusReceivedMessage actual = client.receiveDeferredMessage(sequenceNumber);

        // Assert
        assertEquals(message, actual);

        verify(asyncClient).receiveDeferredMessage(sequenceNumber, SESSION_ID);
    }

    @Test
    void receiveDeferredMessageBatch() {
        // Arrange
        final long sequenceNumber = 154;
        final long sequenceNumber2 = 13124;
        final ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        final ServiceBusReceivedMessage message2 = mock(ServiceBusReceivedMessage.class);
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.receiveDeferredMessages(any(), eq(SESSION_ID))).thenReturn(Flux.just(message, message2));
        List<Long> collection = Arrays.asList(sequenceNumber, sequenceNumber2);

        // Act
        final IterableStream<ServiceBusReceivedMessage> actual = client.receiveDeferredMessageBatch(collection);

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
        final OffsetDateTime response = Instant.ofEpochSecond(1585259339).atOffset(ZoneOffset.UTC);
        when(asyncClient.renewMessageLock(message)).thenReturn(Mono.just(response));

        // Act
        final OffsetDateTime actual = client.renewMessageLock(message);

        // Assert
        assertEquals(response, actual);
    }

    @Test
    void renewSessionLock() {
        // Arrange
        final String sessionId = "a-session-id";
        final OffsetDateTime response = Instant.ofEpochSecond(1585259339).atOffset(ZoneOffset.UTC);
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.renewSessionLock(SESSION_ID)).thenReturn(Mono.just(response));


        // Act
        final OffsetDateTime actual = client.renewSessionLock();

        // Assert
        assertEquals(response, actual);
    }

    @Test
    void setSessionState() {
        // Arrange
        final byte[] contents = new byte[]{10, 111, 23};
        when(asyncClient.getReceiverOptions()).thenReturn(sessionReceiverOptions);
        when(asyncClient.setSessionState(SESSION_ID, contents)).thenReturn(Mono.empty());

        // Act
        client.setSessionState(contents);

        // Assert
        verify(asyncClient).setSessionState(SESSION_ID, contents);
    }
}
