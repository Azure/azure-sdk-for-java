// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The builder to create {@link ServiceBusReceiverAsyncClient} and {@link ServiceBusSenderAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {ServiceBusReceiverAsyncClient.class, ServiceBusSenderAsyncClient.class,
    ServiceBusSenderClient.class, ServiceBusReceiverClient.class})
public final class ServiceBusClientBuilder {
    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final AmqpRetryOptions DEFAULT_RETRY =
        new AmqpRetryOptions().setTryTimeout(ServiceBusConstants.OPERATION_TIMEOUT);

    private static final String SERVICE_BUS_PROPERTIES_FILE = "azure-messaging-servicebus.properties";
    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";
    private static final String UNKNOWN = "UNKNOWN";

    private final Object connectionLock = new Object();
    private final ClientLogger logger = new ClientLogger(ServiceBusClientBuilder.class);
    private final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();
    private final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

    private Configuration configuration;
    private ServiceBusConnectionProcessor sharedConnection;
    private String connectionStringEntityName;
    private TokenCredential credentials;
    private String fullyQualifiedNamespace;
    private ProxyOptions proxyOptions;
    private AmqpRetryOptions retryOptions;
    private Scheduler scheduler;
    private AmqpTransportType transport = AmqpTransportType.AMQP;

    /**
     * Keeps track of the open clients that were created from this builder when there is a shared connection.
     */
    private final AtomicInteger openClients = new AtomicInteger();

    /**
     * Creates a new instance with the default transport {@link AmqpTransportType#AMQP}.
     */
    public ServiceBusClientBuilder() {
    }

    /**
     * Sets the connection string for a Service Bus namespace or a specific Service Bus resource.
     *
     * @param connectionString Connection string for a Service Bus namespace or a specific Service Bus resource.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder connectionString(String connectionString) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new ServiceBusSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ServiceBusConstants.TOKEN_VALIDITY);
        } catch (Exception e) {
            throw logger.logExceptionAsError(
                new AzureException("Could not create the ServiceBusSharedKeyCredential.", e));
        }

        this.fullyQualifiedNamespace = properties.getEndpoint().getHost();

        if (properties.getEntityPath() != null && !properties.getEntityPath().isEmpty()) {
            logger.info("Setting 'entityName' [{}] from connectionString.", properties.getEntityPath());
            this.connectionStringEntityName = properties.getEntityPath();
        }

        return credential(properties.getEndpoint().getHost(), tokenCredential);
    }

    /**
     * Sets the credential for the Service Bus resource.
     *
     * @param fullyQualifiedNamespace for the Service Bus.
     * @param credential {@link TokenCredential} to be used for authentication.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder credential(String fullyQualifiedNamespace, TokenCredential credential) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        }

        return this;
    }

    /**
     * Sets the proxy configuration to use for {@link ServiceBusSenderAsyncClient}. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Sets the retry options for Service Bus clients. If not specified, the default retry options are used.
     *
     * @param retryOptions The retry options to use.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder retryOptions(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Sets the scheduler to use.
     *
     * @param scheduler Scheduler to be used.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    ServiceBusClientBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;
    }

    /**
     * Sets the transport type by which all the communication with Azure Service Bus occurs. Default value is {@link
     * AmqpTransportType#AMQP}.
     *
     * @param transportType The transport type to use.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder transportType(AmqpTransportType transportType) {
        this.transport = transportType;
        return this;
    }

    /**
     * A new instance of {@link ServiceBusSenderClientBuilder} used to configure Service Bus message senders.
     *
     * @return A new instance of {@link ServiceBusSenderClientBuilder}.
     */
    public ServiceBusSenderClientBuilder sender() {
        return new ServiceBusSenderClientBuilder();
    }

    /**
     * A new instance of {@link ServiceBusReceiverClientBuilder} used to configure Service Bus message consumers.
     *
     * @return A new instance of {@link ServiceBusReceiverClientBuilder}.
     */
    public ServiceBusReceiverClientBuilder receiver() {
        return new ServiceBusReceiverClientBuilder();
    }

    /**
     * Called when a child client is closed. Disposes of the shared connection if there are no more clients.
     */
    void onClientClose() {
        synchronized (connectionLock) {
            final int numberOfOpenClients = openClients.decrementAndGet();
            logger.info("Closing a dependent client. # of open clients: {}", numberOfOpenClients);

            if (numberOfOpenClients > 0) {
                return;
            }

            if (numberOfOpenClients < 0) {
                logger.warning("There should not be less than 0 clients. actual: {}", numberOfOpenClients);
            }

            logger.info("No more open clients, closing shared connection.");
            if (sharedConnection != null) {
                sharedConnection.dispose();
                sharedConnection = null;
            } else {
                logger.warning("Shared ServiceBusConnectionProcessor was already disposed.");
            }
        }
    }

    private ServiceBusConnectionProcessor getOrCreateConnectionProcessor(MessageSerializer serializer) {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        synchronized (connectionLock) {
            if (sharedConnection == null) {
                final ConnectionOptions connectionOptions = getConnectionOptions();
                final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
                    connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
                    ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
                final ReactorProvider provider = new ReactorProvider();
                final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);

                final Map<String, String> properties = CoreUtils.getProperties(SERVICE_BUS_PROPERTIES_FILE);
                final String product = properties.getOrDefault(NAME_KEY, UNKNOWN);
                final String clientVersion = properties.getOrDefault(VERSION_KEY, UNKNOWN);

                final Flux<ServiceBusAmqpConnection> connectionFlux = Mono.fromCallable(() -> {
                    final String connectionId = StringUtil.getRandomString("MF");

                    return (ServiceBusAmqpConnection) new ServiceBusReactorAmqpConnection(connectionId,
                        connectionOptions, provider, handlerProvider, tokenManagerProvider, serializer, product,
                        clientVersion);
                }).repeat();

                sharedConnection = connectionFlux.subscribeWith(new ServiceBusConnectionProcessor(
                    connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getRetry()));
            }
        }

        final int numberOfOpenClients = openClients.incrementAndGet();
        logger.info("# of open clients with shared connection: {}", numberOfOpenClients);

        return sharedConnection;
    }

    private ConnectionOptions getConnectionOptions() {
        configuration = configuration == null ? Configuration.getGlobalConfiguration().clone() : configuration;

        if (credentials == null) {
            final String connectionString = configuration.get(AZURE_SERVICE_BUS_CONNECTION_STRING);

            if (CoreUtils.isNullOrEmpty(connectionString)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Credentials have not been set. "
                    + "They can be set using: connectionString(String), connectionString(String, String), "
                    + "credentials(String, String, TokenCredential), or setting the environment variable '"
                    + AZURE_SERVICE_BUS_CONNECTION_STRING + "' with a connection string"));
            }

            connectionString(connectionString);
        }

        // If the proxy has been configured by the user but they have overridden the TransportType with something that
        // is not AMQP_WEB_SOCKETS.
        if (proxyOptions != null && proxyOptions.isProxyAddressConfigured()
            && transport != AmqpTransportType.AMQP_WEB_SOCKETS) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Cannot use a proxy when TransportType is not AMQP."));
        }

        if (proxyOptions == null) {
            proxyOptions = getDefaultProxyConfiguration(configuration);
        }

        final CbsAuthorizationType authorizationType = credentials instanceof ServiceBusSharedKeyCredential
            ? CbsAuthorizationType.SHARED_ACCESS_SIGNATURE
            : CbsAuthorizationType.JSON_WEB_TOKEN;

        return new ConnectionOptions(fullyQualifiedNamespace, credentials, authorizationType, transport, retryOptions,
            proxyOptions, scheduler);
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

        final String[] hostPort = proxyAddress.split(":");
        if (hostPort.length < 2) {
            throw logger.logExceptionAsError(new IllegalArgumentException("HTTP_PROXY cannot be parsed into a proxy"));
        }

        final String host = hostPort[0];
        final int port = Integer.parseInt(hostPort[1]);
        final Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        final String username = configuration.get(ProxyOptions.PROXY_USERNAME);
        final String password = configuration.get(ProxyOptions.PROXY_PASSWORD);

        return new ProxyOptions(authentication, proxy, username, password);
    }

    private static boolean isNullOrEmpty(String item) {
        return item == null || item.isEmpty();
    }

    private static MessagingEntityType validateEntityPaths(ClientLogger logger, String connectionStringEntityName,
        String topicName, String queueName) {

        final boolean hasTopicName = !isNullOrEmpty(topicName);
        final boolean hasQueueName = !isNullOrEmpty(queueName);
        final boolean hasConnectionStringEntity = !isNullOrEmpty(connectionStringEntityName);

        final MessagingEntityType entityType;

        if (!hasConnectionStringEntity && !hasQueueName && !hasTopicName) {
            throw logger.logExceptionAsError(new IllegalStateException(
                "Cannot build client without setting either a queueName or topicName."));
        } else if (hasQueueName && hasTopicName) {
            throw logger.logExceptionAsError(new IllegalStateException(String.format(
                "Cannot build client with both queueName (%s) and topicName (%s) set.", queueName, topicName)));
        } else if (hasQueueName) {
            if (hasConnectionStringEntity && !queueName.equals(connectionStringEntityName)) {
                throw logger.logExceptionAsError(new IllegalStateException(String.format(
                    "queueName (%s) is different than the connectionString's EntityPath (%s).",
                    queueName, connectionStringEntityName)));
            }

            entityType = MessagingEntityType.QUEUE;
        } else if (hasTopicName) {
            if (hasConnectionStringEntity && !topicName.equals(connectionStringEntityName)) {
                throw logger.logExceptionAsError(new IllegalStateException(String.format(
                    "topicName (%s) is different than the connectionString's EntityPath (%s).",
                    topicName, connectionStringEntityName)));
            }

            entityType = MessagingEntityType.TOPIC;
        } else {
            // It is a connection string entity path.
            entityType = MessagingEntityType.UNKNOWN;
        }

        return entityType;
    }

    /**
     * Builder for creating {@link ServiceBusSenderClient} and {@link ServiceBusSenderAsyncClient} to publish messages
     * to Service Bus.
     */
    @ServiceClientBuilder(serviceClients = {ServiceBusSenderClient.class, ServiceBusSenderAsyncClient.class})
    public final class ServiceBusSenderClientBuilder {
        private String queueName;
        private String topicName;

        private ServiceBusSenderClientBuilder() {
        }

        /**
         * Sets the name of the Service Bus queue to publish messages to.
         *
         * @param queueName Name of the queue.
         *
         * @return The modified {@link ServiceBusSenderClientBuilder} object.
         */
        public ServiceBusSenderClientBuilder queueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        /**
         * Sets the name of the Service Bus topic to publish messages to.
         *
         * @param topicName Name of the topic.
         *
         * @return The modified {@link ServiceBusSenderClientBuilder} object.
         */
        public ServiceBusSenderClientBuilder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        /**
         * Creates an <b>asynchronous</b> {@link ServiceBusSenderAsyncClient client} for transmitting {@link
         * ServiceBusMessage} to a Service Bus queue or topic.
         *
         * @return A new {@link ServiceBusSenderAsyncClient} for transmitting to a Service queue or topic.
         * @throws IllegalStateException if {@link #queueName(String) queueName} or {@link #topicName(String)
         *     topicName} are not set or, both of these fields are set. It is also thrown if the Service Bus {@link
         *     #connectionString(String) connectionString} contains an {@code EntityPath} that does not match one set in
         *     {@link #queueName(String) queueName} or {@link #topicName(String) topicName}
         * @throws IllegalArgumentException if the entity type is not a queue or a topic.
         */
        public ServiceBusSenderAsyncClient buildAsyncClient() {
            final ServiceBusConnectionProcessor connectionProcessor = getOrCreateConnectionProcessor(messageSerializer);
            final MessagingEntityType entityType = validateEntityPaths(logger, connectionStringEntityName, topicName,
                queueName);

            final String entityName;
            switch (entityType) {
                case QUEUE:
                    entityName = queueName;
                    break;
                case TOPIC:
                    entityName = topicName;
                    break;
                case UNKNOWN:
                    entityName = connectionStringEntityName;
                    break;
                default:
                    throw logger.logExceptionAsError(
                        new IllegalArgumentException("Unknown entity type: " + entityType));
            }

            return new ServiceBusSenderAsyncClient(entityName, entityType, connectionProcessor, retryOptions,
                tracerProvider, messageSerializer, ServiceBusClientBuilder.this::onClientClose);
        }

        /**
         * Creates a <b>synchronous</b> {@link ServiceBusSenderClient client} for transmitting {@link ServiceBusMessage}
         * to a Service Bus queue or topic.
         *
         * @return A new {@link ServiceBusSenderAsyncClient} for transmitting to a Service queue or topic.
         * @throws IllegalStateException if {@link #queueName(String) queueName} or {@link #topicName(String)
         *     topicName} are not set or, both of these fields are set. It is also thrown if the Service Bus {@link
         *     #connectionString(String) connectionString} contains an {@code EntityPath} that does not match one set in
         *     {@link #queueName(String) queueName} or {@link #topicName(String) topicName}
         * @throws IllegalArgumentException if the entity type is not a queue or a topic.
         */
        public ServiceBusSenderClient buildClient() {
            return new ServiceBusSenderClient(buildAsyncClient(), retryOptions.getTryTimeout());
        }
    }

    /**
     * Builder for creating {@link ServiceBusReceiverClient} and {@link ServiceBusReceiverAsyncClient} to consume
     * messages from Service Bus.
     */
    @ServiceClientBuilder(serviceClients = {ServiceBusReceiverClient.class, ServiceBusReceiverAsyncClient.class})
    public final class ServiceBusReceiverClientBuilder {
        // Using 0 pre-fetch count for both receive modes, to avoid message lock lost exceptions in application
        // receiving messages at a slow rate. Applications can set it to a higher value if they need better performance.
        private static final int DEFAULT_PREFETCH_COUNT = 1;

        private int prefetchCount = DEFAULT_PREFETCH_COUNT;
        private String queueName;
        private String subscriptionName;
        private String topicName;
        private ReceiveMode receiveMode = ReceiveMode.PEEK_LOCK;

        private ServiceBusReceiverClientBuilder() {
        }

        /**
         * Sets the prefetch count of the receiver. Prefetch speeds up the message flow by aiming to have a message
         * readily available for local retrieval when and before the application asks for one using {@link
         * ServiceBusReceiverAsyncClient#receive()}. Setting a non-zero value will prefetch that number of messages.
         * Setting the value to zero turns prefetch off. For both {@link ReceiveMode#PEEK_LOCK PEEK_LOCK} and {@link
         * ReceiveMode#RECEIVE_AND_DELETE RECEIVE_AND_DELETE} modes the default value is 1.
         *
         * @param prefetchCount The prefetch count.
         *
         * @return The modified {@link ServiceBusReceiverClientBuilder} object.
         */
        public ServiceBusReceiverClientBuilder prefetchCount(int prefetchCount) {
            this.prefetchCount = prefetchCount;
            return this;
        }

        /**
         * Sets the name of the queue to create a receiver for.
         *
         * @param queueName Name of the queue.
         *
         * @return The modified {@link ServiceBusReceiverClientBuilder} object.
         */
        public ServiceBusReceiverClientBuilder queueName(String queueName) {
            this.queueName = queueName;
            return this;
        }

        /**
         * Sets the receive mode for the receiver.
         *
         * @param receiveMode Mode for receiving messages.
         *
         * @return The modified {@link ServiceBusReceiverClientBuilder} object.
         */
        public ServiceBusReceiverClientBuilder receiveMode(ReceiveMode receiveMode) {
            this.receiveMode = receiveMode;
            return this;
        }

        /**
         * Sets the name of the subscription in the topic to listen to.
         *
         * @param subscriptionName Name of the subscription.
         *
         * @return The modified {@link ServiceBusReceiverClientBuilder} object.
         * @see #topicName A topic name should be set as well.
         */
        public ServiceBusReceiverClientBuilder subscriptionName(String subscriptionName) {
            this.subscriptionName = subscriptionName;
            return this;
        }

        /**
         * Sets the name of the topic.
         *
         * @param topicName Name of the topic.
         *
         * @return The modified {@link ServiceBusReceiverClientBuilder} object.
         * @see #subscriptionName A subscription name should be set as well.
         */
        public ServiceBusReceiverClientBuilder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        /**
         * Creates an <b>asynchronous</b> Service Bus receiver responsible for reading {@link ServiceBusMessage
         * messages} from a specific queue or topic.
         *
         * @return An new {@link ServiceBusReceiverAsyncClient} that receives messages from a queue or topic.
         * @throws IllegalStateException if {@link #queueName(String) queueName} or {@link #topicName(String)
         *     topicName} are not set or, both of these fields are set. It is also thrown if the Service Bus {@link
         *     #connectionString(String) connectionString} contains an {@code EntityPath} that does not match one set in
         *     {@link #queueName(String) queueName} or {@link #topicName(String) topicName}. Lastly, if a {@link
         *     #topicName(String) topicName} is set, but {@link #subscriptionName(String) subscriptionName} is not.
         * @throws IllegalArgumentException Queue or topic name are not set via {@link #queueName(String)
         *     queueName()} or {@link #topicName(String) topicName()}, respectively.
         */
        public ServiceBusReceiverAsyncClient buildAsyncClient() {
            final MessagingEntityType entityType = validateEntityPaths(logger, connectionStringEntityName, topicName,
                queueName);

            final String entityPath;
            switch (entityType) {
                case QUEUE:
                    entityPath = queueName;
                    break;
                case TOPIC:
                    if (isNullOrEmpty(subscriptionName)) {
                        throw logger.logExceptionAsError(new IllegalStateException(String.format(
                            "topicName (%s) must have a subscriptionName associated with it.", topicName)));
                    }

                    entityPath = topicName;
                    break;
                default:
                    throw logger.logExceptionAsError(
                        new IllegalArgumentException("Unknown entity type: " + entityType));
            }

            if (prefetchCount < 1) {
                throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                    "prefetchCount (%s) cannot be less than 1.", prefetchCount)));
            }

            final MessageLockContainer messageLockContainer = new MessageLockContainer();
            final ServiceBusConnectionProcessor connectionProcessor = getOrCreateConnectionProcessor(messageSerializer);
            final ReceiverOptions receiverOptions = new ReceiverOptions(receiveMode, prefetchCount);

            return new ServiceBusReceiverAsyncClient(connectionProcessor.getFullyQualifiedNamespace(), entityPath,
                entityType, false, receiverOptions, connectionProcessor, tracerProvider,
                messageSerializer, messageLockContainer, ServiceBusClientBuilder.this::onClientClose);
        }

        /**
         * Creates <b>synchronous</b> Service Bus receiver responsible for reading {@link ServiceBusMessage messages}
         * from a specific queue or topic.
         *
         * @return An new {@link ServiceBusReceiverClient} that receives messages from a queue or topic.
         * @throws IllegalStateException if {@link #queueName(String) queueName} or {@link #topicName(String)
         *     topicName} are not set or, both of these fields are set. It is also thrown if the Service Bus {@link
         *     #connectionString(String) connectionString} contains an {@code EntityPath} that does not match one set in
         *     {@link #queueName(String) queueName} or {@link #topicName(String) topicName}. Lastly, if a {@link
         *     #topicName(String) topicName} is set, but {@link #subscriptionName(String) subscriptionName} is not.
         * @throws IllegalArgumentException Queue or topic name are not set via {@link #queueName(String)
         *     queueName()} or {@link #topicName(String) topicName()}, respectively.
         */
        public ServiceBusReceiverClient buildClient() {
            return new ServiceBusReceiverClient(buildAsyncClient(), retryOptions.getTryTimeout());
        }
    }
}
