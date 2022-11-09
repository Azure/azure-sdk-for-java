// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.stress.util.EntityType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Test ServiceBusSenderAsyncClient
 */
@Service("MessageSender")
public class MessageSender extends ServiceBusScenario {
    @Value("${SEND_TIMES:1000000}")
    private int sendTimes;

    @Value("${SEND_MESSAGES:1000}")
    private int messagesToSend;

    @Value("${PAYLOAD_SIZE_IN_BYTE:4096}")
    private int payloadSize;

    private final Random random = new Random();

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

        ServiceBusSenderAsyncClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName(queueName)
            .topicName(topicName)
            .buildAsyncClient();

        final byte[] payload = new byte[payloadSize];
        random.nextBytes(payload);

        Flux.range(0, sendTimes).concatMap(i -> {
            List<ServiceBusMessage> eventDataList = new ArrayList<>();
            IntStream.range(0, messagesToSend).forEach(j -> {
                eventDataList.add(new ServiceBusMessage(payload));
            });
            return client.sendMessages(eventDataList);
        }).subscribe();
    }
}
