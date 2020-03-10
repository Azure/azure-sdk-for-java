// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
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
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import com.azure.messaging.servicebus.models.ReceiveMessageOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * The builder to create {@link ServiceBusReceiverAsyncClient} and {@link ServiceBusSenderAsyncClient}.
 */
@ServiceClientBuilder(serviceClients = {ServiceBusReceiverAsyncClient.class, ServiceBusSenderAsyncClient.class})
public final class ServiceBusClientBuilder {

    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final AmqpRetryOptions DEFAULT_RETRY =
        new AmqpRetryOptions().setTryTimeout(ServiceBusConstants.OPERATION_TIMEOUT);

    private static final String SERVICEBUS_PROPERTIES_FILE = "azure-messaging-servicebus.properties";
    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";
    private static final String UNKNOWN = "UNKNOWN";

    private final ClientLogger logger = new ClientLogger(ServiceBusClientBuilder.class);

    private ReceiveMessageOptions receiveMessageOptions = new ReceiveMessageOptions();
    private ProxyOptions proxyOptions;
    private TokenCredential credentials;
    private Configuration configuration;
    private AmqpRetryOptions retryOptions;
    private Scheduler scheduler;
    private AmqpTransportType transport = AmqpTransportType.AMQP;
    private String fullyQualifiedNamespace;
    private String serviceBusResourceName;

    /**
     * Creates a new instance with the default transport {@link AmqpTransportType#AMQP}.
     */
    public ServiceBusClientBuilder() {
    }

    /**
     * Sets the connection string for a Service Bus resource.
     *
     * @param connectionStringWithResourceName Connection string with name of Service Bus resource in it.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder connectionString(String connectionStringWithResourceName) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionStringWithResourceName);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new ServiceBusSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ServiceBusConstants.TOKEN_VALIDITY);
        } catch (Exception e) {
            throw logger.logExceptionAsError(
                new AzureException("Could not create the ServiceBusSharedKeyCredential.", e));
        }
        this.fullyQualifiedNamespace = properties.getEndpoint().getHost();
        this.serviceBusResourceName = properties.getEntityPath();
        return credential(properties.getEndpoint().getHost(), properties.getEntityPath(), tokenCredential);
    }

    /**
     * Sets the credential for the Service Bus resource.
     *
     * @param fullyQualifiedNamespace for the Service Bus.
     * @param topicOrQueueName The name of the queue or topic.
     * @param credential {@link TokenCredential} to be used for authentication.
     *
     * @return The updated {@link ServiceBusClientBuilder} object.
     */
    public ServiceBusClientBuilder credential(String fullyQualifiedNamespace, String topicOrQueueName,
        TokenCredential credential) {

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.serviceBusResourceName = Objects.requireNonNull(
            topicOrQueueName, "'topicOrQueueName' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        } else if (CoreUtils.isNullOrEmpty(topicOrQueueName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'topicOrQueueName' cannot be an empty string."));
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
     * Specify connection string and  queue name for connection to Queue.
     *
     * @param connectionString to connect to service bus resource.
     * @param topicOrQueueName The name of the queue.
     *
     * @return The {@link ServiceBusClientBuilder}.
     */
    public ServiceBusClientBuilder connectionString(String connectionString, String topicOrQueueName) {
        this.serviceBusResourceName = topicOrQueueName;
        return connectionString(connectionString);
    }

    /**
     * @param retryPolicy to recover from Connection.
     *
     * @return The {@link ServiceBusClientBuilder}.
     */
    public ServiceBusClientBuilder retryPolicy(AmqpRetryPolicy retryPolicy) {
        return this;
    }

    /**
     * This is valid for receiving messages only.
     *
     * @param receiveMessageOptions for receiving.
     *
     * @return The {@link ServiceBusClientBuilder}.
     */
    public ServiceBusClientBuilder receiveMessageOptions(ReceiveMessageOptions receiveMessageOptions) {
        this.receiveMessageOptions = receiveMessageOptions;
        return this;
    }

    /**
     * @param transportType to use.
     *
     * @return The {@link ServiceBusClientBuilder}.
     */
    public ServiceBusClientBuilder transportType(AmqpTransportType transportType) {
        this.transport = transportType;
        return this;
    }

    /**
     * @param retryOptions to manage AMQP connection.
     *
     * @return The {@link ServiceBusClientBuilder}.
     */
    public ServiceBusClientBuilder retry(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
    }

    /**
     * Creates an {@link ServiceBusSenderAsyncClient} for transmitting {@link ServiceBusMessage} to a Service Bus queue
     * or topic.
     *
     * @return A new {@link ServiceBusSenderAsyncClient} for transmitting to a Service queue or topic.
     */
    public ServiceBusSenderAsyncClient buildAsyncSenderClient() {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();
        final ServiceBusConnectionProcessor connectionProcessor = createConnectionProcessor(messageSerializer);

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        return new ServiceBusSenderAsyncClient(serviceBusResourceName, connectionProcessor,
            retryOptions, tracerProvider, messageSerializer);
    }

    /**
     * Creates an Service Bus receiver responsible for reading {@link ServiceBusMessage messages} from a specific queue
     * or topic.
     *
     * @return An new {@link ServiceBusReceiverAsyncClient} that receives messages from a queue or topic.
     */
    public ServiceBusReceiverAsyncClient buildAsyncReceiverClient() {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }

        final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();
        final ServiceBusConnectionProcessor connectionProcessor = createConnectionProcessor(messageSerializer);
        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        return new ServiceBusReceiverAsyncClient(connectionProcessor.getFullyQualifiedNamespace(),
            serviceBusResourceName, connectionProcessor, tracerProvider, messageSerializer, receiveMessageOptions);
    }

    /**
     * Sets the scheduler to use.
     *
     * @param scheduler Scheduler to be used.
     *
     * @return The {@link ServiceBusClientBuilder}.
     */
    ServiceBusClientBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;

    }

    private ServiceBusConnectionProcessor createConnectionProcessor(MessageSerializer messageSerializer) {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
            connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
            ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);

        final Map<String, String> properties = CoreUtils.getProperties(SERVICEBUS_PROPERTIES_FILE);
        final String product = properties.getOrDefault(NAME_KEY, UNKNOWN);
        final String clientVersion = properties.getOrDefault(VERSION_KEY, UNKNOWN);

        final Flux<ServiceBusAmqpConnection> connectionFlux = Mono.fromCallable(() -> {
            final String connectionId = StringUtil.getRandomString("MF");

            return (ServiceBusAmqpConnection) new ServiceBusReactorAmqpConnection(connectionId, connectionOptions,
                provider, handlerProvider, tokenManagerProvider, messageSerializer, product, clientVersion);
        }).repeat();

        return connectionFlux.subscribeWith(new ServiceBusConnectionProcessor(
            connectionOptions.getFullyQualifiedNamespace(), serviceBusResourceName,
            connectionOptions.getRetry()));
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

        return new ConnectionOptions(fullyQualifiedNamespace, credentials, authorizationType,
            transport, retryOptions, proxyOptions, scheduler);
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
}
