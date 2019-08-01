// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.TransportType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.messaging.eventhubs.implementation.handler.ReceiveLinkHandler;
import com.azure.messaging.eventhubs.implementation.handler.SendLinkHandler;
import com.azure.messaging.eventhubs.implementation.handler.SessionHandler;
import com.azure.messaging.eventhubs.implementation.handler.WebSocketsConnectionHandler;
import org.apache.qpid.proton.reactor.Reactor;

import java.time.Duration;
import java.util.Locale;

/**
 * Provides handlers for the various types of links.
 */
public class ReactorHandlerProvider {
    private final ClientLogger logger = new ClientLogger(ReactorHandlerProvider.class);
    private final ReactorProvider provider;

    /**
     * Creates a new instance with the reactor provider to handle {@link ReactorDispatcher ReactorDispatchers} to its
     * generated handlers.
     *
     * @param provider The provider that creates and manages {@link Reactor} instances.
     */
    public ReactorHandlerProvider(ReactorProvider provider) {
        this.provider = provider;
    }

    /**
     * Creates a new connection handler with the given {@code connectionId} and {@code hostname}.
     *
     * @param connectionId Identifier associated with this connection.
     * @param hostname Host for the connection handler.
     * @param transportType Transport type used for the connection.
     * @return A new {@link ConnectionHandler}.
     */
    ConnectionHandler createConnectionHandler(String connectionId, String hostname, TransportType transportType) {
        switch (transportType) {
            case AMQP:
                return new ConnectionHandler(connectionId, hostname);
            case AMQP_WEB_SOCKETS:
                return new WebSocketsConnectionHandler(connectionId, hostname);
            default:
                logger.logAndThrow(new IllegalArgumentException(String.format(Locale.US, "This transport type '%s' is not supported.", transportType)));
                return null;
        }
    }

    /**
     * Creates a new session handler with the given {@code connectionId}, {@code host}, and {@code sessionName}.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param host Host of the parent connection.
     * @param sessionName Name of the session.
     * @param openTimeout Duration to wait for the session to open.
     * @return A new {@link SessionHandler}.
     */
    SessionHandler createSessionHandler(String connectionId, String host, String sessionName, Duration openTimeout) {
        return new SessionHandler(connectionId, host, sessionName, provider.getReactorDispatcher(), openTimeout);
    }

    /**
     * Creates a new link handler for sending messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param host Host of the parent connection.
     * @param senderName Name of the send link.
     * @return A new {@link SendLinkHandler}.
     */
    SendLinkHandler createSendLinkHandler(String connectionId, String host, String senderName, String entityPath) {
        return new SendLinkHandler(connectionId, host, senderName, entityPath);
    }

    /**
     * Creates a new link handler for receiving messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param host Host of the parent connection.
     * @param receiverName Name of the send link.
     * @return A new {@link ReceiveLinkHandler}.
     */
    ReceiveLinkHandler createReceiveLinkHandler(String connectionId, String host, String receiverName, String entityPath) {
        return new ReceiveLinkHandler(connectionId, host, receiverName, entityPath);
    }
}
