// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createBatch;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;

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

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

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
        RateLimiter rateLimiter = new RateLimiter(batchRatePerSecond, sendConcurrency);
        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            if (rateLimiter.tryAcquire()) {
                try {
                    client.sendMessages(createBatch(messagePayload, batchSize));
                } catch (Exception ex) {
                    LOGGER.error("send error", ex);
                }
            } else {
                blockingWait(Duration.ofMillis(10));
            }
        }

        client.close();
        rateLimiter.close();
    }
}
