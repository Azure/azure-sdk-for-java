// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.engine.EndpointState;

/**
 * Exception thrown when the {@link RequestResponseChannel} is used after it's been disposed (closed).
 */
public final class RequestResponseChannelClosedException extends IllegalStateException {

    public RequestResponseChannelClosedException() {
        super("Cannot send a message when request response channel is disposed.");
    }

    public RequestResponseChannelClosedException(EndpointState sendLinkState, EndpointState receiveLinkState) {
        super("Cannot send a message when request response channel is disposed. LinkState: (" + sendLinkState + "," + receiveLinkState + ")");
    }
}
