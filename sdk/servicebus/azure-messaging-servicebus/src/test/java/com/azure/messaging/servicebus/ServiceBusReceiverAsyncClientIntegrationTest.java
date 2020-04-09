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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_POSITION_ID;
import static com.azure.messaging.servicebus.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for receiving from queues or subscriptions in Service Bus.
 */
class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final byte[] CONTENTS_BYTES = "Some-contents".getBytes(StandardCharsets.UTF_8);
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);
    private final AtomicInteger messagesPending = new AtomicInteger();

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusSenderAsyncClient sessionSender;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void afterTest() {
        // In the case that this test failed... we're going to drain the queue or subscription.
        ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false)
            .setMaxAutoRenewDuration(TIMEOUT);

        if (messagesPending.get() == 0) {
            dispose(receiver, sender);
            return;
        }

        try {
            receiver.receive(options)
                .takeUntil(m -> {
                    int pending = messagesPending.decrementAndGet();
                    return pending > 0;
                })
                .flatMap(m -> {
                    logger.info("Completing message: {}", m.getSequenceNumber());
                    return receiver.complete(m);
                })
                .timeout(Duration.ofSeconds(10), Mono.empty())
                .blockLast();
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        } finally {
            dispose(receiver, sender);
        }
    }

    static Stream<Arguments> receiverTypesProvider() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE),
            Arguments.of(MessagingEntityType.SUBSCRIPTION)
        );
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @Disabled("Problem when receiving two messages. Link is closed prematurely.")
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void receiveTwoMessagesAutoComplete(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final int position = 10;
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, position);

        Mono.when(sendMessage(message), sendMessage(message)).block();

        // Assert & Act
        StepVerifier.create(receiver.receive())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId, position))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId, position))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void receiveMessageAutoComplete(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);

        sendMessage(message).block();

        // Assert & Act
        StepVerifier.create(receiver.receive())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId, 0))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void peekMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);

        sendMessage(message).block();

        // Assert & Act
        StepVerifier.create(receiver.peek())
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId, 0))
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive a message.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void sendScheduledMessageAndReceive(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(2);

        sender.scheduleMessage(message, scheduledEnqueueTime).block();

        // Assert & Act
        StepVerifier.create(Mono.delay(Duration.ofSeconds(3)).then(receiver.receive().next()))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId, 0))
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive multiple messages.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void sendMultipleScheduledMessageAndReceive(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, builder -> builder.receiveMode(ReceiveMode.PEEK_LOCK));

        final String messageId1 = UUID.randomUUID().toString();
        final String messageId2 = UUID.randomUUID().toString();
        final ServiceBusMessage message1 = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId1, 0);
        final ServiceBusMessage message2 = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId2, 2);
        final Duration duration = Duration.ofSeconds(10);
        final Instant scheduledEnqueueTime = Instant.now().plus(duration);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        sender.scheduleMessage(message1, scheduledEnqueueTime)
            .block(TIMEOUT);
        sender.scheduleMessage(message2, scheduledEnqueueTime)
            .block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(Mono.delay(duration).thenMany(receiver.receive(options).take(2)))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId1, 0))
            .assertNext(receivedMessage -> assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId2, 2))
            .verifyComplete();
    }

    /**
     * Verifies that we can cancel a scheduled message.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void cancelScheduledMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);
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
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void peekFromSequenceNumberMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final long fromSequenceNumber = 1;
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);

        sendMessage(message).block();

        // Assert & Act
        StepVerifier.create(receiver.peekAt(fromSequenceNumber))
            .assertNext(receivedMessage -> {
                try {
                    assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageId, 0);
                } finally {
                    // Clear out that message after the operation so we don't have to wait.
                    receiver.complete(receivedMessage).block();
                    messagesPending.decrementAndGet();
                }
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void peekBatchMessages(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);
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
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void peekBatchMessagesFromSequence(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);
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
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void deadLetterMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next()
            .block(TIMEOUT);

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.deadLetter(receivedMessage))
            .verifyComplete();

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void receiveAndRenewLock(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, "id-1", 0);

        final AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final AtomicReference<Instant> initialLock = new AtomicReference<>();
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        // Blocking here because it is not part of the scenario we want to test.
        sendMessage(message).block(TIMEOUT);
        ServiceBusReceivedMessage m = receiver.receive(options).next().block(TIMEOUT);
        assertNotNull(m);
        assertNotNull(m.getLockedUntil());
        receivedMessage.set(m);
        initialLock.set(m.getLockedUntil());

        // Assert & Act
        try {
            StepVerifier.create(Mono.delay(Duration.ofSeconds(10))
                .then(Mono.defer(() -> receiver.renewMessageLock(receivedMessage.get()))))
                .assertNext(lockedUntil -> {
                    assertTrue(lockedUntil.isAfter(initialLock.get()),
                        String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                            lockedUntil, initialLock.get()));

                    assertEquals(receivedMessage.get().getLockedUntil(), lockedUntil);
                })
                .verifyComplete();
        } finally {
            receiver.complete(receivedMessage.get()).block();
            messagesPending.decrementAndGet();
        }
    }

    /**
     * Verifies that the lock can be automatically renewed.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void autoRenewLockOnReceiveMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getServiceBusMessage(CONTENTS_BYTES, messageId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false)
            .setMaxAutoRenewDuration(Duration.ofSeconds(120));

        // Send the message to verify.
        sendMessage(message).block(TIMEOUT);

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

    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void receiveAndAbandon(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageTrackingId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        sendMessage(message).block(TIMEOUT);

        final ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next()
            .block(TIMEOUT);

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.abandon(receivedMessage))
            .verifyComplete();
    }

    static Stream<Arguments> receiveBySequenceNumber() {
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
    void receiveBySequenceNumber(MessagingEntityType entityType, DispositionStatus dispositionStatus) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageTrackingId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        final ServiceBusReceivedMessage receivedMessage = sendMessage(message)
            .then(receiver.receive(options).next())
            .block(TIMEOUT);

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

    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void sendReceiveMessageWithVariousPropertyTypes(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType);

        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageTrackingId, 0);
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

        sender.send(messageToSend);

        // Assert & Act
        StepVerifier.create(receiver.receive(options))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, CONTENTS_BYTES, messageTrackingId, 0);

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

    @Test
    void sessionReceiveAndDeleteWithBinaryData() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageTrackingId, 0)
            .setSessionId(SESSION_ID);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        // Assert & Act
        StepVerifier.create(sessionSender.send(message).thenMany(sessionReceiveDeleteModeReceiver.receive(options)))
            .assertNext(receivedMessage ->
                assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .thenCancel()
            .verify();
    }

    void setSenderAndReceiver(MessagingEntityType entityType) {
        setSenderAndReceiver(entityType, Function.identity());
    }

    void setSenderAndReceiver(MessagingEntityType entityType,
        Function<ServiceBusReceiverClientBuilder, ServiceBusReceiverClientBuilder> onCreate) {

        switch (entityType) {
            case QUEUE:
                final String queueName = getQueueName();

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = createBuilder().sender()
                    .queueName(queueName)
                    .buildAsyncClient();
                receiver = onCreate.apply(
                    createBuilder().receiver().queueName(queueName)
                ).buildAsyncClient();
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName();
                final String subscriptionName = getSubscriptionName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = createBuilder().sender()
                    .topicName(topicName)
                    .buildAsyncClient();
                receiver = onCreate.apply(createBuilder().receiver()
                    .topicName(topicName).subscriptionName(subscriptionName))
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

    private void assertMessageEquals(ServiceBusReceivedMessage message, byte[] contents, String messageId, int position) {
        assertEquals(contents, message.getBody());

        final Map<String, Object> properties = message.getProperties();

        assertTrue(properties.containsKey(MESSAGE_TRACKING_ID));
        assertEquals(messageId, properties.get(MESSAGE_TRACKING_ID));

        assertTrue(properties.containsKey(MESSAGE_POSITION_ID));
        assertEquals(position, properties.get(MESSAGE_POSITION_ID));
    }
}
