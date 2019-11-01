// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.CBSAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.ProxyAuthenticationType;
import com.azure.core.amqp.models.ProxyConfiguration;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.models.EventHubConsumerOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;
import java.util.ServiceLoader;

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
 * <strong>Starting position</strong>, <strong>consumer group</strong>, and <strong>credentials</strong> are
 * <strong>required</strong> when creating an {@link EventHubConsumerAsyncClient} or {@link EventHubConsumerClient}.
 * {@link EventHubClientBuilder#consumerOptions(EventHubConsumerOptions) Consumer options} can be supplied for
 * optional consumer customizations.
 * </p>
 *
 * <p><strong>Creating an asynchronous {@link EventHubProducerAsyncClient} using Event Hubs namespace connection string
 * </strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation}
 *
 * <p><strong>Creating a synchronous {@link EventHubConsumerClient} using an Event Hub instance connection string
 * </strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation}
 *
 * @see EventHubClient
 * @see EventHubAsyncClient
 */
@ServiceClientBuilder(serviceClients = {EventHubProducerAsyncClient.class, EventHubProducerClient.class})
public class EventHubClientBuilder {
    /**
     * The name of the default consumer group in the Event Hubs service.
     */
    public static final String DEFAULT_CONSUMER_GROUP_NAME = "$Default";

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
    private String fullyQualifiedNamespace;
    private String eventHubName;
    private EventHubConsumerOptions consumerOptions;
    private EventPosition startingPosition;
    private String consumerGroup;
    private EventHubConnection eventHubConnection;

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

        return credential(properties.getEndpoint().getHost(), properties.getEntityPath(), tokenCredential);
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

        if (!ImplUtils.isNullOrEmpty(properties.getEntityPath())
            && !eventHubName.equals(properties.getEntityPath())) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "'connectionString' contains an Event Hub name [%s] and it does not match the given "
                    + "'eventHubName' parameter [%s]. Please use the credentials(String connectionString) overload. "
                    + "Or supply a 'connectionString' without 'EntityPath' in it.",
                properties.getEntityPath(), eventHubName)));
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
     * Sets the Event Hub connection to use when interacting with Event Hubs. If not set, a new connection will be
     * constructed and used. If a connection is provided, end users are responsible for disposing of it.
     *
     * @param eventHubConnection Event Hub connection to use.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder connection(EventHubConnection eventHubConnection) {
        this.eventHubConnection = eventHubConnection;
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     *
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     *     null.
     */
    public EventHubClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                            TokenCredential credential) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (ImplUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
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
     * Sets the position within the partition where the consumer should begin reading events.
     *
     * @param eventPosition Position within an Event Hub partition to begin consuming events.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder startingPosition(EventPosition eventPosition) {
        this.startingPosition = eventPosition;
        return this;
    }

    /**
     * Sets the name of the consumer group this consumer is associated with. Events are read in the context of this
     * group. The name of the consumer group that is created by default is
     * {@link #DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     * context of this group. The name of the consumer group that is created by default is
     * {@link #DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Sets the set of options to apply when creating the consumer.
     *
     * @param consumerOptions The set of options to apply when creating the consumer.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder consumerOptions(EventHubConsumerOptions consumerOptions) {
        this.consumerOptions = consumerOptions;
        return this;
    }

    /**
     * Creates a new {@link EventHubConnection} based on the options set on this builder. Every time
     * {@code buildConnection()} is invoked, a new instance of {@link EventHubConnection} is created.
     *
     * @return A new {@link EventHubConnection} with the configured options.
     *
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     * #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is specified
     * but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubConnection buildConnection() {
        final MessageSerializer messageSerializer = new EventHubMessageSerializer();
        return buildConnection(messageSerializer);
    }

    /**
     * Creates a new {@link EventHubConsumerAsyncClient} based on the options set on this builder. Every time
     * {@code buildAsyncConsumer()} is invoked, a new instance of {@link EventHubConsumerAsyncClient} is created.
     *
     * @return A new {@link EventHubConsumerAsyncClient} with the configured options.
     *
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     * either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. If
     * {@link #startingPosition(EventPosition)} or {@link #consumerGroup(String)} have not been set.
     * Or, if a proxy is specified but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubConsumerAsyncClient buildAsyncConsumer() {
        final EventHubConsumerOptions options = consumerOptions != null
            ? consumerOptions
            : new EventHubConsumerOptions();

        if (ImplUtils.isNullOrEmpty(consumerGroup)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'consumerGroup' cannot be null or an empty "
                + "string. using EventHubClientBuilder.consumerGroup(String)"));
        } else if (startingPosition == null) {
            throw logger.logExceptionAsError(new NullPointerException("'startingPosition' has not been set. Set it "
                + "using EventHubClientBuilder.consumerGroup(String)"));
        }

        return buildAsyncClient().createConsumer(consumerGroup, startingPosition, options);
    }

    /**
     * Creates a new {@link EventHubConsumerClient} based on the options set on this builder. Every time
     * {@code buildConsumer()} is invoked, a new instance of {@link EventHubConsumerClient} is created.
     *
     * @return A new {@link EventHubConsumerClient} with the configured options.
     *
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     * either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. If
     * {@link #startingPosition(EventPosition)} or {@link #consumerGroup(String)} have not been set.
     * Or, if a proxy is specified but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubConsumerClient buildConsumer() {
        final EventHubConsumerOptions options = consumerOptions != null
            ? consumerOptions
            : new EventHubConsumerOptions();

        return buildClient().createConsumer(consumerGroup, startingPosition, options);
    }

    /**
     * Creates a new {@link EventHubProducerAsyncClient} based on options set on this builder. Every time
     * {@code buildAsyncProducer()} is invoked, a new instance of {@link EventHubProducerAsyncClient} is created.
     *
     * @return A new {@link EventHubProducerAsyncClient} instance with all the configured options.
     *
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     * either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy
     * is specified but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubProducerAsyncClient buildAsyncProducer() {
        return buildAsyncClient().createProducer();
    }

    /**
     * Creates a new {@link EventHubProducerClient} based on options set on this builder. Every time
     * {@code buildAsyncProducer()} is invoked, a new instance of {@link EventHubProducerClient} is created.
     *
     * @return A new {@link EventHubProducerClient} instance with all the configured options.
     *
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     * either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy
     * is specified but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubProducerClient buildProducer() {
        return buildClient().createProducer();
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
    EventHubAsyncClient buildAsyncClient() {
        final MessageSerializer messageSerializer = new EventHubMessageSerializer();

        final boolean isSharedConnection = eventHubConnection != null;
        final EventHubConnection connection = isSharedConnection
            ? eventHubConnection
            : buildConnection(messageSerializer);

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        return new EventHubAsyncClient(connection, tracerProvider, messageSerializer, isSharedConnection);
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
    EventHubClient buildClient() {
        final EventHubAsyncClient client = buildAsyncClient();

        return new EventHubClient(client, retryOptions);
    }

    private EventHubConnection buildConnection(MessageSerializer messageSerializer) {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
            connectionOptions.getAuthorizationType(), connectionOptions.getHostname(),
            ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);

        final Mono<EventHubAmqpConnection> connectionMono = Mono.fromCallable(() -> {
            final String connectionId = StringUtil.getRandomString("MF");

            return new EventHubReactorAmqpConnection(connectionId, connectionOptions, provider, handlerProvider,
                tokenManagerProvider, messageSerializer);
        });

        return new EventHubConnection(connectionMono, connectionOptions);
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

        return new ConnectionOptions(fullyQualifiedNamespace, eventHubName, credentials, authorizationType,
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
