// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.engine.EndpointState;

/**
 * Exception thrown when the {@link RequestResponseChannel} is used after it's been disposed (closed).
 */
public final class RequestResponseChannelClosedException extends IllegalStateException {
    private static final String DISPOSED = "Cannot send a message when request response channel is disposed.";

    /**
     * Creates an instance of {@link RequestResponseChannelClosedException}.
     *
     * @param connectionId Identifier for the connection.
     */
    public RequestResponseChannelClosedException(String connectionId) {
        super(DISPOSED + " ConnectionId:" + connectionId);
    }

    /**
     * Creates an instance of {@link RequestResponseChannelClosedException}.
     *
     * @param connectionId Identifier for the connection.
     * @param message Error message.
     */
    public RequestResponseChannelClosedException(String connectionId, String message) {
        super(DISPOSED + " ConnectionId:" + connectionId + " " + message);
    }

    /**
     * Creates an instance of {@link RequestResponseChannelClosedException}.
     *
     * @param connectionId Identifier for the connection.
     * @param sendLinkState The state of the send link.
     * @param receiveLinkState The state of the receive link.
     */
    public RequestResponseChannelClosedException(String connectionId, EndpointState sendLinkState,
        EndpointState receiveLinkState) {
        super(
            DISPOSED + " ConnectionId:" + connectionId + " LinkState:(" + sendLinkState + "," + receiveLinkState + ")");
    }
}
