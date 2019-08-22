// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import java.util.Objects;

/**
 * Handler that sets the timeout period for waiting for Selectables.
 */
public class ReactorHandler extends BaseHandler {
    /**
     * The specified timeout period (in milliseconds) for one or more Reactor Selectables to become ready for a
     * send/receive operation.
     */
    private static final int REACTOR_IO_POLL_TIMEOUT = 20;

    private final ClientLogger logger = new ClientLogger(ReactorHandler.class);
    private final String name;

    public ReactorHandler(final String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Override
    public void onReactorInit(Event e) {
        logger.info("name[{}] reactor.onReactorInit", name);

        final Reactor reactor = e.getReactor();
        reactor.setTimeout(REACTOR_IO_POLL_TIMEOUT);
    }

    @Override
    public void onReactorFinal(Event e) {
        logger.info("name[{}] reactor.onReactorFinal", name);
    }
}
