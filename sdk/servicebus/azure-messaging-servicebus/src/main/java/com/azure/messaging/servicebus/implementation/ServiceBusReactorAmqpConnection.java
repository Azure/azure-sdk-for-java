// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.AzureTokenManagerProvider;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.amqp.models.CbsAuthorizationType;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A proton-j AMQP connection to an Azure Service Bus instance.
 */
public class ServiceBusReactorAmqpConnection extends ReactorConnection implements ServiceBusAmqpConnection {

    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";
    private static final String MANAGEMENT_ADDRESS = "$management";
    private static final String CROSS_ENTITY_TRANSACTIONS_LINK_NAME = "crossentity-coordinator";

    private final ClientLogger logger = new ClientLogger(ServiceBusReactorAmqpConnection.class);
    private final ConcurrentHashMap<String, ServiceBusManagementNode> managementNodes = new ConcurrentHashMap<>();
    private final String connectionId;
    private final ReactorProvider reactorProvider;
    private final ReactorHandlerProvider handlerProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final AmqpRetryOptions retryOptions;
    private final MessageSerializer messageSerializer;
    private final Scheduler scheduler;
    private final String fullyQualifiedNamespace;
    private final CbsAuthorizationType authorizationType;
    private final boolean distributedTransactionsSupport;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     * @param tokenManagerProvider Provides a token manager for authorizing with CBS node.
     * @param messageSerializer Serializes and deserializes proton-j messages.
     * @param distributedTransactionsSupport indicate if distributed transaction across different entities is required
     *        for this connection.
     */
    public ServiceBusReactorAmqpConnection(String connectionId, ConnectionOptions connectionOptions,
        ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer,
        boolean distributedTransactionsSupport) {
        super(connectionId, connectionOptions, reactorProvider, handlerProvider, tokenManagerProvider,
            messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.FIRST);

        this.connectionId = connectionId;
        this.reactorProvider = reactorProvider;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = tokenManagerProvider;
        this.authorizationType = connectionOptions.getAuthorizationType();
        this.retryOptions = connectionOptions.getRetry();
        this.messageSerializer = messageSerializer;
        this.scheduler = connectionOptions.getScheduler();
        this.fullyQualifiedNamespace = connectionOptions.getFullyQualifiedNamespace();
        this.distributedTransactionsSupport = distributedTransactionsSupport;
    }

    @Override
    public Mono<ServiceBusManagementNode> getManagementNode(String entityPath, MessagingEntityType entityType) {
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "connectionId[%s]: Connection is disposed. Cannot get management instance for '%s'",
                connectionId, entityPath))));
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
                        logger.info("A management node exists already, returning it.");

                        // Close the token manager we had created during this because it is unneeded now.
                        tokenManager.close();
                        return current;
                    }

                    final String sessionName = entityPath + "-" + MANAGEMENT_SESSION_NAME;
                    final String linkName = entityPath + "-" + MANAGEMENT_LINK_NAME;
                    final String address = entityPath + "/" + MANAGEMENT_ADDRESS;

                    logger.info("Creating management node. entityPath: [{}]. address: [{}]. linkName: [{}]",
                        entityPath, address, linkName);

                    return new ManagementChannel(createRequestResponseChannel(sessionName, linkName, address),
                        fullyQualifiedNamespace, entityPath, tokenManager, messageSerializer,
                        retryOptions.getTryTimeout());
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
     *
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions,
         String transferEntityPath) {

        return createSession(linkName).cast(ServiceBusSession.class).flatMap(session -> {
            logger.verbose("Get or create sender link : '{}'", linkName);
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

            return session.createProducer(linkName + entityPath, entityPath, retryOptions.getTryTimeout(),
                retryPolicy, transferEntityPath).cast(AmqpSendLink.class);
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
     *
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<ServiceBusReceiveLink> createReceiveLink(String linkName, String entityPath,
        ServiceBusReceiveMode receiveMode, String transferEntityPath, MessagingEntityType entityType) {
        return createSession(entityPath).cast(ServiceBusSession.class)
            .flatMap(session -> {
                logger.verbose("Get or create consumer for path: '{}'", entityPath);
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createConsumer(linkName, entityPath, entityType, retryOptions.getTryTimeout(),
                    retryPolicy, receiveMode);
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
     * @param sessionId to use when creating the link.
     *
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<ServiceBusReceiveLink> createReceiveLink(String linkName, String entityPath,
        ServiceBusReceiveMode receiveMode, String transferEntityPath, MessagingEntityType entityType,
        String sessionId) {
        return createSession(entityPath).cast(ServiceBusSession.class)
            .flatMap(session -> {
                logger.verbose("Get or create consumer for path: '{}'", entityPath);
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createConsumer(linkName, entityPath, entityType, retryOptions.getTryTimeout(),
                    retryPolicy, receiveMode, sessionId);
            });
    }

    @Override
    protected AmqpSession createSession(String sessionName, Session session, SessionHandler handler) {
        return new ServiceBusReactorSession(this, session, handler, sessionName, reactorProvider,
            handlerProvider, getClaimsBasedSecurityNode(), tokenManagerProvider, messageSerializer, retryOptions,
            new ServiceBusCreateSessionOptions(distributedTransactionsSupport));
    }
}
