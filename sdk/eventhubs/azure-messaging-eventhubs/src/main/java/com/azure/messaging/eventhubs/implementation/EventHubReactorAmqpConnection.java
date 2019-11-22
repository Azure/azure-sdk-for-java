// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.ReactorConnection;
import com.azure.core.amqp.implementation.ReactorHandlerProvider;
import com.azure.core.amqp.implementation.ReactorProvider;
import com.azure.core.amqp.implementation.TokenManagerProvider;
import com.azure.core.amqp.implementation.handler.SessionHandler;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Session;
import reactor.core.publisher.Mono;

/**
 * A proton-j AMQP connection to an Azure Event Hub instance. Adds additional support for management operations.
 */
public class EventHubReactorAmqpConnection extends ReactorConnection implements EventHubAmqpConnection {
    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";
    private static final String MANAGEMENT_ADDRESS = "$management";

    private final Mono<EventHubManagementNode> managementChannelMono;
    private final ReactorProvider reactorProvider;
    private final ReactorHandlerProvider handlerProvider;
    private final TokenManagerProvider tokenManagerProvider;
    private final AmqpRetryOptions retryOptions;
    private final MessageSerializer messageSerializer;

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
    public EventHubReactorAmqpConnection(String connectionId, ConnectionOptions connectionOptions,
                                     ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider,
                                     TokenManagerProvider tokenManagerProvider, MessageSerializer messageSerializer) {
        super(connectionId, connectionOptions, reactorProvider, handlerProvider, tokenManagerProvider,
            messageSerializer);
        this.reactorProvider = reactorProvider;
        this.handlerProvider = handlerProvider;
        this.tokenManagerProvider = tokenManagerProvider;
        this.retryOptions = connectionOptions.getRetry();
        this.messageSerializer = messageSerializer;

        this.managementChannelMono = getReactorConnection().then(
            Mono.fromCallable(() -> {
                return (EventHubManagementNode) new ManagementChannel(
                    createRequestResponseChannel(MANAGEMENT_SESSION_NAME, MANAGEMENT_LINK_NAME, MANAGEMENT_ADDRESS),
                    connectionOptions.getEntityPath(), connectionOptions.getTokenCredential(),
                    this.tokenManagerProvider, this.messageSerializer);
            }))
            .cache();
    }

    @Override
    public Mono<EventHubManagementNode> getManagementNode() {
        return managementChannelMono;
    }

    @Override
    protected AmqpSession createSession(String sessionName, Session session, SessionHandler handler) {
        return new EventHubReactorSession(session, handler, sessionName, reactorProvider, handlerProvider,
            getClaimsBasedSecurityNode(), tokenManagerProvider, retryOptions.getTryTimeout(), messageSerializer);
    }
}
