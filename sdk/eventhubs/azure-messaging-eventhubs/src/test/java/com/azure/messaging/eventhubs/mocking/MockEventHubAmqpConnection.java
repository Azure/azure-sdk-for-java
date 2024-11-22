// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.mocking;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpSession;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.ClaimsBasedSecurityNode;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.messaging.eventhubs.implementation.EventHubAmqpConnection;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Mock implementation of {@link EventHubAmqpConnection}.
 */
public class MockEventHubAmqpConnection implements EventHubAmqpConnection {
    @Override
    public Mono<EventHubManagementNode> getManagementNode() {
        return null;
    }

    @Override
    public Mono<AmqpSendLink> createSendLink(String linkName, String entityPath, AmqpRetryOptions retryOptions,
        String clientIdentifier) {
        return null;
    }

    @Override
    public Mono<AmqpReceiveLink> createReceiveLink(String linkName, String entityPath, EventPosition eventPosition,
        ReceiveOptions options, String clientIdentifier) {
        return null;
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getFullyQualifiedNamespace() {
        return "";
    }

    @Override
    public int getMaxFrameSize() {
        return 0;
    }

    @Override
    public Map<String, Object> getConnectionProperties() {
        return null;
    }

    @Override
    public Mono<ClaimsBasedSecurityNode> getClaimsBasedSecurityNode() {
        return null;
    }

    @Override
    public Mono<AmqpSession> createSession(String s) {
        return null;
    }

    @Override
    public boolean removeSession(String s) {
        return false;
    }

    @Override
    public Flux<AmqpEndpointState> getEndpointStates() {
        return null;
    }

    @Override
    public Flux<AmqpShutdownSignal> getShutdownSignals() {
        return null;
    }

    @Override
    public void dispose() {

    }
}
