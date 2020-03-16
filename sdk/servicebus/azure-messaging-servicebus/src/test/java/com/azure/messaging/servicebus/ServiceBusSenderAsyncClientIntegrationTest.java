// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

class ServiceBusSenderAsyncClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusSenderAsyncClient sender;

    ServiceBusSenderAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusSenderAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sender = createBuilder().buildAsyncSenderClient();
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
}
