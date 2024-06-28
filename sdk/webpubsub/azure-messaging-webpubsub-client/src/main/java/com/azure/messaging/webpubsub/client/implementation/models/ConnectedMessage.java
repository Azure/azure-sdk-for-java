// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

public final class ConnectedMessage extends WebPubSubMessage {

    private final String connectionId;
    private String userId;
    private String reconnectionToken;

    public ConnectedMessage(String connectionId) {
        this.connectionId = connectionId;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getUserId() {
        return userId;
    }

    public ConnectedMessage setUserId(String userId) {
        this.userId = userId;
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
