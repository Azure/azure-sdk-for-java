package com.microsoft.azure.eventhubs.amqp;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

public class CustomIOHandler extends IOHandler {

    @Override
    public void onConnectionLocalOpen(Event event) {

        final Connection connection = event.getConnection();
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED) {
            return;
        }

        final Transport transport = Proton.transport();
        transport.setMaxFrameSize(AmqpConstants.MAX_FRAME_SIZE);
        transport.sasl();
        transport.setEmitFlowEventOnSend(false);
        transport.bind(connection);
    }

    @Override
    public void onTransportClosed(Event event) {

        final Transport transport = event.getTransport();
        final Connection connection = event.getConnection();

        if (transport != null && connection != null && connection.getTransport() != null) {
            transport.unbind();
        }
    }
}
