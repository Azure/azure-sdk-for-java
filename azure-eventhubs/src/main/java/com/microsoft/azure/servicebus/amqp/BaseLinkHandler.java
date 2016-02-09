package com.microsoft.azure.servicebus.amqp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.servicebus.ClientConstants;

// TODO: Implement the Open logic for ServiceBus Service 
// - when the Service returns link Attach with src=null and tgt=null - it means an error 
public class BaseLinkHandler extends BaseHandler
{
	protected static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	@Override
	public void onLinkLocalClose(Event event)
	{
		Link link = event.getLink();
		if (link != null)
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
            {
            	TRACE_LOGGER.log(Level.FINE,
            			String.format("linkName[%s]", link.getName()));
            }
		}
	}
}
