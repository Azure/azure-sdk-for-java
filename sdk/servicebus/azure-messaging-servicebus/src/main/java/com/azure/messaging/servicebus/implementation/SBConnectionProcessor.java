// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpChannelProcessor;
import com.azure.core.amqp.implementation.RetryUtil;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

public class SBConnectionProcessor extends AmqpChannelProcessor<ServiceBusAmqpConnection> {

    private final String fullyQualifiedNamespace;
    private final String entityPath;
    private final AmqpRetryOptions retryOptions;

    public SBConnectionProcessor(String fullyQualifiedNamespace, String entityPath,
                                       AmqpRetryOptions retryOptions) {
        super("", entityPath, channel -> channel.getEndpointStates(),
            RetryUtil.getRetryPolicy(retryOptions), new ClientLogger(SBConnectionProcessor.class));

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
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
    public String getEntityPath() {
        return entityPath;
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
