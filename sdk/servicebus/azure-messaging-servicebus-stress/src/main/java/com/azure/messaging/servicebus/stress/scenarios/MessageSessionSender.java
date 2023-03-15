// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
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
 * Test ServiceBusSenderClient and send session messages
 */
@Component("MessageSessionSender")
public class MessageSessionSender extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSessionSender.class);

    private static final Random RANDOM = new Random();

    @Value("${SEND_TIMES:100000}")
    private int sendTimes;

    @Value("${SEND_SESSIONS:128}")
    private int sessionsToSend;

    @Value("${SEND_MESSAGES:100}")
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
            queueName = options.getServicebusSessionQueueName();
        } else if (entityType == EntityType.TOPIC) {
            topicName = options.getServicebusTopicName();
        }

        ServiceBusSenderClient client = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .retryOptions(new AmqpRetryOptions().setMaxRetries(5).setMode(AmqpRetryMode.FIXED))
            .sender()
            .queueName(queueName)
            .topicName(topicName)
            .buildClient();

        final byte[] payload = new byte[payloadSize];
        RANDOM.nextBytes(payload);

        for (long i = 0; i < sendTimes; i++) {
            for (int j = 0; j < sessionsToSend; j++) {
                List<ServiceBusMessage> eventDataList = new ArrayList<>();
                final String sessionId = Integer.toString(j);
                IntStream.range(0, messagesToSend).forEach(k -> {
                    eventDataList.add(new ServiceBusMessage(payload).setSessionId(sessionId));
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
