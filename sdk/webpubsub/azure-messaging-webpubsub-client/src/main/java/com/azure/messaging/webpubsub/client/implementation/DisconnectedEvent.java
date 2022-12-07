// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

public class DisconnectedEvent {

    private final String reason;

    public DisconnectedEvent(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
