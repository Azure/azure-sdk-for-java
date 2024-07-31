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
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.TestUtils.USE_CASE_PEEK_BATCH;
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
@Execution(ExecutionMode.SAME_THREAD)
class ServiceBusReceiverClientIntegrationTest extends IntegrationTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReceiverClientIntegrationTest.class);

    private final AtomicInteger messagesPending = new AtomicInteger();
    private final AtomicReference<List<Long>> messagesDeferred = new AtomicReference<>(new ArrayList<>());

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

        final String messageId = UUID.randomUUID().toString();
        List<ServiceBusMessage> messageList = new ArrayList<>();
        for (int i = 0; i < totalReceive * maxMessages; ++i) {
            messageList.add(getMessage(messageId, isSessionEnabled));
        }
        sendMessages(messageList);

        setReceiver(entityType, TestUtils.USE_CASE_MULTIPLE_RECEIVE_ONE_TIMEOUT, isSessionEnabled);

        List<ServiceBusReceivedMessage> messages;
        // Act & Assert
        for (int i = 0; i < totalReceive; ++i) {
            IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(maxMessages, shortTimeOut);
            messages = logReceivedMessages(received, receiver.getEntityPath(), "received messages");
            receivedMessageCount = 0;
            for (ServiceBusReceivedMessage receivedMessage : messages) {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.complete(receivedMessage);
                logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
        List<ServiceBusReceivedMessage> messages;

        int receivedMessageCount;
        int totalReceivedCount = 0;
        for (int i = 0; i < totalReceiver; ++i) {
            IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(maxMessagesEachReceive, shortTimeOut);
            messages = logReceivedMessages(received, receiver.getEntityPath(), "received messages");
            receivedMessageCount = 0;
            for (ServiceBusReceivedMessage receivedMessage : messages) {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                receiver.complete(receivedMessage);
                logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
                IterableStream<ServiceBusReceivedMessage> received = receiver.
                    receiveMessages(maxMessagesEachReceive, shortTimeOut);
                List<ServiceBusReceivedMessage> messages1 = logReceivedMessages(received, receiver.getEntityPath(), "received messages");
                int receivedMessageCount = 0;
                long lastSequenceReceiver = 0;
                for (ServiceBusReceivedMessage receivedMessage : messages1) {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    receiver.complete(receivedMessage);
                    logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
                t.join(TIMEOUT.toMillis());
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
        IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(maxMessages, TIMEOUT);
        List<ServiceBusReceivedMessage> messages = logReceivedMessages(received, receiver.getEntityPath(), "received messages");

        // Assert
        int receivedMessageCount = 0;
        final long startTime = System.currentTimeMillis();
        for (ServiceBusReceivedMessage receivedMessage : messages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage);
            logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
        final IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(howManyMessage,
            Duration.ofSeconds(15));
        List<ServiceBusReceivedMessage> messages = logReceivedMessages(received, receiver.getEntityPath(), "received messages");
        // Assert
        assertEquals(noMessages, messages.size());
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
        final IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(maxMessages, TIMEOUT);
        List<ServiceBusReceivedMessage> messageList = logReceivedMessages(received, receiver.getEntityPath(), "received messages");

        final ServiceBusTransactionContext transaction = receiver.createTransaction();

        assertEquals(maxMessages, messageList.size());

        ServiceBusReceivedMessage receivedMessage = messageList.get(0);

        switch (dispositionStatus) {
            case COMPLETED:
                receiver.complete(receivedMessage, new CompleteOptions().setTransactionContext(transaction));
                logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
                messagesPending.decrementAndGet();
                break;
            case ABANDONED:
                receiver.abandon(receivedMessage, new AbandonOptions()
                    .setTransactionContext(transaction));
                logMessage(receivedMessage, receiver.getEntityPath(), "abandoned message");
                break;
            case SUSPENDED:
                DeadLetterOptions deadLetterOptions = new DeadLetterOptions()
                    .setDeadLetterReason(deadLetterReason)
                    .setTransactionContext(transaction);
                receiver.deadLetter(receivedMessage, deadLetterOptions);
                logMessage(receivedMessage, receiver.getEntityPath(), "deadLettered message");
                break;
            case DEFERRED:
                receiver.defer(receivedMessage, new DeferOptions().setTransactionContext(transaction));
                logMessage(receivedMessage, receiver.getEntityPath(), "deferred message");
                break;
            case RELEASED:
                break;
            default:
                throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
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
        final IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(maxMessages, TIMEOUT);
        List<ServiceBusReceivedMessage> messages = logReceivedMessages(received, receiver.getEntityPath(), "received messages");
        // Assert
        final AtomicInteger receivedMessageCount = new AtomicInteger();
        messages.forEach(receivedMessage -> {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage);
            logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
        logMessage(receivedMessage, receiver.getEntityPath(), "peeked messages");

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

        final IterableStream<ServiceBusReceivedMessage> received = receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(received);

        List<ServiceBusReceivedMessage> messages = logReceivedMessages(received, receiver.getEntityPath(), "received messages");
        assertEquals(maxMessages, messages.size());
        final ServiceBusReceivedMessage receivedMessage = messages.get(0);
        assertNotNull(receivedMessage);

        // Act
        final ServiceBusReceivedMessage receivedPeekMessage = receiver.peekMessage(receivedMessage.getSequenceNumber());
        logMessage(receivedPeekMessage, receiver.getEntityPath(), "peeked message");

        // Assert
        assertEquals(receivedMessage.getSequenceNumber(), receivedPeekMessage.getSequenceNumber());
        assertMessageEquals(receivedPeekMessage, messageId, isSessionEnabled);

        receiver.complete(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityWithSessions")
    @ParameterizedTest
    void peekMessages(MessagingEntityType entityType, boolean isSessionEnabled) throws InterruptedException {
        // Arrange
        setSender(entityType, USE_CASE_PEEK_BATCH, isSessionEnabled);
        final byte[] payload = "peek-message".getBytes(Charset.defaultCharset());
        final int maxMessages = 4;

        List<ServiceBusMessage> messagesToSend = IntStream.range(0, maxMessages)
            .mapToObj(i -> getMessage(String.format("%s-%s-%s", entityType, isSessionEnabled, i),
                isSessionEnabled, AmqpMessageBody.fromData(payload)))
            .collect(Collectors.toList());
        Set<String> messageIds = messagesToSend.stream().map(ServiceBusMessage::getMessageId).collect(Collectors.toSet());

        setReceiver(entityType, USE_CASE_PEEK_BATCH, isSessionEnabled);
        List<ServiceBusReceivedMessage> filtered = new ArrayList<>();
        AtomicInteger iterations = new AtomicInteger(0);

        // Act
        Thread peekMessages = new Thread(() -> {
            // maxMessages are not always guaranteed, sometime, we get less than asked for, just trying two times is not enough, so we will try many times
            // https://github.com/Azure/azure-sdk-for-java/issues/21168
            while (iterations.getAndIncrement() < 10 && filtered.size() < maxMessages) {
                receiver.peekMessages(maxMessages)
                        .forEach(m -> {
                            logMessage(m, receiver.getEntityPath(), "peeked messages");
                            if (messageIds.contains(m.getMessageId())) {
                                filtered.add(m);
                            }
                        });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    fail();
                }
            }
        });

        peekMessages.start();
        toClose((AutoCloseable) () -> peekMessages.interrupt());

        messagesToSend.forEach(message -> sendMessage(message));
        peekMessages.join(TIMEOUT.toMillis());

        LOGGER.atInfo()
            .addKeyValue("received", filtered.size())
            .addKeyValue("iterations", iterations.get())
            .addKeyValue("isSessionEnabled", isSessionEnabled)
            .log("done receiving");

        // Assert
        assertEquals(maxMessages, filtered.size());
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
        final IterableStream<ServiceBusReceivedMessage> received = receiver.peekMessages(maxMessages);
        // Assert
        assertNotNull(received);

        List<ServiceBusReceivedMessage> messages = logReceivedMessages(received, receiver.getEntityPath(), "peeked messages");
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
        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(iterableMessages, receiver.getEntityPath(), "peeked messages");
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

        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(context, receiver.getEntityPath(), "received messages");
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.deadLetter(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "deadLettered message");
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

        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(context, receiver.getEntityPath(), "received messages");

        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.complete(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
        logMessage(receivedMessage, receiver.getEntityPath(), "received message, locked until " + initialLock);

        // Assert & Act
        try {
            TimeUnit.SECONDS.sleep(5); // Let some lock duration expire.
            OffsetDateTime lockedUntil = receiver.renewMessageLock(receivedMessage);
            logMessage(receivedMessage, receiver.getEntityPath(), "renewed lock");
            assertTrue(lockedUntil.isAfter(initialLock),
                String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                    lockedUntil, initialLock));
        } finally {
            receiver.complete(receivedMessage);
            logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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

        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(context, receiver.getEntityPath(), "received messages");

        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.abandon(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "abandoned message");
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

        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(context, receiver.getEntityPath(), "received messages");
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        receiver.defer(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "deferred message");
        final ServiceBusReceivedMessage receivedDeferredMessage = receiver
            .receiveDeferredMessage(receivedMessage.getSequenceNumber());

        assertNotNull(receivedDeferredMessage);
        assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        // Assert & Act
        switch (dispositionStatus) {
            case ABANDONED:
                receiver.abandon(receivedDeferredMessage);
                logMessage(receivedMessage, receiver.getEntityPath(), "abandoned message");
                messagesDeferred.get().add(receivedMessage.getSequenceNumber());
                break;
            case SUSPENDED:
                receiver.deadLetter(receivedDeferredMessage);
                logMessage(receivedMessage, receiver.getEntityPath(), "deadLettered message");
                break;
            case COMPLETED:
                receiver.complete(receivedDeferredMessage);
                logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
                break;
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
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

        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(context, receiver.getEntityPath(), "received messages");
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Act & Assert
        receiver.defer(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "deferred messages");

        // cleanup
        final ServiceBusReceivedMessage deferred;
        deferred = receiver.receiveDeferredMessage(receivedMessage.getSequenceNumber());
        logMessage(deferred, receiver.getEntityPath(), "received deffered messages");
        receiver.complete(deferred);
        logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
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
        final IterableStream<ServiceBusReceivedMessage> received =
            receiver.receiveMessages(maxMessages, TIMEOUT);
        assertNotNull(received);

        final List<ServiceBusReceivedMessage> asList = logReceivedMessages(received, receiver.getEntityPath(), "received messages");

        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        messagesPending.decrementAndGet();
        assertMessageEquals(receivedMessage, messageId, isSessionEnabled);

        final Map<String, Object> receivedProps = receivedMessage.getApplicationProperties();

        assertEquals(sentProperties.size(), receivedProps.size());

        for (Map.Entry<String, Object> sentEntry : sentProperties.entrySet()) {
            if (sentEntry.getValue() != null && sentEntry.getValue().getClass().isArray()) {
                assertArrayEquals((Object[]) sentEntry.getValue(), (Object[]) receivedProps.get(sentEntry.getKey()));
            } else if (!sentEntry.getKey().equals("traceparent") && !sentEntry.getKey().equals("Diagnostic-Id")) {
                final Object expected = sentEntry.getValue();
                final Object actual = receivedProps.get(sentEntry.getKey());

                assertEquals(expected, actual, String.format(
                    "Key '%s' does not match. Expected: '%s'. Actual: '%s'", sentEntry.getKey(), expected,
                    actual));
            }
        }
        receiver.complete(receivedMessage);
        logMessage(receivedMessage, receiver.getEntityPath(), "completed message");
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setReceiver(entityType, entityIndex, isSessionEnabled, false);
    }

    private void setSender(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled) {
        setSender(entityType, entityIndex, isSessionEnabled, false);
    }

    private void setSender(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled, boolean sharedConnection) {
        this.sender = toClose(getSenderBuilder(entityType, entityIndex, isSessionEnabled, sharedConnection).buildClient());
    }

    private void setReceiver(MessagingEntityType entityType, int entityIndex, boolean isSessionEnabled, boolean sharedConnection) {

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.sessionReceiver = toClose(getSessionReceiverBuilder(entityType, entityIndex, sharedConnection, null)
                .buildClient());
            this.receiver = toClose(this.sessionReceiver.acceptSession(sessionId));
        } else {
            this.receiver = toClose(getReceiverBuilder(entityType, entityIndex, sharedConnection)
                .buildClient());
        }
    }

    private void sendMessages(List<ServiceBusMessage> messageList) {
        sender.sendMessages(messageList);
        int number = messagesPending.getAndSet(messageList.size());
        logMessages(messageList, sender.getEntityPath(), "sendMessages");
        LOGGER.atInfo().addKeyValue("number", number).log("message sent");
    }

    private void sendMessage(ServiceBusMessage message) {
        sender.sendMessage(message);
        logMessage(message, sender.getEntityPath(), "sent");
        LOGGER.atInfo()
            .addKeyValue("number", messagesPending.incrementAndGet())
            .log("message sent");
    }

    private int completeMessages(ServiceBusReceiverClient client, int totalMessages) {
        final IterableStream<ServiceBusReceivedMessage> contextStream = client.receiveMessages(totalMessages, TIMEOUT);
        final List<ServiceBusReceivedMessage> asList = contextStream.stream().collect(Collectors.toList());
        for (ServiceBusReceivedMessage message : asList) {
            receiver.complete(message);
            logMessage(message, receiver.getEntityPath(), "received and completed");
        }
        return asList.size();
    }
}
