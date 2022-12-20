// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

public class ConnectedEvent {

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
