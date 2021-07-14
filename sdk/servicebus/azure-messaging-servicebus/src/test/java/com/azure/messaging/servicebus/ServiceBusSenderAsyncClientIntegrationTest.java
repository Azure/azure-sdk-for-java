// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ServiceBusSenderAsyncClient} from queues or subscriptions.
 */
@Tag("integration")
class ServiceBusSenderAsyncClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusReceiverAsyncClient receiver;
    private final AtomicInteger messagesPending = new AtomicInteger();

    ServiceBusSenderAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusSenderAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sessionId = UUID.randomUUID().toString();
    }

    @Override
    protected void afterTest() {
        dispose(sender);

        final int numberOfMessages = messagesPending.get();
        if (numberOfMessages < 1) {
            dispose(receiver);
            return;
        }

        try {
            receiver.receiveMessages()
                .take(numberOfMessages)
                .map(message -> {
                    logger.info("Message received: {}", message.getSequenceNumber());
                    return message;
                })
                .timeout(Duration.ofSeconds(5), Mono.empty())
                .blockLast();
        } catch (Exception e) {
            logger.warning("Error occurred when draining queue.", e);
        } finally {
            dispose(receiver);
        }
    }

    static Stream<Arguments> transactionMessageSendAndCompleteTransaction() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE, true),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, true),
            Arguments.of(MessagingEntityType.QUEUE, false),
            Arguments.of(MessagingEntityType.SUBSCRIPTION, false)
        );
    }

    /**
     * Verifies that we can send a message to a non-session queue.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void nonSessionQueueSendMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        // Assert & Act
        StepVerifier.create(sender.sendMessage(message).doOnSuccess(aVoid -> messagesPending.incrementAndGet()))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a list of messages to a non-session entity.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void nonSessionEntitySendMessageList(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT, false);
        int count = 4;

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString(), CONTENTS_BYTES);

        // Assert & Act
        StepVerifier.create(sender.sendMessages(messages).doOnSuccess(aVoid -> {
            messages.forEach(serviceBusMessage -> messagesPending.incrementAndGet());
        }))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a {@link ServiceBusMessageBatch} to a non-session queue.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void nonSessionMessageBatch(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT, false);

        final String messageId = UUID.randomUUID().toString();
        final CreateMessageBatchOptions options = new CreateMessageBatchOptions().setMaximumSizeInBytes(1024);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(3, messageId, CONTENTS_BYTES);

        // Assert & Act
        StepVerifier.create(sender.createMessageBatch(options)
            .flatMap(batch -> {
                for (ServiceBusMessage message : messages) {
                    Assertions.assertTrue(batch.tryAddMessage(message));
                }

                return sender.sendMessages(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .verifyComplete();
    }

    /**
     * Verifies that we can send message to final destination using via-queue.
     */
    @Disabled("The send via functionality is removing for first GA release, later we will come back to it.")
    @Test
    void viaQueueMessageSendTest() {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_TXN_1;
        final int destinationEntity = TestUtils.USE_CASE_TXN_2;
        final boolean shareConnection = true;
        final MessagingEntityType entityType = MessagingEntityType.QUEUE;
        final boolean isSessionEnabled = false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 1;
        final int totalToDestination = 2;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);
        final String viaQueueName = getQueueName(viaIntermediateEntity);

        setSenderAndReceiver(entityType, viaIntermediateEntity, useCredentials);

        final ServiceBusSenderAsyncClient destination1ViaSender = getSenderBuilder(useCredentials, entityType,
            destinationEntity, false, shareConnection)
            //.viaQueueName(viaQueueName)
            .buildAsyncClient();
        final ServiceBusReceiverAsyncClient destination1Receiver = getReceiverBuilder(useCredentials, entityType,
            destinationEntity, shareConnection)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .disableAutoComplete()
            .buildAsyncClient();

        final AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // Act
        try {
            StepVerifier.create(destination1ViaSender.createTransaction())
                .assertNext(transactionContext -> {
                    transaction.set(transactionContext);
                    assertNotNull(transaction);
                })
                .verifyComplete();
            assertNotNull(transaction.get());

            StepVerifier.create(sender.sendMessages(messages, transaction.get()))
                .verifyComplete();
            StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
                .verifyComplete();
            StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
                .verifyComplete();

            StepVerifier.create(destination1ViaSender.commitTransaction(transaction.get())
                .delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();

            // Assert
            // Verify message is received by final destination Entity
            StepVerifier.create(destination1Receiver.receiveMessages().take(totalToDestination).timeout(shortTimeout))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .verifyComplete();

            // Verify, intermediate-via queue has it delivered to intermediate Entity.
            StepVerifier.create(receiver.receiveMessages().take(total).timeout(shortTimeout))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .verifyComplete();
        } finally {
            destination1Receiver.close();
            destination1ViaSender.close();
        }
    }


    /**
     * Verifies that we can send message to final destination using via-topic.
     */
    @Disabled("The send via functionality is removed for first GA release, later we will come back to it.")
    @Test
    void viaTopicMessageSendTest() {
        // Arrange
        final boolean useCredentials = false;
        final Duration shortTimeout = Duration.ofSeconds(15);
        final int viaIntermediateEntity = TestUtils.USE_CASE_SEND_VIA_TOPIC_1;
        final int destinationEntity = TestUtils.USE_CASE_SEND_VIA_TOPIC_2;
        final boolean shareConnection = true;
        final MessagingEntityType entityType = MessagingEntityType.SUBSCRIPTION;
        final boolean isSessionEnabled =  false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 1;
        final int totalToDestination = 2;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);
        final String viaTopicName = getTopicName(viaIntermediateEntity);

        setSenderAndReceiver(entityType, viaIntermediateEntity, useCredentials);
        final ServiceBusReceiverAsyncClient intermediateReceiver =  receiver;
        final ServiceBusSenderAsyncClient intermediateSender = sender;

        final ServiceBusSenderAsyncClient destination1ViaSender = getSenderBuilder(useCredentials, entityType,
            destinationEntity, false, shareConnection)
            //.viaTopicName(viaTopicName)
            .buildAsyncClient();

        final ServiceBusReceiverAsyncClient destination1Receiver = getReceiverBuilder(useCredentials, entityType,
            destinationEntity, shareConnection)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .disableAutoComplete()
            .buildAsyncClient();

        final AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();

        // Act
        StepVerifier.create(destination1ViaSender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        StepVerifier.create(intermediateSender.sendMessages(messages, transaction.get()))
            .verifyComplete();
        StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
            .verifyComplete();
        StepVerifier.create(destination1ViaSender.sendMessages(messages, transaction.get()))
            .verifyComplete();

        StepVerifier.create(destination1ViaSender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
            .verifyComplete();

        // Assert
        // Verify message is received by final destination Entity
        StepVerifier.create(destination1Receiver.receiveMessages().take(totalToDestination).timeout(shortTimeout))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();

        // Verify, intermediate-via topic has it delivered to intermediate Entity.
        StepVerifier.create(intermediateReceiver.receiveMessages().take(total).timeout(shortTimeout))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can do following
     * 1. create transaction
     * 2. send message  with transactionContext
     * 3. Rollback/commit this transaction.
     */
    @MethodSource
    @ParameterizedTest
    void transactionMessageSendAndCompleteTransaction(MessagingEntityType entityType, boolean isCommit) {
        // Arrange
        Duration shortTimeout = Duration.ofSeconds(15);
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SEND_READ_BACK_MESSAGES, false);
        final boolean isSessionEnabled = false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 3;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        // Assert & Act
        StepVerifier.create(sender.sendMessages(messages, transaction.get()))
            .verifyComplete();
        if (isCommit) {
            StepVerifier.create(sender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
            StepVerifier.create(receiver.receiveMessages().take(total))
                .assertNext(receivedMessage -> {
                    System.out.println("1");
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    System.out.println("2");
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    System.out.println("3");
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .expectComplete()
                .verify(shortTimeout);
        } else {
            StepVerifier.create(sender.rollbackTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
            StepVerifier.create(receiver.receiveMessages().take(total))
                .verifyTimeout(shortTimeout);
        }
    }

    /**
     * Verifies that we can send using credentials.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void sendWithCredentials(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, true);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(5, messageId, CONTENTS_BYTES);

        // Act & Assert
        StepVerifier.create(sender.createMessageBatch()
            .flatMap(batch -> {
                messages.forEach(m -> Assertions.assertTrue(batch.tryAddMessage(m)));

                return sender.sendMessages(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .expectComplete()
            .verify();
    }

    /**
     * Verifies that we can create transaction, scheduleMessage and commit.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void transactionScheduleAndCommitTest(MessagingEntityType entityType) {

        // Arrange
        boolean isSessionEnabled = false;
        setSenderAndReceiver(entityType, 0, isSessionEnabled);
        final Duration scheduleDuration = Duration.ofSeconds(3);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = getMessage(messageId, isSessionEnabled);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();
        StepVerifier.create(sender.scheduleMessage(message, OffsetDateTime.now().plusSeconds(5), transaction.get()))
            .assertNext(sequenceNumber -> {
                assertNotNull(sequenceNumber);
                assertTrue(sequenceNumber.intValue() > 0);
            })
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .verifyComplete();
        StepVerifier.create(Mono.delay(scheduleDuration).then(receiver.receiveMessages().next()))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can create transaction, scheduleMessages and commit.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void transactionScheduleMessagesTest(MessagingEntityType entityType) {

        // Arrange
        final boolean isSessionEnabled = false;
        final int total = 2;
        final Duration shortWait = Duration.ofSeconds(3);

        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SCHEDULE_MESSAGES, isSessionEnabled);

        final Duration scheduleDuration = Duration.ofSeconds(5);
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < total; ++i) {
            messages.add(getMessage(messageId, isSessionEnabled));
        }

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(transactionContext -> {
                transaction.set(transactionContext);
                assertNotNull(transaction);
            })
            .verifyComplete();

        StepVerifier.create(sender.scheduleMessages(messages, OffsetDateTime.now().plus(scheduleDuration), transaction.get()).collectList())
            .assertNext(longs -> {
                assertEquals(total, longs.size());
            })
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .verifyComplete();

        StepVerifier.create(Mono.delay(scheduleDuration).thenMany(receiver.receiveMessages().take(total)))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .thenAwait(shortWait)
            .thenCancel()
            .verify();
    }

    /**
     * Verifies that we can schedule messages and cancel them.
     */
    @MethodSource("com.azure.messaging.servicebus.IntegrationTestBase#messagingEntityProvider")
    @ParameterizedTest
    void cancelScheduledMessagesTest(MessagingEntityType entityType) {

        // Arrange
        final boolean isSessionEnabled = false;
        final Duration shortWaitTime = Duration.ofSeconds(5);
        final int total = 2;
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_CANCEL_MESSAGES, isSessionEnabled);
        final Duration scheduleDuration = Duration.ofSeconds(15);
        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = new ArrayList<>();
        for (int i = 0; i < total; ++i) {
            messages.add(getMessage(messageId, isSessionEnabled));
        }
        List<Long> seqNumbers = sender.scheduleMessages(messages, OffsetDateTime.now().plus(scheduleDuration)).collectList().block(shortWaitTime);

        // Assert & Act
        Assertions.assertNotNull(seqNumbers);
        Assertions.assertEquals(total, seqNumbers.size());

        StepVerifier.create(sender.cancelScheduledMessages(seqNumbers))
            .verifyComplete();

        // The messages should have been cancelled and we should not find any messages.
        StepVerifier.create(receiver.receiveMessages().take(total))
            .verifyTimeout(shortWaitTime);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean useCredentials) {
        final boolean isSessionAware = false;
        final boolean sharedConnection = true;

        this.sender = getSenderBuilder(useCredentials, entityType, entityIndex, isSessionAware, sharedConnection)
            .buildAsyncClient();
        this.receiver = getReceiverBuilder(useCredentials, entityType, entityIndex, sharedConnection)
            .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
            .disableAutoComplete()
            .buildAsyncClient();
    }
}
