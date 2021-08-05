// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.models.AmqpMessageBody;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link com.azure.messaging.servicebus.ServiceBusReceiverClient} from queues or subscriptions.
 */
@Tag("integration")
class ServiceBusReceiverClientIntegrationTest extends IntegrationTestBase {
    private final AtomicInteger messagesPending = new AtomicInteger();
    private final AtomicReference<List<Long>> messagesDeferred = new AtomicReference<>(new ArrayList<>());
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverClientIntegrationTest.class);

    private ServiceBusReceiverClient receiver;
    private ServiceBusSenderClient sender;
    private ServiceBusSessionReceiverClient sessionReceiver;

    protected ServiceBusReceiverClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, sender, sessionReceiver);
    }

    /**
     * Verifies that we can only call receive() multiple times and with one of the receive does timeout.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void multipleReceiveByOneSubscriberMessageTimeout(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_MULTIPLE_RECEIVE_ONE_TIMEOUT, isSessionEnabled);
        final int maxMessages = 2;
        final int totalReceive = 2;
        final Duration shortTimeOut = Duration.ofSeconds(7);
        int receivedMessageCount;
        int totalReceivedCount = 0;
        IterableStream<ServiceBusReceivedMessage> messages;

        final String messageId = UUID.randomUUID().toString();
        List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceive * maxMessages; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }
        sendMessages(messageList);

        setReceiver(entityType, TestUtils.USE_CASE_MULTIPLE_RECEIVE_ONE_TIMEOUT, isSessionEnabled);

        // Act & Assert
        for (int i = 0; i < totalReceive; ++i) {
            messages = receiver.receiveMessages(maxMessages, shortTimeOut);
            receivedMessageCount = 0;
            for (ServiceBusReceivedMessage receivedMessage : messages) {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.complete(receivedMessage);
                messagesPending.decrementAndGet();
                ++receivedMessageCount;
            }
            assertEquals(maxMessages, receivedMessageCount);
            totalReceivedCount += receivedMessageCount;
        }

        assertEquals(totalReceive * maxMessages, totalReceivedCount);
    }

    /**
     * Verifies that we can only call receive() multiple times.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void multipleReceiveByOneSubscriber(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);
        final int maxMessagesEachReceive = 3;
        final int totalReceiver = 7;
        final Duration shortTimeOut = Duration.ofSeconds(8);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceiver * maxMessagesEachReceive; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }

        sendMessages(messageList);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Act & Assert
        IterableStream<ServiceBusReceivedMessage> messages;

        int receivedMessageCount;
        int totalReceivedCount = 0;
        for (int i = 0; i < totalReceiver; ++i) {
            messages = receiver.receiveMessages(maxMessagesEachReceive, shortTimeOut);
            receivedMessageCount = 0;
            for (ServiceBusReceivedMessage receivedMessage : messages) {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.complete(receivedMessage);
                messagesPending.decrementAndGet();
                ++receivedMessageCount;
            }
            assertEquals(maxMessagesEachReceive, receivedMessageCount);
            totalReceivedCount += receivedMessageCount;
        }

        assertEquals(totalReceiver * maxMessagesEachReceive, totalReceivedCount);
    }

    /**
     * Verifies that we can only call receive() multiple times.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void parallelReceiveByOneSubscriber(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);
        final int maxMessagesEachReceive = 3;
        final int totalReceiver = 3;
        final Duration shortTimeOut = Duration.ofSeconds(4);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceiver * maxMessagesEachReceive; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }

        sendMessages(messageList);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Act & Assert
        AtomicInteger totalReceivedMessages = new AtomicInteger();
        List<Thread> receiverThreads = new ArrayList<>();
        for (int i = 0; i < totalReceiver; ++i) {
            Thread thread = new Thread(() -> {
                IterableStream<ServiceBusReceivedMessage> messages1 = receiver.
                    receiveMessages(maxMessagesEachReceive, shortTimeOut);
                int receivedMessageCount = 0;
                long lastSequenceReceiver = 0;
                for (ServiceBusReceivedMessage receivedMessage : messages1) {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    receiver.complete(receivedMessage);
                    assertTrue(receivedMessage.getSequenceNumber() > lastSequenceReceiver);
                    lastSequenceReceiver = receivedMessage.getSequenceNumber();
                    messagesPending.decrementAndGet();
                    ++receivedMessageCount;
                }
                totalReceivedMessages.addAndGet(receivedMessageCount);
                assertTrue(receivedMessageCount >= 1);
            });
            receiverThreads.add(thread);
        }

        receiverThreads.forEach(Thread::start);

        receiverThreads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                fail("Error in receiving messages: " + e.getMessage());
            }
        });
        assertTrue(totalReceivedMessages.get() >= totalReceiver);
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_RECEIVE_MORE_AND_COMPLETE, isSessionEnabled);
        int maxMessages = 4;
        int messagesToSend = 4;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        sendMessage(message);
        sendMessage(message);
        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_RECEIVE_MORE_AND_COMPLETE, isSessionEnabled);

        // Act
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT);

        // Assert
        int receivedMessageCount = 0;
        final long startTime = System.currentTimeMillis();
        for (ServiceBusReceivedMessage receivedMessage : messages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage);
            messagesPending.decrementAndGet();
            ++receivedMessageCount;
        }
        final long endTime = System.currentTimeMillis();
        assertEquals(messagesToSend, receivedMessageCount);
        assertTrue(TIMEOUT.toMillis() > (endTime - startTime));
    }

    /**
     * Verifies that we do not receive any message in given timeout.
     */
    @Test
    void receiveNoMessage() {
        // Arrange
        boolean isSessionEnabled = false;
        setReceiver(MessagingEntityType.QUEUE, TestUtils.USE_CASE_RECEIVE_NO_MESSAGES, isSessionEnabled);
        int howManyMessage = 2;
        long noMessages = 0;

        // Act
        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(howManyMessage,
            Duration.ofSeconds(15));

        // Assert
        assertEquals(noMessages, messages.stream().count());
    }

    /**
     * Verifies that we can send, receive one message and settle on non session entity.
     */
    @ParameterizedTest
    @EnumSource(DispositionStatus.class)
    void transactionMessageAndSettle(DispositionStatus dispositionStatus) {

        // Arrange
        final boolean isSessionEnabled = false;
        final MessagingEntityType entityType = MessagingEntityType.QUEUE;
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);
        int maxMessages = 1;
        final String deadLetterReason = "testing";


        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Act & Assert
        final Stream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT).stream();

        final ServiceBusTransactionContext transaction = receiver.createTransaction();

        List<ServiceBusReceivedMessage> messageList = messages.collect(Collectors.toList());
        assertEquals(maxMessages, messageList.size());

        ServiceBusReceivedMessage receivedMessage = messageList.get(0);

        switch (dispositionStatus) {
            case COMPLETED:
                receiver.complete(receivedMessage, new CompleteOptions().setTransactionContext(transaction));
                messagesPending.decrementAndGet();
                break;
            case ABANDONED:
                receiver.abandon(receivedMessage, new AbandonOptions()
                    .setTransactionContext(transaction));
                break;
            case SUSPENDED:
                DeadLetterOptions deadLetterOptions = new DeadLetterOptions()
                    .setDeadLetterReason(deadLetterReason)
                    .setTransactionContext(transaction);
                receiver.deadLetter(receivedMessage, deadLetterOptions);
                break;
            case DEFERRED:
                receiver.defer(receivedMessage, new DeferOptions().setTransactionContext(transaction));
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Disposition status not recognized for this test case: " + dispositionStatus));
        }

        receiver.commitTransaction(transaction);
    }

    /**
     * Verifies that we can send and receive one messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);
        int maxMessages = 1;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Act
        final Stream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT)
            .stream();

        // Assert
        final AtomicInteger receivedMessageCount = new AtomicInteger();
        messages.forEach(receivedMessage -> {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage);
            messagesPending.decrementAndGet();
            receivedMessageCount.incrementAndGet();
        });

        assertEquals(maxMessages, receivedMessageCount.get());

    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        // Act
        ServiceBusReceivedMessage receivedMessage = receiver.peekMessage();

        // Assert
        assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
    }

    /**
     * Verifies that an empty entity does not error when peeked.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessageEmptyEntity(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setReceiver(entityType, TestUtils.USE_CASE_EMPTY_ENTITY, isSessionEnabled);

        // Act
        final ServiceBusReceivedMessage receivedMessage = receiver.peekMessage();

        // Assert
        assertNull(receivedMessage);
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekFromSequenceNumberMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Act
        final ServiceBusReceivedMessage receivedPeekMessage = receiver.peekMessage(receivedMessage.getSequenceNumber());

        // Assert
        assertEquals(receivedMessage.getSequenceNumber(), receivedPeekMessage.getSequenceNumber());
        assertMessageEquals(receivedPeekMessage, messageId, isSessionEnabled);

        receiver.complete(receivedMessage);
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessages(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_PEEK_BATCH, isSessionEnabled);
        final byte[] payload = "peek-message".getBytes(Charset.defaultCharset());
        final AtomicInteger messageId = new AtomicInteger();
        final AtomicLong actualCount = new AtomicLong();

        final int maxMessages = 2;
        for (int i = 0; i < maxMessages; ++i) {
            ServiceBusMessage message = getMessage("" + i, isSessionEnabled, AmqpMessageBody.fromData(payload));
            sendMessage(message);
        }
        setReceiver(entityType, TestUtils.USE_CASE_PEEK_BATCH, isSessionEnabled);

        // Act

        // maxMessages are not always guaranteed, sometime, we get less than asked for, so we will try two times.
        // https://github.com/Azure/azure-sdk-for-java/issues/21168
        for (int i = 0; i < 2 && actualCount.get() < maxMessages; ++i) {
            int finalI = i;
            receiver.peekMessages(maxMessages).stream().forEach(receivedMessage -> {
                actualCount.addAndGet(1);
                assertEquals(String.valueOf(messageId.getAndIncrement()), receivedMessage.getMessageId(),
                    String.format("Message id did not match. Payload: [%s], try [%s].",
                        receivedMessage.getBody().toString(), finalI));
            });
        }

        // Assert
        assertEquals(maxMessages, actualCount.get());
    }

    /**
     * Verifies that an empty entity does not error when a batch is peeked.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessagesEmptyEntity(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setReceiver(entityType, TestUtils.USE_CASE_EMPTY_ENTITY, isSessionEnabled);
        final int maxMessages = 10;

        // Act
        final IterableStream<ServiceBusReceivedMessage> messages = receiver.peekMessages(maxMessages);

        // Assert
        assertNotNull(messages);

        final Optional<ServiceBusReceivedMessage> anyMessages = messages.stream().findAny();
        assertFalse(anyMessages.isPresent());
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void peekMessagesFromSequence(MessagingEntityType entityType) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 2;
        final int fromSequenceNumber = 1;

        sendMessage(message);
        sendMessage(message);

        setReceiver(entityType, 0, false);

        // Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekMessages(maxMessages, fromSequenceNumber);

        // Assert
        final List<ServiceBusReceivedMessage> asList = iterableMessages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());

        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);
        assertTrue(receivedMessage.getSequenceNumber() >= fromSequenceNumber);
    }

    /**
     * Verifies that we can dead-letter a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void deadLetterMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final IterableStream<ServiceBusReceivedMessage> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessage> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.deadLetter(receivedMessage);

        messagesPending.decrementAndGet();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);
        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final IterableStream<ServiceBusReceivedMessage> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessage> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.complete(receivedMessage);

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) throws InterruptedException {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 1;

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message);

        setReceiver(entityType, 0, false);

        final IterableStream<ServiceBusReceivedMessage> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessage> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getLockedUntil());

        final OffsetDateTime initialLock = receivedMessage.getLockedUntil();
        logger.info("Received message. Seq: {}. lockedUntil: {}", receivedMessage.getSequenceNumber(), initialLock);

        // Assert & Act
        try {
            TimeUnit.SECONDS.sleep(5); // Let some lock duration expire.
            OffsetDateTime lockedUntil = receiver.renewMessageLock(receivedMessage);
            assertTrue(lockedUntil.isAfter(initialLock),
                String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                    lockedUntil, initialLock));
        } finally {
            logger.info("Completing message. Seq: {}.", receivedMessage.getSequenceNumber());
            receiver.complete(receivedMessage);
            messagesPending.decrementAndGet();
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final IterableStream<ServiceBusReceivedMessage> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessage> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.abandon(receivedMessage);

        // Cleanup
        int messagesCompleted = completeMessages(receiver, maxMessages);
        messagesPending.addAndGet(-messagesCompleted);
    }

    /**
     * Test we can receive a deferred message via sequence number and then perform abandon, suspend, or complete on it.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#receiveDeferredMessageBySequenceNumber")
    @ParameterizedTest
    void receiveDeferredMessageBySequenceNumber(MessagingEntityType entityType, DispositionStatus dispositionStatus) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 1;

        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final IterableStream<ServiceBusReceivedMessage> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessage> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        receiver.defer(receivedMessage);

        final ServiceBusReceivedMessage receivedDeferredMessage = receiver
            .receiveDeferredMessage(receivedMessage.getSequenceNumber());

        assertNotNull(receivedDeferredMessage);
        assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        // Assert & Act
        switch (dispositionStatus) {
            case ABANDONED:
                receiver.abandon(receivedDeferredMessage);
                messagesDeferred.get().add(receivedMessage.getSequenceNumber());
                break;
            case SUSPENDED:
                receiver.deadLetter(receivedDeferredMessage);
                break;
            case COMPLETED:
                receiver.complete(receivedDeferredMessage);
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Disposition status not recognized for this test case: " + dispositionStatus));
        }

        if (dispositionStatus == DispositionStatus.SUSPENDED || dispositionStatus == DispositionStatus.COMPLETED) {
            messagesPending.decrementAndGet();
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndDefer(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;
        sendMessage(message);

        setReceiver(entityType, TestUtils.USE_CASE_DEFAULT, isSessionEnabled);
        final IterableStream<ServiceBusReceivedMessage> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessage> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Act & Assert
        receiver.defer(receivedMessage);
        // cleanup
        final ServiceBusReceivedMessage deferred;
        deferred = receiver.receiveDeferredMessage(receivedMessage.getSequenceNumber());
        receiver.complete(deferred);
        messagesPending.addAndGet(-maxMessages);
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSender(entityType, TestUtils.USE_CASE_SEND_RECEIVE_WITH_PROPERTIES, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

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

        sendMessage(messageToSend);

        setReceiver(entityType, TestUtils.USE_CASE_SEND_RECEIVE_WITH_PROPERTIES, isSessionEnabled);

        // Assert & Act
        final IterableStream<ServiceBusReceivedMessage> messages =
            receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream()
            .collect(Collectors.toList());

        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        messagesPending.decrementAndGet();
        assertMessageEquals(receivedMessage, messageId, isSessionEnabled);

        final Map<String, Object> received = receivedMessage.getApplicationProperties();

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
        receiver.complete(receivedMessage);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, entityIndex, isSessionEnabled, false);
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setReceiver(entityType, entityIndex, isSessionEnabled, false);
    }

    private void setSender(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSender(entityType, entityIndex, isSessionEnabled, false);
    }

    private void setSender(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled, boolean sharedConnection) {
        this.sender = getSenderBuilder(false, entityType, entityIndex, isSessionEnabled, sharedConnection).buildClient();
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled, boolean sharedConnection) {

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.sessionReceiver = getSessionReceiverBuilder(false, entityType, entityIndex, sharedConnection)
                .buildClient();
            this.receiver = this.sessionReceiver.acceptSession(sessionId);
        } else {
            this.receiver = getReceiverBuilder(false, entityType, entityIndex, sharedConnection)
                .buildClient();
        }
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled,
        boolean sharedConnection) {
        this.sender = getSenderBuilder(false, entityType, entityIndex, isSessionEnabled, sharedConnection).buildClient();

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.sessionReceiver = getSessionReceiverBuilder(false, entityType, entityIndex, sharedConnection)
                .buildClient();
            this.receiver =  this.sessionReceiver.acceptSession(sessionId);
        } else {
            this.receiver = getReceiverBuilder(false, entityType, entityIndex, sharedConnection)
                .buildClient();
        }
    }

    private void sendMessages(List<ServiceBusMessage> messageList) {
        sender.sendMessages(messageList);
        int number = messagesPending.getAndSet(messageList.size());
        logger.info("Number sent: {}", number);
    }

    private void sendMessage(ServiceBusMessage message) {
        sender.sendMessage(message);
        int number = messagesPending.incrementAndGet();
        logger.info("Number sent: {}", number);
    }

    private int completeMessages(ServiceBusReceiverClient client, int totalMessages) {
        final IterableStream<ServiceBusReceivedMessage> contextStream = client.receiveMessages(totalMessages, TIMEOUT);
        final List<ServiceBusReceivedMessage> asList = contextStream.stream().collect(Collectors.toList());
        for (ServiceBusReceivedMessage message : asList) {
            receiver.complete(message);
        }
        return asList.size();
    }
}
