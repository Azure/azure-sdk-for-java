/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;

public class ReactorHandler extends BaseHandler {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReactorHandler.class);

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

        TRACE_LOGGER.info("reactor.onReactorInit");

        final Reactor reactor = e.getReactor();
        reactor.setTimeout(ClientConstants.REACTOR_IO_POLL_TIMEOUT);
    }

    @Override
    public void onReactorFinal(Event e) {

        TRACE_LOGGER.info("reactor.onReactorFinal");
    }
}
