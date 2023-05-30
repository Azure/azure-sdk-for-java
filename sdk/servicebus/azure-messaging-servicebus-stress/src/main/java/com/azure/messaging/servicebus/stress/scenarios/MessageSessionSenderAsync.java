// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

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
        Flux<ServiceBusMessage> messages = Flux.range(0, Integer.MAX_VALUE)
            .map(i -> new ServiceBusMessage(messagePayload).setSessionId(Integer.toString(i % sessionsToSend)));

        messages
            .flatMap(msg ->
                rateLimiter.acquire()
                    .then(client.sendMessage(msg)
                        .doFinally(i -> rateLimiter.release()))
            )
            .take(testDuration)
            .parallel(sendConcurrency, sendConcurrency)
            .runOn(Schedulers.boundedElastic())
            .subscribe(i -> { }, e -> LOGGER.error("error when sending", e), () -> LOGGER.info("done"));

        blockingWait(testDuration);
        client.close();
    }
}
