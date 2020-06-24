// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
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

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;
    private boolean isSessionEnabled;

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
        sharedBuilder =  null;
        final int pending = messagesPending.get();
        if (pending < 1) {
            dispose(receiver, sender, receiveAndDeleteReceiver);
            return;
        }

        // In the case that this test failed... we're going to drain the queue or subscription.
        try {
            receiveAndDeleteReceiver.receive()
                .map(message -> {
                    logger.info("Message received: {}", message.getMessage().getSequenceNumber());
                    return message;
                })
                .timeout(Duration.ofSeconds(15), Mono.empty())
                .blockLast();
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
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
    @MethodSource("messagingEntityProvider")
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

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(OPERATION_TIMEOUT);
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
     * This specifically test that we can use lockToken. This use case is valid when a message is moved from one
     * machine to another machine and user just have access to lock token.
     * Verifies that we can complete a message with lock token only with a transaction and rollback.
     */
    @Test
    void transactionWithLockTokenTest() {

        // Arrange
        MessagingEntityType entityType = MessagingEntityType.QUEUE;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        ServiceBusReceiverAsyncClient receiverNonConnectionSharing = getReceiverBuilder(false, entityType, 0,
            Function.identity(), false).buildAsyncClient();

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // create a transaction.
        StepVerifier.create(receiverNonConnectionSharing.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();

        AtomicReference<MessageLockToken> messageLockToken = new AtomicReference<>();

        // receive a message and get lock token.
        StepVerifier.create(receiver.receive().next()
            .map(messageContext -> {
                ServiceBusReceivedMessage received =  messageContext.getMessage();
                messageLockToken.set(MessageLockToken.fromString(received.getLockToken()));
                return messageContext;
            }))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .verifyComplete();

        // complete the message using lock token only using a receiver which represent a different machine
        StepVerifier.create(receiverNonConnectionSharing.complete(messageLockToken.get(), transaction.get()))
            .verifyComplete();

        // commit the transaction.
        StepVerifier.create(receiverNonConnectionSharing.commitTransaction(transaction.get()))
            .verifyComplete();

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can do following using shared connection and on non session entity.
     * 1. create transaction
     * 2. receive and settle with transactionContext.
     * 3. commit Rollback this transaction.
     */

    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void transactionSendReceiveAndCommit(DispositionStatus dispositionStatus) {

        // Arrange
        final MessagingEntityType entityType = MessagingEntityType.QUEUE;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

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
        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
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
    }

    /**
     * Verifies that we can do following on different clients i.e. sender and receiver.
     * 1. create transaction using sender
     * 2. receive and complete with transactionContext.
     * 3. Commit this transaction using sender.
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
        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
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
        final List<String> lockTokens = new ArrayList<>();

        Mono.when(sendMessage(message), sendMessage(message)).block(TIMEOUT);

        // Assert & Act
        try {
            StepVerifier.create(receiver.receive())
                .assertNext(receivedMessage -> {
                    lockTokens.add(receivedMessage.getMessage().getLockToken());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .assertNext(receivedMessage -> {
                    lockTokens.add(receivedMessage.getMessage().getLockToken());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .thenCancel()
                .verify();
        } finally {
            int numberCompleted = completeMessages(receiver, lockTokens);
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
        final List<String> lockTokens = new ArrayList<>();

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        try {
            StepVerifier.create(receiver.receive())
                .assertNext(receivedMessage -> {
                    lockTokens.add(receivedMessage.getMessage().getLockToken());
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                })
                .thenCancel()
                .verify();
        } finally {
            int numberCompleted = completeMessages(receiver, lockTokens);
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
        StepVerifier.create(receiver.peek())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
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
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(2);

        sender.scheduleMessage(message, scheduledEnqueueTime).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(Mono.delay(Duration.ofSeconds(3)).then(receiveAndDeleteReceiver.receive().next()))
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
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(10);
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
        StepVerifier.create(receiver.receive().take(1))
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

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        try {
            StepVerifier.create(receiver.peekAt(receivedMessage.getSequenceNumber()))
                .assertNext(m -> {
                    assertEquals(receivedMessage.getSequenceNumber(), m.getSequenceNumber());
                    assertMessageEquals(m, messageId, isSessionEnabled);
                })
                .verifyComplete();
        } finally {
            receiver.complete(receivedMessage)
                .block(Duration.ofSeconds(10));
        }
    }

    /**
     * Verifies that we can send and peek a batch of messages and the sequence number is tracked correctly.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekBatchMessages(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_PEEK_BATCH_MESSAGES, isSessionEnabled);

        final BiConsumer<ServiceBusReceivedMessage, Integer> checkCorrectMessage = (message, index) -> {
            final Map<String, Object> properties = message.getProperties();
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
            StepVerifier.create(receiver.peekBatch(3))
                .assertNext(message -> checkCorrectMessage.accept(message, 0))
                .assertNext(message -> checkCorrectMessage.accept(message, 1))
                .assertNext(message -> checkCorrectMessage.accept(message, 2))
                .verifyComplete();

            StepVerifier.create(receiver.peekBatch(4))
                .assertNext(message -> checkCorrectMessage.accept(message, 3))
                .assertNext(message -> checkCorrectMessage.accept(message, 4))
                .assertNext(message -> checkCorrectMessage.accept(message, 5))
                .assertNext(message -> checkCorrectMessage.accept(message, 6))
                .verifyComplete();

            StepVerifier.create(receiver.peek())
                .assertNext(message -> checkCorrectMessage.accept(message, 7))
                .verifyComplete();
        } finally {
            receiveAndDeleteReceiver.receive()
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
    void peekBatchMessagesFromSequence(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 5, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 2;
        final int fromSequenceNumber = 1;

        Mono.when(sendMessage(message), sendMessage(message)).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.peekBatchAt(maxMessages, fromSequenceNumber))
            .expectNextCount(maxMessages)
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

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
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

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
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

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getLockedUntil());

        final Instant initialLock = receivedMessage.getLockedUntil();
        logger.info("Received message. Seq: {}. lockedUntil: {}", receivedMessage.getSequenceNumber(), initialLock);

        // Assert & Act
        try {
            StepVerifier.create(Mono.delay(Duration.ofSeconds(7))
                .then(Mono.defer(() -> receiver.renewMessageLock(receivedMessage))))
                .assertNext(lockedUntil -> {
                    assertTrue(lockedUntil.isAfter(initialLock),
                        String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                            lockedUntil, initialLock));

                    assertEquals(receivedMessage.getLockedUntil(), lockedUntil);
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
        StepVerifier.create(receiver.receive().map(ServiceBusReceivedMessageContext::getMessage))
            .assertNext(received -> {
                assertNotNull(received.getLockedUntil());
                assertNotNull(received.getLockToken());

                logger.info("{}: lockToken[{}]. lockedUntil[{}]. now[{}]", received.getSequenceNumber(),
                    received.getLockToken(), received.getLockedUntil(), Instant.now());

                final Instant initial = received.getLockedUntil();
                final Instant timeToStop = initial.plusSeconds(5);
                Instant latest = Instant.MIN;

                // Simulate some sort of long processing.
                final AtomicInteger iteration = new AtomicInteger();
                while (Instant.now().isBefore(timeToStop)) {
                    logger.info("Iteration {}: {}. Time to stop: {}", iteration.incrementAndGet(), Instant.now(), timeToStop);

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

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.abandon(receivedMessage))
            .verifyComplete();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndDefer(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();

        assertNotNull(receivedMessage);

        // Act & Assert
        StepVerifier.create(receiver.defer(receivedMessage))
            .verifyComplete();
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
        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
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

        if (dispositionStatus == DispositionStatus.ABANDONED || dispositionStatus == DispositionStatus.COMPLETED) {
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

        Map<String, Object> sentProperties = messageToSend.getProperties();
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
        StepVerifier.create(receiveAndDeleteReceiver.receive())
            .assertNext(receivedMessage -> {
                messagesPending.decrementAndGet();
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);

                final Map<String, Object> received = receivedMessage.getMessage().getProperties();

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
        StepVerifier.create(receiver.receive()
            .take(1)
            .flatMap(m -> {
                logger.info("SessionId: {}. LockToken: {}. LockedUntil: {}. Message received.",
                    m.getSessionId(), m.getMessage().getLockToken(), m.getMessage().getLockedUntil());
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
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receivesByNumber(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_RECEIVE_BY_NUMBER, false);

        final String messageId = UUID.randomUUID().toString();
        final int number = 10;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(number, messageId, CONTENTS_BYTES);

        sendMessage(messages).block(Duration.ofSeconds(10));

        // Act & Assert
        StepVerifier.create(receiveAndDeleteReceiver.receive(messages.size(), Duration.ofSeconds(15))
            .doOnNext(next -> messagesPending.decrementAndGet()))
            .expectNextCount(number)
            .verifyComplete();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receivesByTime(MessagingEntityType entityType) {
        // Arrange

        setSenderAndReceiver(entityType, TestUtils.USE_CASE_RECEIVE_BY_TIME, false);
        final String messageId = UUID.randomUUID().toString();
        final int number = 10;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(number, messageId, CONTENTS_BYTES);

        sendMessage(messages).block(Duration.ofSeconds(15));

        // Act & Assert
        StepVerifier.create(receiveAndDeleteReceiver.receive(number + 10, Duration.ofSeconds(15))
            .doOnNext(next -> messagesPending.decrementAndGet()))
            .expectNextCount(number)
            .verifyComplete();
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, entityIndex, isSessionEnabled, false);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created with
     * shared connection as needed.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled, boolean shareConnection) {
        this.sender = getSenderBuilder(false, entityType, entityIndex, isSessionEnabled, shareConnection).buildAsyncClient();


        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.receiver = getSessionReceiverBuilder(false, entityType, entityIndex, Function.identity(), shareConnection)
                .sessionId(sessionId)
                .buildAsyncClient();
            this.receiveAndDeleteReceiver = getSessionReceiverBuilder(false, entityType, entityIndex,
                Function.identity(), shareConnection)

                .sessionId(sessionId)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        } else {
            this.receiver = getReceiverBuilder(false, entityType, entityIndex, Function.identity(), shareConnection)
                .buildAsyncClient();
            this.receiveAndDeleteReceiver = getReceiverBuilder(false, entityType, entityIndex, Function.identity(), shareConnection)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        }
    }

    private Mono<Void> sendMessage(ServiceBusMessage message) {
        return sender.send(message).doOnSuccess(aVoid -> {
            int number = messagesPending.incrementAndGet();
            logger.info("Message Id {}. Number sent: {}", message.getMessageId(), number);
        });
    }

    private Mono<Void> sendMessage(List<ServiceBusMessage> messages) {
        return sender.send(messages).doOnSuccess(aVoid -> {
            int number = messagesPending.addAndGet(messages.size());
            logger.info("Number of messages sent: {}", number);
        });
    }

    private int completeMessages(ServiceBusReceiverAsyncClient client, List<String> lockTokens) {
        Mono.when(lockTokens.stream().map(e -> client.complete(MessageLockToken.fromString(e)))
            .collect(Collectors.toList()))
            .block(TIMEOUT);

        return lockTokens.size();
    }
}
