// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.stress.util.RateLimiter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.servicebus.stress.util.TestUtils.getSenderBuilder;

/**
 * Test ServiceBusSenderClient and send session messages
 */
@Component("MessageSessionSender")
public class MessageSessionSenderAsync extends ServiceBusScenario {

    @Value("${SEND_SESSIONS:8}")
    private int sessionsToSend;

    @Value("${SEND_MESSAGE_RATE:10}")
    private int sendMessageRatePerSecond;

    @Value("${SEND_CONCURRENCY:5}")
    private int sendConcurrency;

    @Override
    public void run() {
        ServiceBusSenderAsyncClient client = toClose(getSenderBuilder(options, true).buildAsyncClient());

        final BinaryData messagePayload = createMessagePayload(options.getMessageSize());

        RateLimiter rateLimiter = toClose(new RateLimiter(sendMessageRatePerSecond, sendConcurrency));

        Flux<ServiceBusMessage> messages
            = Mono.fromSupplier(() -> new ServiceBusMessage(messagePayload).setSessionId(randomSessionId())).repeat();

        messages.take(options.getTestDuration())
            .flatMap(msg -> rateLimiter.acquire().then(client.sendMessage(msg).onErrorResume(t -> true, t -> {
                telemetryHelper.recordError(t, "send");
                return Mono.empty();
            }).doFinally(i -> rateLimiter.release())))
            .parallel(sendConcurrency, sendConcurrency)
            .runOn(Schedulers.boundedElastic())
            .subscribe();

        blockingWait(options.getTestDuration());
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("sendMessageRatePerSecond"), sendMessageRatePerSecond);
        span.setAttribute(AttributeKey.longKey("sendConcurrency"), sendConcurrency);
        span.setAttribute(AttributeKey.longKey("sessionsToSend"), sessionsToSend);
    }

    private String randomSessionId() {
        return Integer.toString((int) (Instant.now().toEpochMilli() % sessionsToSend));
    }
}
