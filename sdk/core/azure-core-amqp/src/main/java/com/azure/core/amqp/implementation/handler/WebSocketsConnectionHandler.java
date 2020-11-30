// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.impl.TransportInternal;

/**
 * Creates an AMQP connection using web sockets (port 443).
 */
public class WebSocketsConnectionHandler extends ConnectionHandler {
    static final int HTTPS_PORT = 443;

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
     * @param hostname Hostname to use for socket creation.
     * @param product The name of the product this connection handler is created for.
     * @param clientVersion The version of the client library creating the connection handler.
     * @param clientOptions provided by the user.
     */
    public WebSocketsConnectionHandler(final String connectionId, final String hostname, final String product,
        final String clientVersion, final SslDomain.VerifyMode verifyMode, final ClientOptions clientOptions) {
        super(connectionId, hostname, product, clientVersion, verifyMode, clientOptions);
    }

    @Override
    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        final String hostName = event.getConnection().getHostname();

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
    public int getProtocolPort() {
        return HTTPS_PORT;
    }

    @Override
    public int getMaxFrameSize() {
        return MAX_FRAME_SIZE;
    }
}
