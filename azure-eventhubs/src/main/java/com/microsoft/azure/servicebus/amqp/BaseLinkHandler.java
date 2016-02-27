/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;

import com.microsoft.azure.servicebus.ClientConstants;

public class BaseLinkHandler extends BaseHandler
{
	protected static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);
	
	private final IAmqpLink underlyingEntity;
	
	public BaseLinkHandler(final IAmqpLink amqpLink)
	{
		this.underlyingEntity = amqpLink;
	}
	
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
		
	public void processOnClose(Link link, ErrorCondition condition)
	{
		if (condition != null)
		{
			if(TRACE_LOGGER.isLoggable(Level.FINE))
	        {
				TRACE_LOGGER.log(Level.FINE, "linkName[" + link.getName() +
						(condition != null ? "], ErrorCondition[" + condition.getCondition() + ", " + condition.getDescription() + "]" : "], condition[null]"));
	        }
        }
		
		if (link.getLocalState() != EndpointState.CLOSED)
		{
			link.close();
		}
		
		this.underlyingEntity.onClose(condition);
	}
	
	public void processOnClose(Link link, Exception exception)
	{
		if (link.getLocalState() != EndpointState.CLOSED)
		{
			link.close();
		}
		
		this.underlyingEntity.onError(exception);
	}
}
