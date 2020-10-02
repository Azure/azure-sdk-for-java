// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UserAgentUtil;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.Connection;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.SslDomain;
import org.apache.qpid.proton.engine.SslPeerDetails;
import org.apache.qpid.proton.engine.Transport;
import org.apache.qpid.proton.engine.impl.TransportInternal;
import org.apache.qpid.proton.reactor.Handshaker;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Creates an AMQP connection using sockets and the default AMQP protocol port 5671.
 */
public class ConnectionHandler extends Handler {
    static final Symbol PRODUCT = Symbol.valueOf("product");
    static final Symbol VERSION = Symbol.valueOf("version");
    static final Symbol PLATFORM = Symbol.valueOf("platform");
    static final Symbol FRAMEWORK = Symbol.valueOf("framework");
    static final Symbol USER_AGENT = Symbol.valueOf("user-agent");

    static final int AMQPS_PORT = 5671;
    static final int MAX_FRAME_SIZE = 65536;

    private final Map<String, Object> connectionProperties;
    private final ClientLogger logger = new ClientLogger(ConnectionHandler.class);
    private final SslDomain.VerifyMode verifyMode;

    /**
     * Creates a handler that handles proton-j's connection events.
     *
     * @param connectionId Identifier for this connection.
     * @param hostname Hostname of the AMQP message broker to create a connection to.
     * @param product The name of the product this connection handler is created for.
     * @param clientVersion The version of the client library creating the connection handler.
     * @param clientOptions provided by user.
     */
    public ConnectionHandler(final String connectionId, final String hostname, final String product,
        final String clientVersion, final SslDomain.VerifyMode verifyMode, final ClientOptions clientOptions) {
        super(connectionId, hostname);

        add(new Handshaker());

        Objects.requireNonNull(connectionId, "'connectionId' cannot be null.");
        Objects.requireNonNull(hostname, "'hostname' cannot be null.");
        Objects.requireNonNull(product, "'product' cannot be null.");
        Objects.requireNonNull(clientVersion, "'clientVersion' cannot be null.");
        Objects.requireNonNull(verifyMode, "'verifyMode' cannot be null.");
        Objects.requireNonNull(clientOptions, "'clientOptions' cannot be null.");

        this.verifyMode = Objects.requireNonNull(verifyMode, "'verifyMode' cannot be null");
        this.connectionProperties = new HashMap<>();
        this.connectionProperties.put(PRODUCT.toString(), product);
        this.connectionProperties.put(VERSION.toString(), clientVersion);
        this.connectionProperties.put(PLATFORM.toString(), ClientConstants.PLATFORM_INFO);
        this.connectionProperties.put(FRAMEWORK.toString(), ClientConstants.FRAMEWORK_INFO);

        final String applicationId = !CoreUtils.isNullOrEmpty(clientOptions.getApplicationId())
            ? clientOptions.getApplicationId()
            : null;
        String userAgent = UserAgentUtil.toUserAgentString(applicationId, product, clientVersion, null);
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
        final SslDomain sslDomain = Proton.sslDomain();
        sslDomain.init(SslDomain.Mode.CLIENT);

        final SSLContext defaultSslContext;

        if (verifyMode == SslDomain.VerifyMode.ANONYMOUS_PEER) {
            defaultSslContext = null;
        } else {
            try {
                defaultSslContext = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw logger.logExceptionAsError(new RuntimeException(
                    "Default SSL algorithm not found in JRE. Please check your JRE setup.", e));
            }
        }

        if (verifyMode == SslDomain.VerifyMode.VERIFY_PEER_NAME) {
            final StrictTlsContextSpi serviceProvider = new StrictTlsContextSpi(defaultSslContext);
            final SSLContext context = new StrictTlsContext(serviceProvider, defaultSslContext.getProvider(),
                defaultSslContext.getProtocol());
            final SslPeerDetails peerDetails = Proton.sslPeerDetails(getHostname(), getProtocolPort());

            sslDomain.setSslContext(context);
            transport.ssl(sslDomain, peerDetails);
            return;
        }

        if (verifyMode == SslDomain.VerifyMode.VERIFY_PEER) {
            sslDomain.setSslContext(defaultSslContext);
        } else if (verifyMode == SslDomain.VerifyMode.ANONYMOUS_PEER) {
            logger.warning("{} is not secure.", verifyMode);
        } else {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "verifyMode is not supported: " + verifyMode));
        }

        sslDomain.setPeerAuthentication(verifyMode);
        transport.ssl(sslDomain);
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
    }

    @Override
    public void onConnectionRemoteClose(Event event) {
        final Connection connection = event.getConnection();
        final ErrorCondition error = connection.getRemoteCondition();

        logErrorCondition("onConnectionRemoteClose", connection, error);
        if (error == null) {
            onNext(connection.getRemoteState());
        } else {
            notifyErrorContext(connection, error);
        }
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

    private void notifyErrorContext(Connection connection, ErrorCondition condition) {
        if (connection == null || connection.getRemoteState() == EndpointState.CLOSED) {
            return;
        }

        if (condition == null) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(
                "connectionId[%s]: notifyErrorContext does not have an ErrorCondition.", getConnectionId())));
        }

        // if the remote-peer abruptly closes the connection without issuing close frame issue one
        final Throwable exception = ExceptionUtil.toException(condition.getCondition().toString(),
            condition.getDescription(), getErrorContext());

        onError(exception);
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
