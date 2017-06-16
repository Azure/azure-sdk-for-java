/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SessionHandler extends BaseHandler
{
	protected static final Logger TRACE_LOGGER = LoggerFactory.getLogger(SessionHandler.class);

	private final String name;

	public SessionHandler(final String name)
	{
		this.name = name;
	}

	@Override
	public void onSessionRemoteOpen(Event e) 
	{		
		TRACE_LOGGER.debug("onSessionRemoteOpen - entityName: {}, sessionIncCapacity: {}, sessionOutgoingWindow: {}", this.name, e.getSession().getIncomingCapacity(), e.getSession().getOutgoingWindow());

		Session session = e.getSession();
		if (session != null && session.getLocalState() == EndpointState.UNINITIALIZED)
		{
			session.open();
		}
	}


	@Override 
	public void onSessionLocalClose(Event e)
	{		
		TRACE_LOGGER.debug("onSessionLocalClose - entityName: {}, condition: {}", this.name, e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString());
	}

	@Override
	public void onSessionRemoteClose(Event e)
	{		
		TRACE_LOGGER.debug("onSessionRemoteClose - entityName: {}, condition: {}", this.name, e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString());

		Session session = e.getSession();
		if (session != null && session.getLocalState() != EndpointState.CLOSED)
		{
			session.close();
		}
	}

	@Override
	public void onSessionFinal(Event e)
	{ 
	    TRACE_LOGGER.debug("onSessionFinal - entityName: {}", this.name);
	}
}
