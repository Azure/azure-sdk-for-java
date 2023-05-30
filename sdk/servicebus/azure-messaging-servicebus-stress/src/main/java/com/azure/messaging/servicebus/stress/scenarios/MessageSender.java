// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createBatch;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.limitRate;

/**
 * Test ServiceBusSenderClient
 */
@Component("MessageSender")
public class MessageSender extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSender.class);

    @Value("${DURATION_IN_MINUTES:15}")
    private int durationInMinutes;

    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Value("${MESSAGE_SIZE_IN_BYTES:8}")
    private int messageSize;

    @Override
    public void run() {
        final byte[] messagePayload = createMessagePayload(messageSize);
        ServiceBusSenderClient client = TestUtils.getSenderBuilder(options, false).buildClient();
        long endAtEpochMillis = Instant.now().plus(durationInMinutes, ChronoUnit.MINUTES).toEpochMilli();

        int batchRatePerSecond = sendMessageRatePerSecond / batchSize;
        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            long start = Instant.now().toEpochMilli();

            try {
                client.sendMessages(createBatch(messagePayload, batchSize));
                limitRate(batchRatePerSecond, start);
            } catch (Exception ex) {
                LOGGER.error("send error", ex);
            }
        }

        client.close();
    }
}
