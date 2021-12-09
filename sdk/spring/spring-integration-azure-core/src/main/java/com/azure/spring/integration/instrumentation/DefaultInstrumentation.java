// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.instrumentation;

/**
 *
 */
public class DefaultInstrumentation implements Instrumentation {

    private final String name;

    private final Type type;

    private boolean isRunning = false;

    private Throwable exception;

    /**
     * Constructor.
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
     * @return !isRunning
     */
    public boolean isDown() {
        return !isRunning;
    }

    /**
     * Check whether is up.
     *
     * @return isRunning
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
     * Get name.
     *
     * @return name the name
     */
    public String getName() {
        return name;
    }

}
