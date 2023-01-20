// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class ConnectedEvent {

    private final String connectionId;
    private final String userId;

    public ConnectedEvent(String connectionId, String userId) {
        this.connectionId = connectionId;
        this.userId = userId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getUserId() {
        return userId;
    }
}
