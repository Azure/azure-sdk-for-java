// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMessageOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.azure.messaging.servicebus.TestUtils.MESSAGE_TRACKING_ID;

class ServiceBusReceiverAsyncClientIntegrationTest extends IntegrationTestBase {
    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderAsyncClient sender;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        sender = createBuilder().buildAsyncSenderClient();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, sender);
    }

    /**
     * Verifies that we can send and receive a message.
     */
    @Test
    void receiveMessageAutoComplete() {
        // Arrange
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);
        final ReceiveMessageOptions options = new ReceiveMessageOptions().setAutoComplete(true);
        receiver = createBuilder()
            .receiveMessageOptions(options)
            .buildAsyncReceiverClient();

        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiver.receive().take(1)))
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, receivedMessage.getBodyAsString());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }
}
