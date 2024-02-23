// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.stress.util.RateLimiter;
import com.azure.messaging.servicebus.stress.util.TestUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.createBatchSync;
import static com.azure.messaging.servicebus.stress.util.TestUtils.createMessagePayload;

/**
 * Test ServiceBusSenderClient
 */
@Component("MessageSender")
public class MessageSender extends ServiceBusScenario {
    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Override
    public void run() {
        final BinaryData messagePayload = createMessagePayload(options.getMessageSize());
        ServiceBusSenderClient client = toClose(TestUtils.getSenderBuilder(options, false).buildClient());
        long endAtEpochMillis = Instant.now().plus(options.getTestDuration()).toEpochMilli();

        int batchRatePerSecond = sendMessageRatePerSecond / batchSize;
        RateLimiter rateLimiter = toClose(new RateLimiter(batchRatePerSecond, sendConcurrency));
        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            if (rateLimiter.tryAcquire()) {
                try {
                    client.sendMessages(createBatchSync(client, messagePayload, batchSize));
                } catch (Exception ex) {
                    telemetryHelper.recordError(ex, "send");
                }
            } else {
                blockingWait(Duration.ofMillis(10));
            }
        }
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("sendMessageRatePerSecond"), sendMessageRatePerSecond);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("batchSize"), batchSize);
    }
}
