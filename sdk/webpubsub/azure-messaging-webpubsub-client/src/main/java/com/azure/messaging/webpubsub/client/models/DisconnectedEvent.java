// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

public class DisconnectedEvent {

    private final String connectionId;
    private final DisconnectedMessage disconnectedMessage;

    public DisconnectedEvent(String connectionId, DisconnectedMessage disconnectedMessage) {
        this.connectionId = connectionId;
        this.disconnectedMessage = disconnectedMessage;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public DisconnectedMessage getDisconnectedMessage() {
        return disconnectedMessage;
    }
}
