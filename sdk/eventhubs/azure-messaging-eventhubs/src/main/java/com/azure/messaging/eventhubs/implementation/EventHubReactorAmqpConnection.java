// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

/**
 * A proton-j AMQP connection to an Azure Event Hub instance. Adds additional support for management operations.
 */
public class EventHubReactorAmqpConnection extends ReactorConnection implements EventHubAmqpConnection {
    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";
    private static final String MANAGEMENT_ADDRESS = "$management";

    private final ClientLogger logger = new ClientLogger(EventHubReactorAmqpConnection.class);
    private final TokenCredential tokenCredential;
    private final String connectionId;
    private final ReactorProvider reactorProvider;
    private final ReactorHandlerProvider handlerProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final AmqpRetryOptions retryOptions;
    private final MessageSerializer messageSerializer;
    private final Scheduler scheduler;
    private final String eventHubName;

    private volatile ManagementChannel managementChannel;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     * @param tokenManagerProvider Provides a token manager for authorizing with CBS node.
     * @param messageSerializer Serializes and deserializes proton-j messages.
     */
    public EventHubReactorAmqpConnection(String connectionId, ConnectionOptions connectionOptions, String eventHubName,
        ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer) {
        super(connectionId, connectionOptions, reactorProvider, handlerProvider, tokenManagerProvider,
            messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND);
        this.connectionId = connectionId;
        this.reactorProvider = reactorProvider;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = tokenManagerProvider;
        this.messageSerializer = messageSerializer;
        this.eventHubName = eventHubName;
        this.retryOptions = connectionOptions.getRetry();
        this.tokenCredential = connectionOptions.getTokenCredential();
        this.scheduler = connectionOptions.getScheduler();
    }

    @Override
    public Mono<EventHubManagementNode> getManagementNode() {
        if (isDisposed()) {
            return Mono.error(logger.logExceptionAsError(new IllegalStateException(String.format(
                "connectionId[%s]: Connection is disposed. Cannot get management instance", connectionId))));
        }

        return getReactorConnection().then(Mono.fromCallable(this::getOrCreateManagementChannel));
    }

    /**
     * Creates or gets a send link. The same link is returned if there is an existing send link with the same {@code
     * linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param retryOptions Options to use when creating the link.
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions) {
        return createSession(entityPath).flatMap(session -> {
            logger.verbose("Get or create producer for path: '{}'", entityPath);
            final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

            return session.createProducer(linkName, entityPath, retryOptions.getTryTimeout(), retryPolicy)
                .cast(AmqpSendLink.class);
        });
    }

    /**
     * Creates or gets an existing receive link. The same link is returned if there is an existing receive link with the
     * same {@code linkName}. Otherwise, a new link is created and returned.
     *
     * @param linkName The name of the link.
     * @param entityPath The remote address to connect to for the message broker.
     * @param eventPosition Position to set the receive link to.
     * @param options Consumer options to use when creating the link.
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<AmqpReceiveLink> createReceiveLink(String linkName, String entityPath, EventPosition eventPosition,
        ReceiveOptions options) {
        return createSession(entityPath).cast(EventHubSession.class)
            .flatMap(session -> {
                logger.verbose("Get or create consumer for path: '{}'", entityPath);
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createConsumer(linkName, entityPath, retryOptions.getTryTimeout(), retryPolicy,
                    eventPosition, options);
            });
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }

        logger.info("connectionId[{}]: Disposing of connection.", connectionId);

        if (managementChannel != null) {
            managementChannel.close();
        }

        super.dispose();
    }

    @Override
    protected AmqpSession createSession(String sessionName, Session session, SessionHandler handler) {
        return new EventHubReactorSession(this, session, handler, sessionName, reactorProvider,
            handlerProvider, getClaimsBasedSecurityNode(), tokenManagerProvider, retryOptions, messageSerializer);
    }

    private synchronized ManagementChannel getOrCreateManagementChannel() {
        if (managementChannel == null) {
            managementChannel = new ManagementChannel(
                createRequestResponseChannel(MANAGEMENT_SESSION_NAME, MANAGEMENT_LINK_NAME, MANAGEMENT_ADDRESS),
                eventHubName, tokenCredential, tokenManagerProvider, this.messageSerializer, scheduler);
        }

        return managementChannel;
    }
}
