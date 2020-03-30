// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

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
    private final String connectionId;

    public ReactorHandler(final String connectionId) {
        Objects.requireNonNull(connectionId);
        this.connectionId = connectionId;
    }

    @Override
    public void onReactorInit(Event e) {
        logger.info("connectionId[{}] reactor.onReactorInit", connectionId);

        final Reactor reactor = e.getReactor();
        reactor.setTimeout(REACTOR_IO_POLL_TIMEOUT);
    }

    @Override
    public void onReactorFinal(Event e) {
        logger.info("connectionId[{}] reactor.onReactorFinal. event: {}", connectionId, e);
    }
}
