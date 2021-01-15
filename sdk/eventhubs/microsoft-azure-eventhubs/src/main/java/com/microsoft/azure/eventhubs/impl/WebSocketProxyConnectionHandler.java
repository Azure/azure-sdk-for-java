// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.ProxyConfiguration;
import com.microsoft.azure.proton.transport.proxy.ProxyAuthenticationType;
import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;
import org.apache.qpid.proton.amqp.transport.ConnectionError;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.List;
import java.util.Objects;

public class WebSocketProxyConnectionHandler extends WebSocketConnectionHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(WebSocketProxyConnectionHandler.class);
    private static final String PROXY_SELECTOR_HAS_BEEN_MODIFIED = "ProxySelector has been modified.";
    private final ProxyConfiguration proxyConfiguration;

    public static Boolean shouldUseProxy(final String hostName) {
        Objects.requireNonNull(hostName);

        final URI uri = createURIFromHostNamePort(hostName, ClientConstants.HTTPS_PORT);

        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            return false;
        }

        final List<Proxy> proxies = proxySelector.select(uri);
        return isProxyAddressLegal(proxies);
    }

    /**
     * Creates a WebSocket proxy connection handler for the {@code amqpConnection} and {@code proxyConfiguration}.
     *
     * @param amqpConnection AMQP connection to the service.
     * @param proxyConfiguration Required. Proxy configuration to use.
     * @throws NullPointerException if {@code proxyConfiguration} is {@code null}.
     */
    public WebSocketProxyConnectionHandler(AmqpConnection amqpConnection, String connectionId,
        SslDomain.VerifyMode verifyMode, ProxyConfiguration proxyConfiguration) {
        super(amqpConnection, connectionId, verifyMode);

        this.proxyConfiguration = proxyConfiguration;
    }

    @Override
    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        super.addTransportLayers(event, transport);

        final ProxyImpl proxy = proxyConfiguration != null
            ? new ProxyImpl(getProtonConfiguration(proxyConfiguration))
            : new ProxyImpl();

        // host name used to create proxy connect request
        // after creating the socket to proxy
        final String hostName = event.getConnection().getHostname();
        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        proxy.configure(hostName, null, proxyHandler, transport);

        transport.addTransportLayer(proxy);

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("addProxyHandshake: hostname[" + hostName + "]");
        }
    }

    @Override
    protected void notifyTransportErrors(final Event event) {
        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();
        if (connection == null || transport == null) {
            return;
        }

        final ErrorCondition errorCondition = transport.getCondition();
        final String hostName = event.getReactor().getConnectionAddress(connection);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        final boolean isProxyConfigured = proxySelector != null
            || (proxyConfiguration != null && proxyConfiguration.isProxyAddressConfigured());

        if (errorCondition == null
                || !(errorCondition.getCondition().equals(ConnectionError.FRAMING_ERROR)
                        || errorCondition.getCondition().equals(AmqpErrorCode.PROTON_IO_ERROR))
                || !isProxyConfigured
                || StringUtil.isNullOrEmpty(hostName)) {
            return;
        }

        final String[] hostNameParts = hostName.split(":");
        if (hostNameParts.length != 2) {
            return;
        }

        int port;
        try {
            port = Integer.parseInt(hostNameParts[1]);
        } catch (NumberFormatException ignore) {
            return;
        }

        final IOException ioException = reconstructIOException(errorCondition);
        final URI url = createURIFromHostNamePort(this.getAmqpConnection().getHostName(), this.getProtocolPort());
        final InetSocketAddress address = new InetSocketAddress(hostNameParts[0], port);

        if (TRACE_LOGGER.isErrorEnabled()) {
            TRACE_LOGGER.error(String.format("Failed to connect to url: '%s', proxy host: '%s'", url.toString(), address.getHostString()), ioException);
        }

        if (proxySelector != null) {
            proxySelector.connectFailed(url, address, ioException);
        }
    }

    @Override
    public String getRemoteHostName() {
        final InetSocketAddress socketAddress = getProxyAddress();
        return socketAddress.getHostString();
    }

    @Override
    public int getRemotePort() {
        final InetSocketAddress socketAddress = getProxyAddress();
        return socketAddress.getPort();
    }

    private InetSocketAddress getProxyAddress() {
        if (proxyConfiguration != null && proxyConfiguration.isProxyAddressConfigured()) {
            return (InetSocketAddress) proxyConfiguration.proxyAddress().address();
        }

        final URI serviceUri = createURIFromHostNamePort(
                this.getAmqpConnection().getHostName(),
                this.getProtocolPort());
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            throw new IllegalStateException(PROXY_SELECTOR_HAS_BEEN_MODIFIED);
        }

        final List<Proxy> proxies = proxySelector.select(serviceUri);
        if (!isProxyAddressLegal(proxies)) {
            throw new IllegalStateException(PROXY_SELECTOR_HAS_BEEN_MODIFIED);
        }

        final Proxy proxy = proxies.get(0);
        return (InetSocketAddress) proxy.address();
    }

    private static URI createURIFromHostNamePort(final String hostName, final int port) {
        return URI.create(String.format(ClientConstants.HTTPS_URI_FORMAT, hostName, port));
    }

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

    private static IOException reconstructIOException(ErrorCondition errorCondition) {
        // since proton library communicates all errors based on amqp-error-condition
        // it swallows the IOException and translates it to proton-io errorCode
        // we reconstruct the IOException in this case - but, callstack is lost
        return new IOException(errorCondition.getDescription());
    }

    private static com.microsoft.azure.proton.transport.proxy.ProxyConfiguration getProtonConfiguration(
        ProxyConfiguration configuration) {
        final ProxyAuthenticationType type = configuration.authentication() != null
            ? getProtonAuthenticationType(configuration.authentication())
            : ProxyAuthenticationType.NONE;
        final String username = configuration.hasUserDefinedCredentials()
            ? configuration.credentials().getUserName()
            : null;
        final String password = configuration.hasUserDefinedCredentials()
            ? new String(configuration.credentials().getPassword())
            : null;

        return new com.microsoft.azure.proton.transport.proxy.ProxyConfiguration(type, configuration.proxyAddress(), username, password);
    }

    private static ProxyAuthenticationType getProtonAuthenticationType(ProxyConfiguration.ProxyAuthenticationType type) {
        switch (type) {
            case DIGEST:
                return ProxyAuthenticationType.DIGEST;
            case BASIC:
                return ProxyAuthenticationType.BASIC;
            case NONE:
                return ProxyAuthenticationType.NONE;
            default:
                throw new IllegalArgumentException("This authentication type is unknown:" + type.name());
        }
    }
}
