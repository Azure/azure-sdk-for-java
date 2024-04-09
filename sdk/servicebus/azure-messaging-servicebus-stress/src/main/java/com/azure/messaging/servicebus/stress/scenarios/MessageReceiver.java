// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.getReceiverBuilder;

/**
 * Test ServiceBusReceiverClient
 */
@Component("MessageReceiver")
public class MessageReceiver extends ServiceBusScenario {
    @Value("${BATCH_SIZE:10}")
    private int batchSize;

    @Override
    public void run() {
        long endAtEpochMillis = Instant.now().plus(options.getTestDuration()).toEpochMilli();

        ServiceBusReceiverClient client = toClose(getReceiverBuilder(options, false).buildClient());

        while (Instant.now().toEpochMilli() < endAtEpochMillis) {
            IterableStream<ServiceBusReceivedMessage> receivedMessages = client.receiveMessages(batchSize);

            int count = 0;

            for (ServiceBusReceivedMessage receivedMessage : receivedMessages) {
                try {
                    client.complete(receivedMessage);
                } catch (Throwable ex) {
                    telemetryHelper.recordError(ex, "complete");
                }

                count++;
            }

            if (count == 0) {
                blockingWait(Duration.ofMillis(100));
            }
        }
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("batchSize"), batchSize);
    }
}
