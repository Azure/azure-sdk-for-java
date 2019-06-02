// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.Map;

/**
 * Represents a TCP connection between the client and a service that uses the AMQP protocol.
 */
public interface AmqpConnection extends StateNotifier, Closeable {
    /**
     * Gets the connection identifier.
     *
     * @return The connection identifier.
     */
    String getIdentifier();

    /**
     * Gets the host for the AMQP connection.
     *
     * @return The host for the AMQP connection.
     */
    String getHost();

    /**
     * Gets the maximum framesize for the connection.
     *
     * @return The maximum frame size for the connection.
     */
    int getMaxFrameSize();

    /**
     * Gets the client properties.
     *
     * @return Client properties associated with this connection.
     */
    Map<String, Object> getClientProperties();

    /**
     * Gets the claims-based security (CBS) node that authorizes access to resources.
     *
     * @return Provider that authorizes access to AMQP resources.
     */
    Mono<CBSNode> getCBSNode();

    /**
     * Creates a new session with the given entity path.
     *
     * @param sessionName Name of the session.
     * @return The AMQP session that was created.
     */
    Mono<AmqpSession> createSession(String sessionName);

    /**
     * Removes a session with the {@code sessionName} from the AMQP connection.
     *
     * @param sessionName Name of the session to remove.
     * @return {@code true} if a session with the name was removed; {@code false} otherwise.
     */
    boolean removeSession(String sessionName);
}
