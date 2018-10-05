package com.microsoft.azure.servicebus.amqp;

import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import org.apache.qpid.proton.amqp.transport.ConnectionError;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyConnectionHandler extends WebSocketConnectionHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ProxyConnectionHandler.class);
    private final String proxySelectorModifiedError = "Proxy Selector has been modified.";

    public static boolean shouldUseProxy(final String hostName) {
        final URI uri = createURIFromHostNamePort(hostName, ClientConstants.HTTPS_PORT);
        final ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector == null) {
            return false;
        }

        final List<Proxy> proxies = proxySelector.select(uri);
        return isProxyAddressLegal(proxies);
    }

    public ProxyConnectionHandler(IAmqpConnection messagingFactory) { super(messagingFactory); }

    @Override
    public void addTransportLayers(final Event event, final TransportInternal transport) {
        super.addTransportLayers(event, transport);

        final ProxyImpl proxy = new ProxyImpl();

        final String hostName = event.getConnection().getHostname();
        final ProxyHandler proxyHandler = new ProxyHandlerImpl();
        final Map<String, String> proxyHeader = getAuthorizationHeader();
        proxy.configure(hostName, proxyHeader, proxyHandler, transport);

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
        if (errorCondition == null
                || !(errorCondition.getCondition().equals(ConnectionError.FRAMING_ERROR)
                || errorCondition.getCondition().equals(AmqpErrorCode.ProtonIOError))
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
        } catch (NumberFormatException ignore) {
            return;
        }

        final IOException ioException = reconstructIOException(errorCondition);
        proxySelector.connectFailed(
                createURIFromHostNamePort(((MessagingFactory)this.getMessagingFactory()).getHostName(), this.getProtocolPort()),
                new InetSocketAddress(hostNameParts[0], port),
                ioException
        );

    }

    private Map<String, String> getAuthorizationHeader() {
        final PasswordAuthentication authentication = Authenticator.requestPasswordAuthentication(
                getOutboundSocketHostName(),
                null,
                getOutboundSocketPort(),
                null,
                null,
                "http",
                null,
                Authenticator.RequestorType.PROXY
        );
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
        final String usernamePasswordPair = proxyUserName + ":" + proxyPassword;
        proxyAuthorizationHeader.put(
                "Proxy-Authorization",
                "Basic" + Base64.getEncoder().encodeToString(usernamePasswordPair.getBytes()));
        return proxyAuthorizationHeader;
    }

    @Override
    public String getOutboundSocketHostName() {
        final InetSocketAddress socketAddress = getProxyAddress();
        return socketAddress.getHostString();
    }

    @Override
    public int getOutboundSocketPort() {
        final InetSocketAddress socketAddress = getProxyAddress();
        return socketAddress.getPort();
    }

    private InetSocketAddress getProxyAddress() {
        final URI serviceUri = createURIFromHostNamePort(
                ((MessagingFactory)this.getMessagingFactory()).getHostName(),
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
        // only checks the first proxy in the list
        // returns true if it is an InetSocketAddress, which is required for qpid-proton-j library
        return proxies != null
                && !proxies.isEmpty()
                && proxies.get(0).type() == Proxy.Type.HTTP
                && proxies.get(0).address() != null
                && proxies.get(0).address() instanceof InetSocketAddress;
    }

    private static IOException reconstructIOException(ErrorCondition errorCondition) {
        // since proton library communicates all errors based on amqp-error-condition
        // it swallows the IOException and translates it to proton-io error code
        // we reconstruct the IOException here, but callstack is lost
        return new IOException(errorCondition.getDescription());
    }
}
