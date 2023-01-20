// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public enum WebPubSubClientState {

    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECOVERING,
    STOPPED,
    CLOSED
}
