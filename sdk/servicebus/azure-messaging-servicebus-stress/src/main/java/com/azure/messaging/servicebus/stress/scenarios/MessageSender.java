// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Service("MessageSender")
public class MessageSender extends ServiceBusScenario {
    private static final int SEND_TIMES = 10000;
    private static final int MESSAGE_NUMBER = 500;
    private static final int PAYLOAD_SIZE = 4 * 1024;

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

        final byte[] payload = new byte[PAYLOAD_SIZE];
        (new Random()).nextBytes(payload);

        Flux.range(0, SEND_TIMES).concatMap(i -> {
            List<ServiceBusMessage> eventDataList = new ArrayList<>();
            IntStream.range(0, MESSAGE_NUMBER).forEach(j -> {
                eventDataList.add(new ServiceBusMessage(payload));
            });
            return client.sendMessages(eventDataList);
        }).subscribe();
    }
}
