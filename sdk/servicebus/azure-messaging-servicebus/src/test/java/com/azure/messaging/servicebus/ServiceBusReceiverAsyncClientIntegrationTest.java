// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
       /* try {
            if (isSessionEnabled) {
                logger.info("Sessioned receiver. It is probably locked until some time.");
            } else {
                receiveAndDeleteReceiver.receive()
                    .take(pending)
                    .map(message -> {
                        logger.info("Message received: {}", message.getMessage().getSequenceNumber());
                        return message;
                    })
                    .timeout(Duration.ofSeconds(5), Mono.empty())
                    .blockLast();
            }
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        } finally {
            dispose(receiver, sender, receiveAndDeleteReceiver);
        }
        */
    }

    /**
     * Verifies that we can create transaction.
     */
    @Test
    void createTansactionTest() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        // Assert & Act
        StepVerifier.create(receiver.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }
    /**
     * Verifies that we can create multiple transaction using sender and receiver.
     */
    @Test
    void createMultipleTansactionTest() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        // Assert & Act
        StepVerifier.create(receiver.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(receiver.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();

        StepVerifier.create(sender.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .verifyComplete();
    }

    /**
     * Verifies that we can create transaction and scheduleMessage.
     */
    @MethodSource("messagingEntityWithSessionsWithTxn")
    @ParameterizedTest
    void createTansactionAndScheduleMessagesTest(MessagingEntityType entityType, boolean isSessionEnabled,
        boolean commitTxn) {

        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();

            StepVerifier.create(sender.scheduleMessage(message, Instant.now().plusSeconds(5), transaction.get()))
            .assertNext(sequenceNumber -> {
                assertNotNull(sequenceNumber);
                assertTrue(sequenceNumber.intValue() > 0);
            })
            .verifyComplete();


        if (commitTxn) {
            StepVerifier.create(sender.commitTransaction(transaction.get()))
                .verifyComplete();
            StepVerifier.create(Mono.delay(Duration.ofSeconds(3)).then(receiveAndDeleteReceiver.receive().next()))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .verifyComplete();

        } else {
            StepVerifier.create(sender.rollbackTransaction(transaction.get()))
                .verifyComplete();

            StepVerifier.create(receiveAndDeleteReceiver.receive())
                .verifyTimeout(Duration.ofSeconds(10));
        }
    }

    /**
     * Verifies that we can create transaction and complete.
     */
    @Test
    void createTansactionAndCommitMessagesTest() throws InterruptedException {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(receiver.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();

        receiver.receive()
            .map(messageContext -> receiver.complete(messageContext.getMessage(), transaction.get()))
            .subscribe();

        StepVerifier.create(receiver.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(15)))
            .verifyComplete();
    }

    /**
     * Verifies that we can create transaction and complete.
     */
    @Test
    void createTansactionAndRollbackMessagesTest() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(receiver.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.complete(receivedMessage, transaction.get()))
            .verifyComplete();

        StepVerifier.create(receiver.rollbackTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
            .verifyComplete();

        // read the message back, since it was rolled-back previously.
        final ServiceBusReceivedMessageContext received = receiveAndDeleteReceiver.receive().next().block(TIMEOUT);
        assertMessageEquals(received, messageId, isSessionEnabled);

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can create transaction and commit/rollback.
     */
    @Test
    void createAndCompleteTansactionTest() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(receiver.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();

        StepVerifier.create(receiver.commitTransaction(transaction.get()))
            .verifyComplete();
    }

    /**
     * This specifically test that we can use lockToken. This use case is valid when a message is moved from one
     * machine to another machine and user just have access to lock token.
     * Verifies that we can complete a message with lock token only with a transaction and rollback.
     */
    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void transactionCommitMessagesAndRollbackWithLockTokenTest(MessagingEntityType entityType) {

        // Arrange
        boolean isSessionEnabled = false;
        setSenderAndReceiver(entityType, isSessionEnabled);
        ServiceBusReceiverAsyncClient receiverNonConnectionSharing = getReceiverBuilder(false, entityType,
            Function.identity()).buildAsyncClient();

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
        StepVerifier.create( receiver.receive().next()
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

        // rollback the transaction.
        StepVerifier.create(receiverNonConnectionSharing.rollbackTransaction(transaction.get()))
            .verifyComplete();

        // read the message back, since it was rolled-back previously.
        final ServiceBusReceivedMessageContext receivedContext = receiveAndDeleteReceiver.receive().next().block(TIMEOUT);
        assertMessageEquals(receivedContext, messageId, isSessionEnabled);

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can do following using shared connection.
     * 1. create transaction
     * 2. send message  with transactionContext
     * 3. receive and settle with transactionContext.
     * 4. commit Rollback this transaction.
     */
    @MethodSource("messagingEntityTxnWithSessions")
    @ParameterizedTest
    void transactionSendReceiveAndSettle(MessagingEntityType entityType, boolean isSessionEnabled,
         boolean commitTxn, DispositionStatus dispositionStatus) {

        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled, true);

        final String messageId1 = UUID.randomUUID().toString();
        final ServiceBusMessage message1 = getMessage(messageId1, isSessionEnabled);
        final String messageId2 = UUID.randomUUID().toString();
        final ServiceBusMessage message2 = getMessage(messageId2, isSessionEnabled);
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
        StepVerifier.create(sender.send(message2, transaction.get()))
            .verifyComplete();
        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);
        final Mono<Void> operation;
        if (DispositionStatus.ABANDONED == dispositionStatus && isSessionEnabled) {
            operation = receiver.abandon(receivedMessage, null, sessionId, transaction.get());
        } else if (DispositionStatus.ABANDONED == dispositionStatus && !isSessionEnabled) {
            operation = receiver.abandon(receivedMessage, null, transaction.get());
        } else if (DispositionStatus.SUSPENDED == dispositionStatus && isSessionEnabled) {
            DeadLetterOptions deadLetterOptions = new DeadLetterOptions().setDeadLetterReason("For testing.");
            operation = receiver.deadLetter(receivedMessage, deadLetterOptions, sessionId, transaction.get());
        } else if (DispositionStatus.SUSPENDED == dispositionStatus && !isSessionEnabled) {
            DeadLetterOptions deadLetterOptions = new DeadLetterOptions().setDeadLetterReason("For testing.");
            operation = receiver.deadLetter(receivedMessage, deadLetterOptions, transaction.get());
        } else if (DispositionStatus.COMPLETED == dispositionStatus && isSessionEnabled) {
            operation = receiver.complete(receivedMessage, sessionId, transaction.get());
        } else if (DispositionStatus.COMPLETED == dispositionStatus && !isSessionEnabled) {
            operation = receiver.complete(receivedMessage, transaction.get());
        } else if (DispositionStatus.DEFERRED == dispositionStatus && isSessionEnabled) {
            operation = receiver.defer(receivedMessage, null, sessionId, transaction.get());
        } else if (DispositionStatus.DEFERRED == dispositionStatus && !isSessionEnabled) {
            operation = receiver.defer(receivedMessage, null, transaction.get());
        }else {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Disposition status not recognized for this test case: " + dispositionStatus));
        }

        StepVerifier.create(operation)
            .verifyComplete();

        if (commitTxn) {
            StepVerifier.create(receiver.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
        } else {
            StepVerifier.create(receiver.rollbackTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
        }
    }

    /**
     * Verifies that we can do following on different clients i.e. sender and receiver.
     * 1. create transaction using sender
     * 2. receive and complete with transactionContext.
     * 3. Rollback this transaction using sender.
     */
    @Test
    void transactionReceiveCompleteRollbackMixClient() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false, true);

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

        StepVerifier.create(sender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
            .verifyComplete();

    }

    /**
     * Verifies that we can do following
     * 1. create transaction
     * 2. send message  with transactionContext
     * 3. commit this transaction.
     */
    @Test
    void transactionMessageSendAndCommit() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

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
        StepVerifier.create(sender.send(message, transaction.get()))
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
            .verifyComplete();

        messagesPending.incrementAndGet();
    }

    /**
     * Verifies that we can do following
     * 1. create transaction
     * 2. send message  with transactionContext
     * 3. Rollback this transaction.
     */
    @Test
    void transactionMessageSendAndRollback() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

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
        StepVerifier.create(sender.send(message, transaction.get()))
            .verifyComplete();

        StepVerifier.create(sender.rollbackTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
            .verifyComplete();

    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        Mono.when(sendMessage(message), sendMessage(message)).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.receive())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .thenCancel()
            .verify();

        messagesPending.decrementAndGet();
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.receive())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .thenCancel()
            .verify();

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.browse())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive a message.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void sendScheduledMessageAndReceive(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void cancelScheduledMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void peekFromSequenceNumberMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessageContext receivedContext = receiver.receive().next().block(TIMEOUT);
        assertNotNull(receivedContext);

        final ServiceBusReceivedMessage receivedMessage = receivedContext.getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.browseAt(receivedMessage.getSequenceNumber()))
            .assertNext(m -> {
                assertEquals(receivedMessage.getSequenceNumber(), m.getSequenceNumber());
                assertMessageEquals(m, messageId, isSessionEnabled);
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can send and peek a batch of messages and the sequence number is tracked correctly.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void peekBatchMessages(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final BiConsumer<ServiceBusReceivedMessage, Integer> checkCorrectMessage = (message, index) -> {
            final Map<String, Object> properties = message.getProperties();
            final Object value = properties.get(MESSAGE_POSITION_ID);
            assertTrue(value instanceof Integer, "Did not contain correct position number: " + value);

            final int position = (int) value;
            assertEquals(index, position);
        };
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(10, messageId);
        if (isSessionEnabled) {
            messages.forEach(m -> m.setSessionId(sessionId));
        }

        sendMessage(messages).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.browseBatch(3))
            .assertNext(message -> checkCorrectMessage.accept(message, 0))
            .assertNext(message -> checkCorrectMessage.accept(message, 1))
            .assertNext(message -> checkCorrectMessage.accept(message, 2))
            .verifyComplete();

        StepVerifier.create(receiver.browseBatch(4))
            .assertNext(message -> checkCorrectMessage.accept(message, 3))
            .assertNext(message -> checkCorrectMessage.accept(message, 4))
            .assertNext(message -> checkCorrectMessage.accept(message, 5))
            .assertNext(message -> checkCorrectMessage.accept(message, 6))
            .verifyComplete();

        StepVerifier.create(receiver.browse())
            .assertNext(message -> checkCorrectMessage.accept(message, 7))
            .verifyComplete();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void peekBatchMessagesFromSequence(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 2;
        final int fromSequenceNumber = 1;

        Mono.when(sendMessage(message), sendMessage(message)).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiver.browseBatchAt(maxMessages, fromSequenceNumber))
            .expectNextCount(maxMessages)
            .verifyComplete();
    }

    /**
     * Verifies that we can dead-letter a message.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void deadLetterMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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
    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, false);

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
            StepVerifier.create(Mono.delay(Duration.ofSeconds(10))
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
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void autoRenewLockOnReceiveMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
//        setSenderAndReceiver(entityType, isSessionEnabled,
//            builder -> builder.maxAutoLockRenewalDuration(Duration.ofSeconds(120)));

        setSenderAndReceiver(entityType, isSessionEnabled);

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
                    logger.info("Iteration {}: {}", iteration.incrementAndGet(), Instant.now());

                    try {
                        TimeUnit.SECONDS.sleep(15);
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

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndDefer(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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
    @MethodSource
    @ParameterizedTest
    void receiveDeferredMessageBySequenceNumber(MessagingEntityType entityType, DispositionStatus dispositionStatus) {
        // Arrange
        setSenderAndReceiver(entityType, false);

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

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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

    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void setAndGetSessionState(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, true);

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

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, isSessionEnabled, null, false);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created with
     * shared connection as needed.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled, boolean shareConnection) {
        setSenderAndReceiver(entityType, isSessionEnabled, null, shareConnection);
    }

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled,
        Duration autoLockRenewal, boolean shareConnection) {
        this.isSessionEnabled = isSessionEnabled;
        this.sender = getSenderBuilder(false, entityType, isSessionEnabled, shareConnection).buildAsyncClient();

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.receiver = getSessionReceiverBuilder(false, entityType, Function.identity(), shareConnection)
                .sessionId(sessionId)
                .maxAutoLockRenewalDuration(autoLockRenewal)
                .buildAsyncClient();
            this.receiveAndDeleteReceiver = getSessionReceiverBuilder(false, entityType, Function.identity(), shareConnection)
                .sessionId(sessionId)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        } else {
            this.receiver = getReceiverBuilder(false, entityType, Function.identity(), shareConnection)
                .maxAutoLockRenewalDuration(autoLockRenewal)
                .buildAsyncClient();
            this.receiveAndDeleteReceiver = getReceiverBuilder(false, entityType, Function.identity(), shareConnection)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        }
    }

    private Mono<Void> sendMessage(ServiceBusMessage message) {
        return sender.send(message).doOnSuccess(aVoid -> {
            int number = messagesPending.incrementAndGet();
            logger.info("Number sent: {}", number);
        });
    }

    private Mono<Void> sendMessage(List<ServiceBusMessage> messages) {
        return sender.send(messages).doOnSuccess(aVoid -> {
            int number = messagesPending.addAndGet(messages.size());
            logger.info("Number of messages sent: {}", number);
        });
    }
}
