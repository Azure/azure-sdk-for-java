// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation.handler;

import com.azure.core.implementation.logging.ServiceLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;

import java.util.Objects;

/*
 * Base class that executes work on reactor.
 */
public class DispatchHandler extends BaseHandler {
    private final ServiceLogger logger = new ServiceLogger(DispatchHandler.class);
    private final Runnable work;

    public DispatchHandler(Runnable work) {
        Objects.requireNonNull(work);
        this.work = work;
    }

    public void onTimerTask(Event e) {
        logger.asTrace().log("Running task for event: %s", e);
        this.work.run();
    }
}
