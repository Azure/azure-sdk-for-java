// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ServiceBusReceiverClient} from queues or subscriptions.
 */
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
                IterableStream<ServiceBusReceivedMessage> removedMessage = receiveAndDeleteReceiver.receive(
                    pending + BUFFER_MESSAGES_TO_REMOVE, Duration.ofSeconds(15));

                removedMessage.stream().forEach(receivedMessage -> {
                    logger.info("Removed Message Seq: {} ", receivedMessage.getSequenceNumber());
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
                    receiver.complete(deferredMessages);
                }
            } catch (Exception e) {
                logger.warning("Error occurred when draining deferred messages Entity: {} ", receiver.getEntityPath(), e);
            }

        }

        dispose(receiver, sender, receiveAndDeleteReceiver);
    }

    /**
     * Verifies that we can only call receive() once only.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveByTwoSubscriber(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        final int maxMessages = 1;
        final Duration shortTimeOut = Duration.ofSeconds(5);

        // Act & Assert
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, shortTimeOut);

        final long receivedMessages = messages.stream().count();
        assertEquals(0L, receivedMessages);

        // Second time user try to receive, it should throw exception.
        assertThrows(IllegalStateException.class, () -> receiver.receive(maxMessages, shortTimeOut));
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int maxMessages = 2;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);

        // Assert
        int receivedMessageCount = 0;
        for (ServiceBusReceivedMessage receivedMessage : messages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage);
            messagesPending.decrementAndGet();
            ++receivedMessageCount;
        }

        assertEquals(maxMessages, receivedMessageCount);

    }

    /**
     * Verifies that we do not receive any message in given timeout.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveNoMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int howManyMessage = 2;
        long noMessages = 0;

        // Act
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(howManyMessage, Duration.ofSeconds(15));

        // Assert
        assertEquals(noMessages, messages.stream().count());
    }

    /**
     * Verifies that we can send and receive one messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int maxMessages = 1;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);

        // Assert

        int receivedMessageCount = 0;
        for (ServiceBusReceivedMessage receivedMessage : messages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
            receiver.complete(receivedMessage);
            messagesPending.decrementAndGet();
            ++receivedMessageCount;
        }
        assertEquals(maxMessages, receivedMessageCount);

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

        sendMessage(message);

        // Act
        ServiceBusReceivedMessage receivedMessage = receiver.peek();

        // Assert
        assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
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
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Act
        ServiceBusReceivedMessage receivedPeekMessage = receiver.peekAt(receivedMessage.getSequenceNumber());

        // Assert
        assertEquals(receivedMessage.getSequenceNumber(), receivedPeekMessage.getSequenceNumber());
        assertMessageEquals(receivedPeekMessage, messageId, isSessionEnabled);
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void peekBatchMessages(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 2;

        sendMessage(message);
        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekBatch(maxMessages);

        // Assert
        Assertions.assertEquals(maxMessages, iterableMessages.stream().collect(Collectors.toList()).size());
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

        sendMessage(message);
        sendMessage(message);

        // Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekBatchAt(maxMessages, fromSequenceNumber);

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
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void deadLetterMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.deadLetter(receivedMessage);

        messagesPending.decrementAndGet();
    }

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
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
    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, false);
        final int maxMessages = 1;

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);
        assertNotNull(receivedMessage.getLockedUntil());

        final Instant initialLock = receivedMessage.getLockedUntil();
        logger.info("Received message. Seq: {}. lockedUntil: {}", receivedMessage.getSequenceNumber(), initialLock);

        // Assert & Act
        try {
            Instant lockedUntil = receiver.renewMessageLock(receivedMessage);
            assertTrue(lockedUntil.isAfter(initialLock),
                String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                    lockedUntil, initialLock));
        } finally {
            logger.info("Completing message. Seq: {}.", receivedMessage.getSequenceNumber());
            receiver.complete(receivedMessage);
            messagesPending.decrementAndGet();
        }
    }

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Assert & Act
        receiver.abandon(receivedMessage);
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
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
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

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndDefer(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final int maxMessages = 1;

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messages = receiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
        assertEquals(maxMessages, asList.size());
        final ServiceBusReceivedMessage receivedMessage = asList.get(0);
        assertNotNull(receivedMessage);

        // Act & Assert
        receiver.defer(receivedMessage);

    }

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

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
        final IterableStream<ServiceBusReceivedMessage> messages = receiveAndDeleteReceiver.receive(maxMessages, TIMEOUT);
        Assertions.assertNotNull(messages);

        final List<ServiceBusReceivedMessage> asList = messages.stream().collect(Collectors.toList());
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

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, isSessionEnabled, Function.identity());
    }

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled,
        Function<ServiceBusClientBuilder.ServiceBusReceiverClientBuilder, ServiceBusClientBuilder.ServiceBusReceiverClientBuilder> onReceiverCreate) {

        switch (entityType) {
            case QUEUE:
                final String queueName = isSessionEnabled ? getSessionQueueName() : getQueueName();

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = createBuilder().sender()
                    .queueName(queueName)
                    .buildClient();
                receiver = onReceiverCreate.apply(
                    createBuilder().receiver()
                        .queueName(queueName)
                        .sessionId(isSessionEnabled ? sessionId : null)
                ).buildClient();

                receiveAndDeleteReceiver = createBuilder().receiver()
                    .queueName(queueName)
                    .sessionId(isSessionEnabled ? sessionId : null)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildClient();
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName();
                final String subscriptionName = isSessionEnabled ? getSessionSubscriptionName() : getSubscriptionName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = createBuilder().sender()
                    .topicName(topicName)
                    .buildClient();
                receiver = onReceiverCreate.apply(
                    createBuilder().receiver()
                        .topicName(topicName).subscriptionName(subscriptionName)
                        .sessionId(isSessionEnabled ? sessionId : null))
                    .buildClient();

                receiveAndDeleteReceiver = createBuilder().receiver()
                    .topicName(topicName).subscriptionName(subscriptionName)
                    .sessionId(isSessionEnabled ? sessionId : null)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildClient();
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    private void sendMessage(ServiceBusMessage message) {
        sender.send(message);
        int number = messagesPending.incrementAndGet();
        logger.info("Number sent: {}", number);
    }
}
