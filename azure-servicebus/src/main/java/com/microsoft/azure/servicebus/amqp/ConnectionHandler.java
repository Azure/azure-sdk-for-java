/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.reactor.Handshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.servicebus.primitives.ClientConstants;
import com.microsoft.azure.servicebus.primitives.StringUtil;

// ServiceBus <-> ProtonReactor interaction handles all
// amqp_connection/transport related events from reactor
public final class ConnectionHandler extends BaseHandler
{
	private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);
	private final IAmqpConnection messagingFactory;

	public ConnectionHandler(final IAmqpConnection messagingFactory)
	{
		add(new Handshaker());
		this.messagingFactory = messagingFactory;
	}
	
	@Override
	public void onConnectionInit(Event event)
	{
		final Connection connection = event.getConnection();
		final String hostName = event.getReactor().getConnectionAddress(connection);
		TRACE_LOGGER.debug("onConnectionInit: hostname:{}", hostName);
		connection.setHostname(hostName);
		connection.setContainer(StringUtil.getShortRandomString());
		
		final Map<Symbol, Object> connectionProperties = new HashMap<Symbol, Object>();
        connectionProperties.put(AmqpConstants.PRODUCT, ClientConstants.PRODUCT_NAME);
        connectionProperties.put(AmqpConstants.VERSION, ClientConstants.CURRENT_JAVACLIENT_VERSION);
        connectionProperties.put(AmqpConstants.PLATFORM, ClientConstants.PLATFORM_INFO);
        connection.setProperties(connectionProperties);
        
		connection.open();
	}

	@Override
	public void onConnectionBound(Event event)
	{
	    TRACE_LOGGER.debug("onConnectionBound: hostname:{}", event.getConnection().getHostname());
		Transport transport = event.getTransport();

		SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
		transport.ssl(domain);

		Sasl sasl = transport.sasl();
		sasl.setMechanisms("ANONYMOUS");
	}

	@Override
	public void onConnectionUnbound(Event event)
	{
		TRACE_LOGGER.debug("Connection.onConnectionUnbound: hostname:{}", event.getConnection().getHostname());
	}

	@Override
	public void onTransportError(Event event)
	{
		ErrorCondition condition = event.getTransport().getCondition();
		if (condition != null)
		{			
			TRACE_LOGGER.warn("Connection.onTransportError: hostname:{}, error:{}", event.getConnection().getHostname(), condition.getDescription());
		}
		else
		{			
			TRACE_LOGGER.warn("Connection.onTransportError: hostname:{}. error:{}", event.getConnection().getHostname(), "no description returned");
		}

		this.messagingFactory.onConnectionError(condition);
		Connection connection = event.getConnection();
		if (connection != null) {
            connection.free();
        }
	}

	@Override
	public void onConnectionRemoteOpen(Event event)
	{		
		TRACE_LOGGER.debug("Connection.onConnectionRemoteOpen: hostname:{}, remotecontainer:{}", event.getConnection().getHostname(), event.getConnection().getRemoteContainer());
		this.messagingFactory.onOpenComplete();
	}

	@Override
	public void onConnectionRemoteClose(Event event)
	{
		final Connection connection = event.getConnection();
		final ErrorCondition error = connection.getRemoteCondition();
		
		TRACE_LOGGER.debug("onConnectionRemoteClose: hostname:{},errorCondition:{}", connection.getHostname(), error != null ? error.getCondition() + "," + error.getDescription() : null);
		
		if (connection.getRemoteState() != EndpointState.CLOSED)
		{
			connection.close();
		}

		this.messagingFactory.onConnectionError(error);
		this.freeOnCloseResponse(connection);
	}
	
	@Override
    public void onConnectionFinal(Event event) {
        final Transport transport = event.getTransport();
        if (transport != null) {
            transport.unbind();
            transport.free();
        }
    }
	
	@Override
    public void onConnectionLocalClose(Event event) {
	    Connection connection = event.getConnection();
	    TRACE_LOGGER.debug("onConnectionLocalClose: hostname:{}", connection.getHostname());
        this.freeOnCloseResponse(connection);
    }
	
	private void freeOnCloseResponse(Connection connection) {
        if (connection != null &&
                connection.getLocalState() == EndpointState.CLOSED &&
                (connection.getRemoteState() == EndpointState.CLOSED)) {
            connection.free();
        }
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
