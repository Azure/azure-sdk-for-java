// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.health;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ServiceBus health details entity class.
 */
public class Instrumentation {

    private final String name;

    protected final AtomicBoolean started = new AtomicBoolean(false);

    protected Exception startException = null;

    public Instrumentation(String name) {
        this.name = name;
    }

    public boolean isDown() {
        return startException != null;
    }

    public boolean isUp() {
        return started.get();
    }

    public boolean isOutOfService() {
        return !started.get() && startException == null;
    }

    public void markStartedSuccessfully() {
        started.set(true);
    }

    public void markStartFailed(Exception e) {
        started.set(false);
        startException = e;
    }

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return started.get();
    }

    public Exception getStartException() {
        return startException;
    }

}
