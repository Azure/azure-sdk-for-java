// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import reactor.core.publisher.Flux;

/**
 * Represents a unidirectional AMQP link.
 */
public interface AmqpLink extends AutoCloseable {
    /**
     * Gets the name of the link.
     *
     * @return The name of the link.
     */
    String getLinkName();

    /**
     * The remote endpoint path this link is connected to.
     *
     * @return The remote endpoint path this link is connected to.
     */
    String getEntityPath();

    /**
     * The host name of the message broker that this link that is connected to.
     *
     * @return The host name of the message broker that this link that is connected to.
     */
    String getHostname();

    /**
     * Gets the endpoint states for the AMQP link. {@link AmqpException AmqpExceptions} that occur on the link are
     * reported in the connection state. When the stream terminates, the link is closed.
     *
     * @return A stream of endpoint states for the AMQP link.
     */
    Flux<AmqpEndpointState> getEndpointStates();

    /**
     * Closes the AMQP link.
     */
    @Override
    void close();
}
