// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.TransportType;
import com.azure.core.credentials.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.implementation.annotation.ServiceClientBuilder;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.messaging.eventhubs.implementation.CBSAuthorizationType;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.ConnectionOptions;
import com.azure.messaging.eventhubs.implementation.ConnectionStringProperties;
import com.azure.messaging.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.messaging.eventhubs.implementation.ReactorProvider;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ProxyAuthenticationType;
import com.azure.messaging.eventhubs.models.ProxyConfiguration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Objects;

/**
 * This class provides a fluent builder API to help aid the configuration and instantiation of the {@link
 * EventHubAsyncClient}. Calling {@link #buildAsyncClient()} constructs an instant of the client.
 *
 * <p>
 * The client requires credentials or a connection string to perform operations against Azure Event Hubs. Setting
 * credentials by using {@link #connectionString(String)}, {@link #connectionString(String, String)}, or {@link
 * #credential(String, String, TokenCredential)}, is required in order to construct an {@link EventHubAsyncClient}.
 * </p>
 *
 * <p><strong>Creating an {@link EventHubAsyncClient} using Event Hubs namespace connection string</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubclientbuilder.connectionString#string-string}
 *
 * <p><strong>Creating an {@link EventHubAsyncClient} using Event Hub instance connection string</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubclientbuilder.connectionstring#string}
 *
 * <p><strong>Creating an {@link EventHubAsyncClient} using Event Hub with no retry, different timeout and new
 * Scheduler</strong></p>
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubclientbuilder.retry-timeout-scheduler}
 *
 * <p><strong>Creating an {@link EventProcessor} instance using Event Hub instance connection
 *  string</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventprocessor.instantiation}
 *
 * @see EventHubAsyncClient
 * @see EventProcessor
 */
@ServiceClientBuilder(serviceClients = {EventHubAsyncClient.class, EventProcessor.class})
public class EventHubClientBuilder {

    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING = "AZURE_EVENT_HUBS_CONNECTION_STRING";
    private static final RetryOptions DEFAULT_RETRY = new RetryOptions()
        .tryTimeout(ClientConstants.OPERATION_TIMEOUT);

    private TokenCredential credentials;
    private Configuration configuration;
    private ProxyConfiguration proxyConfiguration;
    private RetryOptions retryOptions;
    private Scheduler scheduler;
    private TransportType transport;
    private String host;
    private String eventHubName;
    private EventPosition initialEventPosition;
    private PartitionProcessorFactory partitionProcessorFactory;
    private String consumerGroupName;
    private PartitionManager partitionManager;

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
     * @param connectionString The connection string to use for connecting to the Event Hub instance. It is
     *         expected that the Event Hub name and the shared access key properties are contained in this connection
     *         string.
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code connectionString} is null or empty. Or, the {@code
     *         connectionString} does not contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *         connection string.
     */
    public EventHubClientBuilder connectionString(String connectionString) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(properties.sharedAccessKeyName(),
                properties.sharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AzureException("Could not create the EventHubSharedAccessKeyCredential.", e);
        }

        return credential(properties.endpoint().getHost(), properties.eventHubName(), tokenCredential);
    }

    /**
     * Sets the credential information given a connection string to the Event Hubs namespace and name to a specific
     * Event Hub instance.
     *
     * @param connectionString The connection string to use for connecting to the Event Hubs namespace; it is
     *         expected that the shared access key properties are contained in this connection string, but not the Event
     *         Hub name.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code connectionString} or {@code eventHubName} is null or empty.
     *         Or, if the {@code connectionString} contains the Event Hub name.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *         connection string.
     */
    public EventHubClientBuilder connectionString(String connectionString, String eventHubName) {
        if (ImplUtils.isNullOrEmpty(eventHubName)) {
            throw new IllegalArgumentException("'eventHubName' cannot be null or empty");
        }

        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new EventHubSharedAccessKeyCredential(properties.sharedAccessKeyName(),
                properties.sharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AzureException("Could not create the EventHubSharedAccessKeyCredential.", e);
        }

        if (!ImplUtils.isNullOrEmpty(properties.eventHubName())) {
            throw new IllegalArgumentException(String.format(Locale.US,
                "'connectionString' contains an Event Hub name [%s].  Please use the"
                    + " credentials(String connectionString) overload. Or supply a 'connectionString' without"
                    + " 'EntityPath' in it.", properties.eventHubName()));
        }

        return credential(properties.endpoint().getHost(), eventHubName, tokenCredential);
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     *
     * If not specified, the default configuration store is used to configure the {@link EventHubAsyncClient}. Use
     * {@link Configuration#NONE} to bypass using configuration settings during construction.
     *
     * @param configuration The configuration store used to configure the {@link EventHubAsyncClient}.
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
     *         {@literal "{your-namespace}.servicebus.windows.net}".
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *         Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code host} or {@code eventHubName} is null or empty.
     * @throws NullPointerException if {@code credentials} is null.
     */
    public EventHubClientBuilder credential(String host, String eventHubName, TokenCredential credential) {
        if (ImplUtils.isNullOrEmpty(host)) {
            throw new IllegalArgumentException("'host' cannot be null or empty");
        }
        if (ImplUtils.isNullOrEmpty(eventHubName)) {
            throw new IllegalArgumentException("'eventHubName' cannot be null or empty.");
        }

        Objects.requireNonNull(credential);

        this.host = host;
        this.credentials = credential;
        this.eventHubName = eventHubName;
        return this;
    }

    /**
     * Sets the proxy configuration to use for {@link EventHubAsyncClient}. When a proxy is configured, {@link
     * TransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyConfiguration The proxy configuration to use.
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
     * @param scheduler The scheduler for operations such as connecting to and receiving or sending data to
     *         Event Hubs.
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
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder retry(RetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Creates a new {@link EventHubAsyncClient} based on options set on this builder. Every time {@code
     * buildAsyncClient()} is invoked, a new instance of {@link EventHubAsyncClient} is created.
     *
     * <p>
     * The following options are used if ones are not specified in the builder:
     *
     * <ul>
     * <li>If no configuration is specified, the {@link ConfigurationManager#getConfiguration() global configuration}
     * is used to provide any shared configuration values. The configuration values read are the {@link
     * BaseConfigurations#HTTP_PROXY}, {@link ProxyConfiguration#PROXY_USERNAME}, and {@link
     * ProxyConfiguration#PROXY_PASSWORD}.</li>
     * <li>If no retry is specified, the default retry options are used.</li>
     * <li>If no proxy is specified, the builder checks the {@link ConfigurationManager#getConfiguration() global
     * configuration} for a configured proxy, then it checks to see if a system proxy is configured.</li>
     * <li>If no timeout is specified, a {@link ClientConstants#OPERATION_TIMEOUT timeout of one minute} is used.</li>
     * <li>If no scheduler is specified, an {@link Schedulers#elastic() elastic scheduler} is used.</li>
     * </ul>
     *
     * @return A new {@link EventHubAsyncClient} instance with all the configured options.
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     *         #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is
     *         specified but the transport type is not {@link TransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubAsyncClient buildAsyncClient() {
        configuration = configuration == null ? ConfigurationManager.getConfiguration().clone() : configuration;

        if (credentials == null) {
            final String connectionString = configuration.get(AZURE_EVENT_HUBS_CONNECTION_STRING);

            if (ImplUtils.isNullOrEmpty(connectionString)) {
                throw new IllegalArgumentException("Credentials have not been set using 'EventHubClientBuilder.credentials(String)'"
                    + "EventHubClientBuilder.credentials(String, String, TokenCredential). And the connection string is"
                    + "not set in the '" + AZURE_EVENT_HUBS_CONNECTION_STRING + "' environment variable.");
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
            throw new IllegalArgumentException("Cannot use a proxy when TransportType is not AMQP.");
        }

        if (proxyConfiguration == null) {
            proxyConfiguration = getDefaultProxyConfiguration(configuration);
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final CBSAuthorizationType authorizationType = credentials instanceof EventHubSharedAccessKeyCredential
            ? CBSAuthorizationType.SHARED_ACCESS_SIGNATURE
            : CBSAuthorizationType.JSON_WEB_TOKEN;
        final ConnectionOptions parameters = new ConnectionOptions(host, eventHubName, credentials, authorizationType,
            transport, retryOptions, proxyConfiguration, scheduler);

        return new EventHubAsyncClient(parameters, provider, handlerProvider);
    }

    private ProxyConfiguration getDefaultProxyConfiguration(Configuration configuration) {
        ProxyAuthenticationType authentication = ProxyAuthenticationType.NONE;
        if (proxyConfiguration != null) {
            authentication = proxyConfiguration.authentication();
        }

        String proxyAddress = configuration.get(BaseConfigurations.HTTP_PROXY);

        if (ImplUtils.isNullOrEmpty(proxyAddress)) {
            return ProxyConfiguration.SYSTEM_DEFAULTS;
        }

        final String[] hostPort = proxyAddress.split(":");
        if (hostPort.length < 2) {
            throw new IllegalArgumentException("HTTP_PROXY cannot be parsed into a proxy");
        }

        final String host = hostPort[0];
        final int port = Integer.parseInt(hostPort[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        final String username = configuration.get(ProxyConfiguration.PROXY_USERNAME);
        final String password = configuration.get(ProxyConfiguration.PROXY_PASSWORD);

        return new ProxyConfiguration(authentication, proxy, username, password);
    }

    /**
     * This property must be set for building an {@link EventProcessor}.
     *
     * Sets the consumer group name from which the {@link EventProcessor} should consume events from.
     *
     * @param consumerGroupName The consumer group name this {@link EventProcessor} should consume events
     *         from.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder consumerGroupName(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        return this;
    }

    /**
     * This property can be optionally set when building an {@link EventProcessor}.
     *
     * Sets the initial event position. If this property is not set and if checkpoint for a partition doesn't exist,
     * {@link EventPosition#earliest()} will be used as the initial event position to start consuming events.
     *
     * @param initialEventPosition The initial event position.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder initialEventPosition(EventPosition initialEventPosition) {
        this.initialEventPosition = initialEventPosition;
        return this;
    }

    /**
     * This property must be set when building an {@link EventProcessor}.
     *
     * Sets the {@link PartitionManager} the {@link EventProcessor} will use for storing partition
     * ownership and checkpoint information.
     *
     * @param partitionManager Implementation of {@link PartitionManager}.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder partitionManager(PartitionManager partitionManager) {
        // If this is not set, look for classes implementing PartitionManager interface
        // in the classpath and use it automatically. (To be implemented)
        this.partitionManager = partitionManager;
        return this;
    }

    /**
     * This property must be set when building an {@link EventProcessor}.
     *
     * Sets the partition processor factory for creating new instance(s) of {@link PartitionProcessor}.
     *
     * @param partitionProcessorFactory The factory that creates new processor for each partition.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder partitionProcessorFactory(PartitionProcessorFactory partitionProcessorFactory) {
        this.partitionProcessorFactory = partitionProcessorFactory;
        return this;
    }

    /**
     * This will create a new {@link EventProcessor} configured with the options set in this builder. Each call
     * to this method will return a new instance of {@link EventProcessor}.
     *
     * <p>
     * A new instance of {@link EventHubAsyncClient} will be created with configured options by calling the {@link
     * #buildAsyncClient()} that will be used by the {@link EventProcessor}.
     * </p>
     *
     * <p>
     * If the {@link #initialEventPosition(EventPosition) initial event position} is not set, all partitions processed by
     * this {@link EventProcessor} will start processing from {@link EventPosition#earliest() earliest}
     * available event in the respective partitions.
     * </p>
     *
     * @return A new instance of {@link EventProcessor}.
     */
    public EventProcessor buildEventProcessor() {
        EventPosition initialEventPosition =
            this.initialEventPosition == null ? EventPosition.earliest()
                : this.initialEventPosition;

        return new EventProcessor(buildAsyncClient(), this.consumerGroupName,
            this.partitionProcessorFactory, initialEventPosition, partitionManager, eventHubName);
    }
}
