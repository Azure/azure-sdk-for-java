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
    private ReceiveMessageOptions receiveMessageOptions;

    ServiceBusReceiverAsyncClientIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientIntegrationTest.class));
        receiveMessageOptions = new ReceiveMessageOptions().setAutoComplete(true);
    }

    @Override
    protected void beforeTest() {
        sender = createBuilder().buildAsyncSenderClient();
        receiver = createBuilder()
            .receiveMessageOptions(receiveMessageOptions)
            .buildAsyncReceiverClient();
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

        // Assert & Act
        StepVerifier.create(sender.send(message).thenMany(receiver.receive().take(1)))
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, receivedMessage.getBodyAsString());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
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
                Assertions.assertEquals(contents, receivedMessage.getBodyAsString());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }

    /**
     * Verifies that we can send and peek a message.
     */
    @Test
    void peekFromSequencenumberMessage() {
        // Arrange
        final long fromSequenceNumber = 1;
        final String messageId = UUID.randomUUID().toString();
        final String contents = "Some-contents";
        final ServiceBusMessage message = TestUtils.getServiceBusMessage(contents, messageId, 0);

        // Assert & Act
        StepVerifier.create(sender.send(message).then(receiver.peek(fromSequenceNumber)))
            .assertNext(receivedMessage -> {
                Assertions.assertEquals(contents, receivedMessage.getBodyAsString());
                Assertions.assertTrue(receivedMessage.getProperties().containsKey(MESSAGE_TRACKING_ID));
                Assertions.assertEquals(messageId, receivedMessage.getProperties().get(MESSAGE_TRACKING_ID));
            })
            .verifyComplete();
    }
}
