// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createBatch;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;

/**
 * Test ServiceBusSenderAsyncClient
 */
@Component("MessageSenderAsync")
public class MessageSenderAsync extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSenderAsync.class);
    @Value("${DURATION_IN_MINUTES:15}")
    private int durationInMinutes;

    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${BATCH_SIZE:2}")
    private int batchSize;

    @Value("${MESSAGE_SIZE_IN_BYTES:128}")
    private int messageSize;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    private final AtomicLong counter = new AtomicLong();
    @Override
    public void run() {
        final byte[] messagePayload = createMessagePayload(messageSize);
        Duration testDuration = Duration.ofMinutes(durationInMinutes);

        // workaround non-retryable send
        List<ServiceBusSenderAsyncClient> clients = new ArrayList<>();
        for (int i = 0; i < sendConcurrency; i++) {
            clients.add(TestUtils.getSenderBuilder(options, false).buildAsyncClient());
        }

        int batchRatePerSec = sendMessageRatePerSecond / batchSize;
        RateLimiter rateLimiter = new RateLimiter(batchRatePerSec, sendConcurrency);
        Flux<List<ServiceBusMessage>> batches = Mono.fromSupplier(() -> createBatch(messagePayload, batchSize))
            .repeat();

        batches
            .take(testDuration)
            .flatMap(batch ->
                rateLimiter.acquire()
                    .then(getClient(clients).sendMessages(batch)
                        .onErrorResume(t -> true, t -> {
                            LOGGER.error("error when sending", t);
                            return Mono.empty();
                        })
                        .doFinally(i -> rateLimiter.release()))
            )
            .parallel(sendConcurrency, sendConcurrency)
            .runOn(Schedulers.boundedElastic())
            .subscribe();

        blockingWait(testDuration.plusSeconds(30));
        LOGGER.info("done");
        clients.forEach(c -> c.close());
        rateLimiter.close();
    }

    private ServiceBusSenderAsyncClient getClient(List<ServiceBusSenderAsyncClient> clients) {
        int index = (int) counter.getAndIncrement() % clients.size();
        return clients.get(index);
    }
}
