package com.microsoft.azure.servicebus.amqp;

import com.microsoft.azure.proton.transport.proxy.ProxyHandler;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyHandlerImpl;
import com.microsoft.azure.proton.transport.proxy.impl.ProxyImpl;

import com.microsoft.azure.servicebus.primitives.StringUtil;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ProxyConnectionHandler extends WebSocketConnectionHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ProxyConnectionHandler.class);

    public static boolean shouldUseProxy(IAmqpConnection messagingFactory) {
        return !StringUtil.isNullOrEmpty(((MessagingFactory)messagingFactory).getClientSettings().getProxyHostName());
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

    private Map<String, String> getAuthorizationHeader() {
        final String proxyUserName = ((MessagingFactory)messagingFactory).getClientSettings().getProxyUserName();
        final String proxyPassword = ((MessagingFactory)messagingFactory).getClientSettings().getProxyPassword();
        if (StringUtil.isNullOrEmpty(proxyUserName) ||
            StringUtil.isNullOrEmpty(proxyPassword)) {
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
        return ((MessagingFactory)messagingFactory).getClientSettings().getProxyHostName();
    }

    @Override
    public int getOutboundSocketPort() {
        return ((MessagingFactory)messagingFactory).getClientSettings().getProxyHostPort();
    }
}
