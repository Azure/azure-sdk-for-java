// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.messaging.servicebus.stress.util.EntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Test ServiceBusReceiverClient
 */
@Component("MessageReceiver")
public class MessageReceiver extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiver.class);

    @Value("${MAX_RECEIVE_MESSAGES:100000}")
    private int maxReceiveMessages;

    @Override
    public void run() {
        final String connectionString = options.getServicebusConnectionString();
        final EntityType entityType = options.getServicebusEntityType();
        String queueName = null;
        String topicName = null;
        String subscriptionName = null;
        if (entityType == EntityType.QUEUE) {
            queueName = options.getServicebusQueueName();
        } else if (entityType == EntityType.TOPIC) {
            topicName = options.getServicebusTopicName();
            subscriptionName = options.getServicebusSubscriptionName();
        }

        final String receiveCounterKey = "Number of received messages - "
            + (queueName != null ? queueName : topicName + "/" + subscriptionName);

        ServiceBusReceiverClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .retryOptions(new AmqpRetryOptions().setMaxRetries(20)
                .setTryTimeout(Duration.ofMillis(5000))
                .setDelay(Duration.ofMillis(3000))
                .setMode(AmqpRetryMode.FIXED)
            )
            .receiver()
            .queueName(queueName)
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .buildClient();

        IterableStream<ServiceBusReceivedMessage> receivedMessages = client.receiveMessages(maxReceiveMessages);
        try {
            for (ServiceBusReceivedMessage receivedMessage : receivedMessages) {
                try {
                    LOGGER.verbose("Before complete. messageId: {}, lockToken: {}",
                        receivedMessage.getMessageId(),
                        receivedMessage.getLockToken());
                    client.complete(receivedMessage);
                    rateMeter.add(receiveCounterKey, 1);
                    LOGGER.verbose("After complete. messageId: {}, lockToken: {}",
                        receivedMessage.getMessageId(),
                        receivedMessage.getLockToken());
                } catch (ServiceBusException | AmqpException err) {
                    LOGGER.error("Completion error. messageId: {}, lockToken: {}",
                        receivedMessage.getMessageId(),
                        receivedMessage.getLockToken(),
                        err);
                }
            }
        } catch (ServiceBusException | AmqpException err) {
            LOGGER.error("Iterating iterable from receiveMessages({}) error", maxReceiveMessages, err);
        }

        client.close();
    }
}
