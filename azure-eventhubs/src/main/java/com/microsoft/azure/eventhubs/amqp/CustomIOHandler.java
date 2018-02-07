package com.microsoft.azure.eventhubs.amqp;

import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

public class CustomIOHandler extends IOHandler {

    @Override
    public void onTransportClosed(Event event) {

        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }
}
