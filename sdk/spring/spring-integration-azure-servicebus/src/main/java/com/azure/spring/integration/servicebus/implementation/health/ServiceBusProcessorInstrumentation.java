// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.implementation.health;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.integration.core.implementation.instrumentation.AbstractProcessorInstrumentation;

import java.time.Duration;

/**
 * ServiceBus health details entity class.
 */
public class ServiceBusProcessorInstrumentation extends AbstractProcessorInstrumentation<ServiceBusErrorContext> {

    /**
     * Construct a {@link ServiceBusProcessorInstrumentation} with the specified name, {@link Type} and the period of a none error window.
     *
     * @param name the name
     * @param type the type
     * @param noneErrorWindow the period of a none error window
     */
    public ServiceBusProcessorInstrumentation(String name, Type type, Duration noneErrorWindow) {
        super(name, type, noneErrorWindow);
    }

    @Override
    public Throwable getException() {
        return getErrorContext() == null ? null : getErrorContext().getException();
    }
}
