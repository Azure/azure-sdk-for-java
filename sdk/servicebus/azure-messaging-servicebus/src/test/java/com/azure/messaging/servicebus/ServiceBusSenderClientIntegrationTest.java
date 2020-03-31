// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class ServiceBusSenderClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderClient sender;

    ServiceBusSenderClientIntegrationTest() {
        super(new ClientLogger(ServiceBusSenderAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sender = createBuilder()
            .sender()
            .queueName(getQueueName())
            .buildClient();
    }

    @Override
    protected void afterTest() {
        if (sender != null) {
            sender.close();
        }
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
        sender.send(message);
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
        ServiceBusMessageBatch batch = sender.createBatch(options);
        for (ServiceBusMessage message : messages) {
            Assertions.assertTrue(batch.tryAdd(message));
        }
        sender.send(batch);
    }
}
