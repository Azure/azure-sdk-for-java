// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.engine.EndpointState;

/**
 * Exception thrown when the {@link RequestResponseChannel} is used after it's been disposed (closed).
 */
public final class RequestResponseChannelClosedException extends IllegalStateException {
    private static final String DISPOSED = "Cannot send a message when request response channel is disposed.";

    public RequestResponseChannelClosedException(String connectionId) {
        super(DISPOSED + " ConnectionId:" + connectionId);
    }

    public RequestResponseChannelClosedException(String connectionId, String message) {
        super(DISPOSED + " ConnectionId:" + connectionId + " " + message);
    }

    public RequestResponseChannelClosedException(String connectionId, EndpointState sendLinkState, EndpointState receiveLinkState) {
        super(DISPOSED + " ConnectionId:" + connectionId + " LinkState:(" + sendLinkState + "," + receiveLinkState + ")");
    }
}
