// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.models.AmqpAddress;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.amqp.models.AmqpMessageBodyType;
import com.azure.core.amqp.models.AmqpMessageHeader;
import com.azure.core.amqp.models.AmqpMessageId;
import com.azure.core.amqp.models.AmqpMessageProperties;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import com.azure.messaging.servicebus.models.SubQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.servicebus.TestUtils.USE_CASE_AUTO_COMPLETE;
import static com.azure.messaging.servicebus.TestUtils.USE_CASE_PEEK_BATCH_MESSAGES;
import static com.azure.messaging.servicebus.TestUtils.USE_CASE_PEEK_MESSAGE;
import static com.azure.messaging.servicebus.TestUtils.USE_CASE_RECEIVE_AND_COMPLETE;
import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessages;
import static com.azure.messaging.servicebus.TestUtils.getSessionSubscriptionBaseName;
import static com.azure.messaging.servicebus.TestUtils.getSubscriptionBaseName;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link ServiceBusReceiverAsyncClient} from queues or subscriptions.
 */
@Tag("integration")
@Execution(ExecutionMode.SAME_THREAD)
public class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);
    private static final AmqpRetryOptions DEFAULT_RETRY_OPTIONS = null;
    private final boolean isSessionEnabled = false;
    private final ClientCreationOptions defaultClientCreationOptions = new ClientCreationOptions()
        .setMaxAutoLockRenewDuration(Duration.ofMinutes(5));

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusSessionReceiverAsyncClient sessionReceiver;

    public ServiceBusReceiverAsyncClientIntegrationTest() {
        super(LOGGER);
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        sharedBuilder = null;
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
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(receiver.createTransaction())
            .assertNext(Assertions::assertNotNull)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can create transaction and complete.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void createTransactionAndRollbackMessagesTest(MessagingEntityType entityType) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block();

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(receiver.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(receiver.receiveMessages()
                .flatMap(receivedMessage -> {
                    logMessage(receivedMessage, receiver.getEntityPath(), "received message");
                    return receiver.complete(receivedMessage)
                        .doOnSuccess(m -> logMessage(receivedMessage, receiver.getEntityPath(), "completed message"))
                        .thenReturn(receivedMessage);
                }).take(1))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(receiver.rollbackTransaction(transaction.get()))
            .expectComplete()
            .verify(TIMEOUT);
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
            .expectComplete()
            .verify(TIMEOUT);
        assertNotNull(transaction.get());

        // Assert & Act
        final ServiceBusReceivedMessage message = receiver.receiveMessages()
            .flatMap(receivedMessage -> {
                logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
                final Mono<Void> operation;
                switch (dispositionStatus) {
                    case COMPLETED:
                        operation = receiver.complete(receivedMessage, new CompleteOptions().setTransactionContext(transaction.get()));
                        logMessage(receivedMessage, receiver.getEntityPath(), "completed messages");
                        break;
                    case ABANDONED:
                        operation = receiver.abandon(receivedMessage, new AbandonOptions().setTransactionContext(transaction.get()));
                        logMessage(receivedMessage, receiver.getEntityPath(), "abandoned messages");
                        break;
                    case SUSPENDED:
                        DeadLetterOptions deadLetterOptions = new DeadLetterOptions().setTransactionContext(transaction.get())
                            .setDeadLetterReason(deadLetterReason);
                        operation = receiver.deadLetter(receivedMessage, deadLetterOptions);
                        logMessage(receivedMessage, receiver.getEntityPath(), "deadLettered messages");
                        break;
                    case DEFERRED:
                        operation = receiver.defer(receivedMessage, new DeferOptions().setTransactionContext(transaction.get()));
                        logMessage(receivedMessage, receiver.getEntityPath(), "deferred messages");
                        break;
                    case RELEASED:
                        operation = receiver.release(receivedMessage);
                        logMessage(receivedMessage, receiver.getEntityPath(), "released messages");
                        break;
                    default:
                        throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                            "Disposition status not recognized for this test case: " + dispositionStatus));
                }
                return operation
                    .thenReturn(receivedMessage);
            })
            .blockFirst(TIMEOUT);
        assertNotNull(message);

        StepVerifier.create(receiver.commitTransaction(transaction.get()))
            .expectComplete()
            .verify(TIMEOUT);
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
        final boolean shareConnection = true;
        final int entityIndex = 0;
        this.sender = toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient());
        this.receiver = toClose(getReceiverBuilder(entityType, entityIndex, shareConnection)
            .buildAsyncClient());

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
            .expectComplete()
            .verify(TIMEOUT);
        assertNotNull(transaction.get());

        // Assert & Act
        final ServiceBusReceivedMessage receivedMessage = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "received message");
        StepVerifier.create(receiver.complete(receivedMessage, new CompleteOptions().setTransactionContext(transaction.get())))
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int entityIndex = 0;
        final boolean shareConnection = false;
        final Duration shortWait = Duration.ofSeconds(3);

        this.sender = toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient());

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        Mono.when(sendMessage(message), sendMessage(message)).block();

        // Now create receiver
        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.sessionReceiver = toClose(getSessionReceiverBuilder(entityType, entityIndex, shareConnection, DEFAULT_RETRY_OPTIONS)
                .buildAsyncClient());
            this.receiver = toClose(sessionReceiver.acceptSession(sessionId).block());
        } else {
            this.receiver = toClose(getReceiverBuilder(entityType, entityIndex, shareConnection)
                .buildAsyncClient());
        }

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages()
                .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "received message"))
                .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId())))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .thenAwait(shortWait) // Give time for autoComplete to finish
            .thenCancel()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int entityIndex = USE_CASE_AUTO_COMPLETE;
        final boolean shareConnection = false;

        this.sender = toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient());
        final String messageId = CoreUtils.randomUuid().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        StepVerifier.create(sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);

        // Now create receiver
        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.sessionReceiver = toClose(getSessionReceiverBuilder(entityType, entityIndex, shareConnection, DEFAULT_RETRY_OPTIONS)
                .buildAsyncClient());
            this.receiver = toClose(this.sessionReceiver.acceptSession(sessionId).block());
        } else {
            this.receiver = toClose(getReceiverBuilder(entityType, entityIndex, shareConnection)
                .buildAsyncClient());
        }

        // Assert
        StepVerifier.create(receiver.receiveMessages()
                .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId())))
            .assertNext(receivedMessage -> {
                logMessage(receivedMessage, receiver.getEntityPath(), "received message");
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            })
            .expectNoEvent(Duration.ofSeconds(30))
            .thenCancel()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, USE_CASE_PEEK_MESSAGE, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block();

        setReceiver(entityType, USE_CASE_PEEK_MESSAGE, isSessionEnabled);

        Mono<ServiceBusReceivedMessage> peek = receiver.peekMessage()
            .filter(m -> messageId.equals(m.getMessageId()))
            .repeatWhenEmpty(10, i -> Flux.interval(Duration.ofSeconds(1)));

        // Assert & Act
        StepVerifier.create(peek
                .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "peeked and filtered message")))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that an empty entity does not error when peeking.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessageEmptyEntity(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setReceiver(entityType, TestUtils.USE_CASE_EMPTY_ENTITY, isSessionEnabled);

        final int fromSequenceNumber = 1;

        // Assert & Act
        StepVerifier.create(receiver.peekMessage(fromSequenceNumber)
                .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "peeked message")))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can schedule and receive a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void sendScheduledMessageAndReceive(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_SEND_SCHEDULED, isSessionEnabled);
        final Duration shortDelay = Duration.ofSeconds(4);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final OffsetDateTime scheduledEnqueueTime = OffsetDateTime.now().plusSeconds(2);

        sender.scheduleMessage(message, scheduledEnqueueTime).block();

        setReceiver(entityType, TestUtils.USE_CASE_SEND_SCHEDULED, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(Mono.delay(shortDelay).then(receiver.receiveMessages()
                .filter(m -> messageId.equals(m.getMessageId()))
                .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "received message"))
                .flatMap(receivedMessage -> receiver.complete(receivedMessage).thenReturn(receivedMessage)).next()))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can cancel a scheduled message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void cancelScheduledMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final OffsetDateTime scheduledEnqueueTime = OffsetDateTime.now().plusSeconds(10);
        final Duration delayDuration = Duration.ofSeconds(3);

        final Long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime).block(TIMEOUT);
        logMessage(message, sender.getEntityPath(), "scheduled");

        assertNotNull(sequenceNumber);

        Mono.delay(delayDuration)
            .then(sender.cancelScheduledMessage(sequenceNumber))
            .block(TIMEOUT);

        logMessage(message, sender.getEntityPath(), "cancelled messaged with sequence number " + sequenceNumber);
        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages()
                .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "received message"))
                .take(1))
            .thenAwait(Duration.ofSeconds(5))
            .thenCancel()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekFromSequenceNumberMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int entityIndex = 3;

        setSender(entityType, entityIndex, isSessionEnabled);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        sendMessage(message).block();

        setReceiver(entityType, entityIndex, isSessionEnabled);

        // Message are not always guaranteed, so try many times
        final ServiceBusReceivedMessage peekMessage = receiver.peekMessage()
            .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "received message"))
            .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId()))
            .map(receivedMessage -> {
                countDownLatch.countDown();
                return receivedMessage;
            })
            .repeat(() -> countDownLatch.getCount() > 0)
            .next()
            .block();
        assertNotNull(peekMessage);
        final long sequenceNumber = peekMessage.getSequenceNumber();

        // Assert & Act
        try {
            StepVerifier.create(receiver.peekMessage(sequenceNumber)
                    .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "peeked message")))
                .assertNext(m -> {
                    assertEquals(sequenceNumber, m.getSequenceNumber());
                    assertMessageEquals(m, messageId, isSessionEnabled);
                })
                .expectComplete()
                .verify(TIMEOUT);
        } finally {

            // Cleanup
            StepVerifier.create(receiver.receiveMessages()
                 .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "received message"))
                .flatMap(receivedMessage -> receiver.complete(receivedMessage).thenReturn(receivedMessage)).take(1))
                .expectNextCount(1)
                .expectComplete()
                .verify(TIMEOUT);
        }
    }

    /**
     * Verifies that we can send and peek a batch of messages and the sequence number is tracked correctly.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    public void peekMessages(MessagingEntityType entityType, boolean isSessionEnabled) throws InterruptedException {
        // Arrange
        setSender(entityType, USE_CASE_PEEK_BATCH_MESSAGES, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = getServiceBusMessages(10, messageId, CONTENTS_BYTES);
        final Set<Integer> receivedPositions = Collections.synchronizedSet(new HashSet<>());
        final List<ServiceBusReceivedMessage> receivedMessages = Collections.synchronizedList(new ArrayList<>());

        if (isSessionEnabled) {
            messages.forEach(m -> m.setSessionId(sessionId));
        }

        StepVerifier.create(sender.sendMessages(messages)
                .doOnSuccess(aVoid -> logMessages(messages, sender.getEntityPath(), "sent")))
            .expectComplete()
            .verify(TIMEOUT);

        setReceiver(entityType, USE_CASE_PEEK_BATCH_MESSAGES, isSessionEnabled);

        // Assert & Act
        for (int i = 0; i < 5 && receivedPositions.size() < messages.size(); i++) {
            peekMessages(messages.size(), messageId, receivedPositions)
                    .doOnNext(receivedMessage -> receivedMessages.add(receivedMessage))
                    .blockLast();
            if (receivedPositions.size() < messages.size()) {
                Thread.sleep(1000);
            }
        }

        assertEquals(receivedMessages.size(), messages.size());

        final AtomicInteger messageCount = new AtomicInteger();

        synchronized (receivedMessages) {
            receivedMessages.stream()
                .forEach(actualMessage -> {
                    final Object position = actualMessage.getApplicationProperties().get(MESSAGE_POSITION_ID);
                    assertTrue(position instanceof Integer, "Did not contain correct position number: " + position);

                    // messages are received in the same order as they were sent
                    assertEquals(messageCount.getAndIncrement(), position);
                });
        }
    }

    private Flux<ServiceBusReceivedMessage> peekMessages(int count, String messageIdFilter, Set<Integer> receivedPositions) {
        return receiver.peekMessages(count)
            // maxMessages are not always guaranteed, sometime, we get less than asked for, so we will try many times.
            .filter(receivedMessage -> {
                logMessage(receivedMessage, receiver.getEntityPath(), "peeked message");
                Integer position = (Integer) receivedMessage.getApplicationProperties().get(MESSAGE_POSITION_ID);
                boolean filtered = messageIdFilter.equals(receivedMessage.getMessageId()) && receivedPositions.add(position);
                if (filtered) {
                    logMessage(receivedMessage, receiver.getEntityPath(), "filtered message, a few more to go");
                }
                return filtered;
            });
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void peekMessagesFromSequence(MessagingEntityType entityType) throws InterruptedException {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_PEEK_MESSAGE_FROM_SEQUENCE, false);

        final AtomicInteger messageId = new AtomicInteger();
        final int maxMessages = 2;
        final AtomicLong fromSequenceNumber = new AtomicLong();
        final CountDownLatch countdownLatch = new CountDownLatch(maxMessages);
        fromSequenceNumber.set(1);

        final byte[] content = "peek-message-from-sequence".getBytes(Charset.defaultCharset());
        List<String> messageIds = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < maxMessages; ++i) {
            ServiceBusMessage message = getMessage(String.valueOf(i), isSessionEnabled, AmqpMessageBody.fromData(content));
            messageIds.add(String.valueOf(i));
            sendMessage(message).block();
        }

        // Assert & Act

        // maxMessages are not always guaranteed, sometime, we get less than asked for, just trying two times is not enough, so we will try many times
        // https://github.com/Azure/azure-sdk-for-java/issues/21168
        List<String> receivedMessages = Collections.synchronizedList(new ArrayList<>());
        Disposable subscription = receiver.peekMessages(maxMessages, fromSequenceNumber.get())
            .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "peeked message"))
            .filter(receivedMessage -> messageIds.contains(receivedMessage.getMessageId())
                && receivedMessages.parallelStream().noneMatch(mid ->
                    mid.equals(receivedMessage.getMessageId())))
            .sort(Comparator.comparing(ServiceBusReceivedMessage::getMessageId))
            .flatMap(receivedMessage -> {
                Long previousSequenceNumber = fromSequenceNumber.get();
                fromSequenceNumber.set(receivedMessage.getSequenceNumber() + 1);
                countdownLatch.countDown();
                receivedMessages.add(receivedMessage.getMessageId());
                assertEquals(String.valueOf(messageId.getAndIncrement()), receivedMessage.getMessageId(),
                    String.format("Message id did not match. Message payload: [%s], peek from Sequence Number [%s], "
                            + " received message Sequence Number [%s]", receivedMessage.getBody(),
                        previousSequenceNumber, receivedMessage.getSequenceNumber()));
                return Mono.just(receivedMessage);
            })
            .repeat(() -> countdownLatch.getCount() > 0)
            .subscribe();
        toClose(subscription);

        assertTrue(countdownLatch.await(20, TimeUnit.SECONDS), "Failed peek messages from sequence.");

        StepVerifier.create(receiver.receiveMessages().take(maxMessages))
            .assertNext(receivedMessage -> receiver.complete(receivedMessage).block(Duration.ofSeconds(15)))
            .assertNext(receivedMessage -> receiver.complete(receivedMessage).block(Duration.ofSeconds(15)))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that an empty entity does not error when peeking.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessagesFromSequenceEmptyEntity(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setReceiver(entityType, TestUtils.USE_CASE_EMPTY_ENTITY, isSessionEnabled);

        final int maxMessages = 10;
        final int fromSequenceNumber = 1;

        // Assert & Act
        StepVerifier.create(receiver.peekMessages(maxMessages, fromSequenceNumber))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can dead-letter a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void deadLetterMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int entityIndex = 0;
        setSender(entityType, entityIndex, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block();

        setReceiver(entityType, entityIndex, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages()
            .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId()))
            .flatMap(receivedMessage -> receiver.deadLetter(receivedMessage).thenReturn(receivedMessage)).take(1))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .thenCancel()
            .verify(TIMEOUT);

    }

    /**
     * Verifies that we can send and receive a message AMQP Sequence andValue object.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAmqpTypes(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int entityIndex = TestUtils.USE_CASE_AMQP_TYPES;
        final boolean shareConnection = false;
        final Duration shortWait = Duration.ofSeconds(3);
        final Long expectedLongValue = Long.parseLong("6");

        this.sender = toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient());

        // Send  value Object
        String messageId = UUID.randomUUID().toString();
        ServiceBusMessage message = getMessage(messageId, isSessionEnabled, AmqpMessageBody.fromValue(expectedLongValue));
        sendMessage(message).block(TIMEOUT);

        // send SEQUENCE
        messageId = UUID.randomUUID().toString();

        List<Object> sequenceData = new ArrayList<>();
        sequenceData.add("A1");
        sequenceData.add(1L);
        sequenceData.add(2);

        message = getMessage(messageId, isSessionEnabled, AmqpMessageBody.fromSequence(sequenceData));
        sendMessage(message).block(TIMEOUT);

        // Now create receiver
        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.sessionReceiver = toClose(getSessionReceiverBuilder(entityType, entityIndex, shareConnection, DEFAULT_RETRY_OPTIONS)
                .buildAsyncClient());
            this.receiver = toClose(this.sessionReceiver.acceptSession(sessionId).block());
        } else {
            this.receiver = toClose(getReceiverBuilder(entityType, entityIndex, shareConnection)
                .buildAsyncClient());
        }

        // Assert
        StepVerifier.create(receiver.receiveMessages())
            .assertNext(receivedMessage -> {
                AmqpAnnotatedMessage amqpAnnotatedMessage = receivedMessage.getRawAmqpMessage();
                AmqpMessageBodyType type = amqpAnnotatedMessage.getBody().getBodyType();
                assertEquals(AmqpMessageBodyType.VALUE, type);
                Object value = amqpAnnotatedMessage.getBody().getValue();
                assertTrue(value instanceof Long);
                assertEquals(expectedLongValue.longValue(), ((Long) value).longValue());
            })
            .assertNext(receivedMessage -> {
                AmqpAnnotatedMessage amqpAnnotatedMessage = receivedMessage.getRawAmqpMessage();
                AmqpMessageBodyType type = amqpAnnotatedMessage.getBody().getBodyType();
                assertEquals(AmqpMessageBodyType.SEQUENCE, type);
                assertArrayEquals(sequenceData.toArray(), amqpAnnotatedMessage.getBody().getSequence().toArray());
            })
            .thenAwait(shortWait) // Give time for autoComplete to finish
            .thenCancel()
            .verify(TIMEOUT);

        if (!isSessionEnabled) {
            StepVerifier.create(receiver.receiveMessages())
                .thenAwait(shortWait)
                .thenCancel()
                .verify(TIMEOUT);
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, USE_CASE_RECEIVE_AND_COMPLETE, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        setReceiver(entityType, USE_CASE_RECEIVE_AND_COMPLETE, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages()
                .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId()))
                .doOnNext(receivedMessage -> logMessage(receivedMessage, receiver.getEntityPath(), "received and filtered"))
                .flatMap(receivedMessage -> receiver.complete(receivedMessage).thenReturn(receivedMessage)).take(1))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can renew message lock on a non-session receiver.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message).block(TIMEOUT);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final ServiceBusReceivedMessage receivedMessage = receiver.receiveMessages().next().block(TIMEOUT);
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getLockedUntil());

        final OffsetDateTime initialLock = receivedMessage.getLockedUntil();
        LOGGER.info("Received message. Seq: {}. lockedUntil: {}", receivedMessage.getSequenceNumber(), initialLock);

        // Assert & Act
        try {
            StepVerifier.create(Mono.delay(Duration.ofSeconds(7))
                    .then(Mono.defer(() -> receiver.renewMessageLock(receivedMessage))))
                .assertNext(lockedUntil -> assertTrue(lockedUntil.isAfter(initialLock),
                    String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                        lockedUntil, initialLock)))
                .expectComplete()
                .verify(TIMEOUT);
        } finally {
            LOGGER.info("Completing message. Seq: {}.", receivedMessage.getSequenceNumber());

            receiver.complete(receivedMessage)
                .doOnNext(m -> logMessage(receivedMessage, receiver.getEntityPath(), "complete"))
                .block(TIMEOUT);
        }
    }

    /**
     * Receiver should receive the messages even if user is not "settling the messages" in PEEK LOCK mode and
     * autoComplete is disabled.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessagesNoMessageSettlement(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int totalMessages = 5;
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Send messages
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(totalMessages, messageId, CONTENTS_BYTES);
        if (isSessionEnabled) {
            messages.forEach(m -> m.setSessionId(sessionId));
        }
        sender.sendMessages(messages).block(TIMEOUT);
        logMessages(messages, sender.getEntityPath(), "sent messages");

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages().take(totalMessages))
            .expectNextCount(totalMessages)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Receiver should receive the messages if  processing time larger than message lock duration and
     * maxAutoLockRenewDuration is set to a large enough duration so user can complete in end.
     * This test takes longer time.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessagesLargeProcessingTime(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final int totalMessages = 2;
        // The lock timeout property set on the queue.
        final Duration lockRenewTimeout = Duration.ofSeconds(15);
        final ClientCreationOptions clientCreationOptions = new ClientCreationOptions().setMaxAutoLockRenewDuration(Duration.ofMinutes(1));
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Send messages
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(totalMessages, messageId, CONTENTS_BYTES);
        if (isSessionEnabled) {
            messages.forEach(m -> m.setSessionId(sessionId));
        }
        sender.sendMessages(messages).block();
        logMessages(messages, sender.getEntityPath(), "sent messages");
        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled, clientCreationOptions);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages()
                .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId()))
                .map(receivedMessage -> Mono.delay(lockRenewTimeout.plusSeconds(2))
                    .then(receiver.complete(receivedMessage)).thenReturn(receivedMessage).block()).take(totalMessages))
            .expectNextCount(totalMessages)
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that the lock can be automatically renewed.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void autoRenewLockOnReceiveMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final AtomicInteger lockRenewCount = new AtomicInteger();

        setSender(entityType, TestUtils.USE_CASE_AUTO_RENEW_RECEIVE, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        // Send the message to verify.
        sendMessage(message).block();

        setReceiver(entityType, TestUtils.USE_CASE_AUTO_RENEW_RECEIVE, isSessionEnabled);

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages().flatMap(received -> {
            LOGGER.info("{}: lockToken[{}]. lockedUntil[{}]. now[{}]", received.getSequenceNumber(),
                received.getLockToken(), received.getLockedUntil(), OffsetDateTime.now());

            // Simulate some sort of long processing.
            while (lockRenewCount.get() < 4) {
                lockRenewCount.incrementAndGet();
                LOGGER.info("Iteration {}: Curren time {}.", lockRenewCount.get(), OffsetDateTime.now());
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException error) {
                    LOGGER.error("Error occurred while sleeping: " + error);
                }
            }
            return receiver.complete(received).thenReturn(received);
        }))
            .assertNext(received -> assertTrue(lockRenewCount.get() > 0))
            .thenCancel()
            .verify(TIMEOUT);
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);
        StepVerifier.create(receiver.receiveMessages()
                .flatMap(receivedMessage -> receiver.abandon(receivedMessage).thenReturn(receivedMessage)).take(1))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .expectComplete()
            .verify(TIMEOUT);
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndDefer(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_PEEK_RECEIVE_AND_DEFER, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block(TIMEOUT);

        setReceiver(entityType, TestUtils.USE_CASE_PEEK_RECEIVE_AND_DEFER, isSessionEnabled);
        AtomicReference<ServiceBusReceivedMessage> received = new AtomicReference<ServiceBusReceivedMessage>();

        // Act & Assert
        StepVerifier.create(receiver.receiveMessages()
            .flatMap(receivedMessage -> receiver.defer(receivedMessage).thenReturn(receivedMessage)).take(1))
            .assertNext(m -> {
                received.set(m);
                assertMessageEquals(m, messageId, isSessionEnabled);
            })
            .expectComplete()
            .verify(TIMEOUT);

        // TODO(Hemant): Identify if this is valid scenario (https://github.com/Azure/azure-sdk-for-java/issues/19673)
        /*receiver.receiveDeferredMessage(received.get().getSequenceNumber())
            .flatMap(m -> receiver.complete(m))
            .block(TIMEOUT);
        messagesPending.decrementAndGet();
         */
    }

    /**
     * Test we can receive a deferred message via sequence number and then perform abandon, suspend, or complete on it.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#receiveDeferredMessageBySequenceNumber")
    @ParameterizedTest
    void receiveDeferredMessageBySequenceNumber(MessagingEntityType entityType, DispositionStatus dispositionStatus) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFERRED_MESSAGE_BY_SEQUENCE_NUMBER, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        StepVerifier.create(sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);

        StepVerifier.create(
            receiver.receiveMessages()
                .flatMap(m -> receiver.defer(m).thenReturn(m))
                .flatMap(received -> {
                    logMessage(received, receiver.getEntityPath(), "received and deferred");
                    return receiver
                        .receiveDeferredMessage(received.getSequenceNumber())
                        .flatMap(deferred -> {
                            logMessage(deferred, receiver.getEntityPath(), "received deferred");

                            assertNotNull(deferred);
                            assertEquals(received.getSequenceNumber(), deferred.getSequenceNumber());

                            switch (dispositionStatus) {
                                case ABANDONED:
                                    logMessage(deferred, receiver.getEntityPath(), "abandon");
                                    return receiver.abandon(deferred).thenReturn(deferred);
                                case SUSPENDED:
                                    logMessage(deferred, receiver.getEntityPath(), "deadLetter");
                                    return receiver.deadLetter(deferred).thenReturn(deferred);
                                case COMPLETED:
                                    logMessage(deferred, receiver.getEntityPath(), "complete");
                                    return receiver.complete(deferred).thenReturn(deferred);
                                default:
                                    throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                                        "Disposition status not recognized for this test case: " + dispositionStatus));
                            }
                        });
                })
                .take(1))
            .expectNextCount(1)
            .expectComplete()
            .verify(TIMEOUT);
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType) {
        // Arrange
        final boolean isSessionEnabled = true;
        setSender(entityType, TestUtils.USE_CASE_SEND_RECEIVE_WITH_PROPERTIES, isSessionEnabled);

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
        sentProperties.put("UUIDProperty", UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d"));
        sentProperties.put("StringProperty", "string");

        sendMessage(messageToSend).block(TIMEOUT);

        setReceiver(entityType, TestUtils.USE_CASE_SEND_RECEIVE_WITH_PROPERTIES, isSessionEnabled);

        // Assert & Act
        StepVerifier.create(receiver.receiveMessages().flatMap(receivedMessage -> receiver.complete(receivedMessage).thenReturn(receivedMessage)).take(1))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);

                final Map<String, Object> received = receivedMessage.getApplicationProperties();

                assertEquals(sentProperties.size(), received.size());

                for (Map.Entry<String, Object> sentEntry : sentProperties.entrySet()) {
                    if (sentEntry.getValue() != null && sentEntry.getValue().getClass().isArray()) {
                        assertArrayEquals((Object[]) sentEntry.getValue(), (Object[]) received.get(sentEntry.getKey()));
                    } else if (!sentEntry.getKey().equals("traceparent") && !sentEntry.getKey().equals("Diagnostic-Id")) {
                        final Object expected = sentEntry.getValue();
                        final Object actual = received.get(sentEntry.getKey());

                        assertEquals(expected, actual, String.format(
                            "Key '%s' does not match. Expected: '%s'. Actual: '%s'", sentEntry.getKey(), expected,
                            actual));
                    }
                }
            })
            .thenCancel()
            .verify(TIMEOUT);
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void setAndGetSessionState(MessagingEntityType entityType) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, true);

        final byte[] sessionState = "Finished".getBytes(UTF_8);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = getMessage(messageId, true);

        sendMessage(messageToSend).block(Duration.ofSeconds(10));

        // Act
        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, true);

        StepVerifier.create(receiver.receiveMessages()
                .flatMap(message -> {
                    LOGGER.info("SessionId: {}. LockToken: {}. LockedUntil: {}. Message received.",
                        message.getSessionId(), message.getLockToken(), message.getLockedUntil());
                    assertMessageEquals(message, messageId, isSessionEnabled);
                    return receiver.abandon(message)
                        .then(receiver.setSessionState(sessionState))
                        .then(receiver.getSessionState());
                })
                .take(1))
            .assertNext(state -> {
                LOGGER.info("State received: {}", new String(state, UTF_8));
                assertArrayEquals(sessionState, state);
            })
            .expectComplete()
            .verify(TIMEOUT);
    }

    /**
     * Verifies that we can receive a message from dead letter queue.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveFromDeadLetter(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        final Duration shortWait = Duration.ofSeconds(2);
        final int entityIndex = 0;

        if (isSessionEnabled && sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        setSender(entityType, entityIndex, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message).block();

        setReceiver(entityType, entityIndex, isSessionEnabled);

        receiver.receiveMessages()
            .filter(receivedMessage -> messageId.equals(receivedMessage.getMessageId()))
            .map(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.deadLetter(receivedMessage).block();
                return receivedMessage;
            }).next().block(OPERATION_TIMEOUT);

        final ServiceBusReceiverAsyncClient deadLetterReceiver;
        switch (entityType) {
            case QUEUE:
                final String queueName = isSessionEnabled ? getSessionQueueName(entityIndex) : getQueueName(entityIndex);
                assertNotNull(queueName, "'queueName' cannot be null.");

                deadLetterReceiver = toClose(getBuilder().receiver()
                    .queueName(queueName)
                    .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                    .buildAsyncClient());
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = isSessionEnabled ? getSessionSubscriptionBaseName() : getSubscriptionBaseName();
                assertNotNull(topicName, "'topicName' cannot be null.");
                assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                deadLetterReceiver = toClose(getBuilder().receiver()
                    .topicName(topicName)
                    .subscriptionName(subscriptionName)
                    .subQueue(SubQueue.DEAD_LETTER_QUEUE)
                    .buildAsyncClient());
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }

        // Assert & Act
        try {
            deadLetterReceiver.receiveMessages()
                .filter(serviceBusReceivedMessage -> messageId.equals(serviceBusReceivedMessage.getMessageId()))
                .map(serviceBusReceivedMessage -> {
                    assertMessageEquals(serviceBusReceivedMessage, messageId, isSessionEnabled);
                    return serviceBusReceivedMessage;
                })
                .next()
                .block(OPERATION_TIMEOUT);
        } finally {
            // close dead letter receiver.
            deadLetterReceiver.close();
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void manualRenewMessageLock(MessagingEntityType entityType) throws InterruptedException {
        testRenewLock(entityType, Duration.ZERO, (m) -> {
            toClose(receiver.renewMessageLock(m, Duration.ofSeconds(10)).subscribe());
            return Mono.empty();
        });
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void autoRenewMessageLock(MessagingEntityType entityType) throws InterruptedException {
        testRenewLock(entityType, Duration.ofSeconds(10), (m) -> Mono.empty());
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void autoAndManualRenewMessageLock(MessagingEntityType entityType) throws InterruptedException {
        testRenewLock(entityType, Duration.ofSeconds(10), (m) -> {
            toClose(receiver.renewMessageLock(m, Duration.ofSeconds(10)).subscribe());
            return Mono.empty();
        });
    }

    private void testRenewLock(MessagingEntityType entityType, Duration lockRenewalDuration, Function<ServiceBusReceivedMessage, Mono<Void>> renewMono) throws InterruptedException {
        setSender(entityType, TestUtils.USE_CASE_RENEW_LOCK, false);
        setReceiver(entityType, TestUtils.USE_CASE_RENEW_LOCK, false, new ClientCreationOptions()
            .setMaxAutoLockRenewDuration(lockRenewalDuration));

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);

        StepVerifier.create(sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);

        AtomicReference<OffsetDateTime> lockedUntil = new AtomicReference<>(null);

        CountDownLatch latch = new CountDownLatch(2);
        receiver.receiveMessages()
            .doOnNext(m -> logMessage(m, receiver.getEntityPath(), "received message"))
            .filter(m -> messageId.equals(m.getMessageId()))
            .flatMap(receivedMessage -> {
                latch.countDown();
                logMessage(receivedMessage, receiver.getEntityPath(), "filtered message");
                LOGGER.atInfo()
                    .addKeyValue("traceparent", receivedMessage.getApplicationProperties().get("traceparent"))
                    .addKeyValue("seqNo", receivedMessage.getSequenceNumber())
                    .addKeyValue("deliveryCount", receivedMessage.getDeliveryCount())
                    .addKeyValue("lockToken", receivedMessage.getLockToken())
                    .addKeyValue("lockedUntil", receivedMessage.getLockedUntil())
                    .log("message properties");
                assertNotNull(receivedMessage.getLockedUntil());
                // expect to receive the same message but only after lock renewal completes
                if (lockedUntil.compareAndSet(null, receivedMessage.getLockedUntil())) {
                    return renewMono.apply(receivedMessage);
                } else {
                    // TODO: why not not always bigger than lockeduntil? time skew with service?
                    assertEquals(OffsetDateTime.now().toEpochSecond(), lockedUntil.get().toEpochSecond(), 10);
                    return receiver.complete(receivedMessage);
                }
            })
            .subscribe(i -> { }, ex -> fail(ex));

        assertTrue(latch.await(2, TimeUnit.MINUTES));
    }

    @Test
    @Disabled("V2 low level async-receiver impl is missing a check to error if reactive app subscribed more than once.")
    void receiveTwice() {
        setSenderAndReceiver(MessagingEntityType.QUEUE, TestUtils.USE_CASE_DEFAULT, false);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);

        StepVerifier.create(sendMessage(message)).verifyComplete();
        StepVerifier.create(receiver.receiveMessages().take(1))
            .expectNextCount(1)
            .expectComplete()
            .verify(OPERATION_TIMEOUT);

        StepVerifier.create(sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);

        // cannot subscribe to the same receiver - there was a subscription that is disposed now
        StepVerifier.create(receiver.receiveMessages().take(1))
            .expectComplete()
            .verify(OPERATION_TIMEOUT);
    }

    @Test
    @Disabled("V2 low level async-receiver impl is missing a check to error if reactive app subscribed more than once.")
    void receiveActiveSubscription() {
        setSenderAndReceiver(MessagingEntityType.QUEUE, TestUtils.USE_CASE_DEFAULT, false);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);

        StepVerifier.create(sendMessage(message))
            .expectComplete()
            .verify(TIMEOUT);
        toClose(receiver.receiveMessages().subscribe(m -> { }));

        // cannot subscribe to the same receiver - there is active subscription
        StepVerifier.create(receiver.receiveMessages().take(1))
            .expectError()
            .verify(OPERATION_TIMEOUT);
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
        final int totalMessages = 1;
        final String subject = "subject";
        final Map<String, Object> footer = new HashMap<>();
        footer.put("footer-key-1", "footer-value-1");
        footer.put("footer-key-2", "footer-value-2");

        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put("ap-key-1", "ap-value-1");
        applicationProperties.put("ap-key-2", "ap-value-2");

        final Map<String, Object> deliveryAnnotation = new HashMap<>();
        deliveryAnnotation.put("delivery-annotations-key-1", "delivery-annotations-value-1");
        deliveryAnnotation.put("delivery-annotations-key-2", "delivery-annotations-value-2");

        final String messageId = UUID.randomUUID().toString();
        final AmqpAnnotatedMessage expectedAmqpProperties = new AmqpAnnotatedMessage(
            AmqpMessageBody.fromData(CONTENTS_BYTES));
        expectedAmqpProperties.getProperties().setSubject(subject);
        expectedAmqpProperties.getProperties().setReplyToGroupId("r-gid");
        expectedAmqpProperties.getProperties().setReplyTo(new AmqpAddress("reply-to"));
        expectedAmqpProperties.getProperties().setContentType("content-type");
        expectedAmqpProperties.getProperties().setCorrelationId(new AmqpMessageId("correlation-id"));
        expectedAmqpProperties.getProperties().setTo(new AmqpAddress("to"));
        expectedAmqpProperties.getProperties().setAbsoluteExpiryTime(OffsetDateTime.now().plusSeconds(60));
        expectedAmqpProperties.getProperties().setUserId("user-id-1".getBytes());
        expectedAmqpProperties.getProperties().setContentEncoding("string");
        expectedAmqpProperties.getProperties().setGroupSequence(2L);
        expectedAmqpProperties.getProperties().setCreationTime(OffsetDateTime.now().plusSeconds(30));

        expectedAmqpProperties.getHeader().setPriority((short) 2);
        expectedAmqpProperties.getHeader().setFirstAcquirer(true);
        expectedAmqpProperties.getHeader().setDurable(true);

        expectedAmqpProperties.getFooter().putAll(footer);
        expectedAmqpProperties.getDeliveryAnnotations().putAll(deliveryAnnotation);
        expectedAmqpProperties.getApplicationProperties().putAll(applicationProperties);

        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        final AmqpAnnotatedMessage amqpAnnotatedMessage = message.getRawAmqpMessage();
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

        setSender(entityType, TestUtils.USE_CASE_VALIDATE_AMQP_PROPERTIES, isSessionEnabled);

        // Send the message
        sendMessage(message).block(TIMEOUT);

        setReceiver(entityType, TestUtils.USE_CASE_VALIDATE_AMQP_PROPERTIES, isSessionEnabled);
        StepVerifier.create(receiver.receiveMessages()/*.take(totalMessages)*/)
            .assertNext(received -> {
                assertNotNull(received.getLockToken());
                AmqpAnnotatedMessage actual = received.getRawAmqpMessage();
                try {
                    assertArrayEquals(CONTENTS_BYTES, message.getBody().toBytes());
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
                    receiver.complete(received).block(Duration.ofSeconds(15));
                    logMessage(received, receiver.getEntityPath(), "completed message");
                }
            })
            .thenCancel()
            .verify(Duration.ofMinutes(2));
    }

    /**
     * Verifies we can autocomplete for a queue.
     *
     * @param entityType Entity Type.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void autoComplete(MessagingEntityType entityType) {
        // Arrange
        final Duration shortWait = Duration.ofSeconds(2);
        final int index = USE_CASE_AUTO_COMPLETE;
        setSender(entityType, index, false);

        final int numberOfEvents = 3;
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = getServiceBusMessages(numberOfEvents, messageId);

        setReceiver(entityType, index, false);

        // Send messages.
        StepVerifier.create(Flux.fromIterable(messages).flatMap(this::sendMessage))
                .verifyComplete();

        final ServiceBusReceiverAsyncClient autoCompleteReceiver =
            toClose(getReceiverBuilder(entityType, index, false)
                .buildAsyncClient());

        Set<Long> sequenceNumbers = new HashSet<>();
        // Act
        // Expecting that as we receive these messages, they'll be completed.
        StepVerifier.create(autoCompleteReceiver.receiveMessages()
                        .filter(m -> messageId.equals(m.getMessageId()))
                        .doOnNext(m -> sequenceNumbers.add(m.getSequenceNumber())))
            .expectNextCount(numberOfEvents)
            .thenAwait(shortWait) // Give time for autoComplete to finish
            .thenCancel()
            .verify(TIMEOUT);

        // Assert messages are completed.
        for (Long sequenceNumber : sequenceNumbers) {
            StepVerifier.create(autoCompleteReceiver.peekMessage(sequenceNumber))
                .verifyComplete();
        }
    }

    /**
     * Asserts the length and values with in the map.
     */
    private void assertMapValues(Map<String, Object> expectedMap, Map<String, Object> actualMap) {
        assertTrue(actualMap.size() >= expectedMap.size());
        for (String key : expectedMap.keySet()) {
            assertEquals(expectedMap.get(key), actualMap.get(key), "Value is not equal for Key " + key);
        }
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSender(entityType, entityIndex, isSessionEnabled);
        setReceiver(entityType, entityIndex, isSessionEnabled);
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setReceiver(entityType, entityIndex, isSessionEnabled, defaultClientCreationOptions);
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled,
        ClientCreationOptions options) {
        this.receiver = createReceiver(entityType, entityIndex, isSessionEnabled, options);
    }

    private ServiceBusReceiverAsyncClient createReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled,
                                                         ClientCreationOptions options) {
        final boolean shareConnection = false;
        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            sessionReceiver = toClose(getSessionReceiverBuilder(entityType, entityIndex, shareConnection, DEFAULT_RETRY_OPTIONS)
                .maxAutoLockRenewDuration(options.getMaxAutoLockRenewDuration())
                .sessionIdleTimeout(options.getSessionIdleTimeout())
                .disableAutoComplete()
                .buildAsyncClient());

            return toClose(sessionReceiver.acceptSession(sessionId).block());
        }
        return toClose(getReceiverBuilder(entityType, entityIndex, shareConnection)
            .maxAutoLockRenewDuration(options.getMaxAutoLockRenewDuration())
            .disableAutoComplete()
            .buildAsyncClient());
    }

    private void setSender(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        final boolean shareConnection = false;
        this.sender = toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, shareConnection)
            .buildAsyncClient());
    }

    private Mono<Void> sendMessage(ServiceBusMessage message) {
        return sender.sendMessage(message).doOnSuccess(aVoid -> {
            logMessage(message, sender.getEntityPath(), "sent");
        });
    }

    /**
     * Class represents various options while creating receiver/sender client.
     */
    public static class ClientCreationOptions {
        Duration maxAutoLockRenewDuration;

        Duration sessionIdleTimeout;
        ClientCreationOptions setMaxAutoLockRenewDuration(Duration maxAutoLockRenewDuration) {
            this.maxAutoLockRenewDuration = maxAutoLockRenewDuration;
            return this;
        }

        ClientCreationOptions setSessionIdleTimeout(Duration sessionIdleTimeout) {
            this.sessionIdleTimeout = sessionIdleTimeout;
            return this;
        }

        Duration getMaxAutoLockRenewDuration() {
            return this.maxAutoLockRenewDuration;
        }
        Duration getSessionIdleTimeout() {
            return this.sessionIdleTimeout;
        }
    }
}
