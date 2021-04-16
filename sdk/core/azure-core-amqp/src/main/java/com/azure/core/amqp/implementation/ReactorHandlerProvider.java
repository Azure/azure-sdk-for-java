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
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.reactor.Reactor;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

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
     *
     * @throws NullPointerException If {@code provider} is {@code null}.
     */
    public ReactorHandlerProvider(ReactorProvider provider) {
        this.provider = Objects.requireNonNull(provider, "'provider' cannot be null.");
    }

    /**
     * Creates a new connection handler with the given {@code connectionId} and {@code hostname}.
     *
     * @param connectionId Identifier associated with this connection.
     * @param options Options for the connection.
     * @return A new {@link ConnectionHandler}.
     *
     * @throws NullPointerException If {@code connectionId}, {@code productName}, {@code clientVersion},
     *      {@code options} is {@code null}.
     */
    public ConnectionHandler createConnectionHandler(String connectionId, ConnectionOptions options) {
        Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        if (options.getTransportType() == AmqpTransportType.AMQP) {
            final SslPeerDetails peerDetails = Proton.sslPeerDetails(options.getHostname(), options.getPort());

            return new ConnectionHandler(connectionId, options, peerDetails);
        }

        if (options.getTransportType() != AmqpTransportType.AMQP_WEB_SOCKETS) {
            throw logger.logExceptionAsWarning(new IllegalArgumentException(String.format(Locale.US,
                "This transport type '%s' is not supported.", options.getTransportType())));
        }

        final boolean isCustomEndpointConfigured = !options.getFullyQualifiedNamespace().equals(options.getHostname());
        final boolean isUserProxyConfigured = options.getProxyOptions() != null
            && options.getProxyOptions().isProxyAddressConfigured();
        final boolean isSystemProxyConfigured = WebSocketsProxyConnectionHandler.shouldUseProxy(
            options.getFullyQualifiedNamespace(), options.getPort());

        // TODO (conniey): See if we this is supported later on.
        if (isCustomEndpointConfigured && (isUserProxyConfigured || isSystemProxyConfigured)) {
            throw logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "Unable to proxy connection to custom endpoint. Custom endpoint: %s. Proxy settings: %s. "
                    + "Namespace: %s", options.getHostname(), options.getProxyOptions().getProxyAddress(),
                options.getFullyQualifiedNamespace())));
        }

        if (isUserProxyConfigured) {
            logger.info("Using user configured proxy to connect to: '{}:{}'. Proxy: {}",
                options.getFullyQualifiedNamespace(), options.getPort(), options.getProxyOptions().getProxyAddress());

            final SslPeerDetails peerDetails = Proton.sslPeerDetails(options.getHostname(), options.getPort());

            return new WebSocketsProxyConnectionHandler(connectionId, options, options.getProxyOptions(), peerDetails);
        } else if (isSystemProxyConfigured) {
            logger.info("System default proxy configured for hostname:port '{}:{}'. Using proxy.",
                options.getFullyQualifiedNamespace(), options.getPort());

            final SslPeerDetails peerDetails = Proton.sslPeerDetails(options.getHostname(), options.getPort());

            return new WebSocketsProxyConnectionHandler(connectionId, options, ProxyOptions.SYSTEM_DEFAULTS,
                peerDetails);
        }

        final SslPeerDetails peerDetails = isCustomEndpointConfigured
            ? Proton.sslPeerDetails(options.getHostname(), options.getPort())
            : Proton.sslPeerDetails(options.getFullyQualifiedNamespace(), options.getPort());

        return new WebSocketsConnectionHandler(connectionId, options, peerDetails);
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

        return new SessionHandler(connectionId, hostname, sessionName, provider.getReactorDispatcher(),
            openTimeout);
    }

    /**
     * Creates a new link handler for sending messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param hostname Fully qualified namespace of the parent connection.
     * @param senderName Name of the send link.
     *
     * @return A new {@link SendLinkHandler}.
     */
    public SendLinkHandler createSendLinkHandler(String connectionId, String hostname,
        String senderName, String entityPath) {
        return new SendLinkHandler(connectionId, hostname, senderName, entityPath);
    }

    /**
     * Creates a new link handler for receiving messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param hostname Fully qualified namespace of the parent connection.
     * @param receiverName Name of the send link.
     * @return A new {@link ReceiveLinkHandler}.
     */
    public ReceiveLinkHandler createReceiveLinkHandler(String connectionId, String hostname,
        String receiverName, String entityPath) {

        return new ReceiveLinkHandler(connectionId, hostname, receiverName, entityPath);
    }
}
