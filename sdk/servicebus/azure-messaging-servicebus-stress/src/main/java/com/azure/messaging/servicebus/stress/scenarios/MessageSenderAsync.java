// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.BinaryData;
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

/**
 * Test ServiceBusSenderAsyncClient
 */
@Component("MessageSenderAsync")
public class MessageSenderAsync extends ServiceBusScenario {
    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    private ServiceBusSenderAsyncClient client;

    private BinaryData messagePayload;
    private RateLimiter rateLimiter;

    @Override
    public void run() {
        messagePayload = createMessagePayload(options.getMessageSize());

        int batchRatePerSec = sendMessageRatePerSecond / batchSize;
        client = toClose(TestUtils.getSenderBuilder(options, false).buildAsyncClient());
        rateLimiter = toClose(new RateLimiter(batchRatePerSec, sendConcurrency));

        toClose(Mono.just(client)
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
        span.setAttribute(AttributeKey.longKey("sendMessageRatePerSecond"), sendMessageRatePerSecond);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("batchSize"), batchSize);
    }

    private Mono<Void> singleRun() {
        Mono<Void> run = client.createMessageBatch().flatMap(b -> {
            for (int i = 0; i < batchSize; i++) {
                if (!b.tryAddMessage(new ServiceBusMessage(messagePayload))) {
                    telemetryHelper.recordError("batch is full", "createBatch");
                    break;
                }
            }
            return client.sendMessages(b);
        }).onErrorResume(e -> {
            telemetryHelper.recordError(e, "create and send batch");
            return Mono.empty();
        }).doOnCancel(() -> telemetryHelper.recordError("cancelled", "create and send batch"));

        return Mono.usingWhen(rateLimiter.acquire(), i -> run, i -> {
            rateLimiter.release();
            return Mono.empty();
        });
    }
}
