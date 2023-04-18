// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public enum WebPubSubClientState {

    // connection close
    // either change to STOPPED, or to RECOVERING (if reliable), or to RECONNECTING (when autoReconnect=true)
    DISCONNECTED,

    // on start(), connectToServer called but not completed, either change to STOPPED if error, or to CONNECTED
    CONNECTING,

    // connection open
    CONNECTED,

    // recovering (reliable) the connection, still deemed as in the same connection
    RECOVERING,

    // client stopped
    STOPPED,

    // client closed, resource released
    CLOSED,

    // DISCONNECTED then automatically reconnecting (when autoReconnect=true)
    RECONNECTING,

    // close (stop on client) called but not completed
    STOPPING,
}
