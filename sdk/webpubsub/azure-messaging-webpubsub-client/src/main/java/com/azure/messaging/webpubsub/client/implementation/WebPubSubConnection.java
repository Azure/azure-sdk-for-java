// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The class is a logical representation of WebPubSub connection.
 * <p>
 * For reliable protocol, if recover succeeded, it is a same connection.
 */
public class WebPubSubConnection {

    private String connectionId;
    private String reconnectionToken;

    private final SequenceAckId sequenceAckId = new SequenceAckId();

    private final AtomicBoolean disconnected = new AtomicBoolean(false);

    public String getConnectionId() {
        return connectionId;
    }

    public String getReconnectionToken() {
        return reconnectionToken;
    }

    public SequenceAckId getSequenceAckId() {
        return sequenceAckId;
    }

    // ConnectedMessage from server
    public void updateForConnected(String connectionId, String reconnectionToken, Runnable action) {
        boolean newConnectionId = this.connectionId == null;

        this.connectionId = connectionId;
        this.reconnectionToken = reconnectionToken;

        if (newConnectionId) {
            action.run();
        }
    }

    // Either DisconnectedMessage from server,
    // or client find session closed and then regard the connection as closed (when not a reliable protocol)
    public void updateForDisconnected(Runnable action) {
        if (!this.disconnected.getAndSet(true)) {
            action.run();
        }
        // consider to let client remove the instance, if connection is closed
    }
}
