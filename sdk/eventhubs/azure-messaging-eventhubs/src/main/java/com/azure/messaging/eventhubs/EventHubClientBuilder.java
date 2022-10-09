// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpClientOptions;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.client.traits.AmqpTrait;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.TracerProvider;
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
import com.azure.messaging.eventhubs.implementation.ClientConstants;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubConnectionProcessor;
import com.azure.messaging.eventhubs.implementation.EventHubReactorAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubSharedKeyCredential;
import org.apache.qpid.proton.engine.SslDomain;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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
 * <li>{@link #connectionString(String) connectionString(String)} with a connection string to a specific Event Hub.
 * </li>
 * <li>{@link #connectionString(String, String) connectionString(String, String)} with an Event Hub <i>namespace</i>
 * connection string and the Event Hub name.</li>
 * <li>{@link #credential(String, String, TokenCredential) credential(String, String, TokenCredential)} with the
 * fully qualified namespace, Event Hub name, and a set of credentials authorized to use the Event Hub.
 * </li>
 * </ul>
 *
 * <p>
 * In addition, the <strong>consumer group</strong> is required when creating {@link EventHubConsumerAsyncClient} or
 * {@link EventHubConsumerClient}.
 * </p>
 *
 * <p><strong>Creating an asynchronous {@link EventHubProducerAsyncClient} using Event Hubs namespace connection string
 * </strong></p>
 * <p>In the sample, the namespace connection string is used to create an asynchronous Event Hub producer. Notice that
 * {@code "EntityPath"} <b>is not</b> a component in the connection string.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation -->
 * <pre>
 * &#47;&#47; The required parameter is a way to authenticate with Event Hubs using credentials.
 * &#47;&#47; The connectionString provides a way to authenticate with Event Hub.
 * EventHubProducerAsyncClient producer = new EventHubClientBuilder&#40;&#41;
 *     .connectionString&#40;
 *         &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;,
 *         &quot;event-hub-name&quot;&#41;
 *     .buildAsyncProducerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation -->
 *
 * <p><strong>Creating a synchronous {@link EventHubConsumerClient} using an Event Hub instance connection string
 * </strong></p>
 * <p>In the sample, the namespace connection string is used to create a synchronous Event Hub consumer. Notice that
 * {@code "EntityPath"} <b>is</b> in the connection string.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation -->
 * <pre>
 * &#47;&#47; The required parameters are `consumerGroup`, and a way to authenticate with Event Hubs using credentials.
 * EventHubConsumerClient consumer = new EventHubClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;&quot;
 *         + &quot;SharedAccessKey=&#123;key&#125;;Entity-Path=&#123;hub-name&#125;&quot;&#41;
 *     .consumerGroup&#40;&quot;$DEFAULT&quot;&#41;
 *     .buildConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation -->
 *
 * <p><strong>Creating producers and consumers that share the same connection</strong></p>
 * <p>By default, a dedicated connection is created for each producer and consumer created from the builder. If users
 * wish to use the same underlying connection, they can toggle {@link #shareConnection() shareConnection()}.</p>
 *
 * <!-- src_embed com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation -->
 * <pre>
 * &#47;&#47; Toggling `shareConnection` instructs the builder to use the same underlying connection
 * &#47;&#47; for each consumer or producer created using the same builder instance.
 * EventHubClientBuilder builder = new EventHubClientBuilder&#40;&#41;
 *     .connectionString&#40;&quot;event-hubs-instance-connection-string&quot;&#41;
 *     .shareConnection&#40;&#41;;
 *
 * &#47;&#47; Both the producer and consumer created share the same underlying connection.
 * EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient&#40;&#41;;
 * EventHubConsumerAsyncClient consumer = builder
 *     .consumerGroup&#40;&quot;my-consumer-group&quot;&#41;
 *     .buildAsyncConsumerClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation -->
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
    private static final Pattern HOST_PORT_PATTERN = Pattern.compile("^[^:]+:\\d+");

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
    private URL customEndpointAddress;

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
     * @throws IllegalArgumentException if {@code connectionString} is null or empty. Or, the {@code
     *     connectionString} does not contain the "EntityPath" key, which is the name of the Event Hub instance.
     * @throws AzureException If the shared access signature token credential could not be created using the
     *     connection string.
     */
    @Override
    public EventHubClientBuilder connectionString(String connectionString) {
        ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        TokenCredential tokenCredential = getTokenCredential(properties);
        return credential(properties.getEndpoint().getHost(), properties.getEntityPath(), tokenCredential);
    }

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

        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        TokenCredential tokenCredential = getTokenCredential(properties);

        if (!CoreUtils.isNullOrEmpty(properties.getEntityPath())
            && !eventHubName.equals(properties.getEntityPath())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
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
    @Override
    public EventHubClientBuilder configuration(Configuration configuration) {
        this.configuration = configuration;
        return this;
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
            this.customEndpointAddress = new URL(customEndpointAddress);
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException(customEndpointAddress + " : is not a valid URL.", e));
        }

        return this;
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

    private String getAndValidateFullyQualifiedNamespace() {
        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        }
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

    private String getEventHubName() {
        if (CoreUtils.isNullOrEmpty(eventHubName)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'eventHubName' cannot be an empty string."));
        }
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
     * Sets the count used by the receiver to control the number of events the Event Hub consumer will actively receive
     * and queue locally without regard to whether a receive operation is currently active.
     *
     * @param prefetchCount The amount of events to queue locally.
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
     * Package-private method that gets the prefetch count.
     *
     * @return Gets the prefetch count or {@code null} if it has not been set.
     * @see #DEFAULT_PREFETCH_COUNT for default prefetch count.
     */
    Integer getPrefetchCount() {
        return prefetchCount;
    }

    /**
     * Package-private method that sets the scheduler for the created Event Hub client.
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
     * Package-private method that sets the verify mode for this connection.
     *
     * @param verifyMode The verification mode.
     * @return The updated {@link EventHubClientBuilder} object.
     */
    EventHubClientBuilder verifyMode(SslDomain.VerifyMode verifyMode) {
        this.verifyMode = verifyMode;
        return this;
    }

    /**
     * Creates a new {@link EventHubConsumerAsyncClient} based on the options set on this builder. Every time {@code
     * buildAsyncConsumer()} is invoked, a new instance of {@link EventHubConsumerAsyncClient} is created.
     *
     * @return A new {@link EventHubConsumerAsyncClient} with the configured options.
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     *     either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Also, if
     *     {@link #consumerGroup(String)} have not been set. And if a proxy is specified but the transport type is not
     *     {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
     */
    public EventHubConsumerAsyncClient buildAsyncConsumerClient() {
        if (CoreUtils.isNullOrEmpty(consumerGroup)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'consumerGroup' cannot be null or an empty "
                + "string. using EventHubClientBuilder.consumerGroup(String)"));
        }

        return buildAsyncClient().createConsumer(consumerGroup, prefetchCount);
    }

    /**
     * Creates a new {@link EventHubConsumerClient} based on the options set on this builder. Every time {@code
     * buildConsumer()} is invoked, a new instance of {@link EventHubConsumerClient} is created.
     *
     * @return A new {@link EventHubConsumerClient} with the configured options.
     * @throws IllegalArgumentException If shared connection is not used and the credentials have not been set using
     *     either {@link #connectionString(String)} or {@link #credential(String, String, TokenCredential)}. Also, if
     *     {@link #consumerGroup(String)} have not been set. And if a proxy is specified but the transport type is not
     *     {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
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
     *     specified but the transport type is not {@link AmqpTransportType#AMQP_WEB_SOCKETS web sockets}.
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

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        String identifier;
        if (clientOptions instanceof AmqpClientOptions) {
            String clientOptionIdentifier = ((AmqpClientOptions) clientOptions).getIdentifier();
            identifier = CoreUtils.isNullOrEmpty(clientOptionIdentifier) ? UUID.randomUUID().toString() : clientOptionIdentifier;
        } else {
            identifier = UUID.randomUUID().toString();
        }

        return new EventHubAsyncClient(processor, tracerProvider, messageSerializer, scheduler,
            isSharedConnection.get(), this::onClientClose, identifier, meter);
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

    private EventHubConnectionProcessor buildConnectionProcessor(MessageSerializer messageSerializer, Meter meter) {
        final ConnectionOptions connectionOptions = getConnectionOptions();
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

                final EventHubAmqpConnection connection = new EventHubReactorAmqpConnection(connectionId,
                    connectionOptions, getEventHubName(), provider, handlerProvider, tokenManagerProvider,
                    messageSerializer);

                sink.next(connection);
            });
        });

        return connectionFlux.subscribeWith(new EventHubConnectionProcessor(
            connectionOptions.getFullyQualifiedNamespace(), getEventHubName(), connectionOptions.getRetry()));
    }

    private ConnectionOptions getConnectionOptions() {
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
            proxyOptions = getDefaultProxyConfiguration(buildConfiguration);
        }

        // If the proxy has been configured by the user but they have overridden the TransportType with something that
        // is not AMQP_WEB_SOCKETS.
        if (proxyOptions != null && proxyOptions.isProxyAddressConfigured()
            && transport != AmqpTransportType.AMQP_WEB_SOCKETS) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Cannot use a proxy when TransportType is not AMQP Web Sockets."));
        }

        final CbsAuthorizationType authorizationType = credentials instanceof EventHubSharedKeyCredential
            ? CbsAuthorizationType.SHARED_ACCESS_SIGNATURE
            : CbsAuthorizationType.JSON_WEB_TOKEN;

        final SslDomain.VerifyMode verificationMode = verifyMode != null
            ? verifyMode
            : SslDomain.VerifyMode.VERIFY_PEER_NAME;

        final ClientOptions options = clientOptions != null ? clientOptions : new ClientOptions();

        if (customEndpointAddress == null) {
            return new ConnectionOptions(getAndValidateFullyQualifiedNamespace(), credentials, authorizationType,
                ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE, transport, retryOptions, proxyOptions, scheduler,
                options, verificationMode, LIBRARY_NAME, LIBRARY_VERSION);
        } else {
            return new ConnectionOptions(getAndValidateFullyQualifiedNamespace(), credentials, authorizationType,
                ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE, transport, retryOptions, proxyOptions, scheduler,
                options, verificationMode, LIBRARY_NAME, LIBRARY_VERSION, customEndpointAddress.getHost(),
                customEndpointAddress.getPort());
        }
    }

    private ProxyOptions getDefaultProxyConfiguration(Configuration configuration) {
        ProxyAuthenticationType authentication = ProxyAuthenticationType.NONE;
        if (proxyOptions != null) {
            authentication = proxyOptions.getAuthentication();
        }

        String proxyAddress = configuration.get(Configuration.PROPERTY_HTTP_PROXY);

        if (CoreUtils.isNullOrEmpty(proxyAddress)) {
            return ProxyOptions.SYSTEM_DEFAULTS;
        }

        return getProxyOptions(authentication, proxyAddress, configuration,
            Boolean.parseBoolean(configuration.get("java.net.useSystemProxies")));
    }

    private ProxyOptions getProxyOptions(ProxyAuthenticationType authentication, String proxyAddress,
                                         Configuration configuration, boolean useSystemProxies) {
        String host;
        int port;
        if (HOST_PORT_PATTERN.matcher(proxyAddress.trim()).find()) {
            final String[] hostPort = proxyAddress.split(":");
            host = hostPort[0];
            port = Integer.parseInt(hostPort[1]);
            final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            final String username = configuration.get(ProxyOptions.PROXY_USERNAME);
            final String password = configuration.get(ProxyOptions.PROXY_PASSWORD);
            return new ProxyOptions(authentication, proxy, username, password);
        } else if (useSystemProxies) {
            // java.net.useSystemProxies needs to be set to true in this scenario.
            // If it is set to false 'ProxyOptions' in azure-core will return null.
            com.azure.core.http.ProxyOptions coreProxyOptions = com.azure.core.http.ProxyOptions
                .fromConfiguration(configuration);
            Proxy.Type proxyType = coreProxyOptions.getType().toProxyType();
            InetSocketAddress coreProxyAddress = coreProxyOptions.getAddress();
            String username = coreProxyOptions.getUsername();
            String password = coreProxyOptions.getPassword();
            return new ProxyOptions(authentication, new Proxy(proxyType, coreProxyAddress), username, password);
        } else {
            LOGGER.verbose("'HTTP_PROXY' was configured but ignored as 'java.net.useSystemProxies' wasn't "
                + "set or was false.");
            return ProxyOptions.SYSTEM_DEFAULTS;
        }
    }
}
