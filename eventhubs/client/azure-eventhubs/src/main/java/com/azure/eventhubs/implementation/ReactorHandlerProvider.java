package com.azure.eventhubs.implementation;
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.amqp.TransportType;
import com.azure.eventhubs.implementation.handler.ConnectionHandler;
import com.azure.eventhubs.implementation.handler.SessionHandler;
import org.apache.qpid.proton.reactor.Reactor;

import java.time.Duration;
import java.util.Locale;

/**
 * Provides handlers for the various types of links.
 */
public class ReactorHandlerProvider {
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
    public ConnectionHandler createConnectionHandler(String connectionId, String hostname, TransportType transportType) {
        switch (transportType) {
            case AMQP:
                return new ConnectionHandler(connectionId, hostname);
            case AMQP_WEB_SOCKETS:
            default:
                throw new IllegalArgumentException(String.format(Locale.US, "This transport type '%s' is not supported yet.", transportType));
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
    public SessionHandler createSessionHandler(String connectionId, String host, String sessionName, Duration openTimeout) {
        return new SessionHandler(connectionId, host, sessionName, provider.getReactorDispatcher(), openTimeout);
    }
}
