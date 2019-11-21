// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsProxyConnectionHandler;
import com.azure.core.util.logging.ClientLogger;
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
    public ConnectionHandler createConnectionHandler(String connectionId, String hostname,
            AmqpTransportType transportType, ProxyOptions proxyOptions) {
        switch (transportType) {
            case AMQP:
                return new ConnectionHandler(connectionId, hostname);
            case AMQP_WEB_SOCKETS:
                if (proxyOptions != null && proxyOptions.isProxyAddressConfigured()) {
                    return new WebSocketsProxyConnectionHandler(connectionId, hostname, proxyOptions);
                } else if (WebSocketsProxyConnectionHandler.shouldUseProxy(hostname)) {
                    logger.info("System default proxy configured for hostname '{}'. Using proxy.", hostname);
                    return new WebSocketsProxyConnectionHandler(connectionId, hostname,
                        ProxyOptions.SYSTEM_DEFAULTS);
                } else {
                    return new WebSocketsConnectionHandler(connectionId, hostname);
                }
            default:
                throw logger.logExceptionAsWarning(new IllegalArgumentException(String.format(Locale.US,
                    "This transport type '%s' is not supported.", transportType)));
        }
    }

    /**
     * Creates a new session handler with the given {@code connectionId}, {@code hostname}, and {@code sessionName}.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param hostname Host of the parent connection.
     * @param sessionName Name of the session.
     * @param openTimeout Duration to wait for the session to open.
     * @return A new {@link SessionHandler}.
     */
    public SessionHandler createSessionHandler(String connectionId, String hostname, String sessionName,
                                               Duration openTimeout) {
        return new SessionHandler(connectionId, hostname, sessionName, provider.getReactorDispatcher(), openTimeout);
    }

    /**
     * Creates a new link handler for sending messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param fullyQualifiedNamespace Fully qualified namespace of the parent connection.
     * @param senderName Name of the send link.
     * @return A new {@link SendLinkHandler}.
     */
    public SendLinkHandler createSendLinkHandler(String connectionId, String fullyQualifiedNamespace, String senderName,
                                                 String entityPath) {
        return new SendLinkHandler(connectionId, fullyQualifiedNamespace, senderName, entityPath);
    }

    /**
     * Creates a new link handler for receiving messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param fullyQualifiedNamespace Fully qualified namespace of the parent connection.
     * @param receiverName Name of the send link.
     * @return A new {@link ReceiveLinkHandler}.
     */
    public ReceiveLinkHandler createReceiveLinkHandler(String connectionId, String fullyQualifiedNamespace,
            String receiverName, String entityPath) {
        return new ReceiveLinkHandler(connectionId, fullyQualifiedNamespace, receiverName, entityPath);
    }
}
