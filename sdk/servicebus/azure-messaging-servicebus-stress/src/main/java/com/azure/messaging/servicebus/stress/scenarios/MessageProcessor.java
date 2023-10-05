// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.stress.util.RunResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.createMessagePayload;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getProcessorBuilder;

/**
 * Test ServiceBusProcessorClient
 */
@Component("MessageProcessor")
public class MessageProcessor extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageProcessor.class);

    @Value("${PROCESS_CALLBACK_DURATION_AVG_IN_MS:50}")
    private int processMessageDurationAvgInMs;

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

    @Value("${LOCK_DURATION_IN_MS:30000}")
    private int lockDurationInMs;

    @Value("${AUTO_RENEW_LOCK:true}")
    private boolean renewLock;

    private byte[] expectedPayload;

    private final AtomicReference<RunResult> runResult = new AtomicReference<>(RunResult.INCONCLUSIVE);


    @Override
    public RunResult run() throws InterruptedException {
        expectedPayload = createMessagePayload(options.getMessageSize());

        ServiceBusProcessorClient processor = toClose(getProcessorBuilder(options)
            .maxAutoLockRenewDuration(renewLock ? Duration.ofMinutes(5) : Duration.ZERO)
            .maxConcurrentCalls(maxConcurrentCalls)
            .prefetchCount(prefetchCount)
            .processMessage(this::process)
            .processError(err -> {
                LOGGER.atError()
                    .addKeyValue("source", err.getErrorSource())
                    .log("processor error", err.getException());
                runResult.set(RunResult.ERROR);
            })
            .buildProcessorClient());
        processor.start();

        blockingWait(options.getTestDuration());

        int activeMessages = getRemainingQueueMessages();
        for (int extraMinutes = 0; extraMinutes < 3 && activeMessages > 0; extraMinutes++) {
            blockingWait(Duration.ofMinutes(1));
            activeMessages = getRemainingQueueMessages();
        }

        processor.stop();
        if (activeMessages != 0) {
            runResult.set(RunResult.WARNING);
        }

        return runResult.get();
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
            return lockDurationInMs + 1000;
        } else if (processMessageDurationAvgInMs != 0) {
            return random.nextInt(processMessageDurationAvgInMs * 2);
        }

        return 0;
    }

    private boolean checkMessage(ServiceBusReceivedMessage message) {
        LOGGER.atInfo()
            .addKeyValue("messageId", message.getMessageId())
            .addKeyValue("traceparent", message.getApplicationProperties().get("traceparent"))
            .addKeyValue("deliveryCount", message.getDeliveryCount())
            .addKeyValue("lockToken", message.getLockToken())
            .addKeyValue("lockedUntil", message.getLockedUntil())
            .log("message received");

        if (message.getLockedUntil().isBefore(OffsetDateTime.now())) {
            LOGGER.atError()
                .addKeyValue("messageId", message.getMessageId())
                .addKeyValue("deliveryCount", message.getDeliveryCount())
                .log("message lock expired");
            runResult.set(RunResult.ERROR);
            return false;
        }

        byte[] payload = message.getBody().toBytes();
        if (payload.length != expectedPayload.length) {
            LOGGER.atError()
                .addKeyValue("messageId", message.getMessageId())
                .addKeyValue("actualSize", payload.length)
                .addKeyValue("expectedSize", expectedPayload.length)
                .log("message corrupted");
            runResult.set(RunResult.ERROR);
        }

        for (int i = 0; i < payload.length; i++) {
            if (payload[i] != expectedPayload[i]) {
                LOGGER.atError()
                    .addKeyValue("messageId", message.getMessageId())
                    .addKeyValue("index", i)
                    .addKeyValue("actual", payload[i])
                    .addKeyValue("expected", expectedPayload[i])
                    .log("message corrupted");
                runResult.set(RunResult.ERROR);
            }
        }

        return true;
    }

    private void settleMessage(ServiceBusReceivedMessageContext messageContext) {
        String operation = "ignored";
        try {
            double random = ThreadLocalRandom.current().nextDouble(1);
            if (random < abandonRatio) {
                operation = "abandoned";
                messageContext.abandon();
            } else if (random >= abandonRatio + noDispositionRatio) {
                operation = "completed";
                messageContext.complete();
            }

            LOGGER.atInfo()
                .addKeyValue("messageId", messageContext.getMessage().getMessageId())
                .addKeyValue("deliveryCount", messageContext.getMessage().getDeliveryCount())
                .log("message " + operation);
        } catch (RuntimeException ex) {
            runResult.set(RunResult.ERROR);
            LOGGER.atVerbose()
                .addKeyValue("messageId", messageContext.getMessage().getMessageId())
                .log("message settlement failed");
        }
    }
}
