// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.createMessagePayload;
import static com.azure.messaging.servicebus.stress.util.TestUtils.getProcessorBuilder;

/**
 * Test ServiceBusProcessorClient
 */
@Component("MessageProcessor")
public class MessageProcessor extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageProcessor.class);

    @Value("${PROCESS_CALLBACK_DURATION_MAX_IN_MS:50}")
    private int processMessageDurationMaxInMs;

    @Value("${MAX_CONCURRENT_CALLS:100}")
    private int maxConcurrentCalls;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Value("${ABANDON_RATIO:0}")
    private double abandonRatio;

    @Value("${NO_DISPOSITION_RATIO:0}")
    private double noDispositionRatio;

    @Value("${LOCK_RENEWAL_NEEDED_RATIO:0}")
    private double lockRenewalNeededRatio;

    @Value("${LOCK_DURATION_IN_SEC:30}")
    private int lockDurationInSec;

    @Value("${AUTO_RENEW_LOCK:true}")
    private boolean renewLock;

    private byte[] expectedPayload;

    @Override
    public void run() throws InterruptedException {
        expectedPayload = createMessagePayload(options.getMessageSize()).toBytes();

        ServiceBusProcessorClient processor = toClose(
            getProcessorBuilder(options).maxAutoLockRenewDuration(renewLock ? Duration.ofMinutes(5) : Duration.ZERO)
                .maxConcurrentCalls(maxConcurrentCalls)
                .prefetchCount(prefetchCount)
                .processMessage(this::process)
                .processError(err -> telemetryHelper.recordError(err.getException(), "processError"))
                .buildProcessorClient());
        processor.start();

        blockingWait(options.getTestDuration());

        getRemainingQueueMessages();
        processor.stop();
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("processMessageDurationMaxInMs"), processMessageDurationMaxInMs);
        span.setAttribute(AttributeKey.longKey("maxConcurrentCalls"), maxConcurrentCalls);
        span.setAttribute(AttributeKey.longKey("prefetchCount"), prefetchCount);
        span.setAttribute(AttributeKey.doubleKey("abandonRatio"), abandonRatio);
        span.setAttribute(AttributeKey.doubleKey("noDispositionRatio"), noDispositionRatio);
        span.setAttribute(AttributeKey.doubleKey("lockRenewalNeededRatio"), lockRenewalNeededRatio);
        span.setAttribute(AttributeKey.longKey("lockDurationInSec"), lockDurationInSec);
        span.setAttribute(AttributeKey.booleanKey("renewLock"), renewLock);
    }

    private void process(ServiceBusReceivedMessageContext messageContext) {
        ServiceBusReceivedMessage message = messageContext.getMessage();
        if (checkMessage(message)) {
            blockingWait(Duration.ofMillis(getWaitTime()));
            settleMessage(messageContext);
        }
    }

    private int getWaitTime() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble(1) < lockRenewalNeededRatio) {
            return (lockDurationInSec + 1) * 1000;
        } else if (processMessageDurationMaxInMs != 0) {
            return random.nextInt(processMessageDurationMaxInMs);
        }

        return 0;
    }

    private boolean checkMessage(ServiceBusReceivedMessage message) {
        if (message.getLockedUntil().isBefore(OffsetDateTime.now())) {
            telemetryHelper.recordError("message lock expired", "checkMessage");
        }

        byte[] payload = message.getBody().toBytes();
        for (int i = 0; i < expectedPayload.length; i++) {
            if (payload[i] != expectedPayload[i]) {
                telemetryHelper.recordError("message corrupted", "checkMessage");
            }
        }

        return true;
    }

    private void settleMessage(ServiceBusReceivedMessageContext messageContext) {
        try {
            double random = ThreadLocalRandom.current().nextDouble(1);
            if (random < abandonRatio) {
                messageContext.abandon();
            } else if (random >= abandonRatio + noDispositionRatio) {
                messageContext.complete();
            } else {
                LOGGER.info("not settling message");
            }
        } catch (Throwable ex) {
            telemetryHelper.recordError(ex, "settleMessage");
        }
    }
}
