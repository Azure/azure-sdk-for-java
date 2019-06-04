// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.reactor.Reactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ReactorHandler extends BaseHandler {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ReactorHandler.class);

    private final String name;

    private ReactorDispatcher reactorDispatcher;

    public ReactorHandler(final String name) {
        this.name = name;
    }

    public ReactorDispatcher getReactorDispatcher() {
        return this.reactorDispatcher;
    }

    // set needs to happen before starting reactorThread
    public void unsafeSetReactorDispatcher(final ReactorDispatcher reactorDispatcher) {
        this.reactorDispatcher = reactorDispatcher;
    }

    @Override
    public void onReactorInit(Event e) {
        TRACE_LOGGER.info(String.format(Locale.US, "name[%s] reactor.onReactorInit", this.name));

        final Reactor reactor = e.getReactor();
        reactor.setTimeout(ClientConstants.REACTOR_IO_POLL_TIMEOUT);
    }

    @Override
    public void onReactorFinal(Event e) {
        TRACE_LOGGER.info(String.format(Locale.US, "name[%s] reactor.onReactorFinal", this.name));
    }
}
