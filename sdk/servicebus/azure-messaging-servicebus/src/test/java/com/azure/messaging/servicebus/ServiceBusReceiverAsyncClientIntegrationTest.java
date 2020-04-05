// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;

class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String CONTENTS = "Test-contents";
    private final ClientLogger logger = new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class);

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient receiverManualComplete;
    private ServiceBusReceiverAsyncClient receiveDeleteModeReceiver;
    private ServiceBusSenderAsyncClient sender;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final String queueName = "hemant-test2"; //getQueueName();
        Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

        sender = createBuilder().sender().queueName(queueName).buildAsyncClient();
        receiver = createBuilder()
            .receiver()
            .queueName(queueName)
            .isAutoComplete(true)
            .buildAsyncClient();

        receiverManualComplete = createBuilder()
            .receiver()
            .queueName(queueName)
            .isAutoComplete(false)
            .buildAsyncClient();

        receiveDeleteModeReceiver = createBuilder()
            .receiver()
            .queueName(queueName)
            .isAutoComplete(false)
            .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
            .buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, receiverManualComplete, receiveDeleteModeReceiver, sender);
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

        // Assert & Act
        StepVerifier.create(sender.send(message).then(sender.send(message))
            .thenMany(receiverManualComplete.receive()))
            .assertNext(receivedMessage ->
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .assertNext(receivedMessage ->
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
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

        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiverManualComplete.receive()))
            .assertNext(receivedMessage ->
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
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
            .assertNext(receivedMessage -> {
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and peek a message.
     */
    @Test
    void testSendSceduledMessageAndReceive() {
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
                Assertions.assertArrayEquals(contents.getBytes(), receivedMessage.getBody());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can schedule and receive multiple messages.
     */
    @Test
    void testSendMultipleSceduledMessageAndReceive() {
        // Arrange
        final String messageId1 = UUID.randomUUID().toString();
        final String messageId2 = UUID.randomUUID().toString();
        String contents = "Some-contents";
        final ServiceBusMessage message1 = TestUtils.getServiceBusMessage(contents, messageId1, 0);
        final ServiceBusMessage message2 = TestUtils.getServiceBusMessage(contents, messageId2, 0);
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(1);

        sender.scheduleMessage(message1, scheduledEnqueueTime)
            .block();
        sender.scheduleMessage(message2, scheduledEnqueueTime)
            .block(Duration.ofSeconds(4));

        // Assert & Act
        String finalContents = contents;
        StepVerifier.create(receiveDeleteModeReceiver.receive().take(2))
            .assertNext(receivedMessage -> {
                Assertions.assertArrayEquals(finalContents.getBytes(), receivedMessage.getBody());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId1, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
             .assertNext(receivedMessage -> {
                Assertions.assertArrayEquals(finalContents.getBytes(), receivedMessage.getBody());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId2, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can cancel a scheduled message.
     */
    @Test
    void testSendSceduledMessageAndCancel() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);
        final Instant scheduledEnqueueTime = Instant.now().plusSeconds(10);
        final Duration delayDuration = Duration.ofSeconds(3);

        final Long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime).block();
        logger.verbose("Scheduled the message, sequence number {}.", sequenceNumber);

        Mono.delay(delayDuration)
            .then(sender.cancelScheduledMessage(sequenceNumber.longValue()))
            .block();
        logger.verbose("Cancelled the scheduled message, sequence number {}.", sequenceNumber);

        // Assert & Act
        StepVerifier.create(receiver.receive().take(1))
            .expectNoEvent(Duration.ofSeconds(5))
            .verifyComplete();
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
            .assertNext(receivedMessage -> {
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
            })
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

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiverManualComplete.deadLetter(receivedMessage))
            .verifyComplete();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @Test
    void testBasicReceiveAndRenewLock() {
        // Arrange
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, "id-1", 0);

        final AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final AtomicReference<Instant> initialLock = new AtomicReference<>();

        // Blocking here because it is not part of the scenario we want to test.
        sender.send(message).block(Duration.ofSeconds(20));

        // Assert & Act
        StepVerifier.create(
            receiverManualComplete.receive().take(1).map(m -> {
                Assertions.assertNotNull(m.getLockedUntil());
                receivedMessage.set(m);
                initialLock.set(m.getLockedUntil());
                return m;
            }).then(Mono.delay(Duration.ofSeconds(10))
                .then(Mono.defer(() -> receiverManualComplete.renewMessageLock(receivedMessage.get())))))
            .assertNext(lockedUntil -> {
                Assertions.assertTrue(lockedUntil.isAfter(initialLock.get()),
                    String.format("Updated lock is not after the initial Lock. updated: [%s]. initial:[%s]",
                        lockedUntil, initialLock.get()));

                Assertions.assertEquals(receivedMessage.get().getLockedUntil(), lockedUntil);
            })
            .verifyComplete();
    }

    /**
     * Verifies that the lock can be automatically renewed.
     */
    @Test
    void autoRenewLockOnReceiveMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getServiceBusMessage(CONTENTS, messageId, 0);

        // Send the message to verify.
        sender.send(message).block(TIMEOUT);

        final ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .receiver()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .isLockAutoRenewed(true)
            .queueName(getQueueName())
            .maxAutoLockRenewalDuration(Duration.ofSeconds(2))
            .buildAsyncClient();

        try {
            // Act & Assert
            StepVerifier.create(receiver.receive())
                .assertNext(received -> {
                    Assertions.assertNotNull(received.getLockedUntil());
                    Assertions.assertNotNull(received.getLockToken());

                    logger.info("{}: lockId[{}]. lockedUntil[{}]",
                        received.getSequenceNumber(), received.getLockToken(), received.getLockedUntil());

                    final Instant initial = received.getLockedUntil();
                    Instant latest = Instant.MIN;

                    // Simulate some sort of long processing.
                    for (int i = 0; i < 3; i++) {
                        try {
                            TimeUnit.SECONDS.sleep(15);
                        } catch (InterruptedException error) {
                            logger.error("Error occurred while sleeping: " + error);
                        }

                        Assertions.assertNotNull(received.getLockedUntil());
                        latest = received.getLockedUntil();
                    }

                    Assertions.assertTrue(initial.isBefore(latest),
                        String.format("Latest should be after initial. initial: %s. latest: %s", initial, latest));

                    logger.info("Completing message.");
                    receiver.complete(received).block(Duration.ofSeconds(15));
                })
                .thenCancel()
                .verify();
        } finally {
            receiver.close();
        }
    }

    @Test
    void testBasicReceiveAndDeleteWithBinaryData() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64);

        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiveDeleteModeReceiver.receive()))
            .assertNext(receivedMessage ->
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .expectNoEvent(Duration.ofSeconds(2))
            .thenCancel()
            .verify();
    }

    @Test
    void testBasicReceiveAndCompleteWithLargeBinaryData() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiveDeleteModeReceiver.receive()))
            .assertNext(receivedMessage ->
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID)))
            .expectNoEvent(Duration.ofSeconds(2))
            .thenCancel()
            .verify();
    }

    @Test
    void testBasicReceiveAndComplete() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiverManualComplete.complete(receivedMessage))
            .verifyComplete();
    }

    @Test
    void testBasicReceiveAndCompleteMessageWithProperties() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);
        Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));

        // Assert & Act
        StepVerifier.create(receiverManualComplete.complete(receivedMessage))
            .verifyComplete();
    }

    @Test
    void testBasicReceiveAndAbandon() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiverManualComplete.abandon(receivedMessage))
            .verifyComplete();
    }

    @Test
    void testBasicReceiveAndDeadLetter() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiverManualComplete.deadLetter(receivedMessage))
            .verifyComplete();
    }


    @Test
    void testReceiveBySequenceNumberAndDeadletter() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        receiverManualComplete.defer(receivedMessage).block(Duration.ofSeconds(30));

        final ServiceBusReceivedMessage receivedDeferredMessage =  receiverManualComplete
            .receiveDeferredMessage(receivedMessage.getSequenceNumber()).block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedDeferredMessage);
        Assertions.assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        receiverManualComplete.deadLetter(receivedDeferredMessage).block(Duration.ofSeconds(30));

        // Assert & Act
        StepVerifier.create(receiverManualComplete.receiveDeferredMessage(receivedMessage.getSequenceNumber()))
            .expectNextCount(0)
            .thenCancel()
            .verify(Duration.ofSeconds(2));
    }

    @Test
    void testReceiveBySequenceNumberAndAbandon() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        receiverManualComplete.defer(receivedMessage).block(Duration.ofSeconds(30));

        final ServiceBusReceivedMessage receivedDeferredMessage =  receiverManualComplete
            .receiveDeferredMessage(receivedMessage.getSequenceNumber()).block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedDeferredMessage);
        Assertions.assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        receiverManualComplete.abandon(receivedDeferredMessage).block(Duration.ofSeconds(30));

        // Assert & Act
        StepVerifier.create(receiverManualComplete.receiveDeferredMessage(receivedMessage.getSequenceNumber()))
            .expectNextCount(0)
            .thenCancel()
            .verify(Duration.ofSeconds(2));
    }

    @Test
    void testSendReceiveMessageWithVariousPropertyTypes() {
        // Arrange 
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage messageToSend = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);
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

        sender.send(messageToSend).block(Duration.ofSeconds(30));

        // Assert & Act
        StepVerifier.create(receiveDeleteModeReceiver.receive())
            .assertNext(receivedMessage -> {
                Map<String, Object> receivedProperties = receivedMessage.getProperties();
                for (Map.Entry<String, Object> sentEntry : sentProperties.entrySet()) {
                    if (sentEntry.getValue() != null && sentEntry.getValue().getClass().isArray()) {
                        Assertions.assertArrayEquals((Object[]) sentEntry.getValue(), (Object[]) receivedProperties.get(sentEntry.getKey()));
                    } else {
                        Assertions.assertEquals(sentEntry.getValue(), receivedProperties.get(sentEntry.getKey()));
                    }
                }
            })
            .expectNoEvent(Duration.ofSeconds(2))
            .thenCancel()
            .verify();
    }

    @Test
    void testReceiveBySequenceNumberAndComplete() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedMessage = sender.send(message)
            .then(receiverManualComplete.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        receiverManualComplete.defer(receivedMessage).block(Duration.ofSeconds(30));

        final ServiceBusReceivedMessage receivedDeferredMessage =  receiverManualComplete
            .receiveDeferredMessage(receivedMessage.getSequenceNumber()).block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedDeferredMessage);
        Assertions.assertEquals(receivedMessage.getSequenceNumber(), receivedDeferredMessage.getSequenceNumber());

        receiverManualComplete.complete(receivedDeferredMessage).block(Duration.ofSeconds(30));

        // Assert & Act
        StepVerifier.create(receiverManualComplete.receiveDeferredMessage(receivedMessage.getSequenceNumber()))
            .expectNextCount(0)
            .thenCancel()
            .verify(Duration.ofSeconds(2));
    }

    @Test
    void testReceiveDeferedDELETEME() {
        // Arrange
        final String messageTrackingId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessageBinary(messageTrackingId, 0, 64 * 1024);

        final ServiceBusReceivedMessage receivedDeferredMessage =  receiverManualComplete
            .receiveDeferredMessage(20).block(Duration.ofSeconds(30));

        receiverManualComplete.deadLetter(receivedDeferredMessage).block(Duration.ofSeconds(30));
        System.out.println("moved to dead lettter  seq= " + receivedDeferredMessage.getSequenceNumber());
    }
}
