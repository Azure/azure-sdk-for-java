// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusMessageBatch;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.stress.util.RunResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;

/**
 * Test ServiceBusSenderAsyncClient
 */
@Component("MessageSenderAsync")
public class MessageSenderAsync extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSenderAsync.class);
    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    private final AtomicReference<RunResult> runResult = new AtomicReference<>(RunResult.INCONCLUSIVE);
    private final AtomicReference<ServiceBusSenderAsyncClient> client = new AtomicReference<>();

    private final AtomicLong sentCounter = new AtomicLong();

    private final String prefix = UUID.randomUUID().toString().substring(25);

    @Override
    public RunResult run() {

        client.set(toClose(TestUtils.getSenderBuilder(options, false).buildAsyncClient()));

        int batchRatePerSec = sendMessageRatePerSecond / batchSize;
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

        LOGGER.atInfo()
            .addKeyValue("sent", sentCounter.get())
            .log("sender complete");

        return runResult.get();
    }

    private Mono<Void> send(ServiceBusMessageBatch batch) {
        return client.get().sendMessages(batch).onErrorResume(t -> true, t -> {
            runResult.set(RunResult.ERROR);
            LOGGER.error("error when sending", t);
            client.set(toClose(TestUtils.getSenderBuilder(options, false).buildAsyncClient()));
            return Mono.empty();
        });
    }

    private Mono<ServiceBusMessageBatch> createBatch() {
        final byte[] messagePayload = createMessagePayload(options.getMessageSize());

        return Mono.defer(() -> client.get().createMessageBatch()
            .doOnNext(b -> IntStream.range(0, batchSize).boxed()
                .forEach(unused -> {
                    ServiceBusMessage message = new ServiceBusMessage(messagePayload);
                    message.setMessageId(prefix + sentCounter.getAndIncrement());

                    if (b.tryAddMessage(message)) {
                        LOGGER.atInfo()
                            .addKeyValue("messageId", message.getMessageId())
                            .log("message sent");
                    }
                })));
    }
}
