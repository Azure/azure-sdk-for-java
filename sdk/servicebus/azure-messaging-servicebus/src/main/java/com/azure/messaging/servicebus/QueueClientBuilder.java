// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpSharedAccessKeyCredential;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyAuthenticationType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.SendOptions;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.messaging.servicebus.implementation.EventHubReactorAmqpConnection;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.amqp.models.ReceiveOptions;
import com.azure.core.annotation.ServiceClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.ClientConstants;
import com.azure.messaging.servicebus.implementation.SBConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusSharedKeyCredential;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;

@ServiceClientBuilder(serviceClients = {})
public class QueueClientBuilder {

    private final ClientLogger logger = new ClientLogger(QueueClientBuilder.class);
    private static final String AZURE_SERVICE_BUS_CONNECTION_STRING = "AZURE_SERVICE_BUS_CONNECTION_STRING";
    private static final AmqpRetryOptions DEFAULT_RETRY = new AmqpRetryOptions().setTryTimeout(ClientConstants.OPERATION_TIMEOUT);

    private static final String EVENTHUBS_PROPERTIES_FILE = "azure-messaging-eventhubs.properties";
    private static final String NAME_KEY = "name";
    private static final String VERSION_KEY = "version";
    private static final String UNKNOWN = "UNKNOWN";

    private ProxyOptions proxyOptions;
    private TokenCredential credentials;
    private Configuration configuration;
    private ProxyOptions proxyConfiguration;
    private AmqpRetryOptions retryOptions;
    private Scheduler scheduler;
    private AmqpTransportType transport;
    private String fullyQualifiedNamespace;
    private String entityPath;
    private String host;
    private String queuePath;


    private final String connectionId;
    //private  Mono<EventHubConnection> connectionMono;
    private SBConnectionProcessor servicerBusConnectionProcessor;
    private int prefetchCount;
    private boolean isSharedConnection;


    private SendOptions defaultSenderOptions;
    private ReceiveOptions defaultReceiverOptions;


    /**
     * Creates a new instance with the default transport {@link AmqpTransportType#AMQP}.
     */
    public QueueClientBuilder(){
        transport = AmqpTransportType.AMQP;
        this.connectionId = StringUtil.getRandomString("MF");
    }

    public QueueClientBuilder connectionString(String connectionString) {
        final ConnectionStringProperties properties = new ConnectionStringProperties(connectionString);
        final TokenCredential tokenCredential;
        try {
            tokenCredential = new AmqpSharedAccessKeyCredential(properties.getSharedAccessKeyName(),
                properties.getSharedAccessKey(), ClientConstants.TOKEN_VALIDITY);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new AzureException("Could not create the EventHubSharedAccessKeyCredential.", e));
        }
        return credential(properties.getEndpoint().getHost(), properties.getEntityPath(), tokenCredential);
    }


    public QueueClientBuilder credential(String host, String entityPath, TokenCredential credential) {
        this.host = Objects.requireNonNull(host, "'host' cannot be null.");
        this.credentials = Objects.requireNonNull(credential, "'credential' cannot be null.");
        this.queuePath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");

        if (CoreUtils.isNullOrEmpty(host)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'host' cannot be an empty string."));
        } else if (CoreUtils.isNullOrEmpty(entityPath)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'entityPath' cannot be an empty string."));
        }
        return this;
    }

    /*private static class ResponseMapper implements AmqpResponseMapper {
        @Override
        public AMQPResponseProperties toAMQPResponseProperties(Map<?, ?> amqpBody) {
            return new AMQPResponseProperties(amqpBody);
        }
        @Override
        public EventHubProperties toEventHubProperties(Map<?, ?> amqpBody) {
            return new EventHubProperties(
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
                ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_CREATED_AT)).toInstant(),
                (String[]) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IDS));
        }

        @Override
        public PartitionProperties toPartitionProperties(Map<?, ?> amqpBody) {
            return new PartitionProperties(
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_ENTITY_NAME_KEY),
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_PARTITION_NAME_KEY),
                (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_BEGIN_SEQUENCE_NUMBER),
                (Long) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_SEQUENCE_NUMBER),
                (String) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_OFFSET),
                ((Date) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_LAST_ENQUEUED_TIME_UTC)).toInstant(),
                (Boolean) amqpBody.get(ManagementChannel.MANAGEMENT_RESULT_PARTITION_IS_EMPTY));
        }
    }
    */

    /**
     * Creates an {@link SenderAsyncClient} for transmitting {@link EventData} to the Event Hub, grouped together
     * in batches.
     *
     * @return A new {@link SenderAsyncClient}.
     */
    SenderAsyncClient buildAsyncClient() {
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }
        this.defaultSenderOptions = new SendOptions();

        this.defaultReceiverOptions = new ReceiveOptions();

        final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();

        if (isSharedConnection && servicerBusConnectionProcessor == null) {
            servicerBusConnectionProcessor = buildConnectionProcessor(messageSerializer);
        }

        final SBConnectionProcessor processor = isSharedConnection
            ? servicerBusConnectionProcessor
            : buildConnectionProcessor(messageSerializer);

        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));

        return new SenderAsyncClient(queuePath, processor, defaultSenderOptions,  retryOptions, tracerProvider, messageSerializer, isSharedConnection);
    }

    /*SenderAsyncClient buildAsyncClient() {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));
        if (retryOptions == null) {
            retryOptions = DEFAULT_RETRY;
        }

        if (scheduler == null) {
            scheduler = Schedulers.elastic();
        }
        final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();

        //this.entityPath = connectionOptions.getEntityPath();

        this.connectionMono = Mono.fromCallable(() -> {
            return (EventHubAmqpConnection) new ReactorConnection(connectionId, connectionOptions, provider,
                handlerProvider, new ResponseMapper());
        }).doOnSubscribe(c -> hasConnection.set(true))
            .cache();

        this.defaultSenderOptions = new SendOptions()
            .retry(connectionOptions.getRetry());

        this.defaultReceiverOptions = new ReceiveOptions()
            //.retry(connectionOptions.getRetry())
            .scheduler(connectionOptions.getScheduler());

        final Mono<AmqpSendLink> amqpLinkMono = connectionMono
            .flatMap(connection -> connection.createSession(connectionOptions.getEntityPath()))
            .flatMap(session -> {
                logger.verbose("Creating producer for {}", connectionOptions.getEntityPath());
                final RetryPolicy retryPolicy = RetryUtil.getRetryPolicy(connectionOptions.getRetry());

                return session.createProducer(linkName, connectionOptions.getEntityPath(), connectionOptions.getRetry().tryTimeout(), retryPolicy)
                    .cast(AmqpSendLink.class);
            });

        return new SenderAsyncClient(amqpLinkMono, this.defaultSenderOptions,
             tracerProvider);
    }


    public SenderClient buildClient() {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final TracerProvider tracerProvider = new TracerProvider(ServiceLoader.load(Tracer.class));
        final SenderAsyncClient client = new SenderAsyncClient(connectionOptions, provider, handlerProvider, tracerProvider, );

        return new SenderClient(client, connectionOptions);
    }
 */

    /**
     * Sets the proxy configuration to use for {@link SenderAsyncClient}. When a proxy is configured, {@link
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

    public QueueClientBuilder fullyQualifiedNamespace(String fullyQualifiedNamespace) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        return this;
    }

    public QueueClientBuilder entityPath(String entityPath) {
        this.entityPath = entityPath;
        return this;
    }

    private SBConnectionProcessor buildConnectionProcessor(MessageSerializer messageSerializer) {
        final ConnectionOptions connectionOptions = getConnectionOptions();
        final TokenManagerProvider tokenManagerProvider = new AzureTokenManagerProvider(
            connectionOptions.getAuthorizationType(), connectionOptions.getFullyQualifiedNamespace(),
            ClientConstants.AZURE_ACTIVE_DIRECTORY_SCOPE);
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);

        final Map<String, String> properties = CoreUtils.getProperties(EVENTHUBS_PROPERTIES_FILE);
        final String product = properties.getOrDefault(NAME_KEY, UNKNOWN);
        final String clientVersion = properties.getOrDefault(VERSION_KEY, UNKNOWN);

        final Flux<ServiceBusAmqpConnection> connectionFlux = Mono.fromCallable(() -> {
            final String connectionId = StringUtil.getRandomString("MF");

            return (ServiceBusAmqpConnection) new EventHubReactorAmqpConnection(connectionId, connectionOptions, provider,
                handlerProvider, tokenManagerProvider, messageSerializer, product, clientVersion);
        }).repeat();

        return connectionFlux.subscribeWith(new SBConnectionProcessor(
            connectionOptions.getFullyQualifiedNamespace(), connectionOptions.getEntityPath(),
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

        return new ConnectionOptions(fullyQualifiedNamespace, entityPath, credentials, authorizationType,
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
