// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Integration tests for {@link ServiceBusSenderAsyncClient} from queues or subscriptions.
 */
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
            receiver.receive(new ReceiveAsyncOptions().setIsAutoCompleteEnabled(false))
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
     * Verifies that we can send a session message to a non-sessionful entity.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void sendSessionMessageToNonSessionEntity(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, false, false);

        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId);
        message.setSessionId(sessionId);

        // Assert & Act
        StepVerifier.create(sender.send(message).doOnSuccess(aVoid -> messagesPending.incrementAndGet()))
            .verifyComplete();
    }

    /**
     * Verifies that we can not send a non session message to a session enabled queue.
     */
    @Test
    void sendNonSessionMessageToSessionQueue() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.QUEUE, false, true);

        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId);

        // Assert & Act
        StepVerifier.create(sender.send(message))
            .verifyErrorMatches(error -> error instanceof UnsupportedOperationException);
    }

    /**
     * Verifies that we can send a non session message to a topic and a session enabled receiver do not
     * receive the message.
     */
    @Test
    void sendNonSessionMessageToTopic() {
        // Arrange
        setSenderAndReceiver(MessagingEntityType.SUBSCRIPTION, false, true);

        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId);
        final Duration shortDuration = Duration.ofSeconds(5);

        // Assert & Act
        StepVerifier.create(sender.send(message).doOnSuccess(aVoid -> messagesPending.incrementAndGet()))
            .verifyComplete();

        // Make sure no message is received by a session enabled receiver
        StepVerifier.create(receiver.receive(new ReceiveAsyncOptions().setIsAutoCompleteEnabled(false))
            .take(1).timeout(shortDuration))
            .expectTimeout(shortDuration)
            .verify();

        messagesPending.decrementAndGet();
    }

    /**
     * Verifies that we can send a message to a non-session queue.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionQueueSendMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, false, false);

        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId);

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
        setSenderAndReceiver(entityType, false, false);
        int count = 4;

        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString());

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
        setSenderAndReceiver(entityType, false, false);

        final String messageId = UUID.randomUUID().toString();
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(1024);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(3, messageId);

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
     * Verifies that we can send using credentials.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void sendWithCredentials(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, true, false);

        final String messageId = UUID.randomUUID().toString();
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(5, messageId);

        // Act & Assert
        StepVerifier.create(sender.createBatch()
            .flatMap(batch -> {
                messages.forEach(m -> Assertions.assertTrue(batch.tryAdd(m)));

                return sender.send(batch).doOnSuccess(aVoid -> messagesPending.incrementAndGet());
            }))
            .expectComplete()
            .verify();
    }

    void setSenderAndReceiver(MessagingEntityType entityType, boolean useCredentials, boolean isSessionEnabled) {
        switch (entityType) {
            case QUEUE:
                final String queueName = isSessionEnabled ? getSessionQueueName() : getQueueName();

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = getBuilder(useCredentials).sender()
                    .queueName(queueName)
                    .buildAsyncClient();
                if (isSessionEnabled) {
                    receiver = getBuilder(useCredentials).sessionReceiver()
                        .queueName(queueName)
                        .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                        .sessionId(isSessionEnabled ? sessionId : null)
                        .buildAsyncClient();
                } else {
                    receiver = getBuilder(useCredentials).sessionReceiver()
                        .queueName(queueName)
                        .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                        .sessionId(isSessionEnabled ? sessionId : null)
                        .buildAsyncClient();
                }
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName();
                final String subscriptionName = isSessionEnabled ? getSessionSubscriptionName() : getSubscriptionName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = getBuilder(useCredentials).sender()
                    .topicName(topicName)
                    .buildAsyncClient();
                if (isSessionEnabled) {
                    receiver = getBuilder(useCredentials).sessionReceiver()
                        .topicName(topicName)
                        .subscriptionName(subscriptionName)
                        .sessionId(isSessionEnabled ? sessionId : null)
                        .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                        .buildAsyncClient();
                } else {
                    receiver = getBuilder(useCredentials).receiver()
                        .topicName(topicName)
                        .subscriptionName(subscriptionName)
                        .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                        .buildAsyncClient();
                }
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }
}
