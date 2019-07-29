// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.TransportType;
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
import java.util.Locale;
import java.util.Map;

public class ConnectionHandler extends BaseHandler {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ConnectionHandler.class);

    private final AmqpConnection amqpConnection;
    private final String connectionId;

    protected ConnectionHandler(final AmqpConnection amqpConnection, final String connectionId) {

        add(new Handshaker());
        this.amqpConnection = amqpConnection;
        this.connectionId = connectionId;
    }

    static ConnectionHandler create(TransportType transportType, AmqpConnection amqpConnection, String connectionId) {
        switch (transportType) {
            case AMQP_WEB_SOCKETS:
                if (WebSocketProxyConnectionHandler.shouldUseProxy(amqpConnection.getHostName())) {
                    return new WebSocketProxyConnectionHandler(amqpConnection);
                } else {
                    return new WebSocketConnectionHandler(amqpConnection);
                }
            case AMQP:
            default:
                return new ConnectionHandler(amqpConnection, connectionId);
        }
    }

    private static SslDomain makeDomain(SslDomain.Mode mode) {

        final SslDomain domain = Proton.sslDomain();
        domain.init(mode);

        // TODO: VERIFY_PEER_NAME support
        domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
        return domain;
    }

    protected AmqpConnection getAmqpConnection() {
        return this.amqpConnection;
    }

    @Override
    public void onConnectionInit(Event event) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionInit hostname[%s], connectionId[%s]",
                    this.amqpConnection.getHostName(), this.connectionId));
        }

        final Connection connection = event.getConnection();
        final String hostName = new StringBuilder(this.amqpConnection.getHostName())
                .append(":")
                .append(this.getProtocolPort())
                .toString();

        connection.setHostname(hostName);
        connection.setContainer(this.connectionId);

        final Map<Symbol, Object> connectionProperties = new HashMap<>();
        connectionProperties.put(AmqpConstants.PRODUCT, ClientConstants.PRODUCT_NAME);
        connectionProperties.put(AmqpConstants.VERSION, ClientConstants.CURRENT_JAVACLIENT_VERSION);
        connectionProperties.put(AmqpConstants.PLATFORM, ClientConstants.PLATFORM_INFO);
        connectionProperties.put(AmqpConstants.FRAMEWORK, ClientConstants.FRAMEWORK_INFO);

        final String userAgent = EventHubClientImpl.USER_AGENT;
        if (userAgent != null) {
            connectionProperties.put(AmqpConstants.USER_AGENT, userAgent.length() < AmqpConstants.MAX_USER_AGENT_LENGTH
                ? userAgent
                : userAgent.substring(0, AmqpConstants.MAX_USER_AGENT_LENGTH));
        }

        connection.setProperties(connectionProperties);
        connection.open();
    }

    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        final SslDomain domain = makeDomain(SslDomain.Mode.CLIENT);
        transport.ssl(domain);
    }

    protected void notifyTransportErrors(final Event event) {
        // no-op
    }

    /**
     * HostName to be used for socket creation.
     * for ex: in case of proxy server - this could be proxy ip address
     *
     * @return host name
     */
    public String getRemoteHostName() {
        return amqpConnection.getHostName();
    }

    /**
     * port used to create socket.
     * for ex: in case of talking to event hubs service via proxy - use proxy port
     *
     * @return port
     */
    protected int getRemotePort() {
        return this.getProtocolPort();
    }

    /**
     * Port used on connection open frame
     *
     * @return port
     */
    protected int getProtocolPort() {
        return ClientConstants.AMQPS_PORT;
    }

    protected int getMaxFrameSize() {
        return AmqpConstants.MAX_FRAME_SIZE;
    }

    @Override
    public void onConnectionBound(Event event) {
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionBound hostname[%s], connectionId[%s]",
                    this.amqpConnection.getHostName(), this.connectionId));
        }

        final Transport transport = event.getTransport();

        this.addTransportLayers(event, (TransportInternal) transport);
    }

    @Override
    public void onConnectionUnbound(Event event) {

        final Connection connection = event.getConnection();
        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionUnbound hostname[%s], connectionId[%s], state[%s], remoteState[%s]",
                    connection.getHostname(), this.connectionId, connection.getLocalState(), connection.getRemoteState()));
        }

        // if failure happened while establishing transport - nothing to free up.
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED) {
            connection.free();
        }
    }

    @Override
    public void onTransportError(Event event) {

        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        if (TRACE_LOGGER.isWarnEnabled()) {
            TRACE_LOGGER.warn(String.format(Locale.US, "onTransportError hostname[%s], connectionId[%s], error[%s]",
                    connection != null ? connection.getHostname() : "n/a", this.connectionId, condition != null ? condition.getDescription() : "n/a"));
        }

        if (connection != null && connection.getRemoteState() != EndpointState.CLOSED) {
            // if the remote-peer abruptly closes the connection without issuing close frame
            // issue one
            this.amqpConnection.onConnectionError(condition);
        }

        // onTransportError event is not handled by the global IO Handler for cleanup
        transport.unbind();

        this.notifyTransportErrors(event);
    }

    @Override
    public void onTransportClosed(Event event) {

        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onTransportClosed hostname[%s], connectionId[%s], error[%s]",
                    connection != null ? connection.getHostname() : "n/a", this.connectionId, (condition != null ? condition.getDescription() : "n/a")));
        }

        if (connection != null && connection.getRemoteState() != EndpointState.CLOSED) {
            // if the remote-peer abruptly closes the connection without issuing close frame
            // issue one
            this.amqpConnection.onConnectionError(condition);
        }
    }

    @Override
    public void onConnectionLocalOpen(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionLocalOpen hostname[%s], connectionId[%s], errorCondition[%s], errorDescription[%s]",
                    connection.getHostname(), this.connectionId, error != null ? error.getCondition() : "n/a", error != null ? error.getDescription() : "n/a"));
        }
    }

    @Override
    public void onConnectionRemoteOpen(Event event) {

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionRemoteOpen hostname[%s], connectionId[%s], remoteContainer[%s]",
                    event.getConnection().getHostname(), this.connectionId, event.getConnection().getRemoteContainer()));
        }

        this.amqpConnection.onOpenComplete(null);
    }

    @Override
    public void onConnectionLocalClose(Event event) {

        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionLocalClose hostname[%s], connectionId[%s], errorCondition[%s], errorDescription[%s]",
                    connection.getHostname(), this.connectionId, error != null ? error.getCondition() : "n/a", error != null ? error.getDescription() : "n/a"));
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
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionRemoteClose hostname[%s], connectionId[%s], errorCondition[%s], errorDescription[%s]",
                    connection.getHostname(), this.connectionId, error != null ? error.getCondition() : "n/a", error != null ? error.getDescription() : "n/a"));
        }

        this.amqpConnection.onConnectionError(error);
    }

    @Override
    public void onConnectionFinal(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getCondition();

        if (TRACE_LOGGER.isInfoEnabled()) {
            TRACE_LOGGER.info(String.format(Locale.US, "onConnectionFinal hostname[%s], connectionId[%s], errorCondition[%s], errorDescription[%s]",
                    connection.getHostname(), this.connectionId, error != null ? error.getCondition() : "n/a", error != null ? error.getDescription() : "n/a"));
        }
    }
}
