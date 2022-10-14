// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service("MessageSenderSync")
public class MessageSenderSync extends ServiceBusScenario{
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiver.class);

    @Value("${SEND_TIMES:100000}")
    private int sendTimes;

    @Value("${SEND_MESSAGES:100}")
    private int messagesToSend;

    @Override
    public void run() {
        final String connectionString = options.getServicebusConnectionString();
        final MessagingEntityType entityType = options.getServicebusEntityType();
        String queueName = null;
        String topicName = null;
        if (entityType == MessagingEntityType.QUEUE) {
            queueName = options.getServicebusQueueName();
        } else if (entityType == MessagingEntityType.TOPIC) {
            topicName = options.getServicebusTopicName();
        }

        ServiceBusSenderAsyncClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .topicName(topicName)
            .buildAsyncClient();

        try (client) {
            for (long i = 0; i < sendTimes; i++) {
                List<ServiceBusMessage> eventDataList = new ArrayList<>();
                IntStream.range(0, messagesToSend).forEach(j -> {
                    eventDataList.add(new ServiceBusMessage("A"));
                });
                try {
                    client.sendMessages(eventDataList);
                } catch (Exception exp) {
                    LOGGER.error(exp.getMessage());
                }
            }
        }
    }
}
