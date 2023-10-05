// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.stress.util.RunResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.scenarios.TestUtils.getSessionProcessorBuilder;

/**
 * Test ServiceBusSessionProcessorClient
 */
@Component("MessageSessionProcessor")
public class MessageSessionProcessor extends ServiceBusScenario {
    private static final ClientLogger LOGGER = new ClientLogger(MessageSessionProcessor.class);

    @Value("${MAX_CONCURRENT_SESSIONS:1}")
    private int maxConcurrentSessions;

    @Value("${MAX_CONCURRENT_CALLS:1}")
    private int maxConcurrentCalls;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Override
    public RunResult run() {
        AtomicReference<RunResult> runResult = new AtomicReference<>(RunResult.INCONCLUSIVE);
        ServiceBusProcessorClient processor = toClose(getSessionProcessorBuilder(options)
                .maxConcurrentSessions(maxConcurrentSessions)
                .maxConcurrentCalls(maxConcurrentCalls)
                .prefetchCount(prefetchCount)
                .processMessage(messageContext -> messageContext.complete())
                .processError(err -> {
                    LOGGER.atError()
                        .addKeyValue("source", err.getErrorSource())
                        .log("processor error", err.getException());
                    runResult.set(RunResult.ERROR);
                })
                .buildProcessorClient());
        toClose((AutoCloseable) () -> processor.stop());
        processor.start();

        blockingWait(options.getTestDuration());
        return runResult.get();
    }
}
