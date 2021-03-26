// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.impl.TransportInternal;

/**
 * Creates an AMQP connection using web sockets (port 443).
 */
public class WebSocketsConnectionHandler extends ConnectionHandler {
    public static final int HTTPS_PORT = 443;

    // This is the current limitation of https://github.com/Azure/qpid-proton-j-extensions.
    // Once this library enables larger frames - this property can be removed.
    static final int MAX_FRAME_SIZE =  4 * 1024;

    private static final String SOCKET_PATH = "/$servicebus/websocket";
    private static final String PROTOCOL = "AMQPWSB10";
    private final ClientLogger logger = new ClientLogger(WebSocketsConnectionHandler.class);

    /**
     * Creates a handler that handles proton-j's connection events using web sockets.
     *
     * @param connectionId Identifier for this connection.
     * @param connectionOptions Options used when creating the connection.
     */
    public WebSocketsConnectionHandler(String connectionId, ConnectionOptions connectionOptions,
        SslPeerDetails peerDetails) {
        super(connectionId, connectionOptions, peerDetails);
    }

    /**
     * Adds a web sockets layer before adding additional connection layers (ie. SSL).
     *
     * @param event The proton-j event.
     * @param transport Transport to add layers to.
     */
    @Override
    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        final String hostName = event.getConnection().getHostname();

        logger.info("Adding web socket layer");
        final WebSocketImpl webSocket = new WebSocketImpl();
        webSocket.configure(
            hostName,
            SOCKET_PATH,
            "",
            0,
            PROTOCOL,
            null,
            null);

        transport.addTransportLayer(webSocket);

        logger.verbose("connectionId[{}] Adding web sockets transport layer for hostname[{}]",
            getConnectionId(), hostName);

        super.addTransportLayers(event, transport);
    }

    @Override
    public int getMaxFrameSize() {
        return MAX_FRAME_SIZE;
    }
}
