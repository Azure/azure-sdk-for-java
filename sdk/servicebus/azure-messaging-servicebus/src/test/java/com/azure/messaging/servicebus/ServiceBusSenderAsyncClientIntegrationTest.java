// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder.ServiceBusSenderClientBuilder;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

class ServiceBusSenderAsyncClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderAsyncClient sender;

    ServiceBusSenderAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusSenderAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        ServiceBusSenderClientBuilder builder = createBuilder().buildSenderClientBuilder();

        final String queueName = getQueueName();
        if (queueName != null) {
            logger.info("Using queueName: {}", queueName);
            builder.entityName(queueName);
        } else {
            logger.info("Using entityPath from connection string.");
        }

        sender = builder.buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(sender);
    }

    /**
     * Verifies that we can send a message to a non-session queue.
     */
    @Test
    void nonSessionQueueSendMessage() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message))
            .verifyComplete();
    }

    /**
     * Verifies that we can send a {@link ServiceBusMessageBatch} to a non-session queue.
     */
    @Test
    void nonSessionMessageBatch() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(1024);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(3, messageId);

        // Assert & Act
        StepVerifier.create(sender.createBatch(options)
            .flatMap(batch -> {
                for (ServiceBusMessage message : messages) {
                    Assertions.assertTrue(batch.tryAdd(message));
                }

                return sender.send(batch);
            }))
            .verifyComplete();
    }
}
