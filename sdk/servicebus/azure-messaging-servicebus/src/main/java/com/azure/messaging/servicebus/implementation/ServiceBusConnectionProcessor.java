// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpChannelProcessor;
import com.azure.core.amqp.implementation.RetryUtil;
import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Responsible for emitting an active {@link ServiceBusAmqpConnection} to downstream subscribers. Fetches a new
 * connection when the existing connection closes.
 */
public class ServiceBusConnectionProcessor extends AmqpChannelProcessor<ServiceBusAmqpConnection> {

    private final String fullyQualifiedNamespace;
    private final AmqpRetryOptions retryOptions;

    public ServiceBusConnectionProcessor(String fullyQualifiedNamespace, AmqpRetryOptions retryOptions) {
        super(fullyQualifiedNamespace, "N/A", channel -> channel.getEndpointStates(),
            RetryUtil.getRetryPolicy(retryOptions), new ClientLogger(ServiceBusConnectionProcessor.class));

        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
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
     * Gets the retry options associated with the Service Bus connection.
     *
     * @return The retry options associated with the Service Bus connection.
     */
    public AmqpRetryOptions getRetryOptions() {
        return retryOptions;
    }
}
