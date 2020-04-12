// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusReceiverClientBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_POSITION_ID;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ServiceBusReceiverAsyncClient} from queues or subscriptions.
 */
class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);
    private final AtomicInteger messagesPending = new AtomicInteger();

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient receiveAndDeleteReceiver;
    private ServiceBusSenderAsyncClient sender;
    private String sessionId;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        if (messagesPending.get() == 0) {
            dispose(receiver, sender, receiveAndDeleteReceiver);
            return;
        }

        // In the case that this test failed... we're going to drain the queue or subscription.
        try {
            receiveAndDeleteReceiver.receive(new ReceiveAsyncOptions().setEnableAutoComplete(false))
                .take(messagesPending.get())
                .map(message -> {
                    logger.info("Message received: {}", message.getSequenceNumber());
                    return message;
                })
                .timeout(Duration.ofSeconds(5), Mono.empty())
                .blockLast();
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        } finally {
            dispose(receiver, sender, receiveAndDeleteReceiver);
        }
    }

    static Stream<Arguments> messagingEntityProvider() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE),
            Arguments.of(MessagingEntityType.SUBSCRIPTION)
        );
    }

    static Stream<Arguments> messagingEntityWithSessions() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE, false),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, false),
            Arguments.of(MessagingEntityType.QUEUE, true),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, true)
        );
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @Disabled("Problem when receiving two messages. Link is closed prematurely.")
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        Mono.when(sendMessage(message), sendMessage(message)).block();

        // Assert & Act
        StepVerifier.create(receiver.receive())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .thenCancel()
            .verify();
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

        sendMessage(message).block();

        // Assert & Act
        StepVerifier.create(receiver.receive())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .thenCancel()
            .verify();
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

        sendMessage(message).block();

        // Assert & Act
        StepVerifier.create(receiver.peek())
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
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        sender.scheduleMessage(message, scheduledEnqueueTime).block();

        // Assert & Act
        StepVerifier.create(Mono.delay(Duration.ofSeconds(3)).then(receiveAndDeleteReceiver.receive(options).next()))
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

        final Long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime).block();
        logger.verbose("Scheduled the message, sequence number {}.", sequenceNumber);

        assertNotNull(sequenceNumber);

        Mono.delay(delayDuration)
            .then(sender.cancelScheduledMessage(sequenceNumber))
            .block();

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

        sendMessage(message).block();

        final ServiceBusReceivedMessage received = receiver.receive().next().block();
        Assertions.assertNotNull(received);

        // Assert & Act
        StepVerifier.create(receiver.peekAt(received.getSequenceNumber()))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, messageId, isSessionEnabled))
            .verifyComplete();
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

        Mono.when(sendMessage(message), sendMessage(message)).block();

        // Assert & Act
        StepVerifier.create(receiver.peekBatch(maxMessages))
            .expectNextCount(maxMessages)
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

        Mono.when(sendMessage(message), sendMessage(message)).block();

        // Assert & Act
        StepVerifier.create(receiver.peekBatchAt(maxMessages, fromSequenceNumber))
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
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        sendMessage(message).block();

        final ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next()
            .block();

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.deadLetter(receivedMessage))
            .verifyComplete();

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message).block();
        ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next().block();
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
                .block();
            ;
        }
    }

    /**
     * Verifies that the lock can be automatically renewed.
     */
    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void autoRenewLockOnReceiveMessage(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false)
            .setMaxAutoRenewDuration(Duration.ofSeconds(120));

        // Send the message to verify.
        sendMessage(message).block();

        // Act & Assert
        StepVerifier.create(receiver.receive(options))
            .assertNext(received -> {
                assertNotNull(received.getLockedUntil());
                assertNotNull(received.getLockToken());

                logger.info("{}: now: [{}]. lockId[{}]. lockedUntil[{}]", received.getSequenceNumber(), Instant.now(),
                    received.getLockToken(), received.getLockedUntil());

                final Instant initial = received.getLockedUntil();
                final Instant timeToStop = initial.plusSeconds(5);
                Instant latest = Instant.MIN;

                // Simulate some sort of long processing.
                final AtomicInteger iteration = new AtomicInteger();
                while (Instant.now().isBefore(timeToStop)) {
                    logger.info("Iteration: {}", iteration.incrementAndGet());

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
            .verify();
    }

    @MethodSource("messagingEntityWithSessions")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType, boolean isSessionEnabled) {
        // Arrange
        setSenderAndReceiver(entityType, isSessionEnabled);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        sendMessage(message).block();

        final ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next()
            .block();

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.abandon(receivedMessage))
            .verifyComplete();
    }

    static Stream<Arguments> receiveDeferredMessageBySequenceNumber() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE, DispositionStatus.ABANDONED),
            Arguments.of(MessagingEntityType.QUEUE, DispositionStatus.SUSPENDED),
            Arguments.of(MessagingEntityType.QUEUE, DispositionStatus.COMPLETED),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, DispositionStatus.ABANDONED),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, DispositionStatus.SUSPENDED),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, DispositionStatus.COMPLETED)
        );
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
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        final ServiceBusReceivedMessage receivedMessage = sendMessage(message)
            .then(receiver.receive(options).next())
            .block();

        assertNotNull(receivedMessage);

        receiver.defer(receivedMessage).block();

        final ServiceBusReceivedMessage receivedDeferredMessage = receiver
            .receiveDeferredMessage(receivedMessage.getSequenceNumber())
            .block();

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
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

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

        sendMessage(messageToSend).block();

        // Assert & Act
        StepVerifier.create(receiveAndDeleteReceiver.receive(options))
            .assertNext(receivedMessage -> {
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
            })
            .thenCancel()
            .verify();
    }

    private ServiceBusMessage getMessage(String messageId, boolean isSessionEnabled) {
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        return isSessionEnabled ? message.setSessionId(sessionId) : message;
    }

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled) {
        setSenderAndReceiver(entityType, isSessionEnabled, Function.identity());
    }

    private void setSenderAndReceiver(MessagingEntityType entityType, boolean isSessionEnabled,
        Function<ServiceBusReceiverClientBuilder, ServiceBusReceiverClientBuilder> onReceiverCreate) {

        switch (entityType) {
            case QUEUE:
                final String queueName = isSessionEnabled ? getSessionQueueName() : getQueueName();

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = createBuilder().sender()
                    .queueName(queueName)
                    .buildAsyncClient();
                receiver = onReceiverCreate.apply(
                    createBuilder().receiver().queueName(queueName)
                ).buildAsyncClient();

                receiveAndDeleteReceiver = createBuilder().receiver()
                    .queueName(queueName)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildAsyncClient();
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName();
                final String subscriptionName = isSessionEnabled ? getSessionSubscriptionName() : getSubscriptionName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = createBuilder().sender()
                    .topicName(topicName)
                    .buildAsyncClient();
                receiver = onReceiverCreate.apply(createBuilder().receiver()
                    .topicName(topicName).subscriptionName(subscriptionName))
                    .buildAsyncClient();

                receiveAndDeleteReceiver = createBuilder().receiver()
                    .topicName(topicName).subscriptionName(subscriptionName)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildAsyncClient();
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }

    private Mono<Void> sendMessage(ServiceBusMessage message) {
        return sender.send(message).doOnSuccess(aVoid -> {
            int number = messagesPending.incrementAndGet();
            logger.info("Number sent: {}", number);
        });
    }

    private void assertMessageEquals(ServiceBusReceivedMessage message, String messageId, boolean isSessionEnabled) {
        assertArrayEquals(CONTENTS_BYTES, message.getBody());
        assertEquals(messageId, message.getMessageId());

        if (isSessionEnabled) {
            assertEquals(sessionId, message.getSessionId());
        }
    }
}
