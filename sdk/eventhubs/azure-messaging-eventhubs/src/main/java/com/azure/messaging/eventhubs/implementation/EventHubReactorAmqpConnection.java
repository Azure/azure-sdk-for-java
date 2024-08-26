// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.implementation.AmqpLinkProvider;
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

import java.util.HashMap;
import java.util.Map;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.CLIENT_IDENTIFIER_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.CONNECTION_ID_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.ENTITY_PATH_KEY;

/**
 * A proton-j AMQP connection to an Azure Event Hub instance. Adds additional support for management operations.
 */
public class EventHubReactorAmqpConnection extends ReactorConnection implements EventHubAmqpConnection {
    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";
    private static final String MANAGEMENT_ADDRESS = "$management";

    private final ClientLogger logger;
    private final TokenCredential tokenCredential;
    private final String connectionId;
    private final ReactorProvider reactorProvider;
    private final ReactorHandlerProvider handlerProvider;
    private final AmqpLinkProvider linkProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final AmqpRetryOptions retryOptions;
    private final MessageSerializer messageSerializer;
    private final Scheduler scheduler;
    private final String eventHubName;
    private final boolean isV2;

    private volatile ManagementChannel managementChannel;

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
     */
    public EventHubReactorAmqpConnection(String connectionId, ConnectionOptions connectionOptions, String eventHubName,
        ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider, AmqpLinkProvider linkProvider,
        TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer, boolean isV2) {
        super(connectionId, connectionOptions, reactorProvider, handlerProvider, linkProvider, tokenManagerProvider,
            messageSerializer, SenderSettleMode.SETTLED, ReceiverSettleMode.SECOND, isV2);
        this.connectionId = connectionId;
        this.reactorProvider = reactorProvider;
        this.handlerProvider = handlerProvider;
        this.linkProvider = linkProvider;
        this.tokenManagerProvider = tokenManagerProvider;
        this.messageSerializer = messageSerializer;
        this.eventHubName = eventHubName;
        this.isV2 = isV2;
        this.retryOptions = connectionOptions.getRetry();
        this.tokenCredential = connectionOptions.getTokenCredential();
        this.scheduler = connectionOptions.getScheduler();

        Map<String, Object> loggingContext = new HashMap<>(1);
        loggingContext.put(CONNECTION_ID_KEY, connectionId);
        this.logger = new ClientLogger(EventHubReactorAmqpConnection.class, loggingContext);
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
     * @param clientIdentifier The identifier of client.
     * @return A new or existing send link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions, String clientIdentifier) {
        return createSession(entityPath).cast(EventHubSession.class)
            .flatMap(session -> {
                logger.atVerbose()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(CLIENT_IDENTIFIER_KEY, clientIdentifier)
                    .log("Get or create producer.");
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);
                return session.createProducer(linkName, entityPath, retryOptions.getTryTimeout(), retryPolicy, clientIdentifier);
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
     * @param clientIdentifier The identifier of client.
     * @return A new or existing receive link that is connected to the given {@code entityPath}.
     */
    @Override
    public Mono<AmqpReceiveLink> createReceiveLink(String linkName, String entityPath, EventPosition eventPosition,
        ReceiveOptions options, String clientIdentifier) {
        return createSession(entityPath).cast(EventHubSession.class)
            .flatMap(session -> {
                logger.atVerbose()
                    .addKeyValue(ENTITY_PATH_KEY, entityPath)
                    .addKeyValue(CLIENT_IDENTIFIER_KEY, clientIdentifier)
                    .log("Get or create consumer.");
                final AmqpRetryPolicy retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

                return session.createConsumer(linkName, entityPath, retryOptions.getTryTimeout(), retryPolicy,
                    eventPosition, options, clientIdentifier);
            });
    }

    @Override
    public void dispose() {
        if (isDisposed()) {
            return;
        }

        if (managementChannel != null) {
            managementChannel.close();
        }

        super.dispose();
    }

    @Override
    protected AmqpSession createSession(String sessionName, Session session, SessionHandler handler) {
        return new EventHubReactorSession(this, session, handler, sessionName, reactorProvider,
            handlerProvider, linkProvider, getClaimsBasedSecurityNode(), tokenManagerProvider, retryOptions, messageSerializer, isV2);
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
