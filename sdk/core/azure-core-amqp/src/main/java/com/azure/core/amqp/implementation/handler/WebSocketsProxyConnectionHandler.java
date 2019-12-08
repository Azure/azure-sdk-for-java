// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.AmqpErrorCode;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ConnectionError;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Creates an AMQP connection using web sockets (port 443) and connects through a proxy.
 */
public class WebSocketsProxyConnectionHandler extends WebSocketsConnectionHandler {
    private static final String HTTPS_URI_FORMAT = "https://%s:%s";
    private static final String PROXY_SELECTOR_HAS_BEEN_MODIFIED = "ProxySelector has been modified.";

    private final ClientLogger logger = new ClientLogger(WebSocketsProxyConnectionHandler.class);
    private final String amqpHostname;
    private final String remoteHost;
    private final ProxyOptions proxyOptions;

    /**
     * Creates a handler that handles proton-j's connection through a proxy using web sockets.
     *
     * @param connectionId Identifier for this connection.
     * @param amqpHostname Hostname of the AMQP message broker. The hostname of the proxy is exposed in {@link
     *     #getHostname()}.
     * @throws NullPointerException if {@code amqpHostname} or {@code proxyConfiguration} is null.
     */
    public WebSocketsProxyConnectionHandler(String connectionId, String amqpHostname,
                                            ProxyOptions proxyOptions) {
        super(connectionId, amqpHostname);
        this.amqpHostname = Objects.requireNonNull(amqpHostname, "'amqpHostname' cannot be null.");
        this.proxyOptions = Objects.requireNonNull(proxyOptions, "'proxyConfiguration' cannot be null.");
        this.remoteHost = amqpHostname + ":" + HTTPS_PORT;
    }

    /**
     * Looks through system defined proxies to see if one should be used for connecting to the message broker.
     *
     * @param hostname Hostname for the AMQP message broker.
     *
     * @return {@code true} if a proxy should be used to connect to the AMQP message broker and null otherwise.
     */
    public static boolean shouldUseProxy(final String hostname) {
        Objects.requireNonNull(hostname, "'hostname' cannot be null.");

        final URI uri = createURI(hostname, HTTPS_PORT);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            return false;
        }

        final List<Proxy> proxies = proxySelector.select(uri);
        return isProxyAddressLegal(proxies);
    }

    @Override
    public void onConnectionInit(Event event) {
        final Connection connection = event.getConnection();
        logger.info("onConnectionInit host[{}], connectionId[{}]", remoteHost, getConnectionId());

        connection.setHostname(remoteHost);
        connection.setContainer(getConnectionId());

        final Map<Symbol, Object> properties = new HashMap<>();
        getConnectionProperties().forEach((key, value) -> properties.put(Symbol.getSymbol(key), value));

        connection.setProperties(properties);
        connection.open();
    }

    @Override
    public String getHostname() {
        final InetSocketAddress socketAddress = getProxyAddress();
        return socketAddress.getHostString();
    }

    @Override
    public int getProtocolPort() {
        final InetSocketAddress socketAddress = getProxyAddress();
        return socketAddress.getPort();
    }

    @Override
    public void onTransportError(Event event) {
        super.onTransportError(event);

        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();
        if (connection == null || transport == null) {
            return;
        }

        final ErrorCondition errorCondition = transport.getCondition();
        if (errorCondition == null || !(errorCondition.getCondition().equals(ConnectionError.FRAMING_ERROR)
            || errorCondition.getCondition().equals(AmqpErrorCode.PROTON_IO_ERROR))) {
            return;
        }

        final String hostName = event.getReactor().getConnectionAddress(connection);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        final boolean isProxyConfigured = proxySelector != null
            || (proxyOptions != null && proxyOptions.isProxyAddressConfigured());

        if (!isProxyConfigured || CoreUtils.isNullOrEmpty(hostName)) {
            return;
        }

        final String[] hostNameParts = hostName.split(":");
        if (hostNameParts.length != 2) {
            logger.warning("Invalid hostname: {}", hostName);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(hostNameParts[1]);
        } catch (NumberFormatException ignore) {
            logger.warning("Invalid port number: {}", hostNameParts[1]);
            return;
        }

        // since proton library communicates all errors based on amqp-error-condition
        // it swallows the IOException and translates it to proton-io errorCode
        // we reconstruct the IOException in this case - but, callstack is lost
        final IOException ioException = new IOException(errorCondition.getDescription());
        final URI url = createURI(amqpHostname, HTTPS_PORT);
        final InetSocketAddress address = new InetSocketAddress(hostNameParts[0], port);

        logger.error(String.format("Failed to connect to url: '%s', proxy host: '%s'",
            url, address.getHostString()), ioException);

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

        // host name used to create proxy connect request
        // after creating the socket to proxy
        final String hostname = event.getConnection().getHostname();
        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        proxy.configure(hostname, null, proxyHandler, transport);

        transport.addTransportLayer(proxy);

        logger.info("addProxyHandshake: hostname[{}]", hostname);
    }

    private InetSocketAddress getProxyAddress() {
        if (proxyOptions != null && proxyOptions.isProxyAddressConfigured()) {
            return (InetSocketAddress) proxyOptions.getProxyAddress().address();
        }

        final URI serviceUri = createURI(amqpHostname, HTTPS_PORT);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            throw logger.logExceptionAsError(new IllegalStateException(PROXY_SELECTOR_HAS_BEEN_MODIFIED));
        }

        final List<Proxy> proxies = proxySelector.select(serviceUri);
        if (!isProxyAddressLegal(proxies)) {
            throw logger.logExceptionAsError(new IllegalStateException(PROXY_SELECTOR_HAS_BEEN_MODIFIED));
        }

        final Proxy proxy = proxies.get(0);
        return (InetSocketAddress) proxy.address();
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
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "This authentication type is unknown:" + type.name()));
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
