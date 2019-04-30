package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Event.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHandler extends BaseHandler {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(LoggingHandler.class);
    
    @Override
    public void onUnhandled(Event event)
    {
        if(TRACE_LOGGER.isTraceEnabled() && event.getType() != Type.REACTOR_QUIESCED ) // Too may REACTOR_QUIESCED events will be raised
        {
            TRACE_LOGGER.trace("Event raised by protonj: {}", event.toString());
        }
    }   
}
