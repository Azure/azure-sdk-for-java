// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class ServerMessageEvent {

    private final ServerDataMessage message;

    public ServerMessageEvent(ServerDataMessage message) {
        this.message = message;
    }

    public ServerDataMessage getMessage() {
        return message;
    }
}
