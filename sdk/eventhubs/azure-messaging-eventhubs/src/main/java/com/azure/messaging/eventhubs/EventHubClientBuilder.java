// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.AzureTokenManagerProvider;
import com.azure.messaging.eventhubs.implementation.CBSAuthorizationType;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.EventHubConnection;
import com.azure.messaging.eventhubs.implementation.EventHubReactorConnection;
import com.azure.messaging.eventhubs.implementation.ManagementResponseMapper;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.implementation.ReactorProvider;
import com.azure.messaging.eventhubs.implementation.StringUtil;
import com.azure.messaging.eventhubs.implementation.TokenManagerProvider;
import com.azure.messaging.eventhubs.models.ProxyAuthenticationType;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.ServiceLoader;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * This class provides a fluent builder API to aid the instantiation of {@link EventHubAsyncClient} and {@link
 * EventHubClient}. Calling {@link #buildAsyncClient() buildAsyncClient()} or {@link #buildClient() buildClient()}
 * constructs an instance of the respective client.
 *
 * <p>
 * <strong>Credentials are required</strong> to perform operations against Azure Event Hubs. They can be set by using
 * one of the following methods:
 * <ul>
 * <li>{@link #connectionString(String) connectionString(String)} with a connection string to a specific Event Hub.
 * </li>
 * <li>{@link #connectionString(String, String) connectionString(String, String)} with an Event Hub <i>namespace</i>
 * connection string and the Event Hub name.</li>
 * <li>{@link #credential(String, String, TokenCredential) credential(String, String, TokenCredential)} with the
 * fully qualified domain name (FQDN), Event Hub name, and a set of credentials authorized to use the Event Hub.
 * </li>
 * </ul>
 *
 * <p>
 * <strong>Creating an asynchronous {@link EventHubAsyncClient} using Event Hubs namespace connection string</strong>
 * </p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncclient.instantiation#string-string}
 *
 * <p>
 * <strong>Creating a synchronous {@link EventHubClient} using an Event Hub instance connection string</strong>
 * </p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubclient.instantiation}
 *
 * @see EventHubClient
 * @see EventHubAsyncClient
 */
@ServiceClientBuilder(serviceClients = {EventHubAsyncClient.class, EventHubClient.class})
public class EventHubClientBuilder {
    private final ClientLogger logger = new ClientLogger(EventHubClientBuilder.class);

    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING = "AZURE_EVENT_HUBS_CONNECTION_STRING";
    private static final RetryOptions DEFAULT_RETRY = new RetryOptions()
        .setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    private TokenCredential credentials;
    private Configuration configuration;
    private ProxyConfiguration proxyConfiguration;
    private RetryOptions retryOptions;
    private Scheduler scheduler;
    private TransportType transport;
    private String host;
    private String eventHubName;

    /**
     * Creates a new instance with the default transport {@link TransportType#AMQP}.
     */
    public EventHubClientBuilder() {
        transport = TransportType.AMQP;
    }

    /**
     * Sets the credential information given a connection string to the Event Hub instance.
     *
     * <p>
     * If the connection string is copied from the Event Hubs namespace, it will likely not contain the name to the
     * desired Event Hub, which is needed. In this case, the name can be added manually by adding {@literal
     * "EntityPath=EVENT_HUB_NAME"} to the end of the connection string. For example, "EntityPath=telemetry-hub".
     * </p>
     *
     * <p>
     * If you have defined a shared access policy directly on the Event Hub itself, then copying the connection string
     * from that Event Hub will result in a connection string that contains the name.
     * </p>
     *
     * @param connectionString The connection string to use for connecting to the Event Hub instance. It is expected
     *     that the Event Hub name and the shared access key properties are contained in this connection string.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     *
     * @throws IllegalArgumentException if {@code connectionString} is null or empty. Or, the {@code connectionString}
     *     does not contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
     */
    public EventHubClientBuilder connectionString(String connectionString) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new AzureException(
                "Could not create the EventHubSharedAccessKeyCredential.", e));
        }

        return credential(properties.getEndpoint().getHost(), properties.getEventHubName(), tokenCredential);
    }

    /**
     * Sets the credential information given a connection string to the Event Hubs namespace and name to a specific
     * Event Hub instance.
     *
     * @param connectionString The connection string to use for connecting to the Event Hubs namespace; it is
     *     expected that the shared access key properties are contained in this connection string, but not the Event Hub
     *     name.
     * @param eventHubName The name of the Event Hub to connect the client to.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     *
     * @throws NullPointerException if {@code connectionString} or {@code eventHubName} is null.
     * @throws IllegalArgumentException if {@code connectionString} or {@code eventHubName} is an empty string. Or, if
     * the {@code connectionString} contains the Event Hub name.
     * @throws AzureException If the shared access signature token credential could not be created using the connection
     * string.
     */
    public EventHubClientBuilder connectionString(String connectionString, String eventHubName) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (connectionString.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'connectionString' cannot be an empty string."));
        } else if (eventHubName.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }

        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new AzureException(
                "Could not create the EventHubSharedAccessKeyCredential.", e));
        }

        if (!ImplUtils.isNullOrEmpty(properties.getEventHubName())
            && !eventHubName.equals(properties.getEventHubName())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "'connectionString' contains an Event Hub name [%s] and it does not match the given "
                    + "'eventHubName' parameter [%s]. Please use the credentials(String connectionString) overload. "
                    + "Or supply a 'connectionString' without 'EntityPath' in it.",
                properties.getEventHubName(), eventHubName)));
        }

        return credential(properties.getEndpoint().getHost(), eventHubName, tokenCredential);
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * If not specified, the default configuration store is used to configure the {@link EventHubAsyncClient}. Use
     * {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to configure the {@link EventHubAsyncClient}.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param host The fully qualified host name for the Event Hubs namespace. This is likely to be similar to
     *     <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     *
     * @throws IllegalArgumentException if {@code host} or {@code eventHubName} is an empty string.
     * @throws NullPointerException if {@code host}, {@code eventHubName}, {@code credentials} is null.
     */
    public EventHubClientBuilder credential(String host, String eventHubName, TokenCredential credential) {
        this.host = Objects.requireNonNull(host, "'host' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (ImplUtils.isNullOrEmpty(host)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'host' cannot be an empty string."));
        } else if (ImplUtils.isNullOrEmpty(eventHubName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }

        return this;
    }

    /**
     * Sets the proxy configuration to use for {@link EventHubAsyncClient}. When a proxy is configured, {@link
     * TransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyConfiguration The proxy configuration to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder proxyConfiguration(ProxyConfiguration proxyConfiguration) {
        this.proxyConfiguration = proxyConfiguration;
        return this;
    }

    /**
     * Sets the scheduler for operations such as connecting to and receiving or sending data to Event Hubs. If none is
     * specified, an elastic pool is used.
     *
     * @param scheduler The scheduler for operations such as connecting to and receiving or sending data to Event Hubs.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs. Default value is {@link
     * TransportType#AMQP}.
     *
     * @param transport The transport type to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder transportType(TransportType transport) {
        this.transport = transport;
        return this;
    }

    /**
     * Sets the retry policy for {@link EventHubAsyncClient}. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder retry(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Creates a new {@link EventHubAsyncClient} based on options set on this builder. Every time
     * {@code buildAsyncClient()} is invoked, a new instance of {@link EventHubAsyncClient} is created.
     *
     * <p>
     * The following options are used if ones are not specified in the builder:
     *
     * <ul>
     * <li>If no configuration is specified, the {@link Configuration#getGlobalConfiguration() global configuration}
     * is used to provide any shared configuration values. The configuration values read are the {@link
     * Configuration#PROPERTY_HTTP_PROXY}, {@link ProxyConfiguration#PROXY_USERNAME}, and {@link
     * ProxyConfiguration#PROXY_PASSWORD}.</li>
     * <li>If no retry is specified, the default retry options are used.</li>
     * <li>If no proxy is specified, the builder checks the {@link Configuration#getGlobalConfiguration() global
     * configuration} for a configured proxy, then it checks to see if a system proxy is configured.</li>
     * <li>If no timeout is specified, a {@link ClientConstants#OPERATION_TIMEOUT timeout of one minute} is used.</li>
     * <li>If no scheduler is specified, an {@link Schedulers#elastic() elastic scheduler} is used.</li>
     * </ul>
     *
     * @return A new {@link EventHubAsyncClient} instance with all the configured options.
     *
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     * #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is specified
     * but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubAsyncClient buildAsyncClient() {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        return buildAsyncClient(connectionOptions);
    }

    /**
     * Creates a new {@link EventHubClient} based on options set on this builder. Every time {@code buildClient()} is
     * invoked, a new instance of {@link EventHubClient} is created.
     *
     * <p>
     * The following options are used if ones are not specified in the builder:
     *
     * <ul>
     * <li>If no configuration is specified, the {@link Configuration#getGlobalConfiguration() global configuration}
     * is used to provide any shared configuration values. The configuration values read are the {@link
     * Configuration#PROPERTY_HTTP_PROXY}, {@link ProxyConfiguration#PROXY_USERNAME}, and {@link
     * ProxyConfiguration#PROXY_PASSWORD}.</li>
     * <li>If no retry is specified, the default retry options are used.</li>
     * <li>If no proxy is specified, the builder checks the {@link Configuration#getGlobalConfiguration() global
     * configuration} for a configured proxy, then it checks to see if a system proxy is configured.</li>
     * <li>If no timeout is specified, a {@link ClientConstants#OPERATION_TIMEOUT timeout of one minute} is used.</li>
     * <li>If no scheduler is specified, an {@link Schedulers#elastic() elastic scheduler} is used.</li>
     * </ul>
     *
     * @return A new {@link EventHubClient} instance with all the configured options.
     *
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     * #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is specified
     * but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubClient buildClient() {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final EventHubAsyncClient client = buildAsyncClient(connectionOptions);

        return new EventHubClient(client, connectionOptions);
    }

    private static EventHubAsyncClient buildAsyncClient(ConnectionOptions connectionOptions) {
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        final Mono<EventHubConnection> connectionMono = Mono.fromCallable(() -> {
            final String connectionId = StringUtil.getRandomString("MF");
            final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
                connectionOptions.getAuthorizationType(), connectionOptions.getHost(),
                ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
            final ManagementResponseMapper mapper = new EventHubResponseMapper();

            return new EventHubReactorConnection(connectionId, connectionOptions, provider, handlerProvider,
                tokenManagerProvider, mapper);
        });

        return new EventHubAsyncClient(connectionOptions, tracerProvider, connectionMono);
    }

    private ConnectionOptions getConnectionOptions() {
        configuration = configuration == null ? Configuration.getGlobalConfiguration().clone() : configuration;

        if (credentials == null) {
            final String connectionString = configuration.get(AZURE_EVENT_HUBS_CONNECTION_STRING);

            if (ImplUtils.isNullOrEmpty(connectionString)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Credentials have not been set. "
                    + "They can be set using: connectionString(String), connectionString(String, String), "
                    + "credentials(String, String, TokenCredential), or setting the environment variable '"
                    + AZURE_EVENT_HUBS_CONNECTION_STRING + "' with a connection string"));
            }

            connectionString(connectionString);
        }

        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        // If the proxy has been configured by the user but they have overridden the TransportType with something that
        // is not AMQP_WEB_SOCKETS.
        if (proxyConfiguration != null && proxyConfiguration.isProxyAddressConfigured()
            && transport != TransportType.AMQP_WEB_SOCKETS) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Cannot use a proxy when TransportType is not AMQP."));
        }

        if (proxyConfiguration == null) {
            proxyConfiguration = getDefaultProxyConfiguration(configuration);
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        final CBSAuthorizationType authorizationType = credentials instanceof EventHubSharedAccessKeyCredential
            ? CBSAuthorizationType.SHARED_ACCESS_SIGNATURE
            : CBSAuthorizationType.JSON_WEB_TOKEN;

        return new ConnectionOptions(host, eventHubName, credentials, authorizationType,
            transport, retryOptions, proxyConfiguration, scheduler);
    }

    private ProxyConfiguration getDefaultProxyConfiguration(Configuration configuration) {
        ProxyAuthenticationType authentication = ProxyAuthenticationType.NONE;
        if (proxyConfiguration != null) {
            authentication = proxyConfiguration.getAuthentication();
        }

        String proxyAddress = configuration.get(Configuration.PROPERTY_HTTP_PROXY);

        if (ImplUtils.isNullOrEmpty(proxyAddress)) {
            return ProxyConfiguration.SYSTEM_DEFAULTS;
        }

        final String[] hostPort = proxyAddress.split(":");
        if (hostPort.length < 2) {
            throw logger.logExceptionAsError(new IllegalArgumentException("HTTP_PROXY cannot be parsed into a proxy"));
        }

        final String host = hostPort[0];
        final int port = Integer.parseInt(hostPort[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        final String username = configuration.get(ProxyConfiguration.PROXY_USERNAME);
        final String password = configuration.get(ProxyConfiguration.PROXY_PASSWORD);

        return new ProxyConfiguration(authentication, proxy, username, password);
    }
}
