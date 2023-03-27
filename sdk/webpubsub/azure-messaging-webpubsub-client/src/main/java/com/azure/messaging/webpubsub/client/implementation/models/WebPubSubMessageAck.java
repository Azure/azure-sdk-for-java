// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.fasterxml.jackson.annotation.JsonGetter;

public abstract class WebPubSubMessageAck extends WebPubSubMessage {
    private long ackId = 0L;

    @JsonGetter
    public long getAckId() {
        return ackId;
    }

    public WebPubSubMessageAck setAckId(long ackId) {
        this.ackId = ackId;
        return this;
    }
}
