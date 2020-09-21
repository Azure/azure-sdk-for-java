// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpDataBody;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.amqp.models.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.models.DeadLetterOptions;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.models.SubQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ServiceBusReceiverAsyncClient} from queues or subscriptions.
 */
@Tag("integration")
class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);
    private final AtomicInteger messagesPending = new AtomicInteger();
    private final List<Long> messagesDeferredPending = new ArrayList<>();
    private final boolean isSessionEnabled = false;

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;

    /**
     * Receiver used to clean up resources in {@link #afterTest()}.
     */
    private ServiceBusReceiverAsyncClient receiveAndDeleteReceiver;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        sharedBuilder = null;
        final int pending = messagesPending.get();
        final int pendingDeferred = messagesDeferredPending.size();
        if (pending < 1 && pendingDeferred < 1) {
            dispose(receiver, sender, receiveAndDeleteReceiver);
            return;
        }

        // In the case that this test failed... we're going to drain the queue or subscription.
        try {
            if (pending > 0) {
                receiveAndDeleteReceiver.receiveMessages()
                    .map(message -> {
                        logger.info("Message received: {}", message.getMessage().getSequenceNumber());
                        return message;
                    })
                    .timeout(Duration.ofSeconds(15), Mono.empty())
                    .blockLast();
            }
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        }

        try {
            if (pendingDeferred > 0) {
                for (Long sequenceNumber : messagesDeferredPending) {
                    receiveAndDeleteReceiver.receiveDeferredMessage(sequenceNumber)
                        .map(message -> {
                            logger.info("Message received: {}", message.getSequenceNumber());
                            return message;
                        })
                        .timeout(Duration.ofSeconds(15), Mono.empty())
                        .block();
                }
            }
        } catch (Exception e) {
            logger.warning("Error occurred when draining for deferred messages.", e);
        } finally {
            dispose(receiver, sender, receiveAndDeleteReceiver);
        }
    }

    /**
     * Verifies that we can create multiple transaction using sender and receiver.
     */
    @Test
    void createMultipleTransactionTest() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, 0, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(receiver.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(receiver.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    /**
     * Verifies that we can create transaction and complete.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void createTransactionAndRollbackMessagesTest(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(OPERATION_TIMEOUT);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(receiver.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(OPERATION_TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.complete(receivedMessage, transaction.get()))
            .verifyComplete();

        StepVerifier.create(receiver.rollbackTransaction(transaction.get()))
            .verifyComplete();
    }

    /**
     * Verifies that we can do following using shared connection and on non session entity. 1. create transaction 2.
     * receive and settle with transactionContext. 3. commit Rollback this transaction.
     */
    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void transactionSendReceiveAndCommit(DispositionStatus dispositionStatus) {

        // Arrange
        final MessagingEntityType entityType = MessagingEntityType.QUEUE;
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_PEEK_TRANSACTION_SENDRECEIVE_AND_COMPLETE, isSessionEnabled);

        final String messageId1 = UUID.randomUUID().toString();
        final ServiceBusMessage message1 = getMessage(messageId1, isSessionEnabled);
        final String deadLetterReason = "test reason";

        sendMessage(message1).block(TIMEOUT);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(receiver.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        // Assert & Act
        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        final Mono<Void> operation;
        switch (dispositionStatus) {
            case COMPLETED:
                operation = receiver.complete(receivedMessage, transaction.get());
                messagesPending.decrementAndGet();
                break;
            case ABANDONED:
                operation = receiver.abandon(receivedMessage, null, transaction.get());
                break;
            case SUSPENDED:
                DeadLetterOptions deadLetterOptions = new DeadLetterOptions().setDeadLetterReason(deadLetterReason);
                operation = receiver.deadLetter(receivedMessage, deadLetterOptions, transaction.get());
                messagesPending.decrementAndGet();
                break;
            case DEFERRED:
                operation = receiver.defer(receivedMessage, null, transaction.get());
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Disposition status not recognized for this test case: " + dispositionStatus));
        }

        StepVerifier.create(operation)
            .verifyComplete();

        StepVerifier.create(receiver.commitTransaction(transaction.get()))
            .verifyComplete();

        if (dispositionStatus == DispositionStatus.DEFERRED) {
            messagesDeferredPending.add(receivedMessage.getSequenceNumber());
        }
    }

    /**
     * Verifies that we can do following on different clients i.e. sender and receiver. 1. create transaction using
     * sender 2. receive and complete with transactionContext. 3. Commit this transaction using sender.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    @Disabled
    void transactionReceiveCompleteCommitMixClient(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled, true);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        // Assert & Act
        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        StepVerifier.create(receiver.complete(receivedMessage, transaction.get()))
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .verifyComplete();
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final List<ServiceBusReceivedMessage> receivedMessages = new ArrayList<>();

        Mono.when(sendMessage(message), sendMessage(message)).block(TIMEOUT);

        // Assert & Act
        try {
            StepVerifier.create(receiver.receiveMessages())
                .assertNext(receivedMessage -> {
                    receivedMessages.add(receivedMessage.getMessage());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .assertNext(receivedMessage -> {
                    receivedMessages.add(receivedMessage.getMessage());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .thenCancel()
                .verify();
        } finally {
            int numberCompleted = completeMessages(receiver, receivedMessages);
            messagesPending.addAndGet(-numberCompleted);
        }
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final List<ServiceBusReceivedMessage> receivedMessages = new ArrayList<>();

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        try {
            StepVerifier.create(receiver.receiveMessages())
                .assertNext(receivedMessage -> {
                    receivedMessages.add(receivedMessage.getMessage());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .thenCancel()
                .verify();
        } finally {
            int numberCompleted = completeMessages(receiver, receivedMessages);
            messagesPending.addAndGet(-numberCompleted);
        }
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 1, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.peekMessage())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .verifyComplete();
    }

    /**
     * Verifies that an empty entity does not error when peeking.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessageEmptyEntity(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_EMPTY_ENTITY, isSessionEnabled);

        final int fromSequenceNumber = 1;

        // Assert & Act
        StepVerifier.create(receiver.peekMessageAt(fromSequenceNumber))
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void sendScheduledMessageAndReceive(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final OffsetDateTime scheduledEnqueueTime = OffsetDateTime.now().plusSeconds(2);

        sender.scheduleMessage(message, scheduledEnqueueTime).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(Mono.delay(Duration.ofSeconds(3)).then(receiveAndDeleteReceiver.receiveMessages().next()))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can cancel a scheduled message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void cancelScheduledMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final OffsetDateTime scheduledEnqueueTime = OffsetDateTime.now().plusSeconds(10);
        final Duration delayDuration = Duration.ofSeconds(3);

        final Long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime).block(TIMEOUT);
        logger.verbose("Scheduled the message, sequence number {}.", sequenceNumber);

        assertNotNull(sequenceNumber);

        Mono.delay(delayDuration)
            .then(sender.cancelScheduledMessage(sequenceNumber))
            .block(TIMEOUT);

        messagesPending.decrementAndGet();
        logger.verbose("Cancelled the scheduled message, sequence number {}.", sequenceNumber);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages().take(1))
            .thenAwait(Duration.ofSeconds(5))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekFromSequenceNumberMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 3, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        try {
            StepVerifier.create(receiver.peekMessageAt(receivedMessage.getSequenceNumber()))
                .assertNext(m -> {
                    assertEquals(receivedMessage.getSequenceNumber(), m.getSequenceNumber());
                    assertMessageEquals(m, messageId, isSessionEnabled);
                })
                .verifyComplete();
        } finally {
            receiver.complete(receivedMessage)
                .block(Duration.ofSeconds(10));
            messagesPending.decrementAndGet();
        }
    }

    /**
     * Verifies that we can send and peek a batch of messages and the sequence number is tracked correctly.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessages(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_PEEK_BATCH_MESSAGES, isSessionEnabled);

        final BiConsumer<ServiceBusReceivedMessage, Integer> checkCorrectMessage = (message, index) -> {
            final Map<String, Object> properties = message.getApplicationProperties();
            final Object value = properties.get(MESSAGE_POSITION_ID);
            assertTrue(value instanceof Integer, "Did not contain correct position number: " + value);

            final int position = (int) value;
            assertEquals(index, position);
        };
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(10, messageId, CONTENTS_BYTES);
        if (isSessionEnabled) {
            messages.forEach(m -> m.setSessionId(sessionId));
        }

        sendMessage(messages).block(TIMEOUT);

        // Assert & Act
        try {
            StepVerifier.create(receiver.peekMessages(3))
                .assertNext(message -> checkCorrectMessage.accept(message, 0))
                .assertNext(message -> checkCorrectMessage.accept(message, 1))
                .assertNext(message -> checkCorrectMessage.accept(message, 2))
                .verifyComplete();

            StepVerifier.create(receiver.peekMessages(4))
                .assertNext(message -> checkCorrectMessage.accept(message, 3))
                .assertNext(message -> checkCorrectMessage.accept(message, 4))
                .assertNext(message -> checkCorrectMessage.accept(message, 5))
                .assertNext(message -> checkCorrectMessage.accept(message, 6))
                .verifyComplete();

            StepVerifier.create(receiver.peekMessage())
                .assertNext(message -> checkCorrectMessage.accept(message, 7))
                .verifyComplete();
        } finally {
            receiveAndDeleteReceiver.receiveMessages()
                .take(messages.size())
                .blockLast(Duration.ofSeconds(15));

            messagesPending.addAndGet(-messages.size());
        }
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void peekMessagesFromSequence(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_PEEK_MESSAGE_FROM_SEQUENCE, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 2;
        final int fromSequenceNumber = 1;

        Mono.when(sendMessage(message), sendMessage(message)).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.peekMessagesAt(maxMessages, fromSequenceNumber))
            .expectNextCount(maxMessages)
            .verifyComplete();
    }

    /**
     * Verifies that an empty entity does not error when peeking.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessagesFromSequenceEmptyEntity(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_EMPTY_ENTITY, isSessionEnabled);

        final int maxMessages = 10;
        final int fromSequenceNumber = 1;

        // Assert & Act
        StepVerifier.create(receiver.peekMessagesAt(maxMessages, fromSequenceNumber))
            .verifyComplete();
    }

    /**
     * Verifies that we can dead-letter a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void deadLetterMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.deadLetter(receivedMessage))
            .verifyComplete();

        messagesPending.decrementAndGet();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.complete(receivedMessage))
            .verifyComplete();

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can renew message lock on a non-session receiver.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getLockedUntil());

        final OffsetDateTime initialLock = receivedMessage.getLockedUntil();
        logger.info("Received message. Seq: {}. lockedUntil: {}", receivedMessage.getSequenceNumber(), initialLock);

        // Assert & Act
        try {
            StepVerifier.create(Mono.delay(Duration.ofSeconds(7))
                .then(Mono.defer(() -> receiver.renewMessageLock(receivedMessage))))
                .assertNext(lockedUntil -> {
                    assertTrue(lockedUntil.isAfter(initialLock),
                        String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                            lockedUntil, initialLock));

                })
                .verifyComplete();
        } finally {
            logger.info("Completing message. Seq: {}.", receivedMessage.getSequenceNumber());

            receiver.complete(receivedMessage)
                .doOnSuccess(aVoid -> messagesPending.decrementAndGet())
                .block(TIMEOUT);
        }
    }

    /**
     * Verifies that the lock can be automatically renewed.
     */
    @Disabled("Auto-lock renewal is not enabled.")
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void autoRenewLockOnReceiveMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        // Send the message to verify.
        sendMessage(message).block(TIMEOUT);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().map(ServiceBusReceivedMessageContext::getMessage))
            .assertNext(received -> {
                assertNotNull(received.getLockedUntil());
                assertNotNull(received.getLockToken());

                logger.info("{}: lockToken[{}]. lockedUntil[{}]. now[{}]", received.getSequenceNumber(),
                    received.getLockToken(), received.getLockedUntil(), OffsetDateTime.now());

                final OffsetDateTime initial = received.getLockedUntil();
                final OffsetDateTime timeToStop = initial.plusSeconds(5);
                OffsetDateTime latest = OffsetDateTime.MIN;

                // Simulate some sort of long processing.
                final AtomicInteger iteration = new AtomicInteger();
                while (OffsetDateTime.now().isBefore(timeToStop)) {
                    logger.info("Iteration {}: {}. Time to stop: {}", iteration.incrementAndGet(), OffsetDateTime.now(), timeToStop);

                    try {
                        TimeUnit.SECONDS.sleep(4);
                    } catch (InterruptedException error) {
                        logger.error("Error occurred while sleeping: " + error);
                    }

                    assertNotNull(received.getLockedUntil());
                    latest = received.getLockedUntil();
                }

                try {
                    assertTrue(initial.isBefore(latest), String.format(
                        "Latest should be after or equal to initial. initial: %s. latest: %s", initial, latest));
                } finally {
                    logger.info("Completing message.");
                    receiver.complete(received).block(Duration.ofSeconds(15));
                    messagesPending.decrementAndGet();
                }
            })
            .thenCancel()
            .verify(Duration.ofMinutes(2));
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.abandon(receivedMessage))
            .verifyComplete();

        messagesPending.decrementAndGet();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndDefer(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_PEEK_RECEIVE_AND_DEFER, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();

        assertNotNull(receivedMessage);

        // Act & Assert
        StepVerifier.create(receiver.defer(receivedMessage))
            .verifyComplete();

        messagesPending.decrementAndGet();
        completeDeferredMessages(receiver, receivedMessage);

    }

    /**
     * Test we can receive a deferred message via sequence number and then perform abandon, suspend, or complete on it.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#receiveDeferredMessageBySequenceNumber")
    @ParameterizedTest
    void receiveDeferredMessageBySequenceNumber(MessagingEntityType entityType, DispositionStatus dispositionStatus) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        sendMessage(message).block(TIMEOUT);
        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        receiver.defer(receivedMessage).block(TIMEOUT);

        final ServiceBusReceivedMessage receivedDeferredMessage = receiver
            .receiveDeferredMessage(receivedMessage.getSequenceNumber())
            .block(TIMEOUT);

        assertNotNull(receivedDeferredMessage);
        assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        final Mono<Void> operation;
        switch (dispositionStatus) {
            case ABANDONED:
                operation = receiver.abandon(receivedDeferredMessage);
                messagesDeferredPending.add(receivedDeferredMessage.getSequenceNumber());
                break;
            case SUSPENDED:
                operation = receiver.deadLetter(receivedDeferredMessage);
                break;
            case COMPLETED:
                operation = receiver.complete(receivedDeferredMessage);
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Disposition status not recognized for this test case: " + dispositionStatus));
        }

        // Assert & Act
        StepVerifier.create(operation)
            .expectComplete()
            .verify();

        if (dispositionStatus != DispositionStatus.COMPLETED) {
            messagesPending.decrementAndGet();
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SEND_RECEIVE_WITH_PROPERTIES, isSessionEnabled);

        final boolean isSessionEnabled = true;
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = getMessage(messageId, isSessionEnabled);

        Map<String, Object> sentProperties = messageToSend.getApplicationProperties();
        sentProperties.put("NullProperty", null);
        sentProperties.put("BooleanProperty", true);
        sentProperties.put("ByteProperty", (byte) 1);
        sentProperties.put("ShortProperty", (short) 2);
        sentProperties.put("IntProperty", 3);
        sentProperties.put("LongProperty", 4L);
        sentProperties.put("FloatProperty", 5.5f);
        sentProperties.put("DoubleProperty", 6.6f);
        sentProperties.put("CharProperty", 'z');
        sentProperties.put("UUIDProperty", UUID.randomUUID());
        sentProperties.put("StringProperty", "string");

        sendMessage(messageToSend).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiveAndDeleteReceiver.receiveMessages())
            .assertNext(receivedMessage -> {
                messagesPending.decrementAndGet();
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);

                final Map<String, Object> received = receivedMessage.getMessage().getApplicationProperties();

                assertEquals(sentProperties.size(), received.size());

                for (Map.Entry<String, Object> sentEntry : sentProperties.entrySet()) {
                    if (sentEntry.getValue() != null && sentEntry.getValue().getClass().isArray()) {
                        assertArrayEquals((Object[]) sentEntry.getValue(), (Object[]) received.get(sentEntry.getKey()));
                    } else {
                        final Object expected = sentEntry.getValue();
                        final Object actual = received.get(sentEntry.getKey());

                        assertEquals(expected, actual, String.format(
                            "Key '%s' does not match. Expected: '%s'. Actual: '%s'", sentEntry.getKey(), expected,
                            actual));
                    }
                }
            })
            .thenCancel()
            .verify();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void setAndGetSessionState(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, true);

        final byte[] sessionState = "Finished".getBytes(UTF_8);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = getMessage(messageId, true);

        sendMessage(messageToSend).block(Duration.ofSeconds(10));

        // Act
        AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        //AtomicReference<String> session = new AtomicReference<>();
        StepVerifier.create(receiver.receiveMessages()
            .take(1)
            .flatMap(m -> {
                logger.info("SessionId: {}. LockToken: {}. LockedUntil: {}. Message received.",
                    m.getSessionId(), m.getMessage().getLockToken(), m.getMessage().getLockedUntil());
                receivedMessage.set(m.getMessage());
                return receiver.setSessionState(sessionId, sessionState);
            }))
            .expectComplete()
            .verify();

        StepVerifier.create(receiver.getSessionState(sessionId))
            .assertNext(state -> {
                logger.info("State received: {}", new String(state, UTF_8));
                assertArrayEquals(sessionState, state);
            })
            .verifyComplete();

        receiver.complete(receivedMessage.get()).block(Duration.ofSeconds(15));
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can receive a message from dead letter queue.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receiveFromDeadLetter(MessagingEntityType entityType) {
        // Arrange
        final boolean isSessionEnabled = false;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        ServiceBusReceiverAsyncClient deadLetterReceiver = getDeadLetterReceiverBuilder(false, entityType,
            0, Function.identity())
            .buildAsyncClient();

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final List<ServiceBusReceivedMessage> receivedMessages = new ArrayList<>();

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receiveMessages().next()
            .block(OPERATION_TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        StepVerifier.create(receiver.deadLetter(receivedMessage))
            .verifyComplete();

        // Assert & Act
        try {
            StepVerifier.create(deadLetterReceiver.receiveMessages().take(1))
                .assertNext(messageContext -> {
                    receivedMessages.add(messageContext.getMessage());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .thenCancel()
                .verify();
        } finally {
            int numberCompleted = completeMessages(deadLetterReceiver, receivedMessages);
            messagesPending.addAndGet(-numberCompleted);
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void renewMessageLock(MessagingEntityType entityType) throws InterruptedException {
        // Arrange
        final boolean isSessionEnabled = false;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final Duration maximumDuration = Duration.ofSeconds(35);
        final Duration sleepDuration = maximumDuration.plusMillis(500);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        final ServiceBusReceivedMessageContext receivedContext = sendMessage(message)
            .then(receiver.receiveMessages().next())
            .block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        final OffsetDateTime lockedUntil = receivedMessage.getLockedUntil();
        assertNotNull(lockedUntil);

        // Assert & Act
        StepVerifier.create(receiver.renewMessageLock(receivedMessage, maximumDuration))
            .thenAwait(sleepDuration)
            .then(() -> {
                logger.info("Completing message.");
                int numberCompleted = completeMessages(receiver, Collections.singletonList(receivedMessage));

                messagesPending.addAndGet(-numberCompleted);
            })
            .expectComplete()
            .verify(Duration.ofMinutes(3));
    }

    /**
     * Verifies that we can receive a message which have different section set (i.e header, footer, annotations,
     * application properties etc).
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receiveAndValidateProperties(MessagingEntityType entityType) {
        // Arrange
        final boolean isSessionEnabled = false;
        final String subject = "subject";
        final Map<String, Object> footer = new HashMap<>();
        footer.put("footer-key-1", "footer-value-1");
        footer.put("footer-key-2", "footer-value-2");

        final Map<String, Object> aplicaitonProperties = new HashMap<>();
        aplicaitonProperties.put("ap-key-1", "ap-value-1");
        aplicaitonProperties.put("ap-key-2", "ap-value-2");

        final Map<String, Object> deliveryAnnotation = new HashMap<>();
        deliveryAnnotation.put("delivery-annotations-key-1", "delivery-annotations-value-1");
        deliveryAnnotation.put("delivery-annotations-key-2", "delivery-annotations-value-2");

        setSenderAndReceiver(entityType, TestUtils.USE_CASE_VALIDATE_AMQP_PROPERTIES, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final AmqpAnnotatedMessage expectedAmqpProperties = new AmqpAnnotatedMessage(new AmqpDataBody(Collections.singletonList(new BinaryData(CONTENTS_BYTES))));
        expectedAmqpProperties.getProperties().setSubject(subject);
        expectedAmqpProperties.getProperties().setReplyToGroupId("r-gid");
        expectedAmqpProperties.getProperties().setReplyTo("replyto");
        expectedAmqpProperties.getProperties().setContentType("content-type");
        expectedAmqpProperties.getProperties().setCorrelationId("corelation-id");
        expectedAmqpProperties.getProperties().setTo("to");
        expectedAmqpProperties.getProperties().setAbsoluteExpiryTime(OffsetDateTime.now().plusSeconds(60));
        expectedAmqpProperties.getProperties().setUserId("user-id-1".getBytes());
        expectedAmqpProperties.getProperties().setContentEncoding("string");
        expectedAmqpProperties.getProperties().setGroupSequence(Long.valueOf(2));
        expectedAmqpProperties.getProperties().setCreationTime(OffsetDateTime.now().plusSeconds(30));

        expectedAmqpProperties.getHeader().setPriority(Short.valueOf((short) 2));
        expectedAmqpProperties.getHeader().setFirstAcquirer(true);
        expectedAmqpProperties.getHeader().setDurable(true);

        expectedAmqpProperties.getFooter().putAll(footer);
        expectedAmqpProperties.getDeliveryAnnotations().putAll(deliveryAnnotation);
        expectedAmqpProperties.getApplicationProperties().putAll(aplicaitonProperties);

        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        final AmqpAnnotatedMessage amqpAnnotatedMessage = message.getAmqpAnnotatedMessage();
        amqpAnnotatedMessage.getMessageAnnotations().putAll(expectedAmqpProperties.getMessageAnnotations());
        amqpAnnotatedMessage.getApplicationProperties().putAll(expectedAmqpProperties.getApplicationProperties());
        amqpAnnotatedMessage.getDeliveryAnnotations().putAll(expectedAmqpProperties.getDeliveryAnnotations());
        amqpAnnotatedMessage.getFooter().putAll(expectedAmqpProperties.getFooter());

        final AmqpMessageHeader header = amqpAnnotatedMessage.getHeader();
        header.setFirstAcquirer(expectedAmqpProperties.getHeader().isFirstAcquirer());
        header.setTimeToLive(expectedAmqpProperties.getHeader().getTimeToLive());
        header.setDurable(expectedAmqpProperties.getHeader().isDurable());
        header.setDeliveryCount(expectedAmqpProperties.getHeader().getDeliveryCount());
        header.setPriority(expectedAmqpProperties.getHeader().getPriority());

        final AmqpMessageProperties amqpMessageProperties = amqpAnnotatedMessage.getProperties();
        amqpMessageProperties.setReplyTo((expectedAmqpProperties.getProperties().getReplyTo()));
        amqpMessageProperties.setContentEncoding((expectedAmqpProperties.getProperties().getContentEncoding()));
        amqpMessageProperties.setAbsoluteExpiryTime((expectedAmqpProperties.getProperties().getAbsoluteExpiryTime()));
        amqpMessageProperties.setSubject((expectedAmqpProperties.getProperties().getSubject()));
        amqpMessageProperties.setContentType(expectedAmqpProperties.getProperties().getContentType());
        amqpMessageProperties.setCorrelationId(expectedAmqpProperties.getProperties().getCorrelationId());
        amqpMessageProperties.setTo(expectedAmqpProperties.getProperties().getTo());
        amqpMessageProperties.setGroupSequence(expectedAmqpProperties.getProperties().getGroupSequence());
        amqpMessageProperties.setUserId(expectedAmqpProperties.getProperties().getUserId());
        amqpMessageProperties.setAbsoluteExpiryTime(expectedAmqpProperties.getProperties().getAbsoluteExpiryTime());
        amqpMessageProperties.setCreationTime(expectedAmqpProperties.getProperties().getCreationTime());
        amqpMessageProperties.setReplyToGroupId(expectedAmqpProperties.getProperties().getReplyToGroupId());

        // Send the message
        sendMessage(message).block(TIMEOUT);

        StepVerifier.create(receiver.receiveMessages().map(ServiceBusReceivedMessageContext::getMessage))
            .assertNext(received -> {
                assertNotNull(received.getLockToken());
                AmqpAnnotatedMessage actual = received.getAmqpAnnotatedMessage();
                try {
                    assertArrayEquals(CONTENTS_BYTES, message.getBody());
                    assertEquals(expectedAmqpProperties.getHeader().getPriority(), actual.getHeader().getPriority());
                    assertEquals(expectedAmqpProperties.getHeader().isFirstAcquirer(), actual.getHeader().isFirstAcquirer());
                    assertEquals(expectedAmqpProperties.getHeader().isDurable(), actual.getHeader().isDurable());

                    assertEquals(expectedAmqpProperties.getProperties().getSubject(), actual.getProperties().getSubject());
                    assertEquals(expectedAmqpProperties.getProperties().getReplyToGroupId(), actual.getProperties().getReplyToGroupId());
                    assertEquals(expectedAmqpProperties.getProperties().getReplyTo(), actual.getProperties().getReplyTo());
                    assertEquals(expectedAmqpProperties.getProperties().getContentType(), actual.getProperties().getContentType());
                    assertEquals(expectedAmqpProperties.getProperties().getCorrelationId(), actual.getProperties().getCorrelationId());
                    assertEquals(expectedAmqpProperties.getProperties().getTo(), actual.getProperties().getTo());
                    assertEquals(expectedAmqpProperties.getProperties().getAbsoluteExpiryTime().toEpochSecond(), actual.getProperties().getAbsoluteExpiryTime().toEpochSecond());
                    assertEquals(expectedAmqpProperties.getProperties().getSubject(), actual.getProperties().getSubject());
                    assertEquals(expectedAmqpProperties.getProperties().getContentEncoding(), actual.getProperties().getContentEncoding());
                    assertEquals(expectedAmqpProperties.getProperties().getGroupSequence(), actual.getProperties().getGroupSequence());
                    assertEquals(expectedAmqpProperties.getProperties().getCreationTime().toEpochSecond(), actual.getProperties().getCreationTime().toEpochSecond());
                    assertArrayEquals(expectedAmqpProperties.getProperties().getUserId(), actual.getProperties().getUserId());

                    assertMapValues(expectedAmqpProperties.getDeliveryAnnotations(), actual.getDeliveryAnnotations());
                    assertMapValues(expectedAmqpProperties.getMessageAnnotations(), actual.getMessageAnnotations());
                    assertMapValues(expectedAmqpProperties.getApplicationProperties(), actual.getApplicationProperties());
                    assertMapValues(expectedAmqpProperties.getFooter(), actual.getFooter());
                } finally {
                    logger.info("Completing message.");
                    receiver.complete(received).block(Duration.ofSeconds(15));
                    messagesPending.decrementAndGet();
                }
            })
            .thenCancel()
            .verify(Duration.ofMinutes(2));
    }

    /**
     * Asserts the length and values with in the map.
     */
    private void assertMapValues(Map<String, Object> expectedMap, Map<String, Object> actualMap) {
        assertTrue(actualMap.size() >= expectedMap.size());
        Iterator<String> expectedKeys = expectedMap.keySet().iterator();
        while (expectedKeys.hasNext()) {
            String key = expectedKeys.next();
            assertEquals(expectedMap.get(key), actualMap.get(key), "Value is not equal for Key " + key);
        }
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, entityIndex, isSessionEnabled, false);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created with shared
     * connection as needed.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled,
        boolean shareConnection) {
        this.sender = getSenderBuilder(false, entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient();

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.receiver = getSessionReceiverBuilder(false, entityType, entityIndex, Function.identity(),
                shareConnection)
                .sessionId(sessionId)
                .buildAsyncClient();
            this.receiveAndDeleteReceiver = getSessionReceiverBuilder(false, entityType, entityIndex,
                Function.identity(), shareConnection)

                .sessionId(sessionId)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        } else {
            this.receiver = getReceiverBuilder(false, entityType, entityIndex, Function.identity(),
                shareConnection)
                .buildAsyncClient();
            this.receiveAndDeleteReceiver = getReceiverBuilder(false, entityType, entityIndex,
                Function.identity(), shareConnection)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        }
    }

    private Mono<Void> sendMessage(ServiceBusMessage message) {
        return sender.sendMessage(message).doOnSuccess(aVoid -> {
            int number = messagesPending.incrementAndGet();
            logger.info("Message Id {}. Number sent: {}", message.getMessageId(), number);
        });
    }

    private Mono<Void> sendMessage(List<ServiceBusMessage> messages) {
        return sender.sendMessages(messages).doOnSuccess(aVoid -> {
            int number = messagesPending.addAndGet(messages.size());
            logger.info("Number of messages sent: {}", number);
        });
    }

    private int completeMessages(ServiceBusReceiverAsyncClient client, List<ServiceBusReceivedMessage> lockTokens) {
        Mono.when(lockTokens.stream().map(e -> client.complete(e))
            .collect(Collectors.toList()))
            .block(TIMEOUT);

        return lockTokens.size();
    }

    private void completeDeferredMessages(ServiceBusReceiverAsyncClient client, ServiceBusReceivedMessage receivedMessage) {
        final ServiceBusReceivedMessage receivedDeferredMessage = client
            .receiveDeferredMessage(receivedMessage.getSequenceNumber())
            .block(TIMEOUT);

        assertNotNull(receivedDeferredMessage);

        receiver.complete(receivedDeferredMessage).block(TIMEOUT);
    }

    private ServiceBusClientBuilder.ServiceBusReceiverClientBuilder getDeadLetterReceiverBuilder(boolean useCredentials,
        MessagingEntityType entityType, int entityIndex, Function<ServiceBusClientBuilder, ServiceBusClientBuilder> onBuilderCreate) {

        ServiceBusClientBuilder builder = getBuilder(useCredentials);
        builder = onBuilderCreate.apply(builder);

        switch (entityType) {
            case QUEUE:
                final String queueName = getQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");

                return builder.receiver().queueName(queueName).subQueue(SubQueue.DEAD_LETTER_QUEUE);
            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = getSubscriptionBaseName();
                assertNotNull(topicName, "'topicName' cannot be null.");
                assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                return builder.receiver().topicName(topicName).subscriptionName(subscriptionName).subQueue(SubQueue.DEAD_LETTER_QUEUE);
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }
}
