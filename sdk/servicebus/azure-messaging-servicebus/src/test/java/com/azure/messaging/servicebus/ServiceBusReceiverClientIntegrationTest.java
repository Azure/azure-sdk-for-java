// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ServiceBusReceiverClient} from queues or subscriptions.
 */
public class ServiceBusReceiverClientIntegrationTest extends IntegrationTestBase {

    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverClientIntegrationTest.class);

    ServiceBusReceiverClient receiver;
    ServiceBusSenderClient sender;
    /**
     * Receiver used to clean up resources in {@link #afterTest()}.
     */
    ServiceBusReceiverClient receiveAndDeleteReceiver;

    final AtomicInteger messagesPending = new AtomicInteger();


    protected ServiceBusReceiverClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverClientIntegrationTest.class));
    }

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        final int pending = messagesPending.get();
        if (pending < 1) {
            dispose(receiver, sender, receiveAndDeleteReceiver);
            return;
        }
        // In the case that this test failed... we're going to drain the queue or subscription.
        try {
            receiveAndDeleteReceiver.receive(pending);
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        } finally {
            dispose(receiver, sender, receiveAndDeleteReceiver);
        }
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int howManyMessage = 2;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);
        sendMessage(message);

        // Assert & Act
        Iterable<ServiceBusReceivedMessage> iterableMessages = receiver.receive(howManyMessage, TIMEOUT);

        for (ServiceBusReceivedMessage receivedMessage: iterableMessages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
        }

        messagesPending.decrementAndGet();
        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send and receive one messages.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveMessageAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);
        int howManyMessage = 1;

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        sendMessage(message);

        // Assert & Act
        Iterable<ServiceBusReceivedMessage> iterableMessages = receiver.receive(howManyMessage, TIMEOUT);

        for (ServiceBusReceivedMessage receivedMessage: iterableMessages) {
            assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
        }

        messagesPending.decrementAndGet();
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

        sendMessage(message);

        // Assert & Act
        ServiceBusReceivedMessage receivedMessage = receiver.peek();
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

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);

        assertNotNull(receivedMessage);

        // Assert & Act
        ServiceBusReceivedMessage receivedPeekMessage = receiver.peekAt(receivedMessage.getSequenceNumber());
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

        // Assert & Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekBatch(maxMessages);
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

        // Assert & Act
        IterableStream<ServiceBusReceivedMessage> iterableMessages = receiver.peekBatchAt(maxMessages, fromSequenceNumber);
        Assertions.assertEquals(maxMessages, iterableMessages.stream().collect(Collectors.toList()).size());
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

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);

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

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);

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

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);
        Assertions.assertNotNull(receivedMessage);

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

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);
        Assertions.assertNotNull(receivedMessage);

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
        sendMessage(message);
        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);
        Assertions.assertNotNull(receivedMessage);

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

        if (dispositionStatus == DispositionStatus.ABANDONED || dispositionStatus == DispositionStatus.COMPLETED) {
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

        sendMessage(message);

        final IterableStream<ServiceBusReceivedMessage> messageIte = receiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);
        Assertions.assertNotNull(receivedMessage);

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
        final IterableStream<ServiceBusReceivedMessage> messageIte = receiveAndDeleteReceiver.receive(1, TIMEOUT);
        Assertions.assertNotNull(messageIte);

        final List<ServiceBusReceivedMessage> asList = messageIte.stream().collect(Collectors.toList());
        ServiceBusReceivedMessage receivedMessage = asList.get(0);
        Assertions.assertNotNull(receivedMessage);

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
