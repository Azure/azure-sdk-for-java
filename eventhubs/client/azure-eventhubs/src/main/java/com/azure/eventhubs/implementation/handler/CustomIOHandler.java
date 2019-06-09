// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation.handler;

import com.azure.core.implementation.logging.ServiceLogger;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

public class CustomIOHandler extends IOHandler {
    private final ServiceLogger logger = new ServiceLogger(CustomIOHandler.class);
    private final String connectionId;

    public CustomIOHandler(final String connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public void onTransportClosed(Event event) {
        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        logger.asInfo().log("onTransportClosed name[{}], hostname[{}]",
            connectionId, (connection != null ? connection.getHostname() : "n/a"));

        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }

    @Override
    public void onUnhandled(Event event) {
        // logger.asVerbose().log("Unhandled event: {}, {}", event.getEventType(), event.toString());
        super.onUnhandled(event);
    }
}
