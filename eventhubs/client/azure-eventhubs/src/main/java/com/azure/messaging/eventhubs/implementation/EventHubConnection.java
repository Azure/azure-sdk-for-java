// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpConnection;
import reactor.core.publisher.Mono;

/**
 * A connection to a specific Event Hub resource in Azure Event Hubs.
 */
public interface EventHubConnection extends AmqpConnection {
    /**
     * Gets the management node for fetching metadata about the Event Hub and performing management operations.
     *
     * @return A Mono that completes with a session to the Event Hub's management node.
     */
    Mono<EventHubManagementNode> getManagementNode();
}
