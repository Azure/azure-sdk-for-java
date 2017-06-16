package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHandler extends BaseHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(LoggingHandler.class);
    
    @Override
    public void onUnhandled(Event event)
    {
        if(TRACE_LOGGER.isTraceEnabled())
        {
            TRACE_LOGGER.trace("Event raised by protonj: {}", event.toString());
        }
    }   
}
