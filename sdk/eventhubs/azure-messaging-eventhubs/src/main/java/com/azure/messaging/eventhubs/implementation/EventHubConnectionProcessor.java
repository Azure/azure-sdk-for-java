// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpChannelProcessor;
import com.azure.core.amqp.implementation.RetryUtil;

import java.util.Collections;
import java.util.Objects;

import static com.azure.messaging.eventhubs.implementation.ClientConstants.ENTITY_PATH_KEY;

/**
 * Subscribes to an upstream Mono that creates {@link EventHubAmqpConnection} then publishes the created connection
 * until it closes then recreates it.
 */
public class EventHubConnectionProcessor extends AmqpChannelProcessor<EventHubAmqpConnection> {
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final AmqpRetryOptions retryOptions;

    public EventHubConnectionProcessor(String fullyQualifiedNamespace, String eventHubName, AmqpRetryOptions retryOptions) {
        super(fullyQualifiedNamespace, channel -> channel.getEndpointStates(),
            RetryUtil.getRetryPolicy(retryOptions), Collections.singletonMap(ENTITY_PATH_KEY, eventHubName));

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.eventHubName = Objects.requireNonNull(eventHubName, "'eventHubName' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
    }

    /**
     * Gets the fully qualified namespace for the connection.
     *
     * @return The fully qualified namespace this is connection.
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the name of the Event Hub.
     *
     * @return The name of the Event Hub.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Gets the retry options associated with the Event Hub connection.
     *
     * @return The retry options associated with the Event Hub connection.
     */
    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }
}
