package com.microsoft.azure.servicebus.amqp;

import java.util.*;
import java.util.logging.*;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.transport.*;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;

import com.microsoft.azure.servicebus.ClientConstants;

/**
 * ServiceBus <-> ProtonReactor interaction handles all
 * amqp_connection/transport related events from reactor
 */
public final class ConnectionHandler extends BaseHandler
{

	private static final Logger TRACE_LOGGER = Logger.getLogger(ClientConstants.ServiceBusClientTrace);

	private final String hostname;
	private final String username;
	private final String password;

	// TODO: sasl mechanism should be passed - since currently it only supports
	// sasKey/Value - simplified & directly passing username/password
	public ConnectionHandler(final String hostname, final String username, final String password)
	{
		add(new Handshaker());
		this.hostname = hostname;
		this.username = username;
		this.password = password;
	}

	// TODO: REMOVE after hooking proper diagnostics
	@Override
	public void onUnhandled(Event event)
	{
		if (TRACE_LOGGER.isLoggable(Level.FINE))
		{
			TRACE_LOGGER.log(Level.FINE,
					"Connection.onUnhandled: name[" + event.getConnection().getHostname() + "] : event[" + event + "]");
		}
	}

	@Override
	public void onConnectionBound(Event event)
	{
		Transport transport = event.getTransport();

		SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
		Ssl ssl = transport.ssl(domain);

		Sasl sasl = transport.sasl();
		sasl.plain(this.username, this.password);
	}

	@Override
	public void onTransportError(Event event)
	{
		ErrorCondition condition = event.getTransport().getCondition();
		if (condition != null)
		{
			if (TRACE_LOGGER.isLoggable(Level.WARNING))
			{
				TRACE_LOGGER.log(Level.WARNING, "Connection.onTransportError: hostname[" + event.getConnection().getHostname() + "Error: " + condition.getDescription());
			}
		}
		else
		{
			if (TRACE_LOGGER.isLoggable(Level.WARNING))
			{
				System.err.println("");
				TRACE_LOGGER.log(Level.WARNING, "Connection.onTransportError: hostname[" + event.getConnection().getHostname() + "Error (no description returned).");
			}
		}
	}

	@Override
	public void onConnectionInit(Event event)
	{
		Connection connection = event.getConnection();
		connection.setHostname(this.hostname + ":" + ClientConstants.AmqpsPort);
		connection.setContainer(UUID.randomUUID().toString());
		connection.open();
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
