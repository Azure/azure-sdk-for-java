package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.junit.jupiter.api.Assertions;

/**
 * Integration tests for receiving from a Topic.
 */
class ServiceBusReceiverAsyncClientTopicIntegrationTest extends IntegrationTestBase {
    private static final String CONTENTS = "Test-contents";

    private ServiceBusReceiverAsyncClient receiver;
    private ServiceBusSenderClient sender;

    ServiceBusReceiverAsyncClientTopicIntegrationTest() {
        super(new ClientLogger(ServiceBusReceiverAsyncClientTopicIntegrationTest.class));
    }

    @Override
    protected void beforeTest() {
        final String topicName = getTopicName();
        Assertions.assertNotNull(topicName, "'topicName' cannot be null.");

        final String subscriptionName = getSubscriptionName();
        Assertions.assertNotNull(subscriptionName, "'subscriptionName' cannot be null.");

        sender = createBuilder().sender().queueName(topicName).buildClient();
        receiver = createBuilder()
            .receiver()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        dispose(receiver, sender);
    }
}
