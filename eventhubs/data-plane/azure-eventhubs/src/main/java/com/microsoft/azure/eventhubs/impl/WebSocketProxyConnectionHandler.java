/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;

import org.apache.qpid.proton.amqp.transport.ConnectionError;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketProxyConnectionHandler extends WebSocketConnectionHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(WebSocketProxyConnectionHandler.class);
    private final String proxySelectorModifiedError = "ProxySelector has been modified.";

    public static Boolean shouldUseProxy(final String hostName) {
        final URI uri = createURIFromHostNamePort(hostName, ClientConstants.HTTPS_PORT);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            return false;
        }

        final List<Proxy> proxies = proxySelector.select(uri);
        return isProxyAddressLegal(proxies);
    }

    public WebSocketProxyConnectionHandler(AmqpConnection amqpConnection) {
        super(amqpConnection);
    }

    @Override
    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        super.addTransportLayers(event, transport);

        final ProxyImpl proxy = new ProxyImpl();

        // host name used to create proxy connect request
        // after creating the socket to proxy
        final String hostName = event.getConnection().getHostname();
        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        final Map<String, String> proxyHeader = getAuthorizationHeader();
        proxy.configure(hostName, proxyHeader, proxyHandler, transport);

        transport.addTransportLayer(proxy);

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("addProxyHandshake: hostname[" + hostName +"]");
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

        if (errorCondition == null
                || !(errorCondition.getCondition().equals(ConnectionError.FRAMING_ERROR)
                        || errorCondition.getCondition().equals(AmqpErrorCode.PROTON_IO_ERROR))
                || proxySelector == null
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
        } catch (NumberFormatException ignore){
            return;
        }

        final IOException ioException = reconstructIOException(errorCondition);
        proxySelector.connectFailed(
                createURIFromHostNamePort(this.getAmqpConnection().getHostName(), this.getProtocolPort()),
                new InetSocketAddress(hostNameParts[0], port),
                ioException);
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

    private Map<String, String> getAuthorizationHeader() {
        final PasswordAuthentication authentication = Authenticator.requestPasswordAuthentication(
                getRemoteHostName(),
                null,
                getRemotePort(),
                null,
                null,
                "http",
                null,
                Authenticator.RequestorType.PROXY);
        if (authentication == null) {
            return null;
        }

        final String proxyUserName = authentication.getUserName();
        final String proxyPassword = authentication.getPassword() != null
                ? new String(authentication.getPassword())
                : null;
        if (StringUtil.isNullOrEmpty(proxyUserName)
                || StringUtil.isNullOrEmpty(proxyPassword)) {
            return null;
        }

        final HashMap<String, String> proxyAuthorizationHeader = new HashMap<>();
        // https://tools.ietf.org/html/rfc7617
        final String usernamePasswordPair = proxyUserName + ":" + proxyPassword;
        proxyAuthorizationHeader.put(
                "Proxy-Authorization",
                "Basic " + Base64.getEncoder().encodeToString(usernamePasswordPair.getBytes()));
        return proxyAuthorizationHeader;
    }

    private InetSocketAddress getProxyAddress() {
        final URI serviceUri = createURIFromHostNamePort(
                this.getAmqpConnection().getHostName(),
                this.getProtocolPort());
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            throw new IllegalStateException(proxySelectorModifiedError);
        }

        final List<Proxy> proxies = proxySelector.select(serviceUri);
        if (!isProxyAddressLegal(proxies)) {
            throw new IllegalStateException(proxySelectorModifiedError);
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
}
