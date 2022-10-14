// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Service("MessageSessionSenderSync")
public class MessageSessionSenderSync extends ServiceBusScenario {
    private final ClientLogger LOGGER = new ClientLogger(MessageProcessor.class);

    @Value("${SEND_TIMES:100000}")
    private int sendTimes;

    @Value("${SEND_SESSIONS:128}")
    private int sessionsToSend;

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

        ServiceBusSenderClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .retryOptions(new AmqpRetryOptions().setMaxRetries(5).setMode(AmqpRetryMode.FIXED))
            .sender()
            .queueName(queueName)
            .topicName(topicName)
            .buildClient();

        for (long i = 0; i < sendTimes; i++) {
            for (int j = 0; j < sessionsToSend; j++) {
                List<ServiceBusMessage> eventDataList = new ArrayList<>();
                final String sessionId = Integer.toString(j);
                IntStream.range(0, messagesToSend).forEach(k -> {
                    eventDataList.add(new ServiceBusMessage("A").setSessionId(sessionId));
                });
                try {
                    client.sendMessages(eventDataList);
                } catch (Exception exp) {
                    LOGGER.error(exp.getMessage());
                }
            }
        }

        client.close();
    }
}
