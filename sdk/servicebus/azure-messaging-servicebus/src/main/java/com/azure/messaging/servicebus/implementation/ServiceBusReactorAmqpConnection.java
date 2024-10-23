// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.implementation.AmqpChannelProcessor;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ChannelCacheWrapper;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ProtonSessionWrapper;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.ReactorSession;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.RequestResponseChannelCache;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * A proton-j AMQP connection to an Azure Service Bus instance.
 */
public class ServiceBusReactorAmqpConnection extends ReactorConnection implements ServiceBusAmqpConnection {

    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";
    private static final String MANAGEMENT_ADDRESS = "$management";
    private static final String CROSS_ENTITY_TRANSACTIONS_LINK_NAME = "crossentity-coordinator";

    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusReactorAmqpConnection.class);
    private final ConcurrentHashMap<String, ServiceBusManagementNode> managementNodes = new ConcurrentHashMap<>();
    private final String connectionId;
    private final ReactorHandlerProvider handlerProvider;
    private final ServiceBusAmqpLinkProvider linkProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final AmqpRetryOptions retryOptions;
    private final MessageSerializer messageSerializer;
    private final String fullyQualifiedNamespace;
    private final CbsAuthorizationType authorizationType;
    private final boolean distributedTransactionsSupport;
    private final boolean isV2;
    private final boolean useSessionChannelCache;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     * @param linkProvider Provides amqp links for send and receive.
     * @param tokenManagerProvider Provides a token manager for authorizing with CBS node.
     * @param messageSerializer Serializes and deserializes proton-j messages.
     * @param distributedTransactionsSupport indicate if distributed transaction across different entities is required
     *        for this connection.
     * @param isV2 (temporary) flag to use either v1 or v2 receiver.
     * @param useSessionChannelCache indicates if ReactorSessionCache and RequestResponseChannelCache should be used
     *  when in v2 mode.
     */
    public ServiceBusReactorAmqpConnection(String connectionId, ConnectionOptions connectionOptions,
        ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider, ServiceBusAmqpLinkProvider linkProvider,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer,
        boolean distributedTransactionsSupport, boolean isV2, boolean useSessionChannelCache) {
        super(connectionId, connectionOptions, reactorProvider, handlerProvider, linkProvider, tokenManagerProvider,
            messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST, isV2, useSessionChannelCache);

        this.connectionId = connectionId;
        this.handlerProvider = handlerProvider;
        this.linkProvider = linkProvider;
        this.tokenManagerProvider = tokenManagerProvider;
        this.authorizationType = connectionOptions.getAuthorizationType();
        this.retryOptions = connectionOptions.getRetry();
        this.messageSerializer = messageSerializer;
        this.fullyQualifiedNamespace = connectionOptions.getFullyQualifiedNamespace();
        this.distributedTransactionsSupport = distributedTransactionsSupport;
        this.isV2 = isV2;
        this.useSessionChannelCache = useSessionChannelCache;
    }

    @Override
    public Mono<ServiceBusManagementNode> getManagementNode(String entityPath, MessagingEntityType entityType) {
        if (isDisposed()) {
            return monoError(LOGGER.atWarning(), new IllegalStateException(String.format(
                "connectionId[%s]: Connection is disposed. Cannot get management instance for '%s'",
                connectionId, entityPath)));
        }

        final String entityTypePath = String.join("-", entityType.toString(), entityPath);

        final ServiceBusManagementNode existing = managementNodes.get(entityTypePath);
        if (existing != null) {
            return Mono.just(existing);
        }

        return getReactorConnection().then(
            Mono.defer(() -> {
                final TokenManager tokenManager = new AzureTokenManagerProvider(authorizationType,
                    fullyQualifiedNamespace, ServiceBusConstants.AZURE_ACTIVE_DIRECTORY_SCOPE)
                    .getTokenManager(getClaimsBasedSecurityNode(), entityPath);

                return tokenManager.authorize().thenReturn(managementNodes.compute(entityTypePath, (key, current) -> {
                    if (current != null) {
                        LOGGER.info("A management node exists already, returning it.");

                        // Close the token manager we had created during this because it is unneeded now.
                        tokenManager.close();
                        return current;
                    }

                    final String sessionName = entityPath + "-" + MANAGEMENT_SESSION_NAME;
                    final String linkName = entityPath + "-" + MANAGEMENT_LINK_NAME;
                    final String address = entityPath + "/" + MANAGEMENT_ADDRESS;

                    LOGGER.atInfo()
                        .addKeyValue(LINK_NAME_KEY, linkName)
                        .addKeyValue(ENTITY_PATH_KEY, entityPath)
                        .addKeyValue("address", address)
                        .log("Creating management node.");

                    final ChannelCacheWrapper channelCache;
                    if (useSessionChannelCache) {
                        // V2 with 'SessionCache,RequestResponseChannelCache' opted-in.
                        final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
                        final RequestResponseChannelCache cache
                            = new RequestResponseChannelCache(this, address, sessionName, linkName, retryPolicy);
                        channelCache = new ChannelCacheWrapper(cache);
                    } else {
                        // V2 without 'SessionCache,RequestResponseChannelCache' opt-in or V1.
                        final AmqpChannelProcessor<RequestResponseChannel> cache
                            = createRequestResponseChannel(sessionName, linkName, address);
                        channelCache = new ChannelCacheWrapper(cache);
                    }
                    return new ManagementChannel(channelCache, fullyQualifiedNamespace, entityPath, tokenManager,
                        messageSerializer, retryOptions.getTryTimeout());
                }));
            }));
    }

    /**
     * Creates or gets a send link. The same link is returned if there is an existing send link with the same {@code
     * linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param retryOptions Options to use when creating the link.
     * @param transferEntityPath Path if the message should be transferred this destination by message broker.
     * @param clientIdentifier The identifier of the client.
     *
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions,
        String transferEntityPath, String clientIdentifier) {

        return createSession(linkName).cast(ServiceBusSession.class).flatMap(session -> {
            LOGGER.atVerbose().addKeyValue(LINK_NAME_KEY, linkName).log("Get or create sender link.");
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

            return session.createProducer(linkName + entityPath, entityPath, retryOptions.getTryTimeout(),
                retryPolicy, transferEntityPath, clientIdentifier).cast(AmqpSendLink.class);
        });
    }

    /**
     * Creates or gets an existing receive link. The same link is returned if there is an existing receive link with the
     * same {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param receiveMode Consumer options to use when creating the link.
     * @param transferEntityPath Path if the events should be transferred to another link after being received
     *     from this link.
     * @param entityType {@link MessagingEntityType} to use when creating the link.
     * @param clientIdentifier The identifier of the client.
     *
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<ServiceBusReceiveLink> createReceiveLink(String linkName, String entityPath,
        ServiceBusReceiveMode receiveMode, String transferEntityPath, MessagingEntityType entityType, String clientIdentifier) {
        return createSession(entityPath).cast(ServiceBusSession.class)
            .flatMap(session -> {
                LOGGER.atVerbose().addKeyValue(ENTITY_PATH_KEY, entityPath).log("Get or create consumer.");
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createConsumer(linkName, entityPath, entityType, retryOptions.getTryTimeout(),
                    retryPolicy, receiveMode, clientIdentifier);
            });
    }

    @Override
    public Mono<AmqpSession> createSession(String sessionName) {
        return super.createSession(distributedTransactionsSupport ? CROSS_ENTITY_TRANSACTIONS_LINK_NAME : sessionName);
    }

    /**
     * Creates or gets an existing receive link. The same link is returned if there is an existing receive link with the
     * same {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param receiveMode Consumer options to use when creating the link.
     * @param transferEntityPath to use when creating the link.
     * @param entityType {@link MessagingEntityType} to use when creating the link.
     * @param clientIdentifier The identifier of the client.
     * @param sessionId to use when creating the link.
     *
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<ServiceBusReceiveLink> createReceiveLink(String linkName, String entityPath, ServiceBusReceiveMode receiveMode,
        String transferEntityPath, MessagingEntityType entityType, String clientIdentifier, String sessionId) {
        return createSession(entityPath).cast(ServiceBusSession.class)
            .flatMap(session -> {
                LOGGER.atVerbose().addKeyValue(ENTITY_PATH_KEY, entityPath).log("Get or create consumer.");
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createConsumer(linkName, entityPath, entityType, retryOptions.getTryTimeout(),
                    retryPolicy, receiveMode, clientIdentifier, sessionId);
            });
    }

    @Override
    protected ReactorSession createSession(ProtonSessionWrapper session) {
        return new ServiceBusReactorSession(this, session, handlerProvider, linkProvider, getClaimsBasedSecurityNode(),
            tokenManagerProvider, messageSerializer, retryOptions,
            new ServiceBusCreateSessionOptions(distributedTransactionsSupport), isV2);
    }
}
