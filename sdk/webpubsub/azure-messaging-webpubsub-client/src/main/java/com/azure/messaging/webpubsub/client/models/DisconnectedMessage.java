// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.models;

import com.azure.core.annotation.Immutable;

@Immutable
public final class DisconnectedMessage extends WebPubSubMessage {

    private final String reason;

    public DisconnectedMessage(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
