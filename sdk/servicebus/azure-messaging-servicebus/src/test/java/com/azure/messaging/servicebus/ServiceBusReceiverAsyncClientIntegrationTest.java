// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMessageOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.Disposable;
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
    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusReceiverAsyncClient receiverManual;
    private ServiceBusSenderAsyncClient sender;

    private ReceiveMessageOptions receiveMessageOptions;
    private ReceiveMessageOptions receiveMessageOptionsManual;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
        receiveMessageOptions = new ReceiveMessageOptions().setAutoComplete(true);
        receiveMessageOptionsManual = new ReceiveMessageOptions().setAutoComplete(false);
    }

    @Override
    protected void beforeTest() {
        sender = createBuilder().buildAsyncSenderClient();
        receiver = createBuilder()
            .receiveMessageOptions(receiveMessageOptions)
            .buildAsyncReceiverClient();

        receiverManual = createBuilder()
            .receiveMessageOptions(receiveMessageOptionsManual)
            .buildAsyncReceiverClient();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, receiverManual, sender);
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @Test
    void receiveMessageAutoComplete() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "hello-3";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(sender.send(message))
            .thenMany(receiverManual.receive().take(2)))
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, new String(receivedMessage.getBody()));
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
            })
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, new String(receivedMessage.getBody()));
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

    }

    /**
     * Verifies that we can send and peek a message.
     */
    @Test
    void peekMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(receiver.peek()))
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, new String(receivedMessage.getBody()));
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
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(receiver.peek(fromSequenceNumber)))
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, new String(receivedMessage.getBody()));
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
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);
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
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);
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
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);

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
        Duration renewAfterSeconds = Duration.ofSeconds(1);
        long takeTimeToProcessMessageMillis = 10000;
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, "id-1", 0);

        AtomicReference<Integer> renewMessageLockCounter = new AtomicReference<>(0);
        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiverManual.receive()
            .take(1)
            .delayElements(renewAfterSeconds)
            .map(receivedMessage -> {
                //  keep renewing the lock whole you process the message.
                Disposable disposable = receiverManual.renewMessageLock(receivedMessage)
                    .repeat()
                    .delayElements(renewAfterSeconds)
                    .doOnNext(instant -> {
                        // This will ensure that we are getting valid refresh time
                        if (instant != null) {
                            logger.info(" Received new refresh time " + instant);
                            renewMessageLockCounter.set(renewMessageLockCounter.get() + 1);
                        }
                    })
                    .subscribe();


                // This just shows that user is taking time to process.
                // Real production code will not have sleep in it will have message processing code instead.
                try {
                    Thread.sleep(takeTimeToProcessMessageMillis);
                } catch (InterruptedException ignored) {

                }
                disposable.dispose();
                return receivedMessage;
            })))
            .assertNext(receivedMessage -> {
                Assertions.assertNotNull(receivedMessage.getLockedUntil());
            })
            .verifyComplete();
        // ensure that renew lock is called atleast once.
        Assertions.assertTrue(renewMessageLockCounter.get() > 0);
    }

    @Test
    void autoRenewLockOnReceiveMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getServiceBusMessage("contents", messageId, 0);

        // Send the message to verify.
        sender.send(message).block(TIMEOUT);

        final ReceiveMessageOptions options = new ReceiveMessageOptions()
            .setReceiveMode(ReceiveMode.PEEK_LOCK)
            .setIsLockAutoRenewed(true)
            .setMaxAutoRenewDuration(Duration.ofSeconds(2));
        final ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(getConnectionString())
            .receiveMessageOptions(options)
            .buildAsyncReceiverClient();

        // Act & Assert
        StepVerifier.create(receiver.receive())
            .assertNext(received -> {
                Assertions.assertNotNull(received.getLockedUntil());
                Assertions.assertNotNull(received.getLockToken());

                logger.info("{}: lockId[{}]. lockedUntil[{}]",
                    received.getSequenceNumber(), received.getLockToken(), received.getLockedUntil());

                Instant current = received.getLockedUntil();

                // Simulate some sort of long processing. Verify that the lock token is updated every time.
                for (int i = 0; i < 3; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException ignored) {
                    }

                    Instant latest = received.getLockedUntil();
                    Assertions.assertNotNull(latest);
                    Assertions.assertTrue(latest.isAfter(current),
                        String.format("[%s] latestTime: '%s' is not after the current one: '%s'", i, latest, current));

                    current = latest;

                }
            })
            .thenCancel()
            .verify();
    }
}
