// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getSessionProcessorBuilder;

/**
 * Test ServiceBusSessionProcessorClient
 */
@Component("MessageSessionProcessor")
public class MessageSessionProcessor extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSessionProcessor.class);

    @Value("${DURATION_IN_MINUTES:15}")
    private int durationInMinutes;

    @Value("${MAX_CONCURRENT_SESSIONS:1}")
    private int maxConcurrentSessions;

    @Value("${MAX_CONCURRENT_CALLS:1}")
    private int maxConcurrentCalls;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Override
    public void run() {
        ServiceBusProcessorClient processor = getSessionProcessorBuilder(options)
                .maxConcurrentSessions(maxConcurrentSessions)
                .maxConcurrentCalls(maxConcurrentCalls)
                .prefetchCount(prefetchCount)
                .processMessage(messageContext -> messageContext.complete())
                .processError(err -> {
                    throw LOGGER.logExceptionAsError(new RuntimeException(err.getException()));
                })
                .buildProcessorClient();

        processor.start();
        blockingWait(Duration.ofMinutes(durationInMinutes));
        processor.close();
    }
}
