// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getProcessorBuilder;

/**
 * Test ServiceBusProcessorClient
 */
@Component("MessageProcessor")
public class MessageProcessor extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageProcessor.class);

    @Value("${DURATION_IN_MINUTES:15}")
    private int testDurationInMinutes;

    // lock duration is 10 sec, so in some cases we'll do lock renewal
    @Value("${PROCESS_CALLBACK_DURATION_MAX_IN_SECONDS:12}")
    private int processMessageDurationMaxInSeconds;

    @Value("${MAX_CONCURRENT_CALLS:20}")
    private int maxConcurrentCalls;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Override
    public void run() {
        ServiceBusProcessorClient processor = getProcessorBuilder(options)
            .maxAutoLockRenewDuration(Duration.ofSeconds(processMessageDurationMaxInSeconds + 5))
            .maxConcurrentCalls(maxConcurrentCalls)
            .prefetchCount(prefetchCount)
            .processMessage(this::process)
            .processError(err -> {
                throw LOGGER.logExceptionAsError(new RuntimeException(err.getException()));
            })
            .buildProcessorClient();

        processor.start();
        blockingWait(Duration.ofMinutes(testDurationInMinutes));
        processor.close();
    }

    private void process(ServiceBusReceivedMessageContext messageContext) {
        if (processMessageDurationMaxInSeconds != 0) {
            int processTimeMs = ThreadLocalRandom.current().nextInt(processMessageDurationMaxInSeconds * 1000);
            try {
                Thread.sleep(processTimeMs);
            } catch (InterruptedException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }
        messageContext.complete();
    }
}
