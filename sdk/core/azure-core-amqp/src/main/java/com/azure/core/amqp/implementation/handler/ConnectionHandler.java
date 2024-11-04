// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ClientConstants;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ExceptionUtil;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UserAgentUtil;
import com.azure.core.util.logging.LoggingEventBuilder;
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

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addErrorCondition;
import static com.azure.core.amqp.implementation.ClientConstants.FULLY_QUALIFIED_NAMESPACE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.HOSTNAME_KEY;

/**
 * Creates an AMQP connection using sockets.
 */
public class ConnectionHandler extends Handler {
    /**
     * Gets the secure AMQP (amqps) port. The standard AMQP port number that has been assigned by IANA for secure
     * TCP using TLS.
     * When using <b>secure</b> AMQP, the TCP connection is first overlaid with TLS before entering the AMQP protocol
     * handshake.
     *
     * @see <a href="https://learn.microsoft.com/azure/service-bus-messaging/service-bus-amqp-protocol-guide">
     *     Connections and sessions</a>
     */
    public static final int AMQPS_PORT = 5671;
    /**
     * Gets the AMQP (amqp) port.  The standard AMQP port number that has been assigned by IANA for TCP, UDP, and SCTP.
     * Currently, there is no mapping for UDP nor SCTP.
     * When using AMQP, the TCP connection does not, initially, need to be overlaid with TLS, whereby the server
     * immediately <b>offers a mandatory upgrade of connection</b> to TLS using the AMQP-prescribed model.
     */
    public static final int AMQP_PORT = 5672;

    static final Symbol PRODUCT = Symbol.valueOf("product");
    static final Symbol VERSION = Symbol.valueOf("version");
    static final Symbol PLATFORM = Symbol.valueOf("platform");
    static final Symbol FRAMEWORK = Symbol.valueOf("framework");
    static final Symbol USER_AGENT = Symbol.valueOf("user-agent");

    static final int MAX_FRAME_SIZE = 65536;
    static final int CONNECTION_IDLE_TIMEOUT = 60_000;  // milliseconds

    private final Map<String, Object> connectionProperties;
    private final ConnectionOptions connectionOptions;
    private final SslPeerDetails peerDetails;
    private final AmqpMetricsProvider metricProvider;
    private final boolean enableSsl;

    /**
     * Creates a handler that handles proton-j's connection events without using SSL.
     *
     * @param connectionId Identifier for this connection.
     * @param connectionOptions Options used when creating the AMQP connection.
     * @param metricProvider The AMQP metrics provider.
     * @throws NullPointerException if {@code connectionOptions}, {@code peerDetails}, or {@code metricProvider} is
     * null.
     */
    public ConnectionHandler(final String connectionId, final ConnectionOptions connectionOptions,
        AmqpMetricsProvider metricProvider) {
        this(connectionId, connectionOptions, null, metricProvider, false);
    }

    /**
     * Creates a handler that handles proton-j's connection events.
     *
     * @param connectionId Identifier for this connection.
     * @param connectionOptions Options used when creating the AMQP connection.
     * @param peerDetails The peer details for this connection.
     * @param metricProvider The AMQP metrics provider.
     * @throws NullPointerException if {@code connectionOptions}, {@code peerDetails}, or {@code metricProvider} is
     * null.
     */
    public ConnectionHandler(final String connectionId, final ConnectionOptions connectionOptions,
        SslPeerDetails peerDetails, AmqpMetricsProvider metricProvider) {
        this(connectionId, connectionOptions, peerDetails, metricProvider, true);
    }

    ConnectionHandler(String connectionId, ConnectionOptions connectionOptions, SslPeerDetails peerDetails,
        AmqpMetricsProvider metricProvider, boolean enableSsl) {
        super(connectionId,
            Objects.requireNonNull(connectionOptions, "'connectionOptions' cannot be null.").getHostname());
        add(new Handshaker());

        this.connectionOptions = connectionOptions;
        this.connectionProperties = new HashMap<>();
        this.connectionProperties.put(PRODUCT.toString(), connectionOptions.getProduct());
        this.connectionProperties.put(VERSION.toString(), connectionOptions.getClientVersion());
        this.connectionProperties.put(PLATFORM.toString(), ClientConstants.PLATFORM_INFO);
        this.connectionProperties.put(FRAMEWORK.toString(), ClientConstants.FRAMEWORK_INFO);

        final ClientOptions clientOptions = connectionOptions.getClientOptions();
        final String applicationId
            = !CoreUtils.isNullOrEmpty(clientOptions.getApplicationId()) ? clientOptions.getApplicationId() : null;
        final String userAgent = UserAgentUtil.toUserAgentString(applicationId, connectionOptions.getProduct(),
            connectionOptions.getClientVersion(), null);

        this.connectionProperties.put(USER_AGENT.toString(), userAgent);

        if (enableSsl) {
            this.peerDetails = Objects.requireNonNull(peerDetails, "'peerDetails' cannot be null.");
        } else {
            this.peerDetails = peerDetails;
        }

        this.metricProvider = Objects.requireNonNull(metricProvider, "'metricProvider' cannot be null.");
        this.enableSsl = enableSsl;
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
        return connectionOptions.getPort();
    }

    /**
     * Gets the max frame size for this connection.
     *
     * @return The max frame size for this connection.
     */
    public int getMaxFrameSize() {
        return MAX_FRAME_SIZE;
    }

    /**
     * Configures the SSL transport layer for the connection based on the {@link ConnectionOptions#getSslVerifyMode()}.
     *
     * @param event The proton-j event.
     * @param transport Transport to add layers to.
     */
    protected void addTransportLayers(Event event, TransportInternal transport) {
        // default connection idle timeout is 0.
        // Giving it an idle timeout will enable the client side to know broken connection faster.
        // Refer to http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#doc-doc-idle-time-out
        transport.setIdleTimeout(CONNECTION_IDLE_TIMEOUT);

        if (!enableSsl) {
            return;
        }

        final SslDomain sslDomain = Proton.sslDomain();
        sslDomain.init(SslDomain.Mode.CLIENT);

        final SslDomain.VerifyMode verifyMode = connectionOptions.getSslVerifyMode();
        final SSLContext defaultSslContext;

        if (verifyMode == SslDomain.VerifyMode.ANONYMOUS_PEER) {
            defaultSslContext = null;
        } else {
            try {
                defaultSslContext = SSLContext.getDefault();
            } catch (NoSuchAlgorithmException e) {
                throw logger.logExceptionAsError(
                    new RuntimeException("Default SSL algorithm not found in JRE. Please check your JRE setup.", e));
            }
        }

        if (verifyMode == SslDomain.VerifyMode.VERIFY_PEER_NAME) {
            final StrictTlsContextSpi serviceProvider = new StrictTlsContextSpi(defaultSslContext);
            final SSLContext context = new StrictTlsContext(serviceProvider, defaultSslContext.getProvider(),
                defaultSslContext.getProtocol());

            sslDomain.setSslContext(context);
            transport.ssl(sslDomain, peerDetails);
            return;
        }

        if (verifyMode == SslDomain.VerifyMode.VERIFY_PEER) {
            sslDomain.setSslContext(defaultSslContext);
            sslDomain.setPeerAuthentication(SslDomain.VerifyMode.VERIFY_PEER);
        } else if (verifyMode == SslDomain.VerifyMode.ANONYMOUS_PEER) {
            logger.warning("'{}' is not secure.", verifyMode);
            sslDomain.setPeerAuthentication(SslDomain.VerifyMode.ANONYMOUS_PEER);
        } else {
            throw logger
                .logExceptionAsError(new UnsupportedOperationException("verifyMode is not supported: " + verifyMode));
        }

        transport.ssl(sslDomain);
    }

    @Override
    public void onConnectionInit(Event event) {
        logger.atInfo()
            .addKeyValue(HOSTNAME_KEY, getHostname())
            .addKeyValue(FULLY_QUALIFIED_NAMESPACE_KEY, connectionOptions.getFullyQualifiedNamespace())
            .log("onConnectionInit");

        final Connection connection = event.getConnection();
        if (connection == null) {
            logger.warning("Underlying connection is null. Should not be possible.");
            close();
            return;
        }

        // Set the hostname of the AMQP message broker. This may be different from the actual underlying transport
        // in the case we are using an intermediary to connect to Event Hubs.
        connection.setHostname(connectionOptions.getFullyQualifiedNamespace());
        connection.setContainer(getConnectionId());

        final Map<Symbol, Object> properties = new HashMap<>();
        connectionProperties.forEach((key, value) -> properties.put(Symbol.getSymbol(key), value));

        connection.setProperties(properties);
        connection.open();
    }

    @Override
    public void onConnectionBound(Event event) {
        final Transport transport = event.getTransport();

        final LoggingEventBuilder builder = logger.atInfo().addKeyValue(HOSTNAME_KEY, getHostname());
        if (peerDetails != null) {
            builder.addKeyValue("peerDetails", () -> peerDetails.getHostname() + ":" + peerDetails.getPort());
        }

        builder.log("onConnectionBound");

        this.addTransportLayers(event, (TransportInternal) transport);

        final Connection connection = event.getConnection();
        if (connection != null) {
            onNext(connection.getRemoteState());
        }
    }

    @Override
    public void onConnectionUnbound(Event event) {
        final Connection connection = event.getConnection();
        logger.atInfo()
            .addKeyValue(HOSTNAME_KEY, connection.getHostname())
            .addKeyValue("state", connection.getLocalState())
            .addKeyValue("remoteState", connection.getRemoteState())
            .log("onConnectionUnbound");

        // if failure happened while establishing transport - nothing to free up.
        if (connection.getRemoteState() != EndpointState.UNINITIALIZED) {
            connection.free();
        }

        close();
    }

    @Override
    public void onTransportError(Event event) {
        final Connection connection = event.getConnection();
        final Transport transport = event.getTransport();
        final ErrorCondition condition = transport.getCondition();

        addErrorCondition(logger.atWarning(), condition)
            .addKeyValue(HOSTNAME_KEY, connection != null ? connection.getHostname() : ClientConstants.NOT_APPLICABLE)
            .log("onTransportError");
        metricProvider.recordHandlerError(AmqpMetricsProvider.ErrorSource.TRANSPORT, condition);

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

        addErrorCondition(logger.atInfo(), condition)
            .addKeyValue(HOSTNAME_KEY, connection != null ? connection.getHostname() : ClientConstants.NOT_APPLICABLE)
            .log("onTransportClosed");

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

        logger.atInfo()
            .addKeyValue(HOSTNAME_KEY, connection.getHostname())
            .addKeyValue("remoteContainer", connection.getRemoteContainer())
            .log("onConnectionRemoteOpen");

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
        onNext(EndpointState.CLOSED);

        metricProvider.recordConnectionClosed(error);
        // Complete the processors because they no longer have any work to do.
        close();
    }

    /**
     * Gets the error context for this connection.
     *
     * @return The error context for this connection.
     */
    public AmqpErrorContext getErrorContext() {
        return new AmqpErrorContext(getHostname());
    }

    private void notifyErrorContext(Connection connection, ErrorCondition condition) {
        if (connection == null || connection.getRemoteState() == EndpointState.CLOSED) {
            return;
        }

        if (condition == null) {
            throw logger
                .logExceptionAsError(new IllegalStateException("notifyErrorContext does not have an ErrorCondition."));
        }

        // if the remote-peer abruptly closes the connection without issuing close frame issue one
        final Throwable exception = ExceptionUtil.toException(condition.getCondition().toString(),
            condition.getDescription(), getErrorContext());

        onError(exception);
    }

    private void logErrorCondition(String eventName, Connection connection, ErrorCondition error) {
        addErrorCondition(logger.atInfo(), error).addKeyValue(HOSTNAME_KEY, connection.getHostname()).log(eventName);
    }
}
