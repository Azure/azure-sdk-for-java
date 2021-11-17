// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.inbound.health;

import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.spring.integration.instrumentation.Instrumentation;

import java.time.Duration;

/**
 * ServiceBus health details entity class.
 */
public class ServiceBusProcessorInstrumentation implements Instrumentation {

    private final String name;

    private final Type type;

    private long lastErrorTimestamp = Long.MIN_VALUE;

    private final Duration noneErrorWindow;

    private ServiceBusErrorContext errorContext;

    public ServiceBusProcessorInstrumentation(String name, Type type, Duration noneErrorWindow) {
        this.name = name;
        this.type = type;
        this.noneErrorWindow = noneErrorWindow;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Throwable getException() {
        return errorContext == null ? null : errorContext.getException();
    }

    public boolean isDown() {
        if (System.currentTimeMillis() > lastErrorTimestamp + noneErrorWindow.toMillis()) {
            this.errorContext = null;
            return false;
        } else {
            return true;
        }
    }

    public boolean isUp() {
        return !isDown();
    }

    public void markError(ServiceBusErrorContext errorContext) {
        this.errorContext = errorContext;
        this.lastErrorTimestamp = System.currentTimeMillis();
    }

    public ServiceBusErrorContext getErrorContext() {
        return errorContext;
    }

    public String getName() {
        return name;
    }

}
