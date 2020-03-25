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
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_TRACKING_ID;
import static com.azure.messaging.servicebus.TestUtils.getServiceBusMessage;

class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private static final String CONTENTS = "Test-contents";

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient receiverManual;
    private ServiceBusSenderAsyncClient sender;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final String queueName = getQueueName();
        Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

        sender = createBuilder().buildSenderClientBuilder().entityName(queueName).buildAsyncClient();
        receiver = createBuilder()
            .buildReceiverClientBuilder()
            .queueName(queueName)
            .isAutoComplete(true)
            .buildAsyncClient();

        receiverManual = createBuilder()
            .buildReceiverClientBuilder()
            .queueName(queueName)
            .isAutoComplete(false)
            .buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, receiverManual, sender);
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
            .thenMany(receiverManual.receive()))
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
        StepVerifier.create(sender.send(message).thenMany(receiverManual.receive()))
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
     * Verifies that we can send and peek a message.
     */
    @Test
    void peekFromSequenceNumberMessage() {
        // Arrange
        final long fromSequenceNumber = 1;
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(receiver.peek(fromSequenceNumber)))
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
            .thenMany(receiver.peekBatch(maxMessages, fromSequenceNumber)))
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
            .then(receiverManual.receive().next())
            .block(Duration.ofSeconds(30));

        Assertions.assertNotNull(receivedMessage);

        // Assert & Act
        StepVerifier.create(receiverManual.deadLetter(receivedMessage))
            .verifyComplete();
    }

    /**
     * Verifies that we can renew message lock.
     */
    @Test
    void renewMessageLock() {
        // Arrange
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS, "id-1", 0);

        final AtomicReference<ServiceBusReceivedMessage> receivedMessage = new AtomicReference<>();
        final AtomicReference<Instant> initialLock = new AtomicReference<>();

        // Blocking here because it is not part of the scenario we want to test.
        sender.send(message).block(Duration.ofSeconds(20));

        // Assert & Act
        StepVerifier.create(
            receiverManual.receive().take(1).map(m -> {
                Assertions.assertNotNull(m.getLockedUntil());
                receivedMessage.set(m);
                initialLock.set(m.getLockedUntil());
                return m;
            }).then(Mono.delay(Duration.ofSeconds(10))
                .then(Mono.defer(() -> receiverManual.renewMessageLock(receivedMessage.get())))))
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
            .buildReceiverClientBuilder()
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
}
