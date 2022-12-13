// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.stress.util.EntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Test ServiceBusSenderClient
 */
@Component("MessageSender")
public class MessageSender extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSender.class);

    private static final Random RANDOM = new Random();

    @Value("${SEND_TIMES:100000}")
    private int sendTimes;

    @Value("${SEND_MESSAGES:10}")
    private int messagesToSend;

    @Value("${PAYLOAD_SIZE_IN_BYTE:8}")
    private int payloadSize;

    @Override
    public void run() {
        final String connectionString = options.getServicebusConnectionString();
        final EntityType entityType = options.getServicebusEntityType();
        String queueName = null;
        String topicName = null;
        if (entityType == EntityType.QUEUE) {
            queueName = options.getServicebusQueueName();
        } else if (entityType == EntityType.TOPIC) {
            topicName = options.getServicebusTopicName();
        }

        ServiceBusSenderClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .topicName(topicName)
            .buildClient();

        final byte[] payload = new byte[payloadSize];
        RANDOM.nextBytes(payload);

        for (long i = 0; i < sendTimes; i++) {
            List<ServiceBusMessage> eventDataList = new ArrayList<>();
            IntStream.range(0, messagesToSend).forEach(j -> {
                eventDataList.add(new ServiceBusMessage(payload));
            });
            try {
                client.sendMessages(eventDataList);
            } catch (Exception exp) {
                LOGGER.error(exp.getMessage());
            }
        }

        client.close();
    }
}
