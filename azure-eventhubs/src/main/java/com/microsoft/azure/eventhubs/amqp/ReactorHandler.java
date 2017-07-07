/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.amqp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

import com.microsoft.azure.eventhubs.ClientConstants;

public class ReactorHandler extends BaseHandler {

    private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.EVENTHUB_CLIENT_TRACE);

    private ReactorDispatcher reactorDispatcher;

    public ReactorDispatcher getReactorDispatcher() {
        return this.reactorDispatcher;
    }

    // set needs to happen before starting reactorThread
    public void unsafeSetReactorDispatcher(final ReactorDispatcher reactorDispatcher) {
        this.reactorDispatcher = reactorDispatcher;
    }

    @Override
    public void onReactorInit(Event e) {

        if (TRACE_LOGGER.isLoggable(Level.FINE)) {
            TRACE_LOGGER.log(Level.FINE, "reactor.onReactorInit");
        }

        final Reactor reactor = e.getReactor();
        reactor.setTimeout(ClientConstants.REACTOR_IO_POLL_TIMEOUT);
    }

    @Override
    public void onReactorFinal(Event e) {

        if (TRACE_LOGGER.isLoggable(Level.FINE)) {
            TRACE_LOGGER.log(Level.FINE, "reactor.onReactorFinal");
        }
    }
}
