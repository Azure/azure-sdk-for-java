// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.stress.util.RateLimiter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static com.azure.messaging.eventhubs.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.getBuilder;

/**
 * Test for EventSender
 */
@Component("EventSender")
public class EventSender extends EventHubsScenario {
    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    @Value("${SEND_BATCH_SIZE:10}")
    private int sendBatchSize;

    private BinaryData messagePayload;
    private RateLimiter rateLimiter;
    private EventHubProducerAsyncClient client;

    @Override
    public void run() {
        client = getBuilder(options).buildAsyncProducerClient();
        messagePayload = createMessagePayload(options.getMessageSize());
        rateLimiter = toClose(new RateLimiter(sendMessageRatePerSecond / sendBatchSize, sendConcurrency));

        toClose(Mono.just(client).repeat().flatMap(i -> {
            final Duration idleDuration = options.getIdleDuration();
            if (idleDuration.isZero()) {
                return singleRun();
            } else {
                return singleRun().then(Mono.delay(idleDuration));
            }
        }, sendConcurrency)
            .take(options.getTestDuration())
            .parallel(sendConcurrency, 1)
            .runOn(Schedulers.boundedElastic())
            .subscribe());

        blockingWait(options.getTestDuration());
    }

    private Mono<Void> singleRun() {
        Mono<Void> run = client.createBatch().flatMap(b -> {
            for (int i = 0; i < sendBatchSize; i++) {
                if (!b.tryAdd(new EventData(messagePayload))) {
                    telemetryHelper.recordError("batch is full", "createBatch", null);
                    break;
                }
            }
            return client.send(b);
        })
            .doOnError(e -> telemetryHelper.recordError(e, "create and send batch", null))
            .doOnCancel(() -> telemetryHelper.recordError("cancelled", "create and send batch", null));

        return Mono.usingWhen(rateLimiter.acquire(), i -> run, i -> {
            rateLimiter.release();
            return Mono.empty();
        });
    }

    @Override
    public void recordRunOptions(Span span) {
        span.setAttribute(AttributeKey.longKey("sendMessageRatePerSecond"), sendMessageRatePerSecond);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("sendBatchSize"), sendBatchSize);
    }
}
