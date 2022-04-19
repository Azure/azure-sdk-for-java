// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.implementation.instrumentation;

import com.azure.spring.integration.core.instrumentation.Instrumentation;

/**
 *
 */
public class DefaultInstrumentation implements Instrumentation {

    private final String name;

    private final Type type;

    private volatile Status status = Status.DOWN;

    private Throwable exception;

    /**
     * Construct a {@link DefaultInstrumentation} with the specified name and type.
     *
     * @param name the name
     * @param type the type
     */
    public DefaultInstrumentation(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Get type.
     *
     * @return type the type
     * @see Type
     */
    public Type getType() {
        return type;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public Status getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public void setStatus(Status status, Throwable exception) {
        this.status = status;
        this.exception = exception;
    }

    /**
     * Get the name of destination entity.
     *
     * @return name the name of destination entity
     */
    public String getName() {
        return name;
    }

}
