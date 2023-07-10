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
import java.time.Instant;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getSenderBuilder;

/**
 * Test ServiceBusSenderClient and send session messages
 */
@Component("MessageSessionSender")
public class MessageSessionSenderAsync extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSessionSenderAsync.class);

    @Value("${DURATION_IN_MINUTES:15}")
    private int durationInMinutes;

    @Value("${SEND_SESSIONS:8}")
    private int sessionsToSend;

    @Value("${SEND_MESSAGE_RATE:10}")
    private int sendMessageRatePerSecond;

    @Value("${PAYLOAD_SIZE_IN_BYTE:8}")
    private int payloadSize;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    @Override
    public void run() {
        ServiceBusSenderAsyncClient client = getSenderBuilder(options, true).buildAsyncClient();

        final byte[] messagePayload = createMessagePayload(payloadSize);
        Duration testDuration = Duration.ofMinutes(durationInMinutes);

        RateLimiter rateLimiter = new RateLimiter(sendMessageRatePerSecond, sendConcurrency);

        Flux<ServiceBusMessage> messages = Mono.fromSupplier(() -> new ServiceBusMessage(messagePayload).setSessionId(randomSessionId()))
            .repeat();

        messages
            .take(testDuration)
            .flatMap(msg ->
                rateLimiter.acquire()
                    .then(client.sendMessage(msg)
                        .onErrorResume(t -> true, t -> {
                            LOGGER.error("error when sending", t);
                            return Mono.empty();
                        })
                        .doFinally(i -> rateLimiter.release()))
            )
            .parallel(sendConcurrency, sendConcurrency)
            .runOn(Schedulers.boundedElastic())
            .subscribe();

        blockingWait(testDuration);
        LOGGER.info("done");
        client.close();
        rateLimiter.close();
    }

    private String randomSessionId() {
        return Integer.toString((int) (Instant.now().toEpochMilli() % sessionsToSend));
    }
}
