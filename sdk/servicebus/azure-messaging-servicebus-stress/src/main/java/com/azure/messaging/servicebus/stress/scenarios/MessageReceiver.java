// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getReceiverBuilder;

/**
 * Test ServiceBusReceiverClient
 */
@Component("MessageReceiver")
public class MessageReceiver extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiver.class);

    @Value("${DURATION_IN_MINUTES:15}")
    private int durationInMinutes;

    @Value("${BATCH_SIZE:10}")
    private int batchSize;

    @Override
    public void run() {
        long endAtEpochMillis = Instant.now().plus(durationInMinutes, ChronoUnit.MINUTES).toEpochMilli();

        ServiceBusReceiverClient client = getReceiverBuilder(options, false).buildClient();

        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            IterableStream<ServiceBusReceivedMessage> receivedMessages = client.receiveMessages(batchSize);

            int count = 0;

            for (ServiceBusReceivedMessage receivedMessage : receivedMessages) {
                try {
                    client.complete(receivedMessage);
                } catch (Throwable ex) {
                    LOGGER.error("Completion error. messageId: {}, lockToken: {}",
                        receivedMessage.getMessageId(),
                        receivedMessage.getLockToken(),
                        ex);
                }

                count++;
            }

            if (count == 0) {
                try {
                    // avoid busy looping
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    LOGGER.logExceptionAsError(new RuntimeException(e));
                }
            }
        }

        client.close();
    }
}
