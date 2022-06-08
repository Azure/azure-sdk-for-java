// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.implementation.health;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.integration.core.implementation.instrumentation.AbstractProcessorInstrumentation;
import com.azure.spring.integration.core.implementation.instrumentation.AbstractProcessorInstrumentationTests;
import com.azure.spring.integration.core.instrumentation.Instrumentation;

import java.time.Duration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceBusProcessorInstrumentationTests extends AbstractProcessorInstrumentationTests<ServiceBusErrorContext> {

    @Override
    public ServiceBusErrorContext getErrorContext(RuntimeException exception) {
        ServiceBusErrorContext errorContext = mock(ServiceBusErrorContext.class);
        when(errorContext.getException()).thenReturn(exception);
        return errorContext;
    }

    @Override
    public AbstractProcessorInstrumentation<ServiceBusErrorContext> getProcessorInstrumentation(Instrumentation.Type type, Duration window) {
        return new ServiceBusProcessorInstrumentation("test", type, window);
    }
}
