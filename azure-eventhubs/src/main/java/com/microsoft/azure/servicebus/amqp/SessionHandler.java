/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Session;

import com.microsoft.azure.servicebus.ClientConstants;

public class SessionHandler extends BaseHandler
{
	protected static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);

	private final String name;

	public SessionHandler(final String name)
	{
		this.name = name;
	}

	@Override
	public void onSessionRemoteOpen(Event e) 
	{
		if(TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "entityName[%s], sessionIncCapacity[%s], sessionOutgoingWindow[%s]",
					this.name, e.getSession().getIncomingCapacity(), e.getSession().getOutgoingWindow()));
		}

		Session session = e.getSession();
		if (session != null && session.getLocalState() == EndpointState.UNINITIALIZED)
		{
			session.open();
		}
	}


	@Override 
	public void onSessionLocalClose(Event e)
	{
		if(TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "entityName[%s], condition[%s]", this.name, 
					e.getSession().getCondition() == null ? "none" : e.getSession().getCondition().toString()));
		}
	}

	@Override
	public void onSessionRemoteClose(Event e)
	{ 
		if(TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "entityName[%s], condition[%s]", this.name,
					e.getSession().getRemoteCondition() == null ? "none" : e.getSession().getRemoteCondition().toString()));
		}

		Session session = e.getSession();
		if (session != null && session.getLocalState() != EndpointState.CLOSED)
		{
			session.close();
		}
	}

	@Override
	public void onSessionFinal(Event e)
	{ 
		if(TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, String.format(Locale.US, "entityName[%s]", this.name));
		}
	}

}
