// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.stress.util.RateLimiter;
import com.azure.messaging.servicebus.stress.util.TestUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.servicebus.stress.util.TestUtils.getSenderBuilder;

/**
 * Test ServiceBusSenderAsyncClient
 */
@Component("MessageSenderReconnectAsync")
public class MessageSenderReconnectAsync extends ServiceBusScenario {
    private final static ClientLogger LOGGER = new ClientLogger(MessageSenderReconnectAsync.class);
    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    private BinaryData messagePayload;

    @Override
    public void run() {
        messagePayload = createMessagePayload(options.getMessageSize());

        toClose(Mono.just(1)
            .repeat()
            .flatMap(i -> singleRun(), sendConcurrency)
            .take(options.getTestDuration())
            .parallel(sendConcurrency, 1)
            .runOn(Schedulers.boundedElastic())
            .subscribe());

        blockingWait(options.getTestDuration().plusSeconds(1));
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("batchSize"), batchSize);
    }


    private Mono<Void> singleRun() {
        return Mono.using(
            () -> getSenderBuilder(options, false).buildAsyncClient(),
            client -> singleSend(client),
            ServiceBusSenderAsyncClient::close);
    }
    private Mono<Void> singleSend(ServiceBusSenderAsyncClient client) {
        return client.createMessageBatch()
            .flatMap(b -> {
                for (int i = 0; i < batchSize; i ++) {
                    if (!b.tryAddMessage(new ServiceBusMessage(messagePayload))) {
                        telemetryHelper.recordError("batch is full", "createBatch");
                        break;
                    }
                }
                return client.sendMessages(b);
            })
            .onErrorResume(e -> {
                telemetryHelper.recordError(e, "create and send batch");
                return Mono.empty();
            })
            .doOnCancel(() -> telemetryHelper.recordError("cancelled", "create and send batch"));
    }
}
