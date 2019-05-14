// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class CustomIOHandler extends IOHandler {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(CustomIOHandler.class);

    private final String name;

    public CustomIOHandler(final String name) {
        this.name = name;
    }

    @Override
    public void onTransportClosed(Event event) {
        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onTransportClosed name[%s], hostname[%s]",
                    this.name, (connection != null ? connection.getHostname() : "n/a")));
        }

        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }
}
