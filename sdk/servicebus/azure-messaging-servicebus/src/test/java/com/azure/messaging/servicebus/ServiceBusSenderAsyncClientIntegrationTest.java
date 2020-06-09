// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

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
            return;
        }

        try {
            receiver.receive()
                .take(numberOfMessages)
                .map(message -> {
                    logger.info("Message received: {}", message.getMessage().getSequenceNumber());
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

    static Stream<Arguments> receiverTypesProvider() {
        return Stream.of(
            Arguments.of(MessagingEntityType.QUEUE),
            Arguments.of(MessagingEntityType.SUBSCRIPTION)
        );
    }

    /**
     * Verifies that we can send a message to a non-session queue.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionQueueSendMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        // Assert & Act
        StepVerifier.create(sender.send(message).doOnSuccess(aVoid -> messagesPending.incrementAndGet()))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a list of messages to a non-session entity.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionEntitySendMessageList(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);
        int count = 4;

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString(), CONTENTS_BYTES);

        // Assert & Act
        StepVerifier.create(sender.send(messages).doOnSuccess(aVoid -> {
            messages.forEach(serviceBusMessage -> messagesPending.incrementAndGet());
        }))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a {@link ServiceBusMessageBatch} to a non-session queue.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionMessageBatch(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, false);

        final String messageId = UUID.randomUUID().toString();
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(1024);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(3, messageId, CONTENTS_BYTES);

        // Assert & Act
        StepVerifier.create(sender.createBatch(options)
            .flatMap(batch -> {
                for (ServiceBusMessage message : messages) {
                    Assertions.assertTrue(batch.tryAdd(message));
                }

                return sender.send(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .verifyComplete();
    }

    /**
     * Verifies that we can do following
     * 1. create transaction
     * 2. send message  with transactionContext
     * 3. Rollback/commit this transaction.
     */
    @MethodSource("messagingEntityProviderWithTransaction")
    @ParameterizedTest
    void transactionMessageSendAndCompleteTransaction(MessagingEntityType entityType, boolean isCommit) {
        // Arrange
        Duration shortTimeout = Duration.ofSeconds(15);
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_SEND_READ_BACK_MESSAGES, false);
        final boolean isSessionEnabled =  false;
        final String messageId = UUID.randomUUID().toString();
        final int total = 3;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(total, messageId, CONTENTS_BYTES);

        // Assert & Act
        AtomicReference<ServiceBusTransactionContext> transaction = new AtomicReference<>();
        StepVerifier.create(sender.createTransaction())
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();
        assertNotNull(transaction.get());

        // Assert & Act
        StepVerifier.create(sender.send(messages, transaction.get()))
            .verifyComplete();
        if (isCommit) {
            StepVerifier.create(sender.commitTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
            StepVerifier.create(receiver.receive().take(total).timeout(shortTimeout))
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .assertNext(receivedMessage -> {
                    assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                    messagesPending.decrementAndGet();
                })
                .verifyComplete();
        } else {
            StepVerifier.create(sender.rollbackTransaction(transaction.get()).delaySubscription(Duration.ofSeconds(1)))
                .verifyComplete();
            StepVerifier.create(receiver.receive().take(total))
                .verifyTimeout(shortTimeout);
        }
    }

    /**
     * Verifies that we can send using credentials.
     */
    @MethodSource("messagingEntityProvider")
    @ParameterizedTest
    void sendWithCredentials(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, 0, true);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(5, messageId, CONTENTS_BYTES);

        // Act & Assert
        StepVerifier.create(sender.createBatch()
            .flatMap(batch -> {
                messages.forEach(m -> Assertions.assertTrue(batch.tryAdd(m)));

                return sender.send(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .expectComplete()
            .verify();
    }

    /**
     * Verifies that we can create transaction, scheduleMessage and commit.
     */
    @MethodSource("messagingEntityProvider")
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
            .assertNext(txn -> {
                transaction.set(txn);
                assertNotNull(transaction);
            })
            .verifyComplete();
        StepVerifier.create(sender.scheduleMessage(message, Instant.now().plusSeconds(5), transaction.get()))
            .assertNext(sequenceNumber -> {
                assertNotNull(sequenceNumber);
                assertTrue(sequenceNumber.intValue() > 0);
            })
            .verifyComplete();

        StepVerifier.create(sender.commitTransaction(transaction.get()))
            .verifyComplete();
        StepVerifier.create(Mono.delay(scheduleDuration).then(receiver.receive().next()))
            .assertNext(receivedMessage -> {
                assertMessageEquals(receivedMessage, messageId, isSessionEnabled);
                messagesPending.decrementAndGet();
            })
            .verifyComplete();
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean useCredentials) {
        setSenderAndReceiver(entityType, entityIndex, useCredentials, false, false);
    }

    /**
     * Sets the sender and receiver. If session is enabled, then a single-named session receiver is created with
     * shared connection as needed.
     */
    private void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex, boolean useCredentials, boolean isSessionEnabled,
                                      boolean shareConnection) {
        this.sender = getSenderBuilder(useCredentials, entityType, entityIndex, isSessionEnabled, shareConnection).buildAsyncClient();

        if (isSessionEnabled) {
            assertNotNull(sessionId, "'sessionId' should have been set.");
            this.receiver = getSessionReceiverBuilder(useCredentials, entityType, entityIndex, Function.identity(), shareConnection)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .sessionId(sessionId)
                .buildAsyncClient();
        } else {
            this.receiver = getReceiverBuilder(useCredentials, entityType, entityIndex, Function.identity(), shareConnection)
                .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                .buildAsyncClient();
        }
    }
}
