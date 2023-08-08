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
    private final String reason;

    /**
     * Creates a new instance of DisconnectedEvent.
     *
     * @param connectionId the connection ID.
     * @param reason the reason of disconnect.
     */
    public DisconnectedEvent(String connectionId, String reason) {
        this.connectionId = connectionId;
        this.reason = reason;
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
     * Gets the reason of disconnect.
     *
     * @return the reason of disconnect.
     */
    public String getReason() {
        return reason;
    }
}
