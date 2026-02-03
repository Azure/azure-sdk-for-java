// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.stress.scenarios;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubBufferedProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubBufferedProducerClientBuilder;
import com.azure.messaging.eventhubs.stress.util.RateLimiter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.messaging.eventhubs.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.eventhubs.stress.util.TestUtils.createMessagePayload;

/**
 * Test for EventSenderBuffered
 */
@Component("EventSenderBuffered")
public class EventSenderBuffered extends EventHubsScenario {
    private static final String PREFIX = UUID.randomUUID().toString().substring(25);
    private static final ClientLogger LOGGER = new ClientLogger(EventSenderBuffered.class);

    @Value("${SEND_MESSAGE_RATE:100}")
    private int sendMessageRatePerSecond;

    @Value("${MAX_EVENT_BUFFER_LENGTH_PER_PARTITION:100}")
    private int maxEventBufferLengthPerPartition;

    @Value("${MAX_WAIT_TIME_IN_MS:0}")
    private int maxWaitTimeInMs;

    private BinaryData messagePayload;
    private final AtomicLong sentCounter = new AtomicLong();
    private EventHubBufferedProducerAsyncClient sender;
    private RateLimiter rateLimiter;

    @Override
    public void run() {
        sender = toClose(getBuilder()
            .onSendBatchFailed(
                context -> telemetryHelper.recordError(context.getThrowable(), "sendBuffed", context.getPartitionId()))
            .onSendBatchSucceeded(context -> LOGGER.verbose("Send success."))
            .buildAsyncClient());

        messagePayload = createMessagePayload(options.getMessageSize());
        rateLimiter = toClose(new RateLimiter(sendMessageRatePerSecond, 10));

        toClose(telemetryHelper.instrumentRunAsync(singleRun(), "enqueue event")
            .repeat()
            .take(options.getTestDuration())
            .subscribe());

        blockingWait(options.getTestDuration().plusSeconds(30));
    }

    private Mono<Void> singleRun() {
        return createEvent()
            .flatMap(event -> Mono.usingWhen(rateLimiter.acquire(), i -> sender.enqueueEvent(event), i -> {
                rateLimiter.release();
                return Mono.empty();
            }))
            .then();
    }

    @Override
    public void recordRunOptions(Span span) {
        span.setAttribute(AttributeKey.longKey("sendMessageRatePerSecond"), sendMessageRatePerSecond);
        span.setAttribute(AttributeKey.longKey("maxEventBufferLengthPerPartition"), maxEventBufferLengthPerPartition);
        span.setAttribute(AttributeKey.longKey("maxWaitTimeInMs"), maxWaitTimeInMs);
    }

    private Mono<EventData> createEvent() {
        return Mono.fromCallable(() -> {
            EventData message = new EventData(messagePayload);
            message.setMessageId(PREFIX + sentCounter.getAndIncrement());
            return message;
        });
    }

    private EventHubBufferedProducerClientBuilder getBuilder() {
        final TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
        final EventHubBufferedProducerClientBuilder builder = new EventHubBufferedProducerClientBuilder().credential(
            options.getEventHubsFullyQualifiedNamespace(), options.getEventHubsEventHubName(), tokenCredential);

        if (maxEventBufferLengthPerPartition > 0) {
            builder.maxEventBufferLengthPerPartition(maxEventBufferLengthPerPartition);
        }

        if (maxWaitTimeInMs > 0) {
            builder.maxWaitTime(Duration.ofMillis(maxWaitTimeInMs));
        }

        return builder;
    }
}
