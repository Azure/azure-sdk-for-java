// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

public class CustomIOHandler extends IOHandler {
    @Override
    public void onConnectionLocalOpen(Event event) {
        Connection connection = event.getConnection();
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED) {
            return;
        }

        Transport transport = Proton.transport();
        // To detect connection drops in worst case in idleTimeout, which otherwise can take a non deterministic
        // amount of time to get detected. In practice, we saw 15-20 minutes, on Linux docker containers when 
        // simulating packet drops with iptables rules.
        transport.setIdleTimeout(AmqpConstants.TRANSPORT_IDLE_TIMEOUT_MILLIS);
        // We are creating transport, we should also set the max frame size
        // Only if transport is created by qpid handler will it set max frame size from reactor options.
        transport.setMaxFrameSize(event.getReactor().getOptions().getMaxFrameSize());
        transport.sasl();
        transport.setEmitFlowEventOnSend(false);
        transport.bind(connection);
    }

    @Override
    public void onTransportClosed(Event event) {
        if (event.getTransport() != null) {
            event.getTransport().unbind();
        }
    }
}
