// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.DeliverySettleMode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler2;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsProxyConnectionHandler;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.reactor.Reactor;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides handlers for the various types of links.
 */
public class ReactorHandlerProvider {
    private static final ClientLogger LOGGER = new ClientLogger(ReactorHandlerProvider.class);
    private final ConcurrentHashMap<String, AmqpMetricsProvider> metricsCache = new ConcurrentHashMap<>();
    private final Meter meter;

    private final ReactorProvider provider;

    /**
     * Creates a new instance with the reactor provider to handle {@link ReactorDispatcher ReactorDispatchers} to its
     * generated handlers.
     *
     * @param provider The provider that creates and manages {@link Reactor} instances.
     *
     * @throws NullPointerException If {@code provider} is {@code null}.
     * @deprecated use {@link ReactorHandlerProvider#ReactorHandlerProvider(ReactorProvider, Meter)} instead.
     */
    @Deprecated
    public ReactorHandlerProvider(ReactorProvider provider) {
        this(provider, null);
    }

    /**
     * Creates a new instance with the reactor provider to handle {@link ReactorDispatcher ReactorDispatchers} to its
     * generated handlers.
     *
     * @param provider The provider that creates and manages {@link Reactor} instances.
     * @param meter Instance of {@link Meter} to report metrics to.
     *
     * @throws NullPointerException If {@code provider} is {@code null}.
     */
    public ReactorHandlerProvider(ReactorProvider provider, Meter meter) {
        this.provider = Objects.requireNonNull(provider, "'provider' cannot be null.");
        this.meter = meter;
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

        AmqpMetricsProvider metricsProvider = getMetricProvider(options.getFullyQualifiedNamespace(), null);
        if (options.getTransportType() == AmqpTransportType.AMQP) {
            final SslPeerDetails peerDetails = Proton.sslPeerDetails(options.getHostname(), options.getPort());

            return new ConnectionHandler(connectionId, options, peerDetails, metricsProvider);
        }

        if (options.getTransportType() != AmqpTransportType.AMQP_WEB_SOCKETS) {
            throw LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                String.format(Locale.US, "This transport type '%s' is not supported.", options.getTransportType())));
        }

        final boolean isCustomEndpointConfigured = !options.getFullyQualifiedNamespace().equals(options.getHostname());
        final boolean isUserProxyConfigured
            = options.getProxyOptions() != null && options.getProxyOptions().isProxyAddressConfigured();
        final boolean isSystemProxyConfigured
            = WebSocketsProxyConnectionHandler.shouldUseProxy(options.getFullyQualifiedNamespace(), options.getPort());

        if (isUserProxyConfigured) {
            LOGGER.info("Using user configured proxy to connect to: '{}:{}'. Proxy: {}",
                options.getFullyQualifiedNamespace(), options.getPort(), options.getProxyOptions().getProxyAddress());

            final SslPeerDetails peerDetails = Proton.sslPeerDetails(options.getHostname(), options.getPort());

            return new WebSocketsProxyConnectionHandler(connectionId, options, options.getProxyOptions(), peerDetails,
                metricsProvider);
        } else if (isSystemProxyConfigured) {
            LOGGER.info("System default proxy configured for hostname:port '{}:{}'. Using proxy.",
                options.getFullyQualifiedNamespace(), options.getPort());

            final SslPeerDetails peerDetails = Proton.sslPeerDetails(options.getHostname(), options.getPort());

            return new WebSocketsProxyConnectionHandler(connectionId, options, ProxyOptions.SYSTEM_DEFAULTS,
                peerDetails, metricsProvider);
        }

        final SslPeerDetails peerDetails = isCustomEndpointConfigured
            ? Proton.sslPeerDetails(options.getHostname(), options.getPort())
            : Proton.sslPeerDetails(options.getFullyQualifiedNamespace(), options.getPort());

        return new WebSocketsConnectionHandler(connectionId, options, peerDetails, metricsProvider);
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

        return new SessionHandler(connectionId, hostname, sessionName, provider.getReactorDispatcher(), openTimeout,
            getMetricProvider(hostname, sessionName));
    }

    /**
     * Creates a new link handler for sending messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param hostname Fully qualified namespace of the parent connection.
     * @param senderName Name of the send link.
     * @param entityPath The relative path to the messaging entity streaming the messages.
     * @return A new {@link SendLinkHandler}.
     */
    public SendLinkHandler createSendLinkHandler(String connectionId, String hostname, String senderName,
        String entityPath) {
        return new SendLinkHandler(connectionId, hostname, senderName, entityPath,
            getMetricProvider(hostname, entityPath));
    }

    /**
     * Creates a new link handler for receiving messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param hostname Fully qualified namespace of the parent connection.
     * @param receiverName Name of the send link.
     * @param entityPath The relative path to the messaging entity streaming the messages.
     * @return A new {@link ReceiveLinkHandler}.
     */
    public ReceiveLinkHandler createReceiveLinkHandler(String connectionId, String hostname, String receiverName,
        String entityPath) {

        return new ReceiveLinkHandler(connectionId, hostname, receiverName, entityPath,
            getMetricProvider(hostname, entityPath));
    }

    /**
     * Creates a new v2 link handler for receiving messages.
     *
     * @param connectionId Identifier of the parent connection that created this session.
     * @param hostname Fully qualified namespace of the parent connection.
     * @param receiverName Name of the send link.
     * @param entityPath The relative path to the messaging entity streaming the messages.
     * @param deliverySettleMode Indicate how each {@link org.apache.qpid.proton.engine.Delivery} holding message should be settled.
     * @param includeDeliveryTagInMessage Indicate if the delivery tag should be included in the message.
     * @param dispatcher The dispatcher for handler to invoke any ProtonJ API call.
     * @param retryOptions The retry option user set while building the client.
     * @return A new {@link ReceiveLinkHandler2}.
     */
    public ReceiveLinkHandler2 createReceiveLinkHandler(String connectionId, String hostname, String receiverName,
        String entityPath, DeliverySettleMode deliverySettleMode, boolean includeDeliveryTagInMessage,
        ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions) {
        return new ReceiveLinkHandler2(connectionId, hostname, receiverName, entityPath, deliverySettleMode, dispatcher,
            retryOptions, includeDeliveryTagInMessage, getMetricProvider(hostname, entityPath));
    }

    /**
     * Returns cached {@link AmqpMetricsProvider} (or creates one) for given meter and entity.
     * It's recommended to keep returned value in instance variable and to avoid calling
     * this method extensively.
     */
    AmqpMetricsProvider getMetricProvider(String namespace, String entityPath) {
        if (meter != null && !meter.isEnabled()) {
            return AmqpMetricsProvider.noop();
        }

        return metricsCache.computeIfAbsent(namespace + (entityPath == null ? "" : "/" + entityPath),
            ignored -> new AmqpMetricsProvider(meter, namespace, entityPath));
    }
}
