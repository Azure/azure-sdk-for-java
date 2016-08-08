/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.*;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

import com.microsoft.azure.servicebus.*;


// ServiceBus <-> ProtonReactor interaction handles all
// amqp_connection/transport related events from reactor
public final class ConnectionHandler extends BaseHandler
{

	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.SERVICEBUS_CLIENT_TRACE);

	private final String hostname;
	private final String username;
	private final String password;
	private final IAmqpConnection messagingFactory;

	public ConnectionHandler(final IAmqpConnection messagingFactory, final String hostname, final String username, final String password)
	{
		add(new Handshaker());

		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.messagingFactory = messagingFactory;
	}

	@Override
	public void onConnectionInit(Event event)
	{
		Connection connection = event.getConnection();
		connection.setHostname(this.hostname + ":" + ClientConstants.AMQPS_PORT);
		connection.setContainer(UUID.randomUUID().toString());
		connection.open();
	}

	@Override
	public void onConnectionBound(Event event)
	{
		Transport transport = event.getTransport();

		SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
		transport.ssl(domain);

		Sasl sasl = transport.sasl();
		sasl.plain(this.username, this.password);
	}

	@Override
	public void onConnectionUnbound(Event event)
	{
		if (TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, "Connection.onConnectionUnbound: hostname[" + event.getConnection().getHostname() + "]");
		}
	}

	@Override
	public void onTransportError(Event event)
	{
		ErrorCondition condition = event.getTransport().getCondition();
		if (condition != null)
		{
			if (TRACE_LOGGER.isLoggable(Level.WARNING))
			{
				TRACE_LOGGER.log(Level.WARNING, "Connection.onTransportError: hostname[" + event.getConnection().getHostname() + "], error[" + condition.getDescription() + "]");
			}
		}
		else
		{
			if (TRACE_LOGGER.isLoggable(Level.WARNING))
			{
				TRACE_LOGGER.log(Level.WARNING, "Connection.onTransportError: hostname[" + event.getConnection().getHostname() + "], error[no description returned]");
			}
		}

		this.messagingFactory.onConnectionError(condition);
	}

	@Override
	public void onConnectionRemoteOpen(Event event)
	{
		if (TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, "Connection.onConnectionRemoteOpen: hostname[" + event.getConnection().getHostname() + ", " + event.getConnection().getRemoteContainer() +"]");
		}

		this.messagingFactory.onOpenComplete(null);
	}

	@Override
	public void onConnectionRemoteClose(Event event)
	{
		final Connection connection = event.getConnection();
		final ErrorCondition error = connection.getRemoteCondition();

		if (TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE, "hostname[" + connection.getHostname() + 
					(error != null
					? "], errorCondition[" + error.getCondition() + ", " + error.getDescription() + "]"
							: "]"));
		}
		
		if (connection.getRemoteState() != EndpointState.CLOSED)
		{
			connection.close();
		}

		this.messagingFactory.onConnectionError(error);
	}

	private static SslDomain makeDomain(SslDomain.Mode mode)
	{
		SslDomain domain = Proton.sslDomain();
		domain.init(mode);

		// TODO: VERIFY_PEER_NAME support
		domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
		return domain;
	}
}
