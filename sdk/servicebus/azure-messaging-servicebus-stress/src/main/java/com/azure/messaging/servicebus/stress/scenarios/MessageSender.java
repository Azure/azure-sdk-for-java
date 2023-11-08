// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.stress.util.RunResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createBatchSync;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;

/**
 * Test ServiceBusSenderClient
 */
@Component("MessageSender")
public class MessageSender extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSender.class);

    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Value("${MESSAGE_SIZE_IN_BYTES:8}")
    private int messageSize;

    @Override
    public RunResult run() {
        AtomicReference<RunResult> result = new AtomicReference<>(RunResult.INCONCLUSIVE);
        final byte[] messagePayload = createMessagePayload(messageSize);
        ServiceBusSenderClient client = toClose(TestUtils.getSenderBuilder(options, false).buildClient());
        long endAtEpochMillis = Instant.now().plus(options.getTestDuration()).toEpochMilli();

        int batchRatePerSecond = sendMessageRatePerSecond / batchSize;
        RateLimiter rateLimiter = toClose(new RateLimiter(batchRatePerSecond, sendConcurrency));
        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            if (rateLimiter.tryAcquire()) {
                try {
                    client.sendMessages(createBatchSync(client, messagePayload, batchSize));
                } catch (Exception ex) {
                    result.set(RunResult.ERROR);
                    LOGGER.error("send error", ex);
                }
            } else {
                blockingWait(Duration.ofMillis(10));
            }
        }

        return result.get();
    }
}
