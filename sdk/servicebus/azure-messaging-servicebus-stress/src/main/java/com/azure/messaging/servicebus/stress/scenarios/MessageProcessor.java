// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service("MessageProcessor")
public class MessageProcessor extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageProcessor.class);

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

        ServiceBusProcessorClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .retryOptions(new AmqpRetryOptions().setTryTimeout(Duration.ofSeconds(5)))
            .processor()
            .queueName(queueName)
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .maxConcurrentCalls(2)
            //.maxAutoLockRenewDuration(Duration.ofMinutes(5))
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .prefetchCount(0)
            .processMessage(messageContext -> {
                System.out.printf("messageProcessor %s%n", messageContext.getMessage().getLockToken());
                LOGGER.info("Before complete. messageId: {}, lockToken: {}",
                    messageContext.getMessage().getMessageId(),
                    messageContext.getMessage().getLockToken());
                messageContext.complete();
                rateMeter.add(metricKey, 1);
                LOGGER.info("After complete. messageId: {}, lockToken: {}",
                    messageContext.getMessage().getMessageId(),
                    messageContext.getMessage().getLockToken());
            })
            .processError(err -> {
                throw LOGGER.logExceptionAsError(new RuntimeException(err.getException()));
            })
            .buildProcessorClient();

        client.start();

    }
}
