// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.HOSTNAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

public class GlobalIOHandler extends IOHandler {
    private final ClientLogger logger;

    public GlobalIOHandler(final String connectionId) {
        this.logger = new ClientLogger(GlobalIOHandler.class, createContextWithConnectionId(connectionId));
    }

    /**
     * Override the transport_closed event handling behavior of base IOHandler. The base IOHandler does the Transport
     * unbind even if the ConnectionHandler already did the unbind on the transport_error event. Each additional
     * unbinding reduces the Connection's reference count by one. Ideally, removing Transport must lower
     * the overall reference count by only one; else, the undesired reduction can lead to IllegalSateException.
     * By overriding the transport_closed event handling, this method performs unbind only if it is not already done.
     * Also, not doing unbind at least once will result in a memory leak.
     *
     * @param event the event description.
     */
    @Override
    public void onTransportClosed(Event event) {
        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        logger.atInfo()
            .addKeyValue(HOSTNAME_KEY, connection != null ? connection.getHostname() : NOT_APPLICABLE)
            .log("onTransportClosed");

        // connection.getTransport() returns null if the unbind is already done.
        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }
}
