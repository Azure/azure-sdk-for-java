// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

public class CustomIOHandler extends IOHandler {
    private final ClientLogger logger = new ClientLogger(CustomIOHandler.class);
    private final String connectionId;

    public CustomIOHandler(final String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public void onTransportClosed(Event event) {
        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        logger.info("onTransportClosed connectionId[{}], hostname[{}]",
            connectionId, (connection != null ? connection.getHostname() : NOT_APPLICABLE));

        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }

    @Override
    public void onUnhandled(Event event) {
        // logger.verbose("Unhandled event: {}, {}", event.getEventType(), event);

        // There appears to be some race condition where it's possible to get a null selector key in proton-j.
        try {
            super.onUnhandled(event);
        } catch (NullPointerException e) {
            logger.error("Exception occurred when handling event in super.", e);
        }
    }
}
