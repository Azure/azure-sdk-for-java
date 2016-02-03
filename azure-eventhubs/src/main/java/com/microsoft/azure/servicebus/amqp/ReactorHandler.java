package com.microsoft.azure.servicebus.amqp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import com.microsoft.azure.servicebus.ClientConstants;

public class ReactorHandler extends BaseHandler
{
	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);
	
	@Override
	public void onReactorInit(Event e)
	{ 
		if(TRACE_LOGGER.isLoggable(Level.FINE))
        {
            TRACE_LOGGER.log(Level.FINE, "reactor.onReactorInit");
        }
	}
    
    @Override public void onReactorFinal(Event e)
    {
    	if(TRACE_LOGGER.isLoggable(Level.FINE))
	    {
	        TRACE_LOGGER.log(Level.FINE, "reactor.onReactorFinal");
	    }
    }
}
