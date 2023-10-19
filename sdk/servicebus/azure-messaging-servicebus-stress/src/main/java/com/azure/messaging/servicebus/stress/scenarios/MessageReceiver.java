// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.stress.util.RunResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getReceiverBuilder;

/**
 * Test ServiceBusReceiverClient
 */
@Component("MessageReceiver")
public class MessageReceiver extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageReceiver.class);

    @Value("${BATCH_SIZE:10}")
    private int batchSize;

    @Override
    public RunResult run() {
        RunResult result = RunResult.INCONCLUSIVE;
        long endAtEpochMillis = Instant.now().plus(options.getTestDuration()).toEpochMilli();

        ServiceBusReceiverClient client = toClose(getReceiverBuilder(options, false).buildClient());

        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            IterableStream<ServiceBusReceivedMessage> receivedMessages = client.receiveMessages(batchSize);

            int count = 0;

            for (ServiceBusReceivedMessage receivedMessage : receivedMessages) {
                try {
                    client.complete(receivedMessage);
                } catch (Throwable ex) {
                    LOGGER.atError()
                        .addKeyValue("messageId", receivedMessage.getMessageId())
                        .addKeyValue("lockToken", receivedMessage.getLockToken())
                        .log("Completion error", ex);
                    result = RunResult.ERROR;
                }

                count++;
            }

            if (count == 0) {
                blockingWait(Duration.ofMillis(100));
            }
        }

        return result;
    }
}
