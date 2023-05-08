// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

/**
 * The event when connected.
 */
@Immutable
public final class ConnectedEvent {

    private final String connectionId;
    private final String userId;

    /**
     * Creates a new instance of ConnectedEvent.
     *
     * @param connectionId the connection ID.
     * @param userId the user ID.
     */
    public ConnectedEvent(String connectionId, String userId) {
        this.connectionId = connectionId;
        this.userId = userId;
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
     * Gets the user ID.
     *
     * @return the user ID.
     */
    public String getUserId() {
        return userId;
    }
}
