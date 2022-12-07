// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client;

public class DisconnectedEvent {

    private final String connectionId;
    private final String reason;

    DisconnectedEvent(String connectionId, String reason) {
        this.connectionId = connectionId;
        this.reason = reason;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public String getReason() {
        return reason;
    }
}
