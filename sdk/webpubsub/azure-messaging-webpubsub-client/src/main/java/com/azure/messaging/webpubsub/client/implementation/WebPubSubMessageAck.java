// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation;

import com.azure.messaging.webpubsub.client.models.WebPubSubMessage;

public abstract class WebPubSubMessageAck extends WebPubSubMessage {
    private Long ackId;

    public long getAckId() {
        return ackId;
    }

    public WebPubSubMessageAck setAckId(Long ackId) {
        this.ackId = ackId;
        return this;
    }
}
