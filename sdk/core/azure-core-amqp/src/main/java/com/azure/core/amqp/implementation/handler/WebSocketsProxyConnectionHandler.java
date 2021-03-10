// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpErrorCode;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import org.apache.qpid.proton.amqp.transport.ConnectionError;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Creates an AMQP connection using web sockets and connects through a proxy.
 */
public class WebSocketsProxyConnectionHandler extends WebSocketsConnectionHandler {
    private static final String HTTPS_URI_FORMAT = "https://%s:%s";

    private final ClientLogger logger = new ClientLogger(WebSocketsProxyConnectionHandler.class);
    private final InetSocketAddress connectionHostname;
    private final ProxyOptions proxyOptions;
    private final String fullyQualifiedNamespace;
    private final String amqpBrokerHostname;

    /**
     * Creates a handler that handles proton-j's connection through a proxy using web sockets.
     * The hostname of the proxy is exposed in {@link #getHostname()}.
     *
     * @param connectionId Identifier for this connection.
     * @param connectionOptions Options used when creating the connection.
     * @param proxyOptions The options to use for proxy.
     *
     * @throws NullPointerException if {@code amqpHostname} or {@code proxyConfiguration} is null.
     * @throws IllegalStateException if a proxy address is unavailable for the given {@code proxyOptions}.
     */
    public WebSocketsProxyConnectionHandler(String connectionId, ConnectionOptions connectionOptions,
        ProxyOptions proxyOptions, SslPeerDetails peerDetails) {
        super(connectionId, connectionOptions, peerDetails);

        this.proxyOptions = Objects.requireNonNull(proxyOptions, "'proxyConfiguration' cannot be null.");
        this.fullyQualifiedNamespace = connectionOptions.getFullyQualifiedNamespace();
        this.amqpBrokerHostname = connectionOptions.getFullyQualifiedNamespace() + ":" + connectionOptions.getPort();

        if (proxyOptions.isProxyAddressConfigured()) {
            this.connectionHostname = (InetSocketAddress) proxyOptions.getProxyAddress().address();
        } else {
            final URI serviceUri = createURI(connectionOptions.getHostname(), connectionOptions.getPort());
            final ProxySelector proxySelector = ProxySelector.getDefault();
            if (proxySelector == null) {
                throw logger.logExceptionAsError(new IllegalStateException("ProxySelector should not be null."));
            }

            final List<Proxy> proxies = proxySelector.select(serviceUri);
            if (!isProxyAddressLegal(proxies)) {
                final String formatted = String.format("No proxy address found for: '%s'. Available: %s.",
                    serviceUri, proxies.stream().map(Proxy::toString).collect(Collectors.joining(", ")));

                throw logger.logExceptionAsError(new IllegalStateException(formatted));
            }

            final Proxy proxy = proxies.get(0);
            this.connectionHostname = (InetSocketAddress) proxy.address();
        }
    }

    /**
     * Looks through system defined proxies to see if one should be used for connecting to the message broker.
     *
     * @param hostname Hostname for the AMQP connection.
     * @param port Port to connect to.
     *
     * @return {@code true} if a proxy should be used to connect to the AMQP message broker and null otherwise.
     */
    public static boolean shouldUseProxy(final String hostname, final int port) {
        Objects.requireNonNull(hostname, "'hostname' cannot be null.");

        final URI uri = createURI(hostname, port);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            return false;
        }

        final List<Proxy> proxies = proxySelector.select(uri);
        return isProxyAddressLegal(proxies);
    }

    /**
     * Gets the hostname for the proxy.
     *
     * @return The hostname for the proxy.
     */
    @Override
    public String getHostname() {
        return connectionHostname.getHostString();
    }

    /**
     * Gets the port for the proxy.
     *
     * @return The port for the proxy.
     */
    @Override
    public int getProtocolPort() {
        return connectionHostname.getPort();
    }

    @Override
    public void onTransportError(Event event) {
        super.onTransportError(event);

        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();
        if (connection == null || transport == null) {
            logger.verbose("connectionId[{}] There is no connection or transport associated with error. Event: {}",
                event);
            return;
        }

        final ErrorCondition errorCondition = transport.getCondition();
        if (errorCondition == null || !(errorCondition.getCondition().equals(ConnectionError.FRAMING_ERROR)
            || errorCondition.getCondition().equals(AmqpErrorCode.PROTON_IO_ERROR))) {
            logger.verbose("connectionId[{}] There is no error condition and these are not framing errors. Error: {}",
                errorCondition);
            return;
        }

        final String hostname = event.getReactor().getConnectionAddress(connection);

        // If the proxy is not configured, or we are not connected to a host yet.
        if (proxyOptions == null || CoreUtils.isNullOrEmpty(hostname)) {
            logger.verbose("connectionId[{}] Proxy is not configured and there is no host connected. Error: {}",
                errorCondition);
            return;
        }

        final String[] hostNameParts = hostname.split(":");
        if (hostNameParts.length != 2) {
            logger.warning("connectionId[{}] Invalid hostname: {}", getConnectionId(), hostname);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(hostNameParts[1]);
        } catch (NumberFormatException ignore) {
            logger.warning("connectionId[{}] Invalid port number: {}", getConnectionId(), hostNameParts[1]);
            return;
        }

        // since proton library communicates all errors based on amqp-error-condition
        // it swallows the IOException and translates it to proton-io errorCode
        // we reconstruct the IOException in this case - but, callstack is lost
        final IOException ioException = new IOException(errorCondition.getDescription());
        final URI url = createURI(fullyQualifiedNamespace, port);
        final InetSocketAddress address = new InetSocketAddress(hostNameParts[0], port);

        logger.error("connectionId[{}] Failed to connect to url: '{}', proxy host: '{}'",
            getConnectionId(), url, address.getHostString(), ioException);

        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector != null) {
            proxySelector.connectFailed(url, address, ioException);
        }
    }

    @Override
    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        super.addTransportLayers(event, transport);

        // Checking that the proxy configuration is not null and not equal to the system defaults option.
        final ProxyImpl proxy = proxyOptions != null
            && !(proxyOptions == ProxyOptions.SYSTEM_DEFAULTS)
            ? new ProxyImpl(getProtonConfiguration())
            : new ProxyImpl();

        // host name used to create proxy connect request must contain a port number.
        // after creating the socket to proxy
        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        proxy.configure(amqpBrokerHostname, null, proxyHandler, transport);

        transport.addTransportLayer(proxy);

        logger.info("connectionId[{}] addProxyHandshake: hostname[{}]", getConnectionId(), amqpBrokerHostname);
    }

    private com.microsoft.azure.proton.transport.proxy.ProxyConfiguration getProtonConfiguration() {
        final com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType type =
            getProtonAuthType(proxyOptions.getAuthentication());
        final String username = proxyOptions.hasUserDefinedCredentials()
            ? proxyOptions.getCredential().getUserName()
            : null;
        final String password = proxyOptions.hasUserDefinedCredentials()
            ? new String(proxyOptions.getCredential().getPassword())
            : null;

        return new com.microsoft.azure.proton.transport.proxy.ProxyConfiguration(type,
            proxyOptions.getProxyAddress(), username, password);
    }

    private com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType getProtonAuthType(
        ProxyAuthenticationType type) {
        switch (type) {
            case DIGEST:
                return com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType.DIGEST;
            case BASIC:
                return com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType.BASIC;
            case NONE:
                return com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType.NONE;
            default:
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                    "connectionId[%s]: This authentication type is unknown: %s", getConnectionId(), type.name())));
        }
    }

    private static URI createURI(final String hostname, final int port) {
        return URI.create(String.format(Locale.ROOT, HTTPS_URI_FORMAT, hostname, port));
    }

    /**
     * This always selects the first proxy in the list instead of going through all the available ones.
     *
     * @param proxies List of proxies available.
     *
     * @return {@code true} if the first proxy in the list is an HTTP proxy and is an IP address.
     */
    private static boolean isProxyAddressLegal(final List<Proxy> proxies) {
        // we look only at the first proxy in the list
        // if the proxy can be translated to InetSocketAddress
        // only then - can we parse it to hostName and Port
        // which is required by qpid-proton-j library reactor.connectToHost() API
        return proxies != null
            && !proxies.isEmpty()
            && proxies.get(0).type() == Proxy.Type.HTTP
            && proxies.get(0).address() != null
            && proxies.get(0).address() instanceof InetSocketAddress;
    }
}
