// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound.health;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.spring.integration.instrumentation.Instrumentation;

import java.time.Duration;

/**
 * EventHus health details entity class.
 */
public class EventHusProcessorInstrumentation implements Instrumentation {

    private final String name;

    private final Type type;

    private long lastErrorTimestamp = Long.MIN_VALUE;

    private final Duration noneErrorWindow;

    private ErrorContext errorContext;

    public EventHusProcessorInstrumentation(String name, Type type, Duration noneErrorWindow) {
        this.name = name;
        this.type = type;
        this.noneErrorWindow = noneErrorWindow;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Throwable getException() {
        return errorContext == null ? null : errorContext.getThrowable();
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

    public void markError(ErrorContext errorContext) {
        this.errorContext = errorContext;
        this.lastErrorTimestamp = System.currentTimeMillis();
    }

    public ErrorContext getErrorContext() {
        return errorContext;
    }

    public String getName() {
        return name;
    }

}
