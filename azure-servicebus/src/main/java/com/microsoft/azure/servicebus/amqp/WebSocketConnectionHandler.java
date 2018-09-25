package com.microsoft.azure.servicebus.amqp;

import com.microsoft.azure.proton.transport.ws.impl.WebSocketImpl;
import com.microsoft.azure.servicebus.primitives.ClientConstants;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketConnectionHandler extends ConnectionHandler {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(WebSocketConnectionHandler.class);

    public WebSocketConnectionHandler(IAmqpConnection messagingFactory)
    {
        super(messagingFactory);
    }

    @Override
    public void addTransportLayers(final Event event, final TransportInternal transport)
    {
        final String hostName = event.getConnection().getHostname();

        final WebSocketImpl webSocket = new WebSocketImpl();
        webSocket.configure(
                hostName,
                "/$servicebus/websocket",
                "",
                0,
                "AMQPWSB10",
                null,
                null);

        transport.addTransportLayer(webSocket);

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("addWebsocketHandshake: hostname[" + hostName +"]");
        }

        super.addTransportLayers(event, transport);
    }

    @Override
    public int getProtocolPort()
    {
        return ClientConstants.HTTPS_PORT;
    }

    @Override
    public int getMaxFrameSize()
    {
        // This is the current limitation of https://github.com/Azure/qpid-proton-j-extensions
        // once, this library enables larger frames - this property can be removed.
        return 4 * 1024;
    }
}
