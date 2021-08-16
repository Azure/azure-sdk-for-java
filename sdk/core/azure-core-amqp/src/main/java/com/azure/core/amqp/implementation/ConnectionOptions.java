// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.annotation.Immutable;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.engine.SslDomain;
import reactor.core.scheduler.Scheduler;

import java.util.Objects;

/**
 * A wrapper class that contains all parameters that are needed to establish a connection to an AMQP message broker.
 */
@Immutable
public class ConnectionOptions {
    private final TokenCredential tokenCredential;
    private final AmqpTransportType transport;
    private final AmqpRetryOptions retryOptions;
    private final ProxyOptions proxyOptions;
    private final Scheduler scheduler;
    private final String fullyQualifiedNamespace;
    private final CbsAuthorizationType authorizationType;
    private final String authorizationScope;
    private final ClientOptions clientOptions;
    private final String product;
    private final String clientVersion;
    private final SslDomain.VerifyMode verifyMode;
    private final String hostname;
    private final int port;

    /**
     * Creates an instance with the following options set. The AMQP connection is created to the
     * {@code fullyQualifiedNamespace} using a port based on the {@code transport}.
     *
     * @param fullyQualifiedNamespace Fully qualified namespace for the AMQP broker. (ie.
     *     namespace.servicebus.windows.net)
     * @param tokenCredential The credential for connecting to the AMQP broker.
     * @param authorizationType The authorisation type used for authorizing with the CBS node.
     * @param transport The type connection used for the AMQP connection.
     * @param retryOptions Retry options for the connection.
     * @param proxyOptions Any proxy options to set.
     * @param scheduler Scheduler for async operations.
     * @param clientOptions Client options for the connection.
     * @param verifyMode How to verify SSL information.
     *
     * @throws NullPointerException in the case that {@code fullyQualifiedNamespace}, {@code tokenCredential},
     *     {@code authorizationType}, {@code transport}, {@code retryOptions}, {@code scheduler}, {@code clientOptions}
     *     {@code proxyOptions} or {@code verifyMode} is null.
     */
    public ConnectionOptions(String fullyQualifiedNamespace, TokenCredential tokenCredential,
        CbsAuthorizationType authorizationType, String authorizationScope, AmqpTransportType transport,
        AmqpRetryOptions retryOptions, ProxyOptions proxyOptions, Scheduler scheduler, ClientOptions clientOptions,
        SslDomain.VerifyMode verifyMode, String product, String clientVersion) {
        this(fullyQualifiedNamespace, tokenCredential, authorizationType, authorizationScope, transport, retryOptions,
            proxyOptions, scheduler, clientOptions, verifyMode, product, clientVersion, fullyQualifiedNamespace,
            getPort(transport));
    }

    /**
     * Creates an instance with the connection options set. Used when an alternative address should be made for the
     * connection rather than through the fullyQualifiedNamespace.
     *
     * @param fullyQualifiedNamespace Fully qualified namespace for the AMQP broker. (ie.
     *     namespace.servicebus.windows.net)
     * @param tokenCredential The credential for connecting to the AMQP broker.
     * @param authorizationType The authorisation type used for authorizing with the CBS node.
     * @param transport The type connection used for the AMQP connection.
     * @param retryOptions Retry options for the connection.
     * @param proxyOptions (Optional) Any proxy options to set.
     * @param scheduler Scheduler for async operations.
     * @param clientOptions Client options for the connection.
     * @param verifyMode How to verify SSL information.
     * @param hostname Connection hostname. Used to create the connection to in the case that we cannot
     *     connect directly to the AMQP broker.
     * @param port Connection port. Used to create the connection to in the case we cannot connect directly
     *     to the AMQP broker.
     *
     * @throws NullPointerException in the case that {@code fullyQualifiedNamespace}, {@code tokenCredential},
     *     {@code authorizationType}, {@code transport}, {@code retryOptions}, {@code scheduler},
     *     {@code clientOptions}, {@code hostname}, or {@code verifyMode} is null.
     */
    public ConnectionOptions(String fullyQualifiedNamespace, TokenCredential tokenCredential,
        CbsAuthorizationType authorizationType, String authorizationScope, AmqpTransportType transport,
        AmqpRetryOptions retryOptions, ProxyOptions proxyOptions, Scheduler scheduler, ClientOptions clientOptions,
        SslDomain.VerifyMode verifyMode, String product, String clientVersion, String hostname, int port) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' is required.");
        this.tokenCredential = Objects.requireNonNull(tokenCredential, "'tokenCredential' is required.");
        this.authorizationType = Objects.requireNonNull(authorizationType, "'authorizationType' is required.");
        this.authorizationScope = Objects.requireNonNull(authorizationScope, "'authorizationScope' is required.");
        this.transport = Objects.requireNonNull(transport, "'transport' is required.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' is required.");
        this.scheduler = Objects.requireNonNull(scheduler, "'scheduler' is required.");
        this.clientOptions = Objects.requireNonNull(clientOptions, "'clientOptions' is required.");
        this.verifyMode = Objects.requireNonNull(verifyMode, "'verifyMode' is required.");
        this.hostname = Objects.requireNonNull(hostname, "'hostname' cannot be null.");
        this.port = port != -1 ? port : getPort(transport);
        this.proxyOptions = proxyOptions;

        this.product = Objects.requireNonNull(product, "'product' cannot be null.");
        this.clientVersion = Objects.requireNonNull(clientVersion, "'clientVersion' cannot be null.");
    }

    /**
     * Gets the scope to use when authorizing.
     *
     * @return The scope to use when authorizing.
     */
    public String getAuthorizationScope() {
        return authorizationScope;
    }

    /**
     * Gets the authorisation type for the CBS node.
     *
     * @return The authorisation type for the CBS node.
     */
    public CbsAuthorizationType getAuthorizationType() {
        return authorizationType;
    }

    /**
     * Gets the client options.
     *
     * @return The client options.
     */
    public ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Gets the product information for this AMQP connection. (ie. Service Bus or Event Hubs.)
     *
     * @return The product information for this AMQP connection.
     */
    public String getProduct() {
        return product;
    }

    /**
     * Gets the client version for this AMQP connection.
     *
     * @return The client version for this AMQP connection.
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     * The fully qualified domain name for the AMQP broker. Typically of the form
     * {@literal "<your-namespace>.service.windows.net"}.
     *
     * @return The fully qualified domain name for the AMQP broker.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    public AmqpRetryOptions getRetry() {
        return retryOptions;
    }

    /**
     * Gets the proxy options set.
     *
     * @return The proxy options set. {@code null} if there are no options set.
     */
    public ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    /**
     * Gets the scheduler to execute tasks on.
     *
     * @return The scheduler to execute tasks on.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Gets the verification mode for the SSL certificate.
     *
     * @return The verification mode for the SSL certificate.
     */
    public SslDomain.VerifyMode getSslVerifyMode() {
        return verifyMode;
    }

    /**
     * Gets the credential for authorising with Event Hubs.
     *
     * @return The credential for authorising with Event Hubs.
     */
    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }

    /**
     * Gets the transport type for the AMQP connection.
     *
     * @return The transport type for the AMQP connection.
     */
    public AmqpTransportType getTransportType() {
        return transport;
    }

    /**
     * Gets the DNS hostname or IP address of the service. Typically of the form
     * {@literal "<your-namespace>.service.windows.net"}, unless connecting to the service through an intermediary.
     *
     * @return The DNS hostname or IP address to connect to.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Gets the port to connect to when creating the connection to the service. This is usually the port for the
     * AMQP protocol or 443 for web sockets, but can differ if connecting through an intermediary.
     *
     * @return The port to connect to when creating the connection to the service.
     */
    public int getPort() {
        return port;
    }

    private static int getPort(AmqpTransportType transport) {
        switch (transport) {
            case AMQP:
                return ConnectionHandler.AMQPS_PORT;
            case AMQP_WEB_SOCKETS:
                return WebSocketsConnectionHandler.HTTPS_PORT;
            default:
                throw new ClientLogger(ConnectionOptions.class).logThrowableAsError(
                    new IllegalArgumentException("Transport Type is not supported: " + transport));
        }
    }
}
