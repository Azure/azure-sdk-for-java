package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.impl.IOHandler;

public class CustomIOHandler extends IOHandler
{
	@Override public void onConnectionLocalOpen(Event event)
	{
		Connection connection = event.getConnection();
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED)
        {
            return;
        }
        
        Transport transport = Proton.transport();
        transport.setMaxFrameSize(64*1024);
        transport.bind(connection);
	}    
}
