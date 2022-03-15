// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.HOSTNAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

/**
 * Handles transport related events.
 */
public class TransportHandler extends BaseHandler {
    private final ClientLogger logger;

    public TransportHandler(final String connectionId) {
        this.logger = new ClientLogger(TransportHandler.class, createContextWithConnectionId(connectionId));
    }

    @Override
    public void onTransportClosed(Event event) {
        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        logger.atInfo()
            .addKeyValue(HOSTNAME_KEY, connection != null ? connection.getHostname() : NOT_APPLICABLE)
            .log("onTransportClosed");

        // connection.getTransport returns null if already unbound.
        // We need to unbind the transport so that we do not leak memory.
        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }
}
