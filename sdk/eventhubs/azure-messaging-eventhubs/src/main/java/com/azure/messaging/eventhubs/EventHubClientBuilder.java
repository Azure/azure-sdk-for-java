// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpClientOptions;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.client.traits.AmqpTrait;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.ConnectionHandler;
import com.azure.core.amqp.implementation.handler.WebSocketsConnectionHandler;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.annotation.ServiceClientProtocol;
import com.azure.core.client.traits.AzureNamedKeyCredentialTrait;
import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import org.apache.qpid.proton.engine.SslDomain;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_NAMESPACE_VALUE;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.CONNECTION_ID_KEY;

/**
 * This class provides a fluent builder API to aid the instantiation of {@link EventHubProducerAsyncClient}, {@link
 * EventHubProducerClient}, {@link EventHubConsumerAsyncClient}, and {@link EventHubConsumerClient}. Calling any of the
 * {@code .build*Client()} methods will create an instance of the respective client.
 *
 * <p>
 * <strong>Credentials are required</strong> to perform operations against Azure Event Hubs. They can be set by using
 * one of the following methods:
 * <ul>
 *      <li>{@link #connectionString(String)} with a connection string to a specific Event Hub.</li>
 *      <li>{@link #connectionString(String, String)} with an Event Hub <i>namespace</i> connection string and the Event
 *      Hub name.</li>
 *      <li>{@link #credential(String, String, TokenCredential)} with the fully qualified namespace, Event Hub name, and
 *      a set of credentials authorized to use the Event Hub.</li>
 *      <li>{@link #credential(String, String, AzureSasCredential)} with the fully qualified namespace, Event Hub name,
 *      and a shared access signature for the Event Hub.</li>
 *      <li>{@link #credential(String, String, AzureNamedKeyCredential)} with the fully qualified namespace,
 *      Event Hub name, and a named key credential.  The named key can be found in the Azure Portal by navigating to the
 *      Event Hub resource, selecting "Shared access policies" under the Settings section.</li>
 *      <li>{@link #credential(TokenCredential)}, {@link #credential(AzureSasCredential)}, and
 *      {@link #credential(AzureNamedKeyCredential)} overloads can be used with its respective credentials.
 *      {@link #fullyQualifiedNamespace(String)} and {@link #eventHubName(String)} must be set as well.</li>
 * </ul>
 *
 * <p>
 * In addition, the {@link #consumerGroup(String)} is required when creating {@link EventHubConsumerAsyncClient} or
 * {@link EventHubConsumerClient}.
 * </p>
 *
 * <p>The credential used in the following samples is {@code DefaultAzureCredential} for authentication. It is
 * appropriate for most scenarios, including local development and production environments. Additionally, we recommend
 * using
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments.  You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme">Azure Identity documentation"</a>.
 * </p>
 *
 * <p><strong>Sample: Construct a {@link EventHubProducerAsyncClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link EventHubProducerAsyncClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name.
 * It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal. The
 * credential used is {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and
 * development and chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerasyncclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubProducerAsyncClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .buildAsyncProducerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerasyncclient.construct -->
 *
 * <p><strong>Sample: Construct a {@link EventHubConsumerAsyncClient}</strong></p>
 *
 * <p>The following code sample demonstrates the creation of the asynchronous client
 * {@link EventHubConsumerAsyncClient}.  The {@code fullyQualifiedNamespace} is the Event Hubs Namespace's host name.
 * It is listed under the "Essentials" panel after navigating to the Event Hubs Namespace via Azure Portal. The
 * {@code consumerGroup} is found by navigating to the Event Hub instance, and selecting "Consumer groups" under the
 * "Entities" panel.  The {@link #consumerGroup(String)} is required for creating consumer clients.  The credential
 * used is {@code DefaultAzureCredential} because it combines commonly used credentials in deployment and development
 * and chooses the credential to used based on its running environment.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerasyncclient.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubConsumerAsyncClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .consumerGroup&#40;EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME&#41;
 *     .buildAsyncConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerasyncclient.construct -->
 *
 * <p><strong>Sample: Creating a client using web sockets and custom retry options</strong></p>
 *
 * <p>By default, the AMQP port 5671 is used, but clients can use web sockets, port 443.  Customers can replace the
 * {@link AmqpRetryOptions#AmqpRetryOptions() default retry options} with their own policy.
 * The retry options are used when recovering from transient failures in the underlying AMQP connection and performing
 * any operations that require a response from the service.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubproducerclient.websockets.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * AmqpRetryOptions customRetryOptions = new AmqpRetryOptions&#40;&#41;
 *     .setMaxRetries&#40;5&#41;
 *     .setMode&#40;AmqpRetryMode.FIXED&#41;
 *     .setTryTimeout&#40;Duration.ofSeconds&#40;60&#41;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubProducerClient producer = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .transportType&#40;AmqpTransportType.AMQP_WEB_SOCKETS&#41;
 *     .buildProducerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubproducerclient.websockets.construct -->
 *
 * <p><strong>Sample: Creating producers and consumers that share the same connection</strong></p>
 *
 * <p>By default, a dedicated connection is created for each producer and consumer created from the builder. If users
 * wish to use the same underlying connection, they can toggle {@link #shareConnection()}.  This underlying connection
 * is closed when all clients created from this builder instance are disposed.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubclientbuilder.shareconnection.construct -->
 * <pre>
 * TokenCredential credential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; &quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot; will look similar to &quot;&#123;your-namespace&#125;.servicebus.windows.net&quot;
 * &#47;&#47; &quot;&lt;&lt;event-hub-name&gt;&gt;&quot; will be the name of the Event Hub instance you created inside the Event Hubs namespace.
 * EventHubClientBuilder builder = new EventHubClientBuilder&#40;&#41;
 *     .credential&#40;&quot;&lt;&lt;fully-qualified-namespace&gt;&gt;&quot;, &quot;&lt;&lt;event-hub-name&gt;&gt;&quot;,
 *         credential&#41;
 *     .shareConnection&#40;&#41;;
 *
 * &#47;&#47; Both the producer and consumer created share the same underlying connection.
 * EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient&#40;&#41;;
 * EventHubConsumerAsyncClient consumer = builder
 *     .consumerGroup&#40;&quot;my-consumer-group&quot;&#41;
 *     .buildAsyncConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubclientbuilder.shareconnection.construct -->
 *
 * @see EventHubProducerAsyncClient
 * @see EventHubProducerClient
 * @see EventHubConsumerAsyncClient
 * @see EventHubConsumerClient
 */
@ServiceClientBuilder(serviceClients = {EventHubProducerAsyncClient.class, EventHubProducerClient.class,
    EventHubConsumerAsyncClient.class, EventHubConsumerClient.class}, protocol = ServiceClientProtocol.AMQP)
public class EventHubClientBuilder implements
    TokenCredentialTrait<EventHubClientBuilder>,
    AzureNamedKeyCredentialTrait<EventHubClientBuilder>,
    ConnectionStringTrait<EventHubClientBuilder>,
    AzureSasCredentialTrait<EventHubClientBuilder>,
    AmqpTrait<EventHubClientBuilder>,
    ConfigurationTrait<EventHubClientBuilder> {

    // Default number of events to fetch when creating the consumer.
    static final int DEFAULT_PREFETCH_COUNT = 500;

    // Default number of events to fetch for a sync client. The sync client operates in "pull" mode.
    // So, limit the prefetch to just 1 by default.
    static final int DEFAULT_PREFETCH_COUNT_FOR_SYNC_CLIENT = 1;

    static final AmqpRetryOptions DEFAULT_RETRY = new AmqpRetryOptions()
        .setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    /**
     * The name of the default consumer group in the Event Hubs service.
     */
    public static final String DEFAULT_CONSUMER_GROUP_NAME = "$Default";
    /**
     * The minimum value allowed for the prefetch count of the consumer.
     */
    private static final int MINIMUM_PREFETCH_COUNT = 1;
    /**
     * The maximum value allowed for the prefetch count of the consumer.
     */
    private static final int MAXIMUM_PREFETCH_COUNT = 8000;

    private static final String EVENTHUBS_PROPERTIES_FILE = "azure-messaging-eventhubs.properties";
    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";

    private static final String LIBRARY_NAME;
    private static final String LIBRARY_VERSION;
    private static final String UNKNOWN = "UNKNOWN";

    private static final String AZURE_EVENT_HUBS_CONNECTION_STRING = "AZURE_EVENT_HUBS_CONNECTION_STRING";

    private static final ClientLogger LOGGER = new ClientLogger(EventHubClientBuilder.class);
    private final Object connectionLock = new Object();
    private final AtomicBoolean isSharedConnection = new AtomicBoolean();
    private TokenCredential credentials;
    private Configuration configuration;
    private ProxyOptions proxyOptions;
    private AmqpRetryOptions retryOptions;
    private Scheduler scheduler;
    private AmqpTransportType transport;
    private String fullyQualifiedNamespace;
    private String eventHubName;
    private String consumerGroup;
    private EventHubConnectionProcessor eventHubConnectionProcessor;
    private Integer prefetchCount;
    private ClientOptions clientOptions;
    private SslDomain.VerifyMode verifyMode;

    private URI customEndpointAddress;
    private ConnectionStringProperties connectionStringProperties;

    static {
        final Map<String, String> properties = CoreUtils.getProperties(EVENTHUBS_PROPERTIES_FILE);
        LIBRARY_NAME = properties.getOrDefault(NAME_KEY, UNKNOWN);
        LIBRARY_VERSION = properties.getOrDefault(VERSION_KEY, UNKNOWN);
    }

    /**
     * Keeps track of the open clients that were created from this builder when there is a shared connection.
     */
    private final AtomicInteger openClients = new AtomicInteger();

    /**
     * Creates a new instance with the default transport {@link AmqpTransportType#AMQP} and a non-shared connection. A
     * non-shared connection means that a dedicated AMQP connection is created for every Event Hub consumer or producer
     * created using the builder.
     */
    public EventHubClientBuilder() {
        transport = AmqpTransportType.AMQP;
    }

    /**
     * Creates a TokenCredential from the {@link ConnectionStringProperties}.
     *
     * @param properties Connection string components to create TokenCredential from.
     *
     * @return A {@link TokenCredential} represented by the connection string properties.
     */
    private TokenCredential getTokenCredential(ConnectionStringProperties properties) {
        TokenCredential tokenCredential;
        if (properties.getSharedAccessSignature() == null) {
            tokenCredential = new EventHubSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } else {
            tokenCredential = new EventHubSharedKeyCredential(properties.getSharedAccessSignature());
        }
        return tokenCredential;
    }

    /**
     * Sets the client options.
     *
     * @param clientOptions The client options.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    @Override
    public EventHubClientBuilder clientOptions(ClientOptions clientOptions) {
        this.clientOptions = clientOptions;
        return this;
    }

    /***
     * Gets the client options.
     */
    ClientOptions getClientOptions() {
        return clientOptions;
    }

    /**
     * Sets the credential information given a connection string to the Event Hub instance or the Event Hubs namespace.
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
     * @param connectionString The connection string to use for connecting to the Event Hub instance or Event Hubs
     *     instance. It is expected that the Event Hub name and the shared access key properties are contained in this
     *     connection string.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code connectionString} is null or empty. If {@code fullyQualifiedNamespace}
     *     in the connection string is null.
     * @throws NullPointerException if a credential could not be extracted
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
     */
    @Override
    public EventHubClientBuilder connectionString(String connectionString) {
        this.connectionStringProperties = new ConnectionStringProperties(connectionString);

        this.fullyQualifiedNamespace = Objects.requireNonNull(connectionStringProperties.getEndpoint().getHost(),
            "'fullyQualifiedNamespace' cannot be null.");
        this.credentials = getTokenCredential(connectionStringProperties);

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'host' cannot be an empty string."));
        }

        if (!CoreUtils.isNullOrEmpty(connectionStringProperties.getEntityPath())) {
            this.eventHubName = connectionStringProperties.getEntityPath();
        }

        return this;
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
     * @throws NullPointerException if {@code connectionString} or {@code eventHubName} is null.
     * @throws IllegalArgumentException if {@code connectionString} or {@code eventHubName} is an empty string. Or,
     *     if the {@code connectionString} contains the Event Hub name.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
     */
    public EventHubClientBuilder connectionString(String connectionString, String eventHubName) {
        Objects.requireNonNull(connectionString, "'connectionString' cannot be null.");
        Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (connectionString.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'connectionString' cannot be an empty string."));
        } else if (eventHubName.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }

        this.connectionStringProperties = new ConnectionStringProperties(connectionString);
        TokenCredential tokenCredential = getTokenCredential(connectionStringProperties);

        if (!CoreUtils.isNullOrEmpty(connectionStringProperties.getEntityPath())
            && !eventHubName.equals(connectionStringProperties.getEntityPath())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "'connectionString' contains an Event Hub name [%s] and it does not match the given "
                    + "'eventHubName' parameter [%s]. Please use the credentials(String connectionString) overload. "
                    + "Or supply a 'connectionString' without 'EntityPath' in it.",
                connectionStringProperties.getEntityPath(), eventHubName)));
        }

        return credential(connectionStringProperties.getEndpoint().getHost(), eventHubName, tokenCredential);
    }

    /**
     * Sets the configuration store that is used during construction of the service client.
     * <p>
     * If not specified, the default configuration store is used to configure the {@link EventHubAsyncClient}. Use
     * {@link Configuration#NONE} to bypass using configuration settings during construction.
     * </p>
     * @param configuration The configuration store used to configure the {@link EventHubAsyncClient}.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    @Override
    public EventHubClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Gets the configuration to use.
     */
    Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Sets a custom endpoint address when connecting to the Event Hubs service. This can be useful when your network
     * does not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through
     * an intermediary. For example: {@literal https://my.custom.endpoint.com:55300}.
     * <p>
     * If no port is specified, the default port for the {@link #transportType(AmqpTransportType) transport type} is
     * used.
     *
     * @param customEndpointAddress The custom endpoint address.
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code customEndpointAddress} cannot be parsed into a valid {@link URL}.
     */
    public EventHubClientBuilder customEndpointAddress(String customEndpointAddress) {
        if (customEndpointAddress == null) {
            this.customEndpointAddress = null;
            return this;
        }

        try {
            this.customEndpointAddress = new URI(customEndpointAddress);
        } catch (URISyntaxException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(customEndpointAddress + " : is not a valid URL.", e));
        }

        return this;
    }

    /**
     * Gets the custom endpoint address.
     */
    URI getCustomEndpointAddress() {
        return this.customEndpointAddress;
    }

    /**
     * Sets the fully qualified name for the Event Hubs namespace.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} is an empty string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace} is null.
     */
    public EventHubClientBuilder fullyQualifiedNamespace(String fullyQualifiedNamespace) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        }
        return this;
    }

    /**
     * Gets the fully qualified namespace.
     */
    String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Sets the name of the Event Hub to connect the client to.
     *
     * @param eventHubName The name of the Event Hub to connect the client to.

     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code eventHubName} is an empty string.
     * @throws NullPointerException if {@code eventHubName} is null.
     */
    public EventHubClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }
        return this;
    }

    /**
     * Gets the Event Hub name.
     */
    String getEventHubName() {
        return eventHubName;
    }

    /**
     * Toggles the builder to use the same connection for producers or consumers that are built from this instance. By
     * default, a new connection is constructed and used created for each Event Hub consumer or producer created.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder shareConnection() {
        this.isSharedConnection.set(true);
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
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     *     null.
     */
    public EventHubClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
        TokenCredential credential) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'host' cannot be an empty string."));
        } else if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }

        return this;
    }

    /**
     * Sets the {@link TokenCredential} used to authorize requests sent to the service. Refer to the Azure SDK for Java
     * <a href="https://aka.ms/azsdk/java/docs/identity">identity and authentication</a>
     * documentation for more details on proper usage of the {@link TokenCredential} type.
     *
     * @param credential The token credential to use for authorization. Access controls may be specified by the
     *     Event Hubs namespace or the requested Event Hub, depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws NullPointerException if {@code credentials} is null.
     */
    @Override
    public EventHubClientBuilder credential(TokenCredential credential) {
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The shared access name and key credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     *     null.
     */
    public EventHubClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                            AzureNamedKeyCredential credential) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'host' cannot be an empty string."));
        } else if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }

        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credentials = new EventHubSharedKeyCredential(credential.getAzureNamedKey().getName(),
            credential.getAzureNamedKey().getKey(), ClientConstants.TOKEN_VALIDITY);

        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param credential The shared access name and key credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws NullPointerException if {@code credentials} is null.
     */
    @Override
    public EventHubClientBuilder credential(AzureNamedKeyCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credentials = new EventHubSharedKeyCredential(credential.getAzureNamedKey().getName(),
            credential.getAzureNamedKey().getKey(), ClientConstants.TOKEN_VALIDITY);
        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param fullyQualifiedNamespace The fully qualified name for the Event Hubs namespace. This is likely to be
     *     similar to <strong>{@literal "{your-namespace}.servicebus.windows.net}"</strong>.
     * @param eventHubName The name of the Event Hub to connect the client to.
     * @param credential The shared access signature credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code fullyQualifiedNamespace} or {@code eventHubName} is an empty
     *     string.
     * @throws NullPointerException if {@code fullyQualifiedNamespace}, {@code eventHubName}, {@code credentials} is
     *     null.
     */
    public EventHubClientBuilder credential(String fullyQualifiedNamespace, String eventHubName,
                                            AzureSasCredential credential) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'host' cannot be an empty string."));
        } else if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }

        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credentials = new EventHubSharedKeyCredential(credential.getSignature());

        return this;
    }

    /**
     * Sets the credential information for which Event Hub instance to connect to, and how to authorize against it.
     *
     * @param credential The shared access signature credential to use for authorization.
     *     Access controls may be specified by the Event Hubs namespace or the requested Event Hub,
     *     depending on Azure configuration.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws NullPointerException if {@code credentials} is null.
     */
    @Override
    public EventHubClientBuilder credential(AzureSasCredential credential) {
        Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.credentials = new EventHubSharedKeyCredential(credential.getSignature());
        return this;
    }

    /**
     * Gets the credentials.
     */
    TokenCredential getCredentials() {
        return credentials;
    }

    /**
     * Sets the proxy configuration to use for {@link EventHubAsyncClient}. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    @Override
    public EventHubClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Gets proxy options.
     */
    ProxyOptions getProxyOptions() {
        return proxyOptions;
    }

    /**
     * Sets the transport type by which all the communication with Azure Event Hubs occurs. Default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transport The transport type to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    @Override
    public EventHubClientBuilder transportType(AmqpTransportType transport) {
        this.transport = transport;
        return this;
    }

    /**
     * Gets the transport type.
     *
     * @return The transport type.
     */
    AmqpTransportType getTransportType() {
        return transport;
    }

    /**
     * Sets the retry policy for {@link EventHubAsyncClient}. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @deprecated Replaced by {@link #retryOptions(AmqpRetryOptions)}.
     */
    @Deprecated
    public EventHubClientBuilder retry(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the retry policy for {@link EventHubAsyncClient}. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry policy to use.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    @Override
    public EventHubClientBuilder retryOptions(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Gets the retry options.
     */
    AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }

    /**
     * Sets the name of the consumer group this consumer is associated with. Events are read in the context of this
     * group. The name of the consumer group that is created by default is {@link #DEFAULT_CONSUMER_GROUP_NAME
     * "$Default"}.
     *
     * @param consumerGroup The name of the consumer group this consumer is associated with. Events are read in the
     *     context of this group. The name of the consumer group that is created by default is {@link
     *     #DEFAULT_CONSUMER_GROUP_NAME "$Default"}.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    public EventHubClientBuilder consumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
        return this;
    }

    /**
     * Gets the consumer group.
     */
    String getConsumerGroup() {
        return consumerGroup;
    }

    /**
     * Sets the count used by the receiver to control the number of events per partition the Event Hub consumer will actively receive
     * and queue locally without regard to whether a receive operation is currently active.
     *
     * @param prefetchCount The amount of events per partition to queue locally. Defaults to 500 events per partition.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     * @throws IllegalArgumentException if {@code prefetchCount} is less than {@link #MINIMUM_PREFETCH_COUNT 1} or
     *     greater than {@link #MAXIMUM_PREFETCH_COUNT 8000}.
     */
    public EventHubClientBuilder prefetchCount(int prefetchCount) {
        if (prefetchCount < MINIMUM_PREFETCH_COUNT) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "PrefetchCount, '%s' has to be above %s", prefetchCount, MINIMUM_PREFETCH_COUNT)));
        }

        if (prefetchCount > MAXIMUM_PREFETCH_COUNT) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "PrefetchCount, '%s', has to be below %s", prefetchCount, MAXIMUM_PREFETCH_COUNT)));
        }

        this.prefetchCount = prefetchCount;
        return this;
    }

    /**
     * Gets the prefetch count.
     *
     * @return Gets the prefetch count or {@code null} if it has not been set.
     * @see #DEFAULT_PREFETCH_COUNT for default prefetch count.
     */
    Integer getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Gets the connection string properties.
     *
     * @return the connection string properties.
     */
    ConnectionStringProperties getConnectionStringProperties() {
        return connectionStringProperties;
    }

    /**
     * Sets the connection string properties.
     *
     * @param connectionStringProperties the connection string properties to set.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    EventHubClientBuilder setConnectionStringProperties(ConnectionStringProperties connectionStringProperties) {
        this.connectionStringProperties = connectionStringProperties;
        return this;
    }

    /**
     * Sets the scheduler for the created Event Hub client.
     *
     * @param scheduler Scheduler to set.
     *
     * @return The updated {@link EventHubClientBuilder} object.
     */
    EventHubClientBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Gets the scheduler used to subscribe Event Hub operations on.
     *
     * @return The scheduler.
     */
    Scheduler getScheduler() {
        return this.scheduler;
    }

    /**
     * Sets the verify mode for this connection.
     *
     * @param verifyMode The verification mode.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    EventHubClientBuilder verifyMode(SslDomain.VerifyMode verifyMode) {
        this.verifyMode = verifyMode;
        return this;
    }

    /**
     * Gets the verify mode.
     */
    SslDomain.VerifyMode getVerifyMode() {
        return verifyMode;
    }

    /**
     * Creates a new {@link EventHubConsumerAsyncClient} based on the options set on this builder. Every time {@code
     * buildAsyncConsumer()} is invoked, a new instance of {@link EventHubConsumerAsyncClient} is created.
     *
     * @return A new {@link EventHubConsumerAsyncClient} with the configured options.
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     *     either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Also, if
     *     {@link #consumerGroup(String)} have not been set. And if a proxy is specified but the transport type is not
     *     {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.  Or, if the {@code eventHubName} has not been set.
     */
    public EventHubConsumerAsyncClient buildAsyncConsumerClient() {
        if (CoreUtils.isNullOrEmpty(consumerGroup)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'consumerGroup' cannot be null or an empty "
                + "string. using EventHubClientBuilder.consumerGroup(String)"));
        }

        return buildAsyncClient().createConsumer(consumerGroup, prefetchCount, false);
    }

    /**
     * Creates a new {@link EventHubConsumerClient} based on the options set on this builder. Every time {@code
     * buildConsumer()} is invoked, a new instance of {@link EventHubConsumerClient} is created.
     *
     * @return A new {@link EventHubConsumerClient} with the configured options.
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     *     either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Also, if
     *     {@link #consumerGroup(String)} have not been set. And if a proxy is specified but the transport type is not
     *     {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.  Or, if the {@code eventHubName} has not been set.
     */
    public EventHubConsumerClient buildConsumerClient() {
        return buildClient().createConsumer(consumerGroup, prefetchCount);
    }

    /**
     * Creates a new {@link EventHubProducerAsyncClient} based on options set on this builder. Every time {@code
     * buildAsyncProducer()} is invoked, a new instance of {@link EventHubProducerAsyncClient} is created.
     *
     * @return A new {@link EventHubProducerAsyncClient} instance with all the configured options.
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     *     either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a
     *     proxy is specified but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
     *     Or, if the {@code eventHubName} has not been set.
     */
    public EventHubProducerAsyncClient buildAsyncProducerClient() {
        return buildAsyncClient().createProducer();
    }

    /**
     * Creates a new {@link EventHubProducerClient} based on options set on this builder. Every time {@code
     * buildAsyncProducer()} is invoked, a new instance of {@link EventHubProducerClient} is created.
     *
     * @return A new {@link EventHubProducerClient} instance with all the configured options.
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     *     either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a
     *     proxy is specified but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
     *     Or, if the {@code eventHubName} has not been set.
     */
    public EventHubProducerClient buildProducerClient() {
        return buildClient().createProducer();
    }

    /**
     * Creates a new {@link EventHubAsyncClient} based on options set on this builder. Every time {@code
     * buildAsyncClient()} is invoked, a new instance of {@link EventHubAsyncClient} is created.
     *
     * <p>
     * The following options are used if ones are not specified in the builder:
     *
     * <ul>
     * <li>If no configuration is specified, the {@link Configuration#getGlobalConfiguration() global configuration}
     * is used to provide any shared configuration values. The configuration values read are the {@link
     * Configuration#PROPERTY_HTTP_PROXY}, {@link ProxyOptions#PROXY_USERNAME}, and {@link
     * ProxyOptions#PROXY_PASSWORD}.</li>
     * <li>If no retry is specified, the default retry options are used.</li>
     * <li>If no proxy is specified, the builder checks the {@link Configuration#getGlobalConfiguration() global
     * configuration} for a configured proxy, then it checks to see if a system proxy is configured.</li>
     * <li>If no timeout is specified, a {@link ClientConstants#OPERATION_TIMEOUT timeout of one minute} is used.</li>
     * </ul>
     *
     * @return A new {@link EventHubAsyncClient} instance with all the configured options.
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     *     #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is
     *     specified but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}. Or, if the
     *     {@code eventHubName} has not been set.
     */
    EventHubAsyncClient buildAsyncClient() {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.boundedElastic();
        }

        if (prefetchCount == null) {
            prefetchCount = DEFAULT_PREFETCH_COUNT;
        }

        final Meter meter = MeterProvider.getDefaultProvider().createMeter(LIBRARY_NAME, LIBRARY_VERSION,
            clientOptions == null ? null : clientOptions.getMetricsOptions());

        final MessageSerializer messageSerializer = new EventHubMessageSerializer();

        final EventHubConnectionProcessor processor;
        if (isSharedConnection.get()) {
            synchronized (connectionLock) {
                if (eventHubConnectionProcessor == null) {
                    eventHubConnectionProcessor = buildConnectionProcessor(messageSerializer, meter);
                }
            }

            processor = eventHubConnectionProcessor;

            final int numberOfOpenClients = openClients.incrementAndGet();
            LOGGER.info("# of open clients with shared connection: {}", numberOfOpenClients);
        } else {
            processor = buildConnectionProcessor(messageSerializer, meter);
        }

        String identifier;
        if (clientOptions instanceof AmqpClientOptions) {
            String clientOptionIdentifier = ((AmqpClientOptions) clientOptions).getIdentifier();
            identifier = CoreUtils.isNullOrEmpty(clientOptionIdentifier) ? UUID.randomUUID().toString() : clientOptionIdentifier;
        } else {
            identifier = UUID.randomUUID().toString();
        }

        return new EventHubAsyncClient(processor, messageSerializer, scheduler,
            isSharedConnection.get(), this::onClientClose, identifier, meter, createTracer());
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
     * Configuration#PROPERTY_HTTP_PROXY}, {@link ProxyOptions#PROXY_USERNAME}, and {@link
     * ProxyOptions#PROXY_PASSWORD}.</li>
     * <li>If no retry is specified, the default retry options are used.</li>
     * <li>If no proxy is specified, the builder checks the {@link Configuration#getGlobalConfiguration() global
     * configuration} for a configured proxy, then it checks to see if a system proxy is configured.</li>
     * <li>If no timeout is specified, a {@link ClientConstants#OPERATION_TIMEOUT timeout of one minute} is used.</li>
     * <li>If no scheduler is specified, an {@link Schedulers#boundedElastic() elastic scheduler} is used.</li>
     * </ul>
     *
     * @return A new {@link EventHubClient} instance with all the configured options.
     * @throws IllegalArgumentException if the credentials have not been set using either {@link
     *     #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Or, if a proxy is
     *     specified but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    EventHubClient buildClient() {
        if (prefetchCount == null) {
            // For sync clients, do not prefetch eagerly as the client can "pull" as many events as required.
            prefetchCount = DEFAULT_PREFETCH_COUNT_FOR_SYNC_CLIENT;
        }
        final EventHubAsyncClient client = buildAsyncClient();

        return new EventHubClient(client, retryOptions);
    }

    void onClientClose() {
        synchronized (connectionLock) {
            final int numberOfOpenClients = openClients.decrementAndGet();
            LOGGER.info("Closing a dependent client. # of open clients: {}", numberOfOpenClients);

            if (numberOfOpenClients > 0) {
                return;
            }

            if (numberOfOpenClients < 0) {
                LOGGER.warning("There should not be less than 0 clients. actual: {}", numberOfOpenClients);
            }

            LOGGER.info("No more open clients, closing shared connection.");
            if (eventHubConnectionProcessor != null) {
                eventHubConnectionProcessor.dispose();
                eventHubConnectionProcessor = null;
            } else {
                LOGGER.warning("Shared EventHubConnectionProcessor was already disposed.");
            }
        }
    }

    Tracer createTracer() {
        return TracerProvider.getDefaultProvider().createTracer(LIBRARY_NAME, LIBRARY_VERSION,
            AZ_NAMESPACE_VALUE, clientOptions == null ? null : clientOptions.getTracingOptions());
    }

    private EventHubConnectionProcessor buildConnectionProcessor(MessageSerializer messageSerializer, Meter meter) {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final Supplier<String> getEventHubName = () -> {
            if (CoreUtils.isNullOrEmpty(eventHubName)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
            }
            return eventHubName;
        };

        final Flux<EventHubAmqpConnection> connectionFlux = Flux.create(sink -> {
            sink.onRequest(request -> {

                if (request == 0) {
                    return;
                } else if (request > 1) {
                    sink.error(LOGGER.logExceptionAsWarning(new IllegalArgumentException(
                        "Requested more than one connection. Only emitting one. Request: " + request)));
                    return;
                }

                final String connectionId = StringUtil.getRandomString("MF");
                LOGGER.atInfo()
                    .addKeyValue(CONNECTION_ID_KEY, connectionId)
                    .log("Emitting a single connection.");

                final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
                    connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
                    connectionOptions.getAuthorizationScope());
                final ReactorProvider provider = new ReactorProvider();
                final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider, meter);
                final AmqpLinkProvider linkProvider = new AmqpLinkProvider();

                final EventHubAmqpConnection connection = new EventHubReactorAmqpConnection(connectionId,
                    connectionOptions, getEventHubName.get(), provider, handlerProvider, linkProvider, tokenManagerProvider,
                    messageSerializer);

                sink.next(connection);
            });
        });

        return connectionFlux.subscribeWith(new EventHubConnectionProcessor(
            connectionOptions.getFullyQualifiedNamespace(), getEventHubName.get(), connectionOptions.getRetry()));
    }

    ConnectionOptions getConnectionOptions() {
        Configuration buildConfiguration = configuration == null
                ? Configuration.getGlobalConfiguration().clone()
                : configuration;

        if (credentials == null) {
            final String connectionString = buildConfiguration.get(AZURE_EVENT_HUBS_CONNECTION_STRING);

            if (CoreUtils.isNullOrEmpty(connectionString)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Credentials have not been set. "
                    + "They can be set using: connectionString(String), connectionString(String, String), "
                    + "credentials(String, String, TokenCredential), or setting the environment variable '"
                    + AZURE_EVENT_HUBS_CONNECTION_STRING + "' with a connection string"));
            }

            connectionString(connectionString);
        }

        if (proxyOptions == null) {
            proxyOptions = ProxyOptions.fromConfiguration(buildConfiguration);
        }

        // If the proxy has been configured by the user but they have overridden the TransportType with something that
        // is not AMQP_WEB_SOCKETS.
        if (proxyOptions != null && proxyOptions.isProxyAddressConfigured()
            && transport != AmqpTransportType.AMQP_WEB_SOCKETS) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Cannot use a proxy when TransportType is not AMQP Web Sockets. "
                    + "Use the setter 'transportType(AmqpTransportType.AMQP_WEB_SOCKETS)' to enable Web Sockets mode."));
        }

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        }

        final CbsAuthorizationType authorizationType = credentials instanceof EventHubSharedKeyCredential
            ? CbsAuthorizationType.SHARED_ACCESS_SIGNATURE
            : CbsAuthorizationType.JSON_WEB_TOKEN;

        SslDomain.VerifyMode verificationMode = verifyMode != null
            ? verifyMode
            : SslDomain.VerifyMode.VERIFY_PEER_NAME;

        final boolean usingDevelopmentEmulator = connectionStringProperties != null
            && connectionStringProperties.useDevelopmentEmulator();

        if (usingDevelopmentEmulator) {
            verificationMode = SslDomain.VerifyMode.ANONYMOUS_PEER;
        }

        final ClientOptions options = clientOptions != null ? clientOptions : new ClientOptions();

        final String hostname;
        final int port;

        if (customEndpointAddress != null) {
            hostname = customEndpointAddress.getHost();
            port = customEndpointAddress.getPort();
        } else if (connectionStringProperties != null) {
            final URI endpoint = connectionStringProperties.getEndpoint();
            hostname = endpoint.getHost();
            port = endpoint.getPort();
        } else {
            hostname = fullyQualifiedNamespace;
            port = -1;
        }

        // No explicit port was listed, so choose a default port.
        final int portToUse = port != -1 ? port : getPort(transport, usingDevelopmentEmulator);
        final boolean enableSsl = !usingDevelopmentEmulator;

        return new ConnectionOptions(fullyQualifiedNamespace, credentials, authorizationType,
            ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE, transport, retryOptions, proxyOptions, scheduler,
            options, verificationMode, LIBRARY_NAME, LIBRARY_VERSION, hostname, portToUse, enableSsl);
    }

    private static int getPort(AmqpTransportType transport, boolean useDevelopmentEmulator) {
        if (useDevelopmentEmulator) {
            return ConnectionHandler.AMQP_PORT;
        }

        switch (transport) {
            case AMQP:
                return ConnectionHandler.AMQPS_PORT;

            case AMQP_WEB_SOCKETS:
                return WebSocketsConnectionHandler.HTTPS_PORT;

            default:
                throw LOGGER
                    .logThrowableAsError(new IllegalArgumentException("Transport Type is not supported: " + transport));
        }
    }
}
