// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.CreateMessageBatchOptions;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Integration tests for the {@link ServiceBusSenderClient}.
 */
@Tag("integration")
class ServiceBusSenderClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderClient sender;
    private ServiceBusReceiverAsyncClient receiver;
    private final AtomicInteger messagesPending = new AtomicInteger();

    ServiceBusSenderClientIntegrationTest() {
        super(new ClientLogger(ServiceBusSenderAsyncClientIntegrationTest.class));
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
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT);

        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        // Assert & Act
        sender.sendMessage(message);

        messagesPending.incrementAndGet();
    }

    /**
     * Verifies that we can send a {@link ServiceBusMessageBatch} to a non-session queue.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionMessageBatch(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT);

        final String messageId = UUID.randomUUID().toString();
        final CreateMessageBatchOptions options = new CreateMessageBatchOptions().setMaximumSizeInBytes(1024);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(3, messageId, CONTENTS_BYTES);

        // Assert & Act
        ServiceBusMessageBatch batch = sender.createMessageBatch(options);
        for (ServiceBusMessage message : messages) {
            Assertions.assertTrue(batch.tryAddMessage(message));
        }

        sender.sendMessages(batch);

        for (int i = 0; i < messages.size(); i++) {
            messagesPending.incrementAndGet();
        }
    }

    /**
     * Verifies that we can send a list of messages to a non-session entity.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionEntitySendMessageList(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT);
        int count = 3;
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString(), CONTENTS_BYTES);

        // Assert & Act
        sender.sendMessages(messages);

        messages.forEach(serviceBusMessage -> messagesPending.incrementAndGet());
    }

    /**
     * Verifies that we can schedule a message to a non-session entity.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionScheduleMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT);

        final OffsetDateTime scheduledEnqueueTime = OffsetDateTime.now().plusSeconds(10);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        // Act
        long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime);

        // Assert
        Assertions.assertTrue(sequenceNumber >= 0);

        messagesPending.incrementAndGet();
    }

    /**
     * Verifies that we can cancel a scheduled a message to a non-session entity.
     */
    @MethodSource("receiverTypesProvider")
    @ParameterizedTest
    void nonSessionCancelScheduleMessage(MessagingEntityType entityType) {
        // Arrange
        setSenderAndReceiver(entityType, TestUtils.USE_CASE_DEFAULT);

        final OffsetDateTime scheduledEnqueueTime = OffsetDateTime.now().plusSeconds(20);
        final String messageId = UUID.randomUUID().toString();
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(CONTENTS_BYTES, messageId);

        // Assert & Act
        long sequenceNumber = sender.scheduleMessage(message, scheduledEnqueueTime);
        Assertions.assertTrue(sequenceNumber >= 0);

        sender.cancelScheduledMessage(sequenceNumber);

        messagesPending.incrementAndGet();
    }

    void setSenderAndReceiver(MessagingEntityType entityType, int entityIndex) {
        switch (entityType) {
            case QUEUE:
                final String queueName = getQueueName(entityIndex);

                Assertions.assertNotNull(queueName, "'queueName' cannot be null.");

                sender = getBuilder().sender()
                    .queueName(queueName)
                    .buildClient();
                receiver = getBuilder().receiver()
                    .queueName(queueName)
                    .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                    .buildAsyncClient();
                break;
            case SUBSCRIPTION:
                final String topicName = getTopicName(entityIndex);
                final String subscriptionName = TestUtils.getSubscriptionBaseName();

                Assertions.assertNotNull(topicName, "'topicName' cannot be null.");
                Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

                sender = getBuilder().sender()
                    .topicName(topicName)
                    .buildClient();
                receiver = getBuilder().receiver()
                    .topicName(topicName)
                    .subscriptionName(subscriptionName)
                    .receiveMode(ServiceBusReceiveMode.RECEIVE_AND_DELETE)
                    .buildAsyncClient();
                break;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException("Unknown entity type: " + entityType));
        }
    }
}
