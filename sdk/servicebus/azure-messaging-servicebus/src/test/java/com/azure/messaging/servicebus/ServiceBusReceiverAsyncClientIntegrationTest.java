// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import java.util.stream.Stream;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String CONTENTS = "Test-contents";
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient receiveDeleteModeReceiver;
    private ServiceBusSenderAsyncClient sender;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final String queueName = getQueueName();
        assertNotNull(queueName, "'queueName' cannot be null.");

        sender = createBuilder().sender().queueName(queueName).buildAsyncClient();
        receiver = createBuilder()
            .receiver()
            .queueName(queueName)
            .buildAsyncClient();

        receiveDeleteModeReceiver = createBuilder()
            .receiver()
            .queueName(queueName)
            .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
            .buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, receiveDeleteModeReceiver, sender);
    }

    /**
     * Verifies that we can send and receive two messages.
     */
    @Disabled("Problem when receiving two messages. Link is closed prematurely.")
    @Test
    void receiveTwoMessagesAutoComplete() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(sender.send(message))
            .thenMany(receiver.receive(options)))
            .assertNext(receivedMessage ->
                assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .assertNext(receivedMessage ->
                assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @Test
    void receiveMessageAutoComplete() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiver.receive(options)))
            .assertNext(receivedMessage ->
                assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @Test
    void peekMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(receiver.peek()))
            .assertNext(receivedMessage -> assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive a message.
     */
    @Test
    void sendScheduledMessageAndReceive() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(2);

        sender.scheduleMessage(message, scheduledEnqueueTime)
            .delaySubscription(Duration.ofSeconds(3))
            .block();

        // Assert & Act
        StepVerifier.create(receiver.receive().take(1))
            .assertNext(receivedMessage -> {
                assertArrayEquals(contents.getBytes(), receivedMessage.getBody());
                assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive multiple messages.
     */
    @Test
    void sendMultipleScheduledMessageAndReceive() {
        // Arrange
        final String messageId1 = UUID.randomUUID().toString();
        final String messageId2 = UUID.randomUUID().toString();
        final ServiceBusMessage message1 = TestUtils.getServiceBusMessage(CONTENTS, messageId1, 0);
        final ServiceBusMessage message2 = TestUtils.getServiceBusMessage(CONTENTS, messageId2, 0);
        final Duration duration = Duration.ofSeconds(10);
        final Instant scheduledEnqueueTime = Instant.now().plus(duration);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        sender.scheduleMessage(message1, scheduledEnqueueTime)
            .block(TIMEOUT);
        sender.scheduleMessage(message2, scheduledEnqueueTime)
            .block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(Mono.delay(duration).thenMany(receiveDeleteModeReceiver.receive(options).take(2)))
            .assertNext(receivedMessage -> {
                final String actual = new String(receivedMessage.getBody(), StandardCharsets.UTF_8);
                assertEquals(CONTENTS, actual);

                final Map<String, Object> properties = receivedMessage.getProperties();
                assertTrue(properties.containsKey(MESSAGE_TRACKING_ID));
                assertEquals(messageId1, properties.get(MESSAGE_TRACKING_ID));
            })
            .assertNext(receivedMessage -> {
                final String actual = new String(receivedMessage.getBody(), StandardCharsets.UTF_8);
                assertEquals(CONTENTS, actual);
                final Map<String, Object> properties = receivedMessage.getProperties();
                assertTrue(properties.containsKey(MESSAGE_TRACKING_ID));
                assertEquals(messageId2, properties.get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can cancel a scheduled message.
     */
    @Test
    void cancelScheduledMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(10);
        final Duration delayDuration = Duration.ofSeconds(3);

        final Long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime).block();
        logger.verbose("Scheduled the message, sequence number {}.", sequenceNumber);

        assertNotNull(sequenceNumber);

        Mono.delay(delayDuration)
            .then(sender.cancelScheduledMessage(sequenceNumber))
            .block();
        logger.verbose("Cancelled the scheduled message, sequence number {}.", sequenceNumber);

        // Assert & Act
        StepVerifier.create(receiver.receive().take(1))
            .thenAwait(Duration.ofSeconds(2))
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @Test
    void peekFromSequenceNumberMessage() {
        // Arrange
        final long fromSequenceNumber = 1;
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(receiver.peekAt(fromSequenceNumber)))
            .assertNext(receivedMessage -> assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .verifyComplete();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @Test
    void peekBatchMessages() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);
        int maxMessages = 2;

        // Assert & Act
        StepVerifier.create(Mono.when(sender.send(message), sender.send(message))
            .thenMany(receiver.peekBatch(maxMessages)))
            .expectNextCount(maxMessages)
            .verifyComplete();
    }

    /**
     * Verifies that we can send and peek a batch of messages.
     */
    @Test
    void peekBatchMessagesFromSequence() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);
        int maxMessages = 2;
        int fromSequenceNumber = 1;

        // Assert & Act
        StepVerifier.create(Mono.when(sender.send(message), sender.send(message))
            .thenMany(receiver.peekBatchAt(maxMessages, fromSequenceNumber)))
            .expectNextCount(maxMessages)
            .verifyComplete();
    }

    /**
     * Verifies that we can deadletter a message.
     */
    @Test
    void deadLetterMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions().setEnableAutoComplete(false);

        sender.send(message).block(TIMEOUT);

        final ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next()
            .block(TIMEOUT);

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.deadLetter(receivedMessage))
            .verifyComplete();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @Test
    void receiveAndRenewLock() {
        // Arrange
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, "id-1", 0);

        final AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final AtomicReference<Instant> initialLock = new AtomicReference<>();
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        // Blocking here because it is not part of the scenario we want to test.
        sender.send(message).block(TIMEOUT);
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
            receiver.complete(receivedMessage.get());
        }
    }

    /**
     * Verifies that the lock can be automatically renewed.
     */
    @Test
    void autoRenewLockOnReceiveMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getServiceBusMessage(CONTENTS, messageId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false)
            .setMaxAutoRenewDuration(Duration.ofSeconds(120));

        // Send the message to verify.
        sender.send(message).block(TIMEOUT);

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
                }
            })
            .thenCancel()
            .verify();
    }

    @Test
    void receiveAndAbandon() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageTrackingId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        sender.send(message).block(TIMEOUT);

        final ServiceBusReceivedMessage receivedMessage = receiver.receive(options).next()
            .block(TIMEOUT);

        assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiver.abandon(receivedMessage))
            .verifyComplete();
    }

    static Stream<Arguments> receiveBySequenceNumber() {
        return Stream.of(
            Arguments.of(DispositionStatus.ABANDONED),
            Arguments.of(DispositionStatus.SUSPENDED),
            Arguments.of(DispositionStatus.COMPLETED)
        );
    }

    /**
     * Test we can receive a deferred message via sequence number and then perform abandon, suspend, or complete on it.
     */
    @MethodSource
    @ParameterizedTest
    void receiveBySequenceNumber(DispositionStatus dispositionStatus) {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageTrackingId, 0);
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiver.receive(options).next())
            .block(TIMEOUT);

        assertNotNull(receivedMessage);

        receiver.defer(receivedMessage).block(TIMEOUT);

        final ServiceBusReceivedMessage receivedDeferredMessage = receiver
            .receiveDeferredMessage(receivedMessage.getSequenceNumber()).block(TIMEOUT);

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
    }

    @Test
    void sendReceiveMessageWithVariousPropertyTypes() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = TestUtils.getServiceBusMessage(CONTENTS, messageTrackingId, 0);
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

        sender.send(messageToSend).block(TIMEOUT);

        // Assert & Act
        StepVerifier.create(receiveDeleteModeReceiver.receive(options))
            .assertNext(receivedMessage -> {
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
}
