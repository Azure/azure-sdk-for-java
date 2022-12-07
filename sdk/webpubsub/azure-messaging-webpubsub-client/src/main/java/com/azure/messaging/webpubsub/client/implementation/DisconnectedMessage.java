// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.messaging.webpubsub.client.WebPubSubMessage;

public class DisconnectedMessage extends WebPubSubMessage {

    private String reason;

    public String getReason() {
        return reason;
    }

    public DisconnectedMessage setReason(String reason) {
        this.reason = reason;
        return this;
    }
}
