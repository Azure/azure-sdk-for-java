// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.Handshaker;

import java.util.HashMap;
import java.util.Map;

/**
 * Creates an AMQP connection using sockets and the default AMQP protocol port 5671.
 */
public class ConnectionHandler extends Handler {
    static final Symbol PRODUCT = Symbol.valueOf("product");
    static final Symbol VERSION = Symbol.valueOf("version");
    static final Symbol PLATFORM = Symbol.valueOf("platform");
    static final Symbol FRAMEWORK = Symbol.valueOf("framework");
    static final Symbol USER_AGENT = Symbol.valueOf("user-agent");

    static final int MAX_USER_AGENT_LENGTH = 128;
    static final int AMQPS_PORT = 5671;
    static final int MAX_FRAME_SIZE = 65536;

    private final Map<String, Object> connectionProperties;
    protected final ClientLogger logger;

    /**
     * Creates a handler that handles proton-j's connection events.
     *
     * @param connectionId Identifier for this connection.
     * @param hostname Hostname of the AMQP message broker to create a connection to.
     */
    public ConnectionHandler(final String connectionId, final String hostname) {
        this(connectionId, hostname, new ClientLogger(ConnectionHandler.class));
    }

    /**
     * Creates a handler that handles proton-j's connection events.
     *
     * @param connectionId Identifier for this connection.
     * @param hostname Hostname to use for socket creation. If there is a proxy configured, this could be a proxy's
     *     IP address.
     * @param logger The service logger to use.
     */
    protected ConnectionHandler(final String connectionId, final String hostname, final ClientLogger logger) {
        super(connectionId, hostname);

        add(new Handshaker());
        this.logger = logger;

        this.connectionProperties = new HashMap<>();
        this.connectionProperties.put(PRODUCT.toString(), ClientConstants.PRODUCT_NAME);
        this.connectionProperties.put(VERSION.toString(), ClientConstants.CURRENT_JAVA_CLIENT_VERSION);
        this.connectionProperties.put(PLATFORM.toString(), ClientConstants.PLATFORM_INFO);
        this.connectionProperties.put(FRAMEWORK.toString(), ClientConstants.FRAMEWORK_INFO);

        final String userAgent = ClientConstants.USER_AGENT.length() <= MAX_USER_AGENT_LENGTH
            ? ClientConstants.USER_AGENT
            : ClientConstants.USER_AGENT.substring(0, MAX_USER_AGENT_LENGTH);

        this.connectionProperties.put(USER_AGENT.toString(), userAgent);
    }

    /**
     * Gets properties to add when creating AMQP connection.
     *
     * @return A map of properties to add when creating AMQP connection.
     */
    public Map<String, Object> getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * Gets the port used when opening connection.
     *
     * @return The port used to open connection.
     */
    public int getProtocolPort() {
        return AMQPS_PORT;
    }

    /**
     * Gets the max frame size for this connection.
     *
     * @return The max frame size for this connection.
     */
    public int getMaxFrameSize() {
        return MAX_FRAME_SIZE;
    }

    protected void addTransportLayers(final Event event, final TransportInternal transport) {
        final SslDomain domain = createSslDomain(SslDomain.Mode.CLIENT);
        transport.ssl(domain);
    }

    @Override
    public void onConnectionInit(Event event) {
        logger.info("onConnectionInit hostname[{}], connectionId[{}]", getHostname(), getConnectionId());

        final Connection connection = event.getConnection();
        final String hostName = getHostname() + ":" + getProtocolPort();

        connection.setHostname(hostName);
        connection.setContainer(getConnectionId());

        final Map<Symbol, Object> properties = new HashMap<>();
        connectionProperties.forEach((key, value) -> properties.put(Symbol.getSymbol(key), value));

        connection.setProperties(properties);
        connection.open();
    }

    @Override
    public void onConnectionBound(Event event) {
        logger.info("onConnectionBound hostname[{}], connectionId[{}]", getHostname(), getConnectionId());

        final Transport transport = event.getTransport();

        this.addTransportLayers(event, (TransportInternal) transport);

        final Connection connection = event.getConnection();
        if (connection != null) {
            onNext(connection.getRemoteState());
        }
    }

    @Override
    public void onConnectionUnbound(Event event) {
        final Connection connection = event.getConnection();
        logger.info("onConnectionUnbound hostname[{}], connectionId[{}], state[{}], remoteState[{}]",
            connection.getHostname(), getConnectionId(), connection.getLocalState(), connection.getRemoteState());

        // if failure happened while establishing transport - nothing to free up.
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED) {
            connection.free();
        }

        onNext(connection.getRemoteState());
    }

    @Override
    public void onTransportError(Event event) {
        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        logger.warning("onTransportError hostname[{}], connectionId[{}], error[{}]",
            connection != null ? connection.getHostname() : ClientConstants.NOT_APPLICABLE,
            getConnectionId(),
            condition != null ? condition.getDescription() : ClientConstants.NOT_APPLICABLE);

        if (connection != null) {
            notifyErrorContext(connection, condition);
            onNext(connection.getRemoteState());
        }

        // onTransportError event is not handled by the global IO Handler for cleanup
        transport.unbind();
    }

    @Override
    public void onTransportClosed(Event event) {
        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        logger.info("onTransportClosed hostname[{}], connectionId[{}], error[{}]",
            connection != null ? connection.getHostname() : ClientConstants.NOT_APPLICABLE,
            getConnectionId(),
            condition != null ? condition.getDescription() : ClientConstants.NOT_APPLICABLE);

        if (connection != null) {
            notifyErrorContext(connection, condition);
            onNext(connection.getRemoteState());
        }
    }

    @Override
    public void onConnectionLocalOpen(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getCondition();

        logErrorCondition("onConnectionLocalOpen", connection, error);
    }

    @Override
    public void onConnectionRemoteOpen(Event event) {
        final Connection connection = event.getConnection();

        logger.info("onConnectionRemoteOpen hostname[{}], connectionId[{}], remoteContainer[{}]",
            connection.getHostname(), getConnectionId(), connection.getRemoteContainer());

        onNext(connection.getRemoteState());
    }

    @Override
    public void onConnectionLocalClose(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getCondition();

        logErrorCondition("onConnectionLocalClose", connection, error);

        if (connection.getRemoteState() == EndpointState.CLOSED) {
            // This means that the CLOSE origin is Service
            final Transport transport = connection.getTransport();
            if (transport != null) {
                transport.unbind(); // we proactively dispose IO even if service fails to close
            }
        }

        onNext(connection.getRemoteState());
    }

    @Override
    public void onConnectionRemoteClose(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getRemoteCondition();

        logErrorCondition("onConnectionRemoteClose", connection, error);

        onNext(connection.getRemoteState());
        notifyErrorContext(connection, error);
    }

    @Override
    public void onConnectionFinal(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getCondition();

        logErrorCondition("onConnectionFinal", connection, error);
        onNext(connection.getRemoteState());

        // Complete the processors because they no longer have any work to do.
        close();
    }

    public AmqpErrorContext getErrorContext() {
        return new AmqpErrorContext(getHostname());
    }

    private static SslDomain createSslDomain(SslDomain.Mode mode) {
        final SslDomain domain = Proton.sslDomain();
        domain.init(mode);

        // TODO: VERIFY_PEER_NAME support
        domain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
        return domain;
    }

    private void notifyErrorContext(Connection connection, ErrorCondition condition) {
        if (connection == null || connection.getRemoteState() == EndpointState.CLOSED) {
            return;
        }

        if (condition == null) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "notifyErrorContext does not have an ErrorCondition."));
        }

        // if the remote-peer abruptly closes the connection without issuing close frame issue one
        final Throwable exception = ExceptionUtil.toException(condition.getCondition().toString(),
            condition.getDescription(), getErrorContext());

        onNext(exception);
    }

    private void logErrorCondition(String eventName, Connection connection, ErrorCondition error) {
        logger.info("{} hostname[{}], connectionId[{}], errorCondition[{}], errorDescription[{}]",
            eventName,
            connection.getHostname(),
            getConnectionId(),
            error != null ? error.getCondition() : ClientConstants.NOT_APPLICABLE,
            error != null ? error.getDescription() : ClientConstants.NOT_APPLICABLE);
    }
}
