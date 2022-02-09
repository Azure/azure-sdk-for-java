// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.DeliveryOutcome;
import com.azure.core.util.AsyncCloseable;
import reactor.core.publisher.Mono;

/**
 * An AMQP endpoint that allows users to perform management and metadata operations on it.
 */
public interface AmqpManagementNode extends AsyncCloseable {
    /**
     * Sends a message to the management node.
     *
     * @param message Message to send.
     *
     * @return Response from management node.
     */
    Mono<AmqpAnnotatedMessage> send(AmqpAnnotatedMessage message);

    /**
     * Sends a message to the management node and associates the {@code deliveryOutcome} with that message.
     *
     * @param message Message to send.
     * @param deliveryOutcome Delivery outcome to associate with the message.
     *
     * @return Response from management node.
     */
    Mono<AmqpAnnotatedMessage> send(AmqpAnnotatedMessage message, DeliveryOutcome deliveryOutcome);
}
