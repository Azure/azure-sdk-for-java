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
import com.azure.messaging.servicebus.implementation.ClientConstants;
import com.azure.messaging.servicebus.implementation.SendOptions;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

@ServiceClientBuilder(serviceClients = {})
public final class QueueClientBuilder {

    private final ClientLogger logger = new ClientLogger(QueueClientBuilder.class);
    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final AmqpRetryOptions DEFAULT_RETRY = new AmqpRetryOptions().setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    private static final String SERVICEBUS_PROPERTIES_FILE = "azure-messaging-servicebus.properties";
    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";
    private static final String UNKNOWN = "UNKNOWN";

    private ProxyOptions proxyOptions;
    private TokenCredential credentials;
    private Configuration configuration;
    private AmqpRetryOptions retryOptions;
    private Scheduler scheduler;
    private AmqpTransportType transport = AmqpTransportType.AMQP;
    private String fullyQualifiedNamespace;
    private String queueName;


    private final String connectionId;
    private ServiceBusConnectionProcessor servicerBusConnectionProcessor;
    private boolean isSharedConnection;


    private SendOptions defaultSenderOptions;


    /**
     * Creates a new instance with the default transport {@link AmqpTransportType#AMQP}.
     */
    public QueueClientBuilder(){
        this.connectionId = StringUtil.getRandomString("MF");
    }

    public QueueClientBuilder connectionString(String connectionString) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new ServiceBusSharedKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch ( Exception e) {
            throw logger.logExceptionAsError(new AzureException("Could not create the ServiceBusSharedKeyCredential.", e));
        }
        this.fullyQualifiedNamespace = properties.getEndpoint().getHost();
        this.queueName = properties.getEntityPath();
        return credential(properties.getEndpoint().getHost(), properties.getEntityPath(), tokenCredential);
    }


    public QueueClientBuilder credential(String fullyQualifiedNamespace, String queueName, TokenCredential credential) {
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.queueName = Objects.requireNonNull(queueName, "'entityPath' cannot be null.");

        if (CoreUtils.isNullOrEmpty(fullyQualifiedNamespace)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'fullyQualifiedNamespace' cannot be an empty string."));
        } else if (CoreUtils.isNullOrEmpty(queueName)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'entityPath' cannot be an empty string."));
        }
        return this;
    }


    /**
     * Creates an {@link QueueSenderAsyncClient} for transmitting {@link Message} to the Service Bus Queue.
     *
     * @return A new {@link QueueSenderAsyncClient}.
     */
    QueueSenderAsyncClient buildAsyncSenderClient() {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }
        this.defaultSenderOptions = new SendOptions();

        final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();

        if (isSharedConnection && servicerBusConnectionProcessor == null) {
            servicerBusConnectionProcessor = buildConnectionProcessor(messageSerializer);
        }

        final ServiceBusConnectionProcessor connectionProcessor = isSharedConnection
            ? servicerBusConnectionProcessor
            : buildConnectionProcessor(messageSerializer);

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        return new QueueSenderAsyncClient(queueName, connectionProcessor, defaultSenderOptions,  retryOptions, tracerProvider, messageSerializer, isSharedConnection);
    }
    /**
     * Creates an Service Bus Queue receiver responsible for reading {@link Message} from a specific Queue.
     *
     * @param prefetchCount The set of options to apply when creating the consumer.
     * @return An new {@link QueueReceiverAsyncClient} that receives events from the Queue.
     */
    QueueReceiverAsyncClient createAsyncReceiverClient(int prefetchCount) {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }
        this.defaultSenderOptions = new SendOptions();

        final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();

        if (isSharedConnection && servicerBusConnectionProcessor == null) {
            servicerBusConnectionProcessor = buildConnectionProcessor(messageSerializer);
        }

        final ServiceBusConnectionProcessor connectionProcessor = isSharedConnection
            ? servicerBusConnectionProcessor
            : buildConnectionProcessor(messageSerializer);

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        return new QueueReceiverAsyncClient(connectionProcessor.getFullyQualifiedNamespace(), queueName,
            connectionProcessor, tracerProvider, messageSerializer, prefetchCount, isSharedConnection);
    }

    QueueReceiverAsyncClient createAsyncReceiverClient(ReceiveMode receiveMode, int prefetchCount) {
        return createAsyncReceiverClient(prefetchCount);
    }

    /**
     * Sets the proxy configuration to use for {@link QueueSenderAsyncClient}. When a proxy is configured, {@link
     * AmqpTransportType#AMQP_WEB_SOCKETS} must be used for the transport type.
     *
     * @param proxyOptions The proxy configuration to use.
     *
     * @return The updated {@link QueueClientBuilder} object.
     */
    public QueueClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
        return this;
    }

    /**
     * Specify connection string and  queue name for connection to Queue.
     * @param connectionString
     * @param queueName
     * @return
     */
    public QueueClientBuilder connectionString(String connectionString, String queueName) {
        this.queueName = queueName;
        return connectionString(connectionString);
    }

    /**
     *
     * @param queueName  to connect to .
     * @return The {@link QueueClientBuilder}.
     */
    public QueueClientBuilder queueName(String queueName) {
        this.queueName = queueName;
        return this;
    }

    /**
     *
     * @param retryPolicy to recover from Connection.
     * @return The {@link QueueClientBuilder}.
     */
    public QueueClientBuilder retryPolicy(AmqpRetryPolicy retryPolicy) {
        return this;
    }

    /**
     *
     * @param transportType to use.
     * @return The {@link QueueClientBuilder}.
     */
    public QueueClientBuilder transportType(AmqpTransportType transportType) {
        this.transport = transportType;
        return this;
    }

    /** package- private method
     *
     * @param scheduler
     * @return The {@link QueueClientBuilder}.
     */
    QueueClientBuilder scheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
        return this;

    }

    private ServiceBusConnectionProcessor buildConnectionProcessor(MessageSerializer messageSerializer) {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
            connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
            ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);

        final Map<String, String> properties = CoreUtils.getProperties(SERVICEBUS_PROPERTIES_FILE);
        final String product = properties.getOrDefault(NAME_KEY, UNKNOWN);
        final String clientVersion = properties.getOrDefault(VERSION_KEY, UNKNOWN);

        final Flux<ServiceBusAmqpConnection> connectionFlux = Mono.fromCallable(() -> {
            final String connectionId = StringUtil.getRandomString("MF");

            return (ServiceBusAmqpConnection) new ServiceBusReactorAmqpConnection(connectionId, connectionOptions, provider,
                handlerProvider, tokenManagerProvider, messageSerializer, product, clientVersion);
        }).repeat();

        return connectionFlux.subscribeWith(new ServiceBusConnectionProcessor(
            connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getEntityPath(),
            connectionOptions.getRetry()));
    }

    public QueueClientBuilder retry(AmqpRetryOptions retryOptions) {
        this.retryOptions = retryOptions;
        return this;
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

        return new ConnectionOptions(fullyQualifiedNamespace, queueName, credentials, authorizationType,
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
