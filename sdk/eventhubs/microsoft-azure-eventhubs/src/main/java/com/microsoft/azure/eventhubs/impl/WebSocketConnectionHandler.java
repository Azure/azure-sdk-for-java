// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketConnectionHandler extends ConnectionHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(WebSocketConnectionHandler.class);

    public WebSocketConnectionHandler(AmqpConnection amqpConnection, String connectionId,
        SslDomain.VerifyMode verifyMode) {
        super(amqpConnection, connectionId, verifyMode);
    }

    @Override
    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        final String hostName = event.getConnection().getHostname();

        final WebSocketImpl webSocket = new WebSocketImpl();
        webSocket.configure(
                hostName,
                "/$servicebus/websocket",
                "",
                0,
                "AMQPWSB10",
                null,
                null);

        transport.addTransportLayer(webSocket);

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("addWebsocketHandshake: hostname[" + hostName + "]");
        }

        super.addTransportLayers(event, transport);
    }

    @Override
    protected int getProtocolPort() {
        return ClientConstants.HTTPS_PORT;
    }

    @Override
    protected int getMaxFrameSize() {
        // This is the current limitation of https://github.com/Azure/qpid-proton-j-extensions
        // once, this library enables larger frames - this property can be removed.
        return 4 * 1024;
    }
}
