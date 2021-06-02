// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.util.AsyncCloseable;
import reactor.core.publisher.Mono;

/**
 * Management node.
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
}
