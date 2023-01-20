// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.messaging.webpubsub.client.models.WebPubSubMessage;

public final class ConnectedMessage extends WebPubSubMessage {

    private String userId;
    private String connectionId;
    private String reconnectionToken;

    public String getUserId() {
        return userId;
    }

    public ConnectedMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public ConnectedMessage setConnectionId(String connectionId) {
        this.connectionId = connectionId;
        return this;
    }

    public String getReconnectionToken() {
        return reconnectionToken;
    }

    public ConnectedMessage setReconnectionToken(String reconnectionToken) {
        this.reconnectionToken = reconnectionToken;
        return this;
    }
}
