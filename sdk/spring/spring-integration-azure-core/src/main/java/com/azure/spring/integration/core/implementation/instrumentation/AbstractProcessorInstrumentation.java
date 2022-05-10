// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.implementation.instrumentation;

import com.azure.spring.integration.core.instrumentation.Instrumentation;

import java.time.Duration;

/**
 * Abstract instrumentation class for a messaging processor client. Because a process is self-recoverable, so the
 * instrumentation will mark the processor as UP if no exceptions happen in a time window after marking it as DOWN.
 */
public abstract class AbstractProcessorInstrumentation<T> implements Instrumentation {

    private final String name;

    private final Type type;

    private final Duration noneErrorWindow;

    private volatile long lastErrorTimestamp = Long.MIN_VALUE;

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
    public void setStatus(Status status) {

    }

    @Override
    public void setStatus(Status status, Throwable exception) {

    }

    @Override
    public Status getStatus() {
        if (System.currentTimeMillis() > lastErrorTimestamp + noneErrorWindow.toMillis()) {
            this.errorContext = null;
            return Status.UP;
        } else {
            return Status.DOWN;
        }
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
