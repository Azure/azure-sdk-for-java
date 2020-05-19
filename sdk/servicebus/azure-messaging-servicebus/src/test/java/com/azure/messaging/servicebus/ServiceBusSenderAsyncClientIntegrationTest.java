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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

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
        setSenderAndReceiver(entityType, false);

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
        setSenderAndReceiver(entityType, false);
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
        setSenderAndReceiver(entityType, false);

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
        setSenderAndReceiver(entityType, true);

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

    void setSenderAndReceiver(MessagingEntityType entityType, boolean useCredentials) {
        switch (entityType) {
            case QUEUE:
                final String queueName = getQueueName();

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = getBuilder(useCredentials).sender()
                    .queueName(queueName)
                    .buildAsyncClient();
                receiver = getBuilder(useCredentials).receiver()
                    .queueName(queueName)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildAsyncClient();
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName();
                final String subscriptionName = getSubscriptionName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = getBuilder(useCredentials).sender()
                    .topicName(topicName)
                    .buildAsyncClient();
                receiver = getBuilder(useCredentials).receiver()
                    .topicName(topicName)
                    .subscriptionName(subscriptionName)
                    .receiveMode(ReceiveMode.RECEIVE_AND_DELETE)
                    .buildAsyncClient();
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }
}
