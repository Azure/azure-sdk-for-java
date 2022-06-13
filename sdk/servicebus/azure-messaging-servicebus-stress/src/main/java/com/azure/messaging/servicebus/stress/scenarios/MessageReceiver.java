// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service("MessageReceiver")
public class MessageReceiver extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiver.class);

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

        ServiceBusReceiverAsyncClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .queueName(queueName)
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .buildAsyncClient();

        client.receiveMessages()
            .flatMap(message -> {
                LOGGER.info("message received: {}", message.getMessageId());
                rateMeter.add(metricKey, 1);
                return client.complete(message)
                    .onErrorResume(error -> {
                        LOGGER.error("error happened: {}", error.getMessage());
                        return Mono.empty();
                    });
            })
            .subscribe(
                data -> {
                },
                error -> {
                    LOGGER.error("error happened: {}", error.getMessage());
                    telemetryClient.trackException((Exception) error);
                });

    }
}
