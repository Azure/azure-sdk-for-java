// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.util.Objects;

/**
 * Base class that executes work on reactor.
 */
public class DispatchHandler extends BaseHandler {
    private final Runnable work;

    /**
     * Creates a handler that runs work on a {@link Reactor}.
     *
     * @param work The work to run on the {@link Reactor}.
     */
    public DispatchHandler(Runnable work) {
        this.work = Objects.requireNonNull(work, "'work' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTimerTask(Event e) {
        this.work.run();
    }
}
