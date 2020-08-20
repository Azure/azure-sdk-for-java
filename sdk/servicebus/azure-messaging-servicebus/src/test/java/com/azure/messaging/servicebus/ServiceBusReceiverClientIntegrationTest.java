// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.models.DeadLetterOptions;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link ServiceBusReceiverClient} from queues or subscriptions.
 */
@Tag("integration")
class ServiceBusReceiverClientIntegrationTest extends IntegrationTestBase {

    /* Sometime not all the messages are cleaned-up. This is buffer to ensure all the messages are cleaned-up.*/
    private static final int BUFFER_MESSAGES_TO_REMOVE = 10;

    private final AtomicInteger messagesPending = new AtomicInteger();
    private final AtomicReference<List<Long>> messagesDeferred = new AtomicReference<>(new ArrayList<>());
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverClientIntegrationTest.class);

    private ServiceBusReceiverClient receiver;
    private ServiceBusSenderClient sender;

    /**
     * Receiver used to clean up resources in {@link #afterTest()}.
     */
    private ServiceBusReceiverClient receiveAndDeleteReceiver;

    protected ServiceBusReceiverClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        final int pending = messagesPending.get();
        if (pending < 1 && messagesDeferred.get().size() < 1) {
            dispose(receiver, sender, receiveAndDeleteReceiver);
            return;
        }

        // In the case that this test failed... we're going to drain the queue or subscription.
        if (pending > 0) {
            try {
                IterableStream<ServiceBusReceivedMessageContext> removedMessage = receiveAndDeleteReceiver.receiveMessages(
                    pending, Duration.ofSeconds(15));

                removedMessage.stream().forEach(context -> {
                    ServiceBusReceivedMessage message = context.getMessage();
                    logger.info("Removed Message Seq: {} ", message.getSequenceNumber());
                });
            } catch (Exception e) {
                logger.warning("Error occurred when draining queue.", e);
            }
        }

        if (messagesDeferred.get().size() > 0) {
            try {
                List<Long> listOfDeferredMessages = messagesDeferred.get();
                for (Long seqNumber : listOfDeferredMessages) {
                    ServiceBusReceivedMessage deferredMessages = receiver.receiveDeferredMessage(seqNumber);
                    receiver.complete(deferredMessages.getLockToken());
                }
            } catch (Exception e) {
                logger.warning("Error occurred when draining deferred messages Entity: {} ", receiver.getEntityPath(), e);
            }
        }

        dispose(receiver, sender, receiveAndDeleteReceiver);
    }

    /**
     * Verifies that we can only call receive() multiple times and with one of the receive does timeout.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void multipleReceiveByOneSubscriberMessageTimeout(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_MULTIPLE_RECEIVE_ONE_TIMEOUT, isSessionEnabled);
        final int maxMessages = 2;
        final int totalReceive = 2;
        final Duration shortTimeOut = Duration.ofSeconds(7);

        final String messageId = UUID.randomUUID().toString();
        List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceive * maxMessages; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }

        // Act & Assert
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(maxMessages, shortTimeOut);
        long received = messages.stream().count();
        assertEquals(0, received);

        sendMessages(messageList);

        int receivedMessageCount;
        int totalReceivedCount = 0;
        for (int i = 0; i < totalReceive; ++i) {
            messages = receiver.receiveMessages(maxMessages, shortTimeOut);
            receivedMessageCount = 0;
            for (ServiceBusReceivedMessageContext receivedMessage : messages) {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.complete(receivedMessage.getMessage().getLockToken());
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
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        final int maxMessagesEachReceive = 3;
        final int totalReceiver = 7;
        final Duration shortTimeOut = Duration.ofSeconds(8);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceiver * maxMessagesEachReceive; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }

        sendMessages(messageList);

        // Act & Assert
        IterableStream<ServiceBusReceivedMessageContext> messages;

        int receivedMessageCount;
        int totalReceivedCount = 0;
        for (int i = 0; i < totalReceiver; ++i) {
            messages = receiver.receiveMessages(maxMessagesEachReceive, shortTimeOut);
            receivedMessageCount = 0;
            for (ServiceBusReceivedMessageContext receivedMessage : messages) {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.complete(receivedMessage.getMessage().getLockToken());
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
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        final int maxMessagesEachReceive = 3;
        final int totalReceiver = 6;
        final Duration shortTimeOut = Duration.ofSeconds(8);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceiver * maxMessagesEachReceive; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }

        sendMessages(messageList);

        // Act & Assert
        AtomicInteger totalReceivedMessages = new AtomicInteger();
        List<Thread> receiverThreads = new ArrayList<>();
        for (int i = 0; i < totalReceiver; ++i) {
            Thread thread = new Thread(() -> {
                IterableStream<ServiceBusReceivedMessageContext> messages1 = receiver.
                    receiveMessages(maxMessagesEachReceive, shortTimeOut);
                int receivedMessageCount = 0;
                long lastSequenceReceiver = 0;
                for (ServiceBusReceivedMessageContext receivedMessage : messages1) {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    receiver.complete(receivedMessage.getMessage().getLockToken());
                    assertTrue(receivedMessage.getMessage().getSequenceNumber() > lastSequenceReceiver);
                    lastSequenceReceiver = receivedMessage.getMessage().getSequenceNumber();
                    messagesPending.decrementAndGet();
                    ++receivedMessageCount;
                }
                totalReceivedMessages.addAndGet(receivedMessageCount);
                assertEquals(maxMessagesEachReceive, receivedMessageCount);
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
        assertEquals(totalReceiver * maxMessagesEachReceive, totalReceivedMessages.get());
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_RECEIVE_MORE_AND_COMPLETE, isSessionEnabled);
        int maxMessages = 5;
        int messagesToSend = 4;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        sendMessage(message);
        sendMessage(message);
        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(maxMessages, TIMEOUT);

        // Assert
        int receivedMessageCount = 0;
        final long startTime = System.currentTimeMillis();
        for (ServiceBusReceivedMessageContext context : messages) {
            ServiceBusReceivedMessage receivedMessage = context.getMessage();
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage.getLockToken());
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
        setSenderAndReceiver(MessagingEntityType.QUEUE, TestUtils.USE_CASE_RECEIVE_NO_MESSAGES, isSessionEnabled);
        int howManyMessage = 2;
        long noMessages = 0;

        // Act
        final IterableStream<ServiceBusReceivedMessageContext> messages = receiver.receiveMessages(howManyMessage,
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
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        int maxMessages = 1;
        final String deadLetterReason = "testing";


        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Act & Assert
        final Stream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT)
            .stream().map(ServiceBusReceivedMessageContext::getMessage);

        final ServiceBusTransactionContext transaction = receiver.createTransaction();

        List<ServiceBusReceivedMessage> messageList = messages.collect(Collectors.toList());
        assertEquals(maxMessages, messageList.size());

        ServiceBusReceivedMessage receivedMessage = messageList.get(0);

        switch (dispositionStatus) {
            case COMPLETED:
                receiver.complete(receivedMessage.getLockToken(), transaction);
                messagesPending.decrementAndGet();
                break;
            case ABANDONED:
                receiver.abandon(receivedMessage.getLockToken(), null, transaction);
                break;
            case SUSPENDED:
                DeadLetterOptions deadLetterOptions = new DeadLetterOptions().setDeadLetterReason(deadLetterReason);
                receiver.deadLetter(receivedMessage.getLockToken(), deadLetterOptions, transaction);
                break;
            case DEFERRED:
                receiver.defer(receivedMessage.getLockToken(), null, transaction);
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Disposition status not recognized for this test case: " + dispositionStatus));
        }

        receiver.commitTransaction(transaction);
    }

    /**
     * Verifies that we can send, receive one message and settle on session entity.
     */
    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void transactionReceiveAndCommitOnSessionEntity(MessagingEntityType entityType) {

        // Arrange
        boolean isSessionEnabled = true;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        final int maxMessages = 1;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Act & Assert
        final Stream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT)
            .stream().map(ServiceBusReceivedMessageContext::getMessage);

        final ServiceBusTransactionContext transaction = receiver.createTransaction();

        List<ServiceBusReceivedMessage> messageList = messages.collect(Collectors.toList());
        assertEquals(maxMessages, messageList.size());

        ServiceBusReceivedMessage receivedMessage = messageList.get(0);

        receiver.complete(receivedMessage.getLockToken(), sessionId, transaction);
        receiver.commitTransaction(transaction);
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and receive one messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        int maxMessages = 1;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Act
        final Stream<ServiceBusReceivedMessage> messages = receiver.receiveMessages(maxMessages, TIMEOUT)
            .stream()
            .map(ServiceBusReceivedMessageContext::getMessage);

        // Assert
        final AtomicInteger receivedMessageCount = new AtomicInteger();
        messages.forEach(receivedMessage -> {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage.getLockToken());
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
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Act
        ServiceBusReceivedMessage receivedMessage = receiver.peekMessage();

        // Assert
        assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekFromSequenceNumberMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);

        // Act
        ServiceBusReceivedMessage receivedPeekMessage = receiver.peekMessageAt(receivedMessage.getSequenceNumber());

        // Assert
        assertEquals(receivedMessage.getSequenceNumber(), receivedPeekMessage.getSequenceNumber());
        assertMessageEquals(receivedPeekMessage, messageId, isSessionEnabled);

        receiver.complete(receivedMessage.getLockToken());
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekBatchMessages(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 2;

        sendMessage(message);
        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekMessages(maxMessages);

        // Assert
        Assertions.assertEquals(maxMessages, (int) iterableMessages.stream().count());
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void peekBatchMessagesFromSequence(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 2;
        final int fromSequenceNumber = 1;

        sendMessage(message);
        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekMessagesAt(maxMessages, fromSequenceNumber);

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
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.deadLetter(receivedMessage.getLockToken());

        messagesPending.decrementAndGet();
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.complete(receivedMessage.getLockToken());

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 1;

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getLockedUntil());

        final Instant initialLock = receivedMessage.getLockedUntil();
        logger.info("Received message. Seq: {}. lockedUntil: {}", receivedMessage.getSequenceNumber(), initialLock);

        // Assert & Act
        try {
            Instant lockedUntil = receiver.renewMessageLock(receivedMessage.getLockToken());
            assertTrue(lockedUntil.isAfter(initialLock),
                String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                    lockedUntil, initialLock));
        } finally {
            logger.info("Completing message. Seq: {}.", receivedMessage.getSequenceNumber());
            receiver.complete(receivedMessage.getLockToken());
            messagesPending.decrementAndGet();
        }
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.abandon(receivedMessage.getLockToken());

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
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);

        receiver.defer(receivedMessage.getLockToken());

        final ServiceBusReceivedMessage receivedDeferredMessage = receiver
            .receiveDeferredMessage(receivedMessage.getSequenceNumber());

        assertNotNull(receivedDeferredMessage);
        assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        // Assert & Act
        switch (dispositionStatus) {
            case ABANDONED:
                receiver.abandon(receivedDeferredMessage.getLockToken());
                messagesDeferred.get().add(receivedMessage.getSequenceNumber());
                break;
            case SUSPENDED:
                receiver.deadLetter(receivedDeferredMessage.getLockToken());
                break;
            case COMPLETED:
                receiver.complete(receivedDeferredMessage.getLockToken());
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
        setSenderAndReceiver(entityType, 0, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessageContext> context = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(context);

        final List<ServiceBusReceivedMessageContext> asList = context.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0).getMessage();
        assertNotNull(receivedMessage);

        // Act & Assert
        receiver.defer(receivedMessage.getLockToken());

        // cleanup
        completeDeferredMessages(receiver, receivedMessage, isSessionEnabled);
        messagesPending.addAndGet(-maxMessages);
    }

    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SEND_RECEIVE_WITH_PROPERTIES, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

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

        sendMessage(messageToSend);

        // Assert & Act
        final IterableStream<ServiceBusReceivedMessageContext> messages =
            receiveAndDeleteReceiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream()
            .map(ServiceBusReceivedMessageContext::getMessage)
            .collect(Collectors.toList());

        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        messagesPending.decrementAndGet();
        assertMessageEquals(receivedMessage, messageId, isSessionEnabled);

        final Map<String, Object> received = receivedMessage.getProperties();

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
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, entityIndex, isSessionEnabled, false);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled,
        boolean sharedConnection) {
        this.sender = getSenderBuilder(false, entityType, entityIndex, isSessionEnabled, sharedConnection).buildClient();

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.receiver = getSessionReceiverBuilder(false, entityType, entityIndex, Function.identity(), sharedConnection)
                .sessionId(sessionId)
                .buildClient();
            this.receiveAndDeleteReceiver = getSessionReceiverBuilder(false, entityType, entityIndex,
                Function.identity(), sharedConnection)
                .sessionId(sessionId)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildClient();
        } else {
            this.receiver = getReceiverBuilder(false, entityType, entityIndex, Function.identity(), sharedConnection)
                .buildClient();
            this.receiveAndDeleteReceiver = getReceiverBuilder(false, entityType, entityIndex,
                Function.identity(), sharedConnection)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
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
        final IterableStream<ServiceBusReceivedMessageContext> contextStream = client.receiveMessages(totalMessages, TIMEOUT);
        final List<ServiceBusReceivedMessageContext> asList = contextStream.stream().collect(Collectors.toList());
        for (ServiceBusReceivedMessageContext context : asList) {
            receiver.complete(context.getMessage().getLockToken());
        }
        return asList.size();
    }

    private void completeDeferredMessages(ServiceBusReceiverClient client, ServiceBusReceivedMessage receivedMessage, boolean isSessionEnabled) {
        final ServiceBusReceivedMessage message;
        if (isSessionEnabled) {
            message = client.receiveDeferredMessage(receivedMessage.getSequenceNumber(), sessionId);
        } else {
            message = client.receiveDeferredMessage(receivedMessage.getSequenceNumber());
        }
        receiver.complete(message.getLockToken());
    }
}
