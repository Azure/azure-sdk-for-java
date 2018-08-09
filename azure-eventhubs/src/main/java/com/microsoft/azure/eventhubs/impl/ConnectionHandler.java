/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.Handshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

// ServiceBus <-> ProtonReactor interaction handles all
// amqp_connection/transport related events from reactor
public class ConnectionHandler extends BaseHandler {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

    private final AmqpConnection messagingFactory;

    public ConnectionHandler(final AmqpConnection messagingFactory) {

        add(new Handshaker());
        this.messagingFactory = messagingFactory;
    }

    private static SslDomain makeDomain(SslDomain.Mode mode) {

        final SslDomain domain = Proton.sslDomain();
        domain.init(mode);

        // TODO: VERIFY_PEER_NAME support
        domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
        return domain;
    }

    @Override
    public void onConnectionInit(Event event) {

        final Connection connection = event.getConnection();
        final String hostName = event.getReactor().getConnectionAddress(connection);

        connection.setHostname(hostName);
        connection.setContainer(StringUtil.getRandomString());

        final Map<Symbol, Object> connectionProperties = new HashMap<>();
        connectionProperties.put(AmqpConstants.PRODUCT, ClientConstants.PRODUCT_NAME);
        connectionProperties.put(AmqpConstants.VERSION, ClientConstants.CURRENT_JAVACLIENT_VERSION);
        connectionProperties.put(AmqpConstants.PLATFORM, ClientConstants.PLATFORM_INFO);
        connectionProperties.put(AmqpConstants.FRAMEWORK, ClientConstants.FRAMEWORK_INFO);

        final String userAgent = EventHubClientImpl.USER_AGENT;
        if (userAgent != null) {
            connectionProperties.put(AmqpConstants.USER_AGENT, userAgent.length() < AmqpConstants.MAX_USER_AGENT_LENGTH ?
                    userAgent :
                    userAgent.substring(0, AmqpConstants.MAX_USER_AGENT_LENGTH));
        }

        connection.setProperties(connectionProperties);
        connection.open();
    }

    protected void addTransportLayers(final Event event, final TransportInternal transport) {
    }

    protected int getPort() {
        return ClientConstants.AMQPS_PORT;
    }

    protected int getMaxFrameSize() {
        return AmqpConstants.MAX_FRAME_SIZE;
    }

    @Override
    public void onConnectionBound(Event event) {

        final Transport transport = event.getTransport();

        this.addTransportLayers(event, (TransportInternal) transport);

        final SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
        transport.ssl(domain);
    }

    @Override
    public void onConnectionUnbound(Event event) {

        final Connection connection = event.getConnection();
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(
                    "onConnectionUnbound: hostname[" + connection.getHostname() + "], state[" + connection.getLocalState() + "], remoteState[" + connection.getRemoteState() + "]");
        }

        // if failure happened while establishing transport - nothing to free up.
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED)
            connection.free();
    }

    @Override
    public void onTransportError(Event event) {

        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        if (TRACE_LOGGER.isWarnEnabled()) {
            TRACE_LOGGER.warn("onTransportClosed: hostname[" + (connection != null ? connection.getHostname() : "n/a") + "], error[" + (condition != null ? condition.getDescription() : "n/a") + "]");
        }

        if (connection != null && connection.getRemoteState() != EndpointState.CLOSED) {
            // if the remote-peer abruptly closes the connection without issuing close frame
            // issue one
            this.messagingFactory.onConnectionError(condition);
        }

        // onTransportError event is not handled by the global IO Handler for cleanup
        transport.unbind();
    }

    @Override
    public void onTransportClosed(Event event) {

        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("onTransportClosed: hostname[" + (connection != null ? connection.getHostname() : "n/a") + "], error[" + (condition != null ? condition.getDescription() : "n/a") + "]");
        }

        if (connection != null && connection.getRemoteState() != EndpointState.CLOSED) {
            // if the remote-peer abruptly closes the connection without issuing close frame
            // issue one
            this.messagingFactory.onConnectionError(condition);
        }
    }

    @Override
    public void onConnectionRemoteOpen(Event event) {

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("onConnectionRemoteOpen: hostname[" + event.getConnection().getHostname() + ", " + event.getConnection().getRemoteContainer() + "]");
        }

        this.messagingFactory.onOpenComplete(null);
    }

    @Override
    public void onConnectionLocalClose(Event event) {

        final Connection connection = event.getConnection();

        final ErrorCondition error = connection.getCondition();
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("onConnectionLocalClose: hostname[" + connection.getHostname() +
                    (error != null
                            ? "], errorCondition[" + error.getCondition() + ", " + error.getDescription() + "]"
                            : "]"));
        }

        if (connection.getRemoteState() == EndpointState.CLOSED) {
            // This means that the CLOSE origin is Service
            final Transport transport = connection.getTransport();
            if (transport != null) {
                transport.unbind(); // we proactively dispose IO even if service fails to close
            }
        }
    }

    @Override
    public void onConnectionRemoteClose(Event event) {

        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getRemoteCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info("onConnectionRemoteClose: hostname[" + connection.getHostname() +
                    (error != null
                            ? "], errorCondition[" + error.getCondition() + ", " + error.getDescription() + "]"
                            : "]"));
        }

        this.messagingFactory.onConnectionError(error);
    }
}
