// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp;

import com.azure.core.amqp.exception.AmqpException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Represents a TCP connection between the client and a service that uses the AMQP protocol.
 */
public interface AmqpConnection extends AutoCloseable {
    /**
     * Gets the connection identifier.
     *
     * @return The connection identifier.
     */
    String getId();

    /**
     * Gets the fully qualified namespace for the AMQP connection.
     *
     * @return The hostname for the AMQP connection.
     */
    String getFullyQualifiedNamespace();

    /**
     * Gets the maximum frame size for the connection.
     *
     * @return The maximum frame size for the connection.
     */
    int getMaxFrameSize();

    /**
     * Gets the connection properties.
     *
     * @return Properties associated with this connection.
     */
    Map<String, Object> getConnectionProperties();

    /**
     * Gets the claims-based security (CBS) node that authorizes access to resources.
     *
     * @return Provider that authorizes access to AMQP resources.
     */
    Mono<ClaimsBasedSecurityNode> getClaimsBasedSecurityNode();

    /**
     * Creates a new session with the given session name.
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

    /**
     * Gets the endpoint states for the AMQP connection. {@link AmqpException AmqpExceptions} that occur on the link are
     * reported in the connection state. When the stream terminates, the connection is closed.
     *
     * @return A stream of endpoint states for the AMQP connection.
     */
    Flux<AmqpEndpointState> getEndpointStates();

    /**
     * Gets any shutdown signals that occur in the AMQP endpoint.
     *
     * @return A stream of shutdown signals that occur in the AMQP endpoint.
     */
    Flux<AmqpShutdownSignal> getShutdownSignals();

    /**
     * Closes the AMQP connection.
     */
    @Override
    void close();
}
