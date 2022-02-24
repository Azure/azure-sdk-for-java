// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.implementation;

import com.azure.spring.integration.instrumentation.Instrumentation;

/**
 *
 */
public class DefaultInstrumentation implements Instrumentation {

    private final String name;

    private final Type type;

    private boolean isRunning = false;

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

    /**
     * Check whether is down.
     *
     * @return true if the status is down,false otherwise
     */
    public boolean isDown() {
        return !isRunning;
    }

    /**
     * Check whether is up.
     *
     * @return false if the status is up,true otherwise
     */
    public boolean isUp() {
        return isRunning;
    }

    @Override
    public void markDown(Throwable exception) {
        this.isRunning = false;
        this.exception = exception;
    }

    @Override
    public void markUp() {
        this.isRunning = true;
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
