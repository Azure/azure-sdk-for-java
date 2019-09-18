// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import org.apache.qpid.proton.engine.BaseHandler;
import reactor.core.publisher.Mono;

/**
 * A proton-j AMQP connection to an Azure Event Hub instance. Adds additional support for management operations.
 */
public class EventHubReactorConnection extends ReactorConnection implements EventHubConnection {
    private static final String MANAGEMENT_SESSION_NAME = "mgmt-session";
    private static final String MANAGEMENT_LINK_NAME = "mgmt";
    private static final String MANAGEMENT_ADDRESS = "$management";

    private final Mono<EventHubManagementNode> managementChannelMono;

    /**
     * Creates a new AMQP connection that uses proton-j.
     *
     * @param connectionId Identifier for the connection.
     * @param connectionOptions A set of options used to create the AMQP connection.
     * @param reactorProvider Provides proton-j Reactor instances.
     * @param handlerProvider Provides {@link BaseHandler} to listen to proton-j reactor events.
     * @param tokenManagerProvider Provides a token manager for authorizing with CBS node.
     * @param mapper Maps responses from {@link EventHubManagementNode}.
     */
    public EventHubReactorConnection(String connectionId, ConnectionOptions connectionOptions,
                                     ReactorProvider reactorProvider, ReactorHandlerProvider handlerProvider,
                                     TokenManagerProvider tokenManagerProvider, ManagementResponseMapper mapper) {
        super(connectionId, connectionOptions, reactorProvider, handlerProvider, tokenManagerProvider);

        this.managementChannelMono = getReactorConnection().then(
            Mono.fromCallable(() -> {
                return (EventHubManagementNode) new ManagementChannel(
                    createRequestResponseChannel(MANAGEMENT_SESSION_NAME, MANAGEMENT_LINK_NAME, MANAGEMENT_ADDRESS),
                    connectionOptions.getEventHubName(), connectionOptions.getTokenCredential(), tokenManagerProvider,
                    mapper);
            }))
            .cache();
    }

    @Override
    public Mono<EventHubManagementNode> getManagementNode() {
        return managementChannelMono;
    }
}
