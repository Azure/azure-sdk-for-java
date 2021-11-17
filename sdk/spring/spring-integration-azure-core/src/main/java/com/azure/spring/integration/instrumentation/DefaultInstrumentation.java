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

    public DefaultInstrumentation(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    public boolean isDown() {
        return !isRunning;
    }

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

    public String getName() {
        return name;
    }

}
