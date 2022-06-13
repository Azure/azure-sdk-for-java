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
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service("MessageReceiverSync")
public class MessageReceiverSync extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiverSync.class);

    private static final int MAX_MESSAGE_COUNT = 1000;

    @Override
    public void run() {
        final String connectionString = options.getServicebusConnectionString();
        final MessagingEntityType entityType = options.getServicebusEntityType();
        String queueName = null;
        String topicName = null;
        String subscriptionName = null;
        if (entityType == MessagingEntityType.QUEUE) {
            queueName = options.getServicebusQueueName();
        } else if (entityType == MessagingEntityType.TOPIC) {
            topicName = options.getServicebusTopicName();
            subscriptionName = options.getServicebusSubscriptionName();
        }
        final String metricKey = queueName != null ? queueName : topicName + "/" + subscriptionName;

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


        IterableStream<ServiceBusReceivedMessage> receivedMessages = client.receiveMessages(MAX_MESSAGE_COUNT);
        try {
            for (ServiceBusReceivedMessage receivedMessage : receivedMessages) {
                try {
                    LOGGER.info("Before complete. messageId: {}, lockToken: {}",
                        receivedMessage.getMessageId(),
                        receivedMessage.getLockToken());
                    client.complete(receivedMessage);
                    rateMeter.add(metricKey, 1);
                    LOGGER.info("After complete. messageId: {}, lockToken: {}",
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
            LOGGER.error("Iterating iterable from receiveMessages({}) error", MAX_MESSAGE_COUNT, err);
        }


    }
}
