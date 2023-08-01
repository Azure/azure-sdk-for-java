// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.implementation.health;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.integration.core.implementation.instrumentation.AbstractProcessorInstrumentation;
import com.azure.spring.integration.core.implementation.instrumentation.AbstractProcessorInstrumentationTests;
import com.azure.spring.integration.core.instrumentation.Instrumentation;

import java.time.Duration;

class EventHubsProcessorInstrumentationTests extends AbstractProcessorInstrumentationTests<ErrorContext> {


    @Override
    public ErrorContext getErrorContext(RuntimeException exception) {
        return new ErrorContext(
            new PartitionContext("test", "test", "test", "test"), exception);
    }

    @Override
    public AbstractProcessorInstrumentation<ErrorContext> getProcessorInstrumentation(Instrumentation.Type type,
                                                                                      Duration window) {
        return new EventHubsProcessorInstrumentation("test", type, window);
    }
}
