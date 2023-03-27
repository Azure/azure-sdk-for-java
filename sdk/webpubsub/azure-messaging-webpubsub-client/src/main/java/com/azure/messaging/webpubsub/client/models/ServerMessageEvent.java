// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The event for ServerDataMessage.
 */
@Immutable
public final class ServerMessageEvent {

    private final ServerDataMessage message;

    /**
     * Creates a new instance of ServerMessageEvent.
     *
     * @param message the ServerDataMessage.
     */
    public ServerMessageEvent(ServerDataMessage message) {
        this.message = message;
    }

    /**
     * Gets the ServerDataMessage.
     *
     * @return the ServerDataMessage.
     */
    public ServerDataMessage getMessage() {
        return message;
    }
}
