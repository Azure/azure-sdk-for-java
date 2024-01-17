// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.stress.util.RateLimiter;
import com.azure.messaging.servicebus.stress.util.TestUtils;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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

    private final AtomicReference<ServiceBusSenderAsyncClient> client = new AtomicReference<>();

    private final AtomicLong sentCounter = new AtomicLong();

    private final String prefix = UUID.randomUUID().toString().substring(25);
    private BinaryData messagePayload;

    @Override
    public void run() {
        messagePayload = createMessagePayload(options.getMessageSize());

        int batchRatePerSec = sendMessageRatePerSecond / batchSize;
        client.set(toClose(TestUtils.getSenderBuilder(options, false).buildAsyncClient()));
        RateLimiter rateLimiter = toClose(new RateLimiter(batchRatePerSec, sendConcurrency));

        toClose(createBatch().repeat()
            .take(options.getTestDuration())
            .flatMap(batch ->
                rateLimiter.acquire()
                    .then(send(batch)
                        .doFinally(i -> rateLimiter.release())))
            .parallel(sendConcurrency, sendConcurrency)
            .runOn(Schedulers.boundedElastic())
            .subscribe());

        blockingWait(options.getTestDuration().plusSeconds(30));
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("sendMessageRatePerSecond"), sendMessageRatePerSecond);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("batchSize"), batchSize);
    }

    private Mono<Void> send(ServiceBusMessageBatch batch) {
        return client.get().sendMessages(batch).onErrorResume(t -> true, t -> {
            recordError("send error", t, "send");
            client.set(toClose(TestUtils.getSenderBuilder(options, false).buildAsyncClient()));
            return Mono.empty();
        });
    }

    private Mono<ServiceBusMessageBatch> createBatch() {
        return Mono.defer(() -> client.get().createMessageBatch()
            .doOnNext(b -> {
                for (int i = 0; i < batchSize; i ++) {
                    ServiceBusMessage message = new ServiceBusMessage(messagePayload);
                    message.setMessageId(prefix + sentCounter.getAndIncrement());
                    if (!b.tryAddMessage(message)) {
                        recordError("batch is full", null, "createBatch");
                        break;
                    }
                }
            }));
    }
}
