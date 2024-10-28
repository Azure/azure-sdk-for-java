// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.stress.scenarios;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.azure.messaging.servicebus.stress.util.TestUtils.blockingWait;
import static com.azure.messaging.servicebus.stress.util.TestUtils.getSessionProcessorBuilder;

/**
 * Test ServiceBusSessionProcessorClient
 */
@Component("MessageSessionProcessor")
public class MessageSessionProcessor extends ServiceBusScenario {

    @Value("${MAX_CONCURRENT_SESSIONS:1}")
    private int maxConcurrentSessions;

    @Value("${MAX_CONCURRENT_CALLS:1}")
    private int maxConcurrentCalls;

    @Value("${PREFETCH_COUNT:0}")
    private int prefetchCount;

    @Override
    public void run() {
        ServiceBusProcessorClient processor
            = toClose(getSessionProcessorBuilder(options).maxConcurrentSessions(maxConcurrentSessions)
                .maxConcurrentCalls(maxConcurrentCalls)
                .prefetchCount(prefetchCount)
                .processMessage(messageContext -> messageContext.complete())
                .processError(err -> telemetryHelper.recordError(err.getException(), "processError"))
                .buildProcessorClient());
        processor.start();

        blockingWait(options.getTestDuration());
    }

    @Override
    public void recordRunOptions(Span span) {
        super.recordRunOptions(span);
        span.setAttribute(AttributeKey.longKey("maxConcurrentSessions"), maxConcurrentSessions);
        span.setAttribute(AttributeKey.longKey("maxConcurrentCalls"), maxConcurrentCalls);
        span.setAttribute(AttributeKey.longKey("prefetchCount"), prefetchCount);
    }
}
