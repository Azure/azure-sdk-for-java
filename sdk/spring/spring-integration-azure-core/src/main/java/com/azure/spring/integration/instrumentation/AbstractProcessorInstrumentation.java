// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

import java.time.Duration;

/**
 * Abstract instrumentation class.
 */
public abstract class AbstractProcessorInstrumentation<T> implements Instrumentation {

    private final String name;

    private final Type type;

    private long lastErrorTimestamp = Long.MIN_VALUE;

    private final Duration noneErrorWindow;

    private T errorContext;

    /**
     * Construct a {@link AbstractProcessorInstrumentation} with the specified name, {@link Type} and the period of a none error window.
     *
     * @param name the name
     * @param type the type
     * @param noneErrorWindow the period of a none error window
     */
    public AbstractProcessorInstrumentation(String name, Type type, Duration noneErrorWindow) {
        this.name = name;
        this.type = type;
        this.noneErrorWindow = noneErrorWindow;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isDown() {
        if (System.currentTimeMillis() > lastErrorTimestamp + noneErrorWindow.toMillis()) {
            this.errorContext = null;
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean isUp() {
        return !isDown();
    }

    /**
     * Mark error.
     *
     * @param errorContext the error context
     */
    public void markError(T errorContext) {
        this.errorContext = errorContext;
        this.lastErrorTimestamp = System.currentTimeMillis();
    }

    /**
     * Get error context.
     *
     * @return errorContext the error context
     */
    public T getErrorContext() {
        return errorContext;
    }

    @Override
    public String getName() {
        return name;
    }
}
