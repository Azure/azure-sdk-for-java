// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The event when disconnected.
 */
@Immutable
public final class DisconnectedEvent {

    private final String connectionId;
    private final DisconnectedMessage disconnectedMessage;

    /**
     * Creates a new instance of DisconnectedEvent.
     *
     * @param connectionId the connection ID.
     * @param disconnectedMessage the disconnected message.
     */
    public DisconnectedEvent(String connectionId, DisconnectedMessage disconnectedMessage) {
        this.connectionId = connectionId;
        this.disconnectedMessage = disconnectedMessage;
    }

    /**
     * Gets the connection ID.
     *
     * @return the connection ID.
     */
    public String getConnectionId() {
        return connectionId;
    }

    /**
     * Gets the disconnected message.
     *
     * @return the disconnected message.
     */
    public DisconnectedMessage getDisconnectedMessage() {
        return disconnectedMessage;
    }
}
