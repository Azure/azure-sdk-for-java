// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public enum WebPubSubClientState {

    // session disconnected, either change to STOPPED, or to RECOVERING (if reliable), or to CONNECTING (when autoReconnect=true)
    DISCONNECTED,

    // connectToServer called but not completed, either change to STOPPED if error, or to CONNECTED
    CONNECTING,

    // session connected
    CONNECTED,

    // recovering (reliable)
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
